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
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.util.TargetUtils;

@Environment(EnvType.CLIENT)
public class CircleEspGalaxyRenderer {
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655("astolfoclient", "textures/effects/bloom.png");
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
            RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
            class_289 tessellator = class_289.method_1348();
            long currentTime = System.currentTimeMillis();
            int rgb = ThemeManager.getThemedColor(0L);
            Color c = new Color(rgb);
            float r = c.getRed() / 255.0F;
            float g = c.getGreen() / 255.0F;
            float b = c.getBlue() / 255.0F;

            for (CircleEspManager.CircleEspEffect effect : validEffects) {
               class_1297 target = effect.target;
               if (target.method_5805() && !TargetUtils.isInvisible(target)) {
                  long timeSinceHit = currentTime - effect.lastHitTime;
                  float fadeProgress = (float)timeSinceHit / 450.0F;
                  float alpha = 1.0F - Math.max(0.0F, (fadeProgress - 0.5F) * 2.0F);
                  if (!(alpha <= 0.05F)) {
                     float animTime = (float)(currentTime - effect.startTime) / 1000.0F;
                     float tickDelta = context.tickCounter().method_60637(true);
                     double tX = target.field_6038 + (target.method_23317() - target.field_6038) * tickDelta;
                     double tY = target.field_5971 + (target.method_23318() - target.field_5971) * tickDelta;
                     double tZ = target.field_5989 + (target.method_23321() - target.field_5989) * tickDelta;
                     float height = target.method_17682();
                     float radius = target.method_17681() * 0.8F;
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
                     class_4587 matrices = context.matrixStack();
                     matrices.method_22903();
                     matrices.method_22904(
                        tX - context.camera().method_19326().field_1352,
                        tY - context.camera().method_19326().field_1351,
                        tZ - context.camera().method_19326().field_1350
                     );
                     matrices.method_22907(class_7833.field_40716.rotationDegrees(animTime * 45.0F));
                     RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
                     RenderSystem.setShader(class_10142.field_53880);
                     class_287 bbBloom = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
                     this.drawHorizontalBloom(matrices, bbBloom, scanY, radius * 2.5F, r, g, b, alpha * 0.5F);
                     class_286.method_43433(bbBloom.method_60800());
                     RenderSystem.setShader(class_10142.field_53876);
                     class_287 bbTail = tessellator.method_60827(class_5596.field_27382, class_290.field_1576);
                     this.drawGradientCylinder(matrices, bbTail, radius, scanY, scanY + tailOffset, r, g, b, alpha * 0.45F, 0.0F);
                     class_286.method_43433(bbTail.method_60800());
                     RenderSystem.lineWidth(2.5F);
                     class_287 bbRing = tessellator.method_60827(class_5596.field_29345, class_290.field_1576);
                     this.drawCrispRing(matrices, bbRing, radius, scanY, r, g, b, alpha * 0.9F);
                     class_286.method_43433(bbRing.method_60800());
                     RenderSystem.lineWidth(1.0F);
                     matrices.method_22909();
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

   private void drawHorizontalBloom(class_4587 stack, class_287 buffer, float y, float size, float r, float g, float b, float a) {
      Matrix4f m = stack.method_23760().method_23761();
      buffer.method_22918(m, -size, y, -size).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, -size, y, size).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, size, y, size).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, size, y, -size).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
   }

   private void drawGradientCylinder(
      class_4587 stack, class_287 buffer, float radius, float yStart, float yEnd, float r, float g, float b, float aStart, float aEnd
   ) {
      Matrix4f m = stack.method_23760().method_23761();
      int segments = 40;

      for (int i = 0; i < segments; i++) {
         float angle1 = (float)(i * Math.PI * 2.0 / segments);
         float angle2 = (float)((i + 1) * Math.PI * 2.0 / segments);
         float x1 = (float)Math.cos(angle1) * radius;
         float z1 = (float)Math.sin(angle1) * radius;
         float x2 = (float)Math.cos(angle2) * radius;
         float z2 = (float)Math.sin(angle2) * radius;
         buffer.method_22918(m, x1, yStart, z1).method_22915(r, g, b, aStart);
         buffer.method_22918(m, x1, yEnd, z1).method_22915(r, g, b, aEnd);
         buffer.method_22918(m, x2, yEnd, z2).method_22915(r, g, b, aEnd);
         buffer.method_22918(m, x2, yStart, z2).method_22915(r, g, b, aStart);
      }
   }

   private void drawCrispRing(class_4587 stack, class_287 buffer, float radius, float y, float r, float g, float b, float a) {
      Matrix4f m = stack.method_23760().method_23761();
      int segments = 40;

      for (int i = 0; i <= segments; i++) {
         float angle = (float)(i * Math.PI * 2.0 / segments);
         float x = (float)Math.cos(angle) * radius;
         float z = (float)Math.sin(angle) * radius;
         buffer.method_22918(m, x, y, z).method_22915(r, g, b, a);
      }
   }
}
