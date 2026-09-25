package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.TargetEspModule;
import xyz.angames.astolfoclient.client.module.modules.render.HitGlowModule;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
@Mixin(entity.player.PlayerEntity.class)
public class PlayerEntityMixin {
   @Inject(method = "attack", at = @At("HEAD"))
   private void onAttack(minecraft.entity.Entity target, CallbackInfo ci) {
      if (this == minecraft.client.MinecraftClient.getInstance().player && target instanceof minecraft.entity.LivingEntity livingTarget) {
         if (!TargetUtils.isInvisible(livingTarget)) {
            TargetEspModule.addTargetAttack(target);
            if (AstolfoclientClient.targetHudManager != null) {
               AstolfoclientClient.targetHudManager.setTarget(livingTarget);
            }
         }

         HitGlowModule.addWave(target.getPos());
         if (AstolfoclientClient.particleManager != null) {
            AstolfoclientClient.particleManager.addEffects(target.getEyePos());
         }
      }
   }
}
