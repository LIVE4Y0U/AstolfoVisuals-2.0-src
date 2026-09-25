package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_6880;
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
@Mixin(class_1309.class)
public class LivingEntityMixin {
   @Inject(method = "jump", at = @At("HEAD"))
   private void onJump(CallbackInfo ci) {
      class_1309 entity = (class_1309)this;
      if (entity.equals(class_310.method_1551().field_1724) && AstolfoclientClient.moduleManager != null) {
         Module jumpCircleModule = AstolfoclientClient.moduleManager.getModuleByName("JumpCircle");
         if (jumpCircleModule != null && jumpCircleModule.isEnabled() && AstolfoclientClient.jumpCircleManager != null) {
            AstolfoclientClient.jumpCircleManager.addCircle(entity.method_23317(), entity.method_23318(), entity.method_23321());
         }
      }
   }

   @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
   private void onHasStatusEffect(class_6880<class_1291> effect, CallbackInfoReturnable<Boolean> cir) {
      class_1309 entity = (class_1309)this;
      if (entity == class_310.method_1551().field_1724) {
         NoRenderModule noRender = NoRenderModule.getInstance();
         if (noRender != null
            && noRender.isEnabled()
            && noRender.blindness.get()
            && (effect.equals(class_1294.field_5919) || effect.equals(class_1294.field_38092))) {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(method = "getStatusEffect", at = @At("HEAD"), cancellable = true)
   private void onGetStatusEffect(class_6880<class_1291> effect, CallbackInfoReturnable<class_1293> cir) {
      class_1309 entity = (class_1309)this;
      if (entity == class_310.method_1551().field_1724) {
         NoRenderModule noRender = NoRenderModule.getInstance();
         if (noRender != null
            && noRender.isEnabled()
            && noRender.blindness.get()
            && (effect.equals(class_1294.field_5919) || effect.equals(class_1294.field_38092))) {
            cir.setReturnValue(null);
         }
      }
   }

   @Inject(method = "handleStatus(B)V", at = @At("HEAD"))
   private void onHandleStatus(byte status, CallbackInfo ci) {
      class_1309 entity = (class_1309)this;
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
      class_1309 entity = (class_1309)this;
      if (entity == class_310.method_1551().field_1724 && AstolfoclientClient.moduleManager != null) {
         SwingAnimationModule swing = (SwingAnimationModule)AstolfoclientClient.moduleManager.getModuleByName("SwingAnimation");
         if (swing != null && swing.isEnabled() && swing.slow.get()) {
            cir.setReturnValue(Double.valueOf(swing.speed.getValue()).intValue());
         }
      }
   }
}
