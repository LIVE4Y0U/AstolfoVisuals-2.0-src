package xyz.angames.astolfoclient.client.effects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class TargetEspManager {
   public static final long LIFESPAN = 450L;
   private final Map<class_1297, TargetEspEffect> effects = new ConcurrentHashMap<>();

   public void addEffect(class_1297 target) {
      if (target != null && !TargetUtils.isInvisible(target)) {
         this.effects.computeIfAbsent(target, TargetEspEffect::new).registerHit();
      }
   }

   public void tick() {
      this.effects
         .values()
         .removeIf(
            effect -> System.currentTimeMillis() - effect.lastHitTime > 450L
               || effect.target.method_31481()
               || !effect.target.method_5805()
               || TargetUtils.isInvisible(effect.target)
         );
   }

   public Map<class_1297, TargetEspEffect> getEffects() {
      return this.effects;
   }
}
