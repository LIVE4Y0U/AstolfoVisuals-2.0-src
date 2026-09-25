package xyz.angames.astolfoclient.client.util;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10142;
import net.minecraft.class_238;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_4587.class_4665;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4i;

@Environment(EnvType.CLIENT)
public class Render3DUtil {
   private static final List<Render3DUtil.Texture> GLOW_TEXTURES = new ArrayList<>();
   private static final class_289 tessellator = class_289.method_1348();

   public static void onRenderWorld(class_4587 matrix) {
      class_4665 entry = matrix.method_23760();
      if (!GLOW_TEXTURES.isEmpty()) {
         Set<class_2960> identifiers = GLOW_TEXTURES.stream().map(texture -> texture.id).collect(Collectors.toCollection(LinkedHashSet::new));
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         identifiers.forEach(
            id -> {
               RenderSystem.setShaderTexture(0, id);
               RenderSystem.setShader(class_10142.field_53880);
               class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
               GLOW_TEXTURES.stream()
                  .filter(texture -> texture.id.equals(id))
                  .forEach(tex -> quadTexture(tex.entry, buffer, tex.x, tex.y, tex.width, tex.height, tex.color));
               class_286.method_43433(buffer.method_60800());
            }
         );
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         GLOW_TEXTURES.clear();
      }
   }

   public static void drawGlowTexture(class_4665 entry, class_2960 id, float x, float y, float width, float height, int color) {
      GLOW_TEXTURES.add(
         new Render3DUtil.Texture(
            entry, id, x, y, width, height, new Vector4i(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), ColorUtil.alpha(color))
         )
      );
   }

   private static void quadTexture(class_4665 entry, class_287 buffer, float x, float y, float width, float height, Vector4i color) {
      Matrix4f matrix = entry != null ? entry.method_23761() : new Matrix4f();
      int r = color.x;
      int g = color.y;
      int b = color.z;
      int a = color.w;
      buffer.method_22918(matrix, x, y + height, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x + width, y + height, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x + width, y, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
      buffer.method_22918(matrix, x, y, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
   }

   public static void drawGlowTexture(class_4665 peek, class_2960 texture, float v, float v1, float v2, float v3, Vector4i vector4i, boolean b) {
   }

   public static void drawBox(@Nullable class_4587 matrixStack, class_238 box, Color color) {
   }

   @Environment(EnvType.CLIENT)
   public record Texture(class_4665 entry, class_2960 id, float x, float y, float width, float height, Vector4i color) {
   }
}
