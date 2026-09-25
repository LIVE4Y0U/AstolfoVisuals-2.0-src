package xyz.angames.astolfoclient.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.RenderPhase;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class RenderUtil {
   private static final RenderLayer ROUNDED_RECT_LAYER = createLayer("rounded_rect_layer", ShaderProgramKeys.POSITION_COLOR);
   private static final RenderLayer ROUNDED_BORDER_LAYER = createLayer("rounded_border_layer", ShaderProgramKeys.POSITION_COLOR);

   private static RenderLayer createLayer(String name, ShaderProgramKey programKey) {
      return RenderLayer.of(
         name,
         VertexFormats.POSITION_COLOR,
         VertexFormat.DrawMode.QUADS,
         256,
         true,
         true,
         RenderLayer.MultiPhaseParameters.builder()
            .program(new RenderPhase.ShaderProgram(programKey))
            .transparency(new RenderPhase.Transparency("translucent_transparency", RenderSystem::enableBlend, RenderSystem::disableBlend))
            .build(false)
      );
   }

   public static void drawRoundedRect(Matrix4f matrix, VertexConsumerProvider vertexConsumers, float x, float y, float width, float height, BorderRadius radius, int color) {
      if (ShaderManager.ROUNDED_RECT_PROGRAM != null) {
         if (ShaderManager.ROUNDED_RECT_PROGRAM.getUniform("Size") != null) {
            ShaderManager.ROUNDED_RECT_PROGRAM.getUniform("Size").set(width, height);
            ShaderManager.ROUNDED_RECT_PROGRAM
               .getUniform("Radius")
               .set(radius.topLeft(), radius.bottomLeft(), radius.topRight(), radius.bottomRight());
         }

         draw(matrix, vertexConsumers.getBuffer(ROUNDED_RECT_LAYER), x, y, width, height, color);
      }
   }

   public static void drawRoundedRectOutline(
      Matrix4f matrix, VertexConsumerProvider vertexConsumers, float x, float y, float width, float height, BorderRadius radius, int color, float thickness
   ) {
      if (ShaderManager.ROUNDED_BORDER_PROGRAM != null) {
         if (ShaderManager.ROUNDED_BORDER_PROGRAM.getUniform("Size") != null) {
            ShaderManager.ROUNDED_BORDER_PROGRAM.getUniform("Size").set(width, height);
            ShaderManager.ROUNDED_BORDER_PROGRAM
               .getUniform("Radius")
               .set(radius.topLeft(), radius.bottomLeft(), radius.topRight(), radius.bottomRight());
            ShaderManager.ROUNDED_BORDER_PROGRAM.getUniform("Thickness").set(thickness);
         }

         draw(matrix, vertexConsumers.getBuffer(ROUNDED_BORDER_LAYER), x, y, width, height, color);
      }
   }

   private static void draw(Matrix4f matrix, VertexConsumer buffer, float x, float y, float width, float height, int color) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      float pad = 3.0F;
      buffer.vertex(matrix, x - pad, y - pad, 0.0F).color(r, g, b, a);
      buffer.vertex(matrix, x - pad, y + height + pad, 0.0F).color(r, g, b, a);
      buffer.vertex(matrix, x + width + pad, y + height + pad, 0.0F).color(r, g, b, a);
      buffer.vertex(matrix, x + width + pad, y - pad, 0.0F).color(r, g, b, a);
   }
}
