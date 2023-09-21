package tfc.vob.instancing.itf;

import net.minecraft.core.entity.Entity;
import tfc.vob.instancing.InstanceDispatcher;

public interface InstancingWorld {
    InstanceDispatcher<Entity> getEntityDispatcher();
}
