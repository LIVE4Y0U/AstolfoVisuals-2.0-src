package xyz.angames.astolfoclient.client.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_9801;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;
import xyz.angames.astolfoclient.client.module.setting.ModeSetting;
import xyz.angames.astolfoclient.client.module.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public class CubeParticlesModule extends Module {
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/bloom.png");
   public final NumberSetting maxAmount = new NumberSetting("Amount", 60.0, 10.0, 200.0, 5.0);
   public final NumberSetting baseSize = new NumberSetting("Size", 0.25, 0.05, 1.0, 0.01);
   public final NumberSetting speedMultiplier = new NumberSetting("Speed", 1.0, 0.1, 3.0, 0.1);
   public final NumberSetting spawnRadius = new NumberSetting("Spawn Radius", 16.0, 4.0, 32.0, 1.0);
   public final ModeSetting colorMode = new ModeSetting("Color", "Theme", "Theme", "Rainbow", "White");
   public final BooleanSetting bloomGlow = new BooleanSetting("Glow Bloom", true);
   public final NumberSetting bloomScale = new NumberSetting("Bloom Scale", 3.2, 1.0, 8.0, 0.1) {
      @Override
      public boolean isVisible() {
         return CubeParticlesModule.this.bloomGlow.get();
      }
   };
   public final BooleanSetting filledFaces = new BooleanSetting("Filled Faces", true);
   public final NumberSetting fillAlpha = new NumberSetting("Fill Alpha", 0.2, 0.02, 1.0, 0.02) {
      @Override
      public boolean isVisible() {
         return CubeParticlesModule.this.filledFaces.get();
      }
   };
   public final BooleanSetting wireframe = new BooleanSetting("Wireframe", true);
   public final BooleanSetting internalCore = new BooleanSetting("Internal Core", true);
   public final BooleanSetting physics = new BooleanSetting("Physics Drift", true);
   private final List<CubeParticlesModule.CubeParticle> particles = new ArrayList<>();

   public CubeParticlesModule() {
      super("CubeParticles", "Renders glowing ambient 3D geometric cubes with bloom halos around you", Module.Category.RENDER);
   }

   @Override
   public void onDisable() {
      this.particles.clear();
   }

   @Override
   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         float speedMul = this.speedMultiplier.getFloat();
         boolean usePhysics = this.physics.get();
         Iterator<CubeParticlesModule.CubeParticle> it = this.particles.iterator();

         while (it.hasNext()) {
            CubeParticlesModule.CubeParticle p = it.next();
            p.tick(speedMul, usePhysics);
            if (p.isDead(now)) {
               it.remove();
            }
         }

         int targetCount = (int)this.maxAmount.get();
         double radius = this.spawnRadius.get();
         class_243 playerPos = mc.field_1724.method_19538();
         int spawnPerTick = Math.min(2, Math.max(1, (targetCount - this.particles.size()) / 12 + 1));

         for (int i = 0; i < spawnPerTick && this.particles.size() < targetCount; i++) {
            double angle = Math.random() * Math.PI * 2.0;
            double dist = 2.5 + Math.random() * (radius - 2.5);
            double height = (Math.random() - 0.3) * 6.0;
            double spawnX = playerPos.field_1352 + Math.cos(angle) * dist;
            double spawnY = playerPos.field_1351 + height;
            double spawnZ = playerPos.field_1350 + Math.sin(angle) * dist;
            class_243 spawnPos = new class_243(spawnX, spawnY, spawnZ);
            class_243 initialMotion = new class_243((Math.random() - 0.5) * 0.015, 0.003 + Math.random() * 0.01, (Math.random() - 0.5) * 0.015);
            class_243 initialRot = new class_243(Math.random() * Math.PI * 2.0, Math.random() * Math.PI * 2.0, Math.random() * Math.PI * 2.0);
            class_243 rotSpeed = new class_243(this.randomInRange(-0.03, 0.03), this.randomInRange(-0.03, 0.03), this.randomInRange(-0.03, 0.03));
            long lifespan = (long)this.randomInRange(4000.0, 7500.0);
            float scaleVar = (float)this.randomInRange(0.85, 1.15);
            this.particles.add(new CubeParticlesModule.CubeParticle(spawnPos, initialMotion, initialRot, rotSpeed, lifespan, scaleVar));
         }
      } else {
         this.particles.clear();
      }
   }

   public void onRender3D(WorldRenderContext context) {
      if (!this.particles.isEmpty()) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null && mc.field_1687 != null) {
            class_4184 camera = context.camera();
            class_243 cameraPos = camera.method_19326();
            float tickDelta = context.tickCounter().method_60637(true);
            class_4587 ms = context.matrixStack();
            long now = System.currentTimeMillis();
            float baseSizeVal = this.baseSize.getFloat();
            boolean renderBloom = this.bloomGlow.get();
            float bloomScaleVal = this.bloomScale.getFloat();
            boolean renderFaces = this.filledFaces.get();
            float faceAlphaVal = this.fillAlpha.getFloat();
            boolean renderWire = this.wireframe.get();
            boolean renderCore = this.internalCore.get();
            if (renderBloom) {
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
               GlStateManager._texParameter(3553, 10241, 9729);
               GlStateManager._texParameter(3553, 10240, 9729);
               GlStateManager._texParameter(3553, 10242, 33071);
               GlStateManager._texParameter(3553, 10243, 33071);
               RenderSystem.setShader(class_10142.field_53880);
               class_287 bloomBuffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);

               for (CubeParticlesModule.CubeParticle p : this.particles) {
                  float alpha = p.getAlpha(now);
                  float scaleFactor = p.getScaleFactor(now);
                  if (!(alpha <= 0.005F) && !(scaleFactor <= 0.005F)) {
                     double x = class_3532.method_16436(tickDelta, p.prevPos.field_1352, p.pos.field_1352);
                     double y = class_3532.method_16436(tickDelta, p.prevPos.field_1351, p.pos.field_1351);
                     double z = class_3532.method_16436(tickDelta, p.prevPos.field_1350, p.pos.field_1350);
                     float particleSize = baseSizeVal * p.scaleMultiplier * scaleFactor;
                     float quadSize = particleSize * bloomScaleVal;
                     Color particleColor = this.getColor(p.creationTime);
                     int color = this.withAlpha(particleColor, alpha * 0.45F);
                     ms.method_22903();
                     ms.method_22904(x - cameraPos.field_1352, y - cameraPos.field_1351, z - cameraPos.field_1350);
                     ms.method_22907(camera.method_23767());
                     this.drawBillboardQuad(ms, bloomBuffer, -quadSize * 0.5F, -quadSize * 0.5F, quadSize, quadSize, color);
                     ms.method_22909();
                  }
               }

               class_9801 builtBloom = bloomBuffer.method_60794();
               if (builtBloom != null) {
                  class_286.method_43433(builtBloom);
               }
            }

            if (renderFaces) {
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.setShader(class_10142.field_53876);
               class_287 faceBuffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);

               for (CubeParticlesModule.CubeParticle p : this.particles) {
                  float alpha = p.getAlpha(now);
                  float scaleFactor = p.getScaleFactor(now);
                  if (!(alpha <= 0.005F) && !(scaleFactor <= 0.005F)) {
                     double x = class_3532.method_16436(tickDelta, p.prevPos.field_1352, p.pos.field_1352);
                     double y = class_3532.method_16436(tickDelta, p.prevPos.field_1351, p.pos.field_1351);
                     double z = class_3532.method_16436(tickDelta, p.prevPos.field_1350, p.pos.field_1350);
                     double rotX = class_3532.method_16436(tickDelta, p.prevRot.field_1352, p.rot.field_1352);
                     double rotY = class_3532.method_16436(tickDelta, p.prevRot.field_1351, p.rot.field_1351);
                     double rotZ = class_3532.method_16436(tickDelta, p.prevRot.field_1350, p.rot.field_1350);
                     float particleSize = baseSizeVal * p.scaleMultiplier * scaleFactor;
                     Color particleColor = this.getColor(p.creationTime);
                     float effectiveAlpha = alpha * faceAlphaVal;
                     ms.method_22903();
                     ms.method_22904(x - cameraPos.field_1352, y - cameraPos.field_1351, z - cameraPos.field_1350);
                     ms.method_22907(new Quaternionf().rotationXYZ((float)rotX, (float)rotY, (float)rotZ));
                     ms.method_22905(particleSize, particleSize, particleSize);
                     this.renderUnitCubeFaces(ms, faceBuffer, particleColor, effectiveAlpha);
                     ms.method_22909();
                  }
               }

               class_9801 builtFaces = faceBuffer.method_60794();
               if (builtFaces != null) {
                  class_286.method_43433(builtFaces);
               }
            }

            if (renderWire || renderCore) {
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.lineWidth(2.0F);
               RenderSystem.setShader(class_10142.field_53876);
               class_287 lineBuffer = class_289.method_1348().method_60827(class_5596.field_29344, class_290.field_1576);

               for (CubeParticlesModule.CubeParticle p : this.particles) {
                  float alpha = p.getAlpha(now);
                  float scaleFactor = p.getScaleFactor(now);
                  if (!(alpha <= 0.005F) && !(scaleFactor <= 0.005F)) {
                     double x = class_3532.method_16436(tickDelta, p.prevPos.field_1352, p.pos.field_1352);
                     double y = class_3532.method_16436(tickDelta, p.prevPos.field_1351, p.pos.field_1351);
                     double z = class_3532.method_16436(tickDelta, p.prevPos.field_1350, p.pos.field_1350);
                     double rotX = class_3532.method_16436(tickDelta, p.prevRot.field_1352, p.rot.field_1352);
                     double rotY = class_3532.method_16436(tickDelta, p.prevRot.field_1351, p.rot.field_1351);
                     double rotZ = class_3532.method_16436(tickDelta, p.prevRot.field_1350, p.rot.field_1350);
                     float particleSize = baseSizeVal * p.scaleMultiplier * scaleFactor;
                     Color particleColor = this.getColor(p.creationTime);
                     ms.method_22903();
                     ms.method_22904(x - cameraPos.field_1352, y - cameraPos.field_1351, z - cameraPos.field_1350);
                     ms.method_22907(new Quaternionf().rotationXYZ((float)rotX, (float)rotY, (float)rotZ));
                     ms.method_22905(particleSize, particleSize, particleSize);
                     if (renderCore) {
                        int diagColor = this.withAlpha(particleColor, alpha * 0.35F);
                        this.renderUnitInternalDiagonals(ms, lineBuffer, diagColor);
                     }

                     if (renderWire) {
                        int wireColor = this.withAlpha(particleColor, alpha * 0.85F);
                        this.renderUnitOutlinedBox(ms, lineBuffer, wireColor);
                     }

                     ms.method_22909();
                  }
               }

               class_9801 builtLines = lineBuffer.method_60794();
               if (builtLines != null) {
                  class_286.method_43433(builtLines);
               }

               RenderSystem.lineWidth(1.0F);
            }

            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
         }
      }
   }

   private Color getColor(long timeOffset) {
      String mode = this.colorMode.get();
      if (mode.equalsIgnoreCase("Rainbow")) {
         float hue = (float)((System.currentTimeMillis() + timeOffset) % 4000L) / 4000.0F;
         return Color.getHSBColor(hue, 0.75F, 1.0F);
      } else {
         return mode.equalsIgnoreCase("White") ? Color.WHITE : new Color(ThemeManager.getThemedColor(timeOffset));
      }
   }

   private void drawBillboardQuad(class_4587 ms, class_287 builder, float x, float y, float w, float h, int color) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      Matrix4f m = ms.method_23760().method_23761();
      builder.method_22918(m, x, y + h, 0.0F).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      builder.method_22918(m, x + w, y + h, 0.0F).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      builder.method_22918(m, x + w, y, 0.0F).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      builder.method_22918(m, x, y, 0.0F).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
   }

   private void renderUnitCubeFaces(class_4587 ms, class_287 builder, Color baseColor, float alpha) {
      Matrix4f m = ms.method_23760().method_23761();
      int topColor = this.withAlpha(this.shadeColor(baseColor, 1.0F), alpha);
      int botColor = this.withAlpha(this.shadeColor(baseColor, 0.55F), alpha);
      int frontColor = this.withAlpha(this.shadeColor(baseColor, 0.88F), alpha);
      int backColor = this.withAlpha(this.shadeColor(baseColor, 0.7F), alpha);
      int leftColor = this.withAlpha(this.shadeColor(baseColor, 0.78F), alpha);
      int rightColor = this.withAlpha(this.shadeColor(baseColor, 0.82F), alpha);
      this.renderQuad(builder, m, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, topColor);
      this.renderQuad(builder, m, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, botColor);
      this.renderQuad(builder, m, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, frontColor);
      this.renderQuad(builder, m, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, backColor);
      this.renderQuad(builder, m, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F, leftColor);
      this.renderQuad(builder, m, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, rightColor);
   }

   private void renderQuad(
      class_287 builder,
      Matrix4f m,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int color
   ) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      builder.method_22918(m, x1, y1, z1).method_22915(r, g, b, a);
      builder.method_22918(m, x2, y2, z2).method_22915(r, g, b, a);
      builder.method_22918(m, x3, y3, z3).method_22915(r, g, b, a);
      builder.method_22918(m, x4, y4, z4).method_22915(r, g, b, a);
   }

   private Color shadeColor(Color c, float factor) {
      return new Color(Math.min(255, (int)(c.getRed() * factor)), Math.min(255, (int)(c.getGreen() * factor)), Math.min(255, (int)(c.getBlue() * factor)));
   }

   private void renderUnitInternalDiagonals(class_4587 ms, class_287 builder, int color) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      Matrix4f m = ms.method_23760().method_23761();
      builder.method_22918(m, -0.5F, -0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, 0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, -0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, 0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, -0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, 0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, -0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, 0.5F, -0.5F).method_22915(r, g, b, a);
   }

   private void renderUnitOutlinedBox(class_4587 ms, class_287 builder, int color) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      Matrix4f m = ms.method_23760().method_23761();
      builder.method_22918(m, -0.5F, -0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, -0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, -0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, -0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, -0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, -0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, -0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, -0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, 0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, 0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, 0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, 0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, 0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, 0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, 0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, 0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, -0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, 0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, -0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, 0.5F, -0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, -0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, 0.5F, 0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, -0.5F, 0.5F).method_22915(r, g, b, a);
      builder.method_22918(m, -0.5F, 0.5F, 0.5F).method_22915(r, g, b, a);
   }

   private int withAlpha(Color baseColor, float alpha) {
      int a = Math.max(0, Math.min(255, (int)(alpha * 255.0F)));
      return a << 24 | baseColor.getRed() << 16 | baseColor.getGreen() << 8 | baseColor.getBlue();
   }

   private double randomInRange(double min, double max) {
      return min + Math.random() * (max - min);
   }

   @Environment(EnvType.CLIENT)
   private static class CubeParticle {
      class_243 prevPos;
      class_243 pos;
      class_243 motion;
      class_243 prevRot;
      class_243 rot;
      class_243 rotSpeed;
      final long creationTime;
      final long lifespan;
      final long fadeIn = 750L;
      final long fadeOut = 950L;
      final float scaleMultiplier;

      CubeParticle(class_243 pos, class_243 motion, class_243 rot, class_243 rotSpeed, long lifespan, float scaleMultiplier) {
         this.pos = pos;
         this.prevPos = pos;
         this.motion = motion;
         this.rot = rot;
         this.prevRot = rot;
         this.rotSpeed = rotSpeed;
         this.lifespan = lifespan;
         this.scaleMultiplier = scaleMultiplier;
         this.creationTime = System.currentTimeMillis();
      }

      void tick(float speedMul, boolean usePhysics) {
         this.prevPos = this.pos;
         this.prevRot = this.rot;
         this.pos = this.pos.method_1019(this.motion.method_1021(speedMul));
         this.rot = this.rot.method_1019(this.rotSpeed.method_1021(speedMul));
         if (usePhysics) {
            double swayX = Math.sin((System.currentTimeMillis() + this.creationTime) * 0.002) * 6.0E-4;
            double swayZ = Math.cos((System.currentTimeMillis() + this.creationTime) * 0.002) * 6.0E-4;
            this.motion = new class_243(this.motion.field_1352 * 0.98 + swayX, this.motion.field_1351 * 0.98 + 3.0E-4, this.motion.field_1350 * 0.98 + swayZ);
            this.rotSpeed = this.rotSpeed.method_1021(0.995);
         }
      }

      float getAlpha(long now) {
         long elapsed = now - this.creationTime;
         if (elapsed <= 0L) {
            return 0.0F;
         } else if (elapsed >= this.lifespan) {
            return 0.0F;
         } else if (elapsed < 750L) {
            float t = (float)elapsed / 750.0F;
            return (float)(1.0 - Math.cos(t * Math.PI * 0.5));
         } else {
            long remaining = this.lifespan - elapsed;
            if (remaining < 950L) {
               float t = (float)remaining / 950.0F;
               return (float)(1.0 - Math.cos(t * Math.PI * 0.5));
            } else {
               return 1.0F;
            }
         }
      }

      float getScaleFactor(long now) {
         long elapsed = now - this.creationTime;
         if (elapsed <= 0L) {
            return 0.0F;
         } else if (elapsed >= this.lifespan) {
            return 0.0F;
         } else if (elapsed < 750L) {
            float t = (float)elapsed / 750.0F;
            float backT = t - 1.0F;
            float s = 1.3F;
            return Math.max(0.0F, backT * backT * ((s + 1.0F) * backT + s) + 1.0F);
         } else {
            long remaining = this.lifespan - elapsed;
            if (remaining < 950L) {
               float t = (float)remaining / 950.0F;
               return Math.max(0.0F, t * t * (3.0F - 2.0F * t));
            } else {
               return 1.0F + (float)Math.sin((now + this.creationTime) * 0.0025) * 0.035F;
            }
         }
      }

      boolean isDead(long now) {
         return now - this.creationTime >= this.lifespan;
      }
   }
}
