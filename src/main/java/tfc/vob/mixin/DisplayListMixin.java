package tfc.vob.mixin;

import net.minecraft.client.render.DisplayList;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.IntBuffer;

@Mixin(value = DisplayList.class, remap = false)
public abstract class DisplayListMixin {
    @Shadow
    private boolean initialized;
    @Shadow
    @Final
    private IntBuffer callList;
    @Shadow
    private int posX;
    @Shadow
    private float offsetX;
    @Shadow
    private int posY;
    @Shadow
    private float offsetY;
    @Shadow
    private int posZ;
    @Shadow
    private float offsetZ;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void addCallToList(int i) {
        this.callList.put(i);
        if (this.callList.remaining() == 0) {
            this.call();
            this.callList.position(0);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setToPos(int blockX, int blockY, int blockZ, double offsetX, double offsetY, double offsetZ) {
        this.initialized = true;
        this.callList.position(0);
        this.posX = blockX;
        this.posY = blockY;
        this.posZ = blockZ;
        this.offsetX = (float) offsetX;
        this.offsetY = (float) offsetY;
        this.offsetZ = (float) offsetZ;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void call() {
        if (this.initialized) {
            if (this.callList.position() > 0) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float) this.posX - this.offsetX, (float) this.posY - this.offsetY, (float) this.posZ - this.offsetZ);

                callList.limit(callList.position());
                callList.position(0);
                GL11.glCallLists(this.callList);
                callList.position(callList.limit());
                callList.limit(callList.capacity());

                GL11.glPopMatrix();
            }
        }
    }
}
