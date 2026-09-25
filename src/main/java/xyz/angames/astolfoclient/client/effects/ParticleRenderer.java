package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.modules.render.ParticlesModule;

@Environment(EnvType.CLIENT)
public class ParticleRenderer {
   private static final Identifier BUBBLES_TEX = Identifier.of("astolfoclient", "textures/effects/bubbles.png");
   private static final Identifier STARS_TEX = Identifier.of("astolfoclient", "textures/effects/stars.png");
   private static final Identifier DOLLARS_TEX = Identifier.of("astolfoclient", "textures/effects/dollars.png");
   private static final Identifier HEART_TEX = Identifier.of("astolfoclient", "textures/effects/bloom.png");
   private static final Identifier SMOLESTAR_TEX = Identifier.of("astolfoclient", "textures/effects/mini-star.png");
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
            float tickDelta = context.tickCounter().getTickDelta(true);
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            Tessellator tessellator = Tessellator.getInstance();

            for (ParticlesModule.ParticleType type : ParticlesModule.ParticleType.values()) {
               boolean bound = false;
               BufferBuilder buffer = null;

               for (Particle particle : allParticles) {
                  if (particle.type == type) {
                     long age = currentTime - particle.creationTime;
                     if (age <= particle.lifespan) {
                        if (!bound) {
                           RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                           RenderSystem.setShaderTexture(0, this.getTextureForType(type));
                           buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                           bound = true;
                        }

                        float progress = (float)age / (float)particle.lifespan;
                        float alpha = progress > 0.6F ? 1.0F - (progress - 0.6F) / 0.4F : 1.0F;
                        Color particleColor = particle.color != null ? particle.color : new Color(ThemeManager.getThemedColor(particle.creationTime));
                        float r = particleColor.getRed() / 255.0F;
                        float g = particleColor.getGreen() / 255.0F;
                        float b = particleColor.getBlue() / 255.0F;
                        double x = MathHelper.lerp(tickDelta, particle.prevPosition.x, particle.position.x);
                        double y = MathHelper.lerp(tickDelta, particle.prevPosition.y, particle.position.y);
                        double z = MathHelper.lerp(tickDelta, particle.prevPosition.z, particle.position.z);
                        MatrixStack matrixStack = context.matrixStack();
                        matrixStack.push();
                        matrixStack.translate(
                           x - context.camera().getPos().x,
                           y - context.camera().getPos().y,
                           z - context.camera().getPos().z
                        );
                        matrixStack.multiply(context.camera().getRotation());
                        float finalScale = particle.scale / 2.5F;
                        matrixStack.scale(finalScale, finalScale, finalScale);
                        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(particle.rotation + (float)age * 0.1F));
                        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
                        buffer.vertex(matrix, -0.5F, -0.5F, 0.0F).texture(0.0F, 1.0F).color(r, g, b, alpha);
                        buffer.vertex(matrix, 0.5F, -0.5F, 0.0F).texture(1.0F, 1.0F).color(r, g, b, alpha);
                        buffer.vertex(matrix, 0.5F, 0.5F, 0.0F).texture(1.0F, 0.0F).color(r, g, b, alpha);
                        buffer.vertex(matrix, -0.5F, 0.5F, 0.0F).texture(0.0F, 0.0F).color(r, g, b, alpha);
                        matrixStack.pop();
                     }
                  }
               }

               if (bound && buffer != null) {
                  BufferRenderer.drawWithGlobalProgram(buffer.end());
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

   private Identifier getTextureForType(ParticlesModule.ParticleType type) {
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
