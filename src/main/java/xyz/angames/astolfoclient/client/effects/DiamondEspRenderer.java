package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.TargetEspModule;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class DiamondEspRenderer {
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/bloom.png");
   private final DiamondEspManager manager;

   public DiamondEspRenderer(DiamondEspManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context) {
      Module targetEspModule = AstolfoclientClient.moduleManager.getModuleByName("TargetESP");
      if (targetEspModule != null && targetEspModule.isEnabled()) {
         if (!(targetEspModule instanceof TargetEspModule tem && !tem.mode.is("Diamond"))) {
            Map<class_1297, DiamondEspManager.DiamondEffect> allEffects = this.manager.getEffects();
            if (!allEffects.isEmpty()) {
               List<DiamondEspManager.DiamondEffect> validEffects = new ArrayList<>(allEffects.values());
               if (!validEffects.isEmpty()) {
                  RenderSystem.enableBlend();
                  RenderSystem.disableCull();
                  RenderSystem.enableDepthTest();
                  RenderSystem.depthFunc(515);
                  RenderSystem.depthMask(false);
                  RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
                  class_289 tessellator = class_289.method_1348();
                  long currentTime = System.currentTimeMillis();
                  class_243 cameraPos = context.camera().method_19326();

                  for (DiamondEspManager.DiamondEffect effect : validEffects) {
                     class_1297 target = effect.target;
                     if (target != null && target.method_5805() && !TargetUtils.isInvisible(target)) {
                        long timeSinceStart = currentTime - effect.startTime;
                        long timeSinceHit = currentTime - effect.lastHitTime;
                        if (timeSinceHit <= 450L) {
                           float fastTime = (float)timeSinceStart / 1000.0F;
                           float tickDelta = context.tickCounter().method_60637(true);
                           double tX = target.field_6038 + (target.method_23317() - target.field_6038) * tickDelta;
                           double tY = target.field_5971 + (target.method_23318() - target.field_5971) * tickDelta;
                           double tZ = target.field_5989 + (target.method_23321() - target.field_5989) * tickDelta;
                           float entityCenterY = (float)(tY + target.method_17682() * 0.5F);
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
                              float baseRadius = target.method_17681() * 1.1F;
                              class_4587 matrices = context.matrixStack();
                              RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
                              RenderSystem.setShader(class_10142.field_53880);
                              class_287 bbBloom = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);

                              for (int i = 0; i < numDiamonds; i++) {
                                 float[] pos = this.calculateDiamondPosition(
                                    i, numDiamonds, fastTime, baseRadius, target.method_17682(), arriveEase, scatterEase
                                 );
                                 matrices.method_22903();
                                 matrices.method_22904(
                                    tX + pos[0] - cameraPos.field_1352, entityCenterY + pos[1] - cameraPos.field_1351, tZ + pos[2] - cameraPos.field_1350
                                 );
                                 matrices.method_22907(context.camera().method_23767());
                                 this.drawBloom(matrices, bbBloom, 0.35F, r, g, b, baseAlpha * 0.6F * pos[3]);
                                 matrices.method_22909();
                              }

                              class_286.method_43433(bbBloom.method_60800());
                              RenderSystem.setShader(class_10142.field_53876);
                              class_287 bbSolid = tessellator.method_60827(class_5596.field_27379, class_290.field_1576);

                              for (int i = 0; i < numDiamonds; i++) {
                                 float[] pos = this.calculateDiamondPosition(
                                    i, numDiamonds, fastTime, baseRadius, target.method_17682(), arriveEase, scatterEase
                                 );
                                 matrices.method_22903();
                                 matrices.method_22904(
                                    tX + pos[0] - cameraPos.field_1352, entityCenterY + pos[1] - cameraPos.field_1351, tZ + pos[2] - cameraPos.field_1350
                                 );
                                 matrices.method_22907(class_7833.field_40716.rotationDegrees(fastTime * 60.0F + i * 15.0F));
                                 matrices.method_22907(class_7833.field_40714.rotationDegrees(fastTime * 40.0F + i * 10.0F));
                                 this.draw3DDiamond(matrices, bbSolid, 0.08F, r, g, b, baseAlpha * 0.8F * pos[3]);
                                 this.draw3DDiamond(matrices, bbSolid, 0.07F, cr, cg, cb, baseAlpha * pos[3]);
                                 matrices.method_22909();
                              }

                              class_286.method_43433(bbSolid.method_60800());
                              RenderSystem.lineWidth(1.5F);
                              class_287 bbLines = tessellator.method_60827(class_5596.field_29344, class_290.field_1576);

                              for (int i = 0; i < numDiamonds; i++) {
                                 float[] pos = this.calculateDiamondPosition(
                                    i, numDiamonds, fastTime, baseRadius, target.method_17682(), arriveEase, scatterEase
                                 );
                                 matrices.method_22903();
                                 matrices.method_22904(
                                    tX + pos[0] - cameraPos.field_1352, entityCenterY + pos[1] - cameraPos.field_1351, tZ + pos[2] - cameraPos.field_1350
                                 );
                                 matrices.method_22907(class_7833.field_40716.rotationDegrees(fastTime * 60.0F + i * 15.0F));
                                 matrices.method_22907(class_7833.field_40714.rotationDegrees(fastTime * 40.0F + i * 10.0F));
                                 this.draw3DDiamondLines(matrices, bbLines, 0.08F, r, g, b, baseAlpha * pos[3]);
                                 matrices.method_22909();
                              }

                              class_286.method_43433(bbLines.method_60800());
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

   private void drawBloom(class_4587 stack, class_287 buffer, float size, float r, float g, float b, float a) {
      Matrix4f m = stack.method_23760().method_23761();
      buffer.method_22918(m, -size, size, 0.0F).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, size, size, 0.0F).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, size, -size, 0.0F).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, -size, -size, 0.0F).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
   }

   private void draw3DDiamond(class_4587 stack, class_287 buffer, float size, float r, float g, float b, float a) {
      Matrix4f m = stack.method_23760().method_23761();
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
      Matrix4f m, class_287 b, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float r, float g, float bl, float a
   ) {
      b.method_22918(m, x1, y1, z1).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y2, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x3, y3, z3).method_22915(r, g, bl, a);
   }

   private void draw3DDiamondLines(class_4587 stack, class_287 buffer, float size, float r, float g, float b, float a) {
      Matrix4f m = stack.method_23760().method_23761();
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

   private void drawLine(Matrix4f m, class_287 b, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float bl, float a) {
      b.method_22918(m, x1, y1, z1).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y2, z2).method_22915(r, g, bl, a);
   }
}
