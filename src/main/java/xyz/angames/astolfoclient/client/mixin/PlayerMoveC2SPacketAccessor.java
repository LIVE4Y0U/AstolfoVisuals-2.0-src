package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {
   @Accessor("horizontalCollision")
   boolean getHorizontalCollision();

   @Accessor("onGround")
   @Mutable
   void setOnGround(boolean var1);

   @Accessor("x")
   @Mutable
   void setX(double var1);

   @Accessor("y")
   @Mutable
   void setY(double var1);

   @Accessor("z")
   @Mutable
   void setZ(double var1);

   @Accessor("yaw")
   @Mutable
   void setYaw(float var1);

   @Accessor("pitch")
   @Mutable
   void setPitch(float var1);

   @Accessor("changePosition")
   @Mutable
   void setChangePosition(boolean var1);

   @Accessor("changeLook")
   @Mutable
   void setChangeLook(boolean var1);
}
