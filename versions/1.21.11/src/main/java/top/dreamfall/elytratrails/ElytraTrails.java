package top.dreamfall.elytratrails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ElytraTrails implements ModInitializer {
    public static final String MOD_ID = "elytratrails";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // --- config defaults ---
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static float spreadRadius = 1.0f;
    public static int spawnIntervalTicks = 1;
    public static int trailDurationTicks = 100;

    private static long lastConfigCheckTime = 0;
    private static long lastConfigFileModified = 0;

    @Override
    public void onInitialize() {
        loadConfig();
        LOGGER.info("鞘翅尾迹 mod 已加载~");
    }

    private static File getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("elytra-trails.json").toFile();
    }

    public static void checkConfigReload() {
        long now = System.currentTimeMillis();
        if (now - lastConfigCheckTime < 1000) return;
        lastConfigCheckTime = now;
        File file = getConfigFile();
        if (file.exists() && file.lastModified() != lastConfigFileModified) {
            loadConfig();
        }
    }

    public static void loadConfig() {
        File file = getConfigFile();
        if (file.exists()) {
            lastConfigFileModified = file.lastModified();
            try (Reader r = new FileReader(file)) {
                ConfigData d = GSON.fromJson(r, ConfigData.class);
                if (d != null) {
                    spreadRadius = d.spreadRadius;
                    spawnIntervalTicks = Math.max(1, d.spawnInterval);
                    trailDurationTicks = Math.max(1, d.trailDuration);
                }
            } catch (Exception e) {
                LOGGER.warn("配置文件读取失败，使用默认值", e);
            }
        } else {
            saveConfig(file);
        }
    }

    private static void saveConfig(File file) {
        try (Writer w = new FileWriter(file)) {
            ConfigData d = new ConfigData();
            d.spreadRadius = spreadRadius;
            d.spawnInterval = spawnIntervalTicks;
            d.trailDuration = trailDurationTicks;
            GSON.toJson(d, w);
            lastConfigFileModified = file.lastModified();
        } catch (Exception ignored) {}
    }

    private static class ConfigData {
        float spreadRadius = 1.0f;
        int spawnInterval = 1;
        int trailDuration = 100;
    }

    // --- 颜色解析 ---
    public static final String[] COLOR_NAMES = {
        "white","orange","magenta","light_blue","yellow","lime",
        "pink","gray","light_gray","cyan","purple","blue",
        "brown","green","red","black"
    };

    public static int colorIndexFromEnchantId(String path) {
        for (int i = 0; i < COLOR_NAMES.length; i++) {
            if (path.equals(COLOR_NAMES[i] + "_trail")) return i;
        }
        return -1;
    }
}
