package tfc.vob.util;

import org.lwjgl.opengl.APPLEVertexArrayObject;
import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.GL11;

public abstract class VAOAllocator {
    public static final VAOAllocator INSTANCE;

    static {
        if (GL11.glGetString(GL11.GL_VENDOR).equals("Apple")) {
            INSTANCE = new Apple();
        } else {
            INSTANCE = new ARB();
        }
    }

    public abstract int generate();

    public abstract void bind(int vao);

    public abstract void delete(int vao);

    static class Apple extends VAOAllocator {
        @Override
        public int generate() {
            return APPLEVertexArrayObject.glGenVertexArraysAPPLE();
        }

        @Override
        public void bind(int vao) {
            APPLEVertexArrayObject.glBindVertexArrayAPPLE(vao);
        }

        @Override
        public void delete(int vao) {
            APPLEVertexArrayObject.glDeleteVertexArraysAPPLE(vao);
        }
    }

    static class ARB extends VAOAllocator {
        @Override
        public int generate() {
            return ARBVertexArrayObject.glGenVertexArrays();
        }

        @Override
        public void bind(int vao) {
            ARBVertexArrayObject.glBindVertexArray(vao);
        }

        @Override
        public void delete(int vao) {
            ARBVertexArrayObject.glDeleteVertexArrays(vao);
        }
    }
}
