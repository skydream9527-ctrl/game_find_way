package com.gameway.domain.model

import com.gameway.core.GameConstants
import com.gameway.core.util.nextFloat
import kotlin.random.Random

enum class Difficulty {
    EASY, MEDIUM, HARD, EXPERT
}

data class Level(
    val id: Int,
    val chapterId: Int,
    val levelNumber: Int,
    val difficulty: Difficulty,
    val platforms: List<Platform>,
    val powerUps: List<PowerUp>,
    val coins: List<Coin>,
    val targetScore: Int
) {
    companion object {
        fun generate(
            chapterId: Int,
            levelNumber: Int,
            seed: Long = System.currentTimeMillis()
        ): Level {
            val random = Random(seed)
            val difficulty = getDifficultyForLevel(chapterId, levelNumber)
            val platformCount = getPlatformCount(difficulty, random)
            val platforms = generatePlatforms(platformCount, difficulty, random)
            val powerUps = generatePowerUps(platforms, difficulty, random, chapterId, levelNumber)
            val coins = generateCoins(platforms, random)
            
            return Level(
                id = chapterId * 100 + levelNumber,
                chapterId = chapterId,
                levelNumber = levelNumber,
                difficulty = difficulty,
                platforms = platforms,
                powerUps = powerUps,
                coins = coins,
                targetScore = coins.size * GameConstants.COIN_SCORE + powerUps.size * GameConstants.POWERUP_SCORE
            )
        }
        
        private fun getDifficultyForLevel(chapterId: Int, level: Int): Difficulty {
        val baseLevel = (chapterId - 1) * GameConstants.LEVELS_PER_CHAPTER + level
        return when (baseLevel) {
            in 1..10 -> Difficulty.EASY
            in 11..30 -> Difficulty.MEDIUM
            in 31..70 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
    }
        
        private fun getPlatformCount(difficulty: Difficulty, random: Random): Int {
            val (min, max) = when (difficulty) {
                Difficulty.EASY -> GameConstants.MIN_PLATFORMS to 30
                Difficulty.MEDIUM -> 25 to 40
                Difficulty.HARD -> 35 to 45
                Difficulty.EXPERT -> 40 to GameConstants.MAX_PLATFORMS
            }
            return random.nextInt(min, max + 1)
        }
        
        private fun generatePlatforms(count: Int, difficulty: Difficulty, random: Random): List<Platform> {
            val platforms = mutableListOf<Platform>()
            var x = GameConstants.STARTING_POSITION_X
            
            for (i in 0 until count) {
                val maxGap = getMaxGapForDifficulty(difficulty)
                var attempts = 0
                var platform: Platform? = null
                
                while (attempts < 10 && platform == null) {
                    val candidate = generateCandidatePlatform(
                        id = i,
                        startX = x,
                        difficulty = difficulty,
                        random = random,
                        previous = platforms.lastOrNull()
                    )
                    
                    if (!hasOverlap(candidate, platforms) && ensureReachability(candidate, platforms.lastOrNull())) {
                        platform = candidate
                    } else {
                        attempts++
                    }
                }
                
                if (platform == null) {
                    val width = when (difficulty) {
                        Difficulty.EASY -> 100f
                        Difficulty.MEDIUM -> 80f
                        Difficulty.HARD -> 60f
                        Difficulty.EXPERT -> 50f
                    }
                    val previous = platforms.lastOrNull()
                    val safeX = previous?.right?.plus(GameConstants.MIN_HORIZONTAL_GAP) ?: x
                    val safeY = previous?.y ?: GameConstants.STARTING_PLATFORM_Y
                    
                    platform = Platform(
                        id = i,
                        x = safeX,
                        y = safeY.coerceIn(GameConstants.MIN_PLATFORM_Y, GameConstants.MAX_PLATFORM_Y),
                        width = width.coerceAtLeast(60f),
                        type = PlatformType.STATIC,
                        moveRange = 0f,
                        moveSpeed = 0f,
                        moveOffset = 0f
                    )
                }
                
                platforms.add(platform)
                x = platform.right + random.nextFloat(GameConstants.MIN_HORIZONTAL_GAP, maxGap)
            }
            
            return platforms
        }
        
        private fun generateCandidatePlatform(
            id: Int,
            startX: Float,
            difficulty: Difficulty,
            random: Random,
            previous: Platform?
        ): Platform {
            val width = when (difficulty) {
                Difficulty.EASY -> random.nextFloat(80f, 120f)
                Difficulty.MEDIUM -> random.nextFloat(60f, 100f)
                Difficulty.HARD -> random.nextFloat(40f, 80f)
                Difficulty.EXPERT -> random.nextFloat(30f, 60f)
            }
            
            val yVariation = if (previous == null) {
                0f
            } else {
                when (difficulty) {
                    Difficulty.EASY -> random.nextFloat(-60f, 40f)
                    Difficulty.MEDIUM -> random.nextFloat(-80f, 80f)
                    Difficulty.HARD -> random.nextFloat(-100f, GameConstants.MAX_VERTICAL_JUMP_HEIGHT)
                    Difficulty.EXPERT -> random.nextFloat(-120f, GameConstants.MAX_VERTICAL_JUMP_HEIGHT)
                }
            }
            
            val y = GameConstants.STARTING_PLATFORM_Y + yVariation
            
            val type = if (difficulty >= Difficulty.HARD && id > 10) {
                if (random.nextFloat() > GameConstants.MOVING_PLATFORM_PROBABILITY) PlatformType.MOVING_HORIZONTAL
                else PlatformType.STATIC
            } else {
                PlatformType.STATIC
            }
            
            val moveRange = if (type != PlatformType.STATIC) random.nextFloat(50f, 100f) else 0f
            val moveSpeed = if (type != PlatformType.STATIC) random.nextFloat(GameConstants.PLATFORM_MIN_MOVE_SPEED, GameConstants.PLATFORM_MAX_MOVE_SPEED) else 0f
            
            return Platform(
                id = id,
                x = startX,
                y = y.coerceIn(GameConstants.MIN_PLATFORM_Y, GameConstants.MAX_PLATFORM_Y),
                width = width,
                type = type,
                moveRange = moveRange,
                moveSpeed = moveSpeed,
                moveOffset = random.nextFloat(0f, 360f)
            )
        }
        
        private fun hasOverlap(candidate: Platform, existing: List<Platform>): Boolean {
            val verticalOverlapRange = 50f
            
            return existing.any { platform ->
                val horizontalOverlap = candidate.left < platform.right && candidate.right > platform.left
                val verticalClose = kotlin.math.abs(candidate.y - platform.y) < verticalOverlapRange
                horizontalOverlap && verticalClose
            }
        }
        
        private fun getMaxGapForDifficulty(difficulty: Difficulty): Float {
            return when (difficulty) {
                Difficulty.EASY -> GameConstants.MAX_HORIZONTAL_GAP_EASY
                Difficulty.MEDIUM -> GameConstants.MAX_HORIZONTAL_GAP_MEDIUM
                Difficulty.HARD -> GameConstants.MAX_HORIZONTAL_GAP_HARD
                Difficulty.EXPERT -> GameConstants.MAX_HORIZONTAL_GAP_EXPERT
            }
        }
        
        private fun ensureReachability(candidate: Platform, previous: Platform?): Boolean {
            if (previous == null) return true
            
            val horizontalGap = candidate.left - previous.right
            val verticalDiff = previous.y - candidate.y
            
            return horizontalGap <= GameConstants.MAX_HORIZONTAL_JUMP_DISTANCE &&
                   verticalDiff <= GameConstants.MAX_VERTICAL_JUMP_HEIGHT
        }
        
        @Suppress("UNUSED_PARAMETER")
        private fun generatePowerUps(
            platforms: List<Platform>,
            difficulty: Difficulty,
            random: Random,
            chapterId: Int,
            levelNumber: Int
        ): List<PowerUp> {
            val count = random.nextInt(GameConstants.MIN_ITEMS_PER_LEVEL, GameConstants.MAX_ITEMS_PER_LEVEL + 1)
            val isLastLevel = levelNumber == GameConstants.LEVELS_PER_CHAPTER
            
            return List(count) { index ->
                val platform = platforms.random(random)
                val type = if (isLastLevel && index == 0) {
                    if (random.nextFloat() > 0.5f) PowerUpType.GEM else PowerUpType.STAR
                } else {
                    listOf(PowerUpType.FEATHER, PowerUpType.BUTTERFLY, PowerUpType.LIGHTNING, PowerUpType.SHIELD).random(random)
                }
                
                PowerUp(
                    id = index,
                    x = platform.x + platform.width / 2,
                    y = platform.y - GameConstants.POWERUP_Y_OFFSET,
                    type = type,
                    isPermanent = type == PowerUpType.GEM || type == PowerUpType.STAR
                )
            }
        }
        
        private fun generateCoins(platforms: List<Platform>, random: Random): List<Coin> {
            val count = random.nextInt(GameConstants.MIN_COINS_PER_LEVEL, GameConstants.MAX_COINS_PER_LEVEL + 1)
            
            return List(count) { index ->
                val platform = platforms.random(random)
                Coin(
                    id = index,
                    x = platform.x + random.nextFloat(0f, platform.width),
                    y = platform.y - GameConstants.COIN_Y_OFFSET
                )
            }
        }
    }
}