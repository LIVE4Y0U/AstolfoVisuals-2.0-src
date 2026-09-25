package xyz.angames.astolfoclient.client.render;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sxmurxy.mre.builders.Builder;
import dev.sxmurxy.mre.msdf.MsdfFont;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.class_10142;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.manager.GpsManager;

@Environment(EnvType.CLIENT)
public class GpsRenderer {
   private static final Supplier<MsdfFont> BIKO_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("biko").data("biko").build());
   private static final class_2960 GPS_ICON = class_2960.method_60655("astolfoclient", "textures/icons/gps.png");
   private String cachedDistanceText = "";
   private int lastDistanceInt = -1;

   public void render(WorldRenderContext context) {
      GpsManager manager = GpsManager.getInstance();
      if (manager.isActive()) {
         class_310 client = class_310.method_1551();
         if (client.field_1724 != null && client.field_1687 != null) {
            class_243 cameraPos = context.camera().method_19326();
            double targetX = manager.getTargetX();
            double targetZ = manager.getTargetZ();
            float tickDelta = context.tickCounter().method_60637(true);
            double playerX = class_3532.method_16436(tickDelta, client.field_1724.field_6014, client.field_1724.method_23317());
            double playerY = class_3532.method_16436(tickDelta, client.field_1724.field_6036, client.field_1724.method_23318());
            double playerZ = class_3532.method_16436(tickDelta, client.field_1724.field_5969, client.field_1724.method_23321());
            double targetY = playerY + 2.0;
            double distSq = client.field_1724.method_5649(targetX, client.field_1724.method_23318(), targetZ);
            double realDistance = Math.sqrt(distSq);
            int currentDistanceInt = (int)realDistance;
            if (currentDistanceInt != this.lastDistanceInt) {
               this.cachedDistanceText = String.format("%.0fm", realDistance);
               this.lastDistanceInt = currentDistanceInt;
            }

            double renderDist = 10.0;
            double dx = targetX - playerX;
            double dy = targetY - playerY;
            double dz = targetZ - playerZ;
            double distance3D = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double dirX = dx / distance3D;
            double dirY = dy / distance3D;
            double dirZ = dz / distance3D;
            double renderX;
            double renderYFinal;
            double renderZ;
            if (realDistance < renderDist) {
               renderX = targetX;
               renderYFinal = targetY;
               renderZ = targetZ;
            } else {
               renderX = playerX + dirX * renderDist;
               renderYFinal = playerY + dirY * renderDist;
               renderZ = playerZ + dirZ * renderDist;
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            class_4587 matrixStack = context.matrixStack();
            matrixStack.method_22903();
            matrixStack.method_22904(renderX - cameraPos.field_1352, renderYFinal - cameraPos.field_1351, renderZ - cameraPos.field_1350);
            matrixStack.method_22907(context.camera().method_23767());
            matrixStack.method_22905(1.0F, -1.0F, 1.0F);
            float fixedScale = 0.04F;
            matrixStack.method_22905(fixedScale, fixedScale, fixedScale);
            RenderSystem.setShaderTexture(0, GPS_ICON);
            RenderSystem.setShader(class_10142.field_53880);
            class_289 tessellator = class_289.method_1348();
            float size = 40.0F;
            float halfSize = size / 2.0F;
            float shadowOffset = 0.8F;
            matrixStack.method_22903();
            matrixStack.method_46416(shadowOffset, shadowOffset, 0.05F);
            Matrix4f shadowMatrix = matrixStack.method_23760().method_23761();
            class_287 shadowBuffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
            shadowBuffer.method_22918(shadowMatrix, -halfSize, -halfSize, 0.0F).method_22913(0.0F, 0.0F).method_22915(0.0F, 0.0F, 0.0F, 1.0F);
            shadowBuffer.method_22918(shadowMatrix, -halfSize, halfSize, 0.0F).method_22913(0.0F, 1.0F).method_22915(0.0F, 0.0F, 0.0F, 1.0F);
            shadowBuffer.method_22918(shadowMatrix, halfSize, halfSize, 0.0F).method_22913(1.0F, 1.0F).method_22915(0.0F, 0.0F, 0.0F, 1.0F);
            shadowBuffer.method_22918(shadowMatrix, halfSize, -halfSize, 0.0F).method_22913(1.0F, 0.0F).method_22915(0.0F, 0.0F, 0.0F, 1.0F);
            class_286.method_43437(shadowBuffer.method_60800());
            matrixStack.method_22909();
            matrixStack.method_22903();
            matrixStack.method_46416(0.0F, 0.0F, -0.1F);
            Matrix4f iconMatrix = matrixStack.method_23760().method_23761();
            class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1575);
            buffer.method_22918(iconMatrix, -halfSize, -halfSize, 0.0F).method_22913(0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.method_22918(iconMatrix, -halfSize, halfSize, 0.0F).method_22913(0.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.method_22918(iconMatrix, halfSize, halfSize, 0.0F).method_22913(1.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.method_22918(iconMatrix, halfSize, -halfSize, 0.0F).method_22913(1.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
            class_286.method_43437(buffer.method_60800());
            matrixStack.method_22909();
            float padding = 5.0F;
            matrixStack.method_46416(0.0F, halfSize + padding, 0.0F);
            float textScale = 0.6F;
            float textWidth = this.cachedDistanceText.length() * 4.5F;
            matrixStack.method_22904(-(textWidth * textScale) / 2.0, 0.0, 0.0);
            matrixStack.method_46416(shadowOffset, shadowOffset, 0.05F);
            this.renderText(matrixStack, this.cachedDistanceText, Color.BLACK, textScale);
            matrixStack.method_46416(-shadowOffset, -shadowOffset, -0.1F);
            this.renderText(matrixStack, this.cachedDistanceText, Color.WHITE, textScale);
            matrixStack.method_22909();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
         }
      }
   }

   private void renderText(class_4587 stack, String text, Color color, float scale) {
      Matrix4f mat = stack.method_23760().method_23761();
      Builder.text().font((MsdfFont)BIKO_FONT.get()).text(text).color(color).size(20.0F * scale).build().render(mat, 0.0F, 0.0F);
   }
}
