package xyz.angames.astolfoclient.client.module.modules.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1743;
import net.minecraft.class_1747;
import net.minecraft.class_1755;
import net.minecraft.class_1764;
import net.minecraft.class_1766;
import net.minecraft.class_1787;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1806;
import net.minecraft.class_1820;
import net.minecraft.class_1821;
import net.minecraft.class_1829;
import net.minecraft.class_1835;
import net.minecraft.class_1839;
import net.minecraft.class_2190;
import net.minecraft.class_2248;
import net.minecraft.class_2389;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3481;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import net.minecraft.class_898;
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

   private void applyHandPosition(class_4587 matrices, class_1306 arm) {
      this.applyHandPositionBase(matrices, arm);
      this.applyHandPositionItem(matrices, arm);
   }

   private void applyHandPositionBase(class_4587 matrices, class_1306 arm) {
      HandPositionModule handPos = (HandPositionModule)ModuleManager.getModule(HandPositionModule.class);
      if (handPos != null && handPos.isEnabled()) {
         class_310 mc = class_310.method_1551();
         boolean isMainHand = mc.field_1724 != null && arm == mc.field_1724.method_6068();
         float[] pos = isMainHand ? handPos.getMainHandPos() : handPos.getOffHandPos();
         matrices.method_46416(pos[0], pos[1], pos[2]);
      }
   }

   private void applyHandPositionItem(class_4587 matrices, class_1306 arm) {
      HandPositionModule handPos = (HandPositionModule)ModuleManager.getModule(HandPositionModule.class);
      if (handPos != null && handPos.isEnabled()) {
         class_310 mc = class_310.method_1551();
         boolean isMainHand = mc.field_1724 != null && arm == mc.field_1724.method_6068();
         float[] rot = isMainHand ? handPos.getMainHandRot() : handPos.getOffHandRot();
         float[] scale = isMainHand ? handPos.getMainHandScale() : handPos.getOffHandScale();
         if (rot[0] != 0.0F) {
            matrices.method_22907(class_7833.field_40714.rotationDegrees(rot[0]));
         }

         if (rot[1] != 0.0F) {
            matrices.method_22907(class_7833.field_40716.rotationDegrees(rot[1]));
         }

         if (rot[2] != 0.0F) {
            matrices.method_22907(class_7833.field_40718.rotationDegrees(rot[2]));
         }

         if (scale[0] != 1.0F || scale[1] != 1.0F || scale[2] != 1.0F) {
            matrices.method_22905(scale[0], scale[1], scale[2]);
         }
      }
   }

   private void handleSwordAnim(class_4587 matrices, float swingProgress, float equipProgress, class_1306 arm) {
      float str = this.getFloat(this.strength);
      float g = class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
      float anim = (float)Math.sin(swingProgress * (Math.PI / 2) * 2.0);
      float isLeft = arm == class_1306.field_6182 ? -1.0F : 1.0F;
      String currentMode = String.valueOf(this.mode.get()).toUpperCase();
      if (currentMode.contains("VANILLA")) {
         float n = -0.4F * class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
         float mxx = 0.2F * class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) (Math.PI * 2));
         float fxxx = -0.2F * class_3532.method_15374(swingProgress * (float) Math.PI);
         matrices.method_46416(isLeft * n, mxx, fxxx);
         this.applyEquipOffset(matrices, arm, equipProgress);
         this.applySwingOffset(matrices, arm, swingProgress);
      } else {
         this.applyEquipOffset(matrices, arm, 0.0F);
         matrices.method_22905(1.0F, 1.0F, 1.0F);
         if (currentMode.contains("1")) {
            this.applySwingOffset(matrices, arm, swingProgress);
         } else if (currentMode.contains("2")) {
            matrices.method_46416(isLeft * -0.1F, 0.15F, -0.1F);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(isLeft * -60.0F));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(50.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(isLeft * (110.0F + str * g)));
         } else if (currentMode.contains("3")) {
            matrices.method_46416(isLeft * -0.1F, 0.15F, 0.0F);
            matrices.method_22907(class_7833.field_40714.rotationDegrees(50.0F));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(isLeft * (-30.0F + str * g)));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(isLeft * 110.0F));
         } else if (currentMode.contains("4")) {
            matrices.method_46416(isLeft * -0.15F, 0.2F, 0.0F);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(isLeft * 90.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(isLeft * -30.0F));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-90.0F - str * anim + 10.0F));
         } else if (currentMode.contains("5")) {
            this.applySwingOffset(matrices, arm, swingProgress);
            float spinAngle = swingProgress * 360.0F;
            matrices.method_46416(0.0F, 0.0F, 0.0F);
            matrices.method_22907(class_7833.field_40714.rotationDegrees(-isLeft * spinAngle));
            matrices.method_46416(0.0F, 0.0F, 0.0F);
         }
      }
   }

   public void handleRenderItem(
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light
   ) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
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

            if (!player.method_31550()) {
               boolean isMainHand = hand == class_1268.field_5808;
               class_1306 arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
               boolean isRightArm = arm == class_1306.field_6183;
               int i = isRightArm ? 1 : -1;
               matrices.method_22903();
               this.applyHandPositionBase(matrices, arm);
               if (item.method_31574(class_1802.field_8399)) {
                  boolean isCharged = class_1764.method_7781(item);
                  if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand) {
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     matrices.method_46416(i * -0.4785682F, -0.094387F, 0.05731531F);
                     matrices.method_22907(class_7833.field_40714.rotationDegrees(-11.935F));
                     matrices.method_22907(class_7833.field_40716.rotationDegrees(i * 65.3F));
                     matrices.method_22907(class_7833.field_40718.rotationDegrees(i * -9.785F));
                     float f = item.method_7935(mc.field_1724) - (mc.field_1724.method_6014() - tickDelta + 1.0F);
                     float g = f / class_1764.method_7775(item, mc.field_1724);
                     if (g > 1.0F) {
                        g = 1.0F;
                     }

                     if (g > 0.1F) {
                        float h = class_3532.method_15374((f - 0.1F) * 1.3F);
                        float j = g - 0.1F;
                        float k = h * j;
                        matrices.method_46416(k * 0.0F, k * 0.004F, k * 0.0F);
                     }

                     matrices.method_46416(g * 0.0F, g * 0.0F, g * 0.04F);
                     matrices.method_22905(1.0F, 1.0F, 1.0F + g * 0.2F);
                     matrices.method_22907(class_7833.field_40715.rotationDegrees(i * 45.0F));
                  } else {
                     float fx = -0.4F * class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
                     float gx = 0.2F * class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) (Math.PI * 2));
                     float h = -0.2F * class_3532.method_15374(swingProgress * (float) Math.PI);
                     matrices.method_46416(i * fx, gx, h);
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     this.applySwingOffset(matrices, arm, swingProgress);
                     if (isCharged && swingProgress < 0.001F && isMainHand) {
                        matrices.method_46416(i * -0.641864F, 0.0F, 0.0F);
                        matrices.method_22907(class_7833.field_40716.rotationDegrees(i * 10.0F));
                     }
                  }

                  this.applyHandPositionItem(matrices, arm);
                  this.renderItem(player, item, isRightArm ? class_811.field_4322 : class_811.field_4321, !isRightArm, matrices, vertexConsumers, light);
               } else {
                  if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand) {
                     int l = isRightArm ? 1 : -1;
                     switch (item.method_7976()) {
                        case field_8952:
                        case field_8949:
                           this.applyEquipOffset(matrices, arm, equipProgress);
                           break;
                        case field_8950:
                        case field_8946:
                           this.applyEatOrDrinkTransformation(matrices, tickDelta, arm, item);
                           this.applyEquipOffset(matrices, arm, equipProgress);
                           break;
                        case field_8953:
                           this.applyEquipOffset(matrices, arm, equipProgress);
                           matrices.method_46416(l * -0.2785682F, 0.18344387F, 0.15731531F);
                           matrices.method_22907(class_7833.field_40714.rotationDegrees(-13.935F));
                           matrices.method_22907(class_7833.field_40716.rotationDegrees(l * 35.3F));
                           matrices.method_22907(class_7833.field_40718.rotationDegrees(l * -9.785F));
                           float mx = item.method_7935(mc.field_1724) - (mc.field_1724.method_6014() - tickDelta + 1.0F);
                           float fxx = mx / 20.0F;
                           fxx = (fxx * fxx + fxx * 2.0F) / 3.0F;
                           if (fxx > 1.0F) {
                              fxx = 1.0F;
                           }

                           if (fxx > 0.1F) {
                              float gx = class_3532.method_15374((mx - 0.1F) * 1.3F);
                              float h = fxx - 0.1F;
                              float j = gx * h;
                              matrices.method_46416(j * 0.0F, j * 0.004F, j * 0.0F);
                           }

                           matrices.method_46416(fxx * 0.0F, fxx * 0.0F, fxx * 0.04F);
                           matrices.method_22905(1.0F, 1.0F, 1.0F + fxx * 0.2F);
                           matrices.method_22907(class_7833.field_40715.rotationDegrees(l * 45.0F));
                           break;
                        case field_8951:
                           this.applyEquipOffset(matrices, arm, equipProgress);
                           matrices.method_46416(l * -0.5F, 0.7F, 0.1F);
                           matrices.method_22907(class_7833.field_40714.rotationDegrees(-55.0F));
                           matrices.method_22907(class_7833.field_40716.rotationDegrees(l * 35.3F));
                           matrices.method_22907(class_7833.field_40718.rotationDegrees(l * -9.785F));
                           float m = item.method_7935(mc.field_1724) - (mc.field_1724.method_6014() - tickDelta + 1.0F);
                           float fx = m / 10.0F;
                           if (fx > 1.0F) {
                              fx = 1.0F;
                           }

                           if (fx > 0.1F) {
                              float gx = class_3532.method_15374((m - 0.1F) * 1.3F);
                              float h = fx - 0.1F;
                              float j = gx * h;
                              matrices.method_46416(j * 0.0F, j * 0.004F, j * 0.0F);
                           }

                           matrices.method_46416(0.0F, 0.0F, fx * 0.2F);
                           matrices.method_22905(1.0F, 1.0F, 1.0F + fx * 0.2F);
                           matrices.method_22907(class_7833.field_40715.rotationDegrees(l * 45.0F));
                           break;
                        case field_42717:
                           this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
                     }
                  } else if (player.method_6123()) {
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     int l = isRightArm ? 1 : -1;
                     matrices.method_46416(l * -0.4F, 0.8F, 0.3F);
                     matrices.method_22907(class_7833.field_40716.rotationDegrees(l * 65.0F));
                     matrices.method_22907(class_7833.field_40718.rotationDegrees(l * -85.0F));
                  } else if (arm == mc.field_1690.method_42552().method_41753() && this.isEnabled()) {
                     this.handleSwordAnim(matrices, swingProgress, equipProgress, arm);
                  } else {
                     float n = -0.4F * class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
                     float mxx = 0.2F * class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) (Math.PI * 2));
                     float fxxx = -0.2F * class_3532.method_15374(swingProgress * (float) Math.PI);
                     int o = isRightArm ? 1 : -1;
                     matrices.method_46416(o * n, mxx, fxxx);
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     this.applySwingOffset(matrices, arm, swingProgress);
                  }

                  this.applyHandPositionItem(matrices, arm);
                  this.renderItem(player, item, isRightArm ? class_811.field_4322 : class_811.field_4321, !isRightArm, matrices, vertexConsumers, light);
               }

               matrices.method_22909();
            }
         } finally {
            renderingCustomItem = false;
         }
      }
   }

   private void applyBrushTransformation(class_4587 matrices, float tickDelta, class_1306 arm, class_1799 stack, float equipProgress) {
      class_310 mc = class_310.method_1551();
      this.applyEquipOffset(matrices, arm, equipProgress);
      float f = mc.field_1724.method_6014() % 10;
      float g = f - tickDelta + 1.0F;
      float h = 1.0F - g / 10.0F;
      float n = -15.0F + 75.0F * class_3532.method_15362(h * 2.0F * (float) Math.PI);
      if (arm != class_1306.field_6183) {
         matrices.method_22904(0.1, 0.83, 0.35);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-80.0F));
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-90.0F));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(n));
         matrices.method_22904(-0.3, 0.22, 0.35);
      } else {
         matrices.method_22904(-0.25, 0.22, 0.35);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-80.0F));
         matrices.method_22907(class_7833.field_40716.rotationDegrees(90.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(0.0F));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(n));
      }
   }

   private void applyEatOrDrinkTransformation(class_4587 matrices, float tickDelta, class_1306 arm, class_1799 stack) {
      class_310 mc = class_310.method_1551();
      float f = mc.field_1724.method_6014() - tickDelta + 1.0F;
      float g = f / stack.method_7935(mc.field_1724);
      if (g < 0.8F) {
         float h = class_3532.method_15379(class_3532.method_15362(f / 4.0F * (float) Math.PI) * 0.1F);
         matrices.method_46416(0.0F, h, 0.0F);
      }

      float h = 1.0F - (float)Math.pow(g, 27.0);
      int i = arm == class_1306.field_6183 ? 1 : -1;
      matrices.method_46416(h * 0.6F * i, h * -0.5F, h * 0.0F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(i * h * 90.0F));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(h * 10.0F));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(i * h * 30.0F));
   }

   private void applyEquipOffset(class_4587 matrices, class_1306 arm, float equipProgress) {
      int i = arm == class_1306.field_6183 ? 1 : -1;
      matrices.method_46416(i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
   }

   private void applySwingOffset(class_4587 matrices, class_1306 arm, float swingProgress) {
      int i = arm == class_1306.field_6183 ? 1 : -1;
      float f = class_3532.method_15374(swingProgress * swingProgress * (float) Math.PI);
      float g = class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(i * (45.0F + f * -20.0F)));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(i * g * -20.0F));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(g * -80.0F));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(i * -45.0F));
   }

   public void renderItem(
      class_1309 entity, class_1799 stack, class_811 renderMode, boolean leftHanded, class_4587 matrices, class_4597 vertexConsumers, int light
   ) {
      if (!stack.method_7960()) {
         class_898 dispatcher = class_310.method_1551().method_1561();
         if (dispatcher != null && dispatcher.method_43336() != null) {
            dispatcher.method_43336().method_3233(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light);
         } else {
            class_310.method_1551()
               .method_1480()
               .method_23177(
                  entity,
                  stack,
                  renderMode,
                  leftHanded,
                  matrices,
                  vertexConsumers,
                  entity.method_37908(),
                  light,
                  class_4608.field_21444,
                  entity.method_5628() + renderMode.ordinal()
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

   public void updatePhysics(class_746 player, float tickDelta) {
      if (this.isEnabled() && this.isHoldMyItemsEnabled()) {
         double currentTime = System.nanoTime() / 1.0E9;
         this.holdMyItemsDeltaTime = Math.min(0.05, Math.max(0.0, currentTime - this.holdMyItemsPrevFrameTime));
         this.holdMyItemsPrevFrameTime = currentTime;
         this.holdMyItemsPhysicsUpdatedThisFrame = false;
         float f = player.method_6055(tickDelta);
         if (f > 0.0F && this.holdMyItemsPrevSwingProgress == 0.0F) {
            this.holdMyItemsLeft = !this.holdMyItemsLeft;
         }

         this.holdMyItemsPrevSwingProgress = f;
      }
   }

   private boolean shouldUseHoldMyItemsCustom(class_742 player, class_1268 handIn, class_1799 stack) {
      return !this.isHoldMyItemsEnabled()
         ? false
         : !(stack.method_7909() instanceof class_1806)
            && !(stack.method_7909() instanceof class_1764)
            && (!player.method_6115() || player.method_6058() != handIn)
            && !player.method_6123();
   }

   private boolean shouldUseHoldMyItemsBow(class_742 player, class_1268 handIn, class_1799 stack) {
      return !this.isHoldMyItemsEnabled() ? false : stack.method_7976() == class_1839.field_8953 && player.method_6115() && player.method_6058() == handIn;
   }

   private boolean shouldUseHoldMyItemsConsume(class_742 player, class_1268 handIn, class_1799 stack) {
      if (!this.isHoldMyItemsEnabled()) {
         return false;
      }

      class_1799 consumeStack = player.method_6030() != null && !player.method_6030().method_7960() && player.method_6058() == handIn
         ? player.method_6030()
         : stack;
      class_1839 action = consumeStack.method_7976();
      return (action == class_1839.field_8950 || action == class_1839.field_8946) && player.method_6115() && player.method_6058() == handIn;
   }

   private void updateChestRightHandMotion() {
      class_310 mc = class_310.method_1551();
      float target = mc.field_1755 instanceof class_465 && !(mc.field_1755 instanceof class_490) ? 1.0F : 0.0F;
      this.chestRightHandMotion = class_3532.method_16439(0.18F, this.chestRightHandMotion, target);
   }

   private float getHoldMyItemsAttackDamage(class_1799 stack) {
      if (stack.method_7960()) {
         return 0.0F;
      }

      String name = stack.method_7909().toString().toLowerCase();
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

   private boolean isHoldMyItemsCrawling(class_742 player) {
      return player.method_20232() && !player.method_5799();
   }

   private boolean isHoldMyItemsClimbing(class_742 player) {
      return player.method_6101() && !player.method_24828() && Math.abs(player.method_18798().field_1351) > 0.0;
   }

   private boolean isHoldMyItemsWeapon(class_1799 stack) {
      return stack.method_7909() instanceof class_1829 || stack.method_7909() instanceof class_1743;
   }

   private boolean isHoldMyItemsTool(class_1799 stack) {
      return stack.method_7909() instanceof class_1766 || stack.method_7909() instanceof class_1820 || stack.method_7909() instanceof class_1835;
   }

   private boolean isHoldMyItemsShovel(class_1799 stack) {
      return stack.method_7909() instanceof class_1821;
   }

   private boolean isHoldMyItemsLantern(class_1799 stack) {
      return stack.method_31574(class_1802.field_16539) || stack.method_31574(class_1802.field_22016);
   }

   private boolean isHoldMyItemsThinBlock(class_1799 stack) {
      if (!(stack.method_7909() instanceof class_1747)) {
         return false;
      }

      class_2248 block = ((class_1747)stack.method_7909()).method_7711();
      return stack.method_31574(class_1802.field_8276)
         || stack.method_31574(class_1802.field_8725)
         || stack.method_31574(class_1802.field_8865)
         || stack.method_31574(class_1802.field_8366)
         || block instanceof class_2389
         || block.method_9564().method_26164(class_3481.field_15463)
         || block.method_9564().method_26164(class_3481.field_22414)
         || block.method_9564().method_26164(class_3481.field_15495);
   }

   private boolean isHoldMyItemsTorch(class_1799 stack) {
      String name = stack.method_7964().getString().toLowerCase();
      return name.contains("torch") || name.contains("факел");
   }

   private boolean isHoldMyItemsSmallItem(class_1799 stack) {
      return !(stack.method_7909() instanceof class_1747)
         && !this.isHoldMyItemsTool(stack)
         && !this.isHoldMyItemsWeapon(stack)
         && !(stack.method_7909() instanceof class_1787)
         && !(stack.method_7909() instanceof class_1755)
         && stack.method_7976() != class_1839.field_8953
         && stack.method_7976() != class_1839.field_8951
         && stack.method_7976() != class_1839.field_8949;
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
         ? class_3532.method_15374(class_3532.method_15363(swingProgress, 0.0F, 0.12506F) * 12.56F)
         : class_3532.method_15374(class_3532.method_15363(swingProgress, 0.62532F, 0.75038F) * 12.56F);
   }

   private void applyHoldMyItemsBaseHandPose(class_4587 matrices, class_1306 arm, float equippedProgress, float swingProgress) {
      int direction = arm == class_1306.field_6183 ? 1 : -1;
      float swingSin = class_3532.method_15374(swingProgress * (float) Math.PI);
      matrices.method_22904(direction, -equippedProgress * 0.3, 0.3);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(45.0F * direction));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(-40.0F * direction));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(30.0F));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(direction * (45.0F + swingSin * 0.0F)));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(direction * -45.0F));
      matrices.method_22905(0.9F, 0.9F, 0.9F);
   }

   private void applyHoldMyItemsArmPrePose(class_4587 matrices, class_1799 stack, class_1306 arm) {
      int direction = arm == class_1306.field_6183 ? 1 : -1;
      if (this.isHoldMyItemsLantern(stack)) {
         matrices.method_22904(0.1 * direction, 0.0, -0.1);
         matrices.method_22907(class_7833.field_40714.rotationDegrees(10.0F));
      } else {
         if (stack.method_7976() == class_1839.field_8949) {
            matrices.method_22904(0.0, -0.2, 0.0);
         }
      }
   }

   private void applyHoldMyItemsEnvironment(
      class_4587 matrices, class_742 player, class_1268 handIn, class_1306 arm, class_1799 stack, float swingProgress, float partialTicks
   ) {
      float yaw = class_3532.method_16439(partialTicks, player.field_5982, player.method_36454());
      double radians = Math.toRadians(yaw);
      double forwardX = -Math.sin(radians);
      double forwardZ = Math.cos(radians);
      class_243 velocity = player.method_18798();
      double dotProduct = velocity.field_1352 * forwardX + velocity.field_1350 * forwardZ;
      double crossProduct = velocity.field_1352 * forwardZ - velocity.field_1350 * forwardX;
      float pitchFactor = player.method_36455() != 0.0F ? 90.0F / player.method_36455() / 10.0F : 1.0F;
      if (pitchFactor > 1.0F || pitchFactor < 0.0F) {
         pitchFactor = 1.0F;
      }

      boolean crawling = this.isHoldMyItemsCrawling(player);
      boolean climbing = this.isHoldMyItemsClimbing(player);
      boolean elytraFlying = player.method_6128();
      double tt = this.holdMyItemsDeltaTime * 30.0;
      float handDirection = handIn == class_1268.field_5808 ? 1.0F : -1.0F;
      int armDirection = arm == class_1306.field_6183 ? 1 : -1;
      if (elytraFlying) {
         if (!this.holdMyItemsPhysicsUpdatedThisFrame) {
            this.holdMyItemsClimbBlend = 0.0F;
            this.holdMyItemsInWaterCounter = 0.0F;
            this.holdMyItemsVertAngleY = this.holdMyItemsVertAngleY * (float)Math.pow(0.72, tt);
            this.holdMyItemsVertVelocityYSlime = this.holdMyItemsVertVelocityYSlime * (float)Math.pow(0.72, tt);
            this.holdMyItemsVertAngleYSlime = this.holdMyItemsVertAngleYSlime * (float)Math.pow(0.72, tt);
            this.holdMyItemsPhysicsUpdatedThisFrame = true;
         }

         if (!stack.method_7960() && stack.method_7976() != class_1839.field_8949) {
            matrices.method_22904(0.0, -0.1, 0.1);
         }

         if (this.isHoldMyItemsLantern(stack)) {
            matrices.method_22904(0.0, 0.1, 0.0);
         }
      } else {
         if (!this.holdMyItemsPhysicsUpdatedThisFrame) {
            double speed = velocity.method_1033();
            if (speed >= 0.08) {
               double clampedSpeed = Math.min(speed, 0.22);
               double clampedDot = class_3532.method_15350(dotProduct, -0.22, 0.22);
               double clampedCross = class_3532.method_15350(crossProduct, -0.22, 0.22);
               this.holdMyItemsCrawlCount = (float)(this.holdMyItemsCrawlCount + 0.1 * clampedSpeed * 2.0 * tt);
               this.holdMyItemsDirectionalCrawlCount = (float)(this.holdMyItemsDirectionalCrawlCount + 0.1 * clampedDot * 4.0 * tt);
               this.holdMyItemsDirectionalCrawlCount = (float)(
                  this.holdMyItemsDirectionalCrawlCount
                     + (clampedDot > 0.0 ? 0.1 * Math.abs(clampedCross) * 4.0 * tt : 0.1 * Math.abs(clampedCross) * -4.0 * tt)
               );
            }

            if (velocity.field_1351 > 0.0) {
               this.holdMyItemsClimbCount = (float)(this.holdMyItemsClimbCount + 0.1 * tt);
            }

            if (velocity.field_1351 < 0.0) {
               this.holdMyItemsClimbCount = (float)(this.holdMyItemsClimbCount - 0.1 * tt);
            }

            float motionYNormalized = player.method_24828() ? 0.0F : (float)class_3532.method_15350(velocity.field_1351, -0.42, 0.42);
            this.holdMyItemsVertAngleY = (float)(this.holdMyItemsVertAngleY + motionYNormalized * 0.015 * tt);
            this.holdMyItemsVertAngleY = (float)(this.holdMyItemsVertAngleY - 0.1 * this.holdMyItemsVertAngleY * tt);
            this.holdMyItemsVertAngleY = (float)(this.holdMyItemsVertAngleY * Math.pow(0.88, tt));
            this.holdMyItemsVertVelocityYSlime = (float)(this.holdMyItemsVertVelocityYSlime + motionYNormalized * 0.015 * tt);
            this.holdMyItemsVertVelocityYSlime = (float)(this.holdMyItemsVertVelocityYSlime - 0.1 * this.holdMyItemsVertAngleYSlime * tt);
            this.holdMyItemsVertVelocityYSlime = (float)(this.holdMyItemsVertVelocityYSlime * Math.pow(0.88, tt));
            this.holdMyItemsVertAngleYSlime = (float)(this.holdMyItemsVertAngleYSlime + this.holdMyItemsVertVelocityYSlime * tt);
            if (player.method_5799() && !player.method_5869()) {
               this.holdMyItemsInWaterCounter = (float)(this.holdMyItemsInWaterCounter + 0.1 * tt);
               if (this.holdMyItemsInWaterCounter > 1.0F) {
                  this.holdMyItemsInWaterCounter = 1.0F;
               }
            } else {
               this.holdMyItemsInWaterCounter = (float)(this.holdMyItemsInWaterCounter * Math.pow(0.88, tt));
            }

            this.holdMyItemsPhysicsUpdatedThisFrame = true;
         }

         if ((crawling || climbing) && (!player.method_6115() || player.method_6058() != handIn) && swingProgress == 0.0F) {
            this.holdMyItemsClimbBlend = (float)(this.holdMyItemsClimbBlend + 0.1 * tt);
            if (this.holdMyItemsClimbBlend > 1.0F) {
               this.holdMyItemsClimbBlend = 1.0F;
            }

            if (!this.isHoldMyItemsLantern(stack)) {
               matrices.method_22907(class_7833.field_40714.rotationDegrees(-20.0F * this.holdMyItemsClimbBlend));
            }
         } else {
            this.holdMyItemsClimbBlend = (float)(this.holdMyItemsClimbBlend * Math.pow(0.88, tt));
         }

         if (swingProgress == 0.0F) {
            float pitch = player.method_36455();
            matrices.method_46416(
               handDirection > 0.0F ? pitch / 650.0F * this.holdMyItemsClimbBlend * -1.0F : pitch / 650.0F * this.holdMyItemsClimbBlend, 0.0F, 0.0F
            );
            matrices.method_22907(class_7833.field_40714.rotationDegrees(pitch * this.holdMyItemsClimbBlend));
         }

         if (!this.isHoldMyItemsLantern(stack)) {
            matrices.method_22904(0.0, 0.0, player.method_36455() / 120.0F * this.holdMyItemsClimbBlend);
         } else if (swingProgress == 0.0F) {
            matrices.method_22904(0.0, 0.0, player.method_36455() / 80.0F * this.holdMyItemsClimbBlend);
         }

         if (climbing && !this.isHoldMyItemsLantern(stack) && (!player.method_6115() || player.method_6058() != handIn)) {
            matrices.method_22904(0.0, 0.1, -0.2);
         }

         matrices.method_22904(0.0, 0.02 * this.holdMyItemsInWaterCounter, 0.0);
         matrices.method_22907(class_7833.field_40718.rotationDegrees(8.0F * handDirection * this.holdMyItemsInWaterCounter));
         matrices.method_22904(0.0, -this.holdMyItemsVertAngleY, 0.0);
         matrices.method_22904(0.0, Math.sin(player.field_6012 * 0.1) * 0.007 * armDirection, 0.0);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(0.15F * (float)Math.sin(player.field_6012 * 0.15F) * armDirection));
         if ((!stack.method_7960() || crawling || climbing || player.method_5869()) && stack.method_7976() != class_1839.field_8949) {
            matrices.method_22904(0.0, -0.1, 0.1);
         }

         if (this.isHoldMyItemsLantern(stack)) {
            matrices.method_22904(0.0, 0.1, 0.0);
            if (player.method_5869()) {
               matrices.method_22904(0.0, -0.1, 0.1);
            }
         }

         if (player.method_5869() && swingProgress == 0.0F) {
            double distance = (player.field_6012 + partialTicks) * 0.2;
            double handRotation = Math.sin(distance) * 1.5;
            double smoothRotation = handRotation * 0.8 + this.holdMyItemsPreviousRotation * 0.2;
            matrices.method_22907(class_7833.field_40716.rotationDegrees((float)(handIn == class_1268.field_5808 ? smoothRotation : -smoothRotation)));
            matrices.method_22904(0.0, 0.0, smoothRotation * 0.2);
            this.holdMyItemsPreviousRotation = smoothRotation;
         }

         if ((climbing || crawling) && (!player.method_6115() || player.method_6058() != handIn) && swingProgress == 0.0F) {
            float crawlProgress = class_3532.method_15374(this.holdMyItemsDirectionalCrawlCount * 4.0F);
            float upAndDown = class_3532.method_15362(this.holdMyItemsDirectionalCrawlCount * 4.0F);
            if (this.isHoldMyItemsLantern(stack)) {
               crawlProgress *= 0.14F;
               upAndDown *= 0.14F;
            }

            matrices.method_22904(0.2 * crawlProgress, 0.3 * crawlProgress * armDirection, -0.2 * crawlProgress * armDirection * pitchFactor);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(25.0F * crawlProgress));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(class_3532.method_15363(20.0F * upAndDown * armDirection, 0.0F, 20.0F)));
         }
      }
   }

   private void applyHoldMyItemsLanternPose(class_4587 matrices, class_742 player, class_1306 arm, float swingProgress) {
      float dt = (float)(this.holdMyItemsDeltaTime * 30.0);
      int direction = arm == class_1306.field_6183 ? 1 : -1;
      float yawDelta = player.field_6259 - player.field_6241;
      float pitchDelta = player.field_6004 - player.method_36455();
      this.holdMyItemsSwingVelocityY += yawDelta * 0.015F * dt;
      this.holdMyItemsSwingVelocityY += swingProgress * 2.0F * dt;
      this.holdMyItemsSwingVelocityX += pitchDelta * 0.015F * dt;
      this.holdMyItemsSwingVelocityY = this.holdMyItemsSwingVelocityY - 0.1F * this.holdMyItemsSwingAngleY * dt;
      this.holdMyItemsSwingVelocityX = this.holdMyItemsSwingVelocityX - 0.1F * this.holdMyItemsSwingAngleX * dt;
      this.holdMyItemsSwingVelocityY = (float)(this.holdMyItemsSwingVelocityY * Math.pow(0.88, dt));
      this.holdMyItemsSwingVelocityX = (float)(this.holdMyItemsSwingVelocityX * Math.pow(0.88, dt));
      this.holdMyItemsSwingAngleY = this.holdMyItemsSwingAngleY + this.holdMyItemsSwingVelocityY * dt;
      this.holdMyItemsSwingAngleX = this.holdMyItemsSwingAngleX + this.holdMyItemsSwingVelocityX * dt;
      double currentSpeed = player.method_18798().method_1033();
      this.holdMyItemsSwingVelocityZ = (float)(
         this.holdMyItemsSwingVelocityZ
            + (
               direction > 0
                  ? (currentSpeed * -15.0 - this.holdMyItemsSwingVelocityZ) * 0.1 * dt
                  : (currentSpeed * 15.0 - this.holdMyItemsSwingVelocityZ) * 0.1 * dt
            )
      );
      if (currentSpeed > 0.09
         && (player.method_24828() || player.method_5869() || this.isHoldMyItemsClimbing(player))
         && (Boolean)class_310.method_1551().field_1690.method_42448().method_41753()) {
         this.holdMyItemsSwingVelocityY = this.holdMyItemsSwingVelocityY + (float)((Math.random() < 0.5 ? -5.5 : 5.5) * currentSpeed * dt);
      }

      matrices.method_22904(0.0, 0.0, -0.1);
      matrices.method_22907(class_7833.field_40715.rotationDegrees(35.0F * direction + this.holdMyItemsSwingAngleY));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(15.0F + this.holdMyItemsSwingAngleX));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * direction + this.holdMyItemsSwingVelocityZ));
      matrices.method_22904(0.3 * direction, -0.35, 0.0);
      matrices.method_22904(0.0, 0.0, 0.1);
      matrices.method_22905(1.5F, 1.5F, 1.5F);
   }

   private void applyHoldMyItemsItemPose(class_4587 matrices, class_742 player, class_1268 handIn, class_1306 arm, class_1799 stack, float swingProgress) {
      int direction = arm == class_1306.field_6183 ? 1 : -1;
      boolean mainHand = handIn == class_1268.field_5808;
      if (player.method_6068() == class_1306.field_6182) {
         mainHand = !mainHand;
      }

      matrices.method_22904(-0.3 * direction, 0.65, -0.1);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(-65.0F * direction));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(10.0F));
      if (stack.method_7909() instanceof class_1747 && !(stack.method_7909() instanceof class_1755) && stack.method_7976() != class_1839.field_8950) {
         class_2248 block = ((class_1747)stack.method_7909()).method_7711();
         if (block instanceof class_2190) {
            matrices.method_22904(0.1 * direction, 0.15, 0.1);
            matrices.method_22905(0.7F, 0.7F, 0.7F);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(245.0F * direction));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(25.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(-15.0F * direction));
         } else if (this.isHoldMyItemsTorch(stack)) {
            matrices.method_22905(1.5F, 1.5F, 1.5F);
            matrices.method_22907(class_7833.field_40715.rotationDegrees(25.0F * direction));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(5.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * direction));
            matrices.method_22904(0.2 * direction, 0.2, 0.05);
         } else if (this.isHoldMyItemsThinBlock(stack)) {
            matrices.method_22904(0.0, 0.0, -0.1);
            matrices.method_22907(class_7833.field_40715.rotationDegrees(5.0F * direction));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(15.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * direction));
         } else if (this.isHoldMyItemsLantern(stack)) {
            this.applyHoldMyItemsLanternPose(matrices, player, arm, swingProgress);
         } else {
            matrices.method_22907(class_7833.field_40715.rotationDegrees(25.0F * direction));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(5.0F));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * direction));
            matrices.method_22904(0.2 * direction, 0.2, 0.05);
         }
      } else if (this.isHoldMyItemsSmallItem(stack) && this.getHoldMyItemsAttackDamage(stack) == 0.0F) {
         matrices.method_22907(class_7833.field_40715.rotationDegrees(5.0F * direction));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(15.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(75.0F * direction));
         matrices.method_22904(0.0, -0.05, -0.1);
         matrices.method_22905(0.7F, 0.7F, 0.7F);
      } else if (stack.method_7976() == class_1839.field_8949 && stack.method_7976() != class_1839.field_8951) {
         matrices.method_22907(class_7833.field_40718.rotationDegrees(160.0F * direction));
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-60.0F * direction));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-70.0F));
         matrices.method_22905(0.75F, 0.75F, 0.75F);
         matrices.method_22904(0.15 * direction, mainHand ? 0.35 : 0.45, mainHand ? -0.15 : -0.1);
         matrices.method_22904(0.17 * direction, 0.0, 0.3);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(-90.0F * direction));
      } else if (stack.method_7976() == class_1839.field_8951) {
         matrices.method_22907(class_7833.field_40715.rotationDegrees(75.0F * direction));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(45.0F * direction));
         matrices.method_22904(-0.3 * direction, 0.0, 0.0);
      } else {
         matrices.method_22907(class_7833.field_40715.rotationDegrees(75.0F * direction));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(70.0F));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(45.0F * direction));
         if (stack.method_7976() != class_1839.field_8949) {
            matrices.method_22905(1.2F, 1.2F, 1.2F);
         }

         if (stack.method_7976() == class_1839.field_8953 && !player.method_6115()) {
            matrices.method_22904(-0.1 * direction, -0.2, 0.0);
         }
      }
   }

   private void applyHoldMyItemsGenericSwing(class_4587 matrices, float direction, float swingRot, float swing) {
      matrices.method_22904(0.1 * direction * swingRot, 0.1 * swingRot, -0.1 * swing);
      matrices.method_22907(class_7833.field_40713.rotationDegrees(-30.0F * swingRot));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(-10.0F * swingRot * direction));
      matrices.method_22907(class_7833.field_40713.rotationDegrees(40.0F * swing));
      matrices.method_22907(class_7833.field_40716.rotationDegrees(10.0F * swing * direction));
   }

   private void applyHoldMyItemsSwing(class_4587 matrices, class_742 player, class_1268 handIn, class_1799 stack, float swingProgress) {
      boolean mainHand = handIn == class_1268.field_5808;
      if (player.method_6068() == class_1306.field_6182) {
         mainHand = !mainHand;
      }

      boolean hasAuraTarget = true;
      float ll = mainHand ? 1.0F : -1.0F;
      float handDirection = handIn == class_1268.field_5808 ? 1.0F : -1.0F;
      float swingRot = this.getHoldMyItemsSwingRot(swingProgress);
      float swing = this.holdMyItemsEase(class_3532.method_15374(swingProgress * (float) Math.PI));
      String currentAttackMode = this.attackMode.get();
      boolean forwardHandsAttack = "Forward".equals(currentAttackMode) && hasAuraTarget;
      boolean normalHandsAttack = "Normal".equals(currentAttackMode) && hasAuraTarget;
      if (stack.method_7909() instanceof class_1829 && forwardHandsAttack) {
         matrices.method_22904(0.12 * ll * swingRot, 0.04 * swingRot, -0.95 * swing);
         matrices.method_22904(0.02 * ll * swing, 0.1 * swing, -0.1 * swingRot);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(8.0F * swingRot * ll));
         matrices.method_22907(class_7833.field_40713.rotationDegrees(-14.0F * swingRot));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(-18.0F * swingRot * ll));
         matrices.method_22907(class_7833.field_40713.rotationDegrees(32.0F * swing));
      } else if (stack.method_7909() instanceof class_1829 && normalHandsAttack) {
         this.applyHoldMyItemsGenericSwing(matrices, ll, swingRot, swing);
      } else if ((
            this.holdMyItemsLeft
               || stack.method_7909() instanceof class_1743
               || stack.method_7976() == class_1839.field_8951
               || stack.method_7976() == class_1839.field_8949
         )
         && !this.isHoldMyItemsShovel(stack)) {
         if (this.isHoldMyItemsWeapon(stack)) {
            matrices.method_22904(0.8 * ll * swingRot, 0.3 * swingRot, -0.5 * swing);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(15.0F * swingRot * ll));
            matrices.method_22907(class_7833.field_40713.rotationDegrees(-20.0F * swingRot));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(-70.0F * swingRot * ll));
            matrices.method_22907(class_7833.field_40713.rotationDegrees((stack.method_7909() instanceof class_1829 ? 40.0F : 30.0F) * swing));
         } else if (stack.method_7976() == class_1839.field_8951) {
            matrices.method_22904(0.0, 0.0, 0.45 * swingRot);
            matrices.method_22904(-0.25 * handDirection * swing, -0.35 * swingRot, -0.6 * swing);
            matrices.method_22904(0.0, 0.1 * swing, 0.0);
            matrices.method_22907(class_7833.field_40716.rotationDegrees(15.0F * swingRot * ll));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(30.0F * swingRot * ll));
         } else if (this.isHoldMyItemsTool(stack) && stack.method_7976() != class_1839.field_8949) {
            matrices.method_22904(0.1 * ll * swingRot, 0.1 * swingRot, -0.5 * swing);
            matrices.method_22907(class_7833.field_40713.rotationDegrees(-30.0F * swingRot));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(-20.0F * swingRot * ll));
            matrices.method_22907(class_7833.field_40713.rotationDegrees(40.0F * swing));
         } else if (stack.method_7976() != class_1839.field_8949) {
            matrices.method_22904(0.1 * ll * swingRot, 0.1 * swingRot, -0.1 * swing);
            matrices.method_22907(class_7833.field_40713.rotationDegrees(-30.0F * swingRot));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(-10.0F * swingRot * ll));
            matrices.method_22907(class_7833.field_40713.rotationDegrees(40.0F * swing));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(10.0F * swing * ll));
         } else {
            matrices.method_22904(0.1 * ll * swingRot, 0.1 * swingRot, -0.2 * swing);
            matrices.method_22907(class_7833.field_40713.rotationDegrees(-10.0F * swingRot));
            matrices.method_22907(class_7833.field_40718.rotationDegrees(-10.0F * swingRot * ll));
            matrices.method_22907(class_7833.field_40713.rotationDegrees(20.0F * swing));
         }
      } else if (this.isHoldMyItemsShovel(stack)) {
         matrices.method_22904(0.0, 0.15 * swingRot, -0.25 * swingRot);
         matrices.method_22904(0.0, 0.0, -0.2 * swing);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(15.0F * swingRot));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(-35.0F * swingRot));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(30.0F * swing));
      } else if (stack.method_7909() instanceof class_1829) {
         matrices.method_22904(-0.55 * ll * swingRot, -0.8 * swingRot, -0.77 * swing);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(5.0F * swingRot * ll));
         matrices.method_22907(class_7833.field_40713.rotationDegrees(-30.0F * swingRot));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(70.0F * swingRot * ll));
         matrices.method_22907(class_7833.field_40713.rotationDegrees(50.0F * swing));
      } else if (this.isHoldMyItemsTool(stack)) {
         matrices.method_22904(0.1 * ll * swingRot, 0.1 * swingRot, -0.5 * swing);
         matrices.method_22907(class_7833.field_40713.rotationDegrees(-30.0F * swingRot));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(-20.0F * swingRot * ll));
         matrices.method_22907(class_7833.field_40713.rotationDegrees(40.0F * swing));
      } else {
         this.applyHoldMyItemsGenericSwing(matrices, ll, swingRot, swing);
      }
   }

   private void applyChestRightHandMotion(class_4587 matrices, class_1306 arm) {
      if (arm == class_1306.field_6183 && !(this.chestRightHandMotion <= 0.001F)) {
         float progress = this.chestRightHandMotion;
         float time = (float)(System.currentTimeMillis() % 1200L) / 1200.0F;
         float pulse = class_3532.method_15374(time * (float) (Math.PI * 2)) * progress;
         matrices.method_22904(0.04 * progress, -0.03 * progress + 0.01 * pulse, -0.12 * progress);
         matrices.method_22907(class_7833.field_40715.rotationDegrees(12.0F * progress));
         matrices.method_22907(class_7833.field_40714.rotationDegrees(8.0F * progress + 2.5F * pulse));
         matrices.method_22907(class_7833.field_40718.rotationDegrees(-4.0F * progress));
      }
   }

   private void applyHoldMyItemsUseJitter(class_4587 matrices, float useTicks, float progress) {
      if (!(progress <= 0.1F)) {
         float pulse = class_3532.method_15374((useTicks - 0.1F) * 1.3F);
         float offset = pulse * (progress - 0.1F);
         matrices.method_22904(0.0, offset * 0.004, 0.0);
      }
   }

   private void renderHMIBow(
      class_742 player,
      float tickDelta,
      class_1268 handIn,
      float swingProgress,
      class_1799 stack,
      float equippedProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light
   ) {
      boolean isMainHand = handIn == class_1268.field_5808;
      class_1306 arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
      boolean rightHand = arm == class_1306.field_6183;
      int handDirection = rightHand ? 1 : -1;
      float useTicks = stack.method_7935(player) - (player.method_6014() - tickDelta + 1.0F);
      float drawLinear = class_3532.method_15363(useTicks / 20.0F, 0.0F, 1.0F);
      matrices.method_22903();
      this.applyHandPositionBase(matrices, arm);
      this.applyHoldMyItemsEnvironment(matrices, player, handIn, arm, stack, swingProgress, tickDelta);
      matrices.method_22903();
      this.applyHoldMyItemsUseJitter(matrices, useTicks, drawLinear);
      matrices.method_22904(rightHand ? -0.1 : 0.1, 0.0, drawLinear * 0.15);
      class_759 heldItemRenderer = class_310.method_1551().method_1561().method_43336();
      if (heldItemRenderer instanceof HeldItemRendererAccessor) {
         ((HeldItemRendererAccessor)heldItemRenderer).invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equippedProgress, swingProgress, arm);
      }

      matrices.method_22909();
      matrices.method_22903();
      matrices.method_22904(rightHand ? -0.5 : 0.5, -0.45, 0.1);
      matrices.method_22907(class_7833.field_40714.rotation(0.3F));
      if (rightHand) {
         matrices.method_22907(class_7833.field_40717.rotation(-0.3F));
         matrices.method_22907(class_7833.field_40715.rotation(1.0F));
         if (heldItemRenderer instanceof HeldItemRendererAccessor) {
            ((HeldItemRendererAccessor)heldItemRenderer)
               .invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equippedProgress, swingProgress, arm.method_5928());
         }

         matrices.method_22907(class_7833.field_40715.rotation(2.5F));
      } else {
         matrices.method_22907(class_7833.field_40718.rotation(-0.3F));
         matrices.method_22907(class_7833.field_40716.rotation(1.0F));
         if (heldItemRenderer instanceof HeldItemRendererAccessor) {
            ((HeldItemRendererAccessor)heldItemRenderer)
               .invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equippedProgress, swingProgress, arm.method_5928());
         }

         matrices.method_22907(class_7833.field_40716.rotation(2.5F));
      }

      matrices.method_22904(rightHand ? -0.65 : 0.65, -0.35, 0.27);
      matrices.method_22909();
      matrices.method_22907(class_7833.field_40713.rotationDegrees(75.0F));
      matrices.method_22907(class_7833.field_40717.rotationDegrees(-15.0F * handDirection));
      matrices.method_22904(0.8 * handDirection, -equippedProgress * 0.3, -0.1);
      this.applyHoldMyItemsUseJitter(matrices, useTicks, drawLinear);
      this.applyHoldMyItemsItemPose(matrices, player, handIn, arm, stack, swingProgress);
      this.applyHandPositionItem(matrices, arm);
      this.renderItem(player, stack, rightHand ? class_811.field_4322 : class_811.field_4321, !rightHand, matrices, vertexConsumers, light);
      matrices.method_22909();
      this.holdMyItemsIsAttacking = class_310.method_1551().field_1690.field_1886.method_1434();
   }

   private void renderHMIConsume(
      class_742 player,
      float tickDelta,
      class_1268 handIn,
      float swingProgress,
      class_1799 stack,
      float equippedProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light
   ) {
      class_1799 consumeStack = player.method_6030() != null && !player.method_6030().method_7960() && player.method_6058() == handIn
         ? player.method_6030()
         : stack;
      boolean isMainHand = handIn == class_1268.field_5808;
      class_1306 arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
      int direction = arm == class_1306.field_6183 ? 1 : -1;
      float useTicks = consumeStack.method_7935(player) - (player.method_6014() - tickDelta + 1.0F);
      float progress = class_3532.method_15363(useTicks / 5.0F, 0.0F, 1.0F);
      float wobble = class_3532.method_15374(useTicks / 2.0F * (float) Math.PI) * 0.1F;
      matrices.method_22903();
      this.applyHandPositionBase(matrices, arm);
      matrices.method_22904(direction, 0.1, 0.3);
      matrices.method_22904(0.2 * direction * progress, -0.7 * progress, -0.2 * progress);
      matrices.method_22904(0.0, -0.2 * wobble, -0.2 * wobble);
      matrices.method_22904(0.0, 0.1 * this.holdMyItemsEase(class_3532.method_15374(progress * (float) Math.PI)), 0.0);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(45.0F * direction));
      matrices.method_22907(class_7833.field_40718.rotationDegrees(-40.0F * direction));
      matrices.method_22907(class_7833.field_40714.rotationDegrees(30.0F));
      matrices.method_22905(0.9F, 0.9F, 0.9F);
      matrices.method_22907(class_7833.field_40716.rotationDegrees(45.0F * progress * direction));
      class_759 heldItemRenderer = class_310.method_1551().method_1561().method_43336();
      if (heldItemRenderer instanceof HeldItemRendererAccessor) {
         ((HeldItemRendererAccessor)heldItemRenderer).invokeRenderArmHoldingItem(matrices, vertexConsumers, light, 0.0F, swingProgress, arm);
      }

      this.applyHoldMyItemsItemPose(matrices, player, handIn, arm, consumeStack, swingProgress);
      this.applyHandPositionItem(matrices, arm);
      this.renderItem(
         player,
         consumeStack,
         arm == class_1306.field_6183 ? class_811.field_4322 : class_811.field_4321,
         arm == class_1306.field_6182,
         matrices,
         vertexConsumers,
         light
      );
      matrices.method_22909();
      this.holdMyItemsIsAttacking = class_310.method_1551().field_1690.field_1886.method_1434();
   }

   private void renderHMI(
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light
   ) {
      boolean isMainHand = hand == class_1268.field_5808;
      class_1306 arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
      this.updateChestRightHandMotion();
      matrices.method_22903();
      this.applyHandPositionBase(matrices, arm);
      this.applyChestRightHandMotion(matrices, arm);
      this.applyHoldMyItemsSwing(matrices, player, hand, item, swingProgress);
      this.applyHoldMyItemsEnvironment(matrices, player, hand, arm, item, swingProgress, tickDelta);
      this.applyHoldMyItemsArmPrePose(matrices, item, arm);
      this.applyHoldMyItemsBaseHandPose(matrices, arm, equipProgress, swingProgress);
      class_759 heldItemRenderer = class_310.method_1551().method_1561().method_43336();
      if (heldItemRenderer instanceof HeldItemRendererAccessor) {
         ((HeldItemRendererAccessor)heldItemRenderer).invokeRenderArmHoldingItem(matrices, vertexConsumers, light, 0.0F, 0.0F, arm);
      }

      this.applyHoldMyItemsItemPose(matrices, player, hand, arm, item, swingProgress);
      this.applyHandPositionItem(matrices, arm);
      this.renderItem(
         player,
         item,
         arm == class_1306.field_6183 ? class_811.field_4322 : class_811.field_4321,
         arm == class_1306.field_6182,
         matrices,
         vertexConsumers,
         light
      );
      matrices.method_22909();
   }
}
