package xyz.angames.astolfoclient.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1109;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_7923;
import xyz.angames.astolfoclient.client.config.SoundSettings;

@Environment(EnvType.CLIENT)
public class ModSounds {
   public static final class_2960 ENABLE_ID = class_2960.method_60655("astolfoclient", "module_enable");
   public static final class_3414 ENABLE_SOUND = class_3414.method_47908(ENABLE_ID);
   public static final class_2960 DISABLE_ID = class_2960.method_60655("astolfoclient", "module_disable");
   public static final class_3414 DISABLE_SOUND = class_3414.method_47908(DISABLE_ID);
   public static final class_2960 CRASH_DETECTION_ID = class_2960.method_60655("astolfoclient", "crash_detection");
   public static final class_3414 CRASH_DETECTION_SOUND = class_3414.method_47908(CRASH_DETECTION_ID);
   public static final class_2960 CRASH_DETECTION_HYPHEN_ID = class_2960.method_60655("astolfoclient", "crash-detection");
   public static final class_3414 CRASH_DETECTION_HYPHEN_SOUND = class_3414.method_47908(CRASH_DETECTION_HYPHEN_ID);
   public static final class_2960 GUI_OPEN_ID = class_2960.method_60655("astolfoclient", "clickgui_open");
   public static final class_3414 GUI_OPEN_SOUND = class_3414.method_47908(GUI_OPEN_ID);
   public static final class_2960 CATEGORY_ID = class_2960.method_60655("astolfoclient", "clickgui_category");
   public static final class_3414 CATEGORY_SOUND = class_3414.method_47908(CATEGORY_ID);
   public static final class_2960 MODULE_SELECT_ID = class_2960.method_60655("astolfoclient", "clickgui_module");
   public static final class_3414 MODULE_SELECT_SOUND = class_3414.method_47908(MODULE_SELECT_ID);
   public static final class_2960 SLIDER_MOVING_ID = class_2960.method_60655("astolfoclient", "clickgui_slider");
   public static final class_3414 SLIDER_MOVING_SOUND = class_3414.method_47908(SLIDER_MOVING_ID);
   public static final class_2960 SEARCH_CLICK_ID = class_2960.method_60655("astolfoclient", "clickgui_search");
   public static final class_3414 SEARCH_CLICK_SOUND = class_3414.method_47908(SEARCH_CLICK_ID);
   public static final class_2960 MODE_OPEN_ID = class_2960.method_60655("astolfoclient", "clickgui_mode_open");
   public static final class_3414 MODE_OPEN_SOUND = class_3414.method_47908(MODE_OPEN_ID);
   private static long lastSliderSoundTime = 0L;

   public static void register() {
      registerSound(ENABLE_ID, ENABLE_SOUND);
      registerSound(DISABLE_ID, DISABLE_SOUND);
      registerSound(CRASH_DETECTION_ID, CRASH_DETECTION_SOUND);
      registerSound(CRASH_DETECTION_HYPHEN_ID, CRASH_DETECTION_HYPHEN_SOUND);
      registerSound(GUI_OPEN_ID, GUI_OPEN_SOUND);
      registerSound(CATEGORY_ID, CATEGORY_SOUND);
      registerSound(MODULE_SELECT_ID, MODULE_SELECT_SOUND);
      registerSound(SLIDER_MOVING_ID, SLIDER_MOVING_SOUND);
      registerSound(SEARCH_CLICK_ID, SEARCH_CLICK_SOUND);
      registerSound(MODE_OPEN_ID, MODE_OPEN_SOUND);
   }

   private static void registerSound(class_2960 id, class_3414 sound) {
      if (!class_7923.field_41172.method_10250(id)) {
         class_2378.method_10230(class_7923.field_41172, id, sound);
      }
   }

   public static void playSound(class_3414 sound, float volume) {
      if (SoundSettings.isSoundEnabled()) {
         float master = SoundSettings.getMasterVolume() / 100.0F;
         float vol = volume / 100.0F * master;
         if (!(vol <= 0.001F)) {
            class_310 mc = class_310.method_1551();
            if (mc != null) {
               mc.execute(() -> {
                  try {
                     if (mc.method_1483() != null) {
                        mc.method_1483().method_4873(class_1109.method_4757(sound, 1.0F, vol));
                     }
                  } catch (Exception var4x) {
                  }
               });
            }
         }
      }
   }

   public static void playGuiOpen() {
      playSound(GUI_OPEN_SOUND, SoundSettings.getGuiOpenVolume());
   }

   public static void playCategoryChange() {
      playSound(CATEGORY_SOUND, SoundSettings.getCategoryVolume());
   }

   public static void playModuleSelect() {
      playSound(MODULE_SELECT_SOUND, SoundSettings.getModuleSelectVolume());
   }

   public static void playSliderMove() {
      long now = System.currentTimeMillis();
      if (now - lastSliderSoundTime >= 65L) {
         lastSliderSoundTime = now;
         playSound(SLIDER_MOVING_SOUND, SoundSettings.getSliderVolume());
      }
   }

   public static void playSearchClick() {
      playSound(SEARCH_CLICK_SOUND, SoundSettings.getSearchVolume());
   }

   public static void playModeOpen() {
      playSound(MODE_OPEN_SOUND, SoundSettings.getModeOpenVolume());
   }

   public static void playEnable() {
      playSound(ENABLE_SOUND, SoundSettings.getModuleToggleVolume());
   }

   public static void playDisable() {
      playSound(DISABLE_SOUND, SoundSettings.getModuleToggleVolume());
   }

   public static void playCrashDetectionSound() {
      playSound(CRASH_DETECTION_SOUND, 100.0F);
   }
}
