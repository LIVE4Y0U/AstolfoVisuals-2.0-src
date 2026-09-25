package xyz.angames.astolfoclient.client.mixin;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(ShaderProgram.class)
public interface ShaderProgramAccessor {
   @Accessor("uniformsByName")
   Map<String, GlUniform> getUniformsByName();
}
