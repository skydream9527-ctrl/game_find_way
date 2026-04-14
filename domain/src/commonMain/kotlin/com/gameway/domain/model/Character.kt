package com.gameway.domain.model

import com.gameway.core.GameConstants

enum class CharacterState {
    IDLE, RUNNING, JUMPING, FALLING, DEAD
}

data class Character(
    val position: Vector2,
    val velocity: Vector2,
    val state: CharacterState,
    val type: CharacterType,
    val config: CharacterConfig,
    val isGrounded: Boolean,
    val jumpCount: Int,
    val maxJumps: Int,
    val activePowerUps: List<ActivePowerUp>,
    val health: Int,
    val coinsCollected: Int
) {
    companion object {
        fun createDefault(type: CharacterType = CharacterType.CAT): Character {
            val config = CharacterConfig.getByType(type)
            return Character(
                position = Vector2(GameConstants.STARTING_POSITION_X, GameConstants.STARTING_PLATFORM_Y - 5f),
                velocity = Vector2(config.moveSpeedMultiplier * GameConstants.MOVE_SPEED, 0f),
                state = CharacterState.IDLE,
                type = type,
                config = config,
                isGrounded = false,
                jumpCount = 0,
                maxJumps = 1,
                activePowerUps = emptyList(),
                health = 3,
                coinsCollected = 0
            )
        }
    }
    
    val effectiveMoveSpeed: Float
        get() = GameConstants.MOVE_SPEED * config.moveSpeedMultiplier *
                (if (hasActivePowerUp(PowerUpType.LIGHTNING)) 1.3f else 1.0f)
    
    val effectiveJumpPower: Float
        get() = GameConstants.JUMP_POWER * config.jumpPowerMultiplier
    
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