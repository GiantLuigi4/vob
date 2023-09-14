package turniplabs.examplemod.mixin;

import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import turniplabs.examplemod.ExampleMod;
import turniplabs.examplemod.itf.ChunkRendererExtension;
import turniplabs.examplemod.itf.TesselatorExtensions;

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
    int[] vbo = new int[]{-1, 0};
    int[] vbo1 = new int[]{-1, 0};

    int vao = ExampleMod.useVAOs ? ARBVertexArrayObject.glGenVertexArrays() : -1;

    @Inject(at = @At("HEAD"), method = "updateRenderer")
    public void preUR(CallbackInfo ci) {
        if (!ExampleMod.useVAOs) return;
        ExampleMod.WorldDraw = true;

        ARBVertexArrayObject.glBindVertexArray(vao);
        GL11.glTexCoordPointer(2, 5126, 32, 12L);
        GL11.glEnableClientState(32888);
        GL11.glColorPointer(4, 5121, 32, 20L);
        GL11.glEnableClientState(32886);
        GL11.glEnableClientState(32884);
        GL11.glVertexPointer(3, 5126, 32, 0L);
    }

    @Inject(at = @At("RETURN"), method = "updateRenderer")
    public void postUR(CallbackInfo ci) {
        if (!ExampleMod.useVAOs) return;
        GL11.glDisableClientState(32888);
        GL11.glDisableClientState(32886);
        GL11.glDisableClientState(32884);
        tessellator.setTranslation(0, 0, 0);
        ExampleMod.WorldDraw = false;
        ARBVertexArrayObject.glBindVertexArray(0);
    }

    int li, mod;

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V"))
    public void noList(int list, int mode) {
        if (!ExampleMod.useVAOs) GL11.glNewList(list, mode);
        li = list;
        mod = mode;
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEndList()V"))
    public void noList() {
        if (!ExampleMod.useVAOs) {
            GL11.glEndList();
        }
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Tessellator;setTranslation(DDD)V"))
    public void preSetTranslation(Tessellator instance, double x, double y, double z) {
        if (ExampleMod.useVAOs)
            instance.setTranslation(x + posXClip, y + posYClip, z + posZClip);
        else instance.setTranslation(x, y, z);
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Tessellator;draw()V"))
    public void draw(Tessellator instance) {
        if (!ExampleMod.useVAOs) {
            instance.draw();
            return;
        }

        if ((li - glRenderList) == 0) {
            if (vbo1[0] != -1) {
                ARBVertexBufferObject.glDeleteBuffersARB(vbo1[0]);
                vbo1[0] = -1;
            }

            vbo1 = ((TesselatorExtensions) instance).genList(li, mod);
        } else {
            if (vbo[0] != -1) {
                ARBVertexBufferObject.glDeleteBuffersARB(vbo[0]);
                vbo[0] = -1;
            }

            vbo = ((TesselatorExtensions) instance).genList(li, mod);
        }
    }

    @Override
    public void draw(int i) {
        if (isInFrustum && !skipRenderPass[i]) {
            int[] vob = i == 0 ? vbo1 : vbo;
            if (vob[0] < 0 || vob[1] < 3) return;

            GL11.glPushMatrix();
            GL11.glTranslatef(posXMinus, posYMinus, posZMinus);
            ARBVertexBufferObject.glBindBufferARB(34962, vob[0]);
            GL11.glTexCoordPointer(2, 5126, 32, 12L);
            GL11.glColorPointer(4, 5121, 32, 20L);
            GL11.glVertexPointer(3, 5126, 32, 0L);

            if (vob[2] == 7 && true) {
                GL11.glDrawArrays(4, 0, vob[1]);
            } else {
                GL11.glDrawArrays(vob[2], 0, vob[1]);
            }

            GL11.glTranslatef(posXMinus, posYMinus, posZMinus);
            GL11.glPopMatrix();
        }
    }

    @Override
    public int[] getVao(int pass) {
        return pass == 0 ? vbo1 : vbo;
    }
}
