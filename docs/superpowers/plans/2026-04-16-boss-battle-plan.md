# Boss战实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 每10关引入Boss战，玩家存活30秒即可过关，Boss有弹幕、冲刺、激光三种攻击模式

**Architecture:** 独立Boss类+BossEngine处理攻击逻辑，扩展CollisionDetector处理Boss相关碰撞，LevelGenerator在特定关卡生成Boss

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Koin DI, Canvas Rendering

---

## 文件结构

```
domain/src/commonMain/kotlin/com/gameway/domain/
├── model/
│   ├── BossType.kt          # 新建
│   ├── BossConfig.kt        # 新建
│   ├── Boss.kt              # 新建
│   ├── AttackPattern.kt     # 新建
│   ├── Projectile.kt        # 新建
│   ├── Level.kt             # 修改
│   └── LevelType.kt         # 新建（从Level.kt拆分）
├── engine/
│   ├── BossEngine.kt        # 新建
│   ├── LevelGenerator.kt    # 修改
│   ├── CollisionDetector.kt # 修改
│   └── GameEngine.kt        # 修改
core/src/commonMain/kotlin/com/gameway/core/
└── Constants.kt             # 修改
presentation/src/main/kotlin/com/gameway/presentation/
└── screens/game/
    └── GameCanvas.kt        # 修改
```

---

## Task 1: 创建Boss数据模型

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/BossType.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/AttackPattern.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/BossConfig.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Boss.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Projectile.kt`
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/LevelType.kt`

- [ ] **Step 1: 创建 BossType.kt**

```kotlin
package com.gameway.domain.model

enum class BossType {
    SLIME,
    BAT,
    DRAGON,
    GOLEM,
    DEMON
}
```

- [ ] **Step 2: 创建 AttackPattern.kt**

```kotlin
package com.gameway.domain.model

enum class AttackPattern {
    PROJECTILE,
    DASH,
    LASER
}
```

- [ ] **Step 3: 创建 BossConfig.kt**

```kotlin
package com.gameway.domain.model

data class BossConfig(
    val type: BossType,
    val name: String,
    val emoji: String,
    val attackPatterns: List<AttackPattern>,
    val moveSpeed: Float,
    val isFlying: Boolean,
    val health: Int = 1
) {
    companion object {
        val SLIME = BossConfig(BossType.SLIME, "史莱姆", "🟢", listOf(AttackPattern.PROJECTILE), 2f, false)
        val BAT = BossConfig(BossType.BAT, "蝙蝠", "🦇", listOf(AttackPattern.DASH), 5f, true)
        val DRAGON = BossConfig(BossType.DRAGON, "龙", "🐉", listOf(AttackPattern.LASER), 3f, true)
        val GOLEM = BossConfig(BossType.GOLEM, "石头人", "🗿", listOf(AttackPattern.PROJECTILE, AttackPattern.DASH), 2f, false)
        val DEMON = BossConfig(BossType.DEMON, "恶魔", "👹", AttackPattern.entries, 4f, true)

        fun getForChapter(chapter: Int): BossConfig {
            return when {
                chapter <= 1 -> SLIME
                chapter <= 2 -> BAT
                chapter <= 3 -> DRAGON
                chapter <= 4 -> GOLEM
                else -> DEMON
            }
        }
    }
}
```

- [ ] **Step 4: 创建 Projectile.kt**

```kotlin
package com.gameway.domain.model

data class Projectile(
    val position: Vector2,
    val velocity: Vector2,
    val radius: Float,
    val damage: Int = 1
)
```

- [ ] **Step 5: 创建 Boss.kt**

```kotlin
package com.gameway.domain.model

data class Boss(
    val id: Int,
    val config: BossConfig,
    val position: Vector2,
    val velocity: Vector2,
    val state: BossState,
    val currentAttackPattern: AttackPattern?,
    val attackTimer: Float,
    val isActive: Boolean,
    val dashTarget: Vector2?,
    val laserActive: Boolean,
    val laserAngle: Float,
    val phase: Int
) {
    companion object {
        fun create(chapter: Int): Boss {
            val config = BossConfig.getForChapter(chapter)
            return Boss(
                id = chapter,
                config = config,
                position = Vector2(GameConstants.BOSS_SPAWN_X, GameConstants.BOSS_SPAWN_Y),
                velocity = Vector2.ZERO,
                state = BossState.APPEARING,
                currentAttackPattern = null,
                attackTimer = 2f,
                isActive = true,
                dashTarget = null,
                laserActive = false,
                laserAngle = 0f,
                phase = 1
            )
        }
    }
}

enum class BossState {
    APPEARING,
    IDLE,
    ATTACKING,
    DASHING,
    LASERING,
    DEFEATED
}
```

- [ ] **Step 6: 创建 LevelType.kt**

```kotlin
package com.gameway.domain.model

enum class LevelType {
    NORMAL,
    BOSS
}
```

- [ ] **Step 7: 修改 Level.kt 添加 LevelType 和 Boss 字段**

在 `Level` data class 中添加:
```kotlin
val type: LevelType,
val boss: Boss?,
val survivalTime: Float = 0f
```

- [ ] **Step 8: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/
git commit -m "feat(boss): add boss data models"
```

---

## Task 2: 创建常量

**Files:**
- Modify: `core/src/commonMain/kotlin/com/gameway/core/Constants.kt`

- [ ] **Step 1: 添加Boss相关常量到 Constants.kt**

在文件末尾添加:
```kotlin
const val BOSS_SPAWN_X = 800f
const val BOSS_SPAWN_Y = 300f
const val SURVIVAL_TIME = 30f
const val CHARACTER_RADIUS = 20f
const val BOSS_DASH_RADIUS = 40f
const val PROJECTILE_RADIUS = 15f
```

- [ ] **Step 2: 提交**

```bash
git add core/src/commonMain/kotlin/com/gameway/core/Constants.kt
git commit -m "feat(boss): add boss constants"
```

---

## Task 3: 创建 BossEngine

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/engine/BossEngine.kt`

- [ ] **Step 1: 创建 BossEngine.kt**

```kotlin
package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.*
import kotlin.math.*

object BossEngine {
    private const val PROJECTILE_SPEED = 6f
    private const val PROJECTILE_INTERVAL = 1.5f
    private const val LASER_DURATION = 2f
    private const val DASH_SPEED = 12f
    private const val SURVIVAL_TIME = 30f

    fun update(boss: Boss, character: Character, deltaTime: Float): BossUpdateResult {
        if (!boss.isActive) return BossUpdateResult(boss, emptyList(), false)

        var currentBoss = boss
        val projectiles = mutableListOf<Projectile>()
        var laserHit = false

        currentBoss = currentBoss.copy(
            attackTimer = currentBoss.attackTimer - deltaTime
        )

        when (currentBoss.state) {
            BossState.APPEARING -> {
                if (currentBoss.attackTimer <= 0) {
                    currentBoss = currentBoss.copy(state = BossState.IDLE, attackTimer = 1f)
                }
            }
            BossState.IDLE -> {
                if (currentBoss.attackTimer <= 0) {
                    val pattern = selectAttackPattern(currentBoss)
                    currentBoss = when (pattern) {
                        AttackPattern.PROJECTILE -> {
                            projectiles.addAll(fireProjectiles(currentBoss, character))
                            currentBoss.copy(state = BossState.ATTACKING, attackTimer = PROJECTILE_INTERVAL, currentAttackPattern = pattern)
                        }
                        AttackPattern.DASH -> {
                            currentBoss.copy(
                                state = BossState.DASHING,
                                dashTarget = Vector2(character.position.x, currentBoss.position.y),
                                attackTimer = 0.5f,
                                currentAttackPattern = pattern
                            )
                        }
                        AttackPattern.LASER -> {
                            currentBoss.copy(
                                state = BossState.LASERING,
                                laserActive = true,
                                laserAngle = calculateLaserAngle(currentBoss.position, character.position),
                                attackTimer = LASER_DURATION,
                                currentAttackPattern = pattern
                            )
                        }
                    }
                }
            }
            BossState.ATTACKING -> {
                if (currentBoss.attackTimer <= 0) {
                    currentBoss = currentBoss.copy(state = BossState.IDLE, attackTimer = 1f)
                }
            }
            BossState.DASHING -> {
                val direction = currentBoss.dashTarget!!.x - currentBoss.position.x
                currentBoss = currentBoss.copy(
                    position = currentBoss.position.copy(
                        x = currentBoss.position.x + sign(direction) * DASH_SPEED
                    )
                )
                if (abs(currentBoss.position.x - currentBoss.dashTarget!!.x) < DASH_SPEED) {
                    currentBoss = currentBoss.copy(state = BossState.IDLE, attackTimer = 1.5f, dashTarget = null)
                }
            }
            BossState.LASERING -> {
                if (currentBoss.attackTimer <= 0) {
                    currentBoss = currentBoss.copy(state = BossState.IDLE, laserActive = false, attackTimer = 1f)
                }
            }
            BossState.DEFEATED -> {}
        }

        return BossUpdateResult(currentBoss, projectiles, laserHit)
    }

    private fun selectAttackPattern(boss: Boss): AttackPattern {
        val availablePatterns = boss.config.attackPatterns
        return availablePatterns.random()
    }

    private fun fireProjectiles(boss: Boss, character: Character): List<Projectile> {
        val projectiles = mutableListOf<Projectile>()
        val angleToCharacter = atan2(
            character.position.y - boss.position.y,
            character.position.x - boss.position.x
        )

        for (i in -2..2) {
            val angle = angleToCharacter + i * 0.2f
            projectiles.add(Projectile(
                position = boss.position.copy(),
                velocity = Vector2(cos(angle) * PROJECTILE_SPEED, sin(angle) * PROJECTILE_SPEED),
                radius = GameConstants.PROJECTILE_RADIUS
            ))
        }
        return projectiles
    }

    private fun calculateLaserAngle(bossPos: Vector2, targetPos: Vector2): Float {
        return atan2(targetPos.y - bossPos.y, targetPos.x - bossPos.x)
    }

    fun checkSurvival(survivalTime: Float): Boolean {
        return survivalTime >= SURVIVAL_TIME
    }

    fun getSurvivalTimeRequired(): Float = SURVIVAL_TIME
}

data class BossUpdateResult(
    val boss: Boss,
    val projectiles: List<Projectile>,
    val laserHit: Boolean
)
```

- [ ] **Step 2: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/BossEngine.kt
git commit -m "feat(boss): add BossEngine with attack patterns"
```

---

## Task 4: 扩展 CollisionDetector

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/CollisionDetector.kt`

- [ ] **Step 1: 添加 Boss 相关碰撞检测方法**

在 `CollisionDetector` 对象中添加:

```kotlin
fun checkBossCollisions(
    character: Character,
    boss: Boss,
    projectiles: List<Projectile>,
    laserActive: Boolean,
    laserAngle: Float
): CollisionResult {
    for (projectile in projectiles) {
        if (circleCollision(
            character.position,
            GameConstants.CHARACTER_RADIUS,
            projectile.position,
            projectile.radius
        )) {
            return CollisionResult.Hit(projectile.damage)
        }
    }

    if (boss.state == BossState.DASHING) {
        if (circleCollision(
            character.position,
            GameConstants.CHARACTER_RADIUS,
            boss.position,
            GameConstants.BOSS_DASH_RADIUS
        )) {
            return CollisionResult.Hit(1)
        }
    }

    if (laserActive) {
        if (checkLaserCollision(character.position, boss.position, laserAngle)) {
            return CollisionResult.Hit(1)
        }
    }

    return CollisionResult.None
}

private fun checkLaserCollision(characterPos: Vector2, laserOrigin: Vector2, angle: Float): Boolean {
    val laserLength = 1000f
    val laserEnd = Vector2(
        laserOrigin.x + cos(angle) * laserLength,
        laserOrigin.y + sin(angle) * laserLength
    )
    val distance = pointToLineDistance(characterPos, laserOrigin, laserEnd)
    return distance < GameConstants.CHARACTER_RADIUS + 10f
}

private fun pointToLineDistance(point: Vector2, lineStart: Vector2, lineEnd: Vector2): Float {
    val A = point.x - lineStart.x
    val B = point.y - lineStart.y
    val C = lineEnd.x - lineStart.x
    val D = lineEnd.y - lineStart.y

    val dot = A * C + B * D
    val lenSq = C * C + D * D
    val param = if (lenSq != 0f) dot / lenSq else -1f

    val xx = when {
        param < 0 -> lineStart.x
        param > 1 -> lineEnd.x
        else -> lineStart.x + param * C
    }
    val yy = when {
        param < 0 -> lineStart.y
        param > 1 -> lineEnd.y
        else -> lineStart.y + param * D
    }

    val dx = point.x - xx
    val dy = point.y - yy
    return sqrt(dx * dx + dy * dy)
}
```

- [ ] **Step 2: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/CollisionDetector.kt
git commit -m "feat(boss): add boss collision detection"
```

---

## Task 5: 更新 LevelGenerator

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/LevelGenerator.kt`

- [ ] **Step 1: 更新 LevelGenerator 生成 Boss 关卡**

找到 `generateLevelContent` 方法，修改平台生成逻辑:

```kotlin
private fun generateLevelContent(levelNumber: Int, chapter: Int, difficulty: Difficulty, random: Random): LevelContent {
    val isBossLevel = levelNumber % 10 == 0
    val platforms = if (isBossLevel) {
        generateBossLevelPlatforms(random)
    } else {
        generatePlatforms(platformCount, difficulty, random)
    }

    return LevelContent(
        platforms = platforms,
        coins = generateCoins(coinCount, platforms, random),
        powerUps = generatePowerUps(powerUpCount, platforms, random),
        boss = if (isBossLevel) Boss.create(chapter) else null
    )
}

private fun generateBossLevelPlatforms(random: Random): List<Platform> {
    return generatePlatforms(8, Difficulty.HARD, random).map { platform ->
        platform.copy(width = platform.width * 1.5f)
    }
}
```

同时更新 `LevelContent` data class 添加 `boss` 字段。

- [ ] **Step 2: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/LevelGenerator.kt
git commit -m "feat(boss): add boss level generation"
```

---

## Task 6: 更新 GameState

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/model/GameState.kt`

- [ ] **Step 1: 添加 BossActive 状态**

在 `GameState` 密封类中添加:
```kotlin
data class BossActive(val survivalTime: Float) : GameState()
```

- [ ] **Step 2: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/GameState.kt
git commit -m "feat(boss): add BossActive game state"
```

---

## Task 7: 更新 GameEngine

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`

- [ ] **Step 1: 添加 Boss 战处理逻辑**

在 `GameEngine` 中添加:
```kotlin
private var survivalTime = 0f
private var projectiles = listOf<Projectile>()

fun update(deltaTime: Float) {
    when (val state = uiState.value.gameState) {
        is GameState.BossActive -> {
            survivalTime += deltaTime

            val bossResult = BossEngine.update(boss!!, character, deltaTime)
            boss = bossResult.boss
            projectiles = bossResult.projectiles

            val collision = CollisionDetector.checkBossCollisions(
                character,
                boss,
                projectiles,
                boss.laserActive,
                boss.laserAngle
            )

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
            }

            if (BossEngine.checkSurvival(survivalTime)) {
                updateGameState(GameState.Completed(chapter, levelNumber, calculateScore()))
                return
            }

            updateGameState(GameState.BossActive(survivalTime))
        }
        // ... 其他状态处理
    }
}

private fun Character.hasShield(): Boolean = activePowerUps.any { it.type == PowerUpType.SHIELD }
private fun Character.removeShield(): Character = copy(
    activePowerUps = activePowerUps.filter { it.type != PowerUpType.SHIELD }
)
```

- [ ] **Step 2: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt
git commit -m "feat(boss): integrate boss battle into game engine"
```

---

## Task 8: 更新 GameCanvas 渲染

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt`

- [ ] **Step 1: 添加 Boss 和存活时间渲染**

在 Canvas 的 drawBlock 中添加:

```kotlin
if (level.type == LevelType.BOSS && boss != null) {
    drawBoss(boss, viewportX)
    drawProjectiles(boss, viewportX)
    if (boss.laserActive) {
        drawLaser(boss.position, boss.laserAngle, viewportX)
    }
    drawBossHealthBar(boss, survivalTime)
}

if (level.type == LevelType.BOSS) {
    drawSurvivalTimer(survivalTime, BossEngine.getSurvivalTimeRequired())
}
```

添加辅助方法:
```kotlin
private fun DrawScope.drawBoss(boss: Boss, viewportX: Float) {
    val screenX = boss.position.x - viewportX
    val screenY = boss.position.y

    drawContext.canvas.nativeCanvas.drawText(
        boss.config.emoji,
        screenX,
        screenY,
        Paint().apply {
            textSize = 60f
            textAlign = Paint.Align.CENTER
        }
    )
}

private fun DrawScope.drawProjectiles(boss: Boss, viewportX: Float) {
    // 渲染弹幕
}

private fun DrawScope.drawLaser(bossPosition: Vector2, angle: Float, viewportX: Float) {
    val startX = bossPosition.x - viewportX
    val startY = bossPosition.y
    val endX = startX + cos(angle) * 2000f
    val endY = startY + sin(angle) * 2000f

    drawLine(Color.Red, Offset(startX, startY), Offset(endX, endY), strokeWidth = 8f, cap = StrokeCap.Round)
    drawLine(Color.Yellow.copy(alpha = 0.5f), Offset(startX, startY), Offset(endX, endY), strokeWidth = 16f, cap = StrokeCap.Round)
}

private fun DrawScope.drawSurvivalTimer(currentTime: Float, totalTime: Float) {
    val remaining = (totalTime - currentTime).coerceAtLeast(0f)
    val text = "存活时间: ${remaining.toInt()}秒"

    drawContext.canvas.nativeCanvas.drawText(
        text,
        size.width / 2,
        50f,
        Paint().apply {
            textSize = 32f
            color = Color.White.asAndroidColor()
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 2f, 2f, Color.Black.asAndroidColor())
        }
    )
}

private fun DrawScope.drawBossHealthBar(boss: Boss, survivalTime: Float) {
    // 显示存活时间进度条
}
```

- [ ] **Step 2: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt
git commit -m "feat(boss): add boss rendering to GameCanvas"
```

---

## Task 9: 集成测试

**Files:**
- Test: `domain/src/commonMain/kotlin/com/gameway/domain/engine/BossEngineTest.kt`

- [ ] **Step 1: 创建 BossEngine 测试**

```kotlin
class BossEngineTest {
    @Test
    fun `test selectAttackPattern returns valid pattern`() {
        val boss = Boss.create(chapter = 1)
        val pattern = BossEngine.selectAttackPattern(boss)
        assertTrue(pattern in boss.config.attackPatterns)
    }

    @Test
    fun `test checkSurvival returns true after 30 seconds`() {
        assertTrue(BossEngine.checkSurvival(30f))
        assertFalse(BossEngine.checkSurvival(29f))
    }

    @Test
    fun `test fireProjectiles creates 5 projectiles`() {
        val boss = Boss.create(chapter = 1)
        val character = Character.createDefault()
        val projectiles = BossEngine.fireProjectiles(boss, character)
        assertEquals(5, projectiles.size)
    }
}
```

- [ ] **Step 2: 运行测试**

```bash
./gradlew :domain:jvmTest --tests "com.gameway.domain.engine.BossEngineTest"
```

- [ ] **Step 3: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/BossEngineTest.kt
git commit -m "test(boss): add BossEngine unit tests"
```

---

## 实现顺序

1. Task 1: 创建Boss数据模型
2. Task 2: 创建常量
3. Task 3: 创建BossEngine
4. Task 4: 扩展CollisionDetector
5. Task 5: 更新LevelGenerator
6. Task 6: 更新GameState
7. Task 7: 更新GameEngine
8. Task 8: 更新GameCanvas渲染
9. Task 9: 集成测试

---

## 验收标准

1. 第10/20/.../100关为Boss关
2. Boss有弹幕、冲刺、激光三种攻击模式
3. 玩家存活30秒即可过关
4. 被攻击命中扣血（护盾可抵消一次）
5. Boss有入场动画和状态切换
6. 存活时间倒计时显示
7. 不同章节Boss类型不同

---

**Plan complete.**