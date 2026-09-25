package xyz.angames.astolfoclient.client.module.modules.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.PaneBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import xyz.angames.astolfoclient.client.mixin.HeldItemRendererAccessor;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.ModuleManager;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;
import xyz.angames.astolfoclient.client.module.setting.ModeSetting;
import xyz.angames.astolfoclient.client.module.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public class SwingAnimationModule extends Module {
   public final ModeSetting mode = new ModeSetting("Mode", "Mode 1", "Mode 1", "Mode 2", "Mode 3", "Mode 4", "Mode 5", "Vanilla", "HMI");
   public final NumberSetting strength = new NumberSetting("Strength", 20.0, 20.0, 75.0, 0.1);
   public final BooleanSetting slow = new BooleanSetting("Slow", false);
   public final NumberSetting speed = new NumberSetting("Speed", 12.0, 1.0, 50.0, 1.0);
   public final ModeSetting attackMode = new ModeSetting("Attack Mode", "Swings", "Swings", "Forward", "Normal") {
      @Override
      public boolean isVisible() {
         return SwingAnimationModule.this.isHmiVisible();
      }
   };
   public final NumberSetting rightX = new NumberSetting("Right X", 0.0, -2.0, 2.0, 0.1) {
      @Override
      public boolean isVisible() {
         return SwingAnimationModule.this.isHmiVisible();
      }
   };
   public final NumberSetting rightZ = new NumberSetting("Right Y", 0.0, -2.0, 2.0, 0.1) {
      @Override
      public boolean isVisible() {
         return SwingAnimationModule.this.isHmiVisible();
      }
   };
   public final NumberSetting rightY = new NumberSetting("Right Z", 0.0, -2.0, 2.0, 0.1) {
      @Override
      public boolean isVisible() {
         return SwingAnimationModule.this.isHmiVisible();
      }
   };
   public final NumberSetting leftX = new NumberSetting("Left X", 0.0, -2.0, 2.0, 0.1) {
      @Override
      public boolean isVisible() {
         return SwingAnimationModule.this.isHmiVisible();
      }
   };
   public final NumberSetting leftZ = new NumberSetting("Left Y", 0.0, -2.0, 2.0, 0.1) {
      @Override
      public boolean isVisible() {
         return SwingAnimationModule.this.isHmiVisible();
      }
   };
   public final NumberSetting leftY = new NumberSetting("Left Z", 0.0, -2.0, 2.0, 0.1) {
      @Override
      public boolean isVisible() {
         return SwingAnimationModule.this.isHmiVisible();
      }
   };
   public static boolean renderingCustomItem = false;
   private static final double HOLD_MY_ITEMS_MAX_DELTA = 0.05;
   private static final double HOLD_MY_ITEMS_ANIMATION_SPEED = 30.0;
   private double holdMyItemsPrevFrameTime = System.nanoTime() / 1.0E9;
   private double holdMyItemsDeltaTime;
   private double holdMyItemsPreviousRotation;
   private float holdMyItemsSwingAngleY;
   private float holdMyItemsSwingAngleX;
   private float holdMyItemsSwingVelocityY;
   private float holdMyItemsSwingVelocityX;
   private float holdMyItemsSwingVelocityZ;
   private float holdMyItemsVertAngleY;
   private float holdMyItemsVertVelocityYSlime;
   private float holdMyItemsVertAngleYSlime;
   private float holdMyItemsClimbBlend;
   private float holdMyItemsCrawlCount;
   private float holdMyItemsDirectionalCrawlCount;
   private float holdMyItemsClimbCount;
   private float holdMyItemsInWaterCounter;
   private boolean holdMyItemsIsAttacking;
   private boolean holdMyItemsLeft;
   private float holdMyItemsPrevSwingProgress;
   private boolean holdMyItemsPhysicsUpdatedThisFrame;
   private float chestRightHandMotion;

   private boolean isHmiVisible() {
      return "HMI".equals(this.mode.get());
   }

   public SwingAnimationModule() {
      super("SwingAnimation", "Custom Swing Animations", Module.Category.RENDER);
      this.addSettings(
         this.mode, this.strength, this.slow, this.speed, this.attackMode, this.rightX, this.rightZ, this.rightY, this.leftX, this.leftZ, this.leftY
      );
   }

   private float getFloat(NumberSetting setting) {
      return Double.valueOf(setting.getValue()).floatValue();
   }

   public boolean isAuraActive() {
      return false;
   }

   private void applyHandPosition(util.math.MatrixStack matrices, minecraft.util.Arm arm) {
      this.applyHandPositionBase(matrices, arm);
      this.applyHandPositionItem(matrices, arm);
   }

   private void applyHandPositionBase(util.math.MatrixStack matrices, minecraft.util.Arm arm) {
      HandPositionModule handPos = (HandPositionModule)ModuleManager.getModule(HandPositionModule.class);
      if (handPos != null && handPos.isEnabled()) {
         minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
         boolean isMainHand = mc.player != null && arm == mc.player.getMainArm();
         float[] pos = isMainHand ? handPos.getMainHandPos() : handPos.getOffHandPos();
         matrices.translate(pos[0], pos[1], pos[2]);
      }
   }

   private void applyHandPositionItem(util.math.MatrixStack matrices, minecraft.util.Arm arm) {
      HandPositionModule handPos = (HandPositionModule)ModuleManager.getModule(HandPositionModule.class);
      if (handPos != null && handPos.isEnabled()) {
         minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
         boolean isMainHand = mc.player != null && arm == mc.player.getMainArm();
         float[] rot = isMainHand ? handPos.getMainHandRot() : handPos.getOffHandRot();
         float[] scale = isMainHand ? handPos.getMainHandScale() : handPos.getOffHandScale();
         if (rot[0] != 0.0F) {
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(rot[0]));
         }

         if (rot[1] != 0.0F) {
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(rot[1]));
         }

         if (rot[2] != 0.0F) {
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(rot[2]));
         }

         if (scale[0] != 1.0F || scale[1] != 1.0F || scale[2] != 1.0F) {
            matrices.scale(scale[0], scale[1], scale[2]);
         }
      }
   }

   private void handleSwordAnim(util.math.MatrixStack matrices, float swingProgress, float equipProgress, minecraft.util.Arm arm) {
      float str = this.getFloat(this.strength);
      float g = util.math.MathHelper.sin(util.math.MathHelper.sqrt(swingProgress) * (float) Math.PI);
      float anim = (float)Math.sin(swingProgress * (Math.PI / 2) * 2.0);
      float isLeft = arm == minecraft.util.Arm.LEFT ? -1.0F : 1.0F;
      String currentMode = String.valueOf(this.mode.get()).toUpperCase();
      if (currentMode.contains("VANILLA")) {
         float n = -0.4F * util.math.MathHelper.sin(util.math.MathHelper.sqrt(swingProgress) * (float) Math.PI);
         float mxx = 0.2F * util.math.MathHelper.sin(util.math.MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
         float fxxx = -0.2F * util.math.MathHelper.sin(swingProgress * (float) Math.PI);
         matrices.translate(isLeft * n, mxx, fxxx);
         this.applyEquipOffset(matrices, arm, equipProgress);
         this.applySwingOffset(matrices, arm, swingProgress);
      } else {
         this.applyEquipOffset(matrices, arm, 0.0F);
         matrices.scale(1.0F, 1.0F, 1.0F);
         if (currentMode.contains("1")) {
            this.applySwingOffset(matrices, arm, swingProgress);
         } else if (currentMode.contains("2")) {
            matrices.translate(isLeft * -0.1F, 0.15F, -0.1F);
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(isLeft * -60.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(50.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(isLeft * (110.0F + str * g)));
         } else if (currentMode.contains("3")) {
            matrices.translate(isLeft * -0.1F, 0.15F, 0.0F);
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(50.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(isLeft * (-30.0F + str * g)));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(isLeft * 110.0F));
         } else if (currentMode.contains("4")) {
            matrices.translate(isLeft * -0.15F, 0.2F, 0.0F);
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(isLeft * 90.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(isLeft * -30.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-90.0F - str * anim + 10.0F));
         } else if (currentMode.contains("5")) {
            this.applySwingOffset(matrices, arm, swingProgress);
            float spinAngle = swingProgress * 360.0F;
            matrices.translate(0.0F, 0.0F, 0.0F);
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-isLeft * spinAngle));
            matrices.translate(0.0F, 0.0F, 0.0F);
         }
      }
   }

   public void handleRenderItem(
      client.network.AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      minecraft.util.Hand hand,
      float swingProgress,
      minecraft.item.ItemStack item,
      float equipProgress,
      util.math.MatrixStack matrices,
      client.render.VertexConsumerProvider vertexConsumers,
      int light
   ) {
      minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
      if (mc.player != null) {
         renderingCustomItem = true;

         try {
            if (this.isHoldMyItemsEnabled()) {
               if (this.shouldUseHoldMyItemsBow(player, hand, item)) {
                  this.renderHMIBow(player, tickDelta, hand, swingProgress, item, 0.0F, matrices, vertexConsumers, light);
                  return;
               }

               if (this.shouldUseHoldMyItemsConsume(player, hand, item)) {
                  this.renderHMIConsume(player, tickDelta, hand, swingProgress, item, 0.0F, matrices, vertexConsumers, light);
                  return;
               }

               if (this.shouldUseHoldMyItemsCustom(player, hand, item)) {
                  this.renderHMI(player, tickDelta, pitch, hand, swingProgress, item, 0.0F, matrices, vertexConsumers, light);
                  return;
               }
            }

            if (!player.isUsingSpyglass()) {
               boolean isMainHand = hand == minecraft.util.Hand.MAIN_HAND;
               minecraft.util.Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
               boolean isRightArm = arm == minecraft.util.Arm.RIGHT;
               int i = isRightArm ? 1 : -1;
               matrices.push();
               this.applyHandPositionBase(matrices, arm);
               if (item.isOf(minecraft.item.Items.CROSSBOW)) {
                  boolean isCharged = minecraft.item.CrossbowItem.isCharged(item);
                  if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     matrices.translate(i * -0.4785682F, -0.094387F, 0.05731531F);
                     matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
                     matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(i * 65.3F));
                     matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(i * -9.785F));
                     float f = item.getMaxUseTime(mc.player) - (mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                     float g = f / minecraft.item.CrossbowItem.getPullTime(item, mc.player);
                     if (g > 1.0F) {
                        g = 1.0F;
                     }

                     if (g > 0.1F) {
                        float h = util.math.MathHelper.sin((f - 0.1F) * 1.3F);
                        float j = g - 0.1F;
                        float k = h * j;
                        matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
                     }

                     matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
                     matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
                     matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(i * 45.0F));
                  } else {
                     float fx = -0.4F * util.math.MathHelper.sin(util.math.MathHelper.sqrt(swingProgress) * (float) Math.PI);
                     float gx = 0.2F * util.math.MathHelper.sin(util.math.MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
                     float h = -0.2F * util.math.MathHelper.sin(swingProgress * (float) Math.PI);
                     matrices.translate(i * fx, gx, h);
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     this.applySwingOffset(matrices, arm, swingProgress);
                     if (isCharged && swingProgress < 0.001F && isMainHand) {
                        matrices.translate(i * -0.641864F, 0.0F, 0.0F);
                        matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(i * 10.0F));
                     }
                  }

                  this.applyHandPositionItem(matrices, arm);
                  this.renderItem(player, item, isRightArm ? minecraft.item.ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : minecraft.item.ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !isRightArm, matrices, vertexConsumers, light);
               } else {
                  if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
                     int l = isRightArm ? 1 : -1;
                     switch (item.getUseAction()) {
                        case NONE:
                        case BLOCK:
                           this.applyEquipOffset(matrices, arm, equipProgress);
                           break;
                        case EAT:
                        case DRINK:
                           this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item);
                           this.applyEquipOffset(matrices, arm, equipProgress);
                           break;
                        case BOW:
                           this.applyEquipOffset(matrices, arm, equipProgress);
                           matrices.translate(l * -0.2785682F, 0.18344387F, 0.15731531F);
                           matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                           matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(l * 35.3F));
                           matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(l * -9.785F));
                           float mx = item.getMaxUseTime(mc.player) - (mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                           float fxx = mx / 20.0F;
                           fxx = (fxx * fxx + fxx * 2.0F) / 3.0F;
                           if (fxx > 1.0F) {
                              fxx = 1.0F;
                           }

                           if (fxx > 0.1F) {
                              float gx = util.math.MathHelper.sin((mx - 0.1F) * 1.3F);
                              float h = fxx - 0.1F;
                              float j = gx * h;
                              matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                           }

                           matrices.translate(fxx * 0.0F, fxx * 0.0F, fxx * 0.04F);
                           matrices.scale(1.0F, 1.0F, 1.0F + fxx * 0.2F);
                           matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(l * 45.0F));
                           break;
                        case SPEAR:
                           this.applyEquipOffset(matrices, arm, equipProgress);
                           matrices.translate(l * -0.5F, 0.7F, 0.1F);
                           matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                           matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(l * 35.3F));
                           matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(l * -9.785F));
                           float m = item.getMaxUseTime(mc.player) - (mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                           float fx = m / 10.0F;
                           if (fx > 1.0F) {
                              fx = 1.0F;
                           }

                           if (fx > 0.1F) {
                              float gx = util.math.MathHelper.sin((m - 0.1F) * 1.3F);
                              float h = fx - 0.1F;
                              float j = gx * h;
                              matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                           }

                           matrices.translate(0.0F, 0.0F, fx * 0.2F);
                           matrices.scale(1.0F, 1.0F, 1.0F + fx * 0.2F);
                           matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(l * 45.0F));
                           break;
                        case BRUSH:
                           this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
                     }
                  } else if (player.isUsingRiptide()) {
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     int l = isRightArm ? 1 : -1;
                     matrices.translate(l * -0.4F, 0.8F, 0.3F);
                     matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(l * 65.0F));
                     matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(l * -85.0F));
                  } else if (arm == mc.options.getMainArm().getValue() && this.isEnabled()) {
                     this.handleSwordAnim(matrices, swingProgress, equipProgress, arm);
                  } else {
                     float n = -0.4F * util.math.MathHelper.sin(util.math.MathHelper.sqrt(swingProgress) * (float) Math.PI);
                     float mxx = 0.2F * util.math.MathHelper.sin(util.math.MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
                     float fxxx = -0.2F * util.math.MathHelper.sin(swingProgress * (float) Math.PI);
                     int o = isRightArm ? 1 : -1;
                     matrices.translate(o * n, mxx, fxxx);
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     this.applySwingOffset(matrices, arm, swingProgress);
                  }

                  this.applyHandPositionItem(matrices, arm);
                  this.renderItem(player, item, isRightArm ? minecraft.item.ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : minecraft.item.ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !isRightArm, matrices, vertexConsumers, light);
               }

               matrices.pop();
            }
         } finally {
            renderingCustomItem = false;
         }
      }
   }

   private void applyBrushTransformation(util.math.MatrixStack matrices, float tickDelta, minecraft.util.Arm arm, minecraft.item.ItemStack stack, float equipProgress) {
      minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
      this.applyEquipOffset(matrices, arm, equipProgress);
      float f = mc.player.getItemUseTimeLeft() % 10;
      float g = f - tickDelta + 1.0F;
      float h = 1.0F - g / 10.0F;
      float n = -15.0F + 75.0F * util.math.MathHelper.cos(h * 2.0F * (float) Math.PI);
      if (arm != minecraft.util.Arm.RIGHT) {
         matrices.translate(0.1, 0.83, 0.35);
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(n));
         matrices.translate(-0.3, 0.22, 0.35);
      } else {
         matrices.translate(-0.25, 0.22, 0.35);
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(n));
      }
   }

   private void applyEatOrDrinkTransformation(util.math.MatrixStack matrices, float tickDelta, minecraft.util.Arm arm, minecraft.item.ItemStack stack) {
      minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
      float f = mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
      float g = f / stack.getMaxUseTime(mc.player);
      if (g < 0.8F) {
         float h = util.math.MathHelper.abs(util.math.MathHelper.cos(f / 4.0F * (float) Math.PI) * 0.1F);
         matrices.translate(0.0F, h, 0.0F);
      }

      float h = 1.0F - (float)Math.pow(g, 27.0);
      int i = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      matrices.translate(h * 0.6F * i, h * -0.5F, h * 0.0F);
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(i * h * 90.0F));
      matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(i * h * 30.0F));
   }

   private void applyEquipOffset(util.math.MatrixStack matrices, minecraft.util.Arm arm, float equipProgress) {
      int i = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      matrices.translate(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
   }

   private void applySwingOffset(util.math.MatrixStack matrices, minecraft.util.Arm arm, float swingProgress) {
      int i = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      float f = util.math.MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
      float g = util.math.MathHelper.sin(util.math.MathHelper.sqrt(swingProgress) * (float) Math.PI);
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(i * (45.0F + f * -20.0F)));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(i * g * -20.0F));
      matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(i * -45.0F));
   }

   public void renderItem(
      minecraft.entity.LivingEntity entity, minecraft.item.ItemStack stack, minecraft.item.ModelTransformationMode renderMode, boolean leftHanded, util.math.MatrixStack matrices, client.render.VertexConsumerProvider vertexConsumers, int light
   ) {
      if (!stack.isEmpty()) {
         render.entity.EntityRenderDispatcher dispatcher = minecraft.client.MinecraftClient.getInstance().getEntityRenderDispatcher();
         if (dispatcher != null && dispatcher.getHeldItemRenderer() != null) {
            dispatcher.getHeldItemRenderer().renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light);
         } else {
            minecraft.client.MinecraftClient.getInstance()
               .getItemRenderer()
               .renderItem(
                  entity,
                  stack,
                  renderMode,
                  leftHanded,
                  matrices,
                  vertexConsumers,
                  entity.getWorld(),
                  light,
                  client.render.OverlayTexture.DEFAULT_UV,
                  entity.getId() + renderMode.ordinal()
               );
         }
      }
   }

   public boolean auraCheck() {
      return true;
   }

   public float getRightX() {
      return this.getFloat(this.rightX);
   }

   public float getRightY() {
      return this.getFloat(this.rightY);
   }

   public float getRightZ() {
      return this.getFloat(this.rightZ);
   }

   public float getLeftX() {
      return this.getFloat(this.leftX);
   }

   public float getLeftY() {
      return this.getFloat(this.leftY);
   }

   public float getLeftZ() {
      return this.getFloat(this.leftZ);
   }

   public boolean isHoldMyItemsEnabled() {
      return !this.isEnabled() ? false : "HMI".equals(String.valueOf(this.mode.get()));
   }

   public void updatePhysics(client.network.ClientPlayerEntity player, float tickDelta) {
      if (this.isEnabled() && this.isHoldMyItemsEnabled()) {
         double currentTime = System.nanoTime() / 1.0E9;
         this.holdMyItemsDeltaTime = Math.min(0.05, Math.max(0.0, currentTime - this.holdMyItemsPrevFrameTime));
         this.holdMyItemsPrevFrameTime = currentTime;
         this.holdMyItemsPhysicsUpdatedThisFrame = false;
         float f = player.getHandSwingProgress(tickDelta);
         if (f > 0.0F && this.holdMyItemsPrevSwingProgress == 0.0F) {
            this.holdMyItemsLeft = !this.holdMyItemsLeft;
         }

         this.holdMyItemsPrevSwingProgress = f;
      }
   }

   private boolean shouldUseHoldMyItemsCustom(client.network.AbstractClientPlayerEntity player, minecraft.util.Hand handIn, minecraft.item.ItemStack stack) {
      return !this.isHoldMyItemsEnabled()
         ? false
         : !(stack.getItem() instanceof minecraft.item.FilledMapItem)
            && !(stack.getItem() instanceof minecraft.item.CrossbowItem)
            && (!player.isUsingItem() || player.getActiveHand() != handIn)
            && !player.isUsingRiptide();
   }

   private boolean shouldUseHoldMyItemsBow(client.network.AbstractClientPlayerEntity player, minecraft.util.Hand handIn, minecraft.item.ItemStack stack) {
      return !this.isHoldMyItemsEnabled() ? false : stack.getUseAction() == item.consume.UseAction.BOW && player.isUsingItem() && player.getActiveHand() == handIn;
   }

   private boolean shouldUseHoldMyItemsConsume(client.network.AbstractClientPlayerEntity player, minecraft.util.Hand handIn, minecraft.item.ItemStack stack) {
      if (!this.isHoldMyItemsEnabled()) {
         return false;
      }

      minecraft.item.ItemStack consumeStack = player.getActiveItem() != null && !player.getActiveItem().isEmpty() && player.getActiveHand() == handIn
         ? player.getActiveItem()
         : stack;
      item.consume.UseAction action = consumeStack.getUseAction();
      return (action == item.consume.UseAction.EAT || action == item.consume.UseAction.DRINK) && player.isUsingItem() && player.getActiveHand() == handIn;
   }

   private void updateChestRightHandMotion() {
      minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
      float target = mc.currentScreen instanceof screen.ingame.HandledScreen && !(mc.currentScreen instanceof screen.ingame.InventoryScreen) ? 1.0F : 0.0F;
      this.chestRightHandMotion = util.math.MathHelper.lerp(0.18F, this.chestRightHandMotion, target);
   }

   private float getHoldMyItemsAttackDamage(minecraft.item.ItemStack stack) {
      if (stack.isEmpty()) {
         return 0.0F;
      }

      String name = stack.getItem().toString().toLowerCase();
      if (name.contains("sword")) {
         if (name.contains("netherite")) {
            return 8.0F;
         } else if (name.contains("diamond")) {
            return 7.0F;
         } else if (name.contains("iron")) {
            return 6.0F;
         } else {
            return name.contains("stone") ? 5.0F : 4.0F;
         }
      } else if (name.contains("axe")) {
         return !name.contains("netherite") && !name.contains("diamond") && !name.contains("iron") && !name.contains("stone") ? 7.0F : 9.0F;
      } else {
         return 0.0F;
      }
   }

   private boolean isHoldMyItemsCrawling(client.network.AbstractClientPlayerEntity player) {
      return player.isInSwimmingPose() && !player.isTouchingWater();
   }

   private boolean isHoldMyItemsClimbing(client.network.AbstractClientPlayerEntity player) {
      return player.isClimbing() && !player.isOnGround() && Math.abs(player.getVelocity().y) > 0.0;
   }

   private boolean isHoldMyItemsWeapon(minecraft.item.ItemStack stack) {
      return stack.getItem() instanceof minecraft.item.SwordItem || stack.getItem() instanceof minecraft.item.AxeItem;
   }

   private boolean isHoldMyItemsTool(minecraft.item.ItemStack stack) {
      return stack.getItem() instanceof minecraft.item.MiningToolItem || stack.getItem() instanceof minecraft.item.ShearsItem || stack.getItem() instanceof minecraft.item.TridentItem;
   }

   private boolean isHoldMyItemsShovel(minecraft.item.ItemStack stack) {
      return stack.getItem() instanceof minecraft.item.ShovelItem;
   }

   private boolean isHoldMyItemsLantern(minecraft.item.ItemStack stack) {
      return stack.isOf(minecraft.item.Items.LANTERN) || stack.isOf(minecraft.item.Items.SOUL_LANTERN);
   }

   private boolean isHoldMyItemsThinBlock(minecraft.item.ItemStack stack) {
      if (!(stack.getItem() instanceof minecraft.item.BlockItem)) {
         return false;
      }

      minecraft.block.Block block = ((minecraft.item.BlockItem)stack.getItem()).getBlock();
      return stack.isOf(minecraft.item.Items.STRING)
         || stack.isOf(minecraft.item.Items.REDSTONE)
         || stack.isOf(minecraft.item.Items.LEVER)
         || stack.isOf(minecraft.item.Items.TRIPWIRE_HOOK)
         || block instanceof minecraft.block.PaneBlock
         || block.getDefaultState().isIn(registry.tag.BlockTags.RAILS)
         || block.getDefaultState().isIn(registry.tag.BlockTags.CLIMBABLE)
         || block.getDefaultState().isIn(registry.tag.BlockTags.DOORS);
   }

   private boolean isHoldMyItemsTorch(minecraft.item.ItemStack stack) {
      String name = stack.getName().getString().toLowerCase();
      return name.contains("torch") || name.contains("факел");
   }

   private boolean isHoldMyItemsSmallItem(minecraft.item.ItemStack stack) {
      return !(stack.getItem() instanceof minecraft.item.BlockItem)
         && !this.isHoldMyItemsTool(stack)
         && !this.isHoldMyItemsWeapon(stack)
         && !(stack.getItem() instanceof minecraft.item.FishingRodItem)
         && !(stack.getItem() instanceof minecraft.item.BucketItem)
         && stack.getUseAction() != item.consume.UseAction.BOW
         && stack.getUseAction() != item.consume.UseAction.SPEAR
         && stack.getUseAction() != item.consume.UseAction.BLOCK;
   }

   private float holdMyItemsEase(float value) {
      float c1 = 1.70158F;
      float c2 = c1 * 1.525F;
      if (value < 0.5F) {
         float doubled = 2.0F * value;
         return doubled * doubled * ((c2 + 1.0F) * doubled - c2) * 0.5F;
      } else {
         float shifted = 2.0F * value - 2.0F;
         return (shifted * shifted * ((c2 + 1.0F) * shifted + c2) + 2.0F) * 0.5F;
      }
   }

   private float getHoldMyItemsSwingRot(float swingProgress) {
      return swingProgress < 0.6F
         ? util.math.MathHelper.sin(util.math.MathHelper.clamp(swingProgress, 0.0F, 0.12506F) * 12.56F)
         : util.math.MathHelper.sin(util.math.MathHelper.clamp(swingProgress, 0.62532F, 0.75038F) * 12.56F);
   }

   private void applyHoldMyItemsBaseHandPose(util.math.MatrixStack matrices, minecraft.util.Arm arm, float equippedProgress, float swingProgress) {
      int direction = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      float swingSin = util.math.MathHelper.sin(swingProgress * (float) Math.PI);
      matrices.translate(direction, -equippedProgress * 0.3, 0.3);
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * direction));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-40.0F * direction));
      matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(direction * (45.0F + swingSin * 0.0F)));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(direction * -45.0F));
      matrices.scale(0.9F, 0.9F, 0.9F);
   }

   private void applyHoldMyItemsArmPrePose(util.math.MatrixStack matrices, minecraft.item.ItemStack stack, minecraft.util.Arm arm) {
      int direction = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      if (this.isHoldMyItemsLantern(stack)) {
         matrices.translate(0.1 * direction, 0.0, -0.1);
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(10.0F));
      } else {
         if (stack.getUseAction() == item.consume.UseAction.BLOCK) {
            matrices.translate(0.0, -0.2, 0.0);
         }
      }
   }

   private void applyHoldMyItemsEnvironment(
      util.math.MatrixStack matrices, client.network.AbstractClientPlayerEntity player, minecraft.util.Hand handIn, minecraft.util.Arm arm, minecraft.item.ItemStack stack, float swingProgress, float partialTicks
   ) {
      float yaw = util.math.MathHelper.lerp(partialTicks, player.prevYaw, player.getYaw());
      double radians = Math.toRadians(yaw);
      double forwardX = -Math.sin(radians);
      double forwardZ = Math.cos(radians);
      util.math.Vec3d velocity = player.getVelocity();
      double dotProduct = velocity.x * forwardX + velocity.z * forwardZ;
      double crossProduct = velocity.x * forwardZ - velocity.z * forwardX;
      float pitchFactor = player.getPitch() != 0.0F ? 90.0F / player.getPitch() / 10.0F : 1.0F;
      if (pitchFactor > 1.0F || pitchFactor < 0.0F) {
         pitchFactor = 1.0F;
      }

      boolean crawling = this.isHoldMyItemsCrawling(player);
      boolean climbing = this.isHoldMyItemsClimbing(player);
      boolean elytraFlying = player.isGliding();
      double tt = this.holdMyItemsDeltaTime * 30.0;
      float handDirection = handIn == minecraft.util.Hand.MAIN_HAND ? 1.0F : -1.0F;
      int armDirection = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      if (elytraFlying) {
         if (!this.holdMyItemsPhysicsUpdatedThisFrame) {
            this.holdMyItemsClimbBlend = 0.0F;
            this.holdMyItemsInWaterCounter = 0.0F;
            this.holdMyItemsVertAngleY = this.holdMyItemsVertAngleY * (float)Math.pow(0.72, tt);
            this.holdMyItemsVertVelocityYSlime = this.holdMyItemsVertVelocityYSlime * (float)Math.pow(0.72, tt);
            this.holdMyItemsVertAngleYSlime = this.holdMyItemsVertAngleYSlime * (float)Math.pow(0.72, tt);
            this.holdMyItemsPhysicsUpdatedThisFrame = true;
         }

         if (!stack.isEmpty() && stack.getUseAction() != item.consume.UseAction.BLOCK) {
            matrices.translate(0.0, -0.1, 0.1);
         }

         if (this.isHoldMyItemsLantern(stack)) {
            matrices.translate(0.0, 0.1, 0.0);
         }
      } else {
         if (!this.holdMyItemsPhysicsUpdatedThisFrame) {
            double speed = velocity.length();
            if (speed >= 0.08) {
               double clampedSpeed = Math.min(speed, 0.22);
               double clampedDot = util.math.MathHelper.clamp(dotProduct, -0.22, 0.22);
               double clampedCross = util.math.MathHelper.clamp(crossProduct, -0.22, 0.22);
               this.holdMyItemsCrawlCount = (float)(this.holdMyItemsCrawlCount + 0.1 * clampedSpeed * 2.0 * tt);
               this.holdMyItemsDirectionalCrawlCount = (float)(this.holdMyItemsDirectionalCrawlCount + 0.1 * clampedDot * 4.0 * tt);
               this.holdMyItemsDirectionalCrawlCount = (float)(
                  this.holdMyItemsDirectionalCrawlCount
                     + (clampedDot > 0.0 ? 0.1 * Math.abs(clampedCross) * 4.0 * tt : 0.1 * Math.abs(clampedCross) * -4.0 * tt)
               );
            }

            if (velocity.y > 0.0) {
               this.holdMyItemsClimbCount = (float)(this.holdMyItemsClimbCount + 0.1 * tt);
            }

            if (velocity.y < 0.0) {
               this.holdMyItemsClimbCount = (float)(this.holdMyItemsClimbCount - 0.1 * tt);
            }

            float motionYNormalized = player.isOnGround() ? 0.0F : (float)util.math.MathHelper.clamp(velocity.y, -0.42, 0.42);
            this.holdMyItemsVertAngleY = (float)(this.holdMyItemsVertAngleY + motionYNormalized * 0.015 * tt);
            this.holdMyItemsVertAngleY = (float)(this.holdMyItemsVertAngleY - 0.1 * this.holdMyItemsVertAngleY * tt);
            this.holdMyItemsVertAngleY = (float)(this.holdMyItemsVertAngleY * Math.pow(0.88, tt));
            this.holdMyItemsVertVelocityYSlime = (float)(this.holdMyItemsVertVelocityYSlime + motionYNormalized * 0.015 * tt);
            this.holdMyItemsVertVelocityYSlime = (float)(this.holdMyItemsVertVelocityYSlime - 0.1 * this.holdMyItemsVertAngleYSlime * tt);
            this.holdMyItemsVertVelocityYSlime = (float)(this.holdMyItemsVertVelocityYSlime * Math.pow(0.88, tt));
            this.holdMyItemsVertAngleYSlime = (float)(this.holdMyItemsVertAngleYSlime + this.holdMyItemsVertVelocityYSlime * tt);
            if (player.isTouchingWater() && !player.isSubmergedInWater()) {
               this.holdMyItemsInWaterCounter = (float)(this.holdMyItemsInWaterCounter + 0.1 * tt);
               if (this.holdMyItemsInWaterCounter > 1.0F) {
                  this.holdMyItemsInWaterCounter = 1.0F;
               }
            } else {
               this.holdMyItemsInWaterCounter = (float)(this.holdMyItemsInWaterCounter * Math.pow(0.88, tt));
            }

            this.holdMyItemsPhysicsUpdatedThisFrame = true;
         }

         if ((crawling || climbing) && (!player.isUsingItem() || player.getActiveHand() != handIn) && swingProgress == 0.0F) {
            this.holdMyItemsClimbBlend = (float)(this.holdMyItemsClimbBlend + 0.1 * tt);
            if (this.holdMyItemsClimbBlend > 1.0F) {
               this.holdMyItemsClimbBlend = 1.0F;
            }

            if (!this.isHoldMyItemsLantern(stack)) {
               matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-20.0F * this.holdMyItemsClimbBlend));
            }
         } else {
            this.holdMyItemsClimbBlend = (float)(this.holdMyItemsClimbBlend * Math.pow(0.88, tt));
         }

         if (swingProgress == 0.0F) {
            float pitch = player.getPitch();
            matrices.translate(
               handDirection > 0.0F ? pitch / 650.0F * this.holdMyItemsClimbBlend * -1.0F : pitch / 650.0F * this.holdMyItemsClimbBlend, 0.0F, 0.0F
            );
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(pitch * this.holdMyItemsClimbBlend));
         }

         if (!this.isHoldMyItemsLantern(stack)) {
            matrices.translate(0.0, 0.0, player.getPitch() / 120.0F * this.holdMyItemsClimbBlend);
         } else if (swingProgress == 0.0F) {
            matrices.translate(0.0, 0.0, player.getPitch() / 80.0F * this.holdMyItemsClimbBlend);
         }

         if (climbing && !this.isHoldMyItemsLantern(stack) && (!player.isUsingItem() || player.getActiveHand() != handIn)) {
            matrices.translate(0.0, 0.1, -0.2);
         }

         matrices.translate(0.0, 0.02 * this.holdMyItemsInWaterCounter, 0.0);
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(8.0F * handDirection * this.holdMyItemsInWaterCounter));
         matrices.translate(0.0, -this.holdMyItemsVertAngleY, 0.0);
         matrices.translate(0.0, Math.sin(player.age * 0.1) * 0.007 * armDirection, 0.0);
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(0.15F * (float)Math.sin(player.age * 0.15F) * armDirection));
         if ((!stack.isEmpty() || crawling || climbing || player.isSubmergedInWater()) && stack.getUseAction() != item.consume.UseAction.BLOCK) {
            matrices.translate(0.0, -0.1, 0.1);
         }

         if (this.isHoldMyItemsLantern(stack)) {
            matrices.translate(0.0, 0.1, 0.0);
            if (player.isSubmergedInWater()) {
               matrices.translate(0.0, -0.1, 0.1);
            }
         }

         if (player.isSubmergedInWater() && swingProgress == 0.0F) {
            double distance = (player.age + partialTicks) * 0.2;
            double handRotation = Math.sin(distance) * 1.5;
            double smoothRotation = handRotation * 0.8 + this.holdMyItemsPreviousRotation * 0.2;
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees((float)(handIn == minecraft.util.Hand.MAIN_HAND ? smoothRotation : -smoothRotation)));
            matrices.translate(0.0, 0.0, smoothRotation * 0.2);
            this.holdMyItemsPreviousRotation = smoothRotation;
         }

         if ((climbing || crawling) && (!player.isUsingItem() || player.getActiveHand() != handIn) && swingProgress == 0.0F) {
            float crawlProgress = util.math.MathHelper.sin(this.holdMyItemsDirectionalCrawlCount * 4.0F);
            float upAndDown = util.math.MathHelper.cos(this.holdMyItemsDirectionalCrawlCount * 4.0F);
            if (this.isHoldMyItemsLantern(stack)) {
               crawlProgress *= 0.14F;
               upAndDown *= 0.14F;
            }

            matrices.translate(0.2 * crawlProgress, 0.3 * crawlProgress * armDirection, -0.2 * crawlProgress * armDirection * pitchFactor);
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(25.0F * crawlProgress));
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(util.math.MathHelper.clamp(20.0F * upAndDown * armDirection, 0.0F, 20.0F)));
         }
      }
   }

   private void applyHoldMyItemsLanternPose(util.math.MatrixStack matrices, client.network.AbstractClientPlayerEntity player, minecraft.util.Arm arm, float swingProgress) {
      float dt = (float)(this.holdMyItemsDeltaTime * 30.0);
      int direction = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      float yawDelta = player.prevHeadYaw - player.headYaw;
      float pitchDelta = player.prevPitch - player.getPitch();
      this.holdMyItemsSwingVelocityY += yawDelta * 0.015F * dt;
      this.holdMyItemsSwingVelocityY += swingProgress * 2.0F * dt;
      this.holdMyItemsSwingVelocityX += pitchDelta * 0.015F * dt;
      this.holdMyItemsSwingVelocityY = this.holdMyItemsSwingVelocityY - 0.1F * this.holdMyItemsSwingAngleY * dt;
      this.holdMyItemsSwingVelocityX = this.holdMyItemsSwingVelocityX - 0.1F * this.holdMyItemsSwingAngleX * dt;
      this.holdMyItemsSwingVelocityY = (float)(this.holdMyItemsSwingVelocityY * Math.pow(0.88, dt));
      this.holdMyItemsSwingVelocityX = (float)(this.holdMyItemsSwingVelocityX * Math.pow(0.88, dt));
      this.holdMyItemsSwingAngleY = this.holdMyItemsSwingAngleY + this.holdMyItemsSwingVelocityY * dt;
      this.holdMyItemsSwingAngleX = this.holdMyItemsSwingAngleX + this.holdMyItemsSwingVelocityX * dt;
      double currentSpeed = player.getVelocity().length();
      this.holdMyItemsSwingVelocityZ = (float)(
         this.holdMyItemsSwingVelocityZ
            + (
               direction > 0
                  ? (currentSpeed * -15.0 - this.holdMyItemsSwingVelocityZ) * 0.1 * dt
                  : (currentSpeed * 15.0 - this.holdMyItemsSwingVelocityZ) * 0.1 * dt
            )
      );
      if (currentSpeed > 0.09
         && (player.isOnGround() || player.isSubmergedInWater() || this.isHoldMyItemsClimbing(player))
         && (Boolean)minecraft.client.MinecraftClient.getInstance().options.getBobView().getValue()) {
         this.holdMyItemsSwingVelocityY = this.holdMyItemsSwingVelocityY + (float)((Math.random() < 0.5 ? -5.5 : 5.5) * currentSpeed * dt);
      }

      matrices.translate(0.0, 0.0, -0.1);
      matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(35.0F * direction + this.holdMyItemsSwingAngleY));
      matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(15.0F + this.holdMyItemsSwingAngleX));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(75.0F * direction + this.holdMyItemsSwingVelocityZ));
      matrices.translate(0.3 * direction, -0.35, 0.0);
      matrices.translate(0.0, 0.0, 0.1);
      matrices.scale(1.5F, 1.5F, 1.5F);
   }

   private void applyHoldMyItemsItemPose(util.math.MatrixStack matrices, client.network.AbstractClientPlayerEntity player, minecraft.util.Hand handIn, minecraft.util.Arm arm, minecraft.item.ItemStack stack, float swingProgress) {
      int direction = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      boolean mainHand = handIn == minecraft.util.Hand.MAIN_HAND;
      if (player.getMainArm() == minecraft.util.Arm.LEFT) {
         mainHand = !mainHand;
      }

      matrices.translate(-0.3 * direction, 0.65, -0.1);
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-65.0F * direction));
      matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(10.0F));
      if (stack.getItem() instanceof minecraft.item.BlockItem && !(stack.getItem() instanceof minecraft.item.BucketItem) && stack.getUseAction() != item.consume.UseAction.EAT) {
         minecraft.block.Block block = ((minecraft.item.BlockItem)stack.getItem()).getBlock();
         if (block instanceof minecraft.block.AbstractSkullBlock) {
            matrices.translate(0.1 * direction, 0.15, 0.1);
            matrices.scale(0.7F, 0.7F, 0.7F);
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(245.0F * direction));
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(25.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-15.0F * direction));
         } else if (this.isHoldMyItemsTorch(stack)) {
            matrices.scale(1.5F, 1.5F, 1.5F);
            matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(25.0F * direction));
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(5.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(75.0F * direction));
            matrices.translate(0.2 * direction, 0.2, 0.05);
         } else if (this.isHoldMyItemsThinBlock(stack)) {
            matrices.translate(0.0, 0.0, -0.1);
            matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(5.0F * direction));
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(15.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(75.0F * direction));
         } else if (this.isHoldMyItemsLantern(stack)) {
            this.applyHoldMyItemsLanternPose(matrices, player, arm, swingProgress);
         } else {
            matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(25.0F * direction));
            matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(5.0F));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(75.0F * direction));
            matrices.translate(0.2 * direction, 0.2, 0.05);
         }
      } else if (this.isHoldMyItemsSmallItem(stack) && this.getHoldMyItemsAttackDamage(stack) == 0.0F) {
         matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(5.0F * direction));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(15.0F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(75.0F * direction));
         matrices.translate(0.0, -0.05, -0.1);
         matrices.scale(0.7F, 0.7F, 0.7F);
      } else if (stack.getUseAction() == item.consume.UseAction.BLOCK && stack.getUseAction() != item.consume.UseAction.SPEAR) {
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(160.0F * direction));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-60.0F * direction));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-70.0F));
         matrices.scale(0.75F, 0.75F, 0.75F);
         matrices.translate(0.15 * direction, mainHand ? 0.35 : 0.45, mainHand ? -0.15 : -0.1);
         matrices.translate(0.17 * direction, 0.0, 0.3);
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F * direction));
      } else if (stack.getUseAction() == item.consume.UseAction.SPEAR) {
         matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(75.0F * direction));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(45.0F * direction));
         matrices.translate(-0.3 * direction, 0.0, 0.0);
      } else {
         matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(75.0F * direction));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(70.0F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(45.0F * direction));
         if (stack.getUseAction() != item.consume.UseAction.BLOCK) {
            matrices.scale(1.2F, 1.2F, 1.2F);
         }

         if (stack.getUseAction() == item.consume.UseAction.BOW && !player.isUsingItem()) {
            matrices.translate(-0.1 * direction, -0.2, 0.0);
         }
      }
   }

   private void applyHoldMyItemsGenericSwing(util.math.MatrixStack matrices, float direction, float swingRot, float swing) {
      matrices.translate(0.1 * direction * swingRot, 0.1 * swingRot, -0.1 * swing);
      matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swingRot));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-10.0F * swingRot * direction));
      matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(10.0F * swing * direction));
   }

   private void applyHoldMyItemsSwing(util.math.MatrixStack matrices, client.network.AbstractClientPlayerEntity player, minecraft.util.Hand handIn, minecraft.item.ItemStack stack, float swingProgress) {
      boolean mainHand = handIn == minecraft.util.Hand.MAIN_HAND;
      if (player.getMainArm() == minecraft.util.Arm.LEFT) {
         mainHand = !mainHand;
      }

      boolean hasAuraTarget = true;
      float ll = mainHand ? 1.0F : -1.0F;
      float handDirection = handIn == minecraft.util.Hand.MAIN_HAND ? 1.0F : -1.0F;
      float swingRot = this.getHoldMyItemsSwingRot(swingProgress);
      float swing = this.holdMyItemsEase(util.math.MathHelper.sin(swingProgress * (float) Math.PI));
      String currentAttackMode = this.attackMode.get();
      boolean forwardHandsAttack = "Forward".equals(currentAttackMode) && hasAuraTarget;
      boolean normalHandsAttack = "Normal".equals(currentAttackMode) && hasAuraTarget;
      if (stack.getItem() instanceof minecraft.item.SwordItem && forwardHandsAttack) {
         matrices.translate(0.12 * ll * swingRot, 0.04 * swingRot, -0.95 * swing);
         matrices.translate(0.02 * ll * swing, 0.1 * swing, -0.1 * swingRot);
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(8.0F * swingRot * ll));
         matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(-14.0F * swingRot));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-18.0F * swingRot * ll));
         matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(32.0F * swing));
      } else if (stack.getItem() instanceof minecraft.item.SwordItem && normalHandsAttack) {
         this.applyHoldMyItemsGenericSwing(matrices, ll, swingRot, swing);
      } else if ((
            this.holdMyItemsLeft
               || stack.getItem() instanceof minecraft.item.AxeItem
               || stack.getUseAction() == item.consume.UseAction.SPEAR
               || stack.getUseAction() == item.consume.UseAction.BLOCK
         )
         && !this.isHoldMyItemsShovel(stack)) {
         if (this.isHoldMyItemsWeapon(stack)) {
            matrices.translate(0.8 * ll * swingRot, 0.3 * swingRot, -0.5 * swing);
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(15.0F * swingRot * ll));
            matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(-20.0F * swingRot));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-70.0F * swingRot * ll));
            matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees((stack.getItem() instanceof minecraft.item.SwordItem ? 40.0F : 30.0F) * swing));
         } else if (stack.getUseAction() == item.consume.UseAction.SPEAR) {
            matrices.translate(0.0, 0.0, 0.45 * swingRot);
            matrices.translate(-0.25 * handDirection * swing, -0.35 * swingRot, -0.6 * swing);
            matrices.translate(0.0, 0.1 * swing, 0.0);
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(15.0F * swingRot * ll));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(30.0F * swingRot * ll));
         } else if (this.isHoldMyItemsTool(stack) && stack.getUseAction() != item.consume.UseAction.BLOCK) {
            matrices.translate(0.1 * ll * swingRot, 0.1 * swingRot, -0.5 * swing);
            matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swingRot));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F * swingRot * ll));
            matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
         } else if (stack.getUseAction() != item.consume.UseAction.BLOCK) {
            matrices.translate(0.1 * ll * swingRot, 0.1 * swingRot, -0.1 * swing);
            matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swingRot));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-10.0F * swingRot * ll));
            matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(10.0F * swing * ll));
         } else {
            matrices.translate(0.1 * ll * swingRot, 0.1 * swingRot, -0.2 * swing);
            matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(-10.0F * swingRot));
            matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-10.0F * swingRot * ll));
            matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(20.0F * swing));
         }
      } else if (this.isHoldMyItemsShovel(stack)) {
         matrices.translate(0.0, 0.15 * swingRot, -0.25 * swingRot);
         matrices.translate(0.0, 0.0, -0.2 * swing);
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(15.0F * swingRot));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(-35.0F * swingRot));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(30.0F * swing));
      } else if (stack.getItem() instanceof minecraft.item.SwordItem) {
         matrices.translate(-0.55 * ll * swingRot, -0.8 * swingRot, -0.77 * swing);
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(5.0F * swingRot * ll));
         matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swingRot));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(70.0F * swingRot * ll));
         matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(50.0F * swing));
      } else if (this.isHoldMyItemsTool(stack)) {
         matrices.translate(0.1 * ll * swingRot, 0.1 * swingRot, -0.5 * swing);
         matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swingRot));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F * swingRot * ll));
         matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
      } else {
         this.applyHoldMyItemsGenericSwing(matrices, ll, swingRot, swing);
      }
   }

   private void applyChestRightHandMotion(util.math.MatrixStack matrices, minecraft.util.Arm arm) {
      if (arm == minecraft.util.Arm.RIGHT && !(this.chestRightHandMotion <= 0.001F)) {
         float progress = this.chestRightHandMotion;
         float time = (float)(System.currentTimeMillis() % 1200L) / 1200.0F;
         float pulse = util.math.MathHelper.sin(time * (float) (Math.PI * 2)) * progress;
         matrices.translate(0.04 * progress, -0.03 * progress + 0.01 * pulse, -0.12 * progress);
         matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotationDegrees(12.0F * progress));
         matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(8.0F * progress + 2.5F * pulse));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-4.0F * progress));
      }
   }

   private void applyHoldMyItemsUseJitter(util.math.MatrixStack matrices, float useTicks, float progress) {
      if (!(progress <= 0.1F)) {
         float pulse = util.math.MathHelper.sin((useTicks - 0.1F) * 1.3F);
         float offset = pulse * (progress - 0.1F);
         matrices.translate(0.0, offset * 0.004, 0.0);
      }
   }

   private void renderHMIBow(
      client.network.AbstractClientPlayerEntity player,
      float tickDelta,
      minecraft.util.Hand handIn,
      float swingProgress,
      minecraft.item.ItemStack stack,
      float equippedProgress,
      util.math.MatrixStack matrices,
      client.render.VertexConsumerProvider vertexConsumers,
      int light
   ) {
      boolean isMainHand = handIn == minecraft.util.Hand.MAIN_HAND;
      minecraft.util.Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
      boolean rightHand = arm == minecraft.util.Arm.RIGHT;
      int handDirection = rightHand ? 1 : -1;
      float useTicks = stack.getMaxUseTime(player) - (player.getItemUseTimeLeft() - tickDelta + 1.0F);
      float drawLinear = util.math.MathHelper.clamp(useTicks / 20.0F, 0.0F, 1.0F);
      matrices.push();
      this.applyHandPositionBase(matrices, arm);
      this.applyHoldMyItemsEnvironment(matrices, player, handIn, arm, stack, swingProgress, tickDelta);
      matrices.push();
      this.applyHoldMyItemsUseJitter(matrices, useTicks, drawLinear);
      matrices.translate(rightHand ? -0.1 : 0.1, 0.0, drawLinear * 0.15);
      render.item.HeldItemRenderer heldItemRenderer = minecraft.client.MinecraftClient.getInstance().getEntityRenderDispatcher().getHeldItemRenderer();
      if (heldItemRenderer instanceof HeldItemRendererAccessor) {
         ((HeldItemRendererAccessor)heldItemRenderer).invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equippedProgress, swingProgress, arm);
      }

      matrices.pop();
      matrices.push();
      matrices.translate(rightHand ? -0.5 : 0.5, -0.45, 0.1);
      matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotation(0.3F));
      if (rightHand) {
         matrices.multiply(util.math.RotationAxis.NEGATIVE_Z.rotation(-0.3F));
         matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotation(1.0F));
         if (heldItemRenderer instanceof HeldItemRendererAccessor) {
            ((HeldItemRendererAccessor)heldItemRenderer)
               .invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equippedProgress, swingProgress, arm.getOpposite());
         }

         matrices.multiply(util.math.RotationAxis.NEGATIVE_Y.rotation(2.5F));
      } else {
         matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotation(-0.3F));
         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotation(1.0F));
         if (heldItemRenderer instanceof HeldItemRendererAccessor) {
            ((HeldItemRendererAccessor)heldItemRenderer)
               .invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equippedProgress, swingProgress, arm.getOpposite());
         }

         matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotation(2.5F));
      }

      matrices.translate(rightHand ? -0.65 : 0.65, -0.35, 0.27);
      matrices.pop();
      matrices.multiply(util.math.RotationAxis.NEGATIVE_X.rotationDegrees(75.0F));
      matrices.multiply(util.math.RotationAxis.NEGATIVE_Z.rotationDegrees(-15.0F * handDirection));
      matrices.translate(0.8 * handDirection, -equippedProgress * 0.3, -0.1);
      this.applyHoldMyItemsUseJitter(matrices, useTicks, drawLinear);
      this.applyHoldMyItemsItemPose(matrices, player, handIn, arm, stack, swingProgress);
      this.applyHandPositionItem(matrices, arm);
      this.renderItem(player, stack, rightHand ? minecraft.item.ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : minecraft.item.ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !rightHand, matrices, vertexConsumers, light);
      matrices.pop();
      this.holdMyItemsIsAttacking = minecraft.client.MinecraftClient.getInstance().options.attackKey.isPressed();
   }

   private void renderHMIConsume(
      client.network.AbstractClientPlayerEntity player,
      float tickDelta,
      minecraft.util.Hand handIn,
      float swingProgress,
      minecraft.item.ItemStack stack,
      float equippedProgress,
      util.math.MatrixStack matrices,
      client.render.VertexConsumerProvider vertexConsumers,
      int light
   ) {
      minecraft.item.ItemStack consumeStack = player.getActiveItem() != null && !player.getActiveItem().isEmpty() && player.getActiveHand() == handIn
         ? player.getActiveItem()
         : stack;
      boolean isMainHand = handIn == minecraft.util.Hand.MAIN_HAND;
      minecraft.util.Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
      int direction = arm == minecraft.util.Arm.RIGHT ? 1 : -1;
      float useTicks = consumeStack.getMaxUseTime(player) - (player.getItemUseTimeLeft() - tickDelta + 1.0F);
      float progress = util.math.MathHelper.clamp(useTicks / 5.0F, 0.0F, 1.0F);
      float wobble = util.math.MathHelper.sin(useTicks / 2.0F * (float) Math.PI) * 0.1F;
      matrices.push();
      this.applyHandPositionBase(matrices, arm);
      matrices.translate(direction, 0.1, 0.3);
      matrices.translate(0.2 * direction * progress, -0.7 * progress, -0.2 * progress);
      matrices.translate(0.0, -0.2 * wobble, -0.2 * wobble);
      matrices.translate(0.0, 0.1 * this.holdMyItemsEase(util.math.MathHelper.sin(progress * (float) Math.PI)), 0.0);
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * direction));
      matrices.multiply(util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-40.0F * direction));
      matrices.multiply(util.math.RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
      matrices.scale(0.9F, 0.9F, 0.9F);
      matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * progress * direction));
      render.item.HeldItemRenderer heldItemRenderer = minecraft.client.MinecraftClient.getInstance().getEntityRenderDispatcher().getHeldItemRenderer();
      if (heldItemRenderer instanceof HeldItemRendererAccessor) {
         ((HeldItemRendererAccessor)heldItemRenderer).invokeRenderArmHoldingItem(matrices, vertexConsumers, light, 0.0F, swingProgress, arm);
      }

      this.applyHoldMyItemsItemPose(matrices, player, handIn, arm, consumeStack, swingProgress);
      this.applyHandPositionItem(matrices, arm);
      this.renderItem(
         player,
         consumeStack,
         arm == minecraft.util.Arm.RIGHT ? minecraft.item.ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : minecraft.item.ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
         arm == minecraft.util.Arm.LEFT,
         matrices,
         vertexConsumers,
         light
      );
      matrices.pop();
      this.holdMyItemsIsAttacking = minecraft.client.MinecraftClient.getInstance().options.attackKey.isPressed();
   }

   private void renderHMI(
      client.network.AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      minecraft.util.Hand hand,
      float swingProgress,
      minecraft.item.ItemStack item,
      float equipProgress,
      util.math.MatrixStack matrices,
      client.render.VertexConsumerProvider vertexConsumers,
      int light
   ) {
      boolean isMainHand = hand == minecraft.util.Hand.MAIN_HAND;
      minecraft.util.Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
      this.updateChestRightHandMotion();
      matrices.push();
      this.applyHandPositionBase(matrices, arm);
      this.applyChestRightHandMotion(matrices, arm);
      this.applyHoldMyItemsSwing(matrices, player, hand, item, swingProgress);
      this.applyHoldMyItemsEnvironment(matrices, player, hand, arm, item, swingProgress, tickDelta);
      this.applyHoldMyItemsArmPrePose(matrices, item, arm);
      this.applyHoldMyItemsBaseHandPose(matrices, arm, equipProgress, swingProgress);
      render.item.HeldItemRenderer heldItemRenderer = minecraft.client.MinecraftClient.getInstance().getEntityRenderDispatcher().getHeldItemRenderer();
      if (heldItemRenderer instanceof HeldItemRendererAccessor) {
         ((HeldItemRendererAccessor)heldItemRenderer).invokeRenderArmHoldingItem(matrices, vertexConsumers, light, 0.0F, 0.0F, arm);
      }

      this.applyHoldMyItemsItemPose(matrices, player, hand, arm, item, swingProgress);
      this.applyHandPositionItem(matrices, arm);
      this.renderItem(
         player,
         item,
         arm == minecraft.util.Arm.RIGHT ? minecraft.item.ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : minecraft.item.ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
         arm == minecraft.util.Arm.LEFT,
         matrices,
         vertexConsumers,
         light
      );
      matrices.pop();
   }
}
