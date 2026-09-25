package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(class_922.class)
public abstract class LivingEntityRendererMixin {
   @Inject(
      method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD")
   )
   private void shrinkToBabySize(class_10042 state, class_4587 matrixStack, class_4597 vertexConsumerProvider, int i, CallbackInfo ci) {
      if (state instanceof class_10055 playerState
         && class_310.method_1551().field_1724 != null
         && playerState.field_53528 == class_310.method_1551().field_1724.method_5628()
         && AstolfoclientClient.moduleManager != null) {
         Module babyMod = AstolfoclientClient.moduleManager.getModuleByName("BabyPlayer");
         if (babyMod != null && babyMod.isEnabled()) {
            matrixStack.method_22905(0.5F, 0.5F, 0.5F);
         }
      }
   }
}
