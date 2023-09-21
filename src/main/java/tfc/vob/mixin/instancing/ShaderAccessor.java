package tfc.vob.mixin.instancing;

import net.minecraft.client.render.shader.Shader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Shader.class, remap = false)
public interface ShaderAccessor {
    @Accessor
    int getProgram();
}
