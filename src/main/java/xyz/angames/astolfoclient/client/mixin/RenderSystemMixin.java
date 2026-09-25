package xyz.angames.astolfoclient.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9958;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.config.ThemeManager;
import xyz.angames.astolfoclient.client.module.modules.render.AmbientsModule;

@Environment(EnvType.CLIENT)
@Mixin(RenderSystem.class)
public class RenderSystemMixin {
   @ModifyVariable(method = "setShaderFog", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private static class_9958 modifyFog(class_9958 originalFog) {
      if (AstolfoclientClient.moduleManager == null) {
         return originalFog;
      }

      AmbientsModule ambients = (AmbientsModule)AstolfoclientClient.moduleManager.getModuleByName("Ambients");
      if (ambients != null && ambients.isEnabled() && ambients.customFog.get()) {
         float start = originalFog.comp_3009();
         float end = originalFog.comp_3010();
         float r = originalFog.comp_3012();
         float g = originalFog.comp_3013();
         float b = originalFog.comp_3014();
         float a = originalFog.comp_3015();
         start = (float)ambients.fogStart.get();
         end = (float)ambients.fogEnd.get();
         if (ambients.fogColorEnabled.get() && ambients.themeSync.get()) {
            float strength = (float)ambients.fogStrength.get();
            if (strength > 0.0F) {
               Color themeColor = new Color(ThemeManager.getThemedColor(0L));
               float fr = themeColor.getRed() / 255.0F;
               float fg = themeColor.getGreen() / 255.0F;
               float fb = themeColor.getBlue() / 255.0F;
               r = r * (1.0F - strength) + fr * strength;
               g = g * (1.0F - strength) + fg * strength;
               b = b * (1.0F - strength) + fb * strength;
            }
         }

         return new class_9958(start, end, originalFog.comp_3011(), r, g, b, a);
      } else {
         return originalFog;
      }
   }
}
