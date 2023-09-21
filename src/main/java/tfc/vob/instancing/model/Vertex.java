package tfc.vob.instancing.model;

import net.minecraft.core.util.phys.Vec3d;

public class Vertex {
	public Vec3d vector3D;
	public float texturePositionX;
	public float texturePositionY;

	public Vertex(float f, float f1, float f2, float f3, float f4) {
		this(Vec3d.createVectorHelper((double)f, (double)f1, (double)f2), f3, f4);
	}

	public Vertex setTexturePosition(float f, float f1) {
		return new Vertex(this, f, f1);
	}

	public Vertex(Vertex positiontexturevertex, float f, float f1) {
		this.vector3D = positiontexturevertex.vector3D;
		this.texturePositionX = f;
		this.texturePositionY = f1;
	}

	public Vertex(Vec3d vec3d, float f, float f1) {
		this.vector3D = vec3d;
		this.texturePositionX = f;
		this.texturePositionY = f1;
	}
}
