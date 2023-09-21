package tfc.vob.instancing.model;

import net.minecraft.client.render.Tessellator;
import net.minecraft.core.util.phys.Vec3d;

public class Polygon {
    public Vertex[] vertexPositions;
    public int nVertices;
    public boolean invertNormal;

    public Polygon(Vertex[] vertices) {
        this.invertNormal = false;
        this.vertexPositions = vertices;
        this.nVertices = vertices.length;
    }

    public Polygon(Vertex[] vertices, double minU, double minV, double maxU, double maxV, int texWidth, int texHeight) {
        this(vertices);
        float offsetU = 0.0015625F;
        float offsetV = 0.003125F;
        vertices[0] = vertices[0].setTexturePosition((float) maxU / (float) texWidth - offsetU, (float) minV / (float) texHeight + offsetV);
        vertices[1] = vertices[1].setTexturePosition((float) minU / (float) texWidth + offsetU, (float) minV / (float) texHeight + offsetV);
        vertices[2] = vertices[2].setTexturePosition((float) minU / (float) texWidth + offsetU, (float) maxV / (float) texHeight - offsetV);
        vertices[3] = vertices[3].setTexturePosition((float) maxU / (float) texWidth - offsetU, (float) maxV / (float) texHeight - offsetV);
    }

    public void flipFace() {
        Vertex[] vertices = new Vertex[this.vertexPositions.length];

        for (int i = 0; i < this.vertexPositions.length; ++i) {
            vertices[i] = this.vertexPositions[this.vertexPositions.length - i - 1];
        }

        this.vertexPositions = vertices;
    }

    void norm(Tessellator tessellator, float x, float y, float z) {
        if (invertNormal) {
            x *= -1;
            y *= -1;
            z *= -1;
        }
        x *= 0.5;
        x += 0.5;
        y *= 0.5;
        y += 0.5;
        z *= 0.5;
        z += 0.5;

        tessellator.setColorOpaque_F(x, y, z);
    }

    public void draw(Tessellator tessellator, float scale) {
        Vec3d vec3d = this.vertexPositions[1].vector3D.subtract(this.vertexPositions[0].vector3D);
        Vec3d vec3d1 = this.vertexPositions[1].vector3D.subtract(this.vertexPositions[2].vector3D);
        Vec3d vec3d2 = vec3d1.crossProduct(vec3d).normalize();

        norm(tessellator, (float) vec3d2.xCoord, (float) vec3d2.yCoord, (float) vec3d2.zCoord);
//        if (this.invertNormal)
//            tessellator.setColorOpaque_F(-((float) vec3d2.xCoord), -((float) vec3d2.yCoord), -((float) vec3d2.zCoord));
//        else tessellator.setColorOpaque_F((float) vec3d2.xCoord, (float) vec3d2.yCoord, (float) vec3d2.zCoord);

        for (int i = 0; i < 4; ++i) {
            Vertex positiontexturevertex = this.vertexPositions[i];
            tessellator.addVertexWithUV(
                    (float) positiontexturevertex.vector3D.xCoord * scale,
                    (float) positiontexturevertex.vector3D.yCoord * scale,
                    (float) positiontexturevertex.vector3D.zCoord * scale,
                    positiontexturevertex.texturePositionX,
                    positiontexturevertex.texturePositionY
            );
        }
    }
}
