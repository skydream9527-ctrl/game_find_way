# Platform Jumping Game Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement auto-moving character with fixed jump power and platform reachability guarantee for all levels

**Architecture:** Refactor physics system to fixed jump power, implement reachability algorithm for platform generation, simplify game controls to tap-only interaction

**Tech Stack:** Kotlin Multiplatform, Kotlinx Coroutines, Kotlin Test

---

## File Structure

**Modified files:**
- `core/src/commonMain/kotlin/com/gameway/core/Constants.kt` - Update physics constants
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt` - Simplify character model
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt` - Refactor physics
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt` - Simplify controls
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt` - Rewrite platform generation with reachability
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt` - Remove charge logic
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt` - Simplify touch input

**Created test files:**
- `domain/src/commonTest/kotlin/com/gameway/domain/engine/PhysicsSystemTest.kt`
- `domain/src/commonTest/kotlin/com/gameway/domain/model/PlatformReachabilityTest.kt`
- `domain/src/commonTest/kotlin/com/gameway/domain/model/LevelGenerationTest.kt`

---

### Task 1: Update Physics Constants

**Files:**
- Modify: `core/src/commonMain/kotlin/com/gameway/core/Constants.kt:1-25`

- [ ] **Step 1: Write the constants changes**

Replace the existing constants with fixed jump power values:

```kotlin
package com.gameway.core

object GameConstants {
    const val GRAVITY = 0.6f
    const val JUMP_POWER = 12f
    const val MOVE_SPEED = 5f
    const val MAX_MOVE_SPEED = 6f
    
    const val MAX_HORIZONTAL_JUMP_DISTANCE = 200f
    const val MAX_VERTICAL_JUMP_HEIGHT = 120f
    
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

- [ ] **Step 2: Commit the constants update**

```bash
git add core/src/commonMain/kotlin/com/gameway/core/Constants.kt
git commit -m "refactor: update physics constants for fixed jump power system"
```

---

### Task 2: Simplify Character Model

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt:1-65`

- [ ] **Step 1: Write the simplified Character model**

Remove charge-related properties and update the model:

```kotlin
package com.gameway.domain.model

import com.gameway.core.GameConstants

enum class CharacterState {
    IDLE, RUNNING, JUMPING, FALLING, DEAD
}

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
            velocity = Vector2(GameConstants.MOVE_SPEED, 0f),
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
            GameConstants.MOVE_SPEED * 1.3f else GameConstants.MOVE_SPEED
    
    val effectiveGravity: Float
        get() = if (hasActivePowerUp(PowerUpType.FEATHER))
            GameConstants.GRAVITY * 0.5f else GameConstants.GRAVITY
    
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

- [ ] **Step 2: Commit the Character simplification**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt
git commit -m "refactor: simplify Character model by removing charge logic"
```

---

### Task 3: Refactor PhysicsSystem

**Files:**
- Create: `domain/src/commonTest/kotlin/com/gameway/domain/engine/PhysicsSystemTest.kt`
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt:1-71`

- [ ] **Step 1: Write failing test for fixed jump physics**

Create the test file:

```kotlin
package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.Character
import com.gameway.domain.model.CharacterState
import com.gameway.domain.model.Vector2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhysicsSystemTest {
    
    @Test
    fun testApplyJumpSetsFixedJumpPower() {
        val character = Character.createDefault().copy(isGrounded = true)
        val jumped = PhysicsSystem.applyJump(character)
        
        assertEquals(-GameConstants.JUMP_POWER, jumped.velocity.y)
        assertEquals(GameConstants.MOVE_SPEED, jumped.velocity.x)
        assertFalse(jumped.isGrounded)
        assertEquals(CharacterState.JUMPING, jumped.state)
        assertEquals(1, jumped.jumpCount)
    }
    
    @Test
    fun testUpdateAppliesGravityAndMovement() {
        val character = Character.createDefault().copy(
            velocity = Vector2(GameConstants.MOVE_SPEED, -GameConstants.JUMP_POWER)
        )
        val updated = PhysicsSystem.update(character, 16L, 0f)
        
        assertTrue(updated.velocity.y > -GameConstants.JUMP_POWER)
        assertTrue(updated.position.x > character.position.x)
        assertTrue(updated.position.y < character.position.y)
    }
    
    @Test
    fun testLandOnPlatformSetsGroundedState() {
        val character = Character.createDefault().copy(
            velocity = Vector2(GameConstants.MOVE_SPEED, 5f),
            state = CharacterState.FALLING
        )
        val landed = PhysicsSystem.landOnPlatform(character, 300f)
        
        assertTrue(landed.isGrounded)
        assertEquals(0f, landed.velocity.y)
        assertEquals(CharacterState.RUNNING, landed.state)
        assertEquals(0, landed.jumpCount)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :domain:jvmTest --tests "com.gameway.domain.engine.PhysicsSystemTest" 2>&1
```

Expected: Compilation error or test failures (PhysicsSystem.applyJump doesn't exist yet)

- [ ] **Step 3: Implement simplified PhysicsSystem**

Replace the entire PhysicsSystem:

```kotlin
package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.Character
import com.gameway.domain.model.CharacterState
import com.gameway.domain.model.Vector2

object PhysicsSystem {
    
    fun update(character: Character, deltaTime: Long, currentScrollX: Float): Character {
        val dt = deltaTime / 16f
        
        val gravity = character.effectiveGravity
        val newVelocityY = character.velocity.y + gravity * dt
        val moveSpeed = character.effectiveMoveSpeed
        
        val newX = character.position.x + moveSpeed * dt
        val newY = character.position.y + character.velocity.y * dt
        
        return character.copy(
            position = Vector2(newX, newY),
            velocity = Vector2(moveSpeed, newVelocityY),
            state = determineState(character, newVelocityY),
            isGrounded = false
        )
    }
    
    private fun determineState(character: Character, velocityY: Float): CharacterState {
        return when {
            character.isGrounded -> CharacterState.RUNNING
            velocityY < 0 -> CharacterState.JUMPING
            velocityY > 0 -> CharacterState.FALLING
            else -> character.state
        }
    }
    
    fun applyJump(character: Character): Character {
        return character.copy(
            velocity = Vector2(character.effectiveMoveSpeed, -GameConstants.JUMP_POWER),
            isGrounded = false,
            jumpCount = character.jumpCount + 1,
            state = CharacterState.JUMPING
        )
    }
    
    fun landOnPlatform(character: Character, platformY: Float): Character {
        return character.copy(
            position = Vector2(character.position.x, platformY),
            velocity = Vector2(character.effectiveMoveSpeed, 0f),
            isGrounded = true,
            jumpCount = 0,
            state = CharacterState.RUNNING
        )
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :domain:jvmTest --tests "com.gameway.domain.engine.PhysicsSystemTest" 2>&1
```

Expected: All tests pass

- [ ] **Step 5: Commit PhysicsSystem changes**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt
git add domain/src/commonTest/kotlin/com/gameway/domain/engine/PhysicsSystemTest.kt
git commit -m "refactor: implement fixed jump power physics system with tests"
```

---

### Task 4: Simplify GameEngine

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt:1-160`

- [ ] **Step 1: Update GameEngine to use simplified physics**

Replace GameEngine with simplified version:

```kotlin
package com.gameway.domain.engine

import com.gameway.domain.model.ActivePowerUp
import com.gameway.domain.model.Character
import com.gameway.domain.model.GameState
import com.gameway.domain.model.Level
import com.gameway.domain.model.PlatformType
import com.gameway.domain.model.PowerUpType

class GameEngine {
    
    private var gameState: GameState = GameState.Loading
    private var character: Character = Character.createDefault()
    private var level: Level? = null
    private var scrollX: Float = 0f
    private var score: Int = 0
    private var lastUpdateTime: Long = 0L
    private var countdownEnd: Long = 0L
    
    private var collectedCoins: MutableSet<Int> = mutableSetOf()
    private var collectedPowerUps: MutableSet<Int> = mutableSetOf()
    
    fun startLevel(newLevel: Level) {
        level = newLevel
        character = Character.createDefault()
        scrollX = 0f
        score = 0
        collectedCoins = mutableSetOf()
        collectedPowerUps = mutableSetOf()
        gameState = GameState.Countdown
        countdownEnd = System.currentTimeMillis() + 2000L
        lastUpdateTime = System.currentTimeMillis()
    }
    
    fun update(): GameState {
        if (gameState is GameState.Loading || gameState is GameState.Completed || gameState is GameState.Failed) {
            return gameState
        }
        
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastUpdateTime
        lastUpdateTime = currentTime
        
        when (gameState) {
            is GameState.Countdown -> {
                if (currentTime >= countdownEnd) {
                    gameState = GameState.Playing
                }
            }
            is GameState.Playing -> {
                updateGame(deltaTime, currentTime)
            }
            else -> {}
        }
        
        return gameState
    }
    
    private fun updateGame(deltaTime: Long, currentTime: Long) {
        val currentLevel = level ?: return
        
        character = PhysicsSystem.update(character, deltaTime, scrollX)
        scrollX = character.position.x - 100f
        
        val updatedPlatforms = currentLevel.platforms.map { platform ->
            if (platform.type != PlatformType.STATIC) {
                val offset = kotlin.math.sin((currentTime / 1000f) * platform.moveSpeed + platform.moveOffset) * platform.moveRange
                platform.copy(x = platform.x + offset * 0.01f)
            } else {
                platform
            }
        }
        
        val collisions = CollisionDetector.checkCollisions(character, updatedPlatforms, currentLevel.coins, currentLevel.powerUps)
        
        for (collision in collisions) {
            when (collision) {
                is CollisionResult.LandedOnPlatform -> {
                    character = PhysicsSystem.landOnPlatform(character, collision.platform.y)
                }
                is CollisionResult.HitSideOfPlatform -> {
                    gameState = if (character.hasShield) {
                        character = character.copy(activePowerUps = character.activePowerUps.filter { it.type != PowerUpType.SHIELD })
                        GameState.Playing
                    } else {
                        GameState.Failed("撞到平台侧面")
                    }
                }
                is CollisionResult.CollectedCoin -> {
                    if (!collectedCoins.contains(collision.coin.id)) {
                        collectedCoins.add(collision.coin.id)
                        score += 10
                        character = character.copy(coinsCollected = character.coinsCollected + 1)
                    }
                }
                is CollisionResult.CollectedPowerUp -> {
                    if (!collectedPowerUps.contains(collision.powerUp.id)) {
                        collectedPowerUps.add(collision.powerUp.id)
                        val powerUp = collision.powerUp
                        character = character.copy(
                            activePowerUps = character.activePowerUps + ActivePowerUp.create(powerUp.type),
                            maxJumps = if (powerUp.type == PowerUpType.GEM) 2 else character.maxJumps
                        )
                        score += 20
                    }
                }
                is CollisionResult.FellOffScreen -> {
                    gameState = if (character.hasShield) {
                        character = character.copy(activePowerUps = character.activePowerUps.filter { it.type != PowerUpType.SHIELD })
                        GameState.Playing
                    } else {
                        GameState.Failed("掉落屏幕")
                    }
                }
                CollisionResult.NoCollision -> {}
            }
        }
        
        val lastPlatform = updatedPlatforms.lastOrNull()
        if (lastPlatform != null && character.position.x > lastPlatform.x + lastPlatform.width) {
            gameState = GameState.Completed(score, character.coinsCollected)
        }
    }
    
    fun jump() {
        if (gameState !is GameState.Playing) return
        
        if (character.isGrounded || character.jumpCount < character.maxJumps || character.hasDoubleJump) {
            val allowedJumps = if (character.hasDoubleJump) character.maxJumps else 1
            if (character.jumpCount < allowedJumps) {
                character = PhysicsSystem.applyJump(character)
            }
        }
    }
    
    fun pause() {
        if (gameState is GameState.Playing) {
            gameState = GameState.Paused
        }
    }
    
    fun resume() {
        if (gameState is GameState.Paused) {
            gameState = GameState.Playing
            lastUpdateTime = System.currentTimeMillis()
        }
    }
    
    fun restart() {
        level?.let { startLevel(it) }
    }
    
    fun getCharacter(): Character = character
    fun getGameState(): GameState = gameState
    fun getScrollX(): Float = scrollX
    fun getScore(): Int = score
    fun getLevel(): Level? = level
}
```

- [ ] **Step 2: Commit GameEngine changes**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt
git commit -m "refactor: simplify GameEngine by removing charge mechanics"
```

---

### Task 5: Implement Platform Reachability Algorithm

**Files:**
- Create: `domain/src/commonTest/kotlin/com/gameway/domain/model/PlatformReachabilityTest.kt`

- [ ] **Step 1: Write failing test for reachability**

Create the test file:

```kotlin
package com.gameway.domain.model

import com.gameway.core.GameConstants
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformReachabilityTest {
    
    @Test
    fun testHorizontalReachabilityWithinRange() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 150f, y = 350f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testHorizontalUnreachableTooFar() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 250f, y = 350f, width = 80f)
        
        assertFalse(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testVerticalReachabilityGoingDown() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 100f, y = 450f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testVerticalReachabilityGoingUpWithinLimit() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 100f, y = 350f - 100f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testVerticalUnreachableTooHigh() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 100f, y = 350f - 150f, width = 80f)
        
        assertFalse(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testCombinedReachability() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 180f, y = 350f - 110f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :domain:jvmTest --tests "com.gameway.domain.model.PlatformReachabilityTest" 2>&1
```

Expected: Compilation error (Platform.isReachable doesn't exist)

- [ ] **Step 3: Add reachability method to Platform**

Modify Platform.kt:

```kotlin
package com.gameway.domain.model

import com.gameway.core.GameConstants

enum class PlatformType {
    STATIC, MOVING_HORIZONTAL, MOVING_VERTICAL
}

data class Platform(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float = 12f,
    val type: PlatformType = PlatformType.STATIC,
    val moveRange: Float = 0f,
    val moveSpeed: Float = 0f,
    val moveOffset: Float = 0f
) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height
    
    companion object {
        fun isReachable(from: Platform, to: Platform): Boolean {
            val horizontalGap = to.left - from.right
            val verticalDiff = from.y - to.y
            
            val horizontalReachable = horizontalGap <= GameConstants.MAX_HORIZONTAL_JUMP_DISTANCE
            val verticalReachable = verticalDiff <= GameConstants.MAX_VERTICAL_JUMP_HEIGHT
            
            return horizontalReachable && verticalReachable
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :domain:jvmTest --tests "com.gameway.domain.model.PlatformReachabilityTest" 2>&1
```

Expected: All tests pass

- [ ] **Step 5: Commit reachability implementation**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Platform.kt
git add domain/src/commonTest/kotlin/com/gameway/domain/model/PlatformReachabilityTest.kt
git commit -m "feat: implement platform reachability algorithm with tests"
```

---

### Task 6: Rewrite Platform Generation Logic

**Files:**
- Create: `domain/src/commonTest/kotlin/com/gameway/domain/model/LevelGenerationTest.kt`
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt:63-118`

- [ ] **Step 1: Write failing test for reachable level generation**

Create the test file:

```kotlin
package com.gameway.domain.model

import kotlin.test.Test
import kotlin.test.assertTrue

class LevelGenerationTest {
    
    @Test
    fun testAllPlatformsReachableInEasyLevel() {
        val level = Level.generate(chapterId = 1, levelNumber = 1, seed = 12345L)
        
        for (i in 0 until level.platforms.size - 1) {
            val current = level.platforms[i]
            val next = level.platforms[i + 1]
            assertTrue(
                Platform.isReachable(current, next),
                "Platform ${i + 1} should be reachable from platform $i in level ${level.levelNumber}"
            )
        }
    }
    
    @Test
    fun testAllPlatformsReachableInMediumLevel() {
        val level = Level.generate(chapterId = 1, levelNumber = 5, seed = 23456L)
        
        for (i in 0 until level.platforms.size - 1) {
            val current = level.platforms[i]
            val next = level.platforms[i + 1]
            assertTrue(
                Platform.isReachable(current, next),
                "Platform ${i + 1} should be reachable from platform $i"
            )
        }
    }
    
    @Test
    fun testAllPlatformsReachableInHardLevel() {
        val level = Level.generate(chapterId = 1, levelNumber = 7, seed = 34567L)
        
        for (i in 0 until level.platforms.size - 1) {
            val current = level.platforms[i]
            val next = level.platforms[i + 1]
            assertTrue(
                Platform.isReachable(current, next),
                "Platform ${i + 1} should be reachable from platform $i"
            )
        }
    }
    
    @Test
    fun testAllPlatformsReachableInExpertLevel() {
        val level = Level.generate(chapterId = 1, levelNumber = 10, seed = 45678L)
        
        for (i in 0 until level.platforms.size - 1) {
            val current = level.platforms[i]
            val next = level.platforms[i + 1]
            assertTrue(
                Platform.isReachable(current, next),
                "Platform ${i + 1} should be reachable from platform $i"
            )
        }
    }
    
    @Test
    fun testMultipleSeedsGenerateReachableLevels() {
        for (seed in 10000L..10010L) {
            val level = Level.generate(chapterId = 1, levelNumber = 3, seed = seed)
            
            for (i in 0 until level.platforms.size - 1) {
                val current = level.platforms[i]
                val next = level.platforms[i + 1]
                assertTrue(
                    Platform.isReachable(current, next),
                    "Platform ${i + 1} unreachable for seed $seed"
                )
            }
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :domain:jvmTest --tests "com.gameway.domain.model.LevelGenerationTest" 2>&1
```

Expected: Tests fail - current generation doesn't ensure reachability

- [ ] **Step 3: Rewrite generatePlatforms method**

Replace the generatePlatforms method in Level.kt:

```kotlin
private fun generatePlatforms(count: Int, difficulty: Difficulty, random: Random): List<Platform> {
    val platforms = mutableListOf<Platform>()
    
    val startX = 150f
    val startY = 350f
    
    platforms.add(
        Platform(
            id = 0,
            x = startX,
            y = startY,
            width = 100f,
            type = PlatformType.STATIC
        )
    )
    
    for (i in 1 until count) {
        val lastPlatform = platforms.last()
        
        val minHorizontalGap = 80f
        val maxHorizontalGap = when (difficulty) {
            Difficulty.EASY -> 120f
            Difficulty.MEDIUM -> 160f
            Difficulty.HARD -> 180f
            Difficulty.EXPERT -> 200f
        }
        
        val horizontalGap = random.nextFloat(minHorizontalGap, maxHorizontalGap)
        val newX = lastPlatform.right + horizontalGap
        
        val yVariation = when (difficulty) {
            Difficulty.EASY -> random.nextFloat(-60f, 40f)
            Difficulty.MEDIUM -> random.nextFloat(-80f, 80f)
            Difficulty.HARD -> random.nextFloat(-100f, GameConstants.MAX_VERTICAL_JUMP_HEIGHT)
            Difficulty.EXPERT -> random.nextFloat(-120f, GameConstants.MAX_VERTICAL_JUMP_HEIGHT)
        }
        
        val newY = (lastPlatform.y + yVariation).coerceIn(200f, 500f)
        
        val width = when (difficulty) {
            Difficulty.EASY -> random.nextFloat(80f, 120f)
            Difficulty.MEDIUM -> random.nextFloat(60f, 100f)
            Difficulty.HARD -> random.nextFloat(40f, 80f)
            Difficulty.EXPERT -> random.nextFloat(30f, 60f)
        }
        
        val type = if (difficulty >= Difficulty.HARD && i > count / 2) {
            if (random.nextFloat() > 0.7f) PlatformType.MOVING_HORIZONTAL
            else PlatformType.STATIC
        } else {
            PlatformType.STATIC
        }
        
        val moveRange = if (type != PlatformType.STATIC) random.nextFloat(50f, 100f) else 0f
        val moveSpeed = if (type != PlatformType.STATIC) random.nextFloat(0.5f, 1.5f) else 0f
        
        val candidatePlatform = Platform(
            id = i,
            x = newX,
            y = newY,
            width = width,
            type = type,
            moveRange = moveRange,
            moveSpeed = moveSpeed,
            moveOffset = random.nextFloat(0f, 360f)
        )
        
        if (Platform.isReachable(lastPlatform, candidatePlatform)) {
            platforms.add(candidatePlatform)
        } else {
            val fallbackY = lastPlatform.y.coerceIn(200f, 500f)
            platforms.add(
                candidatePlatform.copy(
                    y = fallbackY,
                    width = width.coerceAtLeast(60f)
                )
            )
        }
    }
    
    return platforms
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :domain:jvmTest --tests "com.gameway.domain.model.LevelGenerationTest" 2>&1
```

Expected: All tests pass - all platforms reachable

- [ ] **Step 5: Commit platform generation rewrite**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt
git add domain/src/commonTest/kotlin/com/gameway/domain/model/LevelGenerationTest.kt
git commit -m "feat: rewrite platform generation with reachability guarantee"
```

---

### Task 7: Update GameViewModel

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt:1-93`

- [ ] **Step 1: Simplify GameViewModel - remove charge logic**

Replace GameViewModel:

```kotlin
package com.gameway.presentation.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.engine.GameEngine
import com.gameway.domain.model.Character
import com.gameway.domain.model.GameState
import com.gameway.domain.usecase.GetLevelUseCase
import com.gameway.domain.usecase.SaveProgressUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val character: Character = Character.createDefault(),
    val gameState: GameState = GameState.Loading,
    val scrollX: Float = 0f,
    val score: Int = 0,
    val chapterId: Int = 1,
    val levelNumber: Int = 1
)

class GameViewModel(
    private val getLevelUseCase: GetLevelUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val gameEngine: GameEngine = GameEngine()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    private var gameLoopActive = false
    
    fun loadLevel(chapterId: Int, levelNumber: Int) {
        viewModelScope.launch {
            val level = getLevelUseCase(chapterId, levelNumber)
            gameEngine.startLevel(level)
            _uiState.value = _uiState.value.copy(chapterId = chapterId, levelNumber = levelNumber, gameState = GameState.Countdown)
            startGameLoop()
        }
    }
    
    private fun startGameLoop() {
        if (gameLoopActive) return
        gameLoopActive = true
        
        viewModelScope.launch {
            while (gameLoopActive) {
                val state = gameEngine.getGameState()
                _uiState.value = _uiState.value.copy(
                    character = gameEngine.getCharacter(),
                    gameState = state,
                    scrollX = gameEngine.getScrollX(),
                    score = gameEngine.getScore()
                )
                
                if (state is GameState.Completed || state is GameState.Failed) {
                    gameLoopActive = false
                    break
                }
                
                delay(16L)
            }
        }
    }
    
    fun jump() {
        gameEngine.jump()
    }
    
    fun restart() {
        gameEngine.restart()
        _uiState.value = _uiState.value.copy(gameState = GameState.Countdown)
        gameLoopActive = false
        startGameLoop()
    }
    
    fun completeLevel() {
        viewModelScope.launch {
            val state = _uiState.value
            saveProgressUseCase(com.gameway.domain.model.GameProgress(currentChapter = state.chapterId, completedLevels = mapOf(state.chapterId to listOf(state.levelNumber))))
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        gameLoopActive = false
    }
}
```

- [ ] **Step 2: Commit ViewModel changes**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt
git commit -m "refactor: simplify GameViewModel by removing charge UI logic"
```

---

### Task 8: Update GameCanvas Touch Input

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt:32-35`

- [ ] **Step 1: Simplify touch input in GameCanvas**

Update the pointerInput block in GameCanvas.kt (around line 32-35):

```kotlin
modifier = Modifier
    .fillMaxSize()
    .pointerInput(Unit) {
        detectTapGestures(
            onTap = { viewModel.jump() }
        )
    }
```

Remove the existing onTouchDown and onTouchUp handlers if present.

- [ ] **Step 2: Commit GameCanvas changes**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt
git commit -m "refactor: simplify GameCanvas touch input to tap-only"
```

---

### Task 9: Integration Test - Build and Verify

**Files:**
- All modified files

- [ ] **Step 1: Run full build to verify compilation**

```bash
./gradlew clean build --no-daemon 2>&1 | tail -50
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run all domain tests**

```bash
./gradlew :domain:jvmTest 2>&1 | tail -30
```

Expected: All tests pass

- [ ] **Step 3: Commit integration verification**

```bash
git add -A
git commit -m "test: verify full integration after physics system refactor"
```

---

### Task 10: Push Changes and Manual Testing Instructions

- [ ] **Step 1: Push all changes to remote**

```bash
git push
```

- [ ] **Step 2: Document manual testing steps**

After implementation, manually test:

1. Start any level (chapter 1, level 1)
2. Verify character moves forward automatically
3. Tap screen to jump, verify fixed jump height
4. Test double jump (if butterfly power-up collected)
5. Complete the level to verify all platforms are reachable
6. Try multiple levels (easy, medium, hard, expert) to verify difficulty scaling

---

## Verification Checklist

After implementation, verify:

- [ ] Character moves forward automatically at constant speed
- [ ] Tap jump uses fixed jump power (12 units)
- [ ] All generated platforms in all difficulty levels are reachable
- [ ] Double jump works with butterfly power-up
- [ ] Lightning power-up increases speed 1.3x
- [ ] Feather power-up reduces gravity 0.5x
- [ ] Shield power-up protects from collision
- [ ] Level completion detection works correctly
- [ ] No compilation errors or test failures

---

## Self-Review Notes

**Spec coverage check:**
- Task 1: Constants update ✓
- Task 2: Character simplification ✓
- Task 3: PhysicsSystem refactor ✓
- Task 4: GameEngine simplification ✓
- Task 5: Reachability algorithm ✓
- Task 6: Platform generation rewrite ✓
- Task 7: ViewModel update ✓
- Task 8: GameCanvas touch input ✓
- Tasks 9-10: Integration and testing ✓

**Placeholder scan:** No placeholders found. All steps have complete code.

**Type consistency:** Verified all method signatures match across tasks.