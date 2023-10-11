package tfc.vob.mixin;

import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.Tessellator;
import net.minecraft.core.world.World;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.vob.Config;
import tfc.vob.hook.ChunkRenderHooks;
import tfc.vob.itf.ChunkRendererExtension;
import tfc.vob.itf.TesselatorExtensions;
import tfc.vob.util.VAOAllocator;

import java.util.List;

@Mixin(value = ChunkRenderer.class, remap = false)
public abstract class ChunkRendererMixin implements ChunkRendererExtension {
    @Shadow
    public int posX;

    @Shadow
    public int posXClip;

    @Shadow
    public int posYClip;

    @Shadow
    public int posZClip;

    @Shadow
    @Final
    private static Tessellator tessellator;

    @Shadow
    public boolean isInFrustum;
    @Shadow
    public boolean[] skipRenderPass;
    @Shadow
    @Final
    private int glRenderList;

    @Shadow
    protected abstract void setupGLTranslation();

    @Shadow
    public int posZ;
    @Shadow
    public int posY;
    @Shadow
    public int posXMinus;
    @Shadow
    public int posYMinus;
    @Shadow
    public int posZMinus;
    int[] vbo;
    int[] vbo1;

    int vao;
    boolean allowVao = false;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void postInit(RenderEngine renderEngine, World world, List list, int posX, int posY, int posZ, int size, int renderList, CallbackInfo ci) {
        vbo = new int[]{-1, 0, 4, vao};
        vbo1 = new int[]{-1, 0, 4, vao};
        vao = Config.vobPipeline ? VAOAllocator.INSTANCE.generate() : -1;
    }

    @Inject(at = @At("HEAD"), method = "updateRenderer")
    public void preUR(CallbackInfo ci) {
        if (!Config.vobPipeline) return;

        VAOAllocator.INSTANCE.bind(vao);
        if (!Config.skipList) {
            GL11.glTexCoordPointer(2, 5126, 32, 12L);
            GL11.glEnableClientState(32888);
            GL11.glColorPointer(4, 5121, 32, 20L);
            GL11.glEnableClientState(32886);
            GL11.glEnableClientState(32884);
            GL11.glVertexPointer(3, 5126, 32, 0L);
        }
    }

    @Inject(at = @At("RETURN"), method = "updateRenderer")
    public void postUR(CallbackInfo ci) {
        if (!Config.vobPipeline) return;
        if (!Config.skipList) {
            GL11.glDisableClientState(32888);
            GL11.glDisableClientState(32886);
            GL11.glDisableClientState(32884);
        }
        tessellator.setTranslation(0, 0, 0);
        VAOAllocator.INSTANCE.bind(0);
    }

    int li, mod;

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V"))
    public void noList(int list, int mode) {
        if (!Config.skipList)
            GL11.glNewList(list, mode);
        li = list;
        mod = mode;
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEndList()V"))
    public void noList() {
        if (!Config.skipList)
            GL11.glEndList();
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Tessellator;setTranslation(DDD)V"))
    public void preSetTranslation(Tessellator instance, double x, double y, double z) {
        if (Config.vobPipeline)
            instance.setTranslation(x + posXClip, y + posYClip, z + posZClip);
        else instance.setTranslation(x, y, z);
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Tessellator;draw()V"))
    public void draw(Tessellator instance) {
        if (!Config.vobPipeline) {
            instance.draw();
            return;
        }

        if ((li - glRenderList) == 0) {
            if (vbo1[0] != -1) {
                ARBVertexBufferObject.glDeleteBuffersARB(vbo1[0]);
                vbo1[0] = -1;
            }

            vbo1 = ((TesselatorExtensions) instance).genList(li, mod);
            if (allowVao) vbo1[3] = vao;
        } else {
            if (vbo[0] != -1) {
                ARBVertexBufferObject.glDeleteBuffersARB(vbo[0]);
                vbo[0] = -1;
            }

            vbo = ((TesselatorExtensions) instance).genList(li, mod);
            if (allowVao) vbo[3] = vao;
        }
        // VAOs require proper shaders in order to work
        allowVao = ChunkRenderHooks.postBakeChunk();
    }

    @Override
    public void draw(int i) {
        if (isInFrustum && !skipRenderPass[i]) {
            int[] vob = i == 0 ? vbo1 : vbo;
            if (vob[0] < 0 || vob[1] < 3) return;

            GL11.glPushMatrix();
            GL11.glTranslatef(posXMinus, posYMinus, posZMinus);
            if (allowVao) {
                VAOAllocator.INSTANCE.bind(vao);
                ARBVertexBufferObject.glBindBufferARB(34962, vob[0]);

                if (vob[2] == 7 && true) {
                    GL11.glDrawArrays(4, 0, vob[1]);
                } else {
                    GL11.glDrawArrays(vob[2], 0, vob[1]);
                }

                VAOAllocator.INSTANCE.bind(0);
            } else {
                ARBVertexBufferObject.glBindBufferARB(34962, vob[0]);
                GL11.glTexCoordPointer(2, 5126, 32, 12L);
                GL11.glColorPointer(4, 5121, 32, 20L);
                GL11.glVertexPointer(3, 5126, 32, 0L);

                if (vob[2] == 7 && true) {
                    GL11.glDrawArrays(4, 0, vob[1]);
                } else {
                    GL11.glDrawArrays(vob[2], 0, vob[1]);
                }
            }
            GL11.glTranslatef(posXMinus, posYMinus, posZMinus);
            GL11.glPopMatrix();
        }
    }

    @Override
    public int[] getVao(int pass) {
        return pass == 0 ? vbo1 : vbo;
    }

    @Override
    public void close() {
        if (vbo[0] >= 0) ARBVertexBufferObject.glDeleteBuffersARB(vbo[0]);
        if (vbo1[0] >= 0) ARBVertexBufferObject.glDeleteBuffersARB(vbo1[0]);
        if (vao >= 0) VAOAllocator.INSTANCE.delete(vao);
    }
}
