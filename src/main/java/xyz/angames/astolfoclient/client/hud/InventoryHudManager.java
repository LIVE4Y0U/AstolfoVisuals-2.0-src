package xyz.angames.astolfoclient.client.hud;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
   private final MinecraftClient client = MinecraftClient.getInstance();
   private Module cachedModule;

   public void render(DrawContext context) {
      if (this.cachedModule == null) {
         this.cachedModule = AstolfoclientClient.moduleManager.getModuleByName("Interface");
      }

      boolean isEditing = this.client.currentScreen instanceof HudEditorScreen;
      InterfaceModule interfaceMod = (InterfaceModule)this.cachedModule;
      if (interfaceMod != null) {
         if (isEditing || interfaceMod.isEnabled() && interfaceMod.inventoryHud.get()) {
            if (this.client.player != null) {
               double currentGuiScale = this.client.getWindow().getScaleFactor();
               if (currentGuiScale <= 0.0) {
                  currentGuiScale = 2.0;
               }

               float scaleModifier = (float)(2.0 / currentGuiScale);
               context.getMatrices().push();
               context.getMatrices().translate(this.x, this.y, 0.0F);
               context.getMatrices().scale(scaleModifier, scaleModifier, 1.0F);
               context.getMatrices().translate(-this.x, -this.y, 0.0F);
               List<ItemStack> inventoryList = new ArrayList<>();
               boolean hasItems = false;

               for (int i = 0; i < 27; i++) {
                  ItemStack stack = (ItemStack)this.client.player.getInventory().main.get(9 + i);
                  inventoryList.add(stack);
                  if (!stack.isEmpty()) {
                     hasItems = true;
                  }
               }

               if (isEditing && !hasItems) {
                  inventoryList.set(0, new ItemStack(Items.DIAMOND_SWORD));
                  inventoryList.set(1, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 64));
                  inventoryList.set(2, new ItemStack(Items.ENDER_PEARL, 16));
                  inventoryList.set(3, new ItemStack(Items.COBWEB, 64));
                  inventoryList.set(4, new ItemStack(Items.OBSIDIAN, 64));
                  inventoryList.set(5, new ItemStack(Items.TOTEM_OF_UNDYING));
                  inventoryList.set(6, new ItemStack(Items.DIAMOND_PICKAXE));
                  inventoryList.set(7, new ItemStack(Items.BOW));
                  inventoryList.set(8, new ItemStack(Items.ARROW, 64));
               }

               context.getMatrices().push();
               context.getMatrices().translate(0.0F, 0.0F, 1.0F);

               for (int i = 0; i < 27; i++) {
                  ItemStack stack = inventoryList.get(i);
                  if (!stack.isEmpty()) {
                     int row = i / 9;
                     int col = i % 9;
                     int ix = (int)(this.x + col * 18);
                     int iy = (int)(this.y + row * 18);
                     context.drawItem(stack, ix, iy);
                     context.drawStackOverlay(this.client.textRenderer, stack, ix, iy);
                  }
               }

               context.getMatrices().pop();
               context.getMatrices().pop();
            }
         }
      }
   }

   public float getScaleModifier() {
      double currentGuiScale = this.client.getWindow().getScaleFactor();
      if (currentGuiScale <= 0.0) {
         currentGuiScale = 2.0;
      }

      return (float)(2.0 / currentGuiScale);
   }

   public boolean onMouseClicked(double mouseX, double mouseY, int button) {
      boolean isEditing = this.client.currentScreen instanceof HudEditorScreen;
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
         float screenW = this.client.getWindow().getScaledWidth();
         float screenH = this.client.getWindow().getScaledHeight();
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
