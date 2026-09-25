package xyz.angames.astolfoclient.client.mixin;

import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10039;
import net.minecraft.class_1542;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;
import net.minecraft.class_916;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.ItemPhysicsModule;

@Environment(EnvType.CLIENT)
@Mixin(class_916.class)
public abstract class MixinItemEntityRenderer {
   private static final WeakHashMap<class_10039, class_1542> ENTITY_LINK = new WeakHashMap<>();

   @Inject(method = "updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
   private void onUpdateRenderState(class_1542 itemEntity, class_10039 state, float tickDelta, CallbackInfo ci) {
      ENTITY_LINK.put(state, itemEntity);
   }

   @Inject(
      method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRender(class_10039 state, class_4587 matrixStack, class_4597 vertexConsumerProvider, int light, CallbackInfo ci) {
      ItemPhysicsModule physicsModule = (ItemPhysicsModule)AstolfoclientClient.moduleManager.getModuleByName("ItemPhysics");
      if (physicsModule != null && physicsModule.isEnabled()) {
         class_1542 itemEntity = ENTITY_LINK.get(state);
         if (itemEntity != null) {
            class_1799 itemStack = itemEntity.method_6983();
            if (!itemStack.method_7960()) {
               class_1792 item = itemStack.method_7909();
               boolean isBlock = item instanceof class_1747;
               matrixStack.method_22903();
               float customScale = (float)physicsModule.scale.get();
               matrixStack.method_22905(customScale, customScale, customScale);
               boolean isOnGround = itemEntity.method_24828();
               float speed = (float)physicsModule.spinSpeed.get();
               float age = isOnGround
                  ? itemEntity.method_6985()
                  : (itemEntity.method_6985() + class_310.method_1551().method_61966().method_60637(true)) * speed * 10.0F;
               matrixStack.method_46416(0.0F, 0.1F, 0.0F);
               if (isBlock) {
                  matrixStack.method_46416(0.0F, -0.05F, 0.0F);
                  if (!isOnGround) {
                     matrixStack.method_22907(class_7833.field_40714.rotationDegrees(age));
                     matrixStack.method_22907(class_7833.field_40716.rotationDegrees(age));
                  } else {
                     matrixStack.method_22907(class_7833.field_40716.rotationDegrees(itemEntity.method_5628() * 45.0F));
                  }
               } else {
                  matrixStack.method_46416(0.0F, -0.1F, 0.0F);
                  if (!isOnGround) {
                     matrixStack.method_22907(class_7833.field_40714.rotationDegrees(age));
                     matrixStack.method_22907(class_7833.field_40716.rotationDegrees(age));
                     matrixStack.method_22907(class_7833.field_40718.rotationDegrees(age));
                  } else {
                     matrixStack.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
                     matrixStack.method_22907(class_7833.field_40718.rotationDegrees(itemEntity.method_5628() * 73.0F));
                  }
               }

               state.field_55310.method_65604(matrixStack, vertexConsumerProvider, light, class_4608.field_21444);
               matrixStack.method_22909();
               ci.cancel();
            }
         }
      }
   }
}
