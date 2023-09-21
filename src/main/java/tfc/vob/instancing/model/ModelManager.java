package tfc.vob.instancing.model;

import net.minecraft.core.entity.animal.EntityChicken;
import net.minecraft.core.entity.animal.EntityCow;
import net.minecraft.core.entity.animal.EntityPig;

import java.util.HashMap;

public class ModelManager<T> {
    HashMap<Class<T>, InstanceModel> models = new HashMap<>();

    public ModelManager() {
        models.put((Class<T>) EntityPig.class, new InstanceModel("pig"));
        models.put((Class<T>) EntityChicken.class, new InstanceModel("chicken"));
        models.put((Class<T>) EntityCow.class, new InstanceModel("cow"));
    }

    public boolean hasModel(Class<T> entity) {
        return models.containsKey(entity);
    }

    public InstanceModel get(Class<T> clazz) {
        return models.get(clazz);
    }
}
