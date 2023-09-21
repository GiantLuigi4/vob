package tfc.vob.math;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class MatrixHelper {
    private static final Matrix4f tmp0 = new Matrix4f(), tmp1 = new Matrix4f(), dst = new Matrix4f();

    public static Matrix4f mul(FloatBuffer buf0, FloatBuffer buf1) {
        tmp0.load(buf0);
        tmp1.load(buf1);
        Matrix4f.mul(tmp0, tmp1, dst);
        buf0.position(0);
        buf1.position(0);
        return dst;
    }

    private static void load(
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

    public static void rotationMatrix(
            FloatBuffer buf,
            Vector3f axis, float rotation
    ) {
        float s = (float) Math.sin(rotation);
        float c = (float) Math.cos(rotation);
        float oc = 1.0f - c;

        load(
                buf,
                oc * axis.x * axis.x + c, oc * axis.x * axis.y - axis.z * s, oc * axis.z * axis.x + axis.y * s,
                oc * axis.x * axis.y + axis.z * s, oc * axis.y * axis.y + c, oc * axis.y * axis.z - axis.x * s,
                oc * axis.z * axis.x - axis.y * s, oc * axis.y * axis.z + axis.x * s, oc * axis.z * axis.z + c
        );
    }
}
