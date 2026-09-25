package xyz.angames.astolfoclient.client.mixin;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.module.modules.misc.CoordsHiderModule;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public class DebugHudMixin {
   @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
   private void onGetLeftText(CallbackInfoReturnable<List<String>> cir) {
      List<String> list = (List<String>)cir.getReturnValue();
      if (list != null && !list.isEmpty()) {
         if (CoordsHiderModule.isF3Hidden()) {
            List<String> modified = new ArrayList<>(list.size());

            for (String line : list) {
               modified.add(CoordsHiderModule.filterF3Line(line));
            }

            cir.setReturnValue(modified);
         }
      }
   }
}
