package top.dreamfall.elytratrails.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TrailParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.dreamfall.elytratrails.ElytraTrails;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Unique private double p0x, p0y, p0z;
    @Unique private double p1x, p1y, p1z;
    @Unique private int trailPosCount;
    @Unique private int spawnTickCounter;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        World world = ((EntityAccessor) self).elytratrails$getWorld();
        if (world == null || !world.isClient()) return;
        if (!self.isGliding()) { trailPosCount = 0; spawnTickCounter = 0; return; }

        ItemStack chest = self.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isOf(Items.ELYTRA)) { trailPosCount = 0; spawnTickCounter = 0; return; }

        var enchants = chest.getEnchantments();
        var entries = enchants.getEnchantments();

        RegistryEntry<Enchantment> trailEnchant = null;
        boolean big = false;
        int multiLevel = 0;

        for (RegistryEntry<Enchantment> entry : entries) {
            var key = entry.getKey().orElse(null);
            if (key == null) continue;
            Identifier id = key.getValue();
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
        var key = trailEnchant.getKey().orElse(null);
        if (key == null) { trailPosCount = 0; spawnTickCounter = 0; return; }
        int colorIndex = ElytraTrails.colorIndexFromEnchantId(key.getValue().getPath());
        if (colorIndex < 0) { trailPosCount = 0; spawnTickCounter = 0; return; }

        ElytraTrails.checkConfigReload();

        if (ElytraTrails.spawnIntervalTicks > 1) {
            spawnTickCounter++;
            if (spawnTickCounter % ElytraTrails.spawnIntervalTicks != 0) {
                return;
            }
        }

        DyeColor dye = DyeColor.values()[colorIndex];
        int color = dye.getMapColor().color;

        int duration = ElytraTrails.trailDurationTicks;
        double spread = ElytraTrails.spreadRadius;
        double spacing = 0.5;

        int totalTrails = 1 + multiLevel * 2;
        Vec3d perp = self.getVelocity().normalize().crossProduct(new Vec3d(0, 1, 0));
        if (perp.length() < 0.01) perp = new Vec3d(1, 0, 0);
        perp = perp.normalize();

        var look = self.getRotationVector().multiply(-0.8);
        double cx = self.getX() + look.x;
        double cy = self.getY();
        double cz = self.getZ() + look.z;
        Vec3d currentPos = new Vec3d(cx, cy, cz);

        Vec3d p2 = currentPos;
        Vec3d p1 = trailPosCount >= 1 ? new Vec3d(p1x, p1y, p1z) : p2;
        Vec3d p0 = trailPosCount >= 2 ? new Vec3d(p0x, p0y, p0z) : p1;

        int baseCount = big ? 90 : 25;
        int count = Math.max(3, baseCount / totalTrails);
        int particleColor = big ? brighten(color) : color;
        Random rng = self.getRandom();

        int halfExtra = totalTrails / 2;
        for (int trailIdx = -halfExtra; trailIdx <= halfExtra; trailIdx++) {
            Vec3d trailOffset = perp.multiply(trailIdx * spacing);

            for (int i = 0; i < count; i++) {
                float t = (float) i / count;
                float u = 1 - t;
                double bx = u*u*p0.x + 2*u*t*p1.x + t*t*p2.x;
                double by = u*u*p0.y + 2*u*t*p1.y + t*t*p2.y;
                double bz = u*u*p0.z + 2*u*t*p1.z + t*t*p2.z;
                Vec3d spawnPos = new Vec3d(bx, by, bz).add(trailOffset);

                Vec3d target;
                if (big) {
                    float angle = rng.nextFloat() * (float) Math.PI * 2;
                    float pitch = (rng.nextFloat() - 0.5f) * (float) Math.PI;
                    Vec3d offset = new Vec3d(
                            Math.cos(angle) * Math.cos(pitch) * spread,
                            Math.sin(pitch) * spread,
                            Math.sin(angle) * Math.cos(pitch) * spread
                    );
                    target = spawnPos.add(offset);
                } else {
                    target = spawnPos;
                }

                world.addParticleClient(
                        new TrailParticleEffect(target, particleColor, duration),
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
