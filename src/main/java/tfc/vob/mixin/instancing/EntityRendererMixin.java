package tfc.vob.mixin.instancing;

import net.minecraft.client.render.EntityRenderDispatcher;
import net.minecraft.core.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.vob.instancing.InstanceDispatcher;
import tfc.vob.instancing.data.InstanceData;
import tfc.vob.instancing.itf.Instanceable;

@Mixin(value = EntityRenderDispatcher.class, remap = false)
public class EntityRendererMixin<T extends Entity> {
    @Inject(at = @At("HEAD"), method = "renderEntityWithPosYaw", cancellable = true)
    public void preRenderEntity(T entity, double x, double y, double z, float yaw, float renderPartialTicks, CallbackInfo ci) {
        if (InstanceDispatcher.isInstanceable(entity)) {
            //noinspection unchecked
            ((InstanceData<Object>) ((Instanceable) entity).getData()).update(entity, renderPartialTicks);
            ci.cancel();
        }
    }
}
