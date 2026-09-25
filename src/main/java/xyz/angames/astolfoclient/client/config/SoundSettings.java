package xyz.angames.astolfoclient.client.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SoundSettings {
   private static boolean soundEnabled = true;
   private static float masterVolume = 100.0F;
   private static float guiOpenVolume = 100.0F;
   private static float categoryVolume = 100.0F;
   private static float moduleSelectVolume = 100.0F;
   private static float sliderVolume = 70.0F;
   private static float searchVolume = 100.0F;
   private static float modeOpenVolume = 100.0F;
   private static float moduleToggleVolume = 100.0F;

   public static boolean isSoundEnabled() {
      return soundEnabled;
   }

   public static void setSoundEnabled(boolean enabled) {
      soundEnabled = enabled;
   }

   public static float getMasterVolume() {
      return masterVolume;
   }

   public static void setMasterVolume(float volume) {
      masterVolume = MathHelper.clamp(volume, 0.0F, 100.0F);
   }

   public static float getGuiOpenVolume() {
      return guiOpenVolume;
   }

   public static void setGuiOpenVolume(float volume) {
      guiOpenVolume = MathHelper.clamp(volume, 0.0F, 100.0F);
   }

   public static float getCategoryVolume() {
      return categoryVolume;
   }

   public static void setCategoryVolume(float volume) {
      categoryVolume = MathHelper.clamp(volume, 0.0F, 100.0F);
   }

   public static float getModuleSelectVolume() {
      return moduleSelectVolume;
   }

   public static void setModuleSelectVolume(float volume) {
      moduleSelectVolume = MathHelper.clamp(volume, 0.0F, 100.0F);
   }

   public static float getSliderVolume() {
      return sliderVolume;
   }

   public static void setSliderVolume(float volume) {
      sliderVolume = MathHelper.clamp(volume, 0.0F, 100.0F);
   }

   public static float getSearchVolume() {
      return searchVolume;
   }

   public static void setSearchVolume(float volume) {
      searchVolume = MathHelper.clamp(volume, 0.0F, 100.0F);
   }

   public static float getModeOpenVolume() {
      return modeOpenVolume;
   }

   public static void setModeOpenVolume(float volume) {
      modeOpenVolume = MathHelper.clamp(volume, 0.0F, 100.0F);
   }

   public static float getModuleToggleVolume() {
      return moduleToggleVolume;
   }

   public static void setModuleToggleVolume(float volume) {
      moduleToggleVolume = MathHelper.clamp(volume, 0.0F, 100.0F);
   }
}
