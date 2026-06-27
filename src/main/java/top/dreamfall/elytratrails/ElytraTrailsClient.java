package top.dreamfall.elytratrails;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import top.dreamfall.elytratrails.particle.TrailParticle;

public class ElytraTrailsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(
            ElytraTrails.TRAIL_PARTICLE,
            TrailParticle.Factory::new);
    }
}
