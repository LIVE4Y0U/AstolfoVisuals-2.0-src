package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.util.FriendManager;

@Environment(EnvType.CLIENT)
@Mixin(class_640.class)
public class MixinPlayerListEntry {
   @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
   private void astolfo$formatFriendName(CallbackInfoReturnable<class_2561> cir) {
      class_640 entry = (class_640)this;
      if (entry.method_2966() != null && entry.method_2966().getName() != null) {
         String playerName = entry.method_2966().getName();
         if (FriendManager.isFriend(playerName)) {
            class_5250 friendName = class_2561.method_43470(playerName).method_27695(new class_124[]{class_124.field_1060, class_124.field_1067});
            cir.setReturnValue(friendName);
         }
      }
   }
}
