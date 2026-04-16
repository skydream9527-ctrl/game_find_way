package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.*
import kotlin.math.*

object BossEngine {
    private const val PROJECTILE_SPEED = 6f
    private const val PROJECTILE_INTERVAL = 1.5f
    private const val LASER_DURATION = 2f
    private const val DASH_SPEED = 12f
    private const val SURVIVAL_TIME = 30f

    fun update(boss: Boss, character: Character, deltaTime: Float): BossUpdateResult {
        if (!boss.isActive) return BossUpdateResult(boss, emptyList(), false)

        var currentBoss = boss
        val projectiles = mutableListOf<Projectile>()
        var laserHit = false

        currentBoss = currentBoss.copy(
            attackTimer = currentBoss.attackTimer - deltaTime
        )

        when (currentBoss.state) {
            BossState.APPEARING -> {
                if (currentBoss.attackTimer <= 0) {
                    currentBoss = currentBoss.copy(state = BossState.IDLE, attackTimer = 1f)
                }
            }
            BossState.IDLE -> {
                if (currentBoss.attackTimer <= 0) {
                    val pattern = selectAttackPattern(currentBoss)
                    currentBoss = when (pattern) {
                        AttackPattern.PROJECTILE -> {
                            projectiles.addAll(fireProjectiles(currentBoss, character))
                            currentBoss.copy(state = BossState.ATTACKING, attackTimer = PROJECTILE_INTERVAL, currentAttackPattern = pattern)
                        }
                        AttackPattern.DASH -> {
                            currentBoss.copy(
                                state = BossState.DASHING,
                                dashTarget = Vector2(character.position.x, currentBoss.position.y),
                                attackTimer = 0.5f,
                                currentAttackPattern = pattern
                            )
                        }
                        AttackPattern.LASER -> {
                            currentBoss.copy(
                                state = BossState.LASERING,
                                laserActive = true,
                                laserAngle = calculateLaserAngle(currentBoss.position, character.position),
                                attackTimer = LASER_DURATION,
                                currentAttackPattern = pattern
                            )
                        }
                    }
                }
            }
            BossState.ATTACKING -> {
                if (currentBoss.attackTimer <= 0) {
                    currentBoss = currentBoss.copy(state = BossState.IDLE, attackTimer = 1f)
                }
            }
            BossState.DASHING -> {
                val direction = currentBoss.dashTarget!!.x - currentBoss.position.x
                currentBoss = currentBoss.copy(
                    position = currentBoss.position.copy(
                        x = currentBoss.position.x + sign(direction) * DASH_SPEED
                    )
                )
                if (abs(currentBoss.position.x - currentBoss.dashTarget!!.x) < DASH_SPEED) {
                    currentBoss = currentBoss.copy(state = BossState.IDLE, attackTimer = 1.5f, dashTarget = null)
                }
            }
            BossState.LASERING -> {
                if (currentBoss.attackTimer <= 0) {
                    currentBoss = currentBoss.copy(state = BossState.IDLE, laserActive = false, attackTimer = 1f)
                }
            }
            BossState.DEFEATED -> {}
        }

        return BossUpdateResult(currentBoss, projectiles, laserHit)
    }

    private fun selectAttackPattern(boss: Boss): AttackPattern {
        val availablePatterns = boss.config.attackPatterns
        return availablePatterns.random()
    }

    private fun fireProjectiles(boss: Boss, character: Character): List<Projectile> {
        val projectiles = mutableListOf<Projectile>()
        val angleToCharacter = atan2(
            character.position.y - boss.position.y,
            character.position.x - boss.position.x
        )

        for (i in -2..2) {
            val angle = angleToCharacter + i * 0.2f
            projectiles.add(Projectile(
                position = boss.position.copy(),
                velocity = Vector2(cos(angle) * PROJECTILE_SPEED, sin(angle) * PROJECTILE_SPEED),
                radius = GameConstants.PROJECTILE_RADIUS
            ))
        }
        return projectiles
    }

    private fun calculateLaserAngle(bossPos: Vector2, targetPos: Vector2): Float {
        return atan2(targetPos.y - bossPos.y, targetPos.x - bossPos.x)
    }

    fun checkSurvival(survivalTime: Float): Boolean {
        return survivalTime >= SURVIVAL_TIME
    }

    fun getSurvivalTimeRequired(): Float = SURVIVAL_TIME
}

data class BossUpdateResult(
    val boss: Boss,
    val projectiles: List<Projectile>,
    val laserHit: Boolean
)
