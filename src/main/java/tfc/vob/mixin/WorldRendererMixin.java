package tfc.vob.mixin;

import net.minecraft.client.render.DisplayList;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.core.entity.CameraSorter;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = WorldRenderer.class, remap = false)
public class WorldRendererMixin {
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glColorMask(ZZZZ)V", ordinal = 0))
    public void noMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GL11.glColorMask(true, true, true, true);
    }

    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderGlobal;callAllDisplayLists(ID)V"))
    public void itsAlreadyCalled(RenderGlobal instance, int pass, double pct) {
    }
}
