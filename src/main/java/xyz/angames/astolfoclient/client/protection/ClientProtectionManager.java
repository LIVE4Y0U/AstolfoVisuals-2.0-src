package xyz.angames.astolfoclient.client.protection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10182;
import net.minecraft.class_10264;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2604;
import net.minecraft.class_2664;
import net.minecraft.class_2675;
import net.minecraft.class_2692;
import net.minecraft.class_2708;
import net.minecraft.class_2743;
import net.minecraft.class_2767;
import net.minecraft.class_2777;
import net.minecraft.class_2779;
import net.minecraft.class_2793;
import net.minecraft.class_310;
import xyz.angames.astolfoclient.client.util.ModSounds;

@Environment(EnvType.CLIENT)
public class ClientProtectionManager {
   private static final ClientProtectionManager INSTANCE = new ClientProtectionManager();
   private final List<ClientProtectionManager.CrashAlert> activeAlerts = new CopyOnWriteArrayList<>();
   private long lastSoundTime = 0L;
   private static final int ADVANCEMENT_QUEUE_LIMIT = 1600;
   private static final long ADVANCEMENT_QUIET_PERIOD_MS = 3000L;
   private final Queue<class_2596<?>> pendingAdvancements = new ConcurrentLinkedQueue<>();
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

      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1705 != null && mc.field_1705.method_1743() != null) {
         mc.field_1705.method_1743().method_1812(class_2561.method_43470("§c[Protection] §fBlocked crash exploit: §e" + title + " §7(" + details + ")"));
      }
   }

   public boolean isMaliciousPacket(class_2596<?> packet) {
      if (packet == null) {
         return false;
      }

      if (packet instanceof class_2664 explosion) {
         class_243 center = explosion.comp_2883();
         if (center == null
            || isInvalidDouble(center.field_1352)
            || isInvalidDouble(center.field_1351)
            || isInvalidDouble(center.field_1350)
            || Math.abs(center.field_1352) > 3.0E7
            || Math.abs(center.field_1351) > 3.0E7
            || Math.abs(center.field_1350) > 3.0E7) {
            this.onCrashBlocked("Explosion Crash", "Center coordinates outside world boundary");
            return true;
         }

         if (explosion.comp_2884().isPresent()) {
            class_243 kb = (class_243)explosion.comp_2884().get();
            if (kb == null
               || isInvalidDouble(kb.field_1352)
               || isInvalidDouble(kb.field_1351)
               || isInvalidDouble(kb.field_1350)
               || Math.abs(kb.field_1352) > 1000000.0
               || Math.abs(kb.field_1351) > 1000000.0
               || Math.abs(kb.field_1350) > 1000000.0) {
               this.onCrashBlocked("Explosion Crash", "Malformed knockback vector");
               return true;
            }
         }
      }

      if (packet instanceof class_2675 particle) {
         if (isInvalidDouble(particle.method_11544())
            || isInvalidDouble(particle.method_11547())
            || isInvalidDouble(particle.method_11546())
            || Math.abs(particle.method_11544()) > 3.0E7
            || Math.abs(particle.method_11547()) > 3.0E7
            || Math.abs(particle.method_11546()) > 3.0E7) {
            this.onCrashBlocked("Particle Exploit", "Coordinates out of bounds");
            return true;
         }

         if (particle.method_11545() > 1000 || particle.method_11545() < 0) {
            this.onCrashBlocked("Particle Exploit", "Invalid count: " + particle.method_11545());
            return true;
         }

         if (Float.isNaN(particle.method_11543()) || Float.isInfinite(particle.method_11543()) || Math.abs(particle.method_11543()) > 1000.0F) {
            this.onCrashBlocked("Particle Exploit", "Malformed particle speed");
            return true;
         }

         if (Float.isNaN(particle.method_11548())
            || Float.isNaN(particle.method_11549())
            || Float.isNaN(particle.method_11550())
            || Math.abs(particle.method_11548()) > 1000.0F
            || Math.abs(particle.method_11549()) > 1000.0F
            || Math.abs(particle.method_11550()) > 1000.0F) {
            this.onCrashBlocked("Particle Exploit", "Malformed particle offset");
            return true;
         }
      }

      if (packet instanceof class_2779) {
         if (this.pendingAdvancements.size() >= 1600) {
            this.pendingAdvancements.poll();
            this.onCrashBlocked("Advancement Flood", "Queue exceeded limit (1600)");
         }

         this.pendingAdvancements.add(packet);
         this.lastAdvancementPacketTime = System.currentTimeMillis();
         return true;
      } else {
         if (packet instanceof class_2743 vel) {
            double vx = Math.abs(vel.method_11815() / 8000.0);
            double vy = Math.abs(vel.method_11816() / 8000.0);
            double vz = Math.abs(vel.method_11819() / 8000.0);
            if (isInvalidDouble(vx) || isInvalidDouble(vy) || isInvalidDouble(vz) || vx > 100000.0 || vy > 100000.0 || vz > 100000.0) {
               this.onCrashBlocked("Velocity Exploit", "Extreme entity velocity values");
               return true;
            }
         }

         if (packet instanceof class_2767 sound) {
            if (isInvalidDouble(sound.method_11890())
               || isInvalidDouble(sound.method_11889())
               || isInvalidDouble(sound.method_11893())
               || Math.abs(sound.method_11890()) > 3.0E7
               || Math.abs(sound.method_11889()) > 3.0E7
               || Math.abs(sound.method_11893()) > 3.0E7) {
               this.onCrashBlocked("Sound Exploit", "Coordinates out of bounds");
               return true;
            }

            if (Float.isNaN(sound.method_11891())
               || Float.isNaN(sound.method_11892())
               || sound.method_11891() < 0.0F
               || sound.method_11891() > 100.0F
               || sound.method_11892() < 0.0F
               || sound.method_11892() > 100.0F) {
               this.onCrashBlocked("Sound Exploit", "Invalid sound volume/pitch");
               return true;
            }
         }

         if (packet instanceof class_2604 spawn) {
            if (isInvalidDouble(spawn.method_11175())
               || isInvalidDouble(spawn.method_11174())
               || isInvalidDouble(spawn.method_11176())
               || Math.abs(spawn.method_11175()) > 3.0E7
               || Math.abs(spawn.method_11174()) > 3.0E7
               || Math.abs(spawn.method_11176()) > 3.0E7) {
               this.onCrashBlocked("Spawn Exploit", "Entity coordinates out of bounds");
               return true;
            }

            if (isInvalidDouble(spawn.method_11170())
               || isInvalidDouble(spawn.method_11172())
               || isInvalidDouble(spawn.method_11173())
               || Math.abs(spawn.method_11170()) > 100000.0
               || Math.abs(spawn.method_11172()) > 100000.0
               || Math.abs(spawn.method_11173()) > 100000.0) {
               this.onCrashBlocked("Spawn Exploit", "Entity spawned with extreme velocity");
               return true;
            }
         }

         if (packet instanceof class_2708 teleport) {
            class_10182 change = teleport.comp_3228();
            if (change != null) {
               class_243 pos = change.comp_3148();
               class_243 delta = change.comp_3149();
               if (pos == null
                  || isInvalidDouble(pos.field_1352)
                  || isInvalidDouble(pos.field_1351)
                  || isInvalidDouble(pos.field_1350)
                  || Math.abs(pos.field_1352) > 3.0E7
                  || Math.abs(pos.field_1351) > 3.0E7
                  || Math.abs(pos.field_1350) > 3.0E7
                  || delta != null
                     && (
                        isInvalidDouble(delta.field_1352)
                           || isInvalidDouble(delta.field_1351)
                           || isInvalidDouble(delta.field_1350)
                           || Math.abs(delta.field_1352) > 1000000.0
                           || Math.abs(delta.field_1351) > 1000000.0
                           || Math.abs(delta.field_1350) > 1000000.0
                     )
                  || Float.isNaN(change.comp_3150())
                  || Float.isInfinite(change.comp_3150())
                  || Float.isNaN(change.comp_3151())
                  || Float.isInfinite(change.comp_3151())) {
                  String coordsStr = pos != null ? String.format("X: %.1f, Y: %.1f, Z: %.1f", pos.field_1352, pos.field_1351, pos.field_1350) : "null";
                  this.onCrashBlocked("Teleport Crash", "Invalid Coordinates (" + coordsStr + ")");

                  try {
                     class_310 mc = class_310.method_1551();
                     if (mc != null && mc.method_1562() != null) {
                        mc.method_1562().method_52787(new class_2793(teleport.comp_3133()));
                     }
                  } catch (Exception var9) {
                  }

                  return true;
               }
            }
         }

         if (packet instanceof class_2692 vehicleMove) {
            class_243 pos = vehicleMove.comp_3347();
            if (pos == null
               || isInvalidDouble(pos.field_1352)
               || isInvalidDouble(pos.field_1351)
               || isInvalidDouble(pos.field_1350)
               || Math.abs(pos.field_1352) > 3.0E7
               || Math.abs(pos.field_1351) > 3.0E7
               || Math.abs(pos.field_1350) > 3.0E7
               || Float.isNaN(vehicleMove.comp_3348())
               || Float.isInfinite(vehicleMove.comp_3348())
               || Float.isNaN(vehicleMove.comp_3349())
               || Float.isInfinite(vehicleMove.comp_3349())) {
               this.onCrashBlocked("Vehicle Crash", "Invalid vehicle coordinates or rotation");
               return true;
            }
         }

         if (packet instanceof class_2777 entityPos) {
            class_10182 change = entityPos.comp_3238();
            if (change != null) {
               class_243 pos = change.comp_3148();
               if (pos == null
                  || isInvalidDouble(pos.field_1352)
                  || isInvalidDouble(pos.field_1351)
                  || isInvalidDouble(pos.field_1350)
                  || Math.abs(pos.field_1352) > 3.0E7
                  || Math.abs(pos.field_1351) > 3.0E7
                  || Math.abs(pos.field_1350) > 3.0E7) {
                  this.onCrashBlocked("Entity Position Exploit", "Invalid coordinates for entity");
                  return true;
               }
            }
         }

         if (packet instanceof class_10264 entitySync) {
            class_10182 values = entitySync.comp_3224();
            if (values != null) {
               class_243 pos = values.comp_3148();
               if (pos == null
                  || isInvalidDouble(pos.field_1352)
                  || isInvalidDouble(pos.field_1351)
                  || isInvalidDouble(pos.field_1350)
                  || Math.abs(pos.field_1352) > 3.0E7
                  || Math.abs(pos.field_1351) > 3.0E7
                  || Math.abs(pos.field_1350) > 3.0E7) {
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
         class_310 mc = class_310.method_1551();
         if (mc.method_1562() != null) {
            while (!this.pendingAdvancements.isEmpty()) {
               class_2596<?> p = this.pendingAdvancements.poll();
               if (p != null) {
                  try {
                     p.method_65081(mc.method_1562());
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
