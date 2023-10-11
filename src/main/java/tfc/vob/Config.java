package tfc.vob;

import net.fabricmc.api.ModInitializer;
import turniplabs.halplibe.util.ConfigHandler;

import java.util.Properties;


public class Config implements ModInitializer {
    public static boolean vobPipeline = true;
    public static boolean useBatching = false;
	public static int maxBatches = 64;
    public static boolean skipList = true;

    @Override
    public void onInitialize() {
        Properties cfg = new Properties();
        cfg.put("vob_pipeline", "" + vobPipeline);
        cfg.put("use_batching", "" + useBatching);
        cfg.put("max_batches", "" + maxBatches);
        cfg.put("skip_lists", "" + skipList);
        ConfigHandler handler = new ConfigHandler("vob", cfg);
        vobPipeline = handler.getBoolean("vob_pipeline");
        useBatching = handler.getBoolean("use_batching");
        maxBatches = handler.getInt("max_batches");
        skipList = handler.getBoolean("skip_lists");
        if (!vobPipeline) skipList = false;
    }
}
