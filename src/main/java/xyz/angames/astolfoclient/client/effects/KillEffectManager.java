package xyz.angames.astolfoclient.client.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.KillEffectModule;

@Environment(EnvType.CLIENT)
public class KillEffectManager {
   public static final long LIFESPAN = 3000L;
   private final List<KillEffectManager.KillEffect> effects = new CopyOnWriteArrayList<>();
   private final Map<Integer, KillEffectManager.TrackedTarget> recentAttacks = new ConcurrentHashMap<>();
   private final class_310 client = class_310.method_1551();

   public void onAttack(class_1297 target) {
      if (target instanceof class_1309 living) {
         this.recentAttacks.put(target.method_5628(), new KillEffectManager.TrackedTarget(living));
      }
   }

   public void tick() {
      if (this.client.field_1687 != null && this.client.field_1724 != null) {
         long now = System.currentTimeMillis();

         for (Entry<Integer, KillEffectManager.TrackedTarget> entry : this.recentAttacks.entrySet()) {
            int id = entry.getKey();
            KillEffectManager.TrackedTarget tracked = entry.getValue();
            if (now - tracked.lastHitTime > 10000L) {
               this.recentAttacks.remove(id);
            } else {
               class_1297 currentEntity = this.client.field_1687.method_8469(id);
               boolean isKilled = false;
               class_243 deathPos = tracked.lastPos;
               class_238 deathBox = tracked.lastBox;
               if (currentEntity == null) {
                  isKilled = true;
               } else if (currentEntity instanceof class_1309 living) {
                  class_243 currentPos = living.method_19538();
                  if (living.method_29504() || living.method_6032() <= 0.0F || living.field_6213 > 0 || !living.method_5805()) {
                     isKilled = true;
                     deathPos = currentPos;
                     deathBox = living.method_5829();
                  } else if (currentPos.method_1025(tracked.lastPos) > 400.0) {
                     isKilled = true;
                  }

                  if (!isKilled) {
                     tracked.lastPos = currentPos;
                     tracked.lastBox = living.method_5829();
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
      public final class_243 pos;
      public final String mode;
      public final long startTime;
      public final List<class_243> zapPoints = new ArrayList<>();
      public final List<KillEffectManager.ThanosParticle> thanosParticles = new ArrayList<>();

      public KillEffect(class_243 pos, class_238 box, String mode, long startTime) {
         this.pos = pos;
         this.mode = mode;
         this.startTime = startTime;
         if (mode.equals("Zap")) {
            float currentX = 0.0F;
            float currentZ = 0.0F;
            this.zapPoints.add(new class_243(0.0, 0.0, 0.0));

            for (float y = 1.0F + (float)Math.random() * 1.5F; y <= 20.0F; y = (float)(y + (1.0 + Math.random() * 1.5))) {
               currentX = (float)(currentX + (Math.random() - 0.5) * 3.5);
               currentZ = (float)(currentZ + (Math.random() - 0.5) * 3.5);
               this.zapPoints.add(new class_243(currentX, y, currentZ));
            }
         } else if (mode.equals("Thanos")) {
            float width = box != null ? (float)(box.field_1320 - box.field_1323) : 0.6F;
            float height = box != null ? (float)(box.field_1325 - box.field_1322) : 1.8F;

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
      public class_243 lastPos;
      public class_238 lastBox;
      public final long lastHitTime;

      public TrackedTarget(class_1309 entity) {
         this.lastPos = entity.method_19538();
         this.lastBox = entity.method_5829();
         this.lastHitTime = System.currentTimeMillis();
      }
   }
}
