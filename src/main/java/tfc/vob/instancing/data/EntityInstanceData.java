package tfc.vob.instancing.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityLiving;
import net.minecraft.core.util.phys.Vec3d;
import org.lwjgl.opengl.GL20;
import tfc.vob.instancing.InstancingShader;
import tfc.vob.mixin.instancing.LivingEntityRendererAccessor;

public class EntityInstanceData<T extends Entity> extends InstanceData<T> {
    boolean living;
    EntityRenderer<T> renderer;

    public EntityInstanceData(T entity) {
        super(entity);

        living = entity instanceof EntityLiving;
        renderer = EntityRenderDispatcher.instance.getRenderer(entity);
    }

    double x;
    double y;
    double z;
    float yaw;
    float pitch;
    float renderPartialTicks;

    public void update(T entity, float pct) {
        x = entity.x * pct + entity.xo * (1 - pct);
        y = entity.y * pct + entity.yo * (1 - pct) + 1.5;
        z = entity.z * pct + entity.zo * (1 - pct);
        yaw = entity.yRot * pct + entity.yRotO * (1 - pct);
        pitch = entity.xRot * pct + entity.xRotO * (1 - pct);
        super.update(entity, pct);
    }

    @Override
    public void upload(Minecraft mc, float pct, int index, InstancingShader shader, int[] uforms) {
        int uform;
        float headYawOffset = 0;

        float tickCount = entity.tickCount + renderPartialTicks;

        float hurt = 0;
        float death = 0;
        if (living) {
            uform = uforms[0];

            EntityLiving living = ((EntityLiving) entity);
            if (uform != -1) GL20.glUniform2f(uform + index,
                    living.limbSwing,
                    living.limbYaw * pct + living.prevLimbYaw * (1 - pct)
            );

            headYawOffset = living.prevRenderYawOffset + (living.renderYawOffset - living.prevRenderYawOffset) * renderPartialTicks;

            //noinspection unchecked
            tickCount = ((LivingEntityRendererAccessor<EntityLiving>) renderer).callTicksExisted(living, renderPartialTicks);
            hurt = living.hurtTime + renderPartialTicks;
            if (living.deathTime != 0) hurt = Math.max(hurt, death = (living.deathTime + renderPartialTicks));
        }
        uform = uforms[1];
        if (uform != -1) GL20.glUniform2f(uform + index, pitch, -(headYawOffset - yaw));
        uform = uforms[2];
        if (uform != -1) GL20.glUniform3f(uform + index, tickCount, entity.world.getWorldTime(), hurt);

        float brightness;
        if (Minecraft.getMinecraft(Minecraft.class).fullbright) brightness = 1;
        else brightness = entity.getBrightness(pct);
        uform = uforms[3];
        if (uform != -1) GL20.glUniform4f(uform + index, brightness, brightness, brightness, 1);

        uform = uforms[5];
        if (uform != -1) GL20.glUniform4f(uform + index, (float) (entity.bb.maxX - entity.bb.minX), (float) (entity.bb.maxY - entity.bb.minY), (float) ((entity.y + 1.5) - entity.bb.minY) * 16, 0);

        shader.matrix(
                uforms[4], index,
                Vec3d.createVector(x, y, z),
                0,
                (float) Math.toRadians(headYawOffset),
                death
        );
    }

    @Override
    public Class<T> getInstanceType() {
        //noinspection unchecked
        return (Class<T>) entity.getClass();
    }

    @Override
    public String texture() {
        return entity.getEntityTexture();
    }

    @Override
    public void drawExtra() {
        double y = entity.y * renderPartialTicks + entity.yOld * (1 - renderPartialTicks);
        if (living) //noinspection unchecked
            ((LivingEntityRendererAccessor<EntityLiving>) renderer).callPassSpecialRender((EntityLiving) entity, x, y, z);
        renderer.doRenderShadowAndFire(entity, x, y, z, yaw, renderPartialTicks);
    }

    @Override
    public int[] requestUniformIds(InstancingShader shader) {
        return new int[]{
                shader.getUniform("limbData"),
                shader.getUniform("facings"),
                shader.getUniform("tickData"),
                shader.getUniform("colors"),
                shader.getUniform("matrices"),
                shader.getUniform("special"),
        };
    }
}
