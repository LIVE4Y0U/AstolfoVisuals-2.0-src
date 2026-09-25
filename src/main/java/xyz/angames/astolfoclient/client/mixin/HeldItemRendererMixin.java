package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.item.ItemStack;
import net.minecraft.item.FilledMapItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.module.ModuleManager;
import xyz.angames.astolfoclient.client.module.modules.render.HandPositionModule;
import xyz.angames.astolfoclient.client.module.modules.render.ShaderHand;
import xyz.angames.astolfoclient.client.module.modules.render.SwingAnimationModule;

@Environment(EnvType.CLIENT)
@Mixin(render.item.HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("HEAD")
   )
   private void onRenderFirstPersonItemsHead(float tickDelta, util.math.MatrixStack matrices, render.VertexConsumerProvider.Immediate vertexConsumers, client.network.ClientPlayerEntity player, int light, CallbackInfo ci) {
      SwingAnimationModule swingAnim = (SwingAnimationModule)ModuleManager.getModule(SwingAnimationModule.class);
      if (swingAnim != null) {
         swingAnim.updatePhysics(player, tickDelta);
      }

      ShaderHand mod = ShaderHand.getInstance();
      if (mod != null && mod.shouldRender()) {
         mod.beginRender();
         ShaderHand.rendering = true;
      }
   }

   @Shadow
   protected abstract void renderArmHoldingItem(util.math.MatrixStack var1, client.render.VertexConsumerProvider var2, int var3, float var4, float var5, minecraft.util.Arm var6);

   @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
   private void onRenderFirstPersonItem(
      client.network.AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      minecraft.util.Hand hand,
      float swingProgress,
      minecraft.item.ItemStack item,
      float equipProgress,
      util.math.MatrixStack matrices,
      client.render.VertexConsumerProvider vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      SwingAnimationModule swingMod = (SwingAnimationModule)ModuleManager.getModule(SwingAnimationModule.class);
      HandPositionModule handMod = (HandPositionModule)ModuleManager.getModule(HandPositionModule.class);
      boolean swingEnabled = swingMod != null && swingMod.isEnabled();
      boolean handEnabled = handMod != null && handMod.isEnabled();
      if ((swingEnabled || handEnabled) && !item.isEmpty() && !(item.getItem() instanceof minecraft.item.FilledMapItem)) {
         ci.cancel();
         if (swingMod != null) {
            swingMod.handleRenderItem(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
         }
      }
   }

   @Inject(method = "renderArmHoldingItem", at = @At("HEAD"))
   private void onRenderArmHoldingItemHead(
      util.math.MatrixStack matrices, client.render.VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, minecraft.util.Arm arm, CallbackInfo ci
   ) {
      if (!SwingAnimationModule.renderingCustomItem) {
         HandPositionModule handMod = (HandPositionModule)ModuleManager.getModule(HandPositionModule.class);
         if (handMod != null && handMod.isEnabled()) {
            minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
            boolean isMainHand = mc.player != null && arm == mc.player.getMainArm();
            float[] pos = isMainHand ? handMod.getMainHandPos() : handMod.getOffHandPos();
            matrices.translate(pos[0], pos[1], pos[2]);
         }
      }
   }

   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("TAIL")
   )
   private void onRenderFirstPersonItemsTail(float tickDelta, util.math.MatrixStack matrices, render.VertexConsumerProvider.Immediate vertexConsumers, client.network.ClientPlayerEntity player, int light, CallbackInfo ci) {
      ShaderHand mod = ShaderHand.getInstance();
      if (mod != null && ShaderHand.rendering) {
         vertexConsumers.draw();
         ShaderHand.rendering = false;
         mod.draw();
      }
   }

   @Inject(
      method = "renderArmHoldingItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;renderRightArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;Z)V"
      )
   )
   private void onRenderRightArm(
      util.math.MatrixStack matrices, client.render.VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, minecraft.util.Arm arm, CallbackInfo ci
   ) {
      SwingAnimationModule mod = (SwingAnimationModule)ModuleManager.getModule(SwingAnimationModule.class);
      if (mod != null && mod.isHoldMyItemsEnabled()) {
         matrices.translate(mod.getRightX(), mod.getRightZ(), mod.getRightY());
      }
   }

   @Inject(
      method = "renderArmHoldingItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/entity/PlayerEntityRenderer;renderLeftArm(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/util/Identifier;Z)V"
      )
   )
   private void onRenderLeftArm(
      util.math.MatrixStack matrices, client.render.VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, minecraft.util.Arm arm, CallbackInfo ci
   ) {
      SwingAnimationModule mod = (SwingAnimationModule)ModuleManager.getModule(SwingAnimationModule.class);
      if (mod != null && mod.isHoldMyItemsEnabled()) {
         matrices.translate(mod.getLeftX(), mod.getLeftZ(), mod.getLeftY());
      }
   }
}
