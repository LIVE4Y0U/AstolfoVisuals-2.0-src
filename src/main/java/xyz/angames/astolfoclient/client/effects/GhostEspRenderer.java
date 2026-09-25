package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.TargetEspModule;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class GhostEspRenderer {
   private final GhostEspManager manager;

   public GhostEspRenderer(GhostEspManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context) {
      Module targetEspModule = AstolfoclientClient.moduleManager.getModuleByName("TargetESP");
      if (targetEspModule != null && targetEspModule.isEnabled()) {
         if (!(targetEspModule instanceof TargetEspModule tem && !tem.mode.is("Ghost"))) {
            Map<class_1297, GhostEspEffect> allEffects = this.manager.getEffects();
            if (!allEffects.isEmpty()) {
               List<GhostEspEffect> validEffects = new ArrayList<>();
               long currentTime = System.currentTimeMillis();

               for (GhostEspEffect effect : allEffects.values()) {
                  if (effect.target != null && effect.target.method_5805() && !TargetUtils.isInvisible(effect.target)) {
                     long age = currentTime - effect.lastHitTime;
                     if (age <= 450L) {
                        validEffects.add(effect);
                     }
                  }
               }

               if (!validEffects.isEmpty()) {
                  RenderSystem.enableBlend();
                  RenderSystem.disableCull();
                  RenderSystem.enableDepthTest();
                  RenderSystem.depthFunc(515);
                  RenderSystem.depthMask(false);
                  RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
                  RenderSystem.setShader(class_10142.field_53876);
                  class_289 tessellator = class_289.method_1348();
                  double safeTime = currentTime % 1000000L;
                  float speed = 0.006F;
                  int trailLength = 22;
                  float baseDistanceMultiplier = 1.05F;
                  double spacingDegrees = 120.0;
                  float trailSegmentSpacing = 10.0F;

                  for (GhostEspEffect effect : validEffects) {
                     class_1297 target = effect.target;
                     if (target != null && target.method_5805() && !TargetUtils.isInvisible(target)) {
                        long age = currentTime - effect.lastHitTime;
                        float lifeProgress = (float)age / 450.0F;
                        float baseAlpha = 1.0F - Math.max(0.0F, (lifeProgress - 0.5F) * 2.0F);
                        if (!(baseAlpha <= 0.05F)) {
                           int rgb = ThemeManager.getThemedColor(0L);
                           float themeR = (rgb >> 16 & 0xFF) / 255.0F;
                           float themeG = (rgb >> 8 & 0xFF) / 255.0F;
                           float themeB = (rgb & 0xFF) / 255.0F;
                           long ageSinceAttack = currentTime - effect.lastAttackTime;
                           float hitColorFactor = Math.max(0.0F, 1.0F - (float)ageSinceAttack / 350.0F);
                           float r = hitColorFactor * 1.0F + (1.0F - hitColorFactor) * themeR;
                           float g = hitColorFactor * 0.0F + (1.0F - hitColorFactor) * themeG;
                           float b = hitColorFactor * 0.0F + (1.0F - hitColorFactor) * themeB;
                           float cr = 1.0F;
                           float cg = 1.0F - hitColorFactor;
                           float cb = 1.0F - hitColorFactor;
                           float baseRadius = target.method_17681() * baseDistanceMultiplier;
                           if (age > 400L) {
                              float endProgress = (float)(age - 400L) / 200.0F;
                              baseRadius *= Math.max(0.0F, 1.0F - endProgress);
                           }

                           float tickDelta = context.tickCounter().method_60637(true);
                           double tX = target.field_6038 + (target.method_23317() - target.field_6038) * tickDelta;
                           double tY = target.field_5971 + (target.method_23318() - target.field_5971) * tickDelta;
                           double tZ = target.field_5989 + (target.method_23321() - target.field_5989) * tickDelta;
                           double camX = context.camera().method_19326().field_1352;
                           double camY = context.camera().method_19326().field_1351;
                           double camZ = context.camera().method_19326().field_1350;

                           for (int j = 0; j < 3; j++) {
                              float breathing = (float)Math.sin(safeTime * 0.002 + j * 1.5);
                              float currentRadius = baseRadius + breathing * 0.2F;

                              for (int t = 0; t < trailLength; t++) {
                                 float decay = 1.0F / trailLength;
                                 float scale = (1.0F - t * decay) * 0.2F;
                                 float alpha = baseAlpha * (1.0F - t * decay);
                                 if (!(scale <= 0.01F) && !(alpha <= 0.02F)) {
                                    float timeOffset = t * trailSegmentSpacing;
                                    float histSafeTime = (float)(safeTime - timeOffset);
                                    float angle = histSafeTime * speed + (float)(j * Math.toRadians(spacingDegrees));
                                    float localX = (float)Math.cos(angle) * currentRadius;
                                    float localY = 0.0F;
                                    float localZ = (float)Math.sin(angle) * currentRadius;
                                    float histAnimTime = histSafeTime / 1000.0F;
                                    float scanSpeed = 2.0F;
                                    float phase = (float)Math.sin(histAnimTime * scanSpeed);
                                    float scanY = (phase + 1.0F) / 2.0F * target.method_17682();
                                    class_4587 matrices = context.matrixStack();
                                    matrices.method_22903();
                                    matrices.method_22904(tX + localX - camX, tY + scanY + localY - camY, tZ + localZ - camZ);
                                    matrices.method_22907(context.camera().method_23767());
                                    matrices.method_22905(scale, scale, scale);
                                    class_287 buffer = tessellator.method_60827(class_5596.field_27381, class_290.field_1576);
                                    this.drawGlowingDot(matrices.method_23760().method_23761(), buffer, r, g, b, cr, cg, cb, alpha);
                                    class_286.method_43433(buffer.method_60800());
                                    matrices.method_22909();
                                 }
                              }
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

   private void drawGlowingDot(Matrix4f matrix, class_287 buffer, float r, float g, float b, float cr, float cg, float cb, float alpha) {
      buffer.method_22918(matrix, 0.0F, 0.0F, 0.0F).method_22915(cr, cg, cb, alpha);

      for (int i = 0; i <= 360; i += 20) {
         double rad = Math.toRadians(i);
         float px = (float)Math.cos(rad);
         float py = (float)Math.sin(rad);
         buffer.method_22918(matrix, px, py, 0.0F).method_22915(r, g, b, 0.0F);
      }
   }
}
