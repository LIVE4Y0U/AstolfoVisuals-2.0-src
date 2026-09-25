package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_327.class_6415;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.modules.render.DamageIndicatorModule;

@Environment(EnvType.CLIENT)
public class DamageIndicatorRenderer {
   private final DamageIndicatorManager manager;

   public DamageIndicatorRenderer(DamageIndicatorManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context) {
      DamageIndicatorModule module = (DamageIndicatorModule)AstolfoclientClient.moduleManager.getModuleByName("DamageIndicators");
      if (module != null && module.isEnabled()) {
         if (!this.manager.getParticles().isEmpty()) {
            class_310 client = class_310.method_1551();
            class_327 textRenderer = client.field_1772;
            class_4184 camera = context.camera();
            class_4587 matrices = context.matrixStack();
            class_4598 vertexConsumers = client.method_22940().method_23000();
            double camX = camera.method_19326().field_1352;
            double camY = camera.method_19326().field_1351;
            double camZ = camera.method_19326().field_1350;
            float baseScale = (float)module.scale.get() * 0.02F;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

            for (DamageIndicatorManager.DamageParticle p : this.manager.getParticles()) {
               float alpha = 1.0F;
               int fadeTime = 12;
               if (p.maxAge - p.age <= fadeTime) {
                  float fadeProgress = (float)(p.maxAge - p.age) / fadeTime;
                  alpha = fadeProgress * fadeProgress * (3.0F - 2.0F * fadeProgress);
               }

               float popScale = 1.0F;
               float popDuration = 6.0F;
               if (p.age < popDuration) {
                  float t = p.age / popDuration;
                  float c1 = 1.70158F;
                  float c3 = c1 + 1.0F;
                  popScale = 1.0F + c3 * (float)Math.pow(t - 1.0F, 3.0) + c1 * (float)Math.pow(t - 1.0F, 2.0);
                  if (popScale < 0.0F) {
                     popScale = 0.0F;
                  }
               }

               int color = p.isCrit ? -65536 : -22016;
               int alphaHex = (int)(alpha * 255.0F) << 24;
               color = color & 16777215 | alphaHex;
               matrices.method_22903();
               matrices.method_22904(p.x - camX, p.y - camY, p.z - camZ);
               matrices.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
               matrices.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
               float currentScale = baseScale * (p.isCrit ? 1.3F : 1.0F) * popScale;
               matrices.method_22905(-currentScale, -currentScale, currentScale);
               Matrix4f positionMatrix = matrices.method_23760().method_23761();
               class_2561 text = class_2561.method_43470(p.text);
               float xOffset = -textRenderer.method_27525(text) / 2.0F;
               textRenderer.method_27522(text, xOffset, 0.0F, color, true, positionMatrix, vertexConsumers, class_6415.field_33993, 0, 15728880);
               matrices.method_22909();
            }

            vertexConsumers.method_22993();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
         }
      }
   }
}
