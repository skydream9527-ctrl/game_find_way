# Boss战设计文档

**日期:** 2026-04-16
**项目:** FindWay Game
**目标:** 每10关引入Boss战，混合模式（跑酷+Boss攻击），存活30秒即可过关

---

## 1. 概述

### 当前状态
- 无限跑酷游戏，程序化生成平台、金币、道具
- 无Boss机制
- 每10关难度递增（EASY → EXPERT）

### 改进目标
1. 第10/20/30/.../100关为Boss关
2. 混合模式：边跑酷边躲避Boss攻击
3. Boss有3种攻击模式：弹幕、冲刺、激光
4. 玩家存活30秒即可过关

---

## 2. Boss数据模型

### 2.1 Boss类型枚举

```kotlin
enum class BossType {
    SLIME,      // 史莱姆 - 慢速弹幕
    BAT,        // 蝙蝠 - 冲刺攻击
    DRAGON,     // 龙 - 激光攻击
    GOLEM,      // 石头人 - 混合攻击
    DEMON       // 恶魔 - 全能型
}
```

### 2.2 Boss配置数据

| Boss | Emoji | 章节 | 攻击模式 | 移动速度 | 特殊能力 |
|------|-------|------|----------|----------|----------|
| SLIME | 🟢 | 1-10 | 弹幕 | 慢 | 无 |
| BAT | 🦇 | 11-20 | 冲刺 | 快 | 无 |
| DRAGON | 🐉 | 21-30 | 激光 | 中 | 飞行 |
| GOLEM | 🗿 | 31-40 | 弹幕+冲刺 | 慢 | 护盾 |
| DEMON | 👹 | 41-50 | 弹幕+冲刺+激光 | 快 | 隐身 |

```kotlin
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

enum class AttackPattern {
    PROJECTILE,  // 弹幕：从Boss位置发射圆形弹幕
    DASH,        // 冲刺：快速冲向玩家
    LASER        // 激光：从屏幕一侧到另一侧的激光束
}
```

### 2.3 Boss模型

```kotlin
data class Boss(
    val id: Int,
    val config: BossConfig,
    val position: Vector2,           // 世界坐标
    val velocity: Vector2,
    val state: BossState,
    val currentAttackPattern: AttackPattern?,
    val attackTimer: Float,         // 距离下次攻击的时间
    val isActive: Boolean,          // Boss是否在场
    val dashTarget: Vector2?,       // 冲刺目标位置
    val laserActive: Boolean,
    val laserAngle: Float,
    val phase: Int                 // Boss阶段（影响攻击频率）
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
    APPEARING,  // 入场动画
    IDLE,       // 待机
    ATTACKING,  // 攻击中
    DASHING,    // 冲刺中
    LASERING,   // 激光中
    DEFEATED    // 被击败（存活30秒后）
}

data class Projectile(
    val position: Vector2,
    val velocity: Vector2,
    val radius: Float,
    val damage: Int = 1
)
```

---

## 3. 游戏逻辑改动

### 3.1 关卡类型

```kotlin
enum class LevelType {
    NORMAL,  // 普通关卡
    BOSS     // Boss关
}

data class Level(
    val id: Int,
    val chapter: Int,
    val number: Int,
    val type: LevelType,
    val difficulty: Difficulty,
    val platforms: List<Platform>,
    val coins: List<Coin>,
    val powerUps: List<PowerUp>,
    val boss: Boss?,           // Boss关特有
    val survivalTime: Float = 0f  // Boss关存活计时
)
```

### 3.2 LevelGenerator更新

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
    // Boss关平台更少、更宽、间隔更近
    // 确保玩家有足够的躲避空间
    return generatePlatforms(8, Difficulty.HARD, random).map { platform ->
        platform.copy(width = platform.width * 1.5f)
    }
}
```

### 3.3 BossEngine

```kotlin
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
                        x = currentBoss.position.x + Math.signum(direction) * DASH_SPEED
                    )
                )
                if (Math.abs(currentBoss.position.x - currentBoss.dashTarget!!.x) < DASH_SPEED) {
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

        // 扇形弹幕
        for (i in -2..2) {
            val angle = angleToCharacter + i * 0.2f
            projectiles.add(Projectile(
                position = boss.position.copy(),
                velocity = Vector2(cos(angle) * PROJECTILE_SPEED, sin(angle) * PROJECTILE_SPEED),
                radius = 15f
            ))
        }
        return projectiles
    }

    private fun calculateLaserAngle(bossPos: Vector2, targetPos: Vector2): Float {
        return atan2(targetPos.y - bossPos.y, targetPos.x - bossPos.x)
    }

    fun checkSurvival(boss: Boss, survivalTime: Float): Boolean {
        return survivalTime >= SURVIVAL_TIME
    }
}

data class BossUpdateResult(
    val boss: Boss,
    val projectiles: List<Projectile>,
    val laserHit: Boolean
)
```

### 3.4 CollisionDetector更新

```kotlin
object CollisionDetector {
    fun checkBossCollisions(
        character: Character,
        boss: Boss,
        projectiles: List<Projectile>,
        laserActive: Boolean,
        laserAngle: Float
    ): CollisionResult {
        // 检查弹幕碰撞
        for (projectile in projectiles) {
            if (circleCollision(character.position, CHARACTER_RADIUS, projectile.position, projectile.radius)) {
                return CollisionResult.Hit(projectile.damage)
            }
        }

        // 检查冲刺碰撞
        if (boss.state == BossState.DASHING) {
            if (circleCollision(character.position, CHARACTER_RADIUS, boss.position, BOSS_DASH_RADIUS)) {
                return CollisionResult.Hit(1)
            }
        }

        // 检查激光碰撞
        if (laserActive) {
            if (checkLaserCollision(character, boss.position, laserAngle)) {
                return CollisionResult.Hit(1)
            }
        }

        return CollisionResult.None
    }

    private fun checkLaserCollision(character: Vector2, laserOrigin: Vector2, angle: Float): Boolean {
        // 激光是宽2像素、长1000像素的射线
        // 简化检测：激光角度上距离激光源200像素内
        val laserLength = 1000f
        val laserEnd = Vector2(
            laserOrigin.x + cos(angle) * laserLength,
            laserOrigin.y + sin(angle) * laserLength
        )
        val distance = pointToLineDistance(character, laserOrigin, laserEnd)
        return distance < CHARACTER_RADIUS + 10f
    }

    private fun pointToLineDistance(point: Vector2, lineStart: Vector2, lineEnd: Vector2): Float {
        val A = point.x - lineStart.x
        val B = point.y - lineStart.y
        val C = lineEnd.x - lineStart.x
        val D = lineEnd.y - lineStart.y

        val dot = A * C + B * D
        val lenSq = C * C + D * D
        val param = if (lenSq != 0f) dot / lenSq else -1f

        val xx = if (param < 0) lineStart.x else if (param > 1) lineEnd.x else lineStart.x + param * C
        val yy = if (param < 0) lineStart.y else if (param > 1) lineEnd.y else lineStart.y + param * D

        val dx = point.x - xx
        val dy = point.y - yy
        return sqrt(dx * dx + dy * dy)
    }
}
```

---

## 4. 渲染层改动

### 4.1 GameCanvas更新

```kotlin
@Composable
fun GameCanvas(viewModel: GameViewModel) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // ... 现有渲染逻辑 ...

        // Boss战特殊渲染
        if (level.type == LevelType.BOSS && boss != null) {
            drawBoss(boss, viewportX)
            drawProjectiles(boss.projectiles, viewportX)
            if (boss.laserActive) {
                drawLaser(boss.position, boss.laserAngle, viewportX)
            }
            drawBossHealthBar(boss)
        }

        // 存活时间显示
        if (level.type == LevelType.BOSS) {
            drawSurvivalTimer(level.survivalTime, SURVIVAL_TIME)
        }
    }
}

private fun DrawScope.drawBoss(boss: Boss, viewportX: Float) {
    val screenX = boss.position.x - viewportX
    val screenY = boss.position.y

    // Boss emoji渲染
    drawContext.canvas.nativeCanvas.drawText(
        boss.config.emoji,
        screenX,
        screenY,
        Paint().apply { textSize = 60f; textAlign = Paint.Align.CENTER }
    )

    // 攻击状态特效
    when (boss.state) {
        BossState.DASHING -> {
            // 冲刺轨迹
            drawLine(Color.Red, Offset(screenX - 50, screenY), Offset(screenX, screenY), strokeWidth = 4f)
        }
        BossState.LASERING -> {
            // 激光预警
            drawCircle(Color.Yellow, Offset(screenX, screenY), radius = 30f, alpha = 0.5f)
        }
        else -> {}
    }
}

private fun DrawScope.drawLaser(bossPosition: Vector2, angle: Float, viewportX: Float) {
    val startX = bossPosition.x - viewportX
    val startY = bossPosition.y

    // 激光束
    val endX = startX + cos(angle) * 2000f
    val endY = startY + sin(angle) * 2000f

    drawLine(
        Color.Red,
        Offset(startX, startY),
        Offset(endX, endY),
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )

    // 激光发光效果
    drawLine(
        Color.Yellow.copy(alpha = 0.5f),
        Offset(startX, startY),
        Offset(endX, endY),
        strokeWidth = 16f,
        cap = StrokeCap.Round
    )
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
```

---

## 5. 游戏状态更新

### 5.1 GameState更新

```kotlin
sealed class GameState {
    object Loading : GameState()
    object Countdown : GameState()
    object Playing : GameState()
    object Paused : GameState()
    data class BossActive(val survivalTime: Float) : GameState()  // 新增
    data class Completed(val chapter: Int, val level: Int, val score: Int) : GameState()
    data class Failed(val reason: String) : GameState()
}
```

### 5.2 GameEngine更新

```kotlin
class GameEngine {
    private var survivalTime = 0f
    private const val SURVIVAL_REQUIRED = 30f

    fun update(deltaTime: Float) {
        when (uiState.value.gameState) {
            is GameState.BossActive -> {
                survivalTime += deltaTime

                // 更新Boss
                val bossResult = BossEngine.update(boss!!, character, deltaTime)
                boss = bossResult.boss

                // 检查碰撞
                val collision = CollisionDetector.checkBossCollisions(
                    character,
                    boss,
                    bossResult.projectiles,
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

                // 检查存活时间
                if (BossEngine.checkSurvival(boss, survivalTime)) {
                    updateGameState(GameState.Completed(chapter, levelNumber, calculateScore()))
                }

                updateGameState(GameState.BossActive(survivalTime))
            }
            // ... 其他状态处理 ...
        }
    }
}
```

---

## 6. 常量新增

```kotlin
// Boss相关
const val BOSS_SPAWN_X = 800f
const val BOSS_SPAWN_Y = 300f
const val SURVIVAL_TIME = 30f
const val CHARACTER_RADIUS = 20f
const val BOSS_DASH_RADIUS = 40f
const val PROJECTILE_RADIUS = 15f
```

---

## 7. 实现步骤

### 阶段1：Boss数据层
1. 创建 `BossType` 枚举
2. 创建 `BossConfig` 数据类
3. 创建 `Boss` 模型
4. 创建 `AttackPattern` 枚举
5. 创建 `Projectile` 模型

### 阶段2：Boss引擎
1. 创建 `BossEngine` 对象
2. 实现 `selectAttackPattern`
3. 实现 `fireProjectiles`
4. 实现 `update` 方法
5. 实现 `checkSurvival`

### 阶段3：碰撞检测
1. 扩展 `CollisionDetector`
2. 实现 `checkBossCollisions`
3. 实现 `checkLaserCollision`
4. 实现 `pointToLineDistance`

### 阶段4：关卡生成
1. 更新 `LevelType` 枚举
2. 更新 `Level` 模型
3. 更新 `LevelGenerator` 生成Boss关卡

### 阶段5：渲染
1. 更新 `GameCanvas` 添加Boss渲染
2. 实现 `drawBoss`
3. 实现 `drawProjectiles`
4. 实现 `drawLaser`
5. 实现 `drawSurvivalTimer`

### 阶段6：游戏流程
1. 更新 `GameState` 添加 `BossActive`
2. 更新 `GameEngine` 处理Boss战逻辑
3. 集成测试

---

## 8. 文件修改清单

**新建文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/BossType.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/BossConfig.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Boss.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/AttackPattern.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Projectile.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/BossEngine.kt`

**修改文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/LevelType.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/LevelGenerator.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/CollisionDetector.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt`
- `core/src/commonMain/kotlin/com/gameway/core/Constants.kt`

---

## 9. 验收标准

1. ✅ 第10/20/.../100关为Boss关
2. ✅ Boss有弹幕、冲刺、激光三种攻击模式
3. ✅ 玩家存活30秒即可过关
4. ✅ 被攻击命中扣血（护盾可抵消一次）
5. ✅ Boss有入场动画和状态切换
6. ✅ 存活时间倒计时显示
7. ✅ 不同章节Boss类型不同

---

**文档状态:** 已完成
**下一步:** 用户审核设计文档，然后调用 writing-plans skill 创建实现计划