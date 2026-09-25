package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.TargetEspModule;
import xyz.angames.astolfoclient.client.module.modules.render.HitGlowModule;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
@Mixin(class_1657.class)
public class PlayerEntityMixin {
   @Inject(method = "attack", at = @At("HEAD"))
   private void onAttack(class_1297 target, CallbackInfo ci) {
      if (this == class_310.method_1551().field_1724 && target instanceof class_1309 livingTarget) {
         if (!TargetUtils.isInvisible(livingTarget)) {
            TargetEspModule.addTargetAttack(target);
            if (AstolfoclientClient.targetHudManager != null) {
               AstolfoclientClient.targetHudManager.setTarget(livingTarget);
            }
         }

         HitGlowModule.addWave(target.method_19538());
         if (AstolfoclientClient.particleManager != null) {
            AstolfoclientClient.particleManager.addEffects(target.method_33571());
         }
      }
   }
}
