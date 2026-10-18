package top.dreamfall.elytratrails.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

/**
 * 长寿命尾迹粒子——lifetime 由 TrailParticleEffect.duration 决定。
 * alpha 用 ease-out 平方曲线淡出：1 - (age/lifetime)²。
 */
public class TrailParticle extends TextureSheetParticle {

    protected TrailParticle(ClientLevel level, double x, double y, double z,
                            TrailParticleEffect effect) {
        super(level, x, y, z, 0, 0, 0);
        this.lifetime = effect.duration();
        this.quadSize = effect.scale();
        this.rCol = effect.color().x();
        this.gCol = effect.color().y();
        this.bCol = effect.color().z();
        this.alpha = 1.0f;
        this.gravity = 0;
        // 微小随机初速——让粒子有"呼吸感"
        this.xd = (this.random.nextFloat() - 0.5f) * 0.04f;
        this.yd = (this.random.nextFloat() - 0.5f) * 0.04f;
        this.zd = (this.random.nextFloat() - 0.5f) * 0.04f;
    }

    @Override
    public void tick() {
        super.tick();
        // ease-out 平方淡出：前半段亮得久，后半段快速消失，很自然
        float progress = (float) this.age / this.lifetime;
        this.alpha = 1.0f - progress * progress;
        // 微微缩小
        this.quadSize *= 0.997f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880; // 全天光 + 全方块光 = 不受环境光照影响
    }

    // ---- Factory ----

    public static class Factory implements ParticleProvider<TrailParticleEffect> {
        private final SpriteSet sprite;

        public Factory(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(TrailParticleEffect effect, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            TrailParticle p = new TrailParticle(level, x, y, z, effect);
            p.pickSprite(this.sprite);
            return p;
        }
    }
}
