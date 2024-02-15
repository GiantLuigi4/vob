package tfc.vob.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.ChunkRendererComparator;
import net.minecraft.client.render.DisplayList;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.culling.CameraFrustum;
import net.minecraft.core.util.collection.Pair;
import net.minecraft.core.world.World;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.vob.Config;
import tfc.vob.chunk.Batch;
import tfc.vob.chunk.ChunkBatch;
import tfc.vob.itf.ChunkRendererExtension;

import java.util.*;

@Mixin(value = RenderGlobal.class, remap = false)
public abstract class RenderGlobalMixin {
    @Shadow
    private List<ChunkRenderer> glRenderLists;
    @Shadow
    private int renderersLoaded;
    @Shadow
    private ChunkRenderer[] sortedChunkRenderers;
    @Shadow
    private int renderersSkippingRenderPass;
    @Shadow
    private int renderersBeingClipped;
    @Shadow
    private boolean occlusionEnabled;
    @Shadow
    private int renderersBeingOccluded;
    @Shadow
    private int renderersBeingRendered;
    @Shadow
    private Minecraft mc;
    @Shadow
    private DisplayList[] allDisplayLists;

    @Shadow
    public abstract void callAllDisplayLists(int renderPass, double renderPartialTicks);

    @Shadow
    private List<ChunkRenderer> chunkRenderersToUpdate;

    @Shadow
    private ChunkRenderer[] chunkRenderers;

    /**
     * @author
     * @reason
     */
    @Overwrite
    private int renderSortedRenderers(int min, int max, int renderPass, double renderPartialTicks) {
        this.glRenderLists.clear();
        int addedWorldRenderers = 0;

        double posX = this.mc.activeCamera.getX((float) renderPartialTicks);
        double posY = this.mc.activeCamera.getY((float) renderPartialTicks);
        double posZ = this.mc.activeCamera.getZ((float) renderPartialTicks);

//        int k1 = 0;
        DisplayList[] lists = this.allDisplayLists;

        int displayListIndex = -1;
        for (DisplayList allDisplayList : allDisplayLists)
            allDisplayList.clear();

        for (int i = max - 1; i >= min; --i) {
            if (renderPass == 0) {
                ++this.renderersLoaded;
                if (this.sortedChunkRenderers[i].skipRenderPass[renderPass]) {
                    ++this.renderersSkippingRenderPass;
                } else if (!this.sortedChunkRenderers[i].isInFrustum) {
                    ++this.renderersBeingClipped;
                } else if (this.occlusionEnabled && !this.sortedChunkRenderers[i].isVisible) {
                    ++this.renderersBeingOccluded;
                } else {
                    ++this.renderersBeingRendered;
                }
            }

            if (!this.sortedChunkRenderers[i].skipRenderPass[renderPass] && this.sortedChunkRenderers[i].isInFrustum && (!this.occlusionEnabled || this.sortedChunkRenderers[i].isVisible)) {
                int callList = this.sortedChunkRenderers[i].getGLCallListForPass(renderPass);
                if (callList >= 0) {
                    ChunkRenderer chunkRenderer = this.sortedChunkRenderers[i];

                    if (displayListIndex == -1 || !lists[displayListIndex].isSetToPos(chunkRenderer.posXMinus, chunkRenderer.posYMinus, chunkRenderer.posZMinus)) {
                        displayListIndex++;

                        lists[displayListIndex].setToPos(chunkRenderer.posXMinus, chunkRenderer.posYMinus, chunkRenderer.posZMinus, posX, posY, posZ);
                    }

                    lists[displayListIndex].addCallToList(chunkRenderer.getGLCallListForPass(renderPass));

                    ++addedWorldRenderers;
                }
            }
        }

        boolean fog = !(Boolean) this.mc.gameSettings.fog.value;
        if (fog) GL11.glDisable(2912);
        this.callAllDisplayLists(renderPass, renderPartialTicks);
        if (fog) GL11.glEnable(2912);

        return addedWorldRenderers;
    }

    @Inject(at = @At("HEAD"), method = "loadRenderers")
    public void preLoad(CallbackInfo ci) {
        if (chunkRenderers != null) {
            for (ChunkRenderer chunkRenderer : chunkRenderers) {
                if (chunkRenderer != null) {
                    ((ChunkRendererExtension) chunkRenderer).close();
                }
            }
        }
    }

//    /**
//     * @author
//     * @reason
//     */
//    @Overwrite
//    public boolean updateRenderers(ICamera camera) {
//        if (chunkRenderersToUpdate.isEmpty())
//            return true;
//
//        CameraFrustum frustum = new CameraFrustum(camera);
//
//        ChunkRendererComparator comparator = new ChunkRendererComparator(camera);
//        float nearest = 256.f;
//        int idx = 0;
//        float actualNearest = Float.POSITIVE_INFINITY;
//        int idxNearest = 0;
//
//        ListIterator<ChunkRenderer> rendererListIterator = chunkRenderersToUpdate.listIterator();
//        while (rendererListIterator.hasNext()) {
//            ChunkRenderer chunkRenderer = rendererListIterator.next();
//
//            float d = chunkRenderer.distanceToCameraSquared(camera);
//            if (d < nearest) {
//                chunkRenderer.updateRenderer();
//                chunkRenderer.needsUpdate = false;
//                rendererListIterator.remove();
//            } else {
//                if (d < actualNearest) {
//                    actualNearest = d;
//                    idxNearest = idx;
//                }
//            }
//
//            idx++;
//        }
//
//        ChunkRenderer rdr = chunkRenderersToUpdate.remove(idxNearest);
//        rdr.updateRenderer();
//        rdr.needsUpdate = false;
//
//        return chunkRenderersToUpdate.isEmpty();
//    }
}
