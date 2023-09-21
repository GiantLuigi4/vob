package tfc.vob.instancing;

import net.minecraft.client.GLAllocation;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.core.util.phys.Vec3d;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;
import tfc.vob.mixin.instancing.ShaderAccessor;

import java.nio.FloatBuffer;

public class InstancingShader {
    Shader sdr;

    public InstancingShader() {
    }

//    protected static String read(String res) {
//        try {
//            InputStream strm = InstanceModel.class.getClassLoader()
//                    .getResourceAsStream("assets/instancing/" + res);
//            byte[] data = new byte[strm.available()];
//            strm.read(data);
//            strm.close();
//            return new String(data);
//        } catch (Throwable err) {
//            err.printStackTrace();
//            throw new RuntimeException(err);
//        }
//    }

    public void parse(String fsh, String vsh) {
        if (sdr != null) sdr.delete();

        sdr = new Shader();
        sdr.compile(
                string -> {
                    if (string.endsWith(".fsh")) return new String(InstanceDispatcher.read("assets/instancing/" + fsh));
                    if (string.endsWith(".vsh")) return new String(InstanceDispatcher.read("assets/instancing/" + vsh));
                    return null;
                }, "shader"
        );
    }

    public void bind() {
        sdr.bind();
        GL20.glBindAttribLocation(
                ((ShaderAccessor) sdr).getProgram(),
                0, "Vertex"
        );
        GL20.glBindAttribLocation(
                ((ShaderAccessor) sdr).getProgram(),
                1, "Normal"
        );
        GL20.glBindAttribLocation(
                ((ShaderAccessor) sdr).getProgram(),
                2, "TexCoord"
        );

        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
    }

    public void unbind() {
        sdr.unbind();

        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(0);
    }

    private static final FloatBuffer tmp = GLAllocation.createDirectFloatBuffer(16);

    private static void merge(
            FloatBuffer buf,
            float m00, float m01, float m02,
            float m10, float m11, float m12,
            float m20, float m21, float m22
    ) {
        buf.put(0, m00);
        buf.put(1, m01);
        buf.put(2, m02);

        buf.put(4, m10);
        buf.put(5, m11);
        buf.put(6, m12);

        buf.put(8, m20);
        buf.put(9, m21);
        buf.put(10, m22);
    }

    public void matrix(
            int uform,
            int index,
            Vec3d translation,
            float yRot, float xRot
    ) {
        if (uform == -1) return;
        uform += index;

        tmp.position(0);
        tmp.put(3, (float) (translation.xCoord * 16));
        tmp.put(7, (float) (-translation.yCoord * 16));
        tmp.put(11, (float) (-translation.zCoord * 16));

        tmp.put(12, 0);
        tmp.put(13, 0);
        tmp.put(14, 0);
        tmp.put(15, 1);

        Vector3f axis = new Vector3f(0, 1, 0);
        float s = (float) Math.sin(yRot);
        float c = (float) Math.cos(yRot);
        float oc = 1.0f - c;

        merge(
                tmp,
                oc * axis.x * axis.x + c, oc * axis.x * axis.y - axis.z * s, oc * axis.z * axis.x + axis.y * s,
                oc * axis.x * axis.y + axis.z * s, oc * axis.y * axis.y + c, oc * axis.y * axis.z - axis.x * s,
                oc * axis.z * axis.x - axis.y * s, oc * axis.y * axis.z + axis.x * s, oc * axis.z * axis.z + c);

        GL20.glUniformMatrix4(
                uform, false,
                tmp
        );
    }

    public void matrix(FloatBuffer proj, FloatBuffer modl) {
        int uform = sdr.getUniform("projMat");
        GL20.glUniformMatrix4(uform, true, proj);

        uform = sdr.getUniform("modelViewMat");
        GL20.glUniformMatrix4(uform, true, modl);

        proj.position(0);
        modl.position(0);

        uform = sdr.getUniform("colortex0");
        GL20.glUniform1i(uform, 0);
    }

    public void arrayUniform(String name, float[] array, int dPerEl) {
        int uform = sdr.getUniform(name);
        for (int i = 0; i < array.length; i += dPerEl) {
            switch (dPerEl) {
                case 1:
                    GL20.glUniform1f(uform, array[i]);
                    break;
                case 2:
                    GL20.glUniform2f(uform + (i / 2), array[i], array[i + 1]);
                    break;
                case 3:
                    GL20.glUniform3f(uform + (i / 3), array[i], array[i + 1], array[i + 2]);
                    break;
            }
        }
    }

    public int getUniform(String name) {
        return sdr.getUniform(name);
    }

    public void delete() {
        sdr.delete();
    }
}
