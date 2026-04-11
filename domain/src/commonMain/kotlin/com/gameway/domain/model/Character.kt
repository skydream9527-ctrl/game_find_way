package com.gameway.domain.model

import com.gameway.core.GameConstants
import com.gameway.core.util.clamp

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
    val chargeTime: Long,
    val isCharging: Boolean,
    val activePowerUps: List<ActivePowerUp>,
    val health: Int,
    val coinsCollected: Int
) {
    companion object {
        fun createDefault(): Character = Character(
            position = Vector2(50f, 300f),
            velocity = Vector2(0f, 0f),
            state = CharacterState.IDLE,
            isGrounded = false,
            jumpCount = 0,
            maxJumps = 1,
            chargeTime = 0L,
            isCharging = false,
            activePowerUps = emptyList(),
            health = 3,
            coinsCollected = 0
        )
    }
    
    val effectiveJumpPower: Float
        get() {
            val basePower = GameConstants.MIN_JUMP_POWER + 
                (GameConstants.MAX_JUMP_POWER - GameConstants.MIN_JUMP_POWER) * 
                (chargeTime.toFloat() / GameConstants.MAX_CHARGE_TIME)
            
            val multiplier = if (hasActivePowerUp(PowerUpType.FEATHER)) 1.5f else 1f
            return (basePower * multiplier).clamp(GameConstants.MIN_JUMP_POWER, GameConstants.MAX_JUMP_POWER * 1.5f)
        }
    
    val effectiveMoveSpeed: Float
        get() {
            val multiplier = if (hasActivePowerUp(PowerUpType.LIGHTNING)) 1.3f else 1f
            return (GameConstants.MOVE_SPEED * multiplier).clamp(GameConstants.MOVE_SPEED, GameConstants.MAX_MOVE_SPEED * 1.3f)
        }
    
    val hasDoubleJump: Boolean
        get() = maxJumps >= 2 || hasActivePowerUp(PowerUpType.BUTTERFLY)
    
    val hasShield: Boolean
        get() = hasActivePowerUp(PowerUpType.SHIELD)
    
    fun hasActivePowerUp(type: PowerUpType): Boolean {
        val currentTime = System.currentTimeMillis()
        return activePowerUps.any { it.type == type && it.expiresAt > currentTime }
    }
}