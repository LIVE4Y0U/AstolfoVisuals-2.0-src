package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(render.entity.LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
   @Inject(
      method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD")
   )
   private void shrinkToBabySize(entity.state.LivingEntityRenderState state, util.math.MatrixStack matrixStack, client.render.VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
      if (state instanceof entity.state.PlayerEntityRenderState playerState
         && minecraft.client.MinecraftClient.getInstance().player != null
         && playerState.id == minecraft.client.MinecraftClient.getInstance().player.getId()
         && AstolfoclientClient.moduleManager != null) {
         Module babyMod = AstolfoclientClient.moduleManager.getModuleByName("BabyPlayer");
         if (babyMod != null && babyMod.isEnabled()) {
            matrixStack.scale(0.5F, 0.5F, 0.5F);
         }
      }
   }
}
