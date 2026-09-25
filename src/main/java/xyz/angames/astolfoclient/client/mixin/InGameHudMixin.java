package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
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
@Mixin(InGameHud.class)
public class InGameHudMixin {
   @Shadow
   @Final
   private MinecraftClient client;

   @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
   private void onRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      if (AstolfoclientClient.moduleManager != null
         && AstolfoclientClient.moduleManager.getModuleByName("Interface") instanceof InterfaceModule iface
         && iface.isEnabled()
         && iface.effectHud.get()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
   private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      Module crosshairModule = AstolfoclientClient.moduleManager.getModuleByName("Crosshair");
      if (crosshairModule != null && crosshairModule.isEnabled()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
   private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      Module hotbarModule = AstolfoclientClient.moduleManager.getModuleByName("CustomHotbar");
      if (hotbarModule != null && hotbarModule.isEnabled()) {
         ci.cancel();
      } else {
         float offset = LogoRenderer.getHotbarYOffset();
         if (offset > 0.01F) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, -offset, 0.0F);
         }
      }
   }

   @Inject(method = "renderHotbar", at = @At("RETURN"))
   private void onAfterRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      Module hotbarModule = AstolfoclientClient.moduleManager.getModuleByName("CustomHotbar");
      if (hotbarModule == null || !hotbarModule.isEnabled()) {
         float offset = LogoRenderer.getHotbarYOffset();
         if (offset > 0.01F) {
            context.getMatrices().pop();
         }
      }
   }

   @Inject(method = "renderStatusBars", at = @At("HEAD"))
   private void onBeforeRenderStatusBars(DrawContext context, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, -offset, 0.0F);
      }
   }

   @Inject(method = "renderStatusBars", at = @At("RETURN"))
   private void onAfterRenderStatusBars(DrawContext context, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.getMatrices().pop();
      }
   }

   @Inject(method = "renderExperienceBar", at = @At("HEAD"))
   private void onBeforeRenderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, -offset, 0.0F);
      }
   }

   @Inject(method = "renderExperienceBar", at = @At("RETURN"))
   private void onAfterRenderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.getMatrices().pop();
      }
   }

   @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
   private void onBeforeRenderExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, -offset, 0.0F);
      }
   }

   @Inject(method = "renderExperienceLevel", at = @At("RETURN"))
   private void onAfterRenderExperienceLevel(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.getMatrices().pop();
      }
   }

   @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"))
   private void onBeforeRenderHeldItemTooltip(DrawContext context, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, -offset, 0.0F);
      }
   }

   @Inject(method = "renderHeldItemTooltip", at = @At("RETURN"))
   private void onAfterRenderHeldItemTooltip(DrawContext context, CallbackInfo ci) {
      float offset = LogoRenderer.getHotbarYOffset();
      if (offset > 0.01F) {
         context.getMatrices().pop();
      }
   }
}
