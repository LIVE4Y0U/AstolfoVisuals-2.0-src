package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.modules.render.BlockOutlineModule;

@Environment(EnvType.CLIENT)
public class BlockOutlineRenderer {
   private BlockOutlineRenderer.RenderBox currentBox = null;
   private BlockPos lastPos = null;
   private long lastRenderTime = System.currentTimeMillis();
   private float fadeAlpha = 0.0F;

   public void render(WorldRenderContext context) {
      BlockOutlineModule module = (BlockOutlineModule)AstolfoclientClient.moduleManager.getModuleByName("BlockOutline");
      if (module != null && module.isEnabled()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.world != null && mc.player != null) {
            long now = System.currentTimeMillis();
            float deltaTime = (float)(now - this.lastRenderTime) / 1000.0F;
            this.lastRenderTime = now;
            if (deltaTime > 0.1F) {
               deltaTime = 0.1F;
            }

            if (deltaTime < 0.001F) {
               deltaTime = 0.001F;
            }

            HitResult hit = mc.crosshairTarget;
            boolean hasBlockTarget = hit != null && hit.getType() == HitResult.Type.BLOCK;
            if (module.fadeEffect.get()) {
               float fadeRate = (float)module.fadeSpeed.get();
               if (hasBlockTarget) {
                  this.fadeAlpha = Math.min(1.0F, this.fadeAlpha + deltaTime * fadeRate);
               } else {
                  this.fadeAlpha = Math.max(0.0F, this.fadeAlpha - deltaTime * fadeRate);
               }
            } else {
               this.fadeAlpha = hasBlockTarget ? 1.0F : 0.0F;
            }

            if (this.fadeAlpha <= 0.001F) {
               this.currentBox = null;
               this.lastPos = null;
            } else {
               if (hasBlockTarget) {
                  BlockHitResult blockHit = (BlockHitResult)hit;
                  BlockPos pos = blockHit.getBlockPos();
                  BlockState state = mc.world.getBlockState(pos);
                  VoxelShape shape = state.getOutlineShape(mc.world, pos);
                  if (!shape.isEmpty()) {
                     Box targetBox = shape.getBoundingBox().offset(pos).expand(0.002);
                     BlockOutlineRenderer.RenderBox targetRenderBox = new BlockOutlineRenderer.RenderBox(
                        targetBox.minX, targetBox.minY, targetBox.minZ, targetBox.maxX, targetBox.maxY, targetBox.maxZ
                     );
                     float morphSpeed = (float)module.animSpeed.get();
                     float lerpAmount = Math.min(1.0F, deltaTime * morphSpeed);
                     if (this.currentBox != null && module.smoothAnim.get() && (this.lastPos == null || !(pos.getSquaredDistance(this.lastPos) > 64.0))) {
                        this.currentBox = this.currentBox.lerp(targetRenderBox, lerpAmount);
                     } else {
                        this.currentBox = targetRenderBox;
                     }

                     this.lastPos = pos;
                  }
               }

               if (this.currentBox != null) {
                  Camera camera = context.camera();
                  MatrixStack matrices = context.matrixStack();
                  matrices.push();
                  matrices.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
                  Color themeColor = new Color(ThemeManager.getThemedColor(0L));
                  float r = themeColor.getRed() / 255.0F;
                  float g = themeColor.getGreen() / 255.0F;
                  float b = themeColor.getBlue() / 255.0F;
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableCull();
                  if (module.onlyVisible.get()) {
                     RenderSystem.enableDepthTest();
                  } else {
                     RenderSystem.disableDepthTest();
                  }

                  Tessellator tessellator = Tessellator.getInstance();
                  if (module.shaderFill.get()) {
                     ShaderProgram shader = RenderSystem.setShader(AstolfoclientClient.BLOCK_OUTLINE_SHADER);
                     if (shader != null) {
                        float timeSecs = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
                        if (shader.getUniform("uTime") != null) {
                           shader.getUniform("uTime").set(timeSecs);
                        }

                        int color1 = ThemeManager.getThemedColor(0L);
                        float r1 = (color1 >> 16 & 0xFF) / 255.0F;
                        float g1 = (color1 >> 8 & 0xFF) / 255.0F;
                        float b1 = (color1 & 0xFF) / 255.0F;
                        int color2 = ThemeManager.getThemedColor(1000L);
                        float r2 = (color2 >> 16 & 0xFF) / 255.0F;
                        float g2 = (color2 >> 8 & 0xFF) / 255.0F;
                        float b2 = (color2 & 0xFF) / 255.0F;
                        if (shader.getUniform("uColor1") != null) {
                           shader.getUniform("uColor1").set(r1, g1, b1);
                        }

                        if (shader.getUniform("uColor2") != null) {
                           shader.getUniform("uColor2").set(r2, g2, b2);
                        }

                        if (shader.getUniform("uBlockCenter") != null) {
                           double centerX = (this.currentBox.minX + this.currentBox.maxX) / 2.0;
                           double centerY = (this.currentBox.minY + this.currentBox.maxY) / 2.0;
                           double centerZ = (this.currentBox.minZ + this.currentBox.maxZ) / 2.0;
                           shader.getUniform("uBlockCenter").set((float)centerX, (float)centerY, (float)centerZ);
                        }

                        if (shader.getUniform("uGlowIntensity") != null) {
                           shader.getUniform("uGlowIntensity").set((float)module.glowIntensity.get());
                        }

                        if (shader.getUniform("uPulseSpeed") != null) {
                           shader.getUniform("uPulseSpeed").set((float)module.pulseSpeed.get());
                        }

                        if (shader.getUniform("uPulseWidth") != null) {
                           shader.getUniform("uPulseWidth").set((float)module.pulseWidth.get());
                        }

                        if (shader.getUniform("uDistortion") != null) {
                           shader.getUniform("uDistortion").set(module.distortion.get() ? 1.0F : 0.0F);
                        }

                        if (shader.getUniform("uChromatic") != null) {
                           shader.getUniform("uChromatic").set(module.chromatic.get() ? 1.0F : 0.0F);
                        }

                        if (shader.getUniform("uFadeAlpha") != null) {
                           shader.getUniform("uFadeAlpha").set(this.fadeAlpha);
                        }

                        if (shader.getUniform("uFillAlpha") != null) {
                           shader.getUniform("uFillAlpha").set((float)module.fillAlpha.get());
                        }

                        if (shader.getUniform("uMode") != null) {
                           int modeIdx = 0;
                           byte var47;
                           if (module.mode.is("Pulse Wave")) {
                              var47 = 0;
                           } else if (module.mode.is("Cosmos")) {
                              var47 = 1;
                           } else if (module.mode.is("Neon Glow")) {
                              var47 = 2;
                           } else if (module.mode.is("Rainbow Wave")) {
                              var47 = 3;
                           } else if (module.mode.is("Cyber Grid")) {
                              var47 = 4;
                           } else {
                              var47 = 5;
                           }

                           shader.getUniform("uMode").set(var47);
                        }
                     }

                     BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                     this.drawBoxFaces(matrices, buffer, this.currentBox, r, g, b, 1.0F);
                     BuiltBuffer builtBuffer = buffer.endNullable();
                     if (builtBuffer != null) {
                        BufferRenderer.drawWithGlobalProgram(builtBuffer);
                     }
                  }

                  if (module.outline.get()) {
                     RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                     RenderSystem.lineWidth((float)module.lineWidth.get());
                     float finalOutlineAlpha = (float)module.outlineAlpha.get() * this.fadeAlpha;
                     if (finalOutlineAlpha > 0.01F) {
                        BufferBuilder lineBuffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                        this.drawBoxOutline(matrices, lineBuffer, this.currentBox, r, g, b, finalOutlineAlpha);
                        BuiltBuffer builtLines = lineBuffer.endNullable();
                        if (builtLines != null) {
                           BufferRenderer.drawWithGlobalProgram(builtLines);
                        }
                     }

                     RenderSystem.lineWidth(1.0F);
                  }

                  RenderSystem.enableDepthTest();
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  matrices.pop();
               }
            }
         }
      } else {
         this.fadeAlpha = 0.0F;
         this.currentBox = null;
         this.lastPos = null;
      }
   }

   private void drawBoxFaces(MatrixStack matrices, BufferBuilder buffer, BlockOutlineRenderer.RenderBox box, float r, float g, float b, float a) {
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      Matrix4f m = matrices.peek().getPositionMatrix();
      buffer.vertex(m, minX, minY, maxZ).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, maxZ).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, minZ).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, minX, minY, minZ).texture(0.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, minZ).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, minZ).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, maxZ).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, maxZ).texture(0.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, minZ).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, minX, minY, minZ).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, minZ).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, minZ).texture(0.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, minX, minY, maxZ).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, maxZ).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, maxZ).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, maxZ).texture(0.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, minX, minY, minZ).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, minX, minY, maxZ).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, maxZ).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, minZ).texture(0.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, maxZ).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, minZ).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, minZ).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, maxZ).texture(0.0F, 1.0F).color(r, g, b, a);
   }

   private void drawBoxOutline(MatrixStack matrices, BufferBuilder buffer, BlockOutlineRenderer.RenderBox box, float r, float g, float b, float a) {
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      Matrix4f m = matrices.peek().getPositionMatrix();
      buffer.vertex(m, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(m, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(m, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(m, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(m, minX, minY, minZ).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, minZ).color(r, g, b, a);
      buffer.vertex(m, maxX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(m, maxX, maxY, maxZ).color(r, g, b, a);
      buffer.vertex(m, minX, minY, maxZ).color(r, g, b, a);
      buffer.vertex(m, minX, maxY, maxZ).color(r, g, b, a);
   }

   @Environment(EnvType.CLIENT)
   private static class RenderBox {
      public double minX;
      public double minY;
      public double minZ;
      public double maxX;
      public double maxY;
      public double maxZ;

      public RenderBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
         this.minX = minX;
         this.minY = minY;
         this.minZ = minZ;
         this.maxX = maxX;
         this.maxY = maxY;
         this.maxZ = maxZ;
      }

      public BlockOutlineRenderer.RenderBox lerp(BlockOutlineRenderer.RenderBox target, float delta) {
         return new BlockOutlineRenderer.RenderBox(
            this.minX + (target.minX - this.minX) * delta,
            this.minY + (target.minY - this.minY) * delta,
            this.minZ + (target.minZ - this.minZ) * delta,
            this.maxX + (target.maxX - this.maxX) * delta,
            this.maxY + (target.maxY - this.maxY) * delta,
            this.maxZ + (target.maxZ - this.maxZ) * delta
         );
      }

      public BlockOutlineRenderer.RenderBox scale(float factor) {
         double centerX = (this.minX + this.maxX) / 2.0;
         double centerY = (this.minY + this.maxY) / 2.0;
         double centerZ = (this.minZ + this.maxZ) / 2.0;
         double halfSizeX = (this.maxX - this.minX) / 2.0 * factor;
         double halfSizeY = (this.maxY - this.minY) / 2.0 * factor;
         double halfSizeZ = (this.maxZ - this.minZ) / 2.0 * factor;
         return new BlockOutlineRenderer.RenderBox(
            centerX - halfSizeX, centerY - halfSizeY, centerZ - halfSizeZ, centerX + halfSizeX, centerY + halfSizeY, centerZ + halfSizeZ
         );
      }

      public double getVolume() {
         return (this.maxX - this.minX) * (this.maxY - this.minY) * (this.maxZ - this.minZ);
      }
   }
}
