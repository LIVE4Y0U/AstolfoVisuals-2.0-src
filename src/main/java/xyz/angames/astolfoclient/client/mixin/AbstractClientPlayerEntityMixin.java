package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.util.FriendManager;

@Environment(EnvType.CLIENT)
@Mixin(class_742.class)
public class AbstractClientPlayerEntityMixin {
   private static final class_2960 CUSTOM_CAPE = class_2960.method_60655("astolfoclient", "textures/cape.png");

   @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
   private void onGetSkinTextures(CallbackInfoReturnable<class_8685> cir) {
      class_742 player = (class_742)this;
      class_310 mc = class_310.method_1551();
      boolean isSelf = mc.field_1724 != null && player.method_5667().equals(mc.field_1724.method_5667());
      boolean isFriend = player.method_5477() != null && FriendManager.isFriend(player.method_5477().getString())
         || player.method_7334() != null && FriendManager.isFriend(player.method_7334().getName());
      if (isSelf || isFriend) {
         class_8685 original = (class_8685)cir.getReturnValue();
         if (original == null) {
            return;
         }

         class_8685 customTextures = new class_8685(
            original.comp_1626(), original.comp_1911(), CUSTOM_CAPE, CUSTOM_CAPE, original.comp_1629(), original.comp_1630()
         );
         cir.setReturnValue(customTextures);
      }
   }
}
