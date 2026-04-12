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
            val difficulty = getDifficultyForLevel(levelNumber)
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
                targetScore = coins.size * 10 + powerUps.size * 20
            )
        }
        
        private fun getDifficultyForLevel(level: Int): Difficulty = when (level) {
            in 1..3 -> Difficulty.EASY
            in 4..6 -> Difficulty.MEDIUM
            in 7..8 -> Difficulty.HARD
            else -> Difficulty.EXPERT
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
            var x = 150f
            val startY = 350f
            
            for (i in 0 until count) {
                val width = when (difficulty) {
                    Difficulty.EASY -> random.nextFloat(80f, 120f)
                    Difficulty.MEDIUM -> random.nextFloat(60f, 100f)
                    Difficulty.HARD -> random.nextFloat(40f, 80f)
                    Difficulty.EXPERT -> random.nextFloat(30f, 60f)
                }
                
                val yVariation = when (difficulty) {
                    Difficulty.EASY -> 30f
                    Difficulty.MEDIUM -> 60f
                    Difficulty.HARD -> 100f
                    Difficulty.EXPERT -> 150f
                }
                
                val y = startY + random.nextFloat(-yVariation, yVariation)
                
                val type = if (difficulty >= Difficulty.HARD && i > count / 2) {
                    if (random.nextFloat() > 0.5f) PlatformType.MOVING_HORIZONTAL
                    else PlatformType.STATIC
                } else {
                    PlatformType.STATIC
                }
                
                val moveRange = if (type != PlatformType.STATIC) random.nextFloat(50f, 100f) else 0f
                val moveSpeed = if (type != PlatformType.STATIC) random.nextFloat(0.5f, 1.5f) else 0f
                
                platforms.add(
                    Platform(
                        id = i,
                        x = x,
                        y = y.coerceIn(200f, 500f),
                        width = width,
                        type = type,
                        moveRange = moveRange,
                        moveSpeed = moveSpeed,
                        moveOffset = random.nextFloat(0f, 360f)
                    )
                )
                
                val gap = when (difficulty) {
                    Difficulty.EASY -> random.nextFloat(80f, 120f)
                    Difficulty.MEDIUM -> random.nextFloat(100f, 160f)
                    Difficulty.HARD -> random.nextFloat(120f, 200f)
                    Difficulty.EXPERT -> random.nextFloat(150f, 250f)
                }
                x += width + gap
            }
            
            return platforms
        }
        
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
                    y = platform.y - 30f,
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
                    y = platform.y - 25f
                )
            }
        }
    }
}