package xyz.angames.astolfoclient.client.hud;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4588;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class BoyKisserManager {
   private final class_310 client = class_310.method_1551();
   private final List<class_2960> frames = new ArrayList<>();
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
         this.frames.add(class_2960.method_60655("astolfoclient", "textures/gui/boikiser/boykisser_" + i + ".png"));
      }
   }

   public void render(class_332 context, float delta) {
      long now = (long)(System.nanoTime() / 1000000.0);
      if (now - this.lastFrameTime > 100L) {
         this.currentFrame = (this.currentFrame + 1) % this.frames.size();
         this.lastFrameTime = now;
      }

      class_2960 currentTexture = this.frames.get(this.currentFrame);
      context.method_51448().method_22903();
      float scaleModifier = this.getScaleModifier();
      context.method_51448().method_46416((float)this.x, (float)this.y, 0.0F);
      context.method_51448().method_22905(scaleModifier, scaleModifier, 1.0F);
      context.method_51448().method_46416((float)(-this.x), (float)(-this.y), 0.0F);
      class_4598 provider = this.client.method_22940().method_23000();
      Matrix4f matrix = context.method_51448().method_23760().method_23761();
      class_4588 vertexConsumer = provider.getBuffer(class_1921.method_23028(currentTexture));
      int light = 15728880;
      float x1 = (float)this.x;
      float y1 = (float)this.y;
      float x2 = (float)(this.x + 64.0);
      float y2 = (float)(this.y + 64.0);
      float z = 0.0F;
      vertexConsumer.method_22918(matrix, x1, y2, z).method_1336(255, 255, 255, 255).method_22913(0.0F, 1.0F).method_60803(light);
      vertexConsumer.method_22918(matrix, x2, y2, z).method_1336(255, 255, 255, 255).method_22913(1.0F, 1.0F).method_60803(light);
      vertexConsumer.method_22918(matrix, x2, y1, z).method_1336(255, 255, 255, 255).method_22913(1.0F, 0.0F).method_60803(light);
      vertexConsumer.method_22918(matrix, x1, y1, z).method_1336(255, 255, 255, 255).method_22913(0.0F, 0.0F).method_60803(light);
      provider.method_22993();
      context.method_51448().method_22909();
   }

   public float getScaleModifier() {
      class_310 mc = class_310.method_1551();
      double currentGuiScale = mc.method_22683().method_4495();
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
         float screenW = this.client.method_22683().method_4486();
         float screenH = this.client.method_22683().method_4502();
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
