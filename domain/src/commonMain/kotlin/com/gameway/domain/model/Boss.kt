package com.gameway.domain.model

data class Boss(
    val id: Int,
    val config: BossConfig,
    val position: Vector2,
    val velocity: Vector2,
    val state: BossState,
    val currentAttackPattern: AttackPattern?,
    val attackTimer: Float,
    val isActive: Boolean,
    val dashTarget: Vector2?,
    val laserActive: Boolean,
    val laserAngle: Float,
    val phase: Int
) {
    companion object {
        fun create(chapter: Int): Boss {
            val config = BossConfig.getForChapter(chapter)
            return Boss(
                id = chapter,
                config = config,
                position = Vector2(GameConstants.BOSS_SPAWN_X, GameConstants.BOSS_SPAWN_Y),
                velocity = Vector2.ZERO,
                state = BossState.APPEARING,
                currentAttackPattern = null,
                attackTimer = 2f,
                isActive = true,
                dashTarget = null,
                laserActive = false,
                laserAngle = 0f,
                phase = 1
            )
        }
    }
}

enum class BossState {
    APPEARING,
    IDLE,
    ATTACKING,
    DASHING,
    LASERING,
    DEFEATED
}