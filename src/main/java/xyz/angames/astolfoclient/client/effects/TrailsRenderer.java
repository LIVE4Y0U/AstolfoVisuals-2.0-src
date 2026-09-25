package xyz.angames.astolfoclient.client.effects;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.BabyPlayerModule;

@Environment(EnvType.CLIENT)
public class TrailsRenderer {
   private final class_310 client = class_310.method_1551();
   private final List<TrailsRenderer.TrailPoint> points = new ArrayList<>();
   private static final long TRAIL_LIFESPAN = 850L;

   public void render(WorldRenderContext context) {
      Module module = AstolfoclientClient.moduleManager.getModuleByName("Trails");
      if (module != null && module.isEnabled() && !this.client.field_1690.method_31044().method_31034()) {
         class_746 player = this.client.field_1724;
         if (player != null) {
            float tickDelta = context.tickCounter().method_60637(true);
            double x = class_3532.method_16436(tickDelta, player.field_6038, player.method_23317());
            double y = class_3532.method_16436(tickDelta, player.field_5971, player.method_23318());
            double z = class_3532.method_16436(tickDelta, player.field_5989, player.method_23321());
            class_243 currentPos = new class_243(x, y, z);
            boolean isBaby = false;
            if (AstolfoclientClient.moduleManager.getModuleByName("BabyPlayer") instanceof BabyPlayerModule bpm) {
               isBaby = bpm.isEnabled() && bpm.self.get();
            }

            float targetHeight = isBaby ? 0.9F : player.method_17682();
            if (this.points.isEmpty() || this.points.get(this.points.size() - 1).pos.method_1025(currentPos) > 0.001) {
               this.points.add(new TrailsRenderer.TrailPoint(currentPos, targetHeight, System.currentTimeMillis()));
            }

            long currentTime = System.currentTimeMillis();
            this.points.removeIf(p -> currentTime - p.timeCreated > 850L);
            if (this.points.size() >= 2) {
               RenderSystem.enableBlend();
               RenderSystem.disableCull();
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
               RenderSystem.setShader(class_10142.field_53876);
               class_289 tessellator = class_289.method_1348();
               class_4587 matrices = context.matrixStack();
               Matrix4f matrix = matrices.method_23760().method_23761();
               class_243 cameraPos = context.camera().method_19326();
               class_287 buffer = tessellator.method_60827(class_5596.field_27380, class_290.field_1576);
               int index = 0;

               for (TrailsRenderer.TrailPoint point : this.points) {
                  long age = currentTime - point.timeCreated;
                  float progress = class_3532.method_15363((float)age / 850.0F, 0.0F, 1.0F);
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
                  double renderX = point.pos.field_1352 - cameraPos.field_1352;
                  double renderY = point.pos.field_1351 - cameraPos.field_1351;
                  double renderZ = point.pos.field_1350 - cameraPos.field_1350;
                  buffer.method_22918(matrix, (float)renderX, (float)(renderY + topY), (float)renderZ).method_22915(r, g, b, bodyAlpha);
                  buffer.method_22918(matrix, (float)renderX, (float)(renderY + botY), (float)renderZ).method_22915(r, g, b, bodyAlpha);
                  index++;
               }

               class_286.method_43433(buffer.method_60800());
               RenderSystem.lineWidth(2.5F);
               buffer = tessellator.method_60827(class_5596.field_29345, class_290.field_1576);
               index = 0;

               for (TrailsRenderer.TrailPoint point : this.points) {
                  long age = currentTime - point.timeCreated;
                  float progress = class_3532.method_15363((float)age / 850.0F, 0.0F, 1.0F);
                  float reverseProgress = 1.0F - progress;
                  float alpha = (float)Math.pow(reverseProgress, 2.0);
                  float heightScale = reverseProgress * reverseProgress;
                  float midY = point.height / 2.0F;
                  float topY = midY + midY * heightScale;
                  Color c = new Color(ThemeManager.getThemedColor(index * 20));
                  double renderX = point.pos.field_1352 - cameraPos.field_1352;
                  double renderY = point.pos.field_1351 - cameraPos.field_1351;
                  double renderZ = point.pos.field_1350 - cameraPos.field_1350;
                  buffer.method_22918(matrix, (float)renderX, (float)(renderY + topY), (float)renderZ)
                     .method_22915(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, alpha * 0.8F);
                  index++;
               }

               class_286.method_43433(buffer.method_60800());
               buffer = tessellator.method_60827(class_5596.field_29345, class_290.field_1576);
               index = 0;

               for (TrailsRenderer.TrailPoint point : this.points) {
                  long age = currentTime - point.timeCreated;
                  float progress = class_3532.method_15363((float)age / 850.0F, 0.0F, 1.0F);
                  float reverseProgress = 1.0F - progress;
                  float alpha = (float)Math.pow(reverseProgress, 2.0);
                  float heightScale = reverseProgress * reverseProgress;
                  float midY = point.height / 2.0F;
                  float botY = midY - midY * heightScale;
                  Color c = new Color(ThemeManager.getThemedColor(index * 20));
                  double renderX = point.pos.field_1352 - cameraPos.field_1352;
                  double renderY = point.pos.field_1351 - cameraPos.field_1351;
                  double renderZ = point.pos.field_1350 - cameraPos.field_1350;
                  buffer.method_22918(matrix, (float)renderX, (float)(renderY + botY), (float)renderZ)
                     .method_22915(c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, alpha * 0.8F);
                  index++;
               }

               class_286.method_43433(buffer.method_60800());
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
      final class_243 pos;
      final float height;
      final long timeCreated;

      public TrailPoint(class_243 pos, float height, long timeCreated) {
         this.pos = pos;
         this.height = height;
         this.timeCreated = timeCreated;
      }
   }
}
