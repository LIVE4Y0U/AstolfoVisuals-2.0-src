package xyz.angames.astolfoclient.client.effects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class GhostEspManager {
   public static final long LIFESPAN = 450L;
   private final Map<class_1297, GhostEspEffect> effects = new ConcurrentHashMap<>();

   public void addEffect(class_1297 target) {
      if (target != null && !TargetUtils.isInvisible(target)) {
         this.effects.compute(target, (entity, effect) -> {
            if (effect == null) {
               return new GhostEspEffect(target);
            }

            effect.lastHitTime = System.currentTimeMillis();
            return (GhostEspEffect)effect;
         });
      }
   }

   public void addAttack(class_1297 target) {
      if (target != null && !TargetUtils.isInvisible(target)) {
         this.effects.computeIfPresent(target, (entity, effect) -> {
            effect.lastAttackTime = System.currentTimeMillis();
            return (GhostEspEffect)effect;
         });
      }
   }

   public void tick() {
      this.effects
         .entrySet()
         .removeIf(
            entry -> !entry.getKey().method_5805()
               || System.currentTimeMillis() - entry.getValue().lastHitTime > 450L
               || TargetUtils.isInvisible(entry.getKey())
         );
   }

   public Map<class_1297, GhostEspEffect> getEffects() {
      return this.effects;
   }
}
