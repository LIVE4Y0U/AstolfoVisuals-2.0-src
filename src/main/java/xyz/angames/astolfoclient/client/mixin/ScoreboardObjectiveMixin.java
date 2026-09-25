package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;

@Environment(EnvType.CLIENT)
@Mixin(minecraft.scoreboard.ScoreboardObjective.class)
public class ScoreboardObjectiveMixin {
   @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
   public void onGetDisplayName(CallbackInfoReturnable<minecraft.text.Text> cir) {
      cir.setReturnValue(NameProtectModule.getProtectedText((minecraft.text.Text)cir.getReturnValue()));
   }
}
