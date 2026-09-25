package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;

@Environment(EnvType.CLIENT)
@Mixin(gui.hud.PlayerListHud.class)
public class PlayerListHudMixin {
   @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
   public void onGetPlayerName(client.network.PlayerListEntry entry, CallbackInfoReturnable<minecraft.text.Text> cir) {
      minecraft.text.Text originalText = (minecraft.text.Text)cir.getReturnValue();
      if (originalText != null) {
         cir.setReturnValue(NameProtectModule.getProtectedText(originalText));
      } else {
         String originalStr = entry.getProfile().getName();
         String protectedStr = NameProtectModule.getProtectedName(originalStr);
         if (!originalStr.equals(protectedStr)) {
            cir.setReturnValue(minecraft.text.Text.literal(protectedStr));
         }
      }
   }
}
