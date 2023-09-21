package tfc.vob.instancing.model;

import tfc.vob.instancing.yaml.Hsml;

import java.util.ArrayList;

public class ModelElement {
    int id;
    ArrayList<ModelCube> cubes = new ArrayList<>();
    double px, py, pz;

    public ModelElement(int id, String pivot) {
        this.id = id;

        px = Double.parseDouble(pivot.split(",")[0].trim());
        py = Double.parseDouble(pivot.split(",")[1].trim());
        pz = Double.parseDouble(pivot.split(",")[2].trim());
    }

    public void addCube(Hsml cube) {
        String uv = cube.getText("uv");
        String coord = cube.getText("coord");
        String size = cube.getText("size");

        cubes.add(
                new ModelCube(
                        Float.parseFloat(uv.split(",")[0].trim()),
                        Float.parseFloat(uv.split(",")[1].trim()),

                        Double.parseDouble(coord.split(",")[0].trim()),
                        Double.parseDouble(coord.split(",")[1].trim()),
                        Double.parseDouble(coord.split(",")[2].trim()),

                        Double.parseDouble(size.split(",")[0].trim()),
                        Double.parseDouble(size.split(",")[1].trim()),
                        Double.parseDouble(size.split(",")[2].trim())
                )
        );
    }

    static class ModelCube {
        float u, v;
        double cx, cy, cz;
        double sx, sy, sz;

        public ModelCube(float u, float v, double cx, double cy, double cz, double sx, double sy, double sz) {
            this.u = u;
            this.v = v;
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;
        }
    }
}
