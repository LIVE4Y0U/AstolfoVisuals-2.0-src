package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(entity.projectile.ProjectileEntity.class)
public class ProjectileEntityMixin {
   @Inject(method = "onEntityHit", at = @At("HEAD"))
   private void onEntityHit(util.hit.EntityHitResult entityHitResult, CallbackInfo ci) {
      entity.projectile.ProjectileEntity self = (entity.projectile.ProjectileEntity)this;
      if (self.getOwner() == minecraft.client.MinecraftClient.getInstance().player) {
         minecraft.entity.Entity target = entityHitResult.getEntity();
         if (AstolfoclientClient.killEffectManager != null) {
            Module mod = AstolfoclientClient.moduleManager.getModuleByName("KillEffect");
            if (mod != null && mod.isEnabled()) {
               AstolfoclientClient.killEffectManager.onAttack(target);
            }
         }
      }
   }
}
