package tfc.vob.instancing;

import net.minecraft.client.GLAllocation;
import net.minecraft.client.render.shader.Shader;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.Vec3d;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;
import tfc.vob.math.MatrixHelper;
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

    public void parse(String name, String fsh, String vsh) {
        if (sdr != null) sdr.delete();

        sdr = new Shader();
        sdr.compile(
                string -> {
                    if (string.endsWith(".fsh")) return new String(InstanceDispatcher.read("assets/instancing/" + fsh));
                    if (string.endsWith(".vsh")) return new String(InstanceDispatcher.read("assets/instancing/" + vsh));
                    return null;
                }, name
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
    private static final FloatBuffer tmp1 = GLAllocation.createDirectFloatBuffer(16);

    public void matrix(
            int uform,
            int index,
            Vec3d translation,
            double height,
            float yRot, float death
    ) {
        if (uform == -1) return;
        uform += index;

        tmp.position(0);
        tmp1.position(0);

        tmp.put(12, 0);
        tmp.put(13, 0);
        tmp.put(14, 0);
        tmp.put(15, 1);
        tmp1.put(15, 1);

        Vector3f axis = new Vector3f(0, 1, 0);
        MatrixHelper.rotationMatrix(tmp, axis, yRot);

        if (death != 0) {
            float f3 = (death - 1.0F) / 20.0F * 1.6F;
            f3 = MathHelper.sqrt_float(f3);
            if (f3 > 1.0F) {
                f3 = 1.0F;
            }
            death = f3 * 90;

            axis.set(0, 0, 1);
            MatrixHelper.rotationMatrix(tmp1, axis, (float) Math.toRadians(death));
            tmp1.position(0);

            tmp.position(0);
            MatrixHelper.mul(tmp1, tmp).store(tmp);
            tmp.position(0);
        }

        tmp.put(3, (float) (translation.xCoord * 16));
        tmp.put(7, (float) (-translation.yCoord * 16));
        tmp.put(11, (float) (-translation.zCoord * 16));

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
