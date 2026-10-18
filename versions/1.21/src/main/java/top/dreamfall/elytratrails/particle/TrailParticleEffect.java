package top.dreamfall.elytratrails.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;
import top.dreamfall.elytratrails.ElytraTrails;

/**
 * 鞘翅尾迹粒子数据：颜色(RGB) + 缩放 + 寿命（tick）。
 */
public record TrailParticleEffect(Vector3f color, float scale, int duration) implements ParticleOptions {

    public static final MapCodec<TrailParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("r").forGetter(e -> e.color.x()),
            Codec.FLOAT.fieldOf("g").forGetter(e -> e.color.y()),
            Codec.FLOAT.fieldOf("b").forGetter(e -> e.color.z()),
            Codec.FLOAT.fieldOf("scale").forGetter(TrailParticleEffect::scale),
            Codec.INT.fieldOf("duration").forGetter(TrailParticleEffect::duration)
        ).apply(instance, (r, g, b, scale, duration) ->
            new TrailParticleEffect(new Vector3f(r, g, b), scale, duration)));

    public static final StreamCodec<ByteBuf, TrailParticleEffect> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VECTOR3F, TrailParticleEffect::color,
        ByteBufCodecs.FLOAT, TrailParticleEffect::scale,
        ByteBufCodecs.VAR_INT, TrailParticleEffect::duration,
        TrailParticleEffect::new
    );

    @Override
    public ParticleType<TrailParticleEffect> getType() {
        return ElytraTrails.TRAIL_PARTICLE.get();
    }
}
