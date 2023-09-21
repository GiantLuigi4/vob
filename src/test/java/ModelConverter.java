import net.minecraft.client.render.Vertex;
import net.minecraft.client.render.model.Cube;
import net.minecraft.client.render.model.ModelCow;

import java.lang.reflect.Field;

public class ModelConverter {
    public static void main(String[] args) {
        ModelCow model = new ModelCow();
        Class<?> clazz = model.getClass();

        StringBuilder output = new StringBuilder();

        int id = 0;
        while (clazz != Object.class) {
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.getType().equals(Cube.class)) {
                    try {
                        declaredField.setAccessible(true);
                        Cube cb = (Cube) declaredField.get(model);
                        double x = cb.rotationPointX;
                        double y = cb.rotationPointY;
                        double z = cb.rotationPointZ;

                        output.append(declaredField.getName() + ":\n");
                        output.append("    pivot: \"" + x + ", " + y + ", " + z + "\"\n");
                        output.append("    id: \"" + id + "\"\n");
                        output.append("    cube0:\n");

                        Field f = Cube.class.getDeclaredField("corners");
                        f.setAccessible(true);

                        double minX = Double.POSITIVE_INFINITY;
                        double minY = Double.POSITIVE_INFINITY;
                        double minZ = Double.POSITIVE_INFINITY;
                        double maxX = Double.NEGATIVE_INFINITY;
                        double maxY = Double.NEGATIVE_INFINITY;
                        double maxZ = Double.NEGATIVE_INFINITY;

                        Vertex[] corners = (Vertex[]) f.get(cb);
                        for (Vertex corner : corners) {
                            minX = Math.min(minX, corner.vector3D.xCoord);
                            minY = Math.min(minY, corner.vector3D.yCoord);
                            minZ = Math.min(minZ, corner.vector3D.zCoord);

                            maxX = Math.max(maxX, corner.vector3D.xCoord);
                            maxY = Math.max(maxY, corner.vector3D.yCoord);
                            maxZ = Math.max(maxZ, corner.vector3D.zCoord);
                        }

                        f = Cube.class.getDeclaredField("textureU");
                        f.setAccessible(true);
                        int u = (int) f.get(cb);
                        f = Cube.class.getDeclaredField("textureV");
                        f.setAccessible(true);
                        int v = (int) f.get(cb);

                        output.append("        uv: \"" + u + ", " + v + "\"\n");
                        output.append("        coord: \"" + minX + ", " + minY + ", " + minZ + "\"\n");
                        output.append("        size: \"" + (maxX - minX) + ", " + (maxY - minY) + ", " + (maxZ - minZ) + "\"\n");

                        id++;
                    } catch (Throwable err) {
                        err.printStackTrace();
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        System.out.println(output);
    }
}
