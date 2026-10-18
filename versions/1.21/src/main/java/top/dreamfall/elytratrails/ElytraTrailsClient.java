package top.dreamfall.elytratrails;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import top.dreamfall.elytratrails.particle.TrailParticle;

@EventBusSubscriber(modid = ElytraTrails.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ElytraTrailsClient {
    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ElytraTrails.TRAIL_PARTICLE.get(), TrailParticle.Factory::new);
    }
}
