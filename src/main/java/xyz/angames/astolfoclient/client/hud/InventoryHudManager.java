package xyz.angames.astolfoclient.client.hud;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.gui.HudEditorScreen;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.modules.render.InterfaceModule;

@Environment(EnvType.CLIENT)
public class InventoryHudManager {
   public float x = 20.0F;
   public float y = 300.0F;
   private static final int SLOT_SIZE = 18;
   private static final float WIDTH = 162.0F;
   private static final float HEIGHT = 54.0F;
   private boolean dragging = false;
   private float dragOffsetX;
   private float dragOffsetY;
   private final class_310 client = class_310.method_1551();
   private Module cachedModule;

   public void render(class_332 context) {
      if (this.cachedModule == null) {
         this.cachedModule = AstolfoclientClient.moduleManager.getModuleByName("Interface");
      }

      boolean isEditing = this.client.field_1755 instanceof HudEditorScreen;
      InterfaceModule interfaceMod = (InterfaceModule)this.cachedModule;
      if (interfaceMod != null) {
         if (isEditing || interfaceMod.isEnabled() && interfaceMod.inventoryHud.get()) {
            if (this.client.field_1724 != null) {
               double currentGuiScale = this.client.method_22683().method_4495();
               if (currentGuiScale <= 0.0) {
                  currentGuiScale = 2.0;
               }

               float scaleModifier = (float)(2.0 / currentGuiScale);
               context.method_51448().method_22903();
               context.method_51448().method_46416(this.x, this.y, 0.0F);
               context.method_51448().method_22905(scaleModifier, scaleModifier, 1.0F);
               context.method_51448().method_46416(-this.x, -this.y, 0.0F);
               List<class_1799> inventoryList = new ArrayList<>();
               boolean hasItems = false;

               for (int i = 0; i < 27; i++) {
                  class_1799 stack = (class_1799)this.client.field_1724.method_31548().field_7547.get(9 + i);
                  inventoryList.add(stack);
                  if (!stack.method_7960()) {
                     hasItems = true;
                  }
               }

               if (isEditing && !hasItems) {
                  inventoryList.set(0, new class_1799(class_1802.field_8802));
                  inventoryList.set(1, new class_1799(class_1802.field_8367, 64));
                  inventoryList.set(2, new class_1799(class_1802.field_8634, 16));
                  inventoryList.set(3, new class_1799(class_1802.field_8786, 64));
                  inventoryList.set(4, new class_1799(class_1802.field_8281, 64));
                  inventoryList.set(5, new class_1799(class_1802.field_8288));
                  inventoryList.set(6, new class_1799(class_1802.field_8377));
                  inventoryList.set(7, new class_1799(class_1802.field_8102));
                  inventoryList.set(8, new class_1799(class_1802.field_8107, 64));
               }

               context.method_51448().method_22903();
               context.method_51448().method_46416(0.0F, 0.0F, 1.0F);

               for (int i = 0; i < 27; i++) {
                  class_1799 stack = inventoryList.get(i);
                  if (!stack.method_7960()) {
                     int row = i / 9;
                     int col = i % 9;
                     int ix = (int)(this.x + col * 18);
                     int iy = (int)(this.y + row * 18);
                     context.method_51427(stack, ix, iy);
                     context.method_51431(this.client.field_1772, stack, ix, iy);
                  }
               }

               context.method_51448().method_22909();
               context.method_51448().method_22909();
            }
         }
      }
   }

   public float getScaleModifier() {
      double currentGuiScale = this.client.method_22683().method_4495();
      if (currentGuiScale <= 0.0) {
         currentGuiScale = 2.0;
      }

      return (float)(2.0 / currentGuiScale);
   }

   public boolean onMouseClicked(double mouseX, double mouseY, int button) {
      boolean isEditing = this.client.field_1755 instanceof HudEditorScreen;
      InterfaceModule interfaceMod = (InterfaceModule)this.cachedModule;
      if (interfaceMod == null) {
         return false;
      }

      if (isEditing || interfaceMod.isEnabled() && interfaceMod.inventoryHud.get()) {
         float scaleModifier = this.getScaleModifier();
         float effectiveW = 162.0F * scaleModifier;
         float effectiveH = 54.0F * scaleModifier;
         if (button == 0 && mouseX >= this.x && mouseX <= this.x + effectiveW && mouseY >= this.y && mouseY <= this.y + effectiveH) {
            this.dragging = true;
            this.dragOffsetX = (float)(mouseX - this.x);
            this.dragOffsetY = (float)(mouseY - this.y);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void onMouseDragged(double mouseX, double mouseY, int button) {
      if (this.dragging && button == 0) {
         float scaleModifier = this.getScaleModifier();
         float screenW = this.client.method_22683().method_4486();
         float screenH = this.client.method_22683().method_4502();
         float effectiveW = 162.0F * scaleModifier;
         float effectiveH = 54.0F * scaleModifier;
         float targetX = (float)(mouseX - this.dragOffsetX);
         float targetY = (float)(mouseY - this.dragOffsetY);
         this.x = Math.max(0.0F, Math.min(Math.max(0.0F, screenW - effectiveW), targetX));
         this.y = Math.max(0.0F, Math.min(Math.max(0.0F, screenH - effectiveH), targetY));
      }
   }

   public void onMouseReleased(int button) {
      if (this.dragging && button == 0) {
         this.dragging = false;
      }
   }

   public void onMouseReleased(double mouseX, double mouseY, int button) {
      if (this.dragging && button == 0) {
         this.dragging = false;
      }
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public void setX(float x) {
      this.x = x;
   }

   public void setY(float y) {
      this.y = y;
   }

   public float getWidth() {
      return 162.0F;
   }

   public float getHeight() {
      return 54.0F;
   }
}
