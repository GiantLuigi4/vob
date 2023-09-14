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
	@Shadow @Final private double posX;
	
	@Shadow @Final private double posZ;
	
	@Inject(at = @At("HEAD"), method = "sortByDistanceToEntity", cancellable = true)
	public void preSort(ChunkRenderer a, ChunkRenderer b, CallbackInfoReturnable<Integer> cir) {
		double dx1 = (double)a.posXMinus + this.posX;
		double dz1 = (double)a.posZMinus + this.posZ;
		double dx2 = (double)b.posXMinus + this.posX;
		double dz2 = (double)b.posZMinus + this.posZ;
		int v = Double.compare(dx1 * dx1 + dz1 * dz1, dx2 * dx2 + dz2 * dz2);
		if (v != 0) cir.setReturnValue(v);
	}
}
