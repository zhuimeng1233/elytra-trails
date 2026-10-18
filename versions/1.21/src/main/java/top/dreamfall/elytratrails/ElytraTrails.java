package top.dreamfall.elytratrails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.dreamfall.elytratrails.particle.TrailParticleEffect;
import top.dreamfall.elytratrails.recipe.TrailRecipe;

import java.io.*;

@Mod(ElytraTrails.MOD_ID)
public class ElytraTrails {
    public static final String MOD_ID = "elytratrails";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<TrailParticleEffect>> TRAIL_PARTICLE =
            PARTICLE_TYPES.register("trail", () -> new ParticleType<TrailParticleEffect>(false) {
                @Override
                public MapCodec<TrailParticleEffect> codec() {
                    return TrailParticleEffect.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, TrailParticleEffect> streamCodec() {
                    return TrailParticleEffect.STREAM_CODEC;
                }
            });

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TrailRecipe>> TRAIL_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("trail", TrailRecipe.Serializer::new);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static float spreadRadius = 1.0f;
    public static int spawnIntervalTicks = 1;
    public static int trailDurationTicks = 100;

    private static long lastConfigCheckTime = 0;
    private static long lastConfigFileModified = 0;

    public static final String[] COLOR_NAMES = {
        "white","orange","magenta","light_blue","yellow","lime",
        "pink","gray","light_gray","cyan","purple","blue",
        "brown","green","red","black"
    };

    /** 染料本色（非 MapColor），更鲜亮 */
    private static final int[] BRIGHT_DYE_COLORS = {
        0xF9FFFE, // white
        0xF9801D, // orange
        0xC74EBD, // magenta
        0x3AB3DA, // light_blue
        0xFED83D, // yellow
        0x80C71F, // lime
        0xF38BAA, // pink
        0x474F52, // gray
        0x9D9D97, // light_gray
        0x169C9C, // cyan
        0x8932B8, // purple
        0x3C44AA, // blue
        0x835432, // brown
        0x5E7C16, // green
        0xB02E26, // red
        0x1D1D21  // black
    };

    public static int brightColor(int index) {
        if (index < 0 || index >= BRIGHT_DYE_COLORS.length) return 0xFFFFFF;
        return BRIGHT_DYE_COLORS[index];
    }

    public ElytraTrails(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        loadConfig();
        LOGGER.info("鞘翅尾迹 NeoForge mod 已加载~");
    }

    private static File getConfigFile() {
        return FMLPaths.CONFIGDIR.get().resolve("elytra-trails.json").toFile();
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

    public static int colorIndexFromEnchantId(String path) {
        for (int i = 0; i < COLOR_NAMES.length; i++) {
            if (path.equals(COLOR_NAMES[i] + "_trail")) return i;
        }
        return -1;
    }
}
