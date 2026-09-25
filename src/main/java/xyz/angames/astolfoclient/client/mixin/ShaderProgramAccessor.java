package xyz.angames.astolfoclient.client.mixin;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_284;
import net.minecraft.class_5944;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(class_5944.class)
public interface ShaderProgramAccessor {
   @Accessor("uniformsByName")
   Map<String, class_284> getUniformsByName();
}
