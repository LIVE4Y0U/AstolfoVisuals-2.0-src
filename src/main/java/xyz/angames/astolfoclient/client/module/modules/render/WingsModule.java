package xyz.angames.astolfoclient.client.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;
import xyz.angames.astolfoclient.client.module.setting.NumberSetting;
import xyz.angames.astolfoclient.client.util.FriendManager;

@Environment(EnvType.CLIENT)
public class WingsModule extends Module {
   private final class_310 mc = class_310.method_1551();
   private static final float DEFAULT_SPREAD = 8.0F;
   private static final int DEFAULT_ALPHA = 220;
   private static final WingsModule.WingPoint[] SHAPE = new WingsModule.WingPoint[]{
      new WingsModule.WingPoint(0.08F, 0.1F, 0.88F),
      new WingsModule.WingPoint(0.28F, 0.34F, 0.78F),
      new WingsModule.WingPoint(0.56F, 0.82F, 0.62F),
      new WingsModule.WingPoint(0.86F, 0.3F, 0.52F),
      new WingsModule.WingPoint(1.14F, 0.46F, 0.4F),
      new WingsModule.WingPoint(1.24F, 0.04F, 0.3F),
      new WingsModule.WingPoint(1.02F, -0.18F, 0.28F),
      new WingsModule.WingPoint(1.18F, -0.64F, 0.22F),
      new WingsModule.WingPoint(0.86F, -0.46F, 0.2F),
      new WingsModule.WingPoint(0.8F, -0.98F, 0.14F),
      new WingsModule.WingPoint(0.54F, -0.74F, 0.16F),
      new WingsModule.WingPoint(0.3F, -1.16F, 0.12F),
      new WingsModule.WingPoint(0.1F, -0.54F, 0.18F)
   };
   public final BooleanSetting self = new BooleanSetting("Self", true);
   public final BooleanSetting players = new BooleanSetting("Players", false);
   public final NumberSetting size = new NumberSetting("Size", 1.0, 0.75, 1.35, 0.05);
   private float selfBodyYaw;
   private boolean selfBodyYawInitialized;

   public WingsModule() {
      super("Wings", Module.Category.RENDER);
      this.addSetting(this.self);
      this.addSetting(this.players);
      this.addSetting(this.size);
      WorldRenderEvents.LAST
         .register(
            (Last)context -> {
               if (this.isEnabled() && this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1773 != null) {
                  class_4587 stack = context.matrixStack();
                  float tickDelta = context.tickCounter().method_60637(true);
                  class_243 camera = this.mc.field_1773.method_19418().method_19326();
                  stack.method_22903();
                  RenderSystem.enableBlend();
                  RenderSystem.disableCull();
                  RenderSystem.enableDepthTest();
                  RenderSystem.depthMask(false);
                  RenderSystem.setShader(class_10142.field_53876);
                  if (this.self.get()
                     && !this.mc.field_1690.method_31044().method_31034()
                     && this.mc.field_1724.method_5805()
                     && !this.hasElytra(this.mc.field_1724)) {
                     try {
                        this.renderWings(stack, this.mc.field_1724, tickDelta, camera);
                     } catch (Exception var10) {
                     }
                  }

                  if (this.players.get()) {
                     for (class_1297 entity : this.mc.field_1687.method_18112()) {
                        if (entity instanceof class_1657 player && player != this.mc.field_1724 && player.method_5805() && !this.hasElytra(player)) {
                           try {
                              this.renderWings(stack, player, tickDelta, camera);
                           } catch (Exception var9) {
                           }
                        }
                     }
                  }

                  RenderSystem.depthMask(true);
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA, class_4535.ONE, class_4534.ZERO);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  stack.method_22909();
               }
            }
         );
   }

   @Override
   public void onDisable() {
      this.selfBodyYawInitialized = false;
   }

   private void renderWings(class_4587 stack, class_1657 player, float tickDelta, class_243 camera) {
      double x = class_3532.method_16436(tickDelta, player.field_6014, player.method_23317()) - camera.field_1352;
      double y = class_3532.method_16436(tickDelta, player.field_6036, player.method_23318()) - camera.field_1351;
      double z = class_3532.method_16436(tickDelta, player.field_5969, player.method_23321()) - camera.field_1350;
      float bodyYaw = this.resolveBodyYaw(player, tickDelta);
      float move = class_3532.method_15363(player.field_42108.method_48570(tickDelta), 0.0F, 1.0F);
      WingsModule.WingPose pose = this.resolvePose(player, tickDelta);
      if (pose != null) {
         float flap = (float)Math.sin((player.field_6012 + tickDelta) * pose.flapSpeed) * pose.flapAmplitude;
         float open = (8.0F + flap + move * pose.motionSpreadBoost) * pose.openMultiplier;
         float wingScale = (float)this.size.get() * pose.scaleMultiplier;
         int baseColor = this.resolveBaseColor();
         int glowColor = this.resolveGlowColor(baseColor);
         int coreColor = this.resolveCoreColor(baseColor);
         boolean isBaby = false;
         if (BabyPlayerModule.INSTANCE != null && BabyPlayerModule.INSTANCE.isEnabled()) {
            if (player == this.mc.field_1724 && BabyPlayerModule.INSTANCE.self.get()) {
               isBaby = true;
            } else if (player != this.mc.field_1724 && BabyPlayerModule.INSTANCE.friends.get() && FriendManager.isFriend(player.method_5477().getString())) {
               isBaby = true;
            }
         }

         stack.method_22903();
         stack.method_22904(x, y, z);
         if (isBaby) {
            stack.method_22905(0.5F, 0.5F, 0.5F);
         }

         stack.method_22907(class_7833.field_40716.rotationDegrees(180.0F - bodyYaw));
         if (pose.preTranslateY != 0.0F || pose.preTranslateZ != 0.0F) {
            stack.method_46416(0.0F, pose.preTranslateY, pose.preTranslateZ);
         }

         if (pose.pitchRotation != 0.0F) {
            stack.method_22907(class_7833.field_40714.rotationDegrees(pose.pitchRotation));
         }

         if (pose.rollRotation != 0.0F) {
            stack.method_22907(class_7833.field_40718.rotationDegrees(pose.rollRotation));
         }

         stack.method_46416(0.0F, pose.anchorY, pose.anchorZ);
         stack.method_22905(wingScale, wingScale, wingScale);
         this.renderWingSide(stack, -1.0F, open, baseColor, glowColor, coreColor, pose);
         this.renderWingSide(stack, 1.0F, open, baseColor, glowColor, coreColor, pose);
         stack.method_22909();
      }
   }

   private void renderWingSide(class_4587 stack, float side, float open, int baseColor, int glowColor, int coreColor, WingsModule.WingPose pose) {
      stack.method_22903();
      stack.method_46416(side * pose.sideOffset, pose.sideYOffset, pose.sideZOffset);
      stack.method_22907(class_7833.field_40716.rotationDegrees(side * open));
      stack.method_22907(class_7833.field_40718.rotationDegrees(side * pose.sideRoll));
      stack.method_22907(class_7833.field_40714.rotationDegrees(pose.sidePitch));
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
      this.drawWingLayer(stack, side, 1.22F, setAlpha(glowColor, 48), setAlpha(glowColor, 0));
      this.drawWingLayer(stack, side, 0.84F, setAlpha(coreColor, 57), setAlpha(coreColor, 0));
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
      this.drawWingLayer(stack, side, 1.0F, setAlpha(baseColor, 220), setAlpha(baseColor, 10));
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
      this.drawWingOutline(stack, side, 1.0F, setAlpha(baseColor, 136));
      this.drawWingRibs(stack, side, 0.96F, setAlpha(glowColor, 44));
      stack.method_22909();
   }

   private void drawWingLayer(class_4587 stack, float side, float scale, int rootColor, int edgeColor) {
      Matrix4f matrix = stack.method_23760().method_23761();
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27379, class_290.field_1576);

      for (int i = 0; i < SHAPE.length; i++) {
         WingsModule.WingPoint cur = SHAPE[i];
         WingsModule.WingPoint next = SHAPE[(i + 1) % SHAPE.length];
         this.vertex(buffer, matrix, 0.0F, 0.0F, 0.0F, rootColor);
         this.vertex(buffer, matrix, side * cur.x * scale, cur.y * scale, 0.0F, this.applyPointAlpha(edgeColor, cur.alphaMul));
         this.vertex(buffer, matrix, side * next.x * scale, next.y * scale, 0.0F, this.applyPointAlpha(edgeColor, next.alphaMul));
      }

      class_286.method_43433(buffer.method_60800());
   }

   private void drawWingOutline(class_4587 stack, float side, float scale, int color) {
      Matrix4f matrix = stack.method_23760().method_23761();
      RenderSystem.lineWidth(1.35F);
      GL11.glEnable(2848);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_29345, class_290.field_1576);

      for (WingsModule.WingPoint point : SHAPE) {
         this.vertex(buffer, matrix, side * point.x * scale, point.y * scale, 0.0F, color);
      }

      this.vertex(buffer, matrix, side * SHAPE[0].x * scale, SHAPE[0].y * scale, 0.0F, color);
      class_286.method_43433(buffer.method_60800());
      GL11.glDisable(2848);
   }

   private void drawWingRibs(class_4587 stack, float side, float scale, int color) {
      Matrix4f matrix = stack.method_23760().method_23761();
      int[] ribIndices = new int[]{2, 4, 7, 9, 11};
      RenderSystem.lineWidth(0.9F);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27377, class_290.field_1576);

      for (int idx : ribIndices) {
         WingsModule.WingPoint point = SHAPE[idx];
         this.vertex(buffer, matrix, 0.0F, 0.0F, 0.0F, setAlpha(color, Math.max(8, (int)(alpha(color) * 0.75F))));
         this.vertex(buffer, matrix, side * point.x * scale, point.y * scale, 0.0F, this.applyPointAlpha(color, point.alphaMul));
      }

      class_286.method_43433(buffer.method_60800());
   }

   private int resolveBaseColor() {
      long now = System.currentTimeMillis();
      int colorInt = ThemeManager.getThemedColor(now / 10L);
      return setAlpha(colorInt, 255);
   }

   private int resolveGlowColor(int base) {
      return interpolateColor(base, packARGB(255, 255, 255, 255), 0.28F);
   }

   private int resolveCoreColor(int base) {
      return interpolateColor(base, packARGB(255, 255, 255, 255), 0.55F);
   }

   private static int interpolateColor(int a, int b, float t) {
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int aa = a >> 24 & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      int ba = b >> 24 & 0xFF;
      int r = (int)(ar + (br - ar) * t);
      int g = (int)(ag + (bg - ag) * t);
      int bl2 = (int)(ab + (bb - ab) * t);
      int al = (int)(aa + (ba - aa) * t);
      return packARGB(r, g, bl2, al);
   }

   private static int packARGB(int r, int g, int b, int a) {
      return class_3532.method_15340(a, 0, 255) << 24 | r << 16 | g << 8 | b;
   }

   private static int setAlpha(int color, int a) {
      return class_3532.method_15340(a, 0, 255) << 24 | color & 16777215;
   }

   private static int alpha(int color) {
      return color >> 24 & 0xFF;
   }

   private static int red(int color) {
      return color >> 16 & 0xFF;
   }

   private static int green(int color) {
      return color >> 8 & 0xFF;
   }

   private static int blue(int color) {
      return color & 0xFF;
   }

   private int applyPointAlpha(int color, float multiplier) {
      return setAlpha(color, Math.max(0, Math.min(255, (int)(alpha(color) * multiplier))));
   }

   private void vertex(class_287 buffer, Matrix4f matrix, float x, float y, float z, int color) {
      buffer.method_22918(matrix, x, y, z).method_22915(red(color) / 255.0F, green(color) / 255.0F, blue(color) / 255.0F, alpha(color) / 255.0F);
   }

   private float resolveBodyYaw(class_1657 player, float tickDelta) {
      float target = class_3532.method_17821(tickDelta, player.field_6220, player.field_6283);
      if (player != this.mc.field_1724) {
         return target;
      } else if (this.selfBodyYawInitialized && player.field_6012 >= 2) {
         this.selfBodyYaw = approachDegrees(this.selfBodyYaw, target, 14.0F);
         return this.selfBodyYaw;
      } else {
         this.selfBodyYaw = target;
         this.selfBodyYawInitialized = true;
         return this.selfBodyYaw;
      }
   }

   private static float approachDegrees(float current, float target, float maxDelta) {
      float delta = class_3532.method_15393(target - current);
      delta = class_3532.method_15363(delta, -maxDelta, maxDelta);
      return current + delta;
   }

   private WingsModule.WingPose resolvePose(class_1657 player, float tickDelta) {
      float pitch = class_3532.method_16439(tickDelta, player.field_6004, player.method_36455());
      if (player.method_6128()) {
         float flightTicks = player.method_6003() + tickDelta;
         float flightProgress = class_3532.method_15363(flightTicks * flightTicks / 100.0F, 0.0F, 1.0F);
         float pitchRotation = flightProgress * (-90.0F - pitch);
         return new WingsModule.WingPose(0.34F, 0.46F, 0.0F, 0.0F, pitchRotation, 0.0F, 0.76F, 0.92F, 0.1F, 0.58F, 0.05F, 0.06F, -5.0F, -2.0F, 0.13F);
      } else if (player.method_5799()) {
         return null;
      } else {
         return player.method_5715()
            ? new WingsModule.WingPose(0.0F, 0.0F, 0.96F, 0.1F, 18.0F, 0.0F, 1.0F, 1.0F, 0.18F, 4.5F, 0.06F, 0.02F, -11.0F, -4.0F, 0.12F)
            : new WingsModule.WingPose(0.0F, 0.0F, 1.38F, 0.1F, 0.0F, 0.0F, 1.0F, 1.0F, 0.18F, 4.5F, 0.06F, 0.02F, -11.0F, -4.0F, 0.12F);
      }
   }

   private boolean hasElytra(class_1657 player) {
      return player.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
   }

   @Override
   public void onTick() {
   }

   @Environment(EnvType.CLIENT)
   private static final class WingPoint {
      final float x;
      final float y;
      final float alphaMul;

      WingPoint(float x, float y, float alphaMul) {
         this.x = x;
         this.y = y;
         this.alphaMul = alphaMul;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class WingPose {
      final float preTranslateY;
      final float preTranslateZ;
      final float anchorY;
      final float anchorZ;
      final float pitchRotation;
      final float rollRotation;
      final float openMultiplier;
      final float scaleMultiplier;
      final float motionSpreadBoost;
      final float flapAmplitude;
      final float sideOffset;
      final float sideYOffset;
      final float sideZOffset;
      final float sideRoll;
      final float sidePitch;
      final float flapSpeed;

      WingPose(
         float preTranslateY,
         float preTranslateZ,
         float anchorY,
         float anchorZ,
         float pitchRotation,
         float rollRotation,
         float openMultiplier,
         float scaleMultiplier,
         float motionSpreadBoost,
         float flapAmplitude,
         float sideOffset,
         float sideZOffset,
         float sideRoll,
         float sidePitch,
         float flapSpeed
      ) {
         this(
            preTranslateY,
            preTranslateZ,
            anchorY,
            anchorZ,
            pitchRotation,
            rollRotation,
            openMultiplier,
            scaleMultiplier,
            motionSpreadBoost,
            flapAmplitude,
            sideOffset,
            0.0F,
            sideZOffset,
            sideRoll,
            sidePitch,
            flapSpeed
         );
      }

      WingPose(
         float preTranslateY,
         float preTranslateZ,
         float anchorY,
         float anchorZ,
         float pitchRotation,
         float rollRotation,
         float openMultiplier,
         float scaleMultiplier,
         float motionSpreadBoost,
         float flapAmplitude,
         float sideOffset,
         float sideYOffset,
         float sideZOffset,
         float sideRoll,
         float sidePitch,
         float flapSpeed
      ) {
         this.preTranslateY = preTranslateY;
         this.preTranslateZ = preTranslateZ;
         this.anchorY = anchorY;
         this.anchorZ = anchorZ;
         this.pitchRotation = pitchRotation;
         this.rollRotation = rollRotation;
         this.openMultiplier = openMultiplier;
         this.scaleMultiplier = scaleMultiplier;
         this.motionSpreadBoost = motionSpreadBoost;
         this.flapAmplitude = flapAmplitude;
         this.sideOffset = sideOffset;
         this.sideYOffset = sideYOffset;
         this.sideZOffset = sideZOffset;
         this.sideRoll = sideRoll;
         this.sidePitch = sidePitch;
         this.flapSpeed = flapSpeed;
      }
   }
}
