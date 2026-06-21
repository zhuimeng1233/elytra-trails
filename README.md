# 鞘翅尾迹 (Elytra Trails)

> 🛩️ 飞行时留下彩色尾迹！原版画风的鞘翅专属附魔模组。

---

## ⚠️ AI 构建声明

本项目**完全由 AI（Claude）构建**，仅经过人工代码审查和游戏内测试。

- 可能遗留未发现的 Bug
- 不保证与其他所有模组的兼容性
- 但经过实际测试，兼容性良好，与原版及常见模组无冲突

---

## 📋 适用版本

| 平台 | 版本 |
|------|------|
| Minecraft | **1.21.11** |
| Fabric Loader | **≥ 0.15** |
| Fabric API | **任意 1.21.11 版本** |

> ⚠️ 仅支持 Fabric，不支持 Forge / NeoForge。

---

## ✨ 功能

- 16 种颜色的**鞘翅尾迹附魔**（对应所有染料颜色）
- **大尾迹附魔**——更大的粒子扩散范围
- 原版 `trail` 粒子，无缝融入游戏画风
- 每 tick 生成 25 个粒子（大尾迹 75 个），尾迹连续不断
- 二次贝塞尔曲线插值，拐弯平滑无毛刺
- 配置文件 + gamerule 双重控制
- 自定义合成配方（工作台直接合成附魔书）

---

## 🎮 使用方式

### 合成

> ⚠️ **JEI 暂无法查询本模组的配方**，请按下方配方手动合成：

| 材料 | 产出 |
|------|------|
| 书 + 任意染料 | 对应颜色尾迹附魔书 |
| 书 + 火药 | 大尾迹附魔书 |

合成示例：

![合成示例](img/recipe_example.png)

### 附魔

在铁砧上将附魔书打在鞘翅上即可。

### 配置

首次启动后生成 `config/elytra-trails.json`：

```json
{
  "spreadRadius": 1.0,
  "spawnInterval": 1,
  "trailDuration": 100
}
```

### 游戏规则（gamerule）

| 命令 | 说明 | 范围 |
|------|------|------|
| `/gamerule elytraTrailDuration 200` | 尾迹持续时间（tick） | 20 ~ 5000 |
| `/gamerule elytraTrailSpread 2.0` | 大尾迹扩散半径（格） | 0 ~ 10 |

> gamerule 优先级高于配置文件

---

## 🔧 技术细节

- **1 个 Mixin**：`PlayerEntity.tick()` 注入，最小侵入
- 数据驱动的附魔系统（纯 JSON 定义）
- 自定义配方类型（`CraftingRecipe`）
- 原版 `ParticleTypes.TRAIL` 粒子，无需自定义纹理

---

## 📜 开源协议

[MIT License](LICENSE)

---

## 👤 作者

坠梦 · 2026
