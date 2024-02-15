package tfc.vob.mixin;

import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.core.entity.CameraSorter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CameraSorter.class, remap = false)
public class CameraSorterMixin {
    @Shadow
    @Final
    private double posX;

    @Shadow
    @Final
    private double posZ;

    @Shadow @Final private double posY;

    @Inject(at = @At("HEAD"), method = "sortByDistanceToEntity", cancellable = true)
    public void preSort(ChunkRenderer a, ChunkRenderer b, CallbackInfoReturnable<Integer> cir) {
        float dx1 = a.posXMinus + (float) this.posX;
        float dz1 = a.posZMinus + (float) this.posZ;
        float dx2 = b.posXMinus + (float) this.posX;
        float dz2 = b.posZMinus + (float) this.posZ;

		int v = Float.compare(dx1 * dx1 + dz1 * dz1, dx2 * dx2 + dz2 * dz2);

        if (v != 0) cir.setReturnValue(v);
        else {
            float dy1 = a.posYMinus + (float) this.posY;
            float dy2 = a.posYMinus + (float) this.posY;
            cir.setReturnValue(
                    Double.compare(dy1 * dy1, dy2 * dy2)
            );
        }
    }
}
