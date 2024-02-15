package tfc.vob.mixin;

import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.ChunkRendererComparator;
import net.minecraft.client.render.camera.ICamera;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChunkRendererComparator.class, remap = false)
public class ChunkRendererComparatorMixin {
    @Shadow @Final private ICamera activeCamera;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int compare(ChunkRenderer cr1, ChunkRenderer cr2) {
        if (cr1.equals(cr2)) {
            return 0;
        }

        boolean cr1InFrustum = cr1.isInFrustum;
        boolean cr2InFrustum = cr2.isInFrustum;
        if (cr1InFrustum && !cr2InFrustum) {
            return 1;
        } else if (cr2InFrustum && !cr1InFrustum) {
            return -1;
        } else {
            float cx = (float) activeCamera.getX(1);
            float cy = (float) activeCamera.getY(1);
            float cz = (float) activeCamera.getZ(1);

            float dx0 = cr1.posX - cx;
            float dy0 = cr1.posY - cy;
            float dz0 = cr1.posZ - cz;
            float cr0Distance = dx0 * dx0 + dz0 * dz0 + dy0 * dy0;

            float dx1 = cr2.posX - cx;
            float dy1 = cr2.posY - cy;
            float dz1 = cr2.posZ - cz;
            float cr1Distance = dx1 * dx1 + dz1 * dz1 + dy1 * dy1;

            if (cr0Distance < cr1Distance) {
                return 1;
            } else if (cr0Distance > cr1Distance) {
                return -1;
            } else {
                return cr1.chunkIndex >= cr2.chunkIndex ? -1 : 1;
            }
        }
    }
}
