package com.gameway.domain.engine

import com.gameway.core.GameConstants
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
    
    fun startLevel(newLevel: Level) {
        level = newLevel
        character = Character.createDefault().copy(position = character.position.copy(x = 50f))
        scrollX = 0f
        score = 0
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
                    score += 10
                    character = character.copy(coinsCollected = character.coinsCollected + 1)
                }
                is CollisionResult.CollectedPowerUp -> {
                    val powerUp = collision.powerUp
                    character = character.copy(
                        activePowerUps = character.activePowerUps + ActivePowerUp.create(powerUp.type),
                        maxJumps = if (powerUp.type == PowerUpType.GEM) 2 else character.maxJumps
                    )
                    score += 20
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
        
        if (character.isGrounded || character.jumpCount < character.maxJumps) {
            character = PhysicsSystem.applyJump(character)
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
}