package tfc.vob.mixin.instancing;

import net.minecraft.client.render.entity.LivingRenderer;
import net.minecraft.core.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingRenderer.class)
public interface LivingEntityRendererAccessor<T extends EntityLiving> {
    @Invoker
    float callTicksExisted(T entity, float renderPartialTicks);

    @Invoker
    void callPassSpecialRender(T entity, double x, double y, double z);
}
