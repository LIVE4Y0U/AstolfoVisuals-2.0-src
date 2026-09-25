package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
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
import xyz.angames.astolfoclient.client.module.modules.TargetEspModule;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class DiamondEspRenderer {
   private static final Identifier BLOOM_TEXTURE = Identifier.of("astolfoclient", "textures/effects/bloom.png");
   private final DiamondEspManager manager;

   public DiamondEspRenderer(DiamondEspManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context) {
      Module targetEspModule = AstolfoclientClient.moduleManager.getModuleByName("TargetESP");
      if (targetEspModule != null && targetEspModule.isEnabled()) {
         if (!(targetEspModule instanceof TargetEspModule tem && !tem.mode.is("Diamond"))) {
            Map<Entity, DiamondEspManager.DiamondEffect> allEffects = this.manager.getEffects();
            if (!allEffects.isEmpty()) {
               List<DiamondEspManager.DiamondEffect> validEffects = new ArrayList<>(allEffects.values());
               if (!validEffects.isEmpty()) {
                  RenderSystem.enableBlend();
                  RenderSystem.disableCull();
                  RenderSystem.enableDepthTest();
                  RenderSystem.depthFunc(515);
                  RenderSystem.depthMask(false);
                  RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                  Tessellator tessellator = Tessellator.getInstance();
                  long currentTime = System.currentTimeMillis();
                  Vec3d cameraPos = context.camera().getPos();

                  for (DiamondEspManager.DiamondEffect effect : validEffects) {
                     Entity target = effect.target;
                     if (target != null && target.isAlive() && !TargetUtils.isInvisible(target)) {
                        long timeSinceStart = currentTime - effect.startTime;
                        long timeSinceHit = currentTime - effect.lastHitTime;
                        if (timeSinceHit <= 450L) {
                           float fastTime = (float)timeSinceStart / 1000.0F;
                           float tickDelta = context.tickCounter().getTickDelta(true);
                           double tX = target.lastRenderX + (target.getX() - target.lastRenderX) * tickDelta;
                           double tY = target.lastRenderY + (target.getY() - target.lastRenderY) * tickDelta;
                           double tZ = target.lastRenderZ + (target.getZ() - target.lastRenderZ) * tickDelta;
                           float entityCenterY = (float)(tY + target.getHeight() * 0.5F);
                           float arriveProgress = Math.min(1.0F, (float)timeSinceStart / 400.0F);
                           float arriveEase = this.easeOutCubic(arriveProgress);
                           float scatterProgress = Math.max(0.0F, ((float)timeSinceHit - 360.0F) / 90.0F);
                           float scatterEase = this.easeInQuad(Math.min(1.0F, scatterProgress));
                           float baseAlpha = arriveProgress < 1.0F ? arriveEase : 1.0F - scatterEase;
                           if (!(baseAlpha <= 0.01F)) {
                              long timeSinceAttack = currentTime - effect.lastAttackTime;
                              float hitColorFactor = Math.max(0.0F, 1.0F - (float)timeSinceAttack / 350.0F);
                              int rgb = ThemeManager.getThemedColor(0L);
                              Color themeColor = new Color(rgb);
                              float themeR = themeColor.getRed() / 255.0F;
                              float themeG = themeColor.getGreen() / 255.0F;
                              float themeB = themeColor.getBlue() / 255.0F;
                              float r = hitColorFactor * 1.0F + (1.0F - hitColorFactor) * themeR;
                              float g = hitColorFactor * 0.0F + (1.0F - hitColorFactor) * themeG;
                              float b = hitColorFactor * 0.0F + (1.0F - hitColorFactor) * themeB;
                              float cr = 1.0F;
                              float cg = 1.0F - hitColorFactor;
                              float cb = 1.0F - hitColorFactor;
                              int numDiamonds = 18;
                              float baseRadius = target.getWidth() * 1.1F;
                              MatrixStack matrices = context.matrixStack();
                              RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
                              RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                              BufferBuilder bbBloom = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                              for (int i = 0; i < numDiamonds; i++) {
                                 float[] pos = this.calculateDiamondPosition(
                                    i, numDiamonds, fastTime, baseRadius, target.getHeight(), arriveEase, scatterEase
                                 );
                                 matrices.push();
                                 matrices.translate(
                                    tX + pos[0] - cameraPos.x, entityCenterY + pos[1] - cameraPos.y, tZ + pos[2] - cameraPos.z
                                 );
                                 matrices.multiply(context.camera().getRotation());
                                 this.drawBloom(matrices, bbBloom, 0.35F, r, g, b, baseAlpha * 0.6F * pos[3]);
                                 matrices.pop();
                              }

                              BufferRenderer.drawWithGlobalProgram(bbBloom.end());
                              RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                              BufferBuilder bbSolid = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

                              for (int i = 0; i < numDiamonds; i++) {
                                 float[] pos = this.calculateDiamondPosition(
                                    i, numDiamonds, fastTime, baseRadius, target.getHeight(), arriveEase, scatterEase
                                 );
                                 matrices.push();
                                 matrices.translate(
                                    tX + pos[0] - cameraPos.x, entityCenterY + pos[1] - cameraPos.y, tZ + pos[2] - cameraPos.z
                                 );
                                 matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(fastTime * 60.0F + i * 15.0F));
                                 matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(fastTime * 40.0F + i * 10.0F));
                                 this.draw3DDiamond(matrices, bbSolid, 0.08F, r, g, b, baseAlpha * 0.8F * pos[3]);
                                 this.draw3DDiamond(matrices, bbSolid, 0.07F, cr, cg, cb, baseAlpha * pos[3]);
                                 matrices.pop();
                              }

                              BufferRenderer.drawWithGlobalProgram(bbSolid.end());
                              RenderSystem.lineWidth(1.5F);
                              BufferBuilder bbLines = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

                              for (int i = 0; i < numDiamonds; i++) {
                                 float[] pos = this.calculateDiamondPosition(
                                    i, numDiamonds, fastTime, baseRadius, target.getHeight(), arriveEase, scatterEase
                                 );
                                 matrices.push();
                                 matrices.translate(
                                    tX + pos[0] - cameraPos.x, entityCenterY + pos[1] - cameraPos.y, tZ + pos[2] - cameraPos.z
                                 );
                                 matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(fastTime * 60.0F + i * 15.0F));
                                 matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(fastTime * 40.0F + i * 10.0F));
                                 this.draw3DDiamondLines(matrices, bbLines, 0.08F, r, g, b, baseAlpha * pos[3]);
                                 matrices.pop();
                              }

                              BufferRenderer.drawWithGlobalProgram(bbLines.end());
                              RenderSystem.lineWidth(1.0F);
                           }
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
      }
   }

   private float[] calculateDiamondPosition(int i, int total, float fastTime, float baseRadius, float targetHeight, float arriveEase, float scatterEase) {
      float loops = 1.5F;
      float orbitSpeed = 0.8F;
      boolean isSecondSpiral = i % 2 == 1;
      int totalPerSpiral = total / 2;
      int indexInSpiral = i / 2;
      float progress = totalPerSpiral > 1 ? (float)indexInSpiral / (totalPerSpiral - 1) : 0.5F;
      float height = this.lerp(-targetHeight * 0.5F, targetHeight * 0.7F, progress);
      float helixAngle = progress * loops * 2.0F * (float) Math.PI;
      float spiralOffset = isSecondSpiral ? (float) Math.PI : 0.0F;
      float angle = helixAngle + spiralOffset + fastTime * orbitSpeed;
      float rad = baseRadius;
      float alphaMultiplier = 1.0F;
      if (arriveEase < 1.0F) {
         rad = baseRadius + (1.0F - arriveEase) * 3.0F;
         height += (1.0F - arriveEase) * 2.0F;
         alphaMultiplier = arriveEase;
      } else if (scatterEase > 0.0F) {
         rad = baseRadius + scatterEase * 3.0F;
         height += scatterEase * 2.0F;
         alphaMultiplier = 1.0F - scatterEase;
      }

      float dx = (float)Math.cos(angle) * rad;
      float dz = (float)Math.sin(angle) * rad;
      return new float[]{dx, height, dz, alphaMultiplier};
   }

   private float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   private float easeOutCubic(float t) {
      return 1.0F - (float)Math.pow(1.0F - t, 3.0);
   }

   private float easeInQuad(float t) {
      return t * t;
   }

   private void drawBloom(MatrixStack stack, BufferBuilder buffer, float size, float r, float g, float b, float a) {
      Matrix4f m = stack.peek().getPositionMatrix();
      buffer.vertex(m, -size, size, 0.0F).texture(0.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, size, size, 0.0F).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, size, -size, 0.0F).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, -size, -size, 0.0F).texture(0.0F, 0.0F).color(r, g, b, a);
   }

   private void draw3DDiamond(MatrixStack stack, BufferBuilder buffer, float size, float r, float g, float b, float a) {
      Matrix4f m = stack.peek().getPositionMatrix();
      float h = size * 1.8F;
      float w = size * 0.7F;
      this.drawTri(m, buffer, 0.0F, h, 0.0F, -w, 0.0F, -w, w, 0.0F, -w, r, g, b, a);
      this.drawTri(m, buffer, 0.0F, h, 0.0F, w, 0.0F, -w, w, 0.0F, w, r, g, b, a);
      this.drawTri(m, buffer, 0.0F, h, 0.0F, w, 0.0F, w, -w, 0.0F, w, r, g, b, a);
      this.drawTri(m, buffer, 0.0F, h, 0.0F, -w, 0.0F, w, -w, 0.0F, -w, r, g, b, a);
      this.drawTri(m, buffer, 0.0F, -h, 0.0F, w, 0.0F, -w, -w, 0.0F, -w, r, g, b, a);
      this.drawTri(m, buffer, 0.0F, -h, 0.0F, w, 0.0F, w, w, 0.0F, -w, r, g, b, a);
      this.drawTri(m, buffer, 0.0F, -h, 0.0F, -w, 0.0F, w, w, 0.0F, w, r, g, b, a);
      this.drawTri(m, buffer, 0.0F, -h, 0.0F, -w, 0.0F, -w, -w, 0.0F, w, r, g, b, a);
   }

   private void drawTri(
      Matrix4f m, BufferBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float r, float g, float bl, float a
   ) {
      b.vertex(m, x1, y1, z1).color(r, g, bl, a);
      b.vertex(m, x2, y2, z2).color(r, g, bl, a);
      b.vertex(m, x3, y3, z3).color(r, g, bl, a);
   }

   private void draw3DDiamondLines(MatrixStack stack, BufferBuilder buffer, float size, float r, float g, float b, float a) {
      Matrix4f m = stack.peek().getPositionMatrix();
      float h = size * 1.8F;
      float w = size * 0.7F;
      this.drawLine(m, buffer, -w, 0.0F, -w, w, 0.0F, -w, r, g, b, a);
      this.drawLine(m, buffer, w, 0.0F, -w, w, 0.0F, w, r, g, b, a);
      this.drawLine(m, buffer, w, 0.0F, w, -w, 0.0F, w, r, g, b, a);
      this.drawLine(m, buffer, -w, 0.0F, w, -w, 0.0F, -w, r, g, b, a);
      this.drawLine(m, buffer, 0.0F, h, 0.0F, -w, 0.0F, -w, r, g, b, a);
      this.drawLine(m, buffer, 0.0F, h, 0.0F, w, 0.0F, -w, r, g, b, a);
      this.drawLine(m, buffer, 0.0F, h, 0.0F, w, 0.0F, w, r, g, b, a);
      this.drawLine(m, buffer, 0.0F, h, 0.0F, -w, 0.0F, w, r, g, b, a);
      this.drawLine(m, buffer, 0.0F, -h, 0.0F, -w, 0.0F, -w, r, g, b, a);
      this.drawLine(m, buffer, 0.0F, -h, 0.0F, w, 0.0F, -w, r, g, b, a);
      this.drawLine(m, buffer, 0.0F, -h, 0.0F, w, 0.0F, w, r, g, b, a);
      this.drawLine(m, buffer, 0.0F, -h, 0.0F, -w, 0.0F, w, r, g, b, a);
   }

   private void drawLine(Matrix4f m, BufferBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float bl, float a) {
      b.vertex(m, x1, y1, z1).color(r, g, bl, a);
      b.vertex(m, x2, y2, z2).color(r, g, bl, a);
   }
}
