package tfc.vob.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.DisplayList;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.camera.ICamera;
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

import java.util.List;

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
    private ChunkRenderer[] chunkRenderers;


    @Shadow
    private int renderChunksDeep;
    @Shadow
    private int renderChunksWide;
    @Shadow
    private int renderChunksTall;
    @Shadow
    private World worldObj;

    ChunkBatch batch;

    /**
     * @author
     * @reason
     */
    @Overwrite
    private int renderSortedRenderers(int min, int max, int renderPass, double renderPartialTicks) {
        this.glRenderLists.clear();
        int addedWorldRenderers = 0;

        if (!Config.vobPipeline) {
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
                        this.glRenderLists.add(this.sortedChunkRenderers[i]);
                        ++addedWorldRenderers;
                    }
                }
            }

            double posX = this.mc.activeCamera.getX((float) renderPartialTicks);
            double posY = this.mc.activeCamera.getY((float) renderPartialTicks);
            double posZ = this.mc.activeCamera.getZ((float) renderPartialTicks);
            int k1 = 0;
            DisplayList[] lists = this.allDisplayLists;

            int displayListIndex;
            for (DisplayList allDisplayList : allDisplayLists)
                allDisplayList.clear();

            for (ChunkRenderer chunkRenderer : glRenderLists) {
                displayListIndex = -1;

                for (int i = 0; i < k1; ++i) {
                    if (lists[i].isSetToPos(chunkRenderer.posXMinus, chunkRenderer.posYMinus, chunkRenderer.posZMinus)) {
                        displayListIndex = i;
                        break;
                    }
                }

                if (displayListIndex < 0) {
                    displayListIndex = k1++;
                    lists[displayListIndex].setToPos(chunkRenderer.posXMinus, chunkRenderer.posYMinus, chunkRenderer.posZMinus, posX, posY, posZ);
                }

                lists[displayListIndex].addCallToList(chunkRenderer.getGLCallListForPass(renderPass));
            }

            this.callAllDisplayLists(renderPass, renderPartialTicks);

            return addedWorldRenderers;
        }

        if (Config.useBatching) {
            int lx = Integer.MIN_VALUE;
            int ly = Integer.MAX_VALUE;
            for (int i = min; i < max; i++) {
                ChunkRenderer sortedChunkRenderer = sortedChunkRenderers[i];
                if (renderPass == 0) {
                    ++this.renderersLoaded;
                    if (sortedChunkRenderer.skipRenderPass[renderPass]) {
                        ++this.renderersSkippingRenderPass;
                    } else if (!sortedChunkRenderer.isInFrustum) {
                        ++this.renderersBeingClipped;
                    } else if (this.occlusionEnabled && !sortedChunkRenderer.isVisible) {
                        ++this.renderersBeingOccluded;
                    } else {
                        ++this.renderersBeingRendered;
                    }
                }

                if (!sortedChunkRenderer.skipRenderPass[renderPass] && sortedChunkRenderer.isInFrustum && (!this.occlusionEnabled || sortedChunkRenderer.isVisible)) {
                    if (lx != sortedChunkRenderer.posXMinus || ly != sortedChunkRenderer.posZMinus) {
                        lx = sortedChunkRenderer.posXMinus;
                        ly = sortedChunkRenderer.posZMinus;
                        batch.nextColumn(lx, ly);
                    }

                    batch.add(((ChunkRendererExtension) sortedChunkRenderer).getVao(renderPass));

                    addedWorldRenderers++;
                }
            }
        } else {
            double posX = this.mc.activeCamera.getX((float) renderPartialTicks);
            double posY = this.mc.activeCamera.getY((float) renderPartialTicks);
            double posZ = this.mc.activeCamera.getZ((float) renderPartialTicks);

            GL11.glPushMatrix();
            GL11.glTranslated(-posX, -posY, -posZ);
            GL11.glEnableClientState(32888);
            GL11.glEnableClientState(32886);
            GL11.glEnableClientState(32884);

            for (int i = min; i < max; i++) {
                ChunkRenderer sortedChunkRenderer = sortedChunkRenderers[i];
                if (renderPass == 0) {
                    ++this.renderersLoaded;
                    if (sortedChunkRenderer.skipRenderPass[renderPass]) {
                        ++this.renderersSkippingRenderPass;
                    } else if (!sortedChunkRenderer.isInFrustum) {
                        ++this.renderersBeingClipped;
                    } else if (this.occlusionEnabled && !sortedChunkRenderer.isVisible) {
                        ++this.renderersBeingOccluded;
                    } else {
                        ++this.renderersBeingRendered;
                    }
                }
                if (!sortedChunkRenderer.skipRenderPass[renderPass] && sortedChunkRenderer.isInFrustum && (!this.occlusionEnabled || sortedChunkRenderer.isVisible)) {
                    ((ChunkRendererExtension) sortedChunkRenderer).draw(renderPass);
                    addedWorldRenderers++;
                }
            }

            GL11.glDisableClientState(32888);
            GL11.glDisableClientState(32886);
            GL11.glDisableClientState(32884);
            GL11.glPopMatrix();
        }

        return addedWorldRenderers;
    }

    @Inject(method = "sortAndRender", at = @At("HEAD"))
    public void preRender(ICamera camera, int renderPass, double renderPartialTicks, CallbackInfoReturnable<Integer> cir) {
        if (Config.vobPipeline) {
            if (Config.useBatching) {
                double posX = this.mc.activeCamera.getX((float) renderPartialTicks);
                double posY = this.mc.activeCamera.getY((float) renderPartialTicks);
                double posZ = this.mc.activeCamera.getZ((float) renderPartialTicks);

                GL11.glPushMatrix();
                GL11.glTranslated(-posX, -posY, -posZ);
                GL11.glEnableClientState(32888);
                GL11.glEnableClientState(32886);
                GL11.glEnableClientState(32884);

                batch.clear();
            }
        }

        if (!(Boolean) this.mc.gameSettings.fog.value)
            GL11.glDisable(2912);
    }

    @Inject(method = "sortAndRender", at = @At("RETURN"))
    public void postRender(ICamera camera, int renderPass, double renderPartialTicks, CallbackInfoReturnable<Integer> cir) {
        if (Config.vobPipeline) {
            if (Config.useBatching) {
                batch.draw();

                GL11.glDisableClientState(32888);
                GL11.glDisableClientState(32886);
                GL11.glDisableClientState(32884);
                GL11.glPopMatrix();
            }
        }

        if (!(Boolean) this.mc.gameSettings.fog.value)
            GL11.glEnable(2912);
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

    @Inject(at = @At("TAIL"), method = "loadRenderers")
    public void postLoad(CallbackInfo ci) {
        if (worldObj != null && Config.useBatching)
            batch = new ChunkBatch(new Batch[Config.maxBatches], renderChunksTall);
    }
}
