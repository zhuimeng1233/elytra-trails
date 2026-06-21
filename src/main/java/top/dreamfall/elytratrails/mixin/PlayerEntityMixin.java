package top.dreamfall.elytratrails.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TrailParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.dreamfall.elytratrails.ElytraTrails;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Unique private double p0x, p0y, p0z; // 2 ticks 前
    @Unique private double p1x, p1y, p1z; // 1 tick 前
    @Unique private int trailPosCount;     // 已记录点数

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (!self.getEntityWorld().isClient()) return;
        if (!self.isGliding()) {
            trailPosCount = 0;
            return;
        }

        ItemStack chest = self.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isOf(Items.ELYTRA)) {
            trailPosCount = 0;
            return;
        }

        var enchants = chest.getEnchantments();
        var entries = enchants.getEnchantments();

        RegistryEntry<Enchantment> trailEnchant = null;
        boolean big = false;

        for (RegistryEntry<Enchantment> entry : entries) {
            var key = entry.getKey().orElse(null);
            if (key == null) continue;
            Identifier id = key.getValue();
            if (!id.getNamespace().equals(ElytraTrails.MOD_ID)) continue;

            String path = id.getPath();
            if (path.equals("big_trail")) {
                big = true;
            } else if (path.endsWith("_trail")) {
                trailEnchant = entry;
            }
        }

        if (trailEnchant == null) { trailPosCount = 0; return; }
        var key = trailEnchant.getKey().orElse(null);
        if (key == null) { trailPosCount = 0; return; }
        int colorIndex = ElytraTrails.colorIndexFromEnchantId(key.getValue().getPath());
        if (colorIndex < 0) { trailPosCount = 0; return; }

        DyeColor dye = DyeColor.values()[colorIndex];
        int color = dye.getMapColor().color;

        // 读取持续时间和扩散半径（gamerule 优先于 config）
        int duration = ElytraTrails.trailDurationTicks;
        double spread = ElytraTrails.spreadRadius;
        MinecraftServer server = self.getEntityWorld().getServer();
        if (server != null) {
            GameRules rules = server.getOverworld().getGameRules();
            duration = rules.getValue(ElytraTrails.TRAIL_DURATION);
            spread = rules.getValue(ElytraTrails.TRAIL_SPREAD);
        }

        // 当前位置（背部/鞘翅）
        var look = self.getRotationVector().multiply(-0.8);
        double cx = self.getX() + look.x;
        double cy = self.getY();
        double cz = self.getZ() + look.z;
        Vec3d currentPos = new Vec3d(cx, cy, cz);

        // 二次贝塞尔：3 点曲线 (p0 → p1 → p2)
        Vec3d p2 = currentPos; // 当前
        Vec3d p1 = trailPosCount >= 1 ? new Vec3d(p1x, p1y, p1z) : p2;
        Vec3d p0 = trailPosCount >= 2 ? new Vec3d(p0x, p0y, p0z) : p1;

        int count = big ? 75 : 25;
        Random rng = self.getRandom();

        for (int i = 0; i < count; i++) {
            float t = (float) i / count; // 0 → 1

            // B(t) = (1-t)²·p0 + 2(1-t)t·p1 + t²·p2
            float u = 1 - t;
            double bx = u*u*p0.x + 2*u*t*p1.x + t*t*p2.x;
            double by = u*u*p0.y + 2*u*t*p1.y + t*t*p2.y;
            double bz = u*u*p0.z + 2*u*t*p1.z + t*t*p2.z;
            Vec3d spawnPos = new Vec3d(bx, by, bz);

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

            self.getEntityWorld().addParticleClient(
                    new TrailParticleEffect(target, color, duration),
                    spawnPos.x, spawnPos.y, spawnPos.z, 0, 0, 0
            );
        }

        // 推进记录：p0 ← p1, p1 ← p2 (当前)
        p0x = p1x; p0y = p1y; p0z = p1z;
        p1x = cx;  p1y = cy;  p1z = cz;
        trailPosCount = Math.min(trailPosCount + 1, 2);
    }
}
