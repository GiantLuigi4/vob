package tfc.vob.instancing;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.GLAllocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.EntityRenderDispatcher;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.texturepack.TexturePackList;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import org.lwjgl.opengl.GL11;
import tfc.vob.instancing.data.InstanceData;
import tfc.vob.instancing.model.InstanceModel;
import tfc.vob.instancing.model.ModelManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class InstanceDispatcher<T> {
    HashMap<Class<T>, Collection<InstanceData<?>>> types = new HashMap<>();
    ModelManager<T> models;

    public static final ModelManager<Entity> ENTITY_MANAGER = new ModelManager<>("entity");

    static {
//        //noinspection unchecked
//        ENTITY_MANAGER.loadModel((Class<Entity>) (Class<?>) EntityPig.class, "pig");
//        //noinspection unchecked
//        ENTITY_MANAGER.loadModel((Class<Entity>) (Class<?>) EntityChicken.class, "chicken");
//        //noinspection unchecked
//        ENTITY_MANAGER.loadModel((Class<Entity>) (Class<?>) EntityCow.class, "cow");

        try {
            load();
        } catch (Throwable err) {
            throw new RuntimeException(err);
        }
    }

    public static byte[] read(String resource) {
        try {
            InputStream is = open(resource);
            byte[] data = new byte[is.available()];
            is.read(data);
            try {
                is.close();
            } catch (Throwable ignored) {
            }
            return data;
        } catch (Throwable err) {
            System.err.println("Failed to read " + resource + ".");
            throw new RuntimeException(err);
        }
    }

    private static InputStream open(String name) throws IOException {
        TexturePackList packs = Minecraft.getMinecraft(Minecraft.class).texturePackList;

        InputStream is0 = packs.selectedTexturePack.getResourceAsStream(name);
        if (is0 != null) return is0;
        InputStream is1 = packs.getDefaultTexturePack().getResourceAsStream(name);
        if (is1 != null) return is1;

        for (ModContainer allMod : FabricLoader.getInstance().getAllMods()) {
            Optional<Path> path = allMod.findPath(name);
            if (path.isPresent()) {
                return Files.newInputStream(path.get());
            }
        }

        return null;
    }

    private static ArrayList<InputStream> openAll(String name) throws IOException {
        TexturePackList packs = Minecraft.getMinecraft(Minecraft.class).texturePackList;
        ArrayList<InputStream> streams = new ArrayList<>();

        for (ModContainer allMod : FabricLoader.getInstance().getAllMods()) {
            Optional<Path> path = allMod.findPath(name);
            if (path.isPresent()) {
                streams.add(Files.newInputStream(path.get()));
            }
        }
        InputStream is1 = null;
        if (packs.getDefaultTexturePack() != packs.selectedTexturePack) {
            is1 = packs.getDefaultTexturePack().getResourceAsStream(name);
            if (is1 != null) streams.add(is1);
        }
        InputStream is0 = packs.selectedTexturePack.getResourceAsStream(name);
        if (is0 != null && is0 != is1) streams.add(is0);

        return streams;
    }

    public static void load() throws IOException {
        for (InputStream stream : openAll("assets/instancing/entity/models.txt")) {
            byte[] data = new byte[stream.available()];
            stream.read(data);
            try {
                stream.close();
            } catch (Throwable ignored) {
            }

            String dat = new String(data);
            for (String s : dat.split("\n")) {
                String[] split = s.trim().split("->");
                //noinspection unchecked
                ENTITY_MANAGER.loadModel(
                         (Class<Entity>) EntityDispatcher.stringToClassMapping.get(split[0]),
                        split[1]
                );
            }
            System.out.println(dat);
        }
    }

    public static boolean isInstanceable(Entity e) {
        //noinspection unchecked
        return ENTITY_MANAGER.hasModel((Class<Entity>) e.getClass());
    }

    public void add(InstanceData<T> entity) {
        Collection<InstanceData<?>> instances = types.computeIfAbsent(entity.getInstanceType(), (k) -> new ArrayList<>());
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
