package xyz.angames.astolfoclient.client.module.modules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.LightBlock;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.ModeSetting;

@Environment(EnvType.CLIENT)
public class FullBrightModule extends Module {
   public final ModeSetting mode = new ModeSetting("Mode", "Potion", "Potion", "Light");
   private final minecraft.client.MinecraftClient client = minecraft.client.MinecraftClient.getInstance();
   private final Map<util.math.BlockPos, minecraft.block.BlockState> activeLights = new HashMap<>();
   private String lastMode = "";

   public FullBrightModule() {
      super("FullBright", Module.Category.RENDER);
      this.addSetting(this.mode);
   }

   @Override
   public void onEnable() {
      this.lastMode = this.mode.get();
      if (this.client.player != null && this.client.world != null && this.lastMode.equalsIgnoreCase("Potion")) {
         this.applyEffect();
      }
   }

   @Override
   public void onDisable() {
      this.removePotionEffect();
      this.clearLight();
      this.lastMode = "";
   }

   @Override
   public void onTick() {
      if (this.isEnabled() && this.client.player != null && this.client.world != null) {
         String currentMode = this.mode.get();
         if (!currentMode.equalsIgnoreCase(this.lastMode)) {
            if (this.lastMode.equalsIgnoreCase("Potion")) {
               this.removePotionEffect();
            } else if (this.lastMode.equalsIgnoreCase("Light")) {
               this.clearLight();
            }

            this.lastMode = currentMode;
         }

         if (currentMode.equalsIgnoreCase("Potion")) {
            if (!this.client.player.hasStatusEffect(entity.effect.StatusEffects.NIGHT_VISION)) {
               this.applyEffect();
            }
         } else if (currentMode.equalsIgnoreCase("Light")) {
            this.removePotionEffect();
            this.updateDynamicLight();
         }
      }
   }

   private void updateDynamicLight() {
      if (this.client.player != null && this.client.world != null) {
         util.math.BlockPos playerPos = this.client.player.getBlockPos();
         util.math.BlockPos centerPos = playerPos;
         minecraft.block.BlockState feetState = this.client.world.getBlockState(playerPos);
         if (!feetState.isAir() && !feetState.isOf(minecraft.block.Blocks.LIGHT)) {
            centerPos = playerPos.up();
         }

         Map<util.math.BlockPos, Integer> targetLights = new HashMap<>();
         int radius = 4;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  if (dist <= radius) {
                     int level = 15 - (int)Math.round(dist * 3.2);
                     if (level >= 1) {
                        util.math.BlockPos pos = centerPos.add(dx, dy, dz);
                        targetLights.put(pos, level);
                     }
                  }
               }
            }
         }

         Iterator<Entry<util.math.BlockPos, minecraft.block.BlockState>> iterator = this.activeLights.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<util.math.BlockPos, minecraft.block.BlockState> entry = iterator.next();
            util.math.BlockPos pos = entry.getKey();
            if (!targetLights.containsKey(pos)) {
               minecraft.block.BlockState current = this.client.world.getBlockState(pos);
               if (current.isOf(minecraft.block.Blocks.LIGHT)) {
                  this.client.world.setBlockState(pos, entry.getValue(), 2);
               }

               iterator.remove();
            }
         }

         for (Entry<util.math.BlockPos, Integer> entry : targetLights.entrySet()) {
            util.math.BlockPos pos = entry.getKey();
            int level = entry.getValue();
            minecraft.block.BlockState current = this.client.world.getBlockState(pos);
            if (this.activeLights.containsKey(pos)) {
               if (current.isOf(minecraft.block.Blocks.LIGHT)) {
                  int currentLevel = (Integer)current.get(minecraft.block.LightBlock.LEVEL_15);
                  if (currentLevel != level) {
                     this.client.world.setBlockState(pos, (minecraft.block.BlockState)current.with(minecraft.block.LightBlock.LEVEL_15, level), 2);
                  }
               } else {
                  this.activeLights.remove(pos);
               }
            } else if (current.isAir()) {
               this.activeLights.put(pos.toImmutable(), current);
               this.client.world.setBlockState(pos, (minecraft.block.BlockState)minecraft.block.Blocks.LIGHT.getDefaultState().with(minecraft.block.LightBlock.LEVEL_15, level), 2);
            }
         }
      }
   }

   private void clearLight() {
      for (Entry<util.math.BlockPos, minecraft.block.BlockState> entry : this.activeLights.entrySet()) {
         util.math.BlockPos pos = entry.getKey();
         minecraft.block.BlockState current = this.client.world.getBlockState(pos);
         if (current.isOf(minecraft.block.Blocks.LIGHT)) {
            this.client.world.setBlockState(pos, entry.getValue(), 2);
         }
      }

      this.activeLights.clear();
   }

   private void applyEffect() {
      int longDuration = -1;
      entity.effect.StatusEffectInstance nightVisionEffect = new entity.effect.StatusEffectInstance(entity.effect.StatusEffects.NIGHT_VISION, longDuration, 0, false, false, true);
      this.client.player.addStatusEffect(nightVisionEffect);
   }

   private void removePotionEffect() {
      if (this.client.player != null && this.client.player.hasStatusEffect(entity.effect.StatusEffects.NIGHT_VISION)) {
         this.client.player.removeStatusEffect(entity.effect.StatusEffects.NIGHT_VISION);
      }
   }
}
