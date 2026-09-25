package xyz.angames.astolfoclient.client.protection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.client.MinecraftClient;
import xyz.angames.astolfoclient.client.util.ModSounds;

@Environment(EnvType.CLIENT)
public class ClientProtectionManager {
   private static final ClientProtectionManager INSTANCE = new ClientProtectionManager();
   private final List<ClientProtectionManager.CrashAlert> activeAlerts = new CopyOnWriteArrayList<>();
   private long lastSoundTime = 0L;
   private static final int ADVANCEMENT_QUEUE_LIMIT = 1600;
   private static final long ADVANCEMENT_QUIET_PERIOD_MS = 3000L;
   private final Queue<Packet<?>> pendingAdvancements = new ConcurrentLinkedQueue<>();
   private volatile long lastAdvancementPacketTime = 0L;

   public static ClientProtectionManager getInstance() {
      return INSTANCE;
   }

   public static void init() {
   }

   public List<ClientProtectionManager.CrashAlert> getActiveAlerts() {
      return this.activeAlerts;
   }

   public void onCrashBlocked(String title, String details) {
      long now = System.currentTimeMillis();
      if (now - this.lastSoundTime > 400L) {
         this.lastSoundTime = now;
         ModSounds.playCrashDetectionSound();
      }

      boolean hasDuplicate = false;

      for (ClientProtectionManager.CrashAlert alert : this.activeAlerts) {
         if (alert.title.equalsIgnoreCase(title) && now - alert.timestamp < 1500L) {
            hasDuplicate = true;
            break;
         }
      }

      if (!hasDuplicate) {
         this.activeAlerts.add(new ClientProtectionManager.CrashAlert(title, details, now));

         while (this.activeAlerts.size() > 3) {
            this.activeAlerts.remove(0);
         }
      }

      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc != null && mc.inGameHud != null && mc.inGameHud.getChatHud() != null) {
         mc.inGameHud.getChatHud().addMessage(Text.literal("§c[Protection] §fBlocked crash exploit: §e" + title + " §7(" + details + ")"));
      }
   }

   public boolean isMaliciousPacket(Packet<?> packet) {
      if (packet == null) {
         return false;
      }

      if (packet instanceof ExplosionS2CPacket explosion) {
         Vec3d center = explosion.comp_2883();
         if (center == null
            || isInvalidDouble(center.x)
            || isInvalidDouble(center.y)
            || isInvalidDouble(center.z)
            || Math.abs(center.x) > 3.0E7
            || Math.abs(center.y) > 3.0E7
            || Math.abs(center.z) > 3.0E7) {
            this.onCrashBlocked("Explosion Crash", "Center coordinates outside world boundary");
            return true;
         }

         if (explosion.comp_2884().isPresent()) {
            Vec3d kb = (Vec3d)explosion.comp_2884().get();
            if (kb == null
               || isInvalidDouble(kb.x)
               || isInvalidDouble(kb.y)
               || isInvalidDouble(kb.z)
               || Math.abs(kb.x) > 1000000.0
               || Math.abs(kb.y) > 1000000.0
               || Math.abs(kb.z) > 1000000.0) {
               this.onCrashBlocked("Explosion Crash", "Malformed knockback vector");
               return true;
            }
         }
      }

      if (packet instanceof ParticleS2CPacket particle) {
         if (isInvalidDouble(particle.getX())
            || isInvalidDouble(particle.getY())
            || isInvalidDouble(particle.getZ())
            || Math.abs(particle.getX()) > 3.0E7
            || Math.abs(particle.getY()) > 3.0E7
            || Math.abs(particle.getZ()) > 3.0E7) {
            this.onCrashBlocked("Particle Exploit", "Coordinates out of bounds");
            return true;
         }

         if (particle.getCount() > 1000 || particle.getCount() < 0) {
            this.onCrashBlocked("Particle Exploit", "Invalid count: " + particle.getCount());
            return true;
         }

         if (Float.isNaN(particle.getSpeed()) || Float.isInfinite(particle.getSpeed()) || Math.abs(particle.getSpeed()) > 1000.0F) {
            this.onCrashBlocked("Particle Exploit", "Malformed particle speed");
            return true;
         }

         if (Float.isNaN(particle.getOffsetX())
            || Float.isNaN(particle.getOffsetY())
            || Float.isNaN(particle.getOffsetZ())
            || Math.abs(particle.getOffsetX()) > 1000.0F
            || Math.abs(particle.getOffsetY()) > 1000.0F
            || Math.abs(particle.getOffsetZ()) > 1000.0F) {
            this.onCrashBlocked("Particle Exploit", "Malformed particle offset");
            return true;
         }
      }

      if (packet instanceof AdvancementUpdateS2CPacket) {
         if (this.pendingAdvancements.size() >= 1600) {
            this.pendingAdvancements.poll();
            this.onCrashBlocked("Advancement Flood", "Queue exceeded limit (1600)");
         }

         this.pendingAdvancements.add(packet);
         this.lastAdvancementPacketTime = System.currentTimeMillis();
         return true;
      } else {
         if (packet instanceof EntityVelocityUpdateS2CPacket vel) {
            double vx = Math.abs(vel.getVelocityX() / 8000.0);
            double vy = Math.abs(vel.getVelocityY() / 8000.0);
            double vz = Math.abs(vel.getVelocityZ() / 8000.0);
            if (isInvalidDouble(vx) || isInvalidDouble(vy) || isInvalidDouble(vz) || vx > 100000.0 || vy > 100000.0 || vz > 100000.0) {
               this.onCrashBlocked("Velocity Exploit", "Extreme entity velocity values");
               return true;
            }
         }

         if (packet instanceof PlaySoundS2CPacket sound) {
            if (isInvalidDouble(sound.getX())
               || isInvalidDouble(sound.getY())
               || isInvalidDouble(sound.getZ())
               || Math.abs(sound.getX()) > 3.0E7
               || Math.abs(sound.getY()) > 3.0E7
               || Math.abs(sound.getZ()) > 3.0E7) {
               this.onCrashBlocked("Sound Exploit", "Coordinates out of bounds");
               return true;
            }

            if (Float.isNaN(sound.getVolume())
               || Float.isNaN(sound.getPitch())
               || sound.getVolume() < 0.0F
               || sound.getVolume() > 100.0F
               || sound.getPitch() < 0.0F
               || sound.getPitch() > 100.0F) {
               this.onCrashBlocked("Sound Exploit", "Invalid sound volume/pitch");
               return true;
            }
         }

         if (packet instanceof EntitySpawnS2CPacket spawn) {
            if (isInvalidDouble(spawn.getX())
               || isInvalidDouble(spawn.getY())
               || isInvalidDouble(spawn.getZ())
               || Math.abs(spawn.getX()) > 3.0E7
               || Math.abs(spawn.getY()) > 3.0E7
               || Math.abs(spawn.getZ()) > 3.0E7) {
               this.onCrashBlocked("Spawn Exploit", "Entity coordinates out of bounds");
               return true;
            }

            if (isInvalidDouble(spawn.getVelocityX())
               || isInvalidDouble(spawn.getVelocityY())
               || isInvalidDouble(spawn.getVelocityZ())
               || Math.abs(spawn.getVelocityX()) > 100000.0
               || Math.abs(spawn.getVelocityY()) > 100000.0
               || Math.abs(spawn.getVelocityZ()) > 100000.0) {
               this.onCrashBlocked("Spawn Exploit", "Entity spawned with extreme velocity");
               return true;
            }
         }

         if (packet instanceof PlayerPositionLookS2CPacket teleport) {
            PlayerPosition change = teleport.comp_3228();
            if (change != null) {
               Vec3d pos = change.comp_3148();
               Vec3d delta = change.comp_3149();
               if (pos == null
                  || isInvalidDouble(pos.x)
                  || isInvalidDouble(pos.y)
                  || isInvalidDouble(pos.z)
                  || Math.abs(pos.x) > 3.0E7
                  || Math.abs(pos.y) > 3.0E7
                  || Math.abs(pos.z) > 3.0E7
                  || delta != null
                     && (
                        isInvalidDouble(delta.x)
                           || isInvalidDouble(delta.y)
                           || isInvalidDouble(delta.z)
                           || Math.abs(delta.x) > 1000000.0
                           || Math.abs(delta.y) > 1000000.0
                           || Math.abs(delta.z) > 1000000.0
                     )
                  || Float.isNaN(change.comp_3150())
                  || Float.isInfinite(change.comp_3150())
                  || Float.isNaN(change.comp_3151())
                  || Float.isInfinite(change.comp_3151())) {
                  String coordsStr = pos != null ? String.format("X: %.1f, Y: %.1f, Z: %.1f", pos.x, pos.y, pos.z) : "null";
                  this.onCrashBlocked("Teleport Crash", "Invalid Coordinates (" + coordsStr + ")");

                  try {
                     MinecraftClient mc = MinecraftClient.getInstance();
                     if (mc != null && mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleport.comp_3133()));
                     }
                  } catch (Exception var9) {
                  }

                  return true;
               }
            }
         }

         if (packet instanceof VehicleMoveS2CPacket vehicleMove) {
            Vec3d pos = vehicleMove.comp_3347();
            if (pos == null
               || isInvalidDouble(pos.x)
               || isInvalidDouble(pos.y)
               || isInvalidDouble(pos.z)
               || Math.abs(pos.x) > 3.0E7
               || Math.abs(pos.y) > 3.0E7
               || Math.abs(pos.z) > 3.0E7
               || Float.isNaN(vehicleMove.comp_3348())
               || Float.isInfinite(vehicleMove.comp_3348())
               || Float.isNaN(vehicleMove.comp_3349())
               || Float.isInfinite(vehicleMove.comp_3349())) {
               this.onCrashBlocked("Vehicle Crash", "Invalid vehicle coordinates or rotation");
               return true;
            }
         }

         if (packet instanceof EntityPositionS2CPacket entityPos) {
            PlayerPosition change = entityPos.comp_3238();
            if (change != null) {
               Vec3d pos = change.comp_3148();
               if (pos == null
                  || isInvalidDouble(pos.x)
                  || isInvalidDouble(pos.y)
                  || isInvalidDouble(pos.z)
                  || Math.abs(pos.x) > 3.0E7
                  || Math.abs(pos.y) > 3.0E7
                  || Math.abs(pos.z) > 3.0E7) {
                  this.onCrashBlocked("Entity Position Exploit", "Invalid coordinates for entity");
                  return true;
               }
            }
         }

         if (packet instanceof EntityPositionSyncS2CPacket entitySync) {
            PlayerPosition values = entitySync.comp_3224();
            if (values != null) {
               Vec3d pos = values.comp_3148();
               if (pos == null
                  || isInvalidDouble(pos.x)
                  || isInvalidDouble(pos.y)
                  || isInvalidDouble(pos.z)
                  || Math.abs(pos.x) > 3.0E7
                  || Math.abs(pos.y) > 3.0E7
                  || Math.abs(pos.z) > 3.0E7) {
                  this.onCrashBlocked("Entity Sync Exploit", "Invalid sync coordinates for entity");
                  return true;
               }
            }
         }

         return false;
      }
   }

   public void tick() {
      for (ClientProtectionManager.CrashAlert alert : this.activeAlerts) {
         if (alert.isExpired()) {
            this.activeAlerts.remove(alert);
         }
      }

      if (!this.pendingAdvancements.isEmpty() && System.currentTimeMillis() - this.lastAdvancementPacketTime >= 3000L) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.getNetworkHandler() != null) {
            while (!this.pendingAdvancements.isEmpty()) {
               Packet<?> p = this.pendingAdvancements.poll();
               if (p != null) {
                  try {
                     p.apply(mc.getNetworkHandler());
                  } catch (Exception var5) {
                  }
               }
            }
         }
      }
   }

   private static boolean isInvalidDouble(double val) {
      return Double.isNaN(val) || Double.isInfinite(val);
   }

   @Environment(EnvType.CLIENT)
   public static class CrashAlert {
      public final String title;
      public final String details;
      public final long timestamp;
      public float anim = 0.0F;

      public CrashAlert(String title, String details, long timestamp) {
         this.title = title;
         this.details = details;
         this.timestamp = timestamp;
      }

      public boolean isExpired() {
         return System.currentTimeMillis() - this.timestamp > 4500L;
      }
   }
}
