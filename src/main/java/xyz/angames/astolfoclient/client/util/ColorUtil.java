package xyz.angames.astolfoclient.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;

@Environment(EnvType.CLIENT)
public class ColorUtil {
   public static int red(int c) {
      return c >> 16 & 0xFF;
   }

   public static int green(int c) {
      return c >> 8 & 0xFF;
   }

   public static int blue(int c) {
      return c & 0xFF;
   }

   public static int alpha(int c) {
      return c >> 24 & 0xFF;
   }

   public static int makeColor(int red, int green, int blue, int alpha) {
      return class_3532.method_15340(alpha, 0, 255) << 24
         | class_3532.method_15340(red, 0, 255) << 16
         | class_3532.method_15340(green, 0, 255) << 8
         | class_3532.method_15340(blue, 0, 255);
   }

   public static int multAlpha(int color, float alphaPercent) {
      return makeColor(red(color), green(color), blue(color), Math.round(alpha(color) * alphaPercent));
   }
}
