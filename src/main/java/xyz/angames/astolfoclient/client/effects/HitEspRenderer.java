package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
import xyz.angames.astolfoclient.client.module.modules.HitEspModule;

@Environment(EnvType.CLIENT)
public class HitEspRenderer {
   private static final Identifier TEXTURE = Identifier.of("astolfoclient", "textures/effects/hit_effect.png");
   private final HitEspManager manager;

   public HitEspRenderer(HitEspManager manager) {
      this.manager = manager;
   }

   public void render(WorldRenderContext context) {
      HitEspModule hitEspModule = (HitEspModule)AstolfoclientClient.moduleManager.getModuleByName("HitESP");
      if (hitEspModule != null && hitEspModule.isEnabled()) {
         List<HitEspEffect> validEffects = this.manager.getEffects();
         if (!validEffects.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            long currentTime = System.currentTimeMillis();
            double tickDelta = context.tickCounter().getTickDelta(true);
            int rgb = ThemeManager.getThemedColor(0L);
            float r = (rgb >> 16 & 0xFF) / 255.0F;
            float g = (rgb >> 8 & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;
            float a = 1.0F;
            float baseScale = hitEspModule.size.getFloat();
            float rotSpeed = hitEspModule.rotationSpeed.getFloat();
            float spawnAnimDuration = hitEspModule.spawnAnimDuration.getFloat();
            float leaveTime = hitEspModule.leaveTime.getFloat();
            float fadeTime = hitEspModule.fadeTime.getFloat();
            boolean doExplosion = hitEspModule.doExplosion.get();
            boolean drewAnything = false;

            for (HitEspEffect effect : validEffects) {
               MatrixStack matrixStack = context.matrixStack();
               long age = currentTime - effect.creationTime;
               if (!effect.isShattered) {
                  float textureRotation = (float)age * rotSpeed * effect.rotationDirection;
                  float scale = baseScale;
                  float alpha = 1.0F;
                  if ((float)age < spawnAnimDuration) {
                     float p = (float)age / spawnAnimDuration;
                     float ease = (float)(Math.sin(-20.420352248333657 * (p + 1.0)) * Math.pow(2.0, -10.0 * p) + 1.0);
                     scale = baseScale * ease;
                     alpha = Math.min(1.0F, p * 2.5F);
                  }

                  if (!doExplosion && (float)age >= leaveTime) {
                     float fadeProgress = ((float)age - leaveTime) / fadeTime;
                     alpha = Math.max(0.0F, 1.0F - fadeProgress);
                  }

                  alpha *= a;
                  if (alpha > 0.01F) {
                     matrixStack.push();
                     matrixStack.translate(
                        effect.position.x - context.camera().getPos().x,
                        effect.position.y - context.camera().getPos().y,
                        effect.position.z - context.camera().getPos().z
                     );
                     matrixStack.multiply(effect.orientation);
                     matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(textureRotation));
                     matrixStack.scale(scale, scale, scale);
                     Matrix4f matrix = matrixStack.peek().getPositionMatrix();
                     buffer.vertex(matrix, -0.5F, -0.5F, 0.0F).texture(0.0F, 1.0F).color(r, g, b, alpha);
                     buffer.vertex(matrix, 0.5F, -0.5F, 0.0F).texture(1.0F, 1.0F).color(r, g, b, alpha);
                     buffer.vertex(matrix, 0.5F, 0.5F, 0.0F).texture(1.0F, 0.0F).color(r, g, b, alpha);
                     buffer.vertex(matrix, -0.5F, 0.5F, 0.0F).texture(0.0F, 0.0F).color(r, g, b, alpha);
                     matrixStack.pop();
                     drewAnything = true;
                  }
               } else {
                  float groundLifespan = hitEspModule.groundLifespan.getFloat();
                  boolean shrinkOnGround = hitEspModule.shrinkOnGround.get();
                  float gridSize = hitEspModule.gridSize.getFloat();
                  float baseShardScale = baseScale / gridSize * 0.93F;

                  for (HitEspEffect.Shard shard : effect.shards) {
                     float alpha = 1.0F;
                     float shardScale = baseShardScale;
                     if (shard.onGround) {
                        long timeOnGround = currentTime - shard.groundHitTime;
                        float deathProgress = (float)timeOnGround / groundLifespan;
                        alpha = 1.0F - deathProgress;
                        if (alpha < 0.0F) {
                           alpha = 0.0F;
                        }

                        if (shrinkOnGround) {
                           float shrinkEase = 1.0F - (float)Math.pow(deathProgress, 2.0);
                           shardScale *= Math.max(0.0F, shrinkEase);
                        }
                     }

                     alpha *= a;
                     if (alpha > 0.01F && shardScale > 0.01F) {
                        double renderX = MathHelper.lerp(tickDelta, shard.prevPos.x, shard.pos.x);
                        double renderY = MathHelper.lerp(tickDelta, shard.prevPos.y, shard.pos.y);
                        double renderZ = MathHelper.lerp(tickDelta, shard.prevPos.z, shard.pos.z);
                        float renderRotX = (float)MathHelper.lerp(tickDelta, shard.prevRotX, shard.rotX);
                        float renderRotY = (float)MathHelper.lerp(tickDelta, shard.prevRotY, shard.rotY);
                        float renderRotZ = (float)MathHelper.lerp(tickDelta, shard.prevRotZ, shard.rotZ);
                        matrixStack.push();
                        matrixStack.translate(
                           renderX - context.camera().getPos().x,
                           renderY - context.camera().getPos().y,
                           renderZ - context.camera().getPos().z
                        );
                        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(renderRotX));
                        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(renderRotY));
                        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(renderRotZ));
                        matrixStack.scale(shardScale, shardScale, shardScale);
                        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
                        buffer.vertex(matrix, -0.5F, -0.5F, 0.0F).texture(shard.u1, shard.v2).color(r, g, b, alpha);
                        buffer.vertex(matrix, 0.5F, -0.5F, 0.0F).texture(shard.u2, shard.v2).color(r, g, b, alpha);
                        buffer.vertex(matrix, 0.5F, 0.5F, 0.0F).texture(shard.u2, shard.v1).color(r, g, b, alpha);
                        buffer.vertex(matrix, -0.5F, 0.5F, 0.0F).texture(shard.u1, shard.v1).color(r, g, b, alpha);
                        matrixStack.pop();
                        drewAnything = true;
                     }
                  }
               }
            }

            if (!drewAnything) {
               Matrix4f dummy = context.matrixStack().peek().getPositionMatrix();
               buffer.vertex(dummy, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(0, 0, 0, 0);
               buffer.vertex(dummy, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(0, 0, 0, 0);
               buffer.vertex(dummy, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(0, 0, 0, 0);
               buffer.vertex(dummy, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(0, 0, 0, 0);
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }
}
