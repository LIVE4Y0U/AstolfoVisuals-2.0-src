package xyz.angames.astolfoclient.client.util;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_638;
import net.minecraft.class_745;

@Environment(EnvType.CLIENT)
public class FakePlayerEntity extends class_745 {
   public static FakePlayerEntity instance = null;

   public FakePlayerEntity(class_638 world, GameProfile profile) {
      super(world, profile);
      this.method_6033(20.0F);
   }

   public boolean method_5810() {
      return false;
   }

   public boolean method_30948() {
      return false;
   }

   public boolean method_30949(class_1297 other) {
      return false;
   }

   public void method_6005(double strength, double x, double z) {
   }

   public class_1799 method_6118(class_1304 slot) {
      return slot == class_1304.field_6171 ? new class_1799(class_1802.field_8288) : super.method_6118(slot);
   }
}
