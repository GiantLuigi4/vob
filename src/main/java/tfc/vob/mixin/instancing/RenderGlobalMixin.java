package tfc.vob.mixin.instancing;

import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.vob.instancing.itf.InstancingWorld;

@Mixin(value = RenderGlobal.class, remap = false)
public class RenderGlobalMixin {
    @Shadow
    private World worldObj;

    @Inject(at = @At("HEAD"), method = "renderEntities")
    public void preRenderEnts(ICamera camera, float renderPartialTicks, CallbackInfo ci) {
        ((InstancingWorld) worldObj).getEntityDispatcher().reset();
    }

    @Inject(at = @At("RETURN"), method = "renderEntities")
    public void postRenderEnts(ICamera camera, float renderPartialTicks, CallbackInfo ci) {
        ((InstancingWorld) worldObj).getEntityDispatcher().draw(camera, renderPartialTicks);
    }
}
