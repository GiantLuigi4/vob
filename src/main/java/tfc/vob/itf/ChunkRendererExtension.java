package tfc.vob.itf;

public interface ChunkRendererExtension {
    void draw(int pass);
    int[] getVao(int pass);
    void close();
}
