package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.angames.astolfoclient.client.module.modules.misc.NameProtectModule;
import xyz.angames.astolfoclient.client.module.modules.misc.PasswordHiderModule;

@Environment(EnvType.CLIENT)
@Mixin(class_338.class)
public class ChatHudMixin {
   @ModifyVariable(
      method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
      at = @At("HEAD"),
      argsOnly = true
   )
   private class_2561 modifyChatMessages(class_2561 message) {
      message = PasswordHiderModule.getProtectedChat(message);
      return NameProtectModule.getProtectedText(message);
   }
}
