package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_5944;
import net.minecraft.class_9801;
import net.minecraft.class_239.class_240;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.modules.render.BlockOutlineModule;

@Environment(EnvType.CLIENT)
public class BlockOutlineRenderer {
   private BlockOutlineRenderer.RenderBox currentBox = null;
   private class_2338 lastPos = null;
   private long lastRenderTime = System.currentTimeMillis();
   private float fadeAlpha = 0.0F;

   public void render(WorldRenderContext context) {
      BlockOutlineModule module = (BlockOutlineModule)AstolfoclientClient.moduleManager.getModuleByName("BlockOutline");
      if (module != null && module.isEnabled()) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 != null && mc.field_1724 != null) {
            long now = System.currentTimeMillis();
            float deltaTime = (float)(now - this.lastRenderTime) / 1000.0F;
            this.lastRenderTime = now;
            if (deltaTime > 0.1F) {
               deltaTime = 0.1F;
            }

            if (deltaTime < 0.001F) {
               deltaTime = 0.001F;
            }

            class_239 hit = mc.field_1765;
            boolean hasBlockTarget = hit != null && hit.method_17783() == class_240.field_1332;
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
                  class_3965 blockHit = (class_3965)hit;
                  class_2338 pos = blockHit.method_17777();
                  class_2680 state = mc.field_1687.method_8320(pos);
                  class_265 shape = state.method_26218(mc.field_1687, pos);
                  if (!shape.method_1110()) {
                     class_238 targetBox = shape.method_1107().method_996(pos).method_1014(0.002);
                     BlockOutlineRenderer.RenderBox targetRenderBox = new BlockOutlineRenderer.RenderBox(
                        targetBox.field_1323, targetBox.field_1322, targetBox.field_1321, targetBox.field_1320, targetBox.field_1325, targetBox.field_1324
                     );
                     float morphSpeed = (float)module.animSpeed.get();
                     float lerpAmount = Math.min(1.0F, deltaTime * morphSpeed);
                     if (this.currentBox != null && module.smoothAnim.get() && (this.lastPos == null || !(pos.method_10262(this.lastPos) > 64.0))) {
                        this.currentBox = this.currentBox.lerp(targetRenderBox, lerpAmount);
                     } else {
                        this.currentBox = targetRenderBox;
                     }

                     this.lastPos = pos;
                  }
               }

               if (this.currentBox != null) {
                  class_4184 camera = context.camera();
                  class_4587 matrices = context.matrixStack();
                  matrices.method_22903();
                  matrices.method_22904(-camera.method_19326().field_1352, -camera.method_19326().field_1351, -camera.method_19326().field_1350);
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

                  class_289 tessellator = class_289.method_1348();
                  if (module.shaderFill.get()) {
                     class_5944 shader = RenderSystem.setShader(AstolfoclientClient.BLOCK_OUTLINE_SHADER);
                     if (shader != null) {
                        float timeSecs = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
                        if (shader.method_34582("uTime") != null) {
                           shader.method_34582("uTime").method_1251(timeSecs);
                        }

                        int color1 = ThemeManager.getThemedColor(0L);
                        float r1 = (color1 >> 16 & 0xFF) / 255.0F;
                        float g1 = (color1 >> 8 & 0xFF) / 255.0F;
                        float b1 = (color1 & 0xFF) / 255.0F;
                        int color2 = ThemeManager.getThemedColor(1000L);
                        float r2 = (color2 >> 16 & 0xFF) / 255.0F;
                        float g2 = (color2 >> 8 & 0xFF) / 255.0F;
                        float b2 = (color2 & 0xFF) / 255.0F;
                        if (shader.method_34582("uColor1") != null) {
                           shader.method_34582("uColor1").method_1249(r1, g1, b1);
                        }

                        if (shader.method_34582("uColor2") != null) {
                           shader.method_34582("uColor2").method_1249(r2, g2, b2);
                        }

                        if (shader.method_34582("uBlockCenter") != null) {
                           double centerX = (this.currentBox.minX + this.currentBox.maxX) / 2.0;
                           double centerY = (this.currentBox.minY + this.currentBox.maxY) / 2.0;
                           double centerZ = (this.currentBox.minZ + this.currentBox.maxZ) / 2.0;
                           shader.method_34582("uBlockCenter").method_1249((float)centerX, (float)centerY, (float)centerZ);
                        }

                        if (shader.method_34582("uGlowIntensity") != null) {
                           shader.method_34582("uGlowIntensity").method_1251((float)module.glowIntensity.get());
                        }

                        if (shader.method_34582("uPulseSpeed") != null) {
                           shader.method_34582("uPulseSpeed").method_1251((float)module.pulseSpeed.get());
                        }

                        if (shader.method_34582("uPulseWidth") != null) {
                           shader.method_34582("uPulseWidth").method_1251((float)module.pulseWidth.get());
                        }

                        if (shader.method_34582("uDistortion") != null) {
                           shader.method_34582("uDistortion").method_1251(module.distortion.get() ? 1.0F : 0.0F);
                        }

                        if (shader.method_34582("uChromatic") != null) {
                           shader.method_34582("uChromatic").method_1251(module.chromatic.get() ? 1.0F : 0.0F);
                        }

                        if (shader.method_34582("uFadeAlpha") != null) {
                           shader.method_34582("uFadeAlpha").method_1251(this.fadeAlpha);
                        }

                        if (shader.method_34582("uFillAlpha") != null) {
                           shader.method_34582("uFillAlpha").method_1251((float)module.fillAlpha.get());
                        }

                        if (shader.method_34582("uMode") != null) {
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

                           shader.method_34582("uMode").method_35649(var47);
                        }
                     }

                     class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
                     this.drawBoxFaces(matrices, buffer, this.currentBox, r, g, b, 1.0F);
                     class_9801 builtBuffer = buffer.method_60794();
                     if (builtBuffer != null) {
                        class_286.method_43433(builtBuffer);
                     }
                  }

                  if (module.outline.get()) {
                     RenderSystem.setShader(class_10142.field_53876);
                     RenderSystem.lineWidth((float)module.lineWidth.get());
                     float finalOutlineAlpha = (float)module.outlineAlpha.get() * this.fadeAlpha;
                     if (finalOutlineAlpha > 0.01F) {
                        class_287 lineBuffer = tessellator.method_60827(class_5596.field_29344, class_290.field_1576);
                        this.drawBoxOutline(matrices, lineBuffer, this.currentBox, r, g, b, finalOutlineAlpha);
                        class_9801 builtLines = lineBuffer.method_60794();
                        if (builtLines != null) {
                           class_286.method_43433(builtLines);
                        }
                     }

                     RenderSystem.lineWidth(1.0F);
                  }

                  RenderSystem.enableDepthTest();
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  matrices.method_22909();
               }
            }
         }
      } else {
         this.fadeAlpha = 0.0F;
         this.currentBox = null;
         this.lastPos = null;
      }
   }

   private void drawBoxFaces(class_4587 matrices, class_287 buffer, BlockOutlineRenderer.RenderBox box, float r, float g, float b, float a) {
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      Matrix4f m = matrices.method_23760().method_23761();
      buffer.method_22918(m, minX, minY, maxZ).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, maxZ).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, minZ).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, minZ).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, minZ).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, minZ).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, maxZ).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, maxZ).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, minZ).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, minZ).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, minZ).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, minZ).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, maxZ).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, maxZ).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, maxZ).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, maxZ).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, minZ).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, maxZ).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, maxZ).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, minZ).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, maxZ).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, minZ).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, minZ).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, maxZ).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
   }

   private void drawBoxOutline(class_4587 matrices, class_287 buffer, BlockOutlineRenderer.RenderBox box, float r, float g, float b, float a) {
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      Matrix4f m = matrices.method_23760().method_23761();
      buffer.method_22918(m, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, minZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, maxX, maxY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, minY, maxZ).method_22915(r, g, b, a);
      buffer.method_22918(m, minX, maxY, maxZ).method_22915(r, g, b, a);
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
