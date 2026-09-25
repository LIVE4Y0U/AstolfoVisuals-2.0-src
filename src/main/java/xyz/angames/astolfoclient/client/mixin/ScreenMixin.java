package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.Selectable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.util.IASAccountHelper;

@Environment(EnvType.CLIENT)
@Mixin(gui.screen.Screen.class)
public abstract class ScreenMixin {
   @Shadow
   protected abstract <T extends client.gui.Element & client.gui.Drawable & client.gui.Selectable> T addDrawableChild(T var1);

   @Inject(method = "init", at = @At("TAIL"))
   private void onInit(CallbackInfo ci) {
      gui.screen.Screen screen = (gui.screen.Screen)this;
      IASAccountHelper.onScreenInit(screen, this::addDrawableChild);
   }
}
