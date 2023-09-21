package tfc.vob.instancing.model;

import java.util.HashMap;

public class ModelManager<T> {
    HashMap<Class<T>, InstanceModel> models = new HashMap<>();

    String category;

    public ModelManager(String category) {
        this.category = category;
//        models.put((Class<T>) EntityPig.class, new InstanceModel("entity", "pig"));
//        models.put((Class<T>) EntityChicken.class, new InstanceModel("entity", "chicken"));
//        models.put((Class<T>) EntityCow.class, new InstanceModel("entity", "cow"));
    }

    public void loadModel(Class<T> attachment, String name) {
        InstanceModel old = models.put(attachment, new InstanceModel(category, name));
        if (old != null) old.close();
    }

    public boolean hasModel(Class<T> entity) {
        return models.containsKey(entity);
    }

    public InstanceModel get(Class<T> clazz) {
        return models.get(clazz);
    }
}
