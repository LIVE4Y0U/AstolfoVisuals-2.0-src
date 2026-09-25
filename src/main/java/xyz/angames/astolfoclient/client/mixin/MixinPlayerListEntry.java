package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.angames.astolfoclient.client.util.FriendManager;

@Environment(EnvType.CLIENT)
@Mixin(client.network.PlayerListEntry.class)
public class MixinPlayerListEntry {
   @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
   private void astolfo$formatFriendName(CallbackInfoReturnable<minecraft.text.Text> cir) {
      client.network.PlayerListEntry entry = (client.network.PlayerListEntry)this;
      if (entry.getProfile() != null && entry.getProfile().getName() != null) {
         String playerName = entry.getProfile().getName();
         if (FriendManager.isFriend(playerName)) {
            minecraft.text.MutableText friendName = minecraft.text.Text.literal(playerName).formatted(new minecraft.util.Formatting[]{minecraft.util.Formatting.GREEN, minecraft.util.Formatting.BOLD});
            cir.setReturnValue(friendName);
         }
      }
   }
}
