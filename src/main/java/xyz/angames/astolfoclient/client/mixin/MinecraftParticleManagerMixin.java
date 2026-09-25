package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_702;
import net.minecraft.class_703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.ParticlesModule;

@Environment(EnvType.CLIENT)
@Mixin(class_702.class)
public class MinecraftParticleManagerMixin {
   @Unique
   private static long astolfoclient$lastTotemPopTime = 0L;

   @Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V", at = @At("HEAD"), cancellable = true)
   private void onAddEmitter(class_1297 entity, class_2394 parameters, int maxAge, CallbackInfo ci) {
      if (parameters != null
         && parameters.method_10295() == class_2398.field_11220
         && (AstolfoclientClient.moduleManager != null ? AstolfoclientClient.moduleManager.getModuleByName("Particles") : null) instanceof ParticlesModule pm
         && pm.isEnabled()
         && pm.totemPop.get()) {
         long now = System.currentTimeMillis();
         if (now - astolfoclient$lastTotemPopTime > 40L) {
            astolfoclient$lastTotemPopTime = now;
            if (AstolfoclientClient.particleManager != null && entity != null) {
               class_243 pos = entity.method_19538().method_1031(0.0, entity.method_17682() * 0.5, 0.0);
               AstolfoclientClient.particleManager.addTotemPop(pos);
            }
         }

         ci.cancel();
      }
   }

   @Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V", at = @At("HEAD"), cancellable = true)
   private void onAddEmitterShort(class_1297 entity, class_2394 parameters, CallbackInfo ci) {
      if (parameters != null
         && parameters.method_10295() == class_2398.field_11220
         && (AstolfoclientClient.moduleManager != null ? AstolfoclientClient.moduleManager.getModuleByName("Particles") : null) instanceof ParticlesModule pm
         && pm.isEnabled()
         && pm.totemPop.get()) {
         long now = System.currentTimeMillis();
         if (now - astolfoclient$lastTotemPopTime > 40L) {
            astolfoclient$lastTotemPopTime = now;
            if (AstolfoclientClient.particleManager != null && entity != null) {
               class_243 pos = entity.method_19538().method_1031(0.0, entity.method_17682() * 0.5, 0.0);
               AstolfoclientClient.particleManager.addTotemPop(pos);
            }
         }

         ci.cancel();
      }
   }

   @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
   private void onAddParticle(
      class_2394 parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<class_703> cir
   ) {
      if (!Double.isNaN(x)
         && !Double.isNaN(y)
         && !Double.isNaN(z)
         && !Double.isInfinite(x)
         && !Double.isInfinite(y)
         && !Double.isInfinite(z)
         && !Double.isNaN(velocityX)
         && !Double.isNaN(velocityY)
         && !Double.isNaN(velocityZ)
         && !Double.isInfinite(velocityX)
         && !Double.isInfinite(velocityY)
         && !Double.isInfinite(velocityZ)
         && !(Math.abs(x) > 3.0E7)
         && !(Math.abs(y) > 3.0E7)
         && !(Math.abs(z) > 3.0E7)) {
         if (parameters != null
            && parameters.method_10295() == class_2398.field_11220
            && (AstolfoclientClient.moduleManager != null ? AstolfoclientClient.moduleManager.getModuleByName("Particles") : null) instanceof ParticlesModule pm
            && pm.isEnabled()
            && pm.totemPop.get()) {
            cir.setReturnValue(null);
         }
      } else {
         cir.setReturnValue(null);
      }
   }
}
