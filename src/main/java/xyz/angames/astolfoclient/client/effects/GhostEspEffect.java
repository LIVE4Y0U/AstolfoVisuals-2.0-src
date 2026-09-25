package xyz.angames.astolfoclient.client.effects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;

@Environment(EnvType.CLIENT)
public class GhostEspEffect {
   public final class_1297 target;
   public long lastHitTime;
   public long lastAttackTime = 0L;
   public float currentAngle = 0.0F;
   public long lastRenderTime = 0L;

   public GhostEspEffect(class_1297 target) {
      this.target = target;
      this.lastHitTime = System.currentTimeMillis();
   }
}
