package top.dreamfall.elytratrails.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.joml.Vector3f;
import top.dreamfall.elytratrails.ElytraTrails;

/**
 * 鞘翅尾迹粒子数据：颜色(RGB) + 缩放 + 寿命（tick）。
 */
public record TrailParticleEffect(Vector3f color, float scale, int duration) implements ParticleEffect {

    // ---- Codec：Vector3f 序列化为 r/g/b 三个 float ----
    public static final MapCodec<TrailParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("r").forGetter(e -> e.color.x()),
            Codec.FLOAT.fieldOf("g").forGetter(e -> e.color.y()),
            Codec.FLOAT.fieldOf("b").forGetter(e -> e.color.z()),
            Codec.FLOAT.fieldOf("scale").forGetter(TrailParticleEffect::scale),
            Codec.INT.fieldOf("duration").forGetter(TrailParticleEffect::duration)
        ).apply(instance, (r, g, b, scale, duration) ->
            new TrailParticleEffect(new Vector3f(r, g, b), scale, duration)));

    // ---- PacketCodec ----
    public static final PacketCodec<RegistryByteBuf, TrailParticleEffect> PACKET_CODEC =
        PacketCodec.tuple(
            PacketCodecs.FLOAT, e -> e.color.x(),
            PacketCodecs.FLOAT, e -> e.color.y(),
            PacketCodecs.FLOAT, e -> e.color.z(),
            PacketCodecs.FLOAT, TrailParticleEffect::scale,
            PacketCodecs.VAR_INT, TrailParticleEffect::duration,
            (r, g, b, scale, duration) ->
                new TrailParticleEffect(new Vector3f(r, g, b), scale, duration));

    @Override
    public ParticleType<TrailParticleEffect> getType() {
        return ElytraTrails.TRAIL_PARTICLE;
    }
}
