# 屏幕震动实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现屏幕震动效果，受伤时触发20px震动，过关时触发10px震动，指数衰减

**Architecture:** 独立ScreenShakeController管理震动状态，GameCanvas渲染时应用偏移，GameEngine在相应场景触发

**Tech Stack:** Kotlin, Jetpack Compose, Canvas Rendering

---

## 文件结构

```
presentation/src/main/kotlin/com/gameway/presentation/screens/game/
├── ScreenShakeController.kt   # 新建
├── GameEngine.kt              # 修改
└── GameCanvas.kt              # 修改
```

---

## Task 1: 创建 ScreenShakeController

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/ScreenShakeController.kt`

- [ ] **Step 1: 创建 ScreenShakeController.kt**

```kotlin
package com.gameway.presentation.screens.game

import kotlin.random.Random

class ScreenShakeController(
    private val decay: Float = 0.9f,
    private val minIntensity: Float = 0.5f
) {
    private var offsetX = 0f
    private var offsetY = 0f
    private var intensity = 0f

    fun trigger(intensity: Float) {
        this.intensity = intensity
    }

    fun update(): Pair<Float, Float> {
        if (intensity > minIntensity) {
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

    fun getOffsetX(): Float = offsetX
    fun getOffsetY(): Float = offsetY
    fun isActive(): Boolean = intensity > minIntensity
}
```

- [ ] **Step 2: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/game/ScreenShakeController.kt
git commit -m "feat(screen-shake): add ScreenShakeController"
```

---

## Task 2: 更新 GameEngine 触发震动

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`

- [ ] **Step 1: 添加震动触发字段和方法**

在 GameEngine 类中添加:

```kotlin
private var screenShakeIntensity = 0f

fun triggerScreenShake(intensity: Float) {
    screenShakeIntensity = intensity
}

fun getScreenShakeIntensity(): Float = screenShakeIntensity

fun updateScreenShake() {
    if (screenShakeIntensity > 0.5f) {
        screenShakeIntensity *= 0.9f
    } else {
        screenShakeIntensity = 0f
    }
}
```

- [ ] **Step 2: 在受伤时触发震动**

在碰撞检测返回 Hit 时:
```kotlin
if (collision is CollisionResult.Hit) {
    if (character.hasShield()) {
        character = character.removeShield()
    } else {
        character = character.copy(health = character.health - collision.damage)
        if (character.health <= 0) {
            updateGameState(GameState.Failed("被Boss攻击命中！"))
            return
        }
    }
    triggerScreenShake(20f)
}
```

- [ ] **Step 3: 在过关时触发震动**

在 BossEngine.checkSurvival 返回 true 时:
```kotlin
if (BossEngine.checkSurvival(survivalTime)) {
    triggerScreenShake(10f)
    updateGameState(GameState.Completed(chapter, levelNumber, calculateScore()))
    return
}
```

- [ ] **Step 4: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt
git commit -m "feat(screen-shake): trigger shake on damage and level complete"
```

---

## Task 3: 更新 GameCanvas 应用震动

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt`

- [ ] **Step 1: 添加 ScreenShakeController 实例**

在 GameCanvas 或其 ViewModel 中添加:
```kotlin
val screenShakeController = remember { ScreenShakeController() }
```

- [ ] **Step 2: 在游戏循环中更新震动**

在 Canvas 渲染前调用:
```kotlin
val (shakeX, shakeY) = screenShakeController.update()

Canvas(
    modifier = Modifier.offset(
        x = shakeX.dp,
        y = shakeY.dp
    )
) {
    // 渲染逻辑
}
```

- [ ] **Step 3: 从 GameEngine 获取震动触发**

在 GameUiState 中添加 shakeIntensity 字段，在 GameEngine 更新后同步到 UI state。

- [ ] **Step 4: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt
git commit -m "feat(screen-shake): apply screen shake offset to canvas"
```

---

## 实现顺序

1. Task 1: 创建 ScreenShakeController
2. Task 2: 更新 GameEngine 触发震动
3. Task 3: 更新 GameCanvas 应用震动

---

## 验收标准

1. 受伤时屏幕震动（强度20px）
2. 过关时屏幕震动（强度10px）
3. 震动指数衰减，平滑停止
4. 不影响游戏逻辑运行

---

**Plan complete.**