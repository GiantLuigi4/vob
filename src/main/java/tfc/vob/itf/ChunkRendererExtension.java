package turniplabs.examplemod.itf;

public interface ChunkRendererExtension {
    void draw(int pass);
    int[] getVao(int pass);
}
