package xyz.angames.astolfoclient.client.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.KillEffectModule;

@Environment(EnvType.CLIENT)
public class KillEffectManager {
   public static final long LIFESPAN = 3000L;
   private final List<KillEffectManager.KillEffect> effects = new CopyOnWriteArrayList<>();
   private final Map<Integer, KillEffectManager.TrackedTarget> recentAttacks = new ConcurrentHashMap<>();
   private final minecraft.client.MinecraftClient client = minecraft.client.MinecraftClient.getInstance();

   public void onAttack(minecraft.entity.Entity target) {
      if (target instanceof minecraft.entity.LivingEntity living) {
         this.recentAttacks.put(target.getId(), new KillEffectManager.TrackedTarget(living));
      }
   }

   public void tick() {
      if (this.client.world != null && this.client.player != null) {
         long now = System.currentTimeMillis();

         for (Entry<Integer, KillEffectManager.TrackedTarget> entry : this.recentAttacks.entrySet()) {
            int id = entry.getKey();
            KillEffectManager.TrackedTarget tracked = entry.getValue();
            if (now - tracked.lastHitTime > 10000L) {
               this.recentAttacks.remove(id);
            } else {
               minecraft.entity.Entity currentEntity = this.client.world.getEntityById(id);
               boolean isKilled = false;
               util.math.Vec3d deathPos = tracked.lastPos;
               util.math.Box deathBox = tracked.lastBox;
               if (currentEntity == null) {
                  isKilled = true;
               } else if (currentEntity instanceof minecraft.entity.LivingEntity living) {
                  util.math.Vec3d currentPos = living.getPos();
                  if (living.isDead() || living.getHealth() <= 0.0F || living.deathTime > 0 || !living.isAlive()) {
                     isKilled = true;
                     deathPos = currentPos;
                     deathBox = living.getBoundingBox();
                  } else if (currentPos.squaredDistanceTo(tracked.lastPos) > 400.0) {
                     isKilled = true;
                  }

                  if (!isKilled) {
                     tracked.lastPos = currentPos;
                     tracked.lastBox = living.getBoundingBox();
                  }
               }

               if (isKilled) {
                  KillEffectModule mod = (KillEffectModule)AstolfoclientClient.moduleManager.getModuleByName("KillEffect");
                  String mode = mod != null ? mod.mode.get() : "Zap";
                  this.effects.add(new KillEffectManager.KillEffect(deathPos, deathBox, mode, now));
                  this.recentAttacks.remove(id);
               }
            }
         }

         this.effects.removeIf(e -> now - e.startTime > 3000L);
      } else {
         this.effects.clear();
         this.recentAttacks.clear();
      }
   }

   public List<KillEffectManager.KillEffect> getEffects() {
      return this.effects;
   }

   @Environment(EnvType.CLIENT)
   public static class KillEffect {
      public final util.math.Vec3d pos;
      public final String mode;
      public final long startTime;
      public final List<util.math.Vec3d> zapPoints = new ArrayList<>();
      public final List<KillEffectManager.ThanosParticle> thanosParticles = new ArrayList<>();

      public KillEffect(util.math.Vec3d pos, util.math.Box box, String mode, long startTime) {
         this.pos = pos;
         this.mode = mode;
         this.startTime = startTime;
         if (mode.equals("Zap")) {
            float currentX = 0.0F;
            float currentZ = 0.0F;
            this.zapPoints.add(new util.math.Vec3d(0.0, 0.0, 0.0));

            for (float y = 1.0F + (float)Math.random() * 1.5F; y <= 20.0F; y = (float)(y + (1.0 + Math.random() * 1.5))) {
               currentX = (float)(currentX + (Math.random() - 0.5) * 3.5);
               currentZ = (float)(currentZ + (Math.random() - 0.5) * 3.5);
               this.zapPoints.add(new util.math.Vec3d(currentX, y, currentZ));
            }
         } else if (mode.equals("Thanos")) {
            float width = box != null ? (float)(box.maxX - box.minX) : 0.6F;
            float height = box != null ? (float)(box.maxY - box.minY) : 1.8F;

            for (int i = 0; i < 500; i++) {
               KillEffectManager.ThanosParticle p = new KillEffectManager.ThanosParticle();
               p.startX = (float)((Math.random() - 0.5) * width);
               p.startY = (float)(Math.random() * height);
               p.startZ = (float)((Math.random() - 0.5) * width);
               p.fallSpeed = (float)(0.001 + Math.random() * 0.003);
               p.delay = (float)(Math.random() * 1200.0);
               this.thanosParticles.add(p);
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class ThanosParticle {
      public float startX;
      public float startY;
      public float startZ;
      public float fallSpeed;
      public float delay;
   }

   @Environment(EnvType.CLIENT)
   private static class TrackedTarget {
      public util.math.Vec3d lastPos;
      public util.math.Box lastBox;
      public final long lastHitTime;

      public TrackedTarget(minecraft.entity.LivingEntity entity) {
         this.lastPos = entity.getPos();
         this.lastBox = entity.getBoundingBox();
         this.lastHitTime = System.currentTimeMillis();
      }
   }
}
