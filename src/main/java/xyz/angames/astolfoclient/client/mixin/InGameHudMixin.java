package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.hud.LogoRenderer;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.InterfaceModule;

@Environment(EnvType.CLIENT)
@Mixin(class_329.class)
public class InGameHudMixin {
   @Shadow
   @Final
   private class_310 field_2035;

   @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
   private void onRenderStatusEffectOverlay(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      if (AstolfoclientClient.moduleManager != null
         && AstolfoclientClient.moduleManager.getModuleByName("Interface") instanceof InterfaceModule iface
         && iface.isEnabled()
         && iface.effectHud.get()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
   private void onRenderCrosshair(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      Module crosshairModule = AstolfoclientClient.moduleManager.getModuleByName("Crosshair");
      if (crosshairModule != null && crosshairModule.isEnabled()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
   private void onRenderHotbar(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      Module hotbarModule = AstolfoclientClient.moduleManager.getModuleByName("CustomHotbar");
      if (hotbarModule != null && hotbarModule.isEnabled()) {
         ci.cancel();
      } else {
         float offset = LogoRenderer.getHotbarYOffset();
         if (offset > 0.01F) {
            context.method_51448().method_22903();
            context.method_51448().method_46416(0.0F, -offset, 0.0F);
         }
      }
   }

   @Inject(method = "renderHotbar", at = @At("RETURN"))
   private void onAfterRenderHotbar(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      Module hotbarModule = AstolfoclientClient.moduleManager.getModuleByName("CustomHotbar");
      if (hotbarModule == null || !hotbarModule.isEnabled()) {
         float offset = LogoRenderer.getHotbarYOffset();
         if (offset > 0.01F) {
            context.method_51448().method_22909();
         }
      }
   }

   @Inject(method = "renderStatusBars", at = @At("HEAD"))
   private void onBeforeRenderStatusBars(class_332 context, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.method_51448().method_22903();
         context.method_51448().method_46416(0.0F, -offset, 0.0F);
      }
   }

   @Inject(method = "renderStatusBars", at = @At("RETURN"))
   private void onAfterRenderStatusBars(class_332 context, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.method_51448().method_22909();
      }
   }

   @Inject(method = "renderExperienceBar", at = @At("HEAD"))
   private void onBeforeRenderExperienceBar(class_332 context, int x, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.method_51448().method_22903();
         context.method_51448().method_46416(0.0F, -offset, 0.0F);
      }
   }

   @Inject(method = "renderExperienceBar", at = @At("RETURN"))
   private void onAfterRenderExperienceBar(class_332 context, int x, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.method_51448().method_22909();
      }
   }

   @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
   private void onBeforeRenderExperienceLevel(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.method_51448().method_22903();
         context.method_51448().method_46416(0.0F, -offset, 0.0F);
      }
   }

   @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
   private void onAfterRenderExperienceLevel(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.method_51448().method_22909();
      }
   }

   @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"))
   private void onBeforeRenderHeldItemTooltip(class_332 context, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.method_51448().method_22903();
         context.method_51448().method_46416(0.0F, -offset, 0.0F);
      }
   }

   @Inject(method = "renderHeldItemTooltip", at = @At("RETURN"))
   private void onAfterRenderHeldItemTooltip(class_332 context, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.method_51448().method_22909();
      }
   }
}
