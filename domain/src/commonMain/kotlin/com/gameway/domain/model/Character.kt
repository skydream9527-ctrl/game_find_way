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