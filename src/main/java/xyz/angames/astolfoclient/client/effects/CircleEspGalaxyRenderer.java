package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.entity.Entity;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class CircleEspGalaxyRenderer {
   private static final minecraft.util.Identifier BLOOM_TEXTURE = minecraft.util.Identifier.of("astolfoclient", "textures/effects/bloom.png");
   private final CircleEspManager manager;

   public CircleEspGalaxyRenderer(CircleEspManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context, List<CircleEspManager.CircleEspEffect> validEffects) {
      Module module = AstolfoclientClient.moduleManager.getModuleByName("TargetESP");
      if (module != null && module.isEnabled()) {
         if (!validEffects.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(515);
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(platform.GlStateManager.SrcFactor.SRC_ALPHA, platform.GlStateManager.DstFactor.ONE);
            client.render.Tessellator tessellator = client.render.Tessellator.getInstance();
            long currentTime = System.currentTimeMillis();
            int rgb = ThemeManager.getThemedColor(0L);
            Color c = new Color(rgb);
            float r = c.getRed() / 255.0F;
            float g = c.getGreen() / 255.0F;
            float b = c.getBlue() / 255.0F;

            for (CircleEspManager.CircleEspEffect effect : validEffects) {
               minecraft.entity.Entity target = effect.target;
               if (target.isAlive() && !TargetUtils.isInvisible(target)) {
                  long timeSinceHit = currentTime - effect.lastHitTime;
                  float fadeProgress = (float)timeSinceHit / 450.0F;
                  float alpha = 1.0F - Math.max(0.0F, (fadeProgress - 0.5F) * 2.0F);
                  if (!(alpha <= 0.05F)) {
                     float animTime = (float)(currentTime - effect.startTime) / 1000.0F;
                     float tickDelta = context.tickCounter().getTickDelta(true);
                     double tX = target.lastRenderX + (target.getX() - target.lastRenderX) * tickDelta;
                     double tY = target.lastRenderY + (target.getY() - target.lastRenderY) * tickDelta;
                     double tZ = target.lastRenderZ + (target.getZ() - target.lastRenderZ) * tickDelta;
                     float height = target.getHeight();
                     float radius = target.getWidth() * 0.8F;
                     if (timeSinceHit > 400L) {
                        float endProgress = (float)(timeSinceHit - 400L) / 200.0F;
                        radius *= Math.max(0.0F, 1.0F - endProgress);
                     }

                     float scanSpeed = 3.0F;
                     float phase = (float)Math.sin(animTime * scanSpeed);
                     float scanY = (phase + 1.0F) / 2.0F * height;
                     float velocity = (float)Math.cos(animTime * scanSpeed);
                     float maxTailLength = height * 0.4F;
                     float tailOffset = -velocity * maxTailLength;
                     util.math.MatrixStack matrices = context.matrixStack();
                     matrices.push();
                     matrices.translate(
                        tX - context.camera().getPos().x,
                        tY - context.camera().getPos().y,
                        tZ - context.camera().getPos().z
                     );
                     matrices.multiply(util.math.RotationAxis.POSITIVE_Y.rotationDegrees(animTime * 45.0F));
                     RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
                     RenderSystem.setShader(client.gl.ShaderProgramKeys.POSITION_TEX_COLOR);
                     client.render.BufferBuilder bbBloom = tessellator.begin(render.VertexFormat.DrawMode.QUADS, client.render.VertexFormats.POSITION_TEXTURE_COLOR);
                     this.drawHorizontalBloom(matrices, bbBloom, scanY, radius * 2.5F, r, g, b, alpha * 0.5F);
                     client.render.BufferRenderer.drawWithGlobalProgram(bbBloom.end());
                     RenderSystem.setShader(client.gl.ShaderProgramKeys.POSITION_COLOR);
                     client.render.BufferBuilder bbTail = tessellator.begin(render.VertexFormat.DrawMode.QUADS, client.render.VertexFormats.POSITION_COLOR);
                     this.drawGradientCylinder(matrices, bbTail, radius, scanY, scanY + tailOffset, r, g, b, alpha * 0.45F, 0.0F);
                     client.render.BufferRenderer.drawWithGlobalProgram(bbTail.end());
                     RenderSystem.lineWidth(2.5F);
                     client.render.BufferBuilder bbRing = tessellator.begin(render.VertexFormat.DrawMode.DEBUG_LINE_STRIP, client.render.VertexFormats.POSITION_COLOR);
                     this.drawCrispRing(matrices, bbRing, radius, scanY, r, g, b, alpha * 0.9F);
                     client.render.BufferRenderer.drawWithGlobalProgram(bbRing.end());
                     RenderSystem.lineWidth(1.0F);
                     matrices.pop();
                  }
               }
            }

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }

   private void drawHorizontalBloom(util.math.MatrixStack stack, client.render.BufferBuilder buffer, float y, float size, float r, float g, float b, float a) {
      Matrix4f m = stack.peek().getPositionMatrix();
      buffer.vertex(m, -size, y, -size).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, -size, y, size).texture(0.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, size, y, size).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, size, y, -size).texture(1.0F, 0.0F).color(r, g, b, a);
   }

   private void drawGradientCylinder(
      util.math.MatrixStack stack, client.render.BufferBuilder buffer, float radius, float yStart, float yEnd, float r, float g, float b, float aStart, float aEnd
   ) {
      Matrix4f m = stack.peek().getPositionMatrix();
      int segments = 40;

      for (int i = 0; i < segments; i++) {
         float angle1 = (float)(i * Math.PI * 2.0 / segments);
         float angle2 = (float)((i + 1) * Math.PI * 2.0 / segments);
         float x1 = (float)Math.cos(angle1) * radius;
         float z1 = (float)Math.sin(angle1) * radius;
         float x2 = (float)Math.cos(angle2) * radius;
         float z2 = (float)Math.sin(angle2) * radius;
         buffer.vertex(m, x1, yStart, z1).color(r, g, b, aStart);
         buffer.vertex(m, x1, yEnd, z1).color(r, g, b, aEnd);
         buffer.vertex(m, x2, yEnd, z2).color(r, g, b, aEnd);
         buffer.vertex(m, x2, yStart, z2).color(r, g, b, aStart);
      }
   }

   private void drawCrispRing(util.math.MatrixStack stack, client.render.BufferBuilder buffer, float radius, float y, float r, float g, float b, float a) {
      Matrix4f m = stack.peek().getPositionMatrix();
      int segments = 40;

      for (int i = 0; i <= segments; i++) {
         float angle = (float)(i * Math.PI * 2.0 / segments);
         float x = (float)Math.cos(angle) * radius;
         float z = (float)Math.sin(angle) * radius;
         buffer.vertex(m, x, y, z).color(r, g, b, a);
      }
   }
}
