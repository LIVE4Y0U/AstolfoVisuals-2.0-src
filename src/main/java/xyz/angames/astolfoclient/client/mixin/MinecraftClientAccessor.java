package xyz.angames.astolfoclient.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(class_310.class)
public interface MinecraftClientAccessor {
   @Accessor("itemUseCooldown")
   int getItemUseCooldown();

   @Accessor("itemUseCooldown")
   void setItemUseCooldown(int var1);

   @Invoker("doAttack")
   boolean invokeDoAttack();

   @Invoker("doItemUse")
   void invokeDoItemUse();
}
