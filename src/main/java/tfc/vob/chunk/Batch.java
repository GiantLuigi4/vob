package tfc.vob.chunk;

public class Batch {
    int[][] vaos;

    int idx;
    int lim;

    public Batch(int lim) {
        this.vaos = new int[lim][];
    }

    public void put(int[] vao) {
        this.vaos[idx] = vao;
        idx++;
    }

    public void position(int i) {
        idx = i;
    }
}
