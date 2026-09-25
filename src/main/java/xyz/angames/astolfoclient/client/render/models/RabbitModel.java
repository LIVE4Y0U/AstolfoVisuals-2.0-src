package xyz.angames.astolfoclient.client.render.models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_5603;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_591;
import net.minecraft.class_630;

@Environment(EnvType.CLIENT)
public class RabbitModel {
   private final class_630 root;
   private final class_630 rabbitBone;
   private final class_630 rabbitHead;
   private final class_630 rabbitLarm;
   private final class_630 rabbitRarm;
   private final class_630 rabbitLleg;
   private final class_630 rabbitRleg;

   public RabbitModel() {
      class_5609 modelData = new class_5609();
      class_5610 rootData = modelData.method_32111();
      class_5610 boneData = rootData.method_32117(
         "rabbitBone",
         class_5606.method_32108().method_32101(28, 45).method_32097(-5.0F, -13.0F, -5.0F, 10.0F, 11.0F, 8.0F),
         class_5603.method_32090(0.0F, 24.0F, 0.0F)
      );
      boneData.method_32117(
         "rabbitHead",
         class_5606.method_32108()
            .method_32101(0, 0)
            .method_32097(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 6.0F)
            .method_32101(56, 0)
            .method_32097(-5.0F, -9.0F, -5.0F, 2.0F, 3.0F, 2.0F)
            .method_32101(56, 0)
            .method_32097(3.0F, -9.0F, -5.0F, 2.0F, 3.0F, 2.0F)
            .method_32101(0, 45)
            .method_32097(-4.0F, -11.0F, -4.0F, 8.0F, 11.0F, 8.0F)
            .method_32101(46, 0)
            .method_32097(1.0F, -20.0F, 0.0F, 3.0F, 9.0F, 1.0F)
            .method_32101(46, 0)
            .method_32097(-4.0F, -20.0F, 0.0F, 3.0F, 9.0F, 1.0F),
         class_5603.method_32090(0.0F, -14.0F, -1.0F)
      );
      boneData.method_32117(
         "rabbitLarm",
         class_5606.method_32108().method_32101(0, 0).method_32097(0.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F),
         class_5603.method_32090(5.0F, -13.0F, -1.0F)
      );
      boneData.method_32117(
         "rabbitRarm",
         class_5606.method_32108().method_32101(0, 0).method_32097(-2.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F),
         class_5603.method_32090(-5.0F, -13.0F, -1.0F)
      );
      boneData.method_32117(
         "rabbitLleg",
         class_5606.method_32108().method_32101(0, 0).method_32097(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F),
         class_5603.method_32090(3.0F, -2.0F, -1.0F)
      );
      boneData.method_32117(
         "rabbitRleg",
         class_5606.method_32108().method_32101(0, 0).method_32097(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F),
         class_5603.method_32090(-3.0F, -2.0F, -1.0F)
      );
      class_630 modelRoot = class_5607.method_32110(modelData, 64, 64).method_32109();
      this.root = modelRoot;
      this.rabbitBone = modelRoot.method_32086("rabbitBone");
      this.rabbitHead = this.rabbitBone.method_32086("rabbitHead");
      this.rabbitLarm = this.rabbitBone.method_32086("rabbitLarm");
      this.rabbitRarm = this.rabbitBone.method_32086("rabbitRarm");
      this.rabbitLleg = this.rabbitBone.method_32086("rabbitLleg");
      this.rabbitRleg = this.rabbitBone.method_32086("rabbitRleg");
   }

   public void setAngles(class_10055 state, class_591 baseModel) {
      this.rabbitHead.field_3654 = baseModel.field_3398.field_3654;
      this.rabbitHead.field_3675 = baseModel.field_3398.field_3675;
      this.rabbitHead.field_3674 = baseModel.field_3398.field_3674;
      this.rabbitLarm.field_3654 = baseModel.field_27433.field_3654;
      this.rabbitLarm.field_3675 = baseModel.field_27433.field_3675;
      this.rabbitLarm.field_3674 = baseModel.field_27433.field_3674;
      this.rabbitRarm.field_3654 = baseModel.field_3401.field_3654;
      this.rabbitRarm.field_3675 = baseModel.field_3401.field_3675;
      this.rabbitRarm.field_3674 = baseModel.field_3401.field_3674;
      this.rabbitLleg.field_3654 = baseModel.field_3397.field_3654;
      this.rabbitLleg.field_3675 = baseModel.field_3397.field_3675;
      this.rabbitLleg.field_3674 = baseModel.field_3397.field_3674;
      this.rabbitRleg.field_3654 = baseModel.field_3392.field_3654;
      this.rabbitRleg.field_3675 = baseModel.field_3392.field_3675;
      this.rabbitRleg.field_3674 = baseModel.field_3392.field_3674;
   }

   public void render(class_4587 matrices, class_4588 vertices, int light) {
      this.root.method_22698(matrices, vertices, light, class_4608.field_21444);
   }
}
