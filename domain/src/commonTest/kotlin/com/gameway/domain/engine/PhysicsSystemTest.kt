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