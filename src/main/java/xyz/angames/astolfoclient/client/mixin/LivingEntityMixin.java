package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.NoRenderModule;
import xyz.angames.astolfoclient.client.module.modules.render.RagdollModule;
import xyz.angames.astolfoclient.client.module.modules.render.SwingAnimationModule;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
   @Inject(method = "jump", at = @At("HEAD"))
   private void onJump(CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)this;
      if (entity.equals(MinecraftClient.getInstance().player) && AstolfoclientClient.moduleManager != null) {
         Module jumpCircleModule = AstolfoclientClient.moduleManager.getModuleByName("JumpCircle");
         if (jumpCircleModule != null && jumpCircleModule.isEnabled() && AstolfoclientClient.jumpCircleManager != null) {
            AstolfoclientClient.jumpCircleManager.addCircle(entity.getX(), entity.getY(), entity.getZ());
         }
      }
   }

   @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
   private void onHasStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
      LivingEntity entity = (LivingEntity)this;
      if (entity == MinecraftClient.getInstance().player) {
         NoRenderModule noRender = NoRenderModule.getInstance();
         if (noRender != null
            && noRender.isEnabled()
            && noRender.blindness.get()
            && (effect.equals(StatusEffects.BLINDNESS) || effect.equals(StatusEffects.DARKNESS))) {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
   private void onGetStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
      LivingEntity entity = (LivingEntity)this;
      if (entity == MinecraftClient.getInstance().player) {
         NoRenderModule noRender = NoRenderModule.getInstance();
         if (noRender != null
            && noRender.isEnabled()
            && noRender.blindness.get()
            && (effect.equals(StatusEffects.BLINDNESS) || effect.equals(StatusEffects.DARKNESS))) {
            cir.setReturnValue(null);
         }
      }
   }

   @Inject(method = "handleStatus(B)V", at = @At("HEAD"))
   private void onHandleStatus(byte status, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity)this;
      if ((status == 35 || status == 3) && AstolfoclientClient.moduleManager != null) {
         Module ragdollModule = AstolfoclientClient.moduleManager.getModuleByName("Ragdoll");
         if (ragdollModule != null && ragdollModule.isEnabled()) {
            boolean isTotem = status == 35 && ((RagdollModule)ragdollModule).totemPop.get();
            boolean isDeath = status == 3 && ((RagdollModule)ragdollModule).death.get();
            if ((isTotem || isDeath) && AstolfoclientClient.ragdollRenderer != null) {
               AstolfoclientClient.ragdollRenderer.addRagdoll(entity);
            }
         }
      }
   }

   @Inject(method = "getStuckArrowCount", at = @At("RETURN"), cancellable = true)
   private void onGetStuckArrowCount(CallbackInfoReturnable<Integer> cir) {
      if ((Integer)cir.getReturnValue() > 50) {
         cir.setReturnValue(50);
      }
   }

   @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
   private void onGetHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
      LivingEntity entity = (LivingEntity)this;
      if (entity == MinecraftClient.getInstance().player && AstolfoclientClient.moduleManager != null) {
         SwingAnimationModule swing = (SwingAnimationModule)AstolfoclientClient.moduleManager.getModuleByName("SwingAnimation");
         if (swing != null && swing.isEnabled() && swing.slow.get()) {
            cir.setReturnValue(Double.valueOf(swing.speed.getValue()).intValue());
         }
      }
   }
}
