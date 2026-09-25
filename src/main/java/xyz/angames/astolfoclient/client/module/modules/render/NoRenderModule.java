package xyz.angames.astolfoclient.client.module.modules.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
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
      minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
      if (mc.worldRenderer != null) {
         mc.worldRenderer.reload();
      }
   }

   public static boolean isGrass(minecraft.block.BlockState state) {
      if (state == null) {
         return false;
      }

      minecraft.block.Block block = state.getBlock();
      return block == minecraft.block.Blocks.SHORT_GRASS
         || block == minecraft.block.Blocks.TALL_GRASS
         || block == minecraft.block.Blocks.FERN
         || block == minecraft.block.Blocks.LARGE_FERN
         || block == minecraft.block.Blocks.SEAGRASS
         || block == minecraft.block.Blocks.TALL_SEAGRASS
         || block == minecraft.block.Blocks.DEAD_BUSH
         || block == minecraft.block.Blocks.HANGING_ROOTS
         || block == minecraft.block.Blocks.NETHER_SPROUTS
         || block == minecraft.block.Blocks.CRIMSON_ROOTS
         || block == minecraft.block.Blocks.WARPED_ROOTS;
   }
}
