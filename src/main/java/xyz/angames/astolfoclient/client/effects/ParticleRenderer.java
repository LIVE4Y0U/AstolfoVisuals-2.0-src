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
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.modules.render.ParticlesModule;

@Environment(EnvType.CLIENT)
public class ParticleRenderer {
   private static final class_2960 BUBBLES_TEX = class_2960.method_60655("astolfoclient", "textures/effects/bubbles.png");
   private static final class_2960 STARS_TEX = class_2960.method_60655("astolfoclient", "textures/effects/stars.png");
   private static final class_2960 DOLLARS_TEX = class_2960.method_60655("astolfoclient", "textures/effects/dollars.png");
   private static final class_2960 HEART_TEX = class_2960.method_60655("astolfoclient", "textures/effects/bloom.png");
   private static final class_2960 SMOLESTAR_TEX = class_2960.method_60655("astolfoclient", "textures/effects/mini-star.png");
   private final ParticleManager manager;

   public ParticleRenderer(ParticleManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context) {
      ParticlesModule module = (ParticlesModule)AstolfoclientClient.moduleManager.getModuleByName("Particles");
      if (module != null && module.isEnabled()) {
         List<Particle> allParticles = this.manager.getParticles();
         if (!allParticles.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            float tickDelta = context.tickCounter().method_60637(true);
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
            RenderSystem.setShader(class_10142.field_53880);
            class_289 tessellator = class_289.method_1348();

            for (ParticlesModule.ParticleType type : ParticlesModule.ParticleType.values()) {
               boolean bound = false;
               class_287 buffer = null;

               for (Particle particle : allParticles) {
                  if (particle.type == type) {
                     long age = currentTime - particle.creationTime;
                     if (age <= particle.lifespan) {
                        if (!bound) {
                           RenderSystem.setShader(class_10142.field_53880);
                           RenderSystem.setShaderTexture(0, this.getTextureForType(type));
                           buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
                           bound = true;
                        }

                        float progress = (float)age / (float)particle.lifespan;
                        float alpha = progress > 0.6F ? 1.0F - (progress - 0.6F) / 0.4F : 1.0F;
                        Color particleColor = particle.color != null ? particle.color : new Color(ThemeManager.getThemedColor(particle.creationTime));
                        float r = particleColor.getRed() / 255.0F;
                        float g = particleColor.getGreen() / 255.0F;
                        float b = particleColor.getBlue() / 255.0F;
                        double x = class_3532.method_16436(tickDelta, particle.prevPosition.field_1352, particle.position.field_1352);
                        double y = class_3532.method_16436(tickDelta, particle.prevPosition.field_1351, particle.position.field_1351);
                        double z = class_3532.method_16436(tickDelta, particle.prevPosition.field_1350, particle.position.field_1350);
                        class_4587 matrixStack = context.matrixStack();
                        matrixStack.method_22903();
                        matrixStack.method_22904(
                           x - context.camera().method_19326().field_1352,
                           y - context.camera().method_19326().field_1351,
                           z - context.camera().method_19326().field_1350
                        );
                        matrixStack.method_22907(context.camera().method_23767());
                        float finalScale = particle.scale / 2.5F;
                        matrixStack.method_22905(finalScale, finalScale, finalScale);
                        matrixStack.method_22907(class_7833.field_40718.rotationDegrees(particle.rotation + (float)age * 0.1F));
                        Matrix4f matrix = matrixStack.method_23760().method_23761();
                        buffer.method_22918(matrix, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 1.0F).method_22915(r, g, b, alpha);
                        buffer.method_22918(matrix, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 1.0F).method_22915(r, g, b, alpha);
                        buffer.method_22918(matrix, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 0.0F).method_22915(r, g, b, alpha);
                        buffer.method_22918(matrix, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 0.0F).method_22915(r, g, b, alpha);
                        matrixStack.method_22909();
                     }
                  }
               }

               if (bound && buffer != null) {
                  class_286.method_43433(buffer.method_60800());
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

   private class_2960 getTextureForType(ParticlesModule.ParticleType type) {
      switch (type) {
         case BUBBLES:
            return BUBBLES_TEX;
         case DOLLARS:
            return DOLLARS_TEX;
         case HEART:
            return HEART_TEX;
         case SMOLESTAR:
            return SMOLESTAR_TEX;
         case STARS:
         default:
            return STARS_TEX;
      }
   }
}
