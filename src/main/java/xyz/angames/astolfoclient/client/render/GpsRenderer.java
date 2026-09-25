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
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexFormat;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.manager.GpsManager;

@Environment(EnvType.CLIENT)
public class GpsRenderer {
   private static final Supplier<MsdfFont> BIKO_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("biko").data("biko").build());
   private static final Identifier GPS_ICON = Identifier.of("astolfoclient", "textures/icons/gps.png");
   private String cachedDistanceText = "";
   private int lastDistanceInt = -1;

   public void render(WorldRenderContext context) {
      GpsManager manager = GpsManager.getInstance();
      if (manager.isActive()) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.world != null) {
            Vec3d cameraPos = context.camera().getPos();
            double targetX = manager.getTargetX();
            double targetZ = manager.getTargetZ();
            float tickDelta = context.tickCounter().getTickDelta(true);
            double playerX = MathHelper.lerp(tickDelta, client.player.prevX, client.player.getX());
            double playerY = MathHelper.lerp(tickDelta, client.player.prevY, client.player.getY());
            double playerZ = MathHelper.lerp(tickDelta, client.player.prevZ, client.player.getZ());
            double targetY = playerY + 2.0;
            double distSq = client.player.squaredDistanceTo(targetX, client.player.getY(), targetZ);
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
            MatrixStack matrixStack = context.matrixStack();
            matrixStack.push();
            matrixStack.translate(renderX - cameraPos.x, renderYFinal - cameraPos.y, renderZ - cameraPos.z);
            matrixStack.multiply(context.camera().getRotation());
            matrixStack.scale(1.0F, -1.0F, 1.0F);
            float fixedScale = 0.04F;
            matrixStack.scale(fixedScale, fixedScale, fixedScale);
            RenderSystem.setShaderTexture(0, GPS_ICON);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            Tessellator tessellator = Tessellator.getInstance();
            float size = 40.0F;
            float halfSize = size / 2.0F;
            float shadowOffset = 0.8F;
            matrixStack.push();
            matrixStack.translate(shadowOffset, shadowOffset, 0.05F);
            Matrix4f shadowMatrix = matrixStack.peek().getPositionMatrix();
            BufferBuilder shadowBuffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            shadowBuffer.vertex(shadowMatrix, -halfSize, -halfSize, 0.0F).texture(0.0F, 0.0F).color(0.0F, 0.0F, 0.0F, 1.0F);
            shadowBuffer.vertex(shadowMatrix, -halfSize, halfSize, 0.0F).texture(0.0F, 1.0F).color(0.0F, 0.0F, 0.0F, 1.0F);
            shadowBuffer.vertex(shadowMatrix, halfSize, halfSize, 0.0F).texture(1.0F, 1.0F).color(0.0F, 0.0F, 0.0F, 1.0F);
            shadowBuffer.vertex(shadowMatrix, halfSize, -halfSize, 0.0F).texture(1.0F, 0.0F).color(0.0F, 0.0F, 0.0F, 1.0F);
            BufferRenderer.draw(shadowBuffer.end());
            matrixStack.pop();
            matrixStack.push();
            matrixStack.translate(0.0F, 0.0F, -0.1F);
            Matrix4f iconMatrix = matrixStack.peek().getPositionMatrix();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(iconMatrix, -halfSize, -halfSize, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.vertex(iconMatrix, -halfSize, halfSize, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.vertex(iconMatrix, halfSize, halfSize, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.vertex(iconMatrix, halfSize, -halfSize, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
            BufferRenderer.draw(buffer.end());
            matrixStack.pop();
            float padding = 5.0F;
            matrixStack.translate(0.0F, halfSize + padding, 0.0F);
            float textScale = 0.6F;
            float textWidth = this.cachedDistanceText.length() * 4.5F;
            matrixStack.translate(-(textWidth * textScale) / 2.0, 0.0, 0.0);
            matrixStack.translate(shadowOffset, shadowOffset, 0.05F);
            this.renderText(matrixStack, this.cachedDistanceText, Color.BLACK, textScale);
            matrixStack.translate(-shadowOffset, -shadowOffset, -0.1F);
            this.renderText(matrixStack, this.cachedDistanceText, Color.WHITE, textScale);
            matrixStack.pop();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
         }
      }
   }

   private void renderText(MatrixStack stack, String text, Color color, float scale) {
      Matrix4f mat = stack.peek().getPositionMatrix();
      Builder.text().font((MsdfFont)BIKO_FONT.get()).text(text).color(color).size(20.0F * scale).build().render(mat, 0.0F, 0.0F);
   }
}
