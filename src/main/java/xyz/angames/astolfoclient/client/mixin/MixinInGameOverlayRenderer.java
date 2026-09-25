package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.module.modules.render.NoRenderModule;

@Environment(EnvType.CLIENT)
@Mixin(gui.hud.InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {
   @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
   private static void onRenderFireOverlay(util.math.MatrixStack matrices, client.render.VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      NoRenderModule noRender = NoRenderModule.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.fire.get()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
   private static void onRenderInWallOverlay(client.texture.Sprite sprite, util.math.MatrixStack matrices, client.render.VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      NoRenderModule noRender = NoRenderModule.getInstance();
      if (noRender != null && noRender.isEnabled() && noRender.blockOverlay.get()) {
         ci.cancel();
      }
   }
}
