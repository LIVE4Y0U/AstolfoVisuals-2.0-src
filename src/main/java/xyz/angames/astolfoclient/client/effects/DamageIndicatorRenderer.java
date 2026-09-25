package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider;
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
            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer textRenderer = client.textRenderer;
            Camera camera = context.camera();
            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
            double camX = camera.getPos().x;
            double camY = camera.getPos().y;
            double camZ = camera.getPos().z;
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
               matrices.push();
               matrices.translate(p.x - camX, p.y - camY, p.z - camZ);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
               float currentScale = baseScale * (p.isCrit ? 1.3F : 1.0F) * popScale;
               matrices.scale(-currentScale, -currentScale, currentScale);
               Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
               Text text = Text.literal(p.text);
               float xOffset = -textRenderer.getWidth(text) / 2.0F;
               textRenderer.draw(text, xOffset, 0.0F, color, true, positionMatrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
               matrices.pop();
            }

            vertexConsumers.draw();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
         }
      }
   }
}
