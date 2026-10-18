package top.dreamfall.elytratrails.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.dreamfall.elytratrails.ElytraTrails;
import top.dreamfall.elytratrails.particle.TrailParticleEffect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Unique private double p0x, p0y, p0z;
    @Unique private double p1x, p1y, p1z;
    @Unique private int trailPosCount;
    @Unique private int spawnTickCounter;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!self.getWorld().isClient) return;
        if (!self.isFallFlying()) { trailPosCount = 0; spawnTickCounter = 0; return; }

        ItemStack chest = self.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isOf(Items.ELYTRA)) { trailPosCount = 0; spawnTickCounter = 0; return; }

        var comp = chest.getEnchantments();
        int colorIndex = -1, big = 0, multi = 0;

        for (var entry : comp.getEnchantments()) {
            var optKey = entry.getKey();
            if (optKey.isEmpty()) continue;
            Identifier id = optKey.get().getValue();
            if (!id.getNamespace().equals("elytratrails")) continue;
            String path = id.getPath();
            if (path.equals("big_trail")) big = 1;
            else if (path.equals("multi_trail")) multi = comp.getLevel(entry);
            else if (path.endsWith("_trail")) colorIndex = ElytraTrails.colorIndexFromEnchantId(path);
        }
        if (colorIndex < 0) { trailPosCount = 0; spawnTickCounter = 0; return; }

        ElytraTrails.checkConfigReload();

        if (ElytraTrails.spawnIntervalTicks > 1) {
            spawnTickCounter++;
            if (spawnTickCounter % ElytraTrails.spawnIntervalTicks != 0) {
                return;
            }
        }

        // ---- 颜色（染料本色，不用 MapColor） ----
        int color = ElytraTrails.brightColor(colorIndex);

        // ---- config ----
        int duration = ElytraTrails.trailDurationTicks;
        float spreadRadius = ElytraTrails.spreadRadius;
        if (duration <= 0) duration = 100;

        // ---- 尾迹参数 ----
        int totalTrails = 1 + multi * 2;
        var vel = self.getVelocity();
        Vec3d perp = vel.normalize().crossProduct(new Vec3d(0, 1, 0));
        if (perp.length() < 0.01) perp = new Vec3d(1, 0, 0);
        perp = perp.normalize();
        double spacing = 0.85;

        // 粒子生成位置：玩家身后
        var look = self.getRotationVector().multiply(-0.8);
        double cx = self.getX() + look.x, cy = self.getY(), cz = self.getZ() + look.z;
        Vec3d currentPos = new Vec3d(cx, cy, cz);

        // 贝塞尔曲线 3 控制点（平滑尾迹）
        Vec3d p2 = currentPos;
        Vec3d p1 = trailPosCount >= 1 ? new Vec3d(p1x, p1y, p1z) : p2;
        Vec3d p0 = trailPosCount >= 2 ? new Vec3d(p0x, p0y, p0z) : p1;

        // ---- 粒子属性 ----
        boolean isBig = big > 0;
        int baseCount = isBig ? 60 : 15;
        int count = Math.max(2, baseCount / totalTrails);

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        Vector3f col = new Vector3f(r, g, b);
        float scale = 0.2f;
        Random rng = self.getRandom();

        // ---- 生成粒子 ----
        int halfExtra = totalTrails / 2;
        for (int ti = -halfExtra; ti <= halfExtra; ti++) {
            Vec3d trailOffset = perp.multiply(ti * spacing);
            for (int i = 0; i < count; i++) {
                float t = (float) i / count;
                float u = 1 - t;
                double bx = u*u*p0.x + 2*u*t*p1.x + t*t*p2.x;
                double by = u*u*p0.y + 2*u*t*p1.y + t*t*p2.y;
                double bz = u*u*p0.z + 2*u*t*p1.z + t*t*p2.z;
                Vec3d sp = new Vec3d(bx, by, bz).add(trailOffset);

                self.getWorld().addParticle(
                    new TrailParticleEffect(col, scale, duration),
                    sp.x, sp.y, sp.z, 0, 0, 0
                );
            }
        }

        // 更新控制点
        p0x = p1x; p0y = p1y; p0z = p1z;
        p1x = cx;  p1y = cy;  p1z = cz;
        trailPosCount = Math.min(trailPosCount + 1, 2);
    }
}
