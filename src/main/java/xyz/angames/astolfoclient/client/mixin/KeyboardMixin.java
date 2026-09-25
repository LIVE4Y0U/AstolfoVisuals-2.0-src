package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.AstolfoclientClient;
import xyz.angames.astolfoclient.client.gui.ClickGuiScreen;
import xyz.angames.astolfoclient.client.gui.HudEditorScreen;
import xyz.angames.astolfoclient.client.module.Module;

@Environment(EnvType.CLIENT)
@Mixin(minecraft.client.Keyboard.class)
public class KeyboardMixin {
   @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
   private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
      if (key != -1 && key > 0) {
         minecraft.client.MinecraftClient client = minecraft.client.MinecraftClient.getInstance();
         if (action == 1 && client.currentScreen == null) {
            if (key == AstolfoclientClient.clickGuiKeyCode || key == 260 || key == 344) {
               client.setScreen(new ClickGuiScreen());
               ci.cancel();
               return;
            }

            if (key == AstolfoclientClient.hudEditorKeyCode) {
               client.setScreen(new HudEditorScreen());
               ci.cancel();
               return;
            }

            if (AstolfoclientClient.moduleManager != null) {
               for (Module module : AstolfoclientClient.moduleManager.getModules()) {
                  if (module.getKeyCode() == key) {
                     module.toggle();
                  }
               }
            }
         }
      }
   }
}
