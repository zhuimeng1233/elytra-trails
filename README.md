# 鞘翅尾迹 (Elytra Trails) - NeoForge Edition

> 🛩️ 飞行时留下彩色尾迹！原版画风的鞘翅专属附魔模组（NeoForge 版本）。
>
> colorful · color trail · 尾迹 · 轨迹 · 彩色 · 粒子特效

> **版本差异说明**：不同 Minecraft 版本的原版 API 与粒子实现存在差异，实际尾迹效果可能略有不同，具体以对应版本实测为准。

---

## 📋 适用版本与目录

本项目采用单分支多版本（Monorepo）结构，全面支持 Minecraft 1.21 全系列的 NeoForge 加载器：

| 版本目录 | 适配 Minecraft 版本 | NeoForge 版本 | 特性说明 |
|:---|:---|:---|:---|
| `versions/1.21` | **1.21** | `21.0.167` | 自定义长寿命粒子 + Java 配方序列化器 |
| `versions/1.21.1` | **1.21.1** | `21.1.262` | 自定义长寿命粒子 + Java 配方序列化器 |
| `versions/1.21.2-1.21.3` | **1.21.2 ~ 1.21.3** | `21.3.100` | 原版 2 参数 `TargetColorParticleOption` + 纯 JSON 配方 |
| `versions/1.21.4-1.21.10` | **1.21.4 ~ 1.21.10** | `21.4.161` | 原版 3 参数 `TrailParticleOption(target, color, duration)` |
| `versions/1.21.11` | **1.21.11** | `21.11.48` | 适配最新 `net.minecraft.resources.Identifier` 架构 |

---

## ⚙️ 配置说明

首次启动后在客户端生成 `config/elytra-trails.json`：

```json
{
  "spreadRadius": 1.0,
  "spawnInterval": 1,
  "trailDuration": 100
}
```

| 配置项 | 说明 | 默认 |
|--------|------|------|
| `spreadRadius` | 大尾迹扩散半径（格） | 1.0 |
| `spawnInterval` | 粒子生成间隔（tick，越大越稀疏/越省性能） | 1 |
| `trailDuration` | 粒子持续时间（tick） | 100 |

> 💡 配置文件支持**动态热重载**，在游戏运行期间修改并保存文件，1 秒内即时生效，无需重启游戏。

---

## 🔨 构建方式

根目录采用 Gradle 复合构建（Composite Build）：

```bash
# 一键编译全部版本
.\gradlew.bat build

# 或进入特定版本目录编译单独版本
cd versions/1.21.1
..\..\gradlew.bat build
```

编译产物位于各版本目录下的 `build/libs/` 中。

---

## 📜 开源协议

MIT License
