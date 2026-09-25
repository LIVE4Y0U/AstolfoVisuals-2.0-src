package xyz.angames.astolfoclient.client.module.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexFormat;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;
import xyz.angames.astolfoclient.client.module.setting.ModeSetting;
import xyz.angames.astolfoclient.client.module.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public class CubeParticlesModule extends Module {
   private static final Identifier BLOOM_TEXTURE = Identifier.of("astolfoclient", "textures/effects/bloom.png");
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
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null && mc.world != null) {
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
         Vec3d playerPos = mc.player.getPos();
         int spawnPerTick = Math.min(2, Math.max(1, (targetCount - this.particles.size()) / 12 + 1));

         for (int i = 0; i < spawnPerTick && this.particles.size() < targetCount; i++) {
            double angle = Math.random() * Math.PI * 2.0;
            double dist = 2.5 + Math.random() * (radius - 2.5);
            double height = (Math.random() - 0.3) * 6.0;
            double spawnX = playerPos.x + Math.cos(angle) * dist;
            double spawnY = playerPos.y + height;
            double spawnZ = playerPos.z + Math.sin(angle) * dist;
            Vec3d spawnPos = new Vec3d(spawnX, spawnY, spawnZ);
            Vec3d initialMotion = new Vec3d((Math.random() - 0.5) * 0.015, 0.003 + Math.random() * 0.01, (Math.random() - 0.5) * 0.015);
            Vec3d initialRot = new Vec3d(Math.random() * Math.PI * 2.0, Math.random() * Math.PI * 2.0, Math.random() * Math.PI * 2.0);
            Vec3d rotSpeed = new Vec3d(this.randomInRange(-0.03, 0.03), this.randomInRange(-0.03, 0.03), this.randomInRange(-0.03, 0.03));
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
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.player != null && mc.world != null) {
            Camera camera = context.camera();
            Vec3d cameraPos = camera.getPos();
            float tickDelta = context.tickCounter().getTickDelta(true);
            MatrixStack ms = context.matrixStack();
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
               RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
               GlStateManager._texParameter(3553, 10241, 9729);
               GlStateManager._texParameter(3553, 10240, 9729);
               GlStateManager._texParameter(3553, 10242, 33071);
               GlStateManager._texParameter(3553, 10243, 33071);
               RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
               BufferBuilder bloomBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

               for (CubeParticlesModule.CubeParticle p : this.particles) {
                  float alpha = p.getAlpha(now);
                  float scaleFactor = p.getScaleFactor(now);
                  if (!(alpha <= 0.005F) && !(scaleFactor <= 0.005F)) {
                     double x = MathHelper.lerp(tickDelta, p.prevPos.x, p.pos.x);
                     double y = MathHelper.lerp(tickDelta, p.prevPos.y, p.pos.y);
                     double z = MathHelper.lerp(tickDelta, p.prevPos.z, p.pos.z);
                     float particleSize = baseSizeVal * p.scaleMultiplier * scaleFactor;
                     float quadSize = particleSize * bloomScaleVal;
                     Color particleColor = this.getColor(p.creationTime);
                     int color = this.withAlpha(particleColor, alpha * 0.45F);
                     ms.push();
                     ms.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
                     ms.multiply(camera.getRotation());
                     this.drawBillboardQuad(ms, bloomBuffer, -quadSize * 0.5F, -quadSize * 0.5F, quadSize, quadSize, color);
                     ms.pop();
                  }
               }

               BuiltBuffer builtBloom = bloomBuffer.endNullable();
               if (builtBloom != null) {
                  BufferRenderer.drawWithGlobalProgram(builtBloom);
               }
            }

            if (renderFaces) {
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
               BufferBuilder faceBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

               for (CubeParticlesModule.CubeParticle p : this.particles) {
                  float alpha = p.getAlpha(now);
                  float scaleFactor = p.getScaleFactor(now);
                  if (!(alpha <= 0.005F) && !(scaleFactor <= 0.005F)) {
                     double x = MathHelper.lerp(tickDelta, p.prevPos.x, p.pos.x);
                     double y = MathHelper.lerp(tickDelta, p.prevPos.y, p.pos.y);
                     double z = MathHelper.lerp(tickDelta, p.prevPos.z, p.pos.z);
                     double rotX = MathHelper.lerp(tickDelta, p.prevRot.x, p.rot.x);
                     double rotY = MathHelper.lerp(tickDelta, p.prevRot.y, p.rot.y);
                     double rotZ = MathHelper.lerp(tickDelta, p.prevRot.z, p.rot.z);
                     float particleSize = baseSizeVal * p.scaleMultiplier * scaleFactor;
                     Color particleColor = this.getColor(p.creationTime);
                     float effectiveAlpha = alpha * faceAlphaVal;
                     ms.push();
                     ms.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
                     ms.multiply(new Quaternionf().rotationXYZ((float)rotX, (float)rotY, (float)rotZ));
                     ms.scale(particleSize, particleSize, particleSize);
                     this.renderUnitCubeFaces(ms, faceBuffer, particleColor, effectiveAlpha);
                     ms.pop();
                  }
               }

               BuiltBuffer builtFaces = faceBuffer.endNullable();
               if (builtFaces != null) {
                  BufferRenderer.drawWithGlobalProgram(builtFaces);
               }
            }

            if (renderWire || renderCore) {
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.lineWidth(2.0F);
               RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
               BufferBuilder lineBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

               for (CubeParticlesModule.CubeParticle p : this.particles) {
                  float alpha = p.getAlpha(now);
                  float scaleFactor = p.getScaleFactor(now);
                  if (!(alpha <= 0.005F) && !(scaleFactor <= 0.005F)) {
                     double x = MathHelper.lerp(tickDelta, p.prevPos.x, p.pos.x);
                     double y = MathHelper.lerp(tickDelta, p.prevPos.y, p.pos.y);
                     double z = MathHelper.lerp(tickDelta, p.prevPos.z, p.pos.z);
                     double rotX = MathHelper.lerp(tickDelta, p.prevRot.x, p.rot.x);
                     double rotY = MathHelper.lerp(tickDelta, p.prevRot.y, p.rot.y);
                     double rotZ = MathHelper.lerp(tickDelta, p.prevRot.z, p.rot.z);
                     float particleSize = baseSizeVal * p.scaleMultiplier * scaleFactor;
                     Color particleColor = this.getColor(p.creationTime);
                     ms.push();
                     ms.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
                     ms.multiply(new Quaternionf().rotationXYZ((float)rotX, (float)rotY, (float)rotZ));
                     ms.scale(particleSize, particleSize, particleSize);
                     if (renderCore) {
                        int diagColor = this.withAlpha(particleColor, alpha * 0.35F);
                        this.renderUnitInternalDiagonals(ms, lineBuffer, diagColor);
                     }

                     if (renderWire) {
                        int wireColor = this.withAlpha(particleColor, alpha * 0.85F);
                        this.renderUnitOutlinedBox(ms, lineBuffer, wireColor);
                     }

                     ms.pop();
                  }
               }

               BuiltBuffer builtLines = lineBuffer.endNullable();
               if (builtLines != null) {
                  BufferRenderer.drawWithGlobalProgram(builtLines);
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

   private void drawBillboardQuad(MatrixStack ms, BufferBuilder builder, float x, float y, float w, float h, int color) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      Matrix4f m = ms.peek().getPositionMatrix();
      builder.vertex(m, x, y + h, 0.0F).texture(0.0F, 1.0F).color(r, g, b, a);
      builder.vertex(m, x + w, y + h, 0.0F).texture(1.0F, 1.0F).color(r, g, b, a);
      builder.vertex(m, x + w, y, 0.0F).texture(1.0F, 0.0F).color(r, g, b, a);
      builder.vertex(m, x, y, 0.0F).texture(0.0F, 0.0F).color(r, g, b, a);
   }

   private void renderUnitCubeFaces(MatrixStack ms, BufferBuilder builder, Color baseColor, float alpha) {
      Matrix4f m = ms.peek().getPositionMatrix();
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
      BufferBuilder builder,
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
      builder.vertex(m, x1, y1, z1).color(r, g, b, a);
      builder.vertex(m, x2, y2, z2).color(r, g, b, a);
      builder.vertex(m, x3, y3, z3).color(r, g, b, a);
      builder.vertex(m, x4, y4, z4).color(r, g, b, a);
   }

   private Color shadeColor(Color c, float factor) {
      return new Color(Math.min(255, (int)(c.getRed() * factor)), Math.min(255, (int)(c.getGreen() * factor)), Math.min(255, (int)(c.getBlue() * factor)));
   }

   private void renderUnitInternalDiagonals(MatrixStack ms, BufferBuilder builder, int color) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      Matrix4f m = ms.peek().getPositionMatrix();
      builder.vertex(m, -0.5F, -0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, 0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, -0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, 0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, -0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, 0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, -0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, 0.5F, -0.5F).color(r, g, b, a);
   }

   private void renderUnitOutlinedBox(MatrixStack ms, BufferBuilder builder, int color) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      Matrix4f m = ms.peek().getPositionMatrix();
      builder.vertex(m, -0.5F, -0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, -0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, -0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, -0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, -0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, -0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, -0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, -0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, 0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, 0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, 0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, 0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, 0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, 0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, 0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, 0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, -0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, 0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, -0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, 0.5F, -0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, -0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, 0.5F, 0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, -0.5F, 0.5F).color(r, g, b, a);
      builder.vertex(m, -0.5F, 0.5F, 0.5F).color(r, g, b, a);
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
      Vec3d prevPos;
      Vec3d pos;
      Vec3d motion;
      Vec3d prevRot;
      Vec3d rot;
      Vec3d rotSpeed;
      final long creationTime;
      final long lifespan;
      final long fadeIn = 750L;
      final long fadeOut = 950L;
      final float scaleMultiplier;

      CubeParticle(Vec3d pos, Vec3d motion, Vec3d rot, Vec3d rotSpeed, long lifespan, float scaleMultiplier) {
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
         this.pos = this.pos.add(this.motion.multiply(speedMul));
         this.rot = this.rot.add(this.rotSpeed.multiply(speedMul));
         if (usePhysics) {
            double swayX = Math.sin((System.currentTimeMillis() + this.creationTime) * 0.002) * 6.0E-4;
            double swayZ = Math.cos((System.currentTimeMillis() + this.creationTime) * 0.002) * 6.0E-4;
            this.motion = new Vec3d(this.motion.x * 0.98 + swayX, this.motion.y * 0.98 + 3.0E-4, this.motion.z * 0.98 + swayZ);
            this.rotSpeed = this.rotSpeed.multiply(0.995);
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
