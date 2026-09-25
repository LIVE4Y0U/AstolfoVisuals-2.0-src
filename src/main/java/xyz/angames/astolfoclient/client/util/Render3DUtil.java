package xyz.angames.astolfoclient.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.math.Box;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack.Entry;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4i;

@Environment(EnvType.CLIENT)
public class Render3DUtil {
   private static final List<Render3DUtil.Texture> GLOW_TEXTURES = new ArrayList<>();
   private static final Tessellator tessellator = Tessellator.getInstance();

   public static void onRenderWorld(MatrixStack matrix) {
      MatrixStack.Entry entry = matrix.peek();
      if (!GLOW_TEXTURES.isEmpty()) {
         Set<Identifier> identifiers = GLOW_TEXTURES.stream().map(texture -> texture.id).collect(Collectors.toCollection(LinkedHashSet::new));
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
         identifiers.forEach(
            id -> {
               RenderSystem.setShaderTexture(0, id);
               RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
               BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
               GLOW_TEXTURES.stream()
                  .filter(texture -> texture.id.equals(id))
                  .forEach(tex -> quadTexture(tex.entry, buffer, tex.x, tex.y, tex.width, tex.height, tex.color));
               BufferRenderer.drawWithGlobalProgram(buffer.end());
            }
         );
         RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         GLOW_TEXTURES.clear();
      }
   }

   public static void drawGlowTexture(MatrixStack.Entry entry, Identifier id, float x, float y, float width, float height, int color) {
      GLOW_TEXTURES.add(
         new Render3DUtil.Texture(
            entry, id, x, y, width, height, new Vector4i(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), ColorUtil.alpha(color))
         )
      );
   }

   private static void quadTexture(MatrixStack.Entry entry, BufferBuilder buffer, float x, float y, float width, float height, Vector4i color) {
      Matrix4f matrix = entry != null ? entry.getPositionMatrix() : new Matrix4f();
      int r = color.x;
      int g = color.y;
      int b = color.z;
      int a = color.w;
      buffer.vertex(matrix, x, y + height, 0.0F).texture(0.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(matrix, x + width, y + height, 0.0F).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(matrix, x + width, y, 0.0F).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(matrix, x, y, 0.0F).texture(0.0F, 0.0F).color(r, g, b, a);
   }

   public static void drawGlowTexture(MatrixStack.Entry peek, Identifier texture, float v, float v1, float v2, float v3, Vector4i vector4i, boolean b) {
   }

   public static void drawBox(@Nullable MatrixStack matrixStack, Box box, Color color) {
   }

   @Environment(EnvType.CLIENT)
   public record Texture(MatrixStack.Entry entry, Identifier id, float x, float y, float width, float height, Vector4i color) {
   }
}
