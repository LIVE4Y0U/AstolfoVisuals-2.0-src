package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
public class ChinaHatFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
   public static PlayerEntity currentlyRenderingPlayer;
   private static final float RADIUS = 0.65F;
   private static final float HEIGHT = 0.28F;
   private static final int SEGMENTS = 40;
   private static final Identifier BLOOM_TEXTURE = Identifier.of("astolfoclient", "textures/effects/dashtrail/dashbloom.png");
   private float spinAngle = 0.0F;
   private long lastRenderTime = 0L;

   public ChinaHatFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
      super(context);
   }

   public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
      MinecraftClient mc = MinecraftClient.getInstance();
      Module module = AstolfoclientClient.moduleManager.getModuleByName("ChinaHat");
      if (module != null && module.isEnabled() && mc.player != null) {
         if (currentlyRenderingPlayer != null && currentlyRenderingPlayer.getId() == mc.player.getId()) {
            if (!state.invisible) {
               long now = System.currentTimeMillis();
               if (this.lastRenderTime != 0L) {
                  this.spinAngle = this.spinAngle + (float)(now - this.lastRenderTime) / 1000.0F * 80.0F;
                  if (this.spinAngle > 360.0F) {
                     this.spinAngle -= 360.0F;
                  }
               }

               this.lastRenderTime = now;
               matrices.push();
               ((PlayerEntityModel)this.getContextModel()).head.rotate(matrices);
               matrices.translate(0.0F, -0.4F, 0.0F);
               matrices.scale(1.0F, -1.0F, 1.0F);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.spinAngle));
               Matrix4f matrix = matrices.peek().getPositionMatrix();
               Tessellator tessellator = Tessellator.getInstance();
               if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
                  immediate.draw();
               }

               RenderSystem.enableBlend();
               RenderSystem.disableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(true);
               RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
               ShaderProgram shader = RenderSystem.setShader(AstolfoclientClient.CHINA_HAT_SHADER);
               if (shader != null) {
                  float timeSecs = (float)(System.currentTimeMillis() % 1000000L) / 1000.0F;
                  if (shader.getUniform("uTime") != null) {
                     shader.getUniform("uTime").set(timeSecs);
                  }

                  if (shader.getUniform("uResolution") != null) {
                     shader.getUniform("uResolution").set(1.0F, 1.0F);
                  }

                  int themeRgb = ThemeManager.getThemedColor((int)(now / 10L));
                  float tr = (themeRgb >> 16 & 0xFF) / 255.0F;
                  float tg = (themeRgb >> 8 & 0xFF) / 255.0F;
                  float tb = (themeRgb & 0xFF) / 255.0F;
                  if (shader.getUniform("uThemeColor") != null) {
                     shader.getUniform("uThemeColor").set(tr, tg, tb);
                  }
               }

               Color cInner = new Color(ThemeManager.getThemedColor((int)(now / 10L)));
               Color cOuter = new Color(ThemeManager.getThemedColor((int)(now / 10L) + 60));
               int optimizedLayers = 16;
               BufferBuilder bufBody = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

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
                     bufBody.vertex(matrix, (float)Math.cos(ang0) * r0, h0, (float)Math.sin(ang0) * r0)
                        .texture(u0_0, v0_0)
                        .color(sc0.getRed() / 255.0F, sc0.getGreen() / 255.0F, sc0.getBlue() / 255.0F, a0);
                     bufBody.vertex(matrix, (float)Math.cos(ang1) * r0, h0, (float)Math.sin(ang1) * r0)
                        .texture(u1_0, v1_0)
                        .color(sc0.getRed() / 255.0F, sc0.getGreen() / 255.0F, sc0.getBlue() / 255.0F, a0);
                     bufBody.vertex(matrix, (float)Math.cos(ang1) * r1, h1, (float)Math.sin(ang1) * r1)
                        .texture(u1_1, v1_1)
                        .color(sc1.getRed() / 255.0F, sc1.getGreen() / 255.0F, sc1.getBlue() / 255.0F, a1);
                     bufBody.vertex(matrix, (float)Math.cos(ang0) * r1, h1, (float)Math.sin(ang0) * r1)
                        .texture(u0_1, v0_1)
                        .color(sc1.getRed() / 255.0F, sc1.getGreen() / 255.0F, sc1.getBlue() / 255.0F, a1);
                  }
               }

               BufferRenderer.drawWithGlobalProgram(bufBody.end());
               float r = cOuter.getRed() / 255.0F;
               float g = cOuter.getGreen() / 255.0F;
               float b = cOuter.getBlue() / 255.0F;
               float rimW = 0.035F;
               float innerR = 0.65F - rimW;
               float innerH = 0.28F * (rimW / 0.65F);
               float innerT = (0.65F - rimW) / 0.65F;
               BufferBuilder bufRim = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

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
                  bufRim.vertex(matrix, (float)Math.cos(a0) * innerR, innerH, (float)Math.sin(a0) * innerR)
                     .texture(u0_in, v0_in)
                     .color(r, g, b, 1.0F);
                  bufRim.vertex(matrix, (float)Math.cos(a1) * innerR, innerH, (float)Math.sin(a1) * innerR)
                     .texture(u1_in, v1_in)
                     .color(r, g, b, 1.0F);
                  bufRim.vertex(matrix, (float)Math.cos(a1) * 0.65F, 0.0F, (float)Math.sin(a1) * 0.65F)
                     .texture(u1_out, v1_out)
                     .color(r, g, b, 1.0F);
                  bufRim.vertex(matrix, (float)Math.cos(a0) * 0.65F, 0.0F, (float)Math.sin(a0) * 0.65F)
                     .texture(u0_out, v0_out)
                     .color(r, g, b, 1.0F);
               }

               BufferRenderer.drawWithGlobalProgram(bufRim.end());
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
               RenderSystem.disableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               r = cOuter.getRed() / 255.0F;
               g = cOuter.getGreen() / 255.0F;
               b = cOuter.getBlue() / 255.0F;
               RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
               BufferBuilder glowRimBuf = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
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
                  glowRimBuf.vertex(matrix, x0_in, 0.005F, z0_in).color(r, g, b, innerT);
                  glowRimBuf.vertex(matrix, x1_in, 0.005F, z1_in).color(r, g, b, innerT);
                  glowRimBuf.vertex(matrix, x1_out, 0.001F, z1_out).color(r, g, b, outerAlpha);
                  glowRimBuf.vertex(matrix, x0_out, 0.001F, z0_out).color(r, g, b, outerAlpha);
               }

               BufferRenderer.drawWithGlobalProgram(glowRimBuf.end());
               RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
               RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
               BufferBuilder bloomBaseBuf = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               float bSize = 0.663F;
               bloomBaseBuf.vertex(matrix, -bSize, 0.005F, bSize).texture(0.0F, 1.0F).color(r, g, b, 0.4F);
               bloomBaseBuf.vertex(matrix, bSize, 0.005F, bSize).texture(1.0F, 1.0F).color(r, g, b, 0.4F);
               bloomBaseBuf.vertex(matrix, bSize, 0.005F, -bSize).texture(1.0F, 0.0F).color(r, g, b, 0.4F);
               bloomBaseBuf.vertex(matrix, -bSize, 0.005F, -bSize).texture(0.0F, 0.0F).color(r, g, b, 0.4F);
               BufferRenderer.drawWithGlobalProgram(bloomBaseBuf.end());
               matrices.pop();
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
      t = MathHelper.clamp(t, 0.0F, 1.0F);
      return new Color(
         (int)(a.getRed() + (b.getRed() - a.getRed()) * t),
         (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
         (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t)
      );
   }

   private float smoothstep(float t) {
      t = MathHelper.clamp(t, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }
}
