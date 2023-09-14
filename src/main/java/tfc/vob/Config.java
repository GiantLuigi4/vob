package tfc.vob;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ConfigHandler;

import java.util.Properties;


public class Config implements ModInitializer {
    public static boolean useVAOs = true;
    public static boolean useBatching = false;
	public static int maxBatches = 1;
	
	@Override
    public void onInitialize() {
        Properties cfg = new Properties();
        cfg.put("vob_pipeline", "" + useVAOs);
        cfg.put("use_batching", "" + useBatching);
        cfg.put("max_batches", "" + maxBatches);
        ConfigHandler handler = new ConfigHandler("vob", cfg);
        useVAOs = handler.getBoolean("vob_pipeline");
        useBatching = handler.getBoolean("use_batching");
        maxBatches = handler.getInt("max_batches");
    }
}
