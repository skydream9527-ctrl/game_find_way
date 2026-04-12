package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.Character
import com.gameway.domain.model.CharacterState
import com.gameway.domain.model.Vector2

object PhysicsSystem {
    
    @Suppress("UNUSED_PARAMETER")
    fun update(character: Character, deltaTime: Long, currentScrollX: Float): Character {
        val dt = deltaTime / 16f
        
        val gravity = if (character.velocity.y > 0) GameConstants.GRAVITY * 1.2f
        else GameConstants.GRAVITY
        
        val newVelocityY = character.velocity.y + gravity * dt
        val moveSpeed = character.effectiveMoveSpeed
        
        return character.copy(
            position = Vector2(character.position.x, character.position.y + character.velocity.y * dt),
            velocity = Vector2(moveSpeed * 0.1f, newVelocityY),
            state = determineState(character, newVelocityY),
            isGrounded = character.isGrounded
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
    
    fun applyJump(character: Character, jumpPower: Float): Character {
        return character.copy(
            velocity = Vector2(character.velocity.x, -jumpPower),
            isGrounded = false,
            jumpCount = character.jumpCount + 1,
            state = CharacterState.JUMPING,
            chargeTime = 0L,
            isCharging = false
        )
    }
    
    fun startCharging(character: Character): Character {
        if (character.isGrounded || character.hasDoubleJump) {
            return character.copy(isCharging = true, chargeTime = 0L)
        }
        return character
    }
    
    fun updateCharge(character: Character, deltaTime: Long): Character {
        if (!character.isCharging) return character
        
        val newChargeTime = (character.chargeTime + deltaTime).coerceAtMost(GameConstants.MAX_CHARGE_TIME)
        return character.copy(chargeTime = newChargeTime)
    }
    
    fun landOnPlatform(character: Character, platformY: Float): Character {
        return character.copy(
            position = Vector2(character.position.x, platformY),
            velocity = Vector2(character.velocity.x, 0f),
            isGrounded = true,
            jumpCount = 0,
            state = CharacterState.RUNNING
        )
    }
}