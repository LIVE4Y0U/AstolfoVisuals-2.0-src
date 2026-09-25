package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.BabyPlayerModule;

@Environment(EnvType.CLIENT)
public class TrailsRenderer {
   private final minecraft.client.MinecraftClient client = minecraft.client.MinecraftClient.getInstance();
   private final List<TrailsRenderer.TrailPoint> points = new ArrayList<>();
   private static final long TRAIL_LIFESPAN = 850L;

   public void render(WorldRenderContext context) {
      Module module = AstolfoclientClient.moduleManager.getModuleByName("Trails");
      if (module != null && module.isEnabled() && !this.client.options.getPerspective().isFirstPerson()) {
         client.network.ClientPlayerEntity player = this.client.player;
         if (player != null) {
            float tickDelta = context.tickCounter().getTickDelta(true);
            double x = util.math.MathHelper.lerp(tickDelta, player.lastRenderX, player.getX());
            double y = util.math.MathHelper.lerp(tickDelta, player.lastRenderY, player.getY());
            double z = util.math.MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ());
            util.math.Vec3d currentPos = new util.math.Vec3d(x, y, z);
            boolean isBaby = false;
            if (AstolfoclientClient.moduleManager.getModuleByName("BabyPlayer") instanceof BabyPlayerModule bpm) {
               isBaby = bpm.isEnabled() && bpm.self.get();
            }

            float targetHeight = isBaby ? 0.9F : player.getHeight();
            if (this.points.isEmpty() || this.points.get(this.points.size() - 1).pos.squaredDistanceTo(currentPos) > 0.001) {
               this.points.add(new TrailsRenderer.TrailPoint(currentPos, targetHeight, System.currentTimeMillis()));
            }

            long currentTime = System.currentTimeMillis();
            this.points.removeIf(p -> currentTime - p.timeCreated > 850L);
            if (this.points.size() >= 2) {
               RenderSystem.enableBlend();
               RenderSystem.disableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.blendFunc(platform.GlStateManager.SrcFactor.SRC_ALPHA, platform.GlStateManager.DstFactor.ONE);
               RenderSystem.setShader(client.gl.ShaderProgramKeys.POSITION_COLOR);
               client.render.Tessellator tessellator = client.render.Tessellator.getInstance();
               util.math.MatrixStack matrices = context.matrixStack();
               Matrix4f matrix = matrices.peek().getPositionMatrix();
               util.math.Vec3d cameraPos = context.camera().getPos();
               client.render.BufferBuilder buffer = tessellator.begin(render.VertexFormat.DrawMode.TRIANGLE_STRIP, client.render.VertexFormats.POSITION_COLOR);
               int index = 0;

               for (TrailsRenderer.TrailPoint point : this.points) {
                  long age = currentTime - point.timeCreated;
                  float progress = util.math.MathHelper.clamp((float)age / 850.0F, 0.0F, 1.0F);
                  float reverseProgress = 1.0F - progress;
                  float alpha = (float)Math.pow(reverseProgress, 2.5);
                  float bodyAlpha = alpha * 0.45F;
                  float heightScale = reverseProgress * reverseProgress;
                  float midY = point.height / 2.0F;
                  float topY = midY + midY * heightScale;
                  float botY = midY - midY * heightScale;
                  Color c = new Color(ThemeManager.getThemedColor(index * 20));
                  float r = c.getRed() / 255.0F;
                  float g = c.getGreen() / 255.0F;
                  float b = c.getBlue() / 255.0F;
                  double renderX = point.pos.x - cameraPos.x;
                  double renderY = point.pos.y - cameraPos.y;
                  double renderZ = point.pos.z - cameraPos.z;
                  buffer.vertex(matrix, (float)renderX, (float)(renderY + topY), (float)renderZ).color(r, g, b, bodyAlpha);
                  buffer.vertex(matrix, (float)renderX, (float)(renderY + botY), (float)renderZ).color(r, g, b, bodyAlpha);
                  index++;
               }

               client.render.BufferRenderer.drawWithGlobalProgram(buffer.end());
               RenderSystem.lineWidth(2.5F);
               buffer = tessellator.begin(render.VertexFormat.DrawMode.DEBUG_LINE_STRIP, client.render.VertexFormats.POSITION_COLOR);
               index = 0;

               for (TrailsRenderer.TrailPoint point : this.points) {
                  long age = currentTime - point.timeCreated;
                  float progress = util.math.MathHelper.clamp((float)age / 850.0F, 0.0F, 1.0F);
                  float reverseProgress = 1.0F - progress;
                  float alpha = (float)Math.pow(reverseProgress, 2.0);
                  float heightScale = reverseProgress * reverseProgress;
                  float midY = point.height / 2.0F;
                  float topY = midY + midY * heightScale;
                  Color c = new Color(ThemeManager.getThemedColor(index * 20));
                  double renderX = point.pos.x - cameraPos.x;
                  double renderY = point.pos.y - cameraPos.y;
                  double renderZ = point.pos.z - cameraPos.z;
                  buffer.vertex(matrix, (float)renderX, (float)(renderY + topY), (float)renderZ)
                     .color(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, alpha * 0.8F);
                  index++;
               }

               client.render.BufferRenderer.drawWithGlobalProgram(buffer.end());
               buffer = tessellator.begin(render.VertexFormat.DrawMode.DEBUG_LINE_STRIP, client.render.VertexFormats.POSITION_COLOR);
               index = 0;

               for (TrailsRenderer.TrailPoint point : this.points) {
                  long age = currentTime - point.timeCreated;
                  float progress = util.math.MathHelper.clamp((float)age / 850.0F, 0.0F, 1.0F);
                  float reverseProgress = 1.0F - progress;
                  float alpha = (float)Math.pow(reverseProgress, 2.0);
                  float heightScale = reverseProgress * reverseProgress;
                  float midY = point.height / 2.0F;
                  float botY = midY - midY * heightScale;
                  Color c = new Color(ThemeManager.getThemedColor(index * 20));
                  double renderX = point.pos.x - cameraPos.x;
                  double renderY = point.pos.y - cameraPos.y;
                  double renderZ = point.pos.z - cameraPos.z;
                  buffer.vertex(matrix, (float)renderX, (float)(renderY + botY), (float)renderZ)
                     .color(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, alpha * 0.8F);
                  index++;
               }

               client.render.BufferRenderer.drawWithGlobalProgram(buffer.end());
               RenderSystem.lineWidth(1.0F);
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(true);
               RenderSystem.enableCull();
               RenderSystem.defaultBlendFunc();
               RenderSystem.disableBlend();
            }
         }
      } else {
         this.points.clear();
      }
   }

   @Environment(EnvType.CLIENT)
   private static class TrailPoint {
      final util.math.Vec3d pos;
      final float height;
      final long timeCreated;

      public TrailPoint(util.math.Vec3d pos, float height, long timeCreated) {
         this.pos = pos;
         this.height = height;
         this.timeCreated = timeCreated;
      }
   }
}
