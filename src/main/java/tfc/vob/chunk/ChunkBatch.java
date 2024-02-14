package tfc.vob.chunk;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import tfc.vob.Config;
import tfc.vob.util.VAOAllocator;

public class ChunkBatch {
	Batch[] batches;
	Vec2i[] offsets;
	int[] counts;
	int height;
	
	int numBatches;
	
	public ChunkBatch(Batch[] batches, int height) {
		this.batches = batches;
		height = 65536; // this is what base BTA uses for the size of a batch
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
	}
	
	public void draw() {
		if (numBatches == 0) return;

		GL11.glColorMask(true, true, true, true);
		GL11.glPushMatrix();
		for (int i = 0; i < numBatches; i++) {
			Vec2i vec = offsets[i];
			GL11.glTranslatef(vec.x, 0, vec.z);
			
			Batch buf = batches[i];
			for (int i1 = 0; i1 < buf.idx; i1++) {
				int[] vob = buf.vaos[i1];

				if (vob[3] != 0) {
					VAOAllocator.INSTANCE.bind(vob[3]);
				} else {
					ARBVertexBufferObject.glBindBufferARB(34962, vob[0]);
					GL11.glTexCoordPointer(2, 5126, 32, 12L);
					GL11.glColorPointer(4, 5121, 32, 20L);
					GL11.glVertexPointer(3, 5126, 32, 0L);
				}

				if (vob[2] == 7 && true) {
					GL11.glDrawArrays(4, 0, vob[1]);
				} else {
					GL11.glDrawArrays(vob[2], 0, vob[1]);
				}
			}
			GL11.glTranslatef(-vec.x, 0, -vec.z);
		}
		GL11.glPopMatrix();

		VAOAllocator.INSTANCE.bind(0);
		ARBVertexBufferObject.glBindBufferARB(34962, 0);
	}
	
	int colNo;
	int colHeight;
	
	public void add(int[] glCallListForPass) {
		batches[colNo].put(glCallListForPass);
	}
	
	public boolean nextColumn(int x, int z) {
		if (numBatches >= (Config.maxBatches )) {
			draw();
			clear();
		}
		colNo++;
		offsets[colNo].set(x, z);
		batches[colNo].position(0);
		numBatches = colNo + 1;
		return true;
	}
}
