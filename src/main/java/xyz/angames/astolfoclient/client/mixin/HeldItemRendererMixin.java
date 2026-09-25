package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_1806;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_4597.class_4598;
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
@Mixin(class_759.class)
public abstract class HeldItemRendererMixin {
   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("HEAD")
   )
   private void onRenderFirstPersonItemsHead(float tickDelta, class_4587 matrices, class_4598 vertexConsumers, class_746 player, int light, CallbackInfo ci) {
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
   protected abstract void method_3219(class_4587 var1, class_4597 var2, int var3, float var4, float var5, class_1306 var6);

   @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
   private void onRenderFirstPersonItem(
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      SwingAnimationModule swingMod = (SwingAnimationModule)ModuleManager.getModule(SwingAnimationModule.class);
      HandPositionModule handMod = (HandPositionModule)ModuleManager.getModule(HandPositionModule.class);
      boolean swingEnabled = swingMod != null && swingMod.isEnabled();
      boolean handEnabled = handMod != null && handMod.isEnabled();
      if ((swingEnabled || handEnabled) && !item.method_7960() && !(item.method_7909() instanceof class_1806)) {
         ci.cancel();
         if (swingMod != null) {
            swingMod.handleRenderItem(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
         }
      }
   }

   @Inject(method = "renderArmHoldingItem", at = @At("HEAD"))
   private void onRenderArmHoldingItemHead(
      class_4587 matrices, class_4597 vertexConsumers, int light, float equipProgress, float swingProgress, class_1306 arm, CallbackInfo ci
   ) {
      if (!SwingAnimationModule.renderingCustomItem) {
         HandPositionModule handMod = (HandPositionModule)ModuleManager.getModule(HandPositionModule.class);
         if (handMod != null && handMod.isEnabled()) {
            class_310 mc = class_310.method_1551();
            boolean isMainHand = mc.field_1724 != null && arm == mc.field_1724.method_6068();
            float[] pos = isMainHand ? handMod.getMainHandPos() : handMod.getOffHandPos();
            matrices.method_46416(pos[0], pos[1], pos[2]);
         }
      }
   }

   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("TAIL")
   )
   private void onRenderFirstPersonItemsTail(float tickDelta, class_4587 matrices, class_4598 vertexConsumers, class_746 player, int light, CallbackInfo ci) {
      ShaderHand mod = ShaderHand.getInstance();
      if (mod != null && ShaderHand.rendering) {
         vertexConsumers.method_22993();
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
      class_4587 matrices, class_4597 vertexConsumers, int light, float equipProgress, float swingProgress, class_1306 arm, CallbackInfo ci
   ) {
      SwingAnimationModule mod = (SwingAnimationModule)ModuleManager.getModule(SwingAnimationModule.class);
      if (mod != null && mod.isHoldMyItemsEnabled()) {
         matrices.method_46416(mod.getRightX(), mod.getRightZ(), mod.getRightY());
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
      class_4587 matrices, class_4597 vertexConsumers, int light, float equipProgress, float swingProgress, class_1306 arm, CallbackInfo ci
   ) {
      SwingAnimationModule mod = (SwingAnimationModule)ModuleManager.getModule(SwingAnimationModule.class);
      if (mod != null && mod.isHoldMyItemsEnabled()) {
         matrices.method_46416(mod.getLeftX(), mod.getLeftZ(), mod.getLeftY());
      }
   }
}
