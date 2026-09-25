package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.entity.LivingEntityRenderer;
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
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
   @Unique
   private CustomModelRenderer customModelRenderer;

   @Shadow
   public abstract EntityModel<?> getModel();

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
   private void renderCustomModel(LivingEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
      if (state instanceof PlayerEntityRenderState playerState) {
         if (((Object)this) instanceof PlayerEntityRenderer) {
            ModelsModule modelsModule = (ModelsModule)AstolfoclientClient.moduleManager.getModuleByName("Models");
            if (modelsModule != null && modelsModule.isEnabled()) {
               MinecraftClient mc = MinecraftClient.getInstance();
               String renderedName = playerState.name != null ? playerState.name : "";
               boolean isSelf = false;
               boolean isFriend = false;
               if (mc.player != null) {
                  String myUsername = mc.getSession().getUsername();
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
                  EntityModel<?> model = this.getModel();
                  if (model instanceof PlayerEntityModel playerModel) {
                     playerModel.setAngles(playerState);
                  }

                  matrices.push();
                  matrices.translate(0.0, playerState.height / 2.0, 0.0);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - playerState.bodyYaw));
                  String mode = modelsModule.mode.get();
                  if (mode.equals("Amogus") || mode.equals("Rabbit") || mode.equals("Cow")) {
                     matrices.multiply(new Quaternionf().rotationX((float) Math.PI));
                     if (modelsModule.changeZ.get()) {
                        matrices.multiply(new Quaternionf().rotationY((float) Math.PI));
                     }
                  }

                  matrices.translate(0.0, -playerState.height / 2.0, 0.0);
                  if (mode.equals("Rabbit")) {
                     matrices.translate(0.0, -0.05, 0.0);
                  } else if (mode.equals("Amogus")) {
                     matrices.translate(0.0, -0.5, 0.0);
                  } else if (mode.equals("Cow")) {
                     matrices.translate(0.0, -0.2, 0.0);
                  }

                  float scale = 1.0F;
                  if (mode.equals("Rabbit")) {
                     scale = 1.25F;
                  } else if (mode.equals("Amogus")) {
                     scale = 1.8F;
                  } else if (mode.equals("Cow")) {
                     scale = 1.0F;
                  }

                  matrices.scale(scale, scale, scale);
                  if (model instanceof PlayerEntityModel playerModel) {
                     this.getCustomModelRenderer().render(playerState, matrices, vertexConsumers, light, mode, playerModel);
                  }

                  matrices.pop();
                  ci.cancel();
               }
            }
         }
      }
   }
}
