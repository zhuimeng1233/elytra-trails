# 鞘翅尾迹 (Elytra Trails) — 1.21.1 移植版

> ✅ **所有已知问题已修复** (2026-06-27)

> **版本差异说明**：不同 Minecraft 版本的原版 API 与粒子实现存在差异，实际尾迹效果可能略有不同，具体以对应版本实测为准。

---

## 功能

- 鞘翅飞行时留下彩色粒子尾迹（16 种颜色）
- **大尾迹**附魔：更密集的尾迹，扩散范围可配
- **多轨迹**附魔：2~5 条平行尾迹（共 5 级）
- 配置文件轻量热重载：通过 `config/elytra-trails.json` 随心调节尾迹寿命 (`trailDuration`)、扩散半径 (`spreadRadius`) 与生成间隔 (`spawnInterval`)，保存文件即刻生效，无需重启游戏
- JEI 集成：配方可查、可自动填充

## 合成配方

| 附魔 | 配方 |
|------|------|
| 颜色尾迹 × 16 | 书 + 对应染料 |
| 大尾迹 | 书 + 火药 |
| 多轨迹 | 书 + 火药 + 燧石 |

---

## 项目结构

```
src/main/java/top/dreamfall/elytratrails/
├── ElytraTrails.java              # ModInitializer：ParticleType 注册 + 配置热重载
├── ElytraTrailsClient.java        # ClientModInitializer：粒子工厂注册
├── mixin/
│   └── PlayerEntityMixin.java     # 鞘翅飞行粒子生成（贝塞尔曲线平滑 + 历史缓冲）
├── particle/
│   ├── TrailParticleEffect.java   # 粒子数据（color + scale + duration）
│   └── TrailParticle.java         # 粒子实例（长寿命 + ease-out 淡出 + fullbright）
└── recipe/
    ├── TrailRecipe.java           # 自定义配方（继承 ShapelessRecipe，兼容 JEI）
    └── ElytraTrailsRecipes.java   # 配方序列化器注册

src/main/resources/
├── fabric.mod.json
├── elytratrails.mixins.json
├── assets/elytratrails/
│   ├── lang/                      # 中/英翻译
│   └── particles/trail.json       # 粒子纹理定义
└── data/elytratrails/
    ├── enchantment/               # 18 个附魔 JSON
    ├── recipe/                    # 18 个配方 JSON
    └── tags/                      # 附魔互斥标签
```

---

## 技术要点

### 粒子系统

自定义 `ParticleType<TrailParticleEffect>`，粒子寿命由配置文件 `trailDuration` 控制（不是 `DustParticleEffect` 的固定 ≈20 tick）。

- **寿命**：`maxAge = duration`（默认 100 tick = 5 秒）
- **淡出**：ease-out 平方 `alpha = 1 - (age/maxAge)²`
- **光照**：`getBrightness()` 返回 `15728880`（全天光），晚上也清晰可见
- **纹理**：复用原版 `generic_0~7`，PARTICLE_SHEET_OPAQUE
- **注意**：手写粒子系统与1.21.11的存在效果上的区别

### JEI 兼容

`TrailRecipe` 继承 `ShapelessRecipe` 而非直接实现 `CraftingRecipe`。JEI 的 `VanillaPlugin` 只注册 `isHandled() == true` 的配方，自定义 `CraftingRecipe` 会被归为 unhandled 后丢弃。

### 1.21.11 vs 1.21.1 API 差异

| 功能 | 1.21.11 | 1.21.1 |
|------|---------|--------|
| `Entity.getWorld()` | `getEntityWorld()` | `getWorld()` |
| 鞘翅飞行 | `isGliding()` | `isFallFlying()` |
| 配方 result components | ✅ | ❌ |
| `Recipe.fits()` | ❌ | ✅ |
| `Vector3f.CODEC` | ✅ | ❌（手动序列化 r/g/b） |
| 粒子同步 | 需 `RecipeSynchronization` | 自动 |

---

## 构建

```bash
./gradlew build
# 产物: build/libs/elytra-trails-1.0.0.jar
```

| 依赖 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| Fabric Loader | ≥ 0.16.5 |
| Fabric API | ≥ 0.102.0+1.21.1 |
| Gradle | 8.11 |
| Java | 21 |
