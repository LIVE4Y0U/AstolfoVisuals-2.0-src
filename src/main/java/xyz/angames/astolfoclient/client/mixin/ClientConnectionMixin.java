package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.angames.astolfoclient.client.protection.ClientProtectionManager;

@Environment(EnvType.CLIENT)
@Mixin(minecraft.network.ClientConnection.class)
public abstract class ClientConnectionMixin {
   @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
   private static void receivePackets(network.packet.Packet<?> packet, network.listener.PacketListener listener, CallbackInfo callbackInfo) {
      if (ClientProtectionManager.getInstance().isMaliciousPacket(packet)) {
         callbackInfo.cancel();
      } else {
         if (packet instanceof s2c.play.BundleS2CPacket bundlePacket) {
            for (network.packet.Packet<?> innerPacket : bundlePacket.getPackets()) {
               if (ClientProtectionManager.getInstance().isMaliciousPacket(innerPacket)) {
                  callbackInfo.cancel();
                  return;
               }
            }
         }
      }
   }
}
