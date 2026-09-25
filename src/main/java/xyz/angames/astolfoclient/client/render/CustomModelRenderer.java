package xyz.angames.astolfoclient.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import xyz.angames.astolfoclient.client.render.models.CowModel;
import xyz.angames.astolfoclient.client.render.models.RabbitModel;

@Environment(EnvType.CLIENT)
public class CustomModelRenderer {
   private final RabbitModel rabbitModel = new RabbitModel();
   private final CowModel cowModel = new CowModel();
   private static final class_2960 RABBIT_TEXTURE = class_2960.method_60655("astolfoclient", "textures/models/rabbit.png");
   private static final class_2960 AMOGUS_TEXTURE = class_2960.method_60655("astolfoclient", "textures/models/amogus.png");

   public void render(class_10055 state, class_4587 matrices, class_4597 vertexConsumers, int light, String mode, class_591 baseModel) {
      if (mode.equals("Rabbit")) {
         this.rabbitModel.setAngles(state, baseModel);
         class_4588 buffer = vertexConsumers.getBuffer(class_1921.method_23580(RABBIT_TEXTURE));
         this.rabbitModel.render(matrices, buffer, light);
      } else if (mode.equals("Cow")) {
         this.cowModel.render(matrices, vertexConsumers, state, light);
      } else if (mode.equals("Amogus")) {
      }
   }
}
