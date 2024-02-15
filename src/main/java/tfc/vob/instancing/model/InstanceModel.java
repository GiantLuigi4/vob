package tfc.vob.instancing.model;

import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.ARBDrawInstanced;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL20;
import tfc.vob.instancing.InstancingShader;
import tfc.vob.instancing.yaml.Hsml;
import tfc.vob.itf.TesselatorExtensions;
import tfc.vob.util.VAOAllocator;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class InstanceModel {
    public final InstancingShader shader = new InstancingShader();
    ArrayList<ModelElement> elements = new ArrayList<>();

    public final int maxInstances;

    int vao;
    int[] vbo;

    public InstanceModel(String category, String name) {
        try {
            InputStream strm = InstanceModel.class.getClassLoader()
                    .getResourceAsStream("assets/instancing/" + category + "/model/" + name + ".yaml");
            byte[] data = new byte[strm.available()];
            strm.read(data);
            strm.close();

            String yml = new String(data);
            Hsml hsml = new Hsml(yml);

            Hsml sdr = hsml.getYaml("shader");
            shader.parse(category + "/" + name, sdr.getText("fragment"), sdr.getText("vertex"));
            maxInstances = Integer.parseInt(sdr.getText("max_instances"));

            Hsml theModel = hsml.getYaml("model");
            HashMap<Integer, ModelElement> elements = new HashMap<>();
            for (String key : theModel.keys()) {
                Hsml el = theModel.getYaml(key);

                ModelElement element = new ModelElement(
                        Integer.parseInt(el.getText("id")),
                        el.getText("pivot")
                );
                elements.put(element.id, element);

                for (String s : el.keys()) {
                    if (s.equals("id") || s.equals("pivot")) continue;
                    element.addCube(el.getYaml(s));
                }
            }

            TreeSet<Integer> ints = new TreeSet<>(elements.keySet());
            for (Integer anInt : ints) this.elements.add(elements.get(anInt));
        } catch (Throwable err) {
            err.printStackTrace();
            throw new RuntimeException(err);
        }

        vao = VAOAllocator.INSTANCE.generate();

        VAOAllocator.INSTANCE.bind(vao);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        for (ModelElement element : elements) {
            for (ModelElement.ModelCube cube : element.cubes) {
                drawBox(
                        tessellator,
                        cube.cx, cube.cy, cube.cz,
                        cube.sx, cube.sy, cube.sz,
                        0, false, false,
                        cube.u, cube.v,
                        64, 32
                );
            }
        }
        vbo = ((TesselatorExtensions) tessellator).legacy$genList(0, 0);
        VAOAllocator.INSTANCE.bind(0);
    }

    public void bind() {
        shader.bind();

        ArrayList<Float> floats = new ArrayList<>();
        for (ModelElement element : elements) {
            for (ModelElement.ModelCube cube : element.cubes) {
                floats.add((float) element.px);
                floats.add((float) element.py);
                floats.add((float) element.pz);
            }
        }
        float[] array = new float[floats.size()];
        for (int i = 0; i < array.length; i++) array[i] = floats.get(i);
        shader.arrayUniform("pivots", array, 3);
    }

    public void unbind() {
        shader.unbind();
    }

    public void draw(int count) {
        VAOAllocator.INSTANCE.bind(vao);
        ARBVertexBufferObject.glBindBufferARB(34962, vbo[0]);

        GL20.glVertexAttribPointer(2, 2, 5126, false, 32, 12L);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(1, 3, 5121, true, 32, 20L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(0, 3, 5126, false, 32, 0L);
        GL20.glEnableVertexAttribArray(0);

        if (vbo[2] == 7 && true) {
            ARBDrawInstanced.glDrawArraysInstancedARB(4, 0, vbo[1], count);
//            GL11.glDrawArrays(4, 0, vbo[1]);
        } else {
            ARBDrawInstanced.glDrawArraysInstancedARB(vbo[2], 0, vbo[1], count);
//            GL11.glDrawArrays(vbo[2], 0, vbo[1]);
        }

        VAOAllocator.INSTANCE.bind(0);
    }

    private Vertex[] corners;
    private Polygon[] faces;

    protected void drawBox(
            Tessellator tessellator,
            double minX, double minY, double minZ,
            double sizeX, double sizeY, double sizeZ,
            double expandAmount,
            boolean mirror, boolean flipBottomUV,
            double textureU, double textureV,
            int texWidth, int texHeight
    ) {
        this.corners = new Vertex[8];
        this.faces = new Polygon[6];
        double maxX = minX + sizeX;
        double maxY = minY + sizeY;
        double maxZ = minZ + sizeZ;
        minX -= expandAmount;
        minY -= expandAmount;
        minZ -= expandAmount;
        maxX += expandAmount;
        maxY += expandAmount;
        maxZ += expandAmount;
        if (mirror) {
            double temp = maxX;
            maxX = minX;
            minX = temp;
        }

        Vertex ptvMinXMinYMinZ = new Vertex((float) minX, (float) minY, (float) minZ, 0.0F, 0.0F);
        Vertex ptvMaxXMinYMinZ = new Vertex((float) maxX, (float) minY, (float) minZ, 0.0F, 8.0F);
        Vertex ptvMaxXMaxYMinZ = new Vertex((float) maxX, (float) maxY, (float) minZ, 8.0F, 8.0F);
        Vertex ptvMinXMaxYMinZ = new Vertex((float) minX, (float) maxY, (float) minZ, 8.0F, 0.0F);
        Vertex ptvMinXMinYMaxZ = new Vertex((float) minX, (float) minY, (float) maxZ, 0.0F, 0.0F);
        Vertex ptvMaxXMinYMaxZ = new Vertex((float) maxX, (float) minY, (float) maxZ, 0.0F, 8.0F);
        Vertex ptvMaxXMaxYMaxZ = new Vertex((float) maxX, (float) maxY, (float) maxZ, 8.0F, 8.0F);
        Vertex ptvMinXMaxYMaxZ = new Vertex((float) minX, (float) maxY, (float) maxZ, 8.0F, 0.0F);
        this.corners[0] = ptvMinXMinYMinZ;
        this.corners[1] = ptvMaxXMinYMinZ;
        this.corners[2] = ptvMaxXMaxYMinZ;
        this.corners[3] = ptvMinXMaxYMinZ;
        this.corners[4] = ptvMinXMinYMaxZ;
        this.corners[5] = ptvMaxXMinYMaxZ;
        this.corners[6] = ptvMaxXMaxYMaxZ;
        this.corners[7] = ptvMinXMaxYMaxZ;
        this.faces[0] = new Polygon(new Vertex[]{ptvMaxXMinYMaxZ, ptvMaxXMinYMinZ, ptvMaxXMaxYMinZ, ptvMaxXMaxYMaxZ}, textureU + sizeZ + sizeX, textureV + sizeZ, textureU + sizeZ + sizeX + sizeZ, textureV + sizeZ + sizeY, texWidth, texHeight);
        this.faces[1] = new Polygon(new Vertex[]{ptvMinXMinYMinZ, ptvMinXMinYMaxZ, ptvMinXMaxYMaxZ, ptvMinXMaxYMinZ}, textureU, textureV + sizeZ, textureU + sizeZ, textureV + sizeZ + sizeY, texWidth, texHeight);
        this.faces[2] = new Polygon(new Vertex[]{ptvMaxXMinYMaxZ, ptvMinXMinYMaxZ, ptvMinXMinYMinZ, ptvMaxXMinYMinZ}, textureU + sizeZ, textureV, textureU + sizeZ + sizeX, textureV + sizeZ, texWidth, texHeight);
        if (flipBottomUV) {
            this.faces[3] = new Polygon(new Vertex[]{ptvMaxXMaxYMaxZ, ptvMinXMaxYMaxZ, ptvMinXMaxYMinZ, ptvMaxXMaxYMinZ}, textureU + sizeZ + sizeX, textureV, textureU + sizeZ + sizeX + sizeX, textureV + sizeZ, texWidth, texHeight);
            this.faces[3].invertNormal = true;
        } else {
            this.faces[3] = new Polygon(new Vertex[]{ptvMaxXMaxYMinZ, ptvMinXMaxYMinZ, ptvMinXMaxYMaxZ, ptvMaxXMaxYMaxZ}, textureU + sizeZ + sizeX, textureV, textureU + sizeZ + sizeX + sizeX, textureV + sizeZ, texWidth, texHeight);
        }

        this.faces[4] = new Polygon(new Vertex[]{ptvMaxXMinYMinZ, ptvMinXMinYMinZ, ptvMinXMaxYMinZ, ptvMaxXMaxYMinZ}, textureU + sizeZ, textureV + sizeZ, textureU + sizeZ + sizeX, textureV + sizeZ + sizeY, texWidth, texHeight);
        this.faces[5] = new Polygon(new Vertex[]{ptvMinXMinYMaxZ, ptvMaxXMinYMaxZ, ptvMaxXMaxYMaxZ, ptvMinXMaxYMaxZ}, textureU + sizeZ + sizeX + sizeZ, textureV + sizeZ, textureU + sizeZ + sizeX + sizeZ + sizeX, textureV + sizeZ + sizeY, texWidth, texHeight);
        if (mirror) {
            Polygon[] var20 = this.faces;
            int var21 = var20.length;

            for (int var22 = 0; var22 < var21; ++var22) {
                Polygon face = var20[var22];
                face.flipFace();
            }
        }

        tessellator.setColorRGBA_F(1, 1, 1f, 1);
        for (Polygon face : faces) {
            face.draw(tessellator, 1);
        }
    }

    public void matrix(FloatBuffer proj, FloatBuffer modl) {
        shader.matrix(proj, modl);
    }

    public void close() {
        shader.delete();
        VAOAllocator.INSTANCE.delete(vao);
        ARBVertexBufferObject.glDeleteBuffersARB(vbo[0]);
    }
}
