package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
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
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
public class KillEffectRenderer {
   private final KillEffectManager manager;
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/bloom.png");

   public KillEffectRenderer(KillEffectManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context) {
      Module module = AstolfoclientClient.moduleManager.getModuleByName("KillEffect");
      if (module != null && module.isEnabled()) {
         if (!this.manager.getEffects().isEmpty()) {
            long currentTime = System.currentTimeMillis();
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
            class_289 tessellator = class_289.method_1348();
            double camX = context.camera().method_19326().field_1352;
            double camY = context.camera().method_19326().field_1351;
            double camZ = context.camera().method_19326().field_1350;
            int rgb = ThemeManager.getThemedColor(0L);
            Color c = new Color(rgb);
            float r = c.getRed() / 255.0F;
            float g = c.getGreen() / 255.0F;
            float b = c.getBlue() / 255.0F;

            for (KillEffectManager.KillEffect effect : this.manager.getEffects()) {
               long age = currentTime - effect.startTime;
               if (age <= 3000L) {
                  float progress = (float)age / 3000.0F;
                  float globalAlpha = 1.0F - progress;
                  class_4587 matrices = context.matrixStack();
                  matrices.method_22903();
                  matrices.method_22904(effect.pos.field_1352 - camX, effect.pos.field_1351 - camY, effect.pos.field_1350 - camZ);
                  if (effect.mode.equals("Zap")) {
                     RenderSystem.setShader(class_10142.field_53876);
                     float zapAlpha = 1.0F - (float)age / 800.0F;
                     if (zapAlpha > 0.0F) {
                        class_287 buffer = tessellator.method_60827(class_5596.field_27379, class_290.field_1576);

                        for (int i = 0; i < effect.zapPoints.size() - 1; i++) {
                           class_243 p1 = effect.zapPoints.get(i);
                           class_243 p2 = effect.zapPoints.get(i + 1);
                           double distance = p1.method_1022(p2);
                           int bubbles = (int)(distance / 0.25);

                           for (int j = 0; j <= bubbles; j++) {
                              float lerp = (float)j / Math.max(1, bubbles);
                              float bx = (float)(p1.field_1352 + (p2.field_1352 - p1.field_1352) * lerp);
                              float by = (float)(p1.field_1351 + (p2.field_1351 - p1.field_1351) * lerp);
                              float bz = (float)(p1.field_1350 + (p2.field_1350 - p1.field_1350) * lerp);
                              matrices.method_22903();
                              matrices.method_46416(bx, by, bz);
                              matrices.method_22907(context.camera().method_23767());
                              float scale = 0.6F;
                              matrices.method_22905(scale, scale, scale);
                              this.drawBatchedGlowingDot(matrices.method_23760().method_23761(), buffer, r, g, b, zapAlpha);
                              matrices.method_22909();
                           }
                        }

                        class_286.method_43433(buffer.method_60800());
                     }
                  } else if (effect.mode.equals("Thanos")) {
                     RenderSystem.setShader(class_10142.field_53880);
                     RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
                     Quaternionf cameraRot = context.camera().method_23767();
                     class_287 buffer = null;

                     for (KillEffectManager.ThanosParticle p : effect.thanosParticles) {
                        float currentY = p.startY;
                        float pAlpha = globalAlpha;
                        if ((float)age > p.delay) {
                           float fallTime = (float)age - p.delay;
                           currentY -= fallTime * p.fallSpeed;
                           if (currentY <= 0.0F) {
                              currentY = 0.0F;
                              pAlpha *= 0.6F;
                           }
                        }

                        if (!(pAlpha <= 0.05F)) {
                           if (buffer == null) {
                              buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
                           }

                           matrices.method_22903();
                           matrices.method_46416(p.startX, currentY, p.startZ);
                           matrices.method_22907(cameraRot);
                           float pScale = 0.18F;
                           matrices.method_22905(pScale, pScale, pScale);
                           Matrix4f pMatrix = matrices.method_23760().method_23761();
                           buffer.method_22918(pMatrix, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, pAlpha);
                           buffer.method_22918(pMatrix, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, pAlpha);
                           buffer.method_22918(pMatrix, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, pAlpha);
                           buffer.method_22918(pMatrix, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, pAlpha);
                           matrices.method_22909();
                        }
                     }

                     if (buffer != null) {
                        class_286.method_43433(buffer.method_60800());
                     }
                  }

                  matrices.method_22909();
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

   private void drawBatchedGlowingDot(Matrix4f matrix, class_287 buffer, float r, float g, float b, float alpha) {
      for (int i = 0; i < 360; i += 30) {
         double rad1 = Math.toRadians(i);
         double rad2 = Math.toRadians(i + 30);
         float px1 = (float)Math.cos(rad1);
         float py1 = (float)Math.sin(rad1);
         float px2 = (float)Math.cos(rad2);
         float py2 = (float)Math.sin(rad2);
         buffer.method_22918(matrix, 0.0F, 0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, alpha);
         buffer.method_22918(matrix, px1, py1, 0.0F).method_22915(r, g, b, 0.0F);
         buffer.method_22918(matrix, px2, py2, 0.0F).method_22915(r, g, b, 0.0F);
      }
   }
}
