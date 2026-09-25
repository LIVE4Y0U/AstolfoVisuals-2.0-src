package xyz.angames.astolfoclient.client.hud;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import dev.sxmurxy.mre.builders.Builder;
import dev.sxmurxy.mre.builders.states.QuadColorState;
import dev.sxmurxy.mre.builders.states.QuadRadiusState;
import dev.sxmurxy.mre.builders.states.SizeState;
import dev.sxmurxy.mre.msdf.MsdfFont;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
public class TestHudManager {
   private final MinecraftClient client = MinecraftClient.getInstance();
   private static final Supplier<MsdfFont> BIKO_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("biko").data("biko").build());
   public double x = 100.0;
   public double y = 100.0;
   public double width = 150.0;
   public double height = 100.0;
   private boolean isDragging = false;
   private double dragX;
   private double dragY;

   public void render(DrawContext context, float delta) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world != null) {
         Module testModule = AstolfoclientClient.moduleManager.getModuleByName("Test");
         boolean isModuleOn = testModule != null && testModule.isEnabled();
         if (isModuleOn) {
            float blurRad = 15.0F;
            float squirtVal = 7.0F;
            float distortVal = 0.08F;
            float fresnelPowerVal = 2.0F;
            float baseAlphaVal = 0.2F;
            float roundingVal = 7.0F;
            context.getMatrices().push();
            float scaleModifier = this.getScaleModifier();
            context.getMatrices().translate((float)this.x, (float)this.y, 0.0F);
            context.getMatrices().scale(scaleModifier, scaleModifier, 1.0F);
            context.getMatrices().translate((float)(-this.x), (float)(-this.y), 0.0F);
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            float x1 = (float)this.x;
            float y1 = (float)this.y;
            float w = (float)this.width;
            float h = (float)this.height;
            Builder.blur()
               .size(new SizeState(w, h))
               .radius(new QuadRadiusState(roundingVal))
               .blurRadius(blurRad)
               .color(new QuadColorState(Color.WHITE))
               .build()
               .render(matrix, x1, y1);
            float liquidGlassRadius = roundingVal * squirtVal / 2.0F;
            Builder.liquidGlass()
               .size(new SizeState(w + 2.0F, h + 2.0F))
               .radius(new QuadRadiusState(liquidGlassRadius))
               .color(new QuadColorState(Color.WHITE))
               .smoothness(0.5F, squirtVal)
               .alpha(1.0F, baseAlphaVal)
               .fresnel(fresnelPowerVal, Color.WHITE.getRGB(), 1.0F, 0.0F, true)
               .distortStrength(distortVal)
               .captureBackground()
               .build()
               .render(matrix, x1 - 1.0F, y1 - 1.0F);
            Builder.rectangle()
               .size(new SizeState(w, h))
               .radius(new QuadRadiusState(roundingVal))
               .color(new QuadColorState(new Color(30, 30, 30, 180)))
               .build()
               .render(matrix, x1, y1);
            String text = "67";
            float textWidth = ((MsdfFont)BIKO_FONT.get()).getWidth(text, 12.0F);
            Builder.text()
               .font((MsdfFont)BIKO_FONT.get())
               .text(text)
               .color(Color.WHITE)
               .size(12.0F)
               .build()
               .render(matrix, x1 + (w - textWidth) / 2.0F, y1 + (h - 12.0F) / 2.0F);
            context.getMatrices().pop();
         }
      }
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
      MinecraftClient mc = MinecraftClient.getInstance();
      Module testModule = AstolfoclientClient.moduleManager.getModuleByName("Test");
      boolean isModuleOn = testModule != null && testModule.isEnabled();
      if (!isModuleOn) {
         return false;
      } else {
         float scaleModifier = this.getScaleModifier();
         double effectiveW = this.width * scaleModifier;
         double effectiveH = this.height * scaleModifier;
         if (button == 0 && mouseX >= this.x && mouseX <= this.x + effectiveW && mouseY >= this.y && mouseY <= this.y + effectiveH) {
            this.isDragging = true;
            this.dragX = mouseX - this.x;
            this.dragY = mouseY - this.y;
            return true;
         } else {
            return false;
         }
      }
   }

   public void onMouseDragged(double mouseX, double mouseY, int button) {
      if (button == 0 && this.isDragging) {
         MinecraftClient mc = MinecraftClient.getInstance();
         float scaleModifier = this.getScaleModifier();
         float screenW = mc.getWindow().getScaledWidth();
         float screenH = mc.getWindow().getScaledHeight();
         float effectiveW = (float)(this.width * scaleModifier);
         float effectiveH = (float)(this.height * scaleModifier);
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
      return this.width;
   }

   public double getHeight() {
      return this.height;
   }
}
