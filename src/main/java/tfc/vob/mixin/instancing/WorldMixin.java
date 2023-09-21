package tfc.vob.mixin.instancing;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.vob.instancing.InstanceDispatcher;
import tfc.vob.instancing.data.InstanceData;
import tfc.vob.instancing.itf.Instanceable;
import tfc.vob.instancing.itf.InstancingWorld;

import java.util.List;

@Mixin(value = World.class, remap = false)
public class WorldMixin implements InstancingWorld {
    private final InstanceDispatcher<Entity> entityDispatcher = new InstanceDispatcher<>(InstanceDispatcher.ENTITY_MANAGER);

    @Override
    public InstanceDispatcher<Entity> getEntityDispatcher() {
        return entityDispatcher;
    }

    @Inject(at = @At("RETURN"), method = "entityJoinedWorld")
    public void postAddEnt(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            Instanceable instanceable = (Instanceable) entity;
            InstanceData<?> data = instanceable.getData();
            if (data != null)
                entityDispatcher.add((InstanceData<Entity>) data);
        }
    }

    @Inject(at = @At("RETURN"), method = "addLoadedEntities")
    public void postAddEnt(List list, CallbackInfo ci) {
        for (Object o : list) {
            Instanceable instanceable = (Instanceable) o;
            InstanceData<?> data = instanceable.getData();
            if (data != null)
                entityDispatcher.add((InstanceData<Entity>) data);
        }
    }

//    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/world/chunk/Chunk;removeEntity(Lnet/minecraft/core/entity/Entity;)V"))
//    public void preRemoveEnt(Chunk instance, Entity entity) {
//        //noinspection unchecked
//        entityDispatcher.remove((InstanceData<Entity>) ((Instanceable) entity).getData());
//    }
}
