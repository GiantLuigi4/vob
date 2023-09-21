package tfc.vob.mixin.instancing;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.vob.instancing.data.InstanceData;
import tfc.vob.instancing.itf.Instanceable;
import tfc.vob.instancing.itf.InstancingWorld;

@Mixin(value = Chunk.class, remap = false)
public class ChunkMixin {
    @Shadow
    public World worldObj;

    @Inject(at = @At("HEAD"), method = "addEntity")
    public void preAddEnt(Entity entity, CallbackInfo ci) {
//        Instanceable instanceable = (Instanceable) entity;
//        InstanceData<?> data = instanceable.getData();
//        if (data != null)
//            ((InstancingWorld) worldObj).getEntityDispatcher()
//                    .add((InstanceData<Entity>) data);
    }

    @Inject(at = @At("HEAD"), method = "removeEntity")
    public void preRemoveEnt(Entity entity, CallbackInfo ci) {
        Instanceable instanceable = (Instanceable) entity;
        InstanceData<?> data = instanceable.getData();
        if (data != null)
            ((InstancingWorld) worldObj).getEntityDispatcher()
                    .remove((InstanceData<Entity>) data);
    }
}
