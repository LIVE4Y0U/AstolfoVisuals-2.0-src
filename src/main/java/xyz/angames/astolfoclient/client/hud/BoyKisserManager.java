package xyz.angames.astolfoclient.client.hud;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BoyKisserManager {
   private final MinecraftClient client = MinecraftClient.getInstance();
   private final List<Identifier> frames = new ArrayList<>();
   private int currentFrame = 0;
   private long lastFrameTime = 0L;
   private final int frameDelay = 100;
   private double x = 10.0;
   private double y = 50.0;
   private final double width = 64.0;
   private final double height = 64.0;
   private boolean isDragging = false;
   private double dragX;
   private double dragY;

   public BoyKisserManager() {
      int frameCount = 52;

      for (int i = 0; i < frameCount; i++) {
         this.frames.add(Identifier.of("astolfoclient", "textures/gui/boikiser/boykisser_" + i + ".png"));
      }
   }

   public void render(DrawContext context, float delta) {
      long now = (long)(System.nanoTime() / 1000000.0);
      if (now - this.lastFrameTime > 100L) {
         this.currentFrame = (this.currentFrame + 1) % this.frames.size();
         this.lastFrameTime = now;
      }

      Identifier currentTexture = this.frames.get(this.currentFrame);
      context.getMatrices().push();
      float scaleModifier = this.getScaleModifier();
      context.getMatrices().translate((float)this.x, (float)this.y, 0.0F);
      context.getMatrices().scale(scaleModifier, scaleModifier, 1.0F);
      context.getMatrices().translate((float)(-this.x), (float)(-this.y), 0.0F);
      VertexConsumerProvider.Immediate provider = this.client.getBufferBuilders().getEntityVertexConsumers();
      Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
      VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getText(currentTexture));
      int light = 15728880;
      float x1 = (float)this.x;
      float y1 = (float)this.y;
      float x2 = (float)(this.x + 64.0);
      float y2 = (float)(this.y + 64.0);
      float z = 0.0F;
      vertexConsumer.vertex(matrix, x1, y2, z).color(255, 255, 255, 255).texture(0.0F, 1.0F).light(light);
      vertexConsumer.vertex(matrix, x2, y2, z).color(255, 255, 255, 255).texture(1.0F, 1.0F).light(light);
      vertexConsumer.vertex(matrix, x2, y1, z).color(255, 255, 255, 255).texture(1.0F, 0.0F).light(light);
      vertexConsumer.vertex(matrix, x1, y1, z).color(255, 255, 255, 255).texture(0.0F, 0.0F).light(light);
      provider.draw();
      context.getMatrices().pop();
   }

   public float getScaleModifier() {
      MinecraftClient mc = MinecraftClient.getInstance();
      double currentGuiScale = mc.getWindow().getScaleFactor();
      if (currentGuiScale <= 0.0) {
         currentGuiScale = 2.0;
      }

      return (float)(2.0 / currentGuiScale);
   }

   public boolean onMouseClicked(double mouseX, double mouseY, int button) {
      float scaleModifier = this.getScaleModifier();
      double effectiveW = 64.0 * scaleModifier;
      double effectiveH = 64.0 * scaleModifier;
      if (button == 0 && mouseX >= this.x && mouseX <= this.x + effectiveW && mouseY >= this.y && mouseY <= this.y + effectiveH) {
         this.isDragging = true;
         this.dragX = mouseX - this.x;
         this.dragY = mouseY - this.y;
         return true;
      } else {
         return false;
      }
   }

   public void onMouseDragged(double mouseX, double mouseY, int button) {
      if (button == 0 && this.isDragging) {
         float scaleModifier = this.getScaleModifier();
         float screenW = this.client.getWindow().getScaledWidth();
         float screenH = this.client.getWindow().getScaledHeight();
         float effectiveW = (float)(64.0 * scaleModifier);
         float effectiveH = (float)(64.0 * scaleModifier);
         this.x = Math.max(0.0, Math.min(Math.max(0.0F, screenW - effectiveW), mouseX - this.dragX));
         this.y = Math.max(0.0, Math.min(Math.max(0.0F, screenH - effectiveH), mouseY - this.dragY));
      }
   }

   public void onMouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.isDragging = false;
      }
   }

   public void onMouseReleased(int button) {
      if (button == 0) {
         this.isDragging = false;
      }
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public void setX(double x) {
      this.x = x;
   }

   public void setY(double y) {
      this.y = y;
   }

   public double getWidth() {
      return 64.0;
   }

   public double getHeight() {
      return 64.0;
   }
}
