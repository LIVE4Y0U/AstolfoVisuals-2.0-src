package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2547;
import net.minecraft.class_2596;
import net.minecraft.class_8042;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.protection.ClientProtectionManager;

@Environment(EnvType.CLIENT)
@Mixin(class_2535.class)
public abstract class ClientConnectionMixin {
   @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
   private static void receivePackets(class_2596<?> packet, class_2547 listener, CallbackInfo callbackInfo) {
      if (ClientProtectionManager.getInstance().isMaliciousPacket(packet)) {
         callbackInfo.cancel();
      } else {
         if (packet instanceof class_8042 bundlePacket) {
            for (class_2596<?> innerPacket : bundlePacket.method_48324()) {
               if (ClientProtectionManager.getInstance().isMaliciousPacket(innerPacket)) {
                  callbackInfo.cancel();
                  return;
               }
            }
         }
      }
   }
}
