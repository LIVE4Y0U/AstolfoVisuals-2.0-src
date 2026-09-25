package xyz.angames.astolfoclient.client.util;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.OtherClientPlayerEntity;

@Environment(EnvType.CLIENT)
public class FakePlayerEntity extends OtherClientPlayerEntity {
   public static FakePlayerEntity instance = null;

   public FakePlayerEntity(ClientWorld world, GameProfile profile) {
      super(world, profile);
      this.setHealth(20.0F);
   }

   public boolean isPushable() {
      return false;
   }

   public boolean isCollidable() {
      return false;
   }

   public boolean collidesWith(Entity other) {
      return false;
   }

   public void takeKnockback(double strength, double x, double z) {
   }

   public ItemStack getEquippedStack(EquipmentSlot slot) {
      return slot == EquipmentSlot.OFFHAND ? new ItemStack(Items.TOTEM_OF_UNDYING) : super.getEquippedStack(slot);
   }
}
