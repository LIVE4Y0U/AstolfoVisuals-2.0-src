package xyz.angames.astolfoclient.client.hud;

import dev.sxmurxy.mre.builders.Builder;
import dev.sxmurxy.mre.builders.states.QuadColorState;
import dev.sxmurxy.mre.builders.states.QuadRadiusState;
import dev.sxmurxy.mre.builders.states.SizeState;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;
import org.joml.Matrix4f;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.gui.clickgui.GuiUtils;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
public class CustomHotbarManager {
   private final class_310 client = class_310.method_1551();
   private Module cachedModule;
   private static final float SLOT_SIZE = 26.0F;
   private static final float SPACING = 4.0F;
   private static final int SLOTS = 9;
   private static final float PADDING = 0.0F;
   private static final float HOTBAR_WIDTH = 266.0F;
   private static final float HOTBAR_HEIGHT = 26.0F;
   private final SizeState mainSizeState = new SizeState(266.0F, 26.0F);
   private final SizeState singleSlotSizeState = new SizeState(26.0F, 26.0F);
   private final QuadRadiusState commonRadiusState = new QuadRadiusState(7.0F);
   private static final Color BG_COLOR_OBJ = new Color(10, 10, 12, 220);
   private static final Color BLUR_COLOR_OBJ = new Color(0, 0, 0, 120);
   private final QuadColorState bgSolidState = new QuadColorState(BG_COLOR_OBJ);
   private final QuadColorState bgBlurState = new QuadColorState(BLUR_COLOR_OBJ);
   private float animatedSlotPosition = 0.0F;
   private long lastUpdateTimeNs = -1L;

   public void render(class_332 context, float tickDelta) {
      if (this.cachedModule == null) {
         this.cachedModule = AstolfoclientClient.moduleManager.getModuleByName("CustomHotbar");
      }

      if (this.cachedModule != null && this.cachedModule.isEnabled()) {
         class_746 player = this.client.field_1724;
         if (player != null && !this.client.field_1690.field_1842 && !player.method_7325()) {
            Matrix4f matrix = context.method_51448().method_23760().method_23761();
            int screenWidth = context.method_51421();
            int screenHeight = context.method_51443();
            context.method_51448().method_22903();
            float scaleModifier = this.getScaleModifier();
            context.method_51448().method_22905(scaleModifier, scaleModifier, 1.0F);
            float scaledWidth = screenWidth / scaleModifier;
            float scaledHeight = screenHeight / scaleModifier;
            float startX = (scaledWidth - 266.0F) / 2.0F;
            float startY = scaledHeight - 26.0F - 5.0F - LogoRenderer.getHotbarYOffset() / scaleModifier;
            long now = System.currentTimeMillis();
            Color themeColor = new Color(ThemeManager.getThemedColor(now / 10L));
            int darkR = (int)(themeColor.getRed() * 0.2F);
            int darkG = (int)(themeColor.getGreen() * 0.2F);
            int darkB = (int)(themeColor.getBlue() * 0.2F);
            Color topGradColor = new Color(darkR, darkG, darkB, 220);
            Color bottomGradColor = new Color(0, 0, 0, 220);
            float wave = (float)(Math.sin(now / 1000.0 * 2.0) * 0.5 + 0.5);
            Color topColor = GuiUtils.interpolateColor(bottomGradColor, topGradColor, wave);
            Color bottomColor = GuiUtils.interpolateColor(bottomGradColor, topGradColor, 1.0F - wave);
            this.renderShadow(matrix, startX, startY, 266.0F, 26.0F, 7.0F, 1.0F);
            Builder.rectangle()
               .size(this.mainSizeState)
               .radius(this.commonRadiusState)
               .color(new QuadColorState(topColor, bottomColor, bottomColor, topColor))
               .build()
               .render(matrix, startX, startY);
            int currentSlot = player.method_31548().field_7545;
            long nowNs = System.nanoTime();
            if (this.lastUpdateTimeNs == -1L) {
               this.lastUpdateTimeNs = nowNs;
            }

            long elapsedNs = nowNs - this.lastUpdateTimeNs;
            this.lastUpdateTimeNs = nowNs;
            double deltaSeconds = elapsedNs / 1.0E9;
            if (deltaSeconds > 0.1) {
               deltaSeconds = 0.1;
            }

            float interpolationSpeed = 15.0F;
            this.animatedSlotPosition = this.animatedSlotPosition
               + (currentSlot - this.animatedSlotPosition) * (float)(1.0 - Math.exp(-interpolationSpeed * deltaSeconds));
            float selectionX = startX + 0.0F + this.animatedSlotPosition * 30.0F;
            float selectionY = startY + 0.0F;
            Color faintFill = new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 50);
            Builder.border()
               .size(this.singleSlotSizeState)
               .radius(this.commonRadiusState)
               .color(new QuadColorState(themeColor))
               .thickness(2.0F)
               .build()
               .render(matrix, selectionX, selectionY);
            Builder.rectangle()
               .size(this.singleSlotSizeState)
               .radius(this.commonRadiusState)
               .color(new QuadColorState(faintFill))
               .build()
               .render(matrix, selectionX, selectionY);
            float itemBaseY = startY + 0.0F + 5.0F;

            for (int i = 0; i < 9; i++) {
               class_1799 stack = (class_1799)player.method_31548().field_7547.get(i);
               if (!stack.method_7960()) {
                  float itemX = startX + 0.0F + i * 30.0F + 5.0F;
                  this.renderItem(context, stack, (int)itemX, (int)itemBaseY);
               }
            }

            class_1799 offhandStack = player.method_6079();
            if (!offhandStack.method_7960()) {
               float offhandX = startX - 26.0F - 8.0F;
               float offhandY = startY;
               this.renderShadow(matrix, offhandX, offhandY, 26.0F, 26.0F, 7.0F, 1.0F);
               Builder.rectangle()
                  .size(this.singleSlotSizeState)
                  .radius(this.commonRadiusState)
                  .color(new QuadColorState(topColor, bottomColor, bottomColor, topColor))
                  .build()
                  .render(matrix, offhandX, offhandY);
               Builder.border()
                  .size(this.singleSlotSizeState)
                  .radius(this.commonRadiusState)
                  .color(new QuadColorState(themeColor))
                  .thickness(1.5F)
                  .build()
                  .render(matrix, offhandX, offhandY);
               this.renderItem(context, offhandStack, (int)(offhandX + 0.0F + 5.0F), (int)(offhandY + 0.0F + 5.0F));
            }

            context.method_51448().method_22909();
         }
      }
   }

   private void renderItem(class_332 context, class_1799 stack, int x, int y) {
      context.method_51427(stack, x, y);
      context.method_51431(this.client.field_1772, stack, x, y);
   }

   private void renderShadow(Matrix4f matrix, float x, float y, float w, float h, float radius, float masterAlpha) {
      int layers = 6;
      float maxSpread = 5.0F;

      for (int i = layers; i > 0; i--) {
         float progress = (float)i / layers;
         float fade = 1.0F - progress;
         float alpha = fade * fade * 0.4F * masterAlpha;
         int alphaInt = Math.max(0, Math.min(255, (int)(255.0F * alpha)));
         if (alphaInt > 0) {
            float expand = progress * maxSpread;
            Color shadowColor = new Color(0, 0, 0, alphaInt);
            Builder.rectangle()
               .size(new SizeState(w + expand * 2.0F, h + expand * 2.0F))
               .radius(new QuadRadiusState(radius + expand))
               .color(new QuadColorState(shadowColor))
               .build()
               .render(matrix, x - expand, y - expand);
         }
      }
   }

   private float getScaleModifier() {
      class_310 mc = class_310.method_1551();
      double currentGuiScale = mc.method_22683().method_4495();
      float baseScale = 0.8F;
      float referenceWidth = 2560.0F;
      float screenWidth = mc.method_22683().method_4489();
      float resolutionScale = screenWidth / referenceWidth;
      resolutionScale = Math.max(0.5F, Math.min(1.5F, resolutionScale));
      return (float)(2.0 * baseScale * resolutionScale / currentGuiScale);
   }
}
