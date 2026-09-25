package xyz.angames.astolfoclient.client.module.modules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_6089;
import xyz.angames.astolfoclient.client.module.Module;
import xyz.angames.astolfoclient.client.module.setting.ModeSetting;

@Environment(EnvType.CLIENT)
public class FullBrightModule extends Module {
   public final ModeSetting mode = new ModeSetting("Mode", "Potion", "Potion", "Light");
   private final class_310 client = class_310.method_1551();
   private final Map<class_2338, class_2680> activeLights = new HashMap<>();
   private String lastMode = "";

   public FullBrightModule() {
      super("FullBright", Module.Category.RENDER);
      this.addSetting(this.mode);
   }

   @Override
   public void onEnable() {
      this.lastMode = this.mode.get();
      if (this.client.field_1724 != null && this.client.field_1687 != null && this.lastMode.equalsIgnoreCase("Potion")) {
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
      if (this.isEnabled() && this.client.field_1724 != null && this.client.field_1687 != null) {
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
            if (!this.client.field_1724.method_6059(class_1294.field_5925)) {
               this.applyEffect();
            }
         } else if (currentMode.equalsIgnoreCase("Light")) {
            this.removePotionEffect();
            this.updateDynamicLight();
         }
      }
   }

   private void updateDynamicLight() {
      if (this.client.field_1724 != null && this.client.field_1687 != null) {
         class_2338 playerPos = this.client.field_1724.method_24515();
         class_2338 centerPos = playerPos;
         class_2680 feetState = this.client.field_1687.method_8320(playerPos);
         if (!feetState.method_26215() && !feetState.method_27852(class_2246.field_31037)) {
            centerPos = playerPos.method_10084();
         }

         Map<class_2338, Integer> targetLights = new HashMap<>();
         int radius = 4;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  if (dist <= radius) {
                     int level = 15 - (int)Math.round(dist * 3.2);
                     if (level >= 1) {
                        class_2338 pos = centerPos.method_10069(dx, dy, dz);
                        targetLights.put(pos, level);
                     }
                  }
               }
            }
         }

         Iterator<Entry<class_2338, class_2680>> iterator = this.activeLights.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<class_2338, class_2680> entry = iterator.next();
            class_2338 pos = entry.getKey();
            if (!targetLights.containsKey(pos)) {
               class_2680 current = this.client.field_1687.method_8320(pos);
               if (current.method_27852(class_2246.field_31037)) {
                  this.client.field_1687.method_8652(pos, entry.getValue(), 2);
               }

               iterator.remove();
            }
         }

         for (Entry<class_2338, Integer> entry : targetLights.entrySet()) {
            class_2338 pos = entry.getKey();
            int level = entry.getValue();
            class_2680 current = this.client.field_1687.method_8320(pos);
            if (this.activeLights.containsKey(pos)) {
               if (current.method_27852(class_2246.field_31037)) {
                  int currentLevel = (Integer)current.method_11654(class_6089.field_31187);
                  if (currentLevel != level) {
                     this.client.field_1687.method_8652(pos, (class_2680)current.method_11657(class_6089.field_31187, level), 2);
                  }
               } else {
                  this.activeLights.remove(pos);
               }
            } else if (current.method_26215()) {
               this.activeLights.put(pos.method_10062(), current);
               this.client.field_1687.method_8652(pos, (class_2680)class_2246.field_31037.method_9564().method_11657(class_6089.field_31187, level), 2);
            }
         }
      }
   }

   private void clearLight() {
      for (Entry<class_2338, class_2680> entry : this.activeLights.entrySet()) {
         class_2338 pos = entry.getKey();
         class_2680 current = this.client.field_1687.method_8320(pos);
         if (current.method_27852(class_2246.field_31037)) {
            this.client.field_1687.method_8652(pos, entry.getValue(), 2);
         }
      }

      this.activeLights.clear();
   }

   private void applyEffect() {
      int longDuration = -1;
      class_1293 nightVisionEffect = new class_1293(class_1294.field_5925, longDuration, 0, false, false, true);
      this.client.field_1724.method_6092(nightVisionEffect);
   }

   private void removePotionEffect() {
      if (this.client.field_1724 != null && this.client.field_1724.method_6059(class_1294.field_5925)) {
         this.client.field_1724.method_6016(class_1294.field_5925);
      }
   }
}
