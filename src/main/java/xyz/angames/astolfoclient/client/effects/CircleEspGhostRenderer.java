package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class CircleEspGhostRenderer {
   public void render(WorldRenderContext context, List<CircleEspManager.CircleEspEffect> effects) {
      if (!effects.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(515);
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         class_289 tessellator = class_289.method_1348();
         long currentTime = System.currentTimeMillis();
         class_4587 matrices = context.matrixStack();
         double camX = context.camera().method_19326().field_1352;
         double camY = context.camera().method_19326().field_1351;
         double camZ = context.camera().method_19326().field_1350;
         Quaternionf cameraRot = context.camera().method_23767();

         for (CircleEspManager.CircleEspEffect effect : effects) {
            class_1297 target = effect.target;
            if (target.method_5805() && !TargetUtils.isInvisible(target)) {
               long timeSinceHit = currentTime - effect.lastHitTime;
               float fadeProgress = class_3532.method_15363((float)timeSinceHit / 450.0F, 0.0F, 1.0F);
               float baseAlpha = 1.0F - Math.max(0.0F, (fadeProgress - 0.5F) * 2.0F);
               if (!(baseAlpha <= 0.05F)) {
                  float animTime = (float)(currentTime - effect.startTime) / 1000.0F;
                  float tickDelta = context.tickCounter().method_60637(true);
                  double tX = class_3532.method_16436(tickDelta, target.field_6038, target.method_23317()) - camX;
                  double tY = class_3532.method_16436(tickDelta, target.field_5971, target.method_23318()) - camY;
                  double tZ = class_3532.method_16436(tickDelta, target.field_5989, target.method_23321()) - camZ;
                  float height = target.method_17682();
                  float radius = target.method_17681() / 2.0F + 0.1F;
                  if (timeSinceHit > 400L) {
                     float endProgress = (float)(timeSinceHit - 400L) / 200.0F;
                     radius *= Math.max(0.0F, 1.0F - endProgress);
                  }

                  float speedY = 3.5F;
                  RenderSystem.setShader(class_10142.field_53876);
                  class_287 buffer = tessellator.method_60827(class_5596.field_27379, class_290.field_1576);
                  float mainY = (float)((Math.sin(animTime * speedY) + 1.0) / 2.0) * height;
                  Color headColor = new Color(ThemeManager.getThemedColor(0L));
                  float rH = headColor.getRed() / 255.0F;
                  float gH = headColor.getGreen() / 255.0F;
                  float bH = headColor.getBlue() / 255.0F;

                  for (int angle = 0; angle < 360; angle += 10) {
                     double rad = Math.toRadians(angle);
                     float pX = (float)Math.cos(rad) * radius;
                     float pZ = (float)Math.sin(rad) * radius;
                     matrices.method_22903();
                     matrices.method_22904(tX + pX, tY + mainY, tZ + pZ);
                     matrices.method_22907(cameraRot);
                     float scale = 0.12F;
                     matrices.method_22905(scale, scale, scale);
                     this.drawBatchedGlowingDot(matrices.method_23760().method_23761(), buffer, rH, gH, bH, baseAlpha);
                     matrices.method_22909();
                  }

                  int tailSteps = 22;

                  for (int i = 1; i <= tailSteps; i++) {
                     float tHist = animTime - i * 0.025F;
                     if (!(tHist < 0.0F)) {
                        float histY = (float)((Math.sin(tHist * speedY) + 1.0) / 2.0) * height;
                        float tailProgress = (float)i / tailSteps;
                        float alphaMultiplier = (float)Math.pow(1.0 - tailProgress, 2.0);
                        float tailAlpha = baseAlpha * alphaMultiplier * 0.6F;
                        float tailRadius = radius * (1.0F - tailProgress * 0.15F);
                        Color tColor = new Color(ThemeManager.getThemedColor(i * 15));
                        float rT = tColor.getRed() / 255.0F;
                        float gT = tColor.getGreen() / 255.0F;
                        float bT = tColor.getBlue() / 255.0F;

                        for (int angle = 0; angle < 360; angle += 12) {
                           double rad = Math.toRadians(angle);
                           float pX = (float)Math.cos(rad) * tailRadius;
                           float pZ = (float)Math.sin(rad) * tailRadius;
                           matrices.method_22903();
                           matrices.method_22904(tX + pX, tY + histY, tZ + pZ);
                           matrices.method_22907(cameraRot);
                           float scale = 0.12F * (1.0F - tailProgress * 0.5F);
                           matrices.method_22905(scale, scale, scale);
                           this.drawBatchedGlowingDot(matrices.method_23760().method_23761(), buffer, rT, gT, bT, tailAlpha);
                           matrices.method_22909();
                        }
                     }
                  }

                  class_286.method_43433(buffer.method_60800());
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

   private void drawBatchedGlowingDot(Matrix4f matrix, class_287 buffer, float r, float g, float b, float alpha) {
      float coreR = r * 0.15F + 0.85F;
      float coreG = g * 0.15F + 0.85F;
      float coreB = b * 0.15F + 0.85F;

      for (int i = 0; i < 360; i += 30) {
         double rad1 = Math.toRadians(i);
         double rad2 = Math.toRadians(i + 30);
         float px1 = (float)Math.cos(rad1);
         float py1 = (float)Math.sin(rad1);
         float px2 = (float)Math.cos(rad2);
         float py2 = (float)Math.sin(rad2);
         buffer.method_22918(matrix, 0.0F, 0.0F, 0.0F).method_22915(coreR, coreG, coreB, alpha);
         buffer.method_22918(matrix, px1, py1, 0.0F).method_22915(r, g, b, 0.0F);
         buffer.method_22918(matrix, px2, py2, 0.0F).method_22915(r, g, b, 0.0F);
      }
   }
}
