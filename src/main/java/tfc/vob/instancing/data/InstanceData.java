package tfc.vob.instancing.data;

import net.minecraft.client.Minecraft;
import tfc.vob.instancing.InstancingShader;

public abstract class InstanceData<T> {
    public boolean visible;
    protected float renderPartialTicks;
    protected final T entity;

    public InstanceData(T entity) {
        this.entity = entity;
    }

    public void update(T entity, float pct) {
        this.renderPartialTicks = pct;
        visible = true;
    }

    public abstract void upload(Minecraft mc, float pct, int index, InstancingShader shader, int[] uforms);

    public abstract Class<T> getInstanceType();

    public abstract String texture();

    public void drawExtra() {
    }

    public abstract int[] requestUniformIds(InstancingShader shader);
}
