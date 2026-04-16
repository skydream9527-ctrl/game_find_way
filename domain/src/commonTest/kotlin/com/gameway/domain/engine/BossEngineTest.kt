package com.gameway.domain.engine

import com.gameway.domain.model.*
import kotlin.test.*
import kotlin.math.*

class BossEngineTest {
    @Test
    fun `test selectAttackPattern returns valid pattern for SLIME`() {
        val boss = Boss.create(chapter = 1)
        val pattern = BossEngine.selectAttackPattern(boss)
        assertTrue(pattern in listOf(AttackPattern.PROJECTILE))
    }

    @Test
    fun `test checkSurvival returns true after 30 seconds`() {
        assertTrue(BossEngine.checkSurvival(30f))
        assertFalse(BossEngine.checkSurvival(29f))
    }

    @Test
    fun `test fireProjectiles creates 5 projectiles`() {
        val boss = Boss.create(chapter = 1)
        val character = Character.createDefault()
        val projectiles = BossEngine.fireProjectiles(boss, character)
        assertEquals(5, projectiles.size)
    }

    @Test
    fun `test boss update transitions from APPEARING to IDLE`() {
        val boss = Boss.create(chapter = 1).copy(attackTimer = 0f)
        val character = Character.createDefault()
        val result = BossEngine.update(boss, character, deltaTime = 0.1f)
        assertEquals(BossState.IDLE, result.boss.state)
    }
}