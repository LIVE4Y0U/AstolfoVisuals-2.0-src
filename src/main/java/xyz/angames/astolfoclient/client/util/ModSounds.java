package xyz.angames.astolfoclient.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;
import xyz.angames.astolfoclient.client.config.SoundSettings;

@Environment(EnvType.CLIENT)
public class ModSounds {
   public static final minecraft.util.Identifier ENABLE_ID = minecraft.util.Identifier.of("astolfoclient", "module_enable");
   public static final minecraft.sound.SoundEvent ENABLE_SOUND = minecraft.sound.SoundEvent.of(ENABLE_ID);
   public static final minecraft.util.Identifier DISABLE_ID = minecraft.util.Identifier.of("astolfoclient", "module_disable");
   public static final minecraft.sound.SoundEvent DISABLE_SOUND = minecraft.sound.SoundEvent.of(DISABLE_ID);
   public static final minecraft.util.Identifier CRASH_DETECTION_ID = minecraft.util.Identifier.of("astolfoclient", "crash_detection");
   public static final minecraft.sound.SoundEvent CRASH_DETECTION_SOUND = minecraft.sound.SoundEvent.of(CRASH_DETECTION_ID);
   public static final minecraft.util.Identifier CRASH_DETECTION_HYPHEN_ID = minecraft.util.Identifier.of("astolfoclient", "crash-detection");
   public static final minecraft.sound.SoundEvent CRASH_DETECTION_HYPHEN_SOUND = minecraft.sound.SoundEvent.of(CRASH_DETECTION_HYPHEN_ID);
   public static final minecraft.util.Identifier GUI_OPEN_ID = minecraft.util.Identifier.of("astolfoclient", "clickgui_open");
   public static final minecraft.sound.SoundEvent GUI_OPEN_SOUND = minecraft.sound.SoundEvent.of(GUI_OPEN_ID);
   public static final minecraft.util.Identifier CATEGORY_ID = minecraft.util.Identifier.of("astolfoclient", "clickgui_category");
   public static final minecraft.sound.SoundEvent CATEGORY_SOUND = minecraft.sound.SoundEvent.of(CATEGORY_ID);
   public static final minecraft.util.Identifier MODULE_SELECT_ID = minecraft.util.Identifier.of("astolfoclient", "clickgui_module");
   public static final minecraft.sound.SoundEvent MODULE_SELECT_SOUND = minecraft.sound.SoundEvent.of(MODULE_SELECT_ID);
   public static final minecraft.util.Identifier SLIDER_MOVING_ID = minecraft.util.Identifier.of("astolfoclient", "clickgui_slider");
   public static final minecraft.sound.SoundEvent SLIDER_MOVING_SOUND = minecraft.sound.SoundEvent.of(SLIDER_MOVING_ID);
   public static final minecraft.util.Identifier SEARCH_CLICK_ID = minecraft.util.Identifier.of("astolfoclient", "clickgui_search");
   public static final minecraft.sound.SoundEvent SEARCH_CLICK_SOUND = minecraft.sound.SoundEvent.of(SEARCH_CLICK_ID);
   public static final minecraft.util.Identifier MODE_OPEN_ID = minecraft.util.Identifier.of("astolfoclient", "clickgui_mode_open");
   public static final minecraft.sound.SoundEvent MODE_OPEN_SOUND = minecraft.sound.SoundEvent.of(MODE_OPEN_ID);
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

   private static void registerSound(minecraft.util.Identifier id, minecraft.sound.SoundEvent sound) {
      if (!minecraft.registry.Registries.SOUND_EVENT.containsId(id)) {
         minecraft.registry.Registry.register(minecraft.registry.Registries.SOUND_EVENT, id, sound);
      }
   }

   public static void playSound(minecraft.sound.SoundEvent sound, float volume) {
      if (SoundSettings.isSoundEnabled()) {
         float master = SoundSettings.getMasterVolume() / 100.0F;
         float vol = volume / 100.0F * master;
         if (!(vol <= 0.001F)) {
            minecraft.client.MinecraftClient mc = minecraft.client.MinecraftClient.getInstance();
            if (mc != null) {
               mc.execute(() -> {
                  try {
                     if (mc.getSoundManager() != null) {
                        mc.getSoundManager().play(client.sound.PositionedSoundInstance.master(sound, 1.0F, vol));
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
