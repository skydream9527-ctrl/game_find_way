# 屏幕震动效果设计文档

**日期:** 2026-04-16
**项目:** FindWay Game
**目标:** 实现屏幕震动效果，增强游戏反馈体验

---

## 1. 概述

### 当前状态
- 游戏仅有基础渲染，无震动效果
- 受伤、过关等重要事件缺乏视觉反馈

### 改进目标
1. 受伤时触发屏幕震动（强度20px，指数衰减）
2. 过关时触发屏幕震动（强度10px，指数衰减）
3. 震动效果平滑，不影响游戏逻辑

---

## 2. 设计方案

### 2.1 ScreenShakeController

```kotlin
class ScreenShakeController {
    private var offsetX = 0f
    private var offsetY = 0f
    private var intensity = 0f
    private var decay = 0.9f

    fun trigger(intensity: Float) {
        this.intensity = intensity
    }

    fun update(): Pair<Float, Float> {
        if (intensity > 0.5f) {
            offsetX = (Random.nextFloat() - 0.5f) * 2 * intensity
            offsetY = (Random.nextFloat() - 0.5f) * 2 * intensity
            intensity *= decay
        } else {
            offsetX = 0f
            offsetY = 0f
            intensity = 0f
        }
        return Pair(offsetX, offsetY)
    }

    fun getOffset(): Vector2 = Vector2(offsetX, offsetY)
}
```

### 2.2 触发场景

| 场景 | 强度 | 衰减系数 |
|------|------|----------|
| 受伤 | 20px | 0.9 |
| 过关 | 10px | 0.9 |

### 2.3 GameCanvas集成

```kotlin
@Composable
fun GameCanvas(viewModel: GameViewModel) {
    val screenShake = remember { ScreenShakeController() }

    LaunchedEffect(uiState.shakeIntensity) {
        if (uiState.shakeIntensity > 0) {
            screenShake.trigger(uiState.shakeIntensity)
        }
    }

    val (offsetX, offsetY) = screenShake.update()

    Canvas(
        modifier = Modifier.offset(
            x = offsetX.dp,
            y = offsetY.dp
        )
    ) {
        // 渲染逻辑
    }
}
```

### 2.4 GameEngine集成

```kotlin
// 受伤时
character = character.copy(health = character.health - damage)
screenShakeIntensity = 20f

// 过关时
updateGameState(GameState.Completed(...))
screenShakeIntensity = 10f
```

---

## 3. 实现步骤

### 阶段1：ScreenShakeController
1. 创建 `ScreenShakeController.kt`
2. 实现 `trigger()` 和 `update()` 方法

### 阶段2：GameEngine集成
1. 添加 `screenShakeIntensity` 字段
2. 在受伤和过关时触发震动

### 阶段3：GameCanvas集成
1. 添加 ScreenShakeController 实例
2. 在 Canvas modifier 中应用偏移

---

## 4. 文件修改清单

**新建文件：**
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/ScreenShakeController.kt`

**修改文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt`

---

## 5. 验收标准

1. ✅ 受伤时屏幕震动（强度20px）
2. ✅ 过关时屏幕震动（强度10px）
3. ✅ 震动指数衰减，平滑停止
4. ✅ 不影响游戏逻辑运行

---

**文档状态:** 已完成
**下一步:** 用户审核后创建实现计划