package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_7833;
import net.minecraft.class_922;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.ModelsModule;
import xyz.angames.astolfoclient.client.render.CustomModelRenderer;
import xyz.angames.astolfoclient.client.util.FriendManager;

@Environment(EnvType.CLIENT)
@Mixin(class_922.class)
public abstract class MixinLivingEntityRenderer {
   @Unique
   private CustomModelRenderer customModelRenderer;

   @Shadow
   public abstract class_583<?> method_4038();

   @Unique
   private CustomModelRenderer getCustomModelRenderer() {
      if (this.customModelRenderer == null) {
         this.customModelRenderer = new CustomModelRenderer();
      }

      return this.customModelRenderer;
   }

   @Inject(
      method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void renderCustomModel(class_10042 state, class_4587 matrices, class_4597 vertexConsumers, int light, CallbackInfo ci) {
      if (state instanceof class_10055 playerState) {
         if (this instanceof class_1007) {
            ModelsModule modelsModule = (ModelsModule)AstolfoclientClient.moduleManager.getModuleByName("Models");
            if (modelsModule != null && modelsModule.isEnabled()) {
               class_310 mc = class_310.method_1551();
               String renderedName = playerState.field_53529 != null ? playerState.field_53529 : "";
               boolean isSelf = false;
               boolean isFriend = false;
               if (mc.field_1724 != null) {
                  String myUsername = mc.method_1548().method_1676();
                  if (renderedName.toLowerCase().contains(myUsername.toLowerCase())) {
                     isSelf = true;
                  }
               }

               for (String friendName : FriendManager.getFriends()) {
                  if (renderedName.toLowerCase().contains(friendName.toLowerCase())) {
                     isFriend = true;
                     break;
                  }
               }

               boolean shouldRenderCustom = false;
               if (isSelf) {
                  shouldRenderCustom = true;
               } else if (isFriend && modelsModule.friends.get()) {
                  shouldRenderCustom = true;
               }

               if (shouldRenderCustom) {
                  class_583<?> model = this.method_4038();
                  if (model instanceof class_591 playerModel) {
                     playerModel.method_62110(playerState);
                  }

                  matrices.method_22903();
                  matrices.method_22904(0.0, playerState.field_53330 / 2.0, 0.0);
                  matrices.method_22907(class_7833.field_40716.rotationDegrees(180.0F - playerState.field_53446));
                  String mode = modelsModule.mode.get();
                  if (mode.equals("Amogus") || mode.equals("Rabbit") || mode.equals("Cow")) {
                     matrices.method_22907(new Quaternionf().rotationX((float) Math.PI));
                     if (modelsModule.changeZ.get()) {
                        matrices.method_22907(new Quaternionf().rotationY((float) Math.PI));
                     }
                  }

                  matrices.method_22904(0.0, -playerState.field_53330 / 2.0, 0.0);
                  if (mode.equals("Rabbit")) {
                     matrices.method_22904(0.0, -0.05, 0.0);
                  } else if (mode.equals("Amogus")) {
                     matrices.method_22904(0.0, -0.5, 0.0);
                  } else if (mode.equals("Cow")) {
                     matrices.method_22904(0.0, -0.2, 0.0);
                  }

                  float scale = 1.0F;
                  if (mode.equals("Rabbit")) {
                     scale = 1.25F;
                  } else if (mode.equals("Amogus")) {
                     scale = 1.8F;
                  } else if (mode.equals("Cow")) {
                     scale = 1.0F;
                  }

                  matrices.method_22905(scale, scale, scale);
                  if (model instanceof class_591 playerModel) {
                     this.getCustomModelRenderer().render(playerState, matrices, vertexConsumers, light, mode, playerModel);
                  }

                  matrices.method_22909();
                  ci.cancel();
               }
            }
         }
      }
   }
}
