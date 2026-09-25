package xyz.angames.astolfoclient.client.module.modules.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.BooleanSetting;

@Environment(EnvType.CLIENT)
public class NoRenderModule extends Module {
   private static NoRenderModule instance;
   public final BooleanSetting fire = new BooleanSetting("Fire Overlay", true);
   public final BooleanSetting totem = new BooleanSetting("Totem Pop", true);
   public final BooleanSetting scoreboard = new BooleanSetting("Scoreboard", false);
   public final BooleanSetting bossbar = new BooleanSetting("Bossbar", false);
   public final BooleanSetting hurtCam = new BooleanSetting("Hurt Camera", true);
   public final BooleanSetting pumpkin = new BooleanSetting("Pumpkin Blur", true);
   public final BooleanSetting portal = new BooleanSetting("Portal Nausea", true);
   public final BooleanSetting blindness = new BooleanSetting("Blindness", true);
   public final BooleanSetting blockOverlay = new BooleanSetting("Block Overlay", true);
   public final BooleanSetting grass = new BooleanSetting("Grass", false);
   private boolean lastGrassState = false;

   public NoRenderModule() {
      super("NoRender", Module.Category.RENDER);
      this.addSetting(this.fire);
      this.addSetting(this.totem);
      this.addSetting(this.scoreboard);
      this.addSetting(this.bossbar);
      this.addSetting(this.hurtCam);
      this.addSetting(this.pumpkin);
      this.addSetting(this.portal);
      this.addSetting(this.blindness);
      this.addSetting(this.blockOverlay);
      this.addSetting(this.grass);
      instance = this;
   }

   public static NoRenderModule getInstance() {
      return instance;
   }

   @Override
   public void onEnable() {
      if (this.grass.get()) {
         this.reloadWorldRenderer();
      }

      this.lastGrassState = this.grass.get();
   }

   @Override
   public void onDisable() {
      if (this.lastGrassState) {
         this.reloadWorldRenderer();
      }

      this.lastGrassState = false;
   }

   @Override
   public void onTick() {
      if (this.grass.get() != this.lastGrassState) {
         this.lastGrassState = this.grass.get();
         this.reloadWorldRenderer();
      }
   }

   private void reloadWorldRenderer() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1769 != null) {
         mc.field_1769.method_3279();
      }
   }

   public static boolean isGrass(class_2680 state) {
      if (state == null) {
         return false;
      }

      class_2248 block = state.method_26204();
      return block == class_2246.field_10479
         || block == class_2246.field_10214
         || block == class_2246.field_10112
         || block == class_2246.field_10313
         || block == class_2246.field_10376
         || block == class_2246.field_10238
         || block == class_2246.field_10428
         || block == class_2246.field_28686
         || block == class_2246.field_22117
         || block == class_2246.field_22125
         || block == class_2246.field_22116;
   }
}
