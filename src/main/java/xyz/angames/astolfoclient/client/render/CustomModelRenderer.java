package xyz.angames.astolfoclient.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import xyz.angames.astolfoclient.client.render.models.CowModel;
import xyz.angames.astolfoclient.client.render.models.RabbitModel;

@Environment(EnvType.CLIENT)
public class CustomModelRenderer {
   private final RabbitModel rabbitModel = new RabbitModel();
   private final CowModel cowModel = new CowModel();
   private static final Identifier RABBIT_TEXTURE = Identifier.of("astolfoclient", "textures/models/rabbit.png");
   private static final Identifier AMOGUS_TEXTURE = Identifier.of("astolfoclient", "textures/models/amogus.png");

   public void render(PlayerEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, String mode, PlayerEntityModel baseModel) {
      if (mode.equals("Rabbit")) {
         this.rabbitModel.setAngles(state, baseModel);
         VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(RABBIT_TEXTURE));
         this.rabbitModel.render(matrices, buffer, light);
      } else if (mode.equals("Cow")) {
         this.cowModel.render(matrices, vertexConsumers, state, light);
      } else if (mode.equals("Amogus")) {
      }
   }
}
