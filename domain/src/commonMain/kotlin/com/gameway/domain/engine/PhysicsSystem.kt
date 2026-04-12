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