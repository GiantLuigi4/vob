package tfc.vob.mixin.instancing;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.vob.instancing.InstanceDispatcher;
import tfc.vob.instancing.data.EntityInstanceData;
import tfc.vob.instancing.data.InstanceData;
import tfc.vob.instancing.itf.Instanceable;

@Mixin(value = Entity.class, remap = false)
public class EntityMixin implements Instanceable {
    InstanceData<?> data;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void postInit(World world, CallbackInfo ci) {
        if (InstanceDispatcher.isInstanceable((Entity) (Object) this)) {
            data = new EntityInstanceData<>((Entity) (Object) this);
        }
    }

    @Override
    public InstanceData<?> getData() {
        return data;
    }
}
