package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;

@Environment(EnvType.CLIENT)
@Mixin(minecraft.scoreboard.Team.class)
public class ScoreboardTeamMixin {
   @Inject(method = "getPrefix", at = @At("RETURN"), cancellable = true)
   public void onGetPrefix(CallbackInfoReturnable<minecraft.text.Text> cir) {
      cir.setReturnValue(NameProtectModule.getProtectedText((minecraft.text.Text)cir.getReturnValue()));
   }

   @Inject(method = "getSuffix", at = @At("RETURN"), cancellable = true)
   public void onGetSuffix(CallbackInfoReturnable<minecraft.text.Text> cir) {
      cir.setReturnValue(NameProtectModule.getProtectedText((minecraft.text.Text)cir.getReturnValue()));
   }

   @Inject(method = "decorateName", at = @At("RETURN"), cancellable = true)
   public void onDecorateName(CallbackInfoReturnable<minecraft.text.MutableText> cir) {
      minecraft.text.Text protectedText = NameProtectModule.getProtectedText((minecraft.text.Text)cir.getReturnValue());
      if (protectedText != null) {
         cir.setReturnValue(protectedText.copy());
      }
   }
}
