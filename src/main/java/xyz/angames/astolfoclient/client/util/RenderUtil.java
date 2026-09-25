package xyz.angames.astolfoclient.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10142;
import net.minecraft.class_10156;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_1921.class_4688;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_4668.class_4685;
import net.minecraft.class_4668.class_5942;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class RenderUtil {
   private static final class_1921 ROUNDED_RECT_LAYER = createLayer("rounded_rect_layer", class_10142.field_53876);
   private static final class_1921 ROUNDED_BORDER_LAYER = createLayer("rounded_border_layer", class_10142.field_53876);

   private static class_1921 createLayer(String name, class_10156 programKey) {
      return class_1921.method_24049(
         name,
         class_290.field_1576,
         class_5596.field_27382,
         256,
         true,
         true,
         class_4688.method_23598()
            .method_34578(new class_5942(programKey))
            .method_23615(new class_4685("translucent_transparency", RenderSystem::enableBlend, RenderSystem::disableBlend))
            .method_23617(false)
      );
   }

   public static void drawRoundedRect(Matrix4f matrix, class_4597 vertexConsumers, float x, float y, float width, float height, BorderRadius radius, int color) {
      if (ShaderManager.ROUNDED_RECT_PROGRAM != null) {
         if (ShaderManager.ROUNDED_RECT_PROGRAM.method_34582("Size") != null) {
            ShaderManager.ROUNDED_RECT_PROGRAM.method_34582("Size").method_1255(width, height);
            ShaderManager.ROUNDED_RECT_PROGRAM
               .method_34582("Radius")
               .method_35657(radius.topLeft(), radius.bottomLeft(), radius.topRight(), radius.bottomRight());
         }

         draw(matrix, vertexConsumers.getBuffer(ROUNDED_RECT_LAYER), x, y, width, height, color);
      }
   }

   public static void drawRoundedRectOutline(
      Matrix4f matrix, class_4597 vertexConsumers, float x, float y, float width, float height, BorderRadius radius, int color, float thickness
   ) {
      if (ShaderManager.ROUNDED_BORDER_PROGRAM != null) {
         if (ShaderManager.ROUNDED_BORDER_PROGRAM.method_34582("Size") != null) {
            ShaderManager.ROUNDED_BORDER_PROGRAM.method_34582("Size").method_1255(width, height);
            ShaderManager.ROUNDED_BORDER_PROGRAM
               .method_34582("Radius")
               .method_35657(radius.topLeft(), radius.bottomLeft(), radius.topRight(), radius.bottomRight());
            ShaderManager.ROUNDED_BORDER_PROGRAM.method_34582("Thickness").method_1251(thickness);
         }

         draw(matrix, vertexConsumers.getBuffer(ROUNDED_BORDER_LAYER), x, y, width, height, color);
      }
   }

   private static void draw(Matrix4f matrix, class_4588 buffer, float x, float y, float width, float height, int color) {
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      float a = (color >> 24 & 0xFF) / 255.0F;
      float pad = 3.0F;
      buffer.method_22918(matrix, x - pad, y - pad, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(matrix, x - pad, y + height + pad, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(matrix, x + width + pad, y + height + pad, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(matrix, x + width + pad, y - pad, 0.0F).method_22915(r, g, b, a);
   }
}
