package top.dreamfall.elytratrails.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

/**
 * 长寿命尾迹粒子——maxAge 由 TrailParticleEffect.duration 决定。
 * alpha 用 ease-out 平方曲线淡出：1 - (age/maxAge)²。
 */
public class TrailParticle extends SpriteBillboardParticle {

    protected TrailParticle(ClientWorld world, double x, double y, double z,
                            TrailParticleEffect effect) {
        super(world, x, y, z, 0, 0, 0);
        this.maxAge = effect.duration();
        this.scale = effect.scale();
        this.red = effect.color().x();
        this.green = effect.color().y();
        this.blue = effect.color().z();
        this.alpha = 1.0f;
        this.gravityStrength = 0;
        // 微小随机初速——让粒子有"呼吸感"
        this.velocityX = (random.nextFloat() - 0.5f) * 0.04f;
        this.velocityY = (random.nextFloat() - 0.5f) * 0.04f;
        this.velocityZ = (random.nextFloat() - 0.5f) * 0.04f;
    }

    @Override
    public void tick() {
        super.tick();
        // ease-out 平方淡出：前半段亮得久，后半段快速消失，很自然
        float progress = (float) this.age / this.maxAge;
        this.alpha = 1.0f - progress * progress;
        // 微微缩小
        this.scale *= 0.997f;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getBrightness(float tickDelta) {
        return 15728880; // 全天光 + 全方块光 = 不受环境光照影响
    }

    // ---- Factory ----

    public static class Factory implements ParticleFactory<TrailParticleEffect> {
        private final SpriteProvider sprite;

        public Factory(SpriteProvider sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(TrailParticleEffect effect, ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            TrailParticle p = new TrailParticle(world, x, y, z, effect);
            p.setSprite(sprite);
            return p;
        }
    }
}
