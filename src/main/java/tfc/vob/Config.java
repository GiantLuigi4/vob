package tfc.vob;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.ConfigHandler;

import java.util.Properties;


public class Config implements ModInitializer {
    public static boolean useVAOs = true;
    public static boolean useBatching = false;

    @Override
    public void onInitialize() {
        Properties cfg = new Properties();
        cfg.put("use_vaos", "" + true);
        cfg.put("use_batching", "" + false);
        ConfigHandler handler = new ConfigHandler("vob", cfg);
        useVAOs = handler.getBoolean("use_vaos");
        useBatching = handler.getBoolean("use_batching");
    }
}
