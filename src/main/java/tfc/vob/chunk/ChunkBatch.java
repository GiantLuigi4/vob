package tfc.vob.chunk;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;

public class ChunkBatch {
    Batch[] batches;
    Vec2i[] offsets;
    int[] counts;
    int height;

    int numBatches;

    public ChunkBatch(Batch[] batches, int height) {
        this.batches = batches;
        height = 65536;
        this.height = height;
        for (int i = 0; i < batches.length; i++)
            batches[i] = new Batch(height);
        counts = new int[batches.length];
        offsets = new Vec2i[batches.length];
        for (int i = 0; i < offsets.length; i++)
            offsets[i] = new Vec2i(0, 0);
    }

    public void clear() {
        numBatches = 0;
        colHeight = 0;
        colNo = -1;
        current.clear();
    }

    public void draw(boolean reverse) {
        if (numBatches == 0) return;
        GL11.glDisableClientState(32885);

        GL11.glColorMask(true, true, true, true);
        GL11.glPushMatrix();
        if (true) {
            for (int i = 0; i < numBatches; i++) {
                Vec2i vec = offsets[i];
                GL11.glTranslatef(vec.x, 0, vec.z);

                Batch buf = batches[i];
                for (int i1 = 0; i1 < buf.idx; i1++) {
                    int[] vob = buf.vaos[i1];

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
                GL11.glTranslatef(-vec.x, 0, -vec.z);
            }
        } else {
            for (int i = numBatches; i >= 0; i--) {
                Vec2i vec = offsets[i];
                GL11.glTranslatef(vec.x, 0, vec.z);

                Batch buf = batches[i];
                for (int i1 = 0; i1 < buf.idx; i1++) {
                    int[] vob = buf.vaos[i1];

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

                GL11.glTranslatef(-vec.x, 0, -vec.z);
            }
        }
        GL11.glPopMatrix();

        ARBVertexBufferObject.glBindBufferARB(34962, 0);
    }

    int colNo;
    int colHeight;

    public void add(int[] glCallListForPass) {
        batches[colNo].put(glCallListForPass);
    }

    HashMap<Vec2i, Integer> current = new HashMap<>();

    public boolean nextColumn(int x, int z) {
        Vec2i crd = new Vec2i(x, z);
        Integer i = current.get(crd);
        if (i != null) {
            colNo = i;
            return true;
        }
        colNo = current.size();
//        colNo++;
        offsets[colNo].set(x, z);
        batches[colNo].position(0);
        numBatches = colNo + 1;
        current.put(crd, colNo);
        return true;
    }
}
