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
import tfc.vob.itf.ChunkRendererExtension;
import tfc.vob.itf.TesselatorExtensions;
import tfc.vob.util.VAOAllocator;

import java.util.List;

@Mixin(value = ChunkRenderer.class, remap = false)
public abstract class ChunkRendererMixin implements ChunkRendererExtension {
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
    @Final
    private int glRenderList;

    @Shadow public boolean[] skipRenderPass;
    int vao, vao1;
    int vbo = ARBVertexBufferObject.glGenBuffersARB();
    int vbo1 = ARBVertexBufferObject.glGenBuffersARB();

    @Inject(at = @At("TAIL"), method = "<init>")
    public void postInit(RenderEngine renderEngine, World world, List list, int posX, int posY, int posZ, int size, int renderList, CallbackInfo ci) {
        vao = VAOAllocator.INSTANCE.generate();
        vao1 = VAOAllocator.INSTANCE.generate();
    }

    @Inject(at = @At("RETURN"), method = "updateRenderer")
    public void postUR(CallbackInfo ci) {
        tessellator.setTranslation(0, 0, 0);
        VAOAllocator.INSTANCE.bind(0);
    }

    int li, mod;

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glNewList(II)V"))
    public void noList(int list, int mode) {
        li = list;
        mod = mode;
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEndList()V"))
    public void noList() {
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Tessellator;setTranslation(DDD)V"))
    public void preSetTranslation(Tessellator instance, double x, double y, double z) {
        instance.setTranslation(x + posXClip, y + posYClip, z + posZClip);
    }

    @Redirect(method = "updateRenderer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Tessellator;draw()V"))
    public void draw(Tessellator instance) {
        int l = li - glRenderList;
        VAOAllocator.INSTANCE.bind(l == 0 ? vao1 : vao);
        int verts = ((TesselatorExtensions) tessellator).genList(
                li, mod, l == 0 ? vbo1 : vbo
        );

        if (verts == 0) {
            skipRenderPass[l] = true;
        } else {
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

            GL11.glNewList(li, mod);
            VAOAllocator.INSTANCE.bind(l == 0 ? vao1 : vao);
            GL11.glDrawArrays(4, 0, verts);
            GL11.glEndList();
        }

        tessellator.setTranslation(0, 0, 0);
    }

    @Override
    public void close() {
        if (vbo >= 0) ARBVertexBufferObject.glDeleteBuffersARB(vbo);
        if (vbo1 >= 0) ARBVertexBufferObject.glDeleteBuffersARB(vbo1);
        if (vao >= 0) VAOAllocator.INSTANCE.delete(vao);
        if (vao1 >= 0) VAOAllocator.INSTANCE.delete(vao1);
    }
}
