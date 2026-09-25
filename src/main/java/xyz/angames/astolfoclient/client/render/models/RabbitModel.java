package xyz.angames.astolfoclient.client.render.models;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.model.ModelPart;

@Environment(EnvType.CLIENT)
public class RabbitModel {
   private final client.model.ModelPart root;
   private final client.model.ModelPart rabbitBone;
   private final client.model.ModelPart rabbitHead;
   private final client.model.ModelPart rabbitLarm;
   private final client.model.ModelPart rabbitRarm;
   private final client.model.ModelPart rabbitLleg;
   private final client.model.ModelPart rabbitRleg;

   public RabbitModel() {
      client.model.ModelData modelData = new client.model.ModelData();
      client.model.ModelPartData rootData = modelData.getRoot();
      client.model.ModelPartData boneData = rootData.addChild(
         "rabbitBone",
         client.model.ModelPartBuilder.create().uv(28, 45).cuboid(-5.0F, -13.0F, -5.0F, 10.0F, 11.0F, 8.0F),
         client.model.ModelTransform.pivot(0.0F, 24.0F, 0.0F)
      );
      boneData.addChild(
         "rabbitHead",
         client.model.ModelPartBuilder.create()
            .uv(0, 0)
            .cuboid(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 6.0F)
            .uv(56, 0)
            .cuboid(-5.0F, -9.0F, -5.0F, 2.0F, 3.0F, 2.0F)
            .uv(56, 0)
            .cuboid(3.0F, -9.0F, -5.0F, 2.0F, 3.0F, 2.0F)
            .uv(0, 45)
            .cuboid(-4.0F, -11.0F, -4.0F, 8.0F, 11.0F, 8.0F)
            .uv(46, 0)
            .cuboid(1.0F, -20.0F, 0.0F, 3.0F, 9.0F, 1.0F)
            .uv(46, 0)
            .cuboid(-4.0F, -20.0F, 0.0F, 3.0F, 9.0F, 1.0F),
         client.model.ModelTransform.pivot(0.0F, -14.0F, -1.0F)
      );
      boneData.addChild(
         "rabbitLarm",
         client.model.ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F),
         client.model.ModelTransform.pivot(5.0F, -13.0F, -1.0F)
      );
      boneData.addChild(
         "rabbitRarm",
         client.model.ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F),
         client.model.ModelTransform.pivot(-5.0F, -13.0F, -1.0F)
      );
      boneData.addChild(
         "rabbitLleg",
         client.model.ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F),
         client.model.ModelTransform.pivot(3.0F, -2.0F, -1.0F)
      );
      boneData.addChild(
         "rabbitRleg",
         client.model.ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F),
         client.model.ModelTransform.pivot(-3.0F, -2.0F, -1.0F)
      );
      client.model.ModelPart modelRoot = client.model.TexturedModelData.of(modelData, 64, 64).createModel();
      this.root = modelRoot;
      this.rabbitBone = modelRoot.getChild("rabbitBone");
      this.rabbitHead = this.rabbitBone.getChild("rabbitHead");
      this.rabbitLarm = this.rabbitBone.getChild("rabbitLarm");
      this.rabbitRarm = this.rabbitBone.getChild("rabbitRarm");
      this.rabbitLleg = this.rabbitBone.getChild("rabbitLleg");
      this.rabbitRleg = this.rabbitBone.getChild("rabbitRleg");
   }

   public void setAngles(entity.state.PlayerEntityRenderState state, entity.model.PlayerEntityModel baseModel) {
      this.rabbitHead.pitch = baseModel.head.pitch;
      this.rabbitHead.yaw = baseModel.head.yaw;
      this.rabbitHead.roll = baseModel.head.roll;
      this.rabbitLarm.pitch = baseModel.leftArm.pitch;
      this.rabbitLarm.yaw = baseModel.leftArm.yaw;
      this.rabbitLarm.roll = baseModel.leftArm.roll;
      this.rabbitRarm.pitch = baseModel.rightArm.pitch;
      this.rabbitRarm.yaw = baseModel.rightArm.yaw;
      this.rabbitRarm.roll = baseModel.rightArm.roll;
      this.rabbitLleg.pitch = baseModel.leftLeg.pitch;
      this.rabbitLleg.yaw = baseModel.leftLeg.yaw;
      this.rabbitLleg.roll = baseModel.leftLeg.roll;
      this.rabbitRleg.pitch = baseModel.rightLeg.pitch;
      this.rabbitRleg.yaw = baseModel.rightLeg.yaw;
      this.rabbitRleg.roll = baseModel.rightLeg.roll;
   }

   public void render(util.math.MatrixStack matrices, client.render.VertexConsumer vertices, int light) {
      this.root.render(matrices, vertices, light, client.render.OverlayTexture.DEFAULT_UV);
   }
}
