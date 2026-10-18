package top.dreamfall.elytratrails.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.dreamfall.elytratrails.ElytraTrails;
import top.dreamfall.elytratrails.particle.TrailParticleEffect;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Unique private double p0x, p0y, p0z;
    @Unique private double p1x, p1y, p1z;
    @Unique private int trailPosCount;
    @Unique private int spawnTickCounter;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        Player self = (Player) (Object) this;

        if (!self.level().isClientSide()) return;
        if (!self.isFallFlying()) { trailPosCount = 0; spawnTickCounter = 0; return; }

        ItemStack chest = self.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.is(Items.ELYTRA)) { trailPosCount = 0; spawnTickCounter = 0; return; }

        ItemEnchantments enchants = chest.getEnchantments();
        Holder<Enchantment> trailEnchant = null;
        boolean big = false;
        int multiLevel = 0;

        for (Holder<Enchantment> entry : enchants.keySet()) {
            ResourceLocation id = entry.unwrapKey().map(k -> k.location()).orElse(null);
            if (id == null) continue;
            if (!id.getNamespace().equals(ElytraTrails.MOD_ID)) continue;

            String path = id.getPath();
            if (path.equals("big_trail")) {
                big = true;
            } else if (path.equals("multi_trail")) {
                multiLevel = enchants.getLevel(entry);
            } else if (path.endsWith("_trail")) {
                trailEnchant = entry;
            }
        }

        if (trailEnchant == null) { trailPosCount = 0; spawnTickCounter = 0; return; }
        var key = trailEnchant.unwrapKey().orElse(null);
        if (key == null) { trailPosCount = 0; spawnTickCounter = 0; return; }
        int colorIndex = ElytraTrails.colorIndexFromEnchantId(key.location().getPath());
        if (colorIndex < 0) { trailPosCount = 0; spawnTickCounter = 0; return; }

        ElytraTrails.checkConfigReload();

        if (ElytraTrails.spawnIntervalTicks > 1) {
            spawnTickCounter++;
            if (spawnTickCounter % ElytraTrails.spawnIntervalTicks != 0) {
                return;
            }
        }

        int color = ElytraTrails.brightColor(colorIndex);

        int duration = ElytraTrails.trailDurationTicks;
        double spread = ElytraTrails.spreadRadius;
        double spacing = 0.5;

        int totalTrails = 1 + multiLevel * 2;
        Vec3 vel = self.getDeltaMovement();
        Vec3 perp = vel.normalize().cross(new Vec3(0, 1, 0));
        if (perp.length() < 0.01) perp = new Vec3(1, 0, 0);
        perp = perp.normalize();

        Vec3 look = self.getLookAngle().scale(-0.8);
        double cx = self.getX() + look.x;
        double cy = self.getY();
        double cz = self.getZ() + look.z;
        Vec3 currentPos = new Vec3(cx, cy, cz);

        Vec3 p2 = currentPos;
        Vec3 p1 = trailPosCount >= 1 ? new Vec3(p1x, p1y, p1z) : p2;
        Vec3 p0 = trailPosCount >= 2 ? new Vec3(p0x, p0y, p0z) : p1;

        int baseCount = big ? 60 : 15;
        int count = Math.max(2, baseCount / totalTrails);
        int particleColor = big ? brighten(color) : color;

        float r = ((particleColor >> 16) & 0xFF) / 255f;
        float g = ((particleColor >> 8) & 0xFF) / 255f;
        float b = (particleColor & 0xFF) / 255f;
        Vector3f col = new Vector3f(r, g, b);
        float scale = 0.2f;
        RandomSource rng = self.getRandom();

        int halfExtra = totalTrails / 2;
        for (int trailIdx = -halfExtra; trailIdx <= halfExtra; trailIdx++) {
            Vec3 trailOffset = perp.scale(trailIdx * spacing);

            for (int i = 0; i < count; i++) {
                float t = (float) i / count;
                float u = 1 - t;
                double bx = u*u*p0.x + 2*u*t*p1.x + t*t*p2.x;
                double by = u*u*p0.y + 2*u*t*p1.y + t*t*p2.y;
                double bz = u*u*p0.z + 2*u*t*p1.z + t*t*p2.z;
                Vec3 spawnPos = new Vec3(bx, by, bz).add(trailOffset);

                if (big) {
                    float angle = rng.nextFloat() * (float) Math.PI * 2;
                    float pitch = (rng.nextFloat() - 0.5f) * (float) Math.PI;
                    Vec3 offset = new Vec3(
                            Math.cos(angle) * Math.cos(pitch) * spread,
                            Math.sin(pitch) * spread,
                            Math.sin(angle) * Math.cos(pitch) * spread
                    );
                    spawnPos = spawnPos.add(offset);
                }

                self.level().addParticle(
                        new TrailParticleEffect(col, scale, duration),
                        spawnPos.x, spawnPos.y, spawnPos.z, 0, 0, 0
                );
            }
        }

        p0x = p1x; p0y = p1y; p0z = p1z;
        p1x = cx;  p1y = cy;  p1z = cz;
        trailPosCount = Math.min(trailPosCount + 1, 2);
    }

    @Unique
    private static int brighten(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        r = Math.min(255, r + (255 - r) * 2 / 5);
        g = Math.min(255, g + (255 - g) * 2 / 5);
        b = Math.min(255, b + (255 - b) * 2 / 5);
        return (r << 16) | (g << 8) | b;
    }
}
