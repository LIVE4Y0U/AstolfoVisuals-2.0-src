package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_10142;
import net.minecraft.class_1657;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3883;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_5944;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
public class ChinaHatFeatureRenderer extends class_3887<class_10055, class_591> {
   public static class_1657 currentlyRenderingPlayer;
   private static final float RADIUS = 0.65F;
   private static final float HEIGHT = 0.28F;
   private static final int SEGMENTS = 40;
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/dashtrail/dashbloom.png");
   private float spinAngle = 0.0F;
   private long lastRenderTime = 0L;

   public ChinaHatFeatureRenderer(class_3883<class_10055, class_591> context) {
      super(context);
   }

   public void render(class_4587 matrices, class_4597 vertexConsumers, int light, class_10055 state, float limbAngle, float limbDistance) {
      class_310 mc = class_310.method_1551();
      Module module = AstolfoclientClient.moduleManager.getModuleByName("ChinaHat");
      if (module != null && module.isEnabled() && mc.field_1724 != null) {
         if (currentlyRenderingPlayer != null && currentlyRenderingPlayer.method_5628() == mc.field_1724.method_5628()) {
            if (!state.field_53333) {
               long now = System.currentTimeMillis();
               if (this.lastRenderTime != 0L) {
                  this.spinAngle = this.spinAngle + (float)(now - this.lastRenderTime) / 1000.0F * 80.0F;
                  if (this.spinAngle > 360.0F) {
                     this.spinAngle -= 360.0F;
                  }
               }

               this.lastRenderTime = now;
               matrices.method_22903();
               ((class_591)this.method_17165()).field_3398.method_22703(matrices);
               matrices.method_46416(0.0F, -0.4F, 0.0F);
               matrices.method_22905(1.0F, -1.0F, 1.0F);
               matrices.method_22907(class_7833.field_40716.rotationDegrees(this.spinAngle));
               Matrix4f matrix = matrices.method_23760().method_23761();
               class_289 tessellator = class_289.method_1348();
               if (vertexConsumers instanceof class_4598 immediate) {
                  immediate.method_22993();
               }

               RenderSystem.enableBlend();
               RenderSystem.disableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(true);
               RenderSystem.blendFuncSeparate(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA, class_4535.ONE, class_4534.ZERO);
               class_5944 shader = RenderSystem.setShader(AstolfoclientClient.CHINA_HAT_SHADER);
               if (shader != null) {
                  float timeSecs = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
                  if (shader.method_34582("uTime") != null) {
                     shader.method_34582("uTime").method_1251(timeSecs);
                  }

                  if (shader.method_34582("uResolution") != null) {
                     shader.method_34582("uResolution").method_1255(1.0F, 1.0F);
                  }

                  int themeRgb = ThemeManager.getThemedColor((int)(now / 10L));
                  float tr = (themeRgb >> 16 & 0xFF) / 255.0F;
                  float tg = (themeRgb >> 8 & 0xFF) / 255.0F;
                  float tb = (themeRgb & 0xFF) / 255.0F;
                  if (shader.method_34582("uThemeColor") != null) {
                     shader.method_34582("uThemeColor").method_1249(tr, tg, tb);
                  }
               }

               Color cInner = new Color(ThemeManager.getThemedColor((int)(now / 10L)));
               Color cOuter = new Color(ThemeManager.getThemedColor((int)(now / 10L) + 60));
               int optimizedLayers = 16;
               class_287 bufBody = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);

               for (int layer = 1; layer < optimizedLayers; layer++) {
                  float t0 = (float)(layer - 1) / (optimizedLayers - 1);
                  float t1 = (float)layer / (optimizedLayers - 1);
                  float r0 = 0.65F * t0;
                  float h0 = 0.28F * (1.0F - t0);
                  float r1 = 0.65F * t1;
                  float h1 = 0.28F * (1.0F - t1);
                  Color c0 = this.lerpColor(cInner, cOuter, this.smoothstep(t0));
                  Color c1 = this.lerpColor(cInner, cOuter, this.smoothstep(t1));
                  float a0 = 1.0F;
                  float a1 = 1.0F;

                  for (int i = 0; i < 40; i++) {
                     double ang0 = (Math.PI * 2) * i / 40.0;
                     double ang1 = (Math.PI * 2) * (i + 1) / 40.0;
                     float st = i / 40.0F;
                     Color sc0 = this.lerpColor(c0, cOuter, st * 0.25F);
                     Color sc1 = this.lerpColor(c1, cOuter, st * 0.25F);
                     float u0_0 = (float)Math.cos(ang0) * t0 * 0.5F + 0.5F;
                     float v0_0 = (float)Math.sin(ang0) * t0 * 0.5F + 0.5F;
                     float u1_0 = (float)Math.cos(ang1) * t0 * 0.5F + 0.5F;
                     float v1_0 = (float)Math.sin(ang1) * t0 * 0.5F + 0.5F;
                     float u1_1 = (float)Math.cos(ang1) * t1 * 0.5F + 0.5F;
                     float v1_1 = (float)Math.sin(ang1) * t1 * 0.5F + 0.5F;
                     float u0_1 = (float)Math.cos(ang0) * t1 * 0.5F + 0.5F;
                     float v0_1 = (float)Math.sin(ang0) * t1 * 0.5F + 0.5F;
                     bufBody.method_22918(matrix, (float)Math.cos(ang0) * r0, h0, (float)Math.sin(ang0) * r0)
                        .method_22913(u0_0, v0_0)
                        .method_22915(sc0.getRed() / 255.0F, sc0.getGreen() / 255.0F, sc0.getBlue() / 255.0F, a0);
                     bufBody.method_22918(matrix, (float)Math.cos(ang1) * r0, h0, (float)Math.sin(ang1) * r0)
                        .method_22913(u1_0, v1_0)
                        .method_22915(sc0.getRed() / 255.0F, sc0.getGreen() / 255.0F, sc0.getBlue() / 255.0F, a0);
                     bufBody.method_22918(matrix, (float)Math.cos(ang1) * r1, h1, (float)Math.sin(ang1) * r1)
                        .method_22913(u1_1, v1_1)
                        .method_22915(sc1.getRed() / 255.0F, sc1.getGreen() / 255.0F, sc1.getBlue() / 255.0F, a1);
                     bufBody.method_22918(matrix, (float)Math.cos(ang0) * r1, h1, (float)Math.sin(ang0) * r1)
                        .method_22913(u0_1, v0_1)
                        .method_22915(sc1.getRed() / 255.0F, sc1.getGreen() / 255.0F, sc1.getBlue() / 255.0F, a1);
                  }
               }

               class_286.method_43433(bufBody.method_60800());
               float r = cOuter.getRed() / 255.0F;
               float g = cOuter.getGreen() / 255.0F;
               float b = cOuter.getBlue() / 255.0F;
               float rimW = 0.035F;
               float innerR = 0.65F - rimW;
               float innerH = 0.28F * (rimW / 0.65F);
               float innerT = (0.65F - rimW) / 0.65F;
               class_287 bufRim = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);

               for (int i = 0; i < 40; i++) {
                  double a0 = (Math.PI * 2) * i / 40.0;
                  double a1 = (Math.PI * 2) * (i + 1) / 40.0;
                  float u0_in = (float)Math.cos(a0) * innerT * 0.5F + 0.5F;
                  float v0_in = (float)Math.sin(a0) * innerT * 0.5F + 0.5F;
                  float u1_in = (float)Math.cos(a1) * innerT * 0.5F + 0.5F;
                  float v1_in = (float)Math.sin(a1) * innerT * 0.5F + 0.5F;
                  float u1_out = (float)Math.cos(a1) * 0.5F + 0.5F;
                  float v1_out = (float)Math.sin(a1) * 0.5F + 0.5F;
                  float u0_out = (float)Math.cos(a0) * 0.5F + 0.5F;
                  float v0_out = (float)Math.sin(a0) * 0.5F + 0.5F;
                  bufRim.method_22918(matrix, (float)Math.cos(a0) * innerR, innerH, (float)Math.sin(a0) * innerR)
                     .method_22913(u0_in, v0_in)
                     .method_22915(r, g, b, 1.0F);
                  bufRim.method_22918(matrix, (float)Math.cos(a1) * innerR, innerH, (float)Math.sin(a1) * innerR)
                     .method_22913(u1_in, v1_in)
                     .method_22915(r, g, b, 1.0F);
                  bufRim.method_22918(matrix, (float)Math.cos(a1) * 0.65F, 0.0F, (float)Math.sin(a1) * 0.65F)
                     .method_22913(u1_out, v1_out)
                     .method_22915(r, g, b, 1.0F);
                  bufRim.method_22918(matrix, (float)Math.cos(a0) * 0.65F, 0.0F, (float)Math.sin(a0) * 0.65F)
                     .method_22913(u0_out, v0_out)
                     .method_22915(r, g, b, 1.0F);
               }

               class_286.method_43433(bufRim.method_60800());
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
               RenderSystem.disableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               r = cOuter.getRed() / 255.0F;
               g = cOuter.getGreen() / 255.0F;
               b = cOuter.getBlue() / 255.0F;
               RenderSystem.setShader(class_10142.field_53876);
               class_287 glowRimBuf = tessellator.method_60827(class_5596.field_27382, class_290.field_1576);
               innerR = 0.598F;
               innerH = 0.68899995F;
               innerT = 0.7F;
               float outerAlpha = 0.0F;

               for (int i = 0; i < 40; i++) {
                  double a0 = (Math.PI * 2) * i / 40.0;
                  double a1 = (Math.PI * 2) * (i + 1) / 40.0;
                  float x0_in = (float)Math.cos(a0) * innerR;
                  float z0_in = (float)Math.sin(a0) * innerR;
                  float x1_in = (float)Math.cos(a1) * innerR;
                  float z1_in = (float)Math.sin(a1) * innerR;
                  float x0_out = (float)Math.cos(a0) * innerH;
                  float z0_out = (float)Math.sin(a0) * innerH;
                  float x1_out = (float)Math.cos(a1) * innerH;
                  float z1_out = (float)Math.sin(a1) * innerH;
                  glowRimBuf.method_22918(matrix, x0_in, 0.005F, z0_in).method_22915(r, g, b, innerT);
                  glowRimBuf.method_22918(matrix, x1_in, 0.005F, z1_in).method_22915(r, g, b, innerT);
                  glowRimBuf.method_22918(matrix, x1_out, 0.001F, z1_out).method_22915(r, g, b, outerAlpha);
                  glowRimBuf.method_22918(matrix, x0_out, 0.001F, z0_out).method_22915(r, g, b, outerAlpha);
               }

               class_286.method_43433(glowRimBuf.method_60800());
               RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
               RenderSystem.setShader(class_10142.field_53880);
               class_287 bloomBaseBuf = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
               float bSize = 0.663F;
               bloomBaseBuf.method_22918(matrix, -bSize, 0.005F, bSize).method_22913(0.0F, 1.0F).method_22915(r, g, b, 0.4F);
               bloomBaseBuf.method_22918(matrix, bSize, 0.005F, bSize).method_22913(1.0F, 1.0F).method_22915(r, g, b, 0.4F);
               bloomBaseBuf.method_22918(matrix, bSize, 0.005F, -bSize).method_22913(1.0F, 0.0F).method_22915(r, g, b, 0.4F);
               bloomBaseBuf.method_22918(matrix, -bSize, 0.005F, -bSize).method_22913(0.0F, 0.0F).method_22915(r, g, b, 0.4F);
               class_286.method_43433(bloomBaseBuf.method_60800());
               matrices.method_22909();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(true);
               RenderSystem.enableCull();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableBlend();
            }
         }
      }
   }

   private Color lerpColor(Color a, Color b, float t) {
      t = class_3532.method_15363(t, 0.0F, 1.0F);
      return new Color(
         (int)(a.getRed() + (b.getRed() - a.getRed()) * t),
         (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
         (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t)
      );
   }

   private float smoothstep(float t) {
      t = class_3532.method_15363(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }
}
