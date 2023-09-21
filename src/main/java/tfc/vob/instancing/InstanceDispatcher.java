package tfc.vob.instancing;

import net.minecraft.client.GLAllocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.EntityRenderDispatcher;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.entity.Entity;
import org.lwjgl.opengl.GL11;
import tfc.vob.instancing.data.InstanceData;
import tfc.vob.instancing.model.InstanceModel;
import tfc.vob.instancing.model.ModelManager;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class InstanceDispatcher<T> {
    HashMap<Class<T>, Collection<InstanceData<?>>> types = new HashMap<>();
    ModelManager<T> models;

    public static final ModelManager<Entity> ENTITY_MANAGER = new ModelManager<>();

    public static boolean isInstanceable(Entity e) {
        //noinspection unchecked
        return ENTITY_MANAGER.hasModel((Class<Entity>) e.getClass());
    }

    public void add(InstanceData<T> entity) {
        Collection<InstanceData<?>> instances = types.computeIfAbsent(entity.getInstanceType(), (k) -> new ArrayList<>());
//        if (!instances.contains(entity))
            instances.add(entity);
    }

    public void remove(InstanceData<T> data) {
        Collection<InstanceData<?>> instances = types.computeIfAbsent(data.getInstanceType(), (k) -> new ArrayList<>());
        instances.remove(data);
    }

    private final FloatBuffer _proj = GLAllocation.createDirectFloatBuffer(16);

    private final FloatBuffer _modl = GLAllocation.createDirectFloatBuffer(16);

    public void draw(ICamera camera, float pct) {
        if (EntityRenderDispatcher.instance.renderEngine == null) return;

        GL11.glPushMatrix();
        GL11.glTranslated(
                -camera.getX(pct),
                -camera.getY(pct),
                -camera.getZ(pct)
        );

        GL11.glScaled(1 / 16d, -1 / 16d, -1 / 16d);

        this._proj.clear();
        this._modl.clear();
        GL11.glGetFloat(2983, this._proj);
        GL11.glGetFloat(2982, this._modl);
        this._proj.flip().limit(16);
        this._modl.flip().limit(16);

        GL11.glDisable(GL11.GL_CULL_FACE);

        Minecraft mc = Minecraft.getMinecraft(Minecraft.class);

        types.forEach((clazz, value) -> {
            HashMap<String, ArrayList<InstanceData<?>>> byTex = new HashMap<>();

            for (InstanceData<?> instanceData : value) {
                if (!instanceData.visible) continue;

                ArrayList<InstanceData<?>> data = byTex.computeIfAbsent(instanceData.texture(), (k) -> new ArrayList<>());
                data.add(instanceData);
            }

            if (byTex.isEmpty()) return;

            InstanceModel model = models.get(clazz);
            model.bind();
            model.matrix(_proj, _modl);


            byTex.forEach((tex, datas) -> {
                int[] uforms = null;

                int qnty = 0;
                EntityRenderDispatcher.instance.renderEngine.bindTexture(
                        EntityRenderDispatcher.instance.renderEngine.getTexture(tex)
                );
                for (InstanceData<?> data : datas) {
                    if (uforms == null) uforms = data.requestUniformIds(model.shader);

                    data.upload(mc, pct, qnty++, model.shader, uforms);
                    if (qnty >= model.maxInstances) {
                        model.draw(qnty);
                        qnty = 0;
                    }
                }

                model.draw(qnty);
            });
            model.unbind();
        });
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(
                -camera.getX(pct),
                -camera.getY(pct),
                -camera.getZ(pct)
        );
        for (Collection<InstanceData<?>> value : types.values()) {
            for (InstanceData<?> instanceData : value) {
                if (instanceData.visible) {
                    instanceData.drawExtra();
                }
            }
        }
        GL11.glPopMatrix();
    }

    public InstanceDispatcher(ModelManager<T> manager) {
        this.models = manager;
    }

    public void reset() {
        for (Collection<InstanceData<?>> value : types.values()) {
            for (InstanceData<?> instanceData : value) {
                instanceData.visible = false;
            }
        }
    }
}
