package xyz.angames.astolfoclient.client.mixin;

import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.ItemPhysicsModule;

@Environment(EnvType.CLIENT)
@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer {
   private static final WeakHashMap<ItemEntityRenderState, ItemEntity> ENTITY_LINK = new WeakHashMap<>();

   @Inject(method = "updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
   private void onUpdateRenderState(ItemEntity itemEntity, ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
      ENTITY_LINK.put(state, itemEntity);
   }

   @Inject(
      method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRender(ItemEntityRenderState state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
      ItemPhysicsModule physicsModule = (ItemPhysicsModule)AstolfoclientClient.moduleManager.getModuleByName("ItemPhysics");
      if (physicsModule != null && physicsModule.isEnabled()) {
         ItemEntity itemEntity = ENTITY_LINK.get(state);
         if (itemEntity != null) {
            ItemStack itemStack = itemEntity.getStack();
            if (!itemStack.isEmpty()) {
               Item item = itemStack.getItem();
               boolean isBlock = item instanceof BlockItem;
               matrixStack.push();
               float customScale = (float)physicsModule.scale.get();
               matrixStack.scale(customScale, customScale, customScale);
               boolean isOnGround = itemEntity.isOnGround();
               float speed = (float)physicsModule.spinSpeed.get();
               float age = isOnGround
                  ? itemEntity.getItemAge()
                  : (itemEntity.getItemAge() + MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true)) * speed * 10.0F;
               matrixStack.translate(0.0F, 0.1F, 0.0F);
               if (isBlock) {
                  matrixStack.translate(0.0F, -0.05F, 0.0F);
                  if (!isOnGround) {
                     matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(age));
                     matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age));
                  } else {
                     matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(itemEntity.getId() * 45.0F));
                  }
               } else {
                  matrixStack.translate(0.0F, -0.1F, 0.0F);
                  if (!isOnGround) {
                     matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(age));
                     matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age));
                     matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(age));
                  } else {
                     matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
                     matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(itemEntity.getId() * 73.0F));
                  }
               }

               state.itemRenderState.render(matrixStack, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV);
               matrixStack.pop();
               ci.cancel();
            }
         }
      }
   }
}
