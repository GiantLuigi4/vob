package turniplabs.examplemod.mixin;

import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import turniplabs.examplemod.itf.TesselatorExtensions;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Mixin(value = Tessellator.class, remap = false)
public abstract class TessellatorMixin implements TesselatorExtensions {
    @Shadow
    protected abstract void checkIsDrawing();

    @Shadow
    private boolean isDrawing;
    @Shadow
    private int vertexCount;
    @Shadow
    private IntBuffer intBuffer;
    @Shadow
    private int[] rawBuffer;
    @Shadow
    private int rawBufferIndex;
    @Shadow
    private ByteBuffer byteBuffer;
    @Shadow
    private boolean useVBO;
    @Shadow
    private int vboIndex;
    @Shadow
    private int vboCount;
    @Shadow
    private IntBuffer vertexBuffers;
    @Shadow
    private boolean hasTexture;
    @Shadow
    private FloatBuffer floatBuffer;
    @Shadow
    private boolean hasColor;
    @Shadow
    private boolean hasNormals;
    @Shadow
    private int drawMode;
    @Shadow
    private static boolean convertQuadsToTriangles;

    @Shadow
    protected abstract void reset();

    @Inject(at = @At("TAIL"), method = "<init>")
    public void postINit(int bufferSize, CallbackInfo ci) {
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void draw() {
        this.checkIsDrawing();
        this.isDrawing = false;
        if (this.vertexCount > 0) {
            this.intBuffer.clear();
            this.intBuffer.put(this.rawBuffer, 0, this.rawBufferIndex);
            this.byteBuffer.position(0);
            this.byteBuffer.limit(this.rawBufferIndex * 4);
            if (this.useVBO) {
                this.vboIndex = (this.vboIndex + 1) % this.vboCount;
                ARBVertexBufferObject.glBindBufferARB(34962, this.vertexBuffers.get(this.vboIndex));
                ARBVertexBufferObject.glBufferDataARB(34962, this.byteBuffer, 35040);
            }

            if (this.hasTexture) {
                if (this.useVBO) {
                    GL11.glTexCoordPointer(2, 5126, 32, 12L);
                } else {
                    this.floatBuffer.position(3);
                    GL11.glTexCoordPointer(2, 32, (FloatBuffer) this.floatBuffer);
                }

                GL11.glEnableClientState(32888);
            }

            if (this.hasColor) {
                if (this.useVBO) {
                    GL11.glColorPointer(4, 5121, 32, 20L);
                } else {
                    this.byteBuffer.position(20);
                    GL11.glColorPointer(4, true, 32, this.byteBuffer);
                }

                GL11.glEnableClientState(32886);
            }

            if (this.hasNormals) {
                if (this.useVBO) {
                    GL11.glNormalPointer(5120, 32, 24L);
                } else {
                    this.byteBuffer.position(24);
                    GL11.glNormalPointer(32, (ByteBuffer) this.byteBuffer);
                }

                GL11.glEnableClientState(32885);
            }

            if (this.useVBO) {
                GL11.glVertexPointer(3, 5126, 32, 0L);
            } else {
                this.floatBuffer.position(0);
                GL11.glVertexPointer(3, 32, (FloatBuffer) this.floatBuffer);
            }

            GL11.glEnableClientState(32884);
            if (this.drawMode == 7 && convertQuadsToTriangles) {
                GL11.glDrawArrays(4, 0, this.vertexCount);
            } else {
                GL11.glDrawArrays(this.drawMode, 0, this.vertexCount);
            }
            GL11.glDisableClientState(32884);

            if (this.hasTexture) {
                GL11.glDisableClientState(32888);
            }

            if (this.hasColor) {
                GL11.glDisableClientState(32886);
            }

            if (this.hasNormals) {
                GL11.glDisableClientState(32885);
            }
        }

        this.reset();
    }

    @Override
    public int[] genList(int list, int mode) {
        int vbo = ARBVertexBufferObject.glGenBuffersARB();
        int vcount = vertexCount;

        this.checkIsDrawing();
        this.isDrawing = false;
        if (this.vertexCount > 0) {
            GL11.glNewList(list, mode);
            this.intBuffer.clear();
            this.intBuffer.put(this.rawBuffer, 0, this.rawBufferIndex);
            this.byteBuffer.position(0);
            this.byteBuffer.limit(this.rawBufferIndex * 4);
            if (this.useVBO) {
                ARBVertexBufferObject.glBindBufferARB(34962, vbo);
                ARBVertexBufferObject.glBufferDataARB(34962, this.byteBuffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
            }

            if (this.hasTexture) {
                if (this.useVBO) {
                    GL11.glTexCoordPointer(2, 5126, 32, 12L);
                } else {
                    this.floatBuffer.position(3);
                    GL11.glTexCoordPointer(2, 32, (FloatBuffer) this.floatBuffer);
                }
            }

            if (this.hasColor) {
                if (this.useVBO) {
                    GL11.glColorPointer(4, 5121, 32, 20L);
                } else {
                    this.byteBuffer.position(20);
                    GL11.glColorPointer(4, true, 32, this.byteBuffer);
                }
            }

            if (this.hasNormals) {
                if (this.useVBO) {
                    GL11.glNormalPointer(5120, 32, 24L);
                } else {
                    this.byteBuffer.position(24);
                    GL11.glNormalPointer(32, (ByteBuffer) this.byteBuffer);
                }
            }

            if (this.useVBO) {
                GL11.glVertexPointer(3, 5126, 32, 0L);
            } else {
                this.floatBuffer.position(0);
                GL11.glVertexPointer(3, 32, (FloatBuffer) this.floatBuffer);
            }

            if (this.drawMode == 7 && convertQuadsToTriangles) {
                GL11.glDrawArrays(4, 0, this.vertexCount);
            } else {
                GL11.glDrawArrays(this.drawMode, 0, this.vertexCount);
            }
            GL11.glEndList();
        }

        int dmode = drawMode;

        this.reset();

        return new int[]{vbo, vcount, dmode};
    }
}
