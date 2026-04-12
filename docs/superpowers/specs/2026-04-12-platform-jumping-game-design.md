# 平台跳跃游戏功能设计文档

**日期:** 2026-04-12  
**项目:** FindWay Game  
**目标:** 实现角色自动前进 + 点击跳跃的平台跳跃游戏，确保所有关卡平台可达

---

## 1. 概述

### 当前状态
项目已有基础游戏框架，包含：
- 角色系统、平台系统、物理引擎
- 关卡生成逻辑（随机生成，未检查可达性）
- 蓄力跳跃控制
- 道具系统、碰撞检测

### 问题
1. 平台随机生成，未确保角色能跳到每个平台
2. 蓄力跳跃控制复杂，不符合简化需求
3. 角色前进逻辑不清晰

### 目标
1. 角色持续自动前进，用户只需控制跳跃时机
2. 简化为点击即跳跃（固定跳力）
3. 所有难度关卡都确保平台可达

---

## 2. 核心物理模型

### 2.1 固定跳力物理系统

**物理参数：**
```
JUMP_POWER = 12f         // 固定跳跃力（向上速度）
GRAVITY = 0.6f           // 重力加速度（保持）
MOVE_SPEED = 5f          // 自动前进速度（从3f提高到5f）
```

**跳跃距离计算：**
- 纵向跳跃高度计算：
  - 到达最高点时间：t = v_y / gravity = 12 / 0.6 = 20帧
  - 最大纵向高度：h = v_y * t - 0.5 * gravity * t² ≈ 120像素
  
- 横向跳跃距离计算：
  - 满纵向跳跃周期（上升+下降）：约40帧
  - 最大横向距离：d = MOVE_SPEED * 周期帧数 ≈ 200像素

**物理规则：**
- 点击即跳跃，固定向上速度 = -12
- 角色持续以MOVE_SPEED向右移动
- 重力持续作用，使角色下落
- 保留双重跳跃功能（通过道具获得）

---

## 3. 可达性算法

### 3.1 可达性判断公式

从当前平台(x1, y1)跳跃到目标平台(x2, y2)：

**判断条件：**
```
横向距离：Δx = x2.x - x1.x (前一平台右边缘到下一平台左边缘)
纵向距离：Δy = y2.y - y1.y (正值表示目标平台更高)

可达条件：
1. 横向：Δx ≤ MAX_HORIZONTAL_JUMP_DISTANCE (200像素)
2. 纵向：
   - 若 y2 ≤ y1 (目标平台更低或持平)：始终可达
   - 若 y2 > y1 (目标平台更高)：Δy ≤ MAX_VERTICAL_HEIGHT (120像素)
```

### 3.2 生成逻辑改进

**生成流程：**
1. 第一个平台固定在起点(150, 350)
2. 生成下一个平台时，基于上一个平台的位置
3. 在"可达范围窗口"内随机选择位置
4. 平台生成后立即验证可达性，不可达则重新生成

**可达范围窗口：**
- 横向：前一平台右边缘 + [minGap, maxGap]
- 纵向：前一平台y + [向下最多60像素, 向上最多120像素]

---

## 4. 平台生成重构

### 4.1 新的平台生成逻辑

```kotlin
fun generateNextPlatform(
    lastPlatform: Platform, 
    difficulty: Difficulty, 
    random: Random
): Platform {
    val minHorizontalGap = 80f
    val maxHorizontalGap = when (difficulty) {
        EASY -> 120f
        MEDIUM -> 160f
        HARD -> 180f
        EXPERT -> 200f
    }
    
    val horizontalGap = random.nextFloat(minHorizontalGap, maxHorizontalGap)
    val newX = lastPlatform.right + horizontalGap
    
    val maxVerticalJump = 120f
    val yVariation = when (difficulty) {
        EASY -> random.nextFloat(-60f, 40f)    // 简单更倾向于向下
        MEDIUM -> random.nextFloat(-80f, 80f)
        HARD -> random.nextFloat(-100f, maxVerticalJump)
        EXPERT -> random.nextFloat(-120f, maxVerticalJump)
    }
    
    val newY = (lastPlatform.y + yVariation).coerceIn(200f, 500f)
    
    return Platform(
        id = lastPlatform.id + 1,
        x = newX,
        y = newY,
        width = calculateWidth(difficulty, random),
        type = calculateType(difficulty, random),
        ...
    )
}
```

### 4.2 难度影响

**简单难度：**
- 小间隙（80-120像素）
- 平台更宽（80-120像素）
- 倾向于向下（纵向变化-60到+40）

**中等难度：**
- 中等间隙（100-160像素）
- 平台宽度60-100像素
- 平衡纵向变化（-80到+80）

**困难难度：**
- 大间隙（120-180像素）
- 平台更窄（40-80像素）
- 允许向上跳跃挑战（-100到+120）

**专家难度：**
- 最大间隙（150-200像素）
- 最窄平台（30-60像素）
- 最多纵向挑战（-120到+120）

---

## 5. 游戏控制简化

### 5.1 自动前进逻辑

```kotlin
fun update(character: Character, deltaTime: Long): Character {
    val dt = deltaTime / 16f
    
    // 重力作用
    val newVelocityY = character.velocity.y + GRAVITY * dt
    
    // 自动前进（固定速度）
    val newX = character.position.x + MOVE_SPEED * dt
    
    return character.copy(
        position = Vector2(newX, character.position.y + newVelocityY * dt),
        velocity = Vector2(MOVE_SPEED, newVelocityY),
        state = determineState(character, newVelocityY),
        isGrounded = false
    )
}
```

### 5.2 简化跳跃控制

```kotlin
fun jump() {
    if (gameState !is GameState.Playing) return
    
    if (character.isGrounded || character.jumpCount < character.maxJumps) {
        character = character.copy(
            velocity = Vector2(MOVE_SPEED, -JUMP_POWER),
            isGrounded = false,
            jumpCount = character.jumpCount + 1,
            state = CharacterState.JUMPING
        )
    }
}
```

### 5.3 移除的功能

- 蓄力跳跃逻辑（`startCharge()` 和 `releaseCharge()`）
- `chargeTime` 和 `isCharging` 属性从Character
- 蓄力相关的UI指示器

### 5.4 保留的功能

- 双重跳跃（通过道具获得）
- 道具效果：
  - LIGHTNING: 加速前进 1.3x
  - FEATHER: 减少重力效果 0.5x（延长滞空时间，增加跳跃距离）
  - BUTTERFLY: 双重跳跃
  - SHIELD: 碰撞保护
  - GEM/STAR: 永久道具

---

## 6. 数据模型更新

### 6.1 Constants.kt

```kotlin
object GameConstants {
    // 物理常量 - 固定值
    const val GRAVITY = 0.6f
    const val JUMP_POWER = 12f          // 新增：固定跳跃力
    const val MOVE_SPEED = 5f           // 更新：自动前进速度
    
    // 可达性范围（基于物理计算）
    const val MAX_HORIZONTAL_JUMP_DISTANCE = 200f  // 新增
    const val MAX_VERTICAL_JUMP_HEIGHT = 120f      // 新增
    
    // 移除的常量
    // MIN_JUMP_POWER, MAX_JUMP_POWER, MAX_CHARGE_TIME
    
    // 保留其他常量
    const val TOTAL_CHAPTERS = 10
    const val LEVELS_PER_CHAPTER = 10
    const val MIN_PLATFORMS = 20
    const val MAX_PLATFORMS = 50
    const val MIN_ITEMS_PER_LEVEL = 3
    const val MAX_ITEMS_PER_LEVEL = 8
    const val MIN_COINS_PER_LEVEL = 10
    const val MAX_COINS_PER_LEVEL = 30
    
    const val FEATHER_DURATION = 10000L
    const val BUTTERFLY_DURATION = 15000L
    const val LIGHTNING_DURATION = 8000L
}
```

### 6.2 Character.kt

```kotlin
data class Character(
    val position: Vector2,
    val velocity: Vector2,
    val state: CharacterState,
    val isGrounded: Boolean,
    val jumpCount: Int,
    val maxJumps: Int,
    val activePowerUps: List<ActivePowerUp>,
    val health: Int,
    val coinsCollected: Int
) {
    companion object {
        fun createDefault(): Character = Character(
            position = Vector2(50f, 350f),
            velocity = Vector2(MOVE_SPEED, 0f),
            state = CharacterState.IDLE,
            isGrounded = false,
            jumpCount = 0,
            maxJumps = 1,
            activePowerUps = emptyList(),
            health = 3,
            coinsCollected = 0
        )
    }
    
    val effectiveMoveSpeed: Float
        get() = if (hasActivePowerUp(PowerUpType.LIGHTNING)) 
            MOVE_SPEED * 1.3f else MOVE_SPEED
    
    val effectiveGravity: Float
        get() = if (hasActivePowerUp(PowerUpType.FEATHER))
            GRAVITY * 0.5f else GRAVITY
    
    val hasDoubleJump: Boolean
        get() = maxJumps >= 2 || hasActivePowerUp(PowerUpType.BUTTERFLY)
    
    val hasShield: Boolean
        get() = hasActivePowerUp(PowerUpType.SHIELD)
    
    fun hasActivePowerUp(type: PowerUpType): Boolean {
        val currentTime = System.currentTimeMillis()
        return activePowerUps.any { it.type == type && it.expiresAt > currentTime }
    }
}
```

**移除属性：**
- `chargeTime: Long`
- `isCharging: Boolean`

**移除方法：**
- `effectiveJumpPower`（改为固定跳力）

---

## 7. 碰撞检测与游戏状态

### 7.1 CollisionDetector

保持现有逻辑，无需修改：
- 检测平台着陆（从上方）
- 检测平台侧面碰撞
- 检测金币和道具收集
- 检测掉落屏幕底部

**参数保持：**
```kotlin
private const val SCREEN_BOTTOM = 600f
private const val CHARACTER_WIDTH = 30f
private const val CHARACTER_HEIGHT = 40f
```

### 7.2 GameEngine

```kotlin
class GameEngine {
    private var gameState: GameState = GameState.Loading
    private var character: Character = Character.createDefault()
    
    fun update(): GameState {
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastUpdateTime
        
        character = PhysicsSystem.update(character, deltaTime)
        scrollX = character.position.x - 100f
        
        val collisions = CollisionDetector.checkCollisions(...)
        processCollisions(collisions)
        
        return gameState
    }
    
    fun jump() {
        if (character.isGrounded || character.jumpCount < character.maxJumps) {
            character = character.copy(
                velocity = Vector2(MOVE_SPEED, -JUMP_POWER),
                isGrounded = false,
                jumpCount = character.jumpCount + 1
            )
        }
    }
    
    // 移除方法
    // fun startCharge()
    // fun releaseCharge()
}
```

---

## 8. UI层交互

### 8.1 GameScreen.kt

```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.jump() }
                )
            }
    ) {
        GameCanvas(
            character = viewModel.character,
            platforms = viewModel.platforms,
            scrollX = viewModel.scrollX
        )
        
        ScoreDisplay(score = viewModel.score)
    }
}
```

**移除UI元素：**
- 蓄力指示器
- 蓄力进度条

### 8.2 GameViewModel.kt

```kotlin
class GameViewModel : ViewModel() {
    fun jump() {
        gameEngine.jump()
    }
    
    // 移除方法
    // fun startCharge()
    // fun releaseCharge()
}
```

---

## 9. 实现范围

### 9.1 必须修改的文件

**核心文件：**
1. `core/src/commonMain/kotlin/com/gameway/core/Constants.kt`
2. `domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt`
3. `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt`
4. `domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt`
5. `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`

**UI文件：**
6. `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameScreen.kt`
7. `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt`

### 9.2 测试策略

**单元测试：**
- PhysicsSystemTest: 验证固定跳力物理计算
- PlatformReachabilityTest: 验证可达性算法
- LevelGenerationTest: 验证生成的关卡所有平台都可达

**手动测试：**
- 验证角色自动前进流畅
- 测试跳跃控制响应
- 验证每个关卡都能完成

### 9.3 不在本范围

- 不修改道具系统核心逻辑
- 不修改关卡选择UI
- 不修改存档系统
- 不添加新道具类型
- 不添加音效系统

---

## 10. 实现步骤

### 步骤1: 更新物理常量
修改 `Constants.kt`，添加固定跳力和可达性范围常量

### 步骤2: 简化Character模型
移除蓄力相关属性，更新默认状态

### 步骤3: 重构PhysicsSystem
实现固定跳力物理，简化update方法

### 步骤4: 简化GameEngine
移除蓄力控制，简化jump方法

### 步骤5: 重写平台生成逻辑
在 `Level.kt` 中实现基于可达性的平台生成

### 步骤6: 更新UI层
移除蓄力UI，简化触摸控制

### 步骤7: 测试验证
运行游戏，验证所有关卡可达

---

## 11. 验收标准

1. ✅ 角色持续自动向右前进
2. ✅ 点击屏幕角色立即跳跃（固定跳力）
3. ✅ 所有难度的关卡都能被完成（平台可达性100%）
4. ✅ 双重跳跃功能正常工作（道具激活）
5. ✅ 道具效果正常（加速、增强跳力等）
6. ✅ 碰撞检测准确（着陆、侧面碰撞、掉落）
7. ✅ 游戏循环流畅（60 FPS）

---

## 12. 风险与限制

**潜在风险：**
- 可达性算法过于保守，关卡难度降低
- 固定跳力可能导致游戏缺乏挑战性
- 自动前进速度过快，用户反应时间不足

**缓解措施：**
- 通过难度调整间隙和纵向变化范围
- 保留道具增强效果作为挑战元素
- 测试不同前进速度，调整到合适的值

**限制：**
- 本次不实现音效系统
- 不添加新的平台类型
- 不修改关卡选择和存档UI

---

**文档状态:** 已完成  
**下一步:** 提交设计文档，等待用户审核，然后调用 writing-plans skill 创建实现计划