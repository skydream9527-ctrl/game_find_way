package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.Boss
import com.gameway.domain.model.BossState
import com.gameway.domain.model.Character
import com.gameway.domain.model.Coin
import com.gameway.domain.model.Platform
import com.gameway.domain.model.PowerUp
import com.gameway.domain.model.Projectile
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

sealed class CollisionResult {
    data object None : CollisionResult()
    data class LandedOnPlatform(val platform: Platform) : CollisionResult()
    data class HitSideOfPlatform(val platform: Platform) : CollisionResult()
    data class CollectedCoin(val coin: Coin) : CollisionResult()
    data class CollectedPowerUp(val powerUp: PowerUp) : CollisionResult()
    data object FellOffScreen : CollisionResult()
    data class Hit(val damage: Int) : CollisionResult()
}

object CollisionDetector {
    
    private const val CHARACTER_WIDTH = 30f
    private const val CHARACTER_HEIGHT = 40f
    
    fun checkCollisions(
        character: Character,
        platforms: List<Platform>,
        coins: List<Coin>,
        powerUps: List<PowerUp>
    ): List<CollisionResult> {
        val results = mutableListOf<CollisionResult>()
        
        val charLeft = character.position.x - CHARACTER_WIDTH / 2
        val charRight = character.position.x + CHARACTER_WIDTH / 2
        val charTop = character.position.y - CHARACTER_HEIGHT
        val charBottom = character.position.y
        
        if (charBottom > GameConstants.GAME_WORLD_HEIGHT) {
            results.add(CollisionResult.FellOffScreen)
            return results
        }
        
        for (platform in platforms) {
            val platLeft = platform.x
            val platRight = platform.x + platform.width
            val platTop = platform.y
            val platBottom = platform.y + platform.height
            
            if (charRight > platLeft && charLeft < platRight &&
                charBottom > platTop && charTop < platBottom) {
                
                val overlapLeft = charRight - platLeft
                val overlapRight = platRight - charLeft
                val overlapTop = charBottom - platTop
                val overlapBottom = platBottom - charTop
                
                val minOverlap = minOf(overlapLeft, overlapRight, overlapTop, overlapBottom)
                
                when (minOverlap) {
                    overlapTop -> {
                        if (character.velocity.y > 0) {
                            results.add(CollisionResult.LandedOnPlatform(platform))
                        }
                    }
                    overlapLeft -> {
                        if (character.velocity.y >= 0) {
                            results.add(CollisionResult.HitSideOfPlatform(platform))
                        }
                    }
                    overlapRight -> {}
                }
            }
        }
        
        for (coin in coins) {
            if (!coin.collected) {
                val dx = character.position.x - coin.x
                val dy = character.position.y - coin.y
                if (dx * dx + dy * dy < 900f) {
                    results.add(CollisionResult.CollectedCoin(coin))
                }
            }
        }
        
        for (powerUp in powerUps) {
            if (!powerUp.collected) {
                val dx = character.position.x - powerUp.x
                val dy = character.position.y - powerUp.y
                if (dx * dx + dy * dy < 900f) {
                    results.add(CollisionResult.CollectedPowerUp(powerUp))
                }
            }
        }
        
        return results
    }

    fun checkBossCollisions(
        character: Character,
        boss: Boss,
        projectiles: List<Projectile>,
        laserActive: Boolean,
        laserAngle: Float
    ): CollisionResult {
        for (projectile in projectiles) {
            if (circleCollision(
                character.position,
                GameConstants.CHARACTER_RADIUS,
                projectile.position,
                projectile.radius
            )) {
                return CollisionResult.Hit(projectile.damage)
            }
        }

        if (boss.state == BossState.DASHING) {
            if (circleCollision(
                character.position,
                GameConstants.CHARACTER_RADIUS,
                boss.position,
                GameConstants.BOSS_DASH_RADIUS
            )) {
                return CollisionResult.Hit(1)
            }
        }

        if (laserActive) {
            if (checkLaserCollision(character.position, boss.position, laserAngle)) {
                return CollisionResult.Hit(1)
            }
        }

        return CollisionResult.None
    }

    private fun circleCollision(
        center1: com.gameway.domain.model.Vector2,
        radius1: Float,
        center2: com.gameway.domain.model.Vector2,
        radius2: Float
    ): Boolean {
        val dx = center1.x - center2.x
        val dy = center1.y - center2.y
        val distanceSquared = dx * dx + dy * dy
        val radiusSum = radius1 + radius2
        return distanceSquared < radiusSum * radiusSum
    }

    private fun checkLaserCollision(
        characterPos: com.gameway.domain.model.Vector2,
        laserOrigin: com.gameway.domain.model.Vector2,
        angle: Float
    ): Boolean {
        val laserLength = 1000f
        val laserEnd = com.gameway.domain.model.Vector2(
            laserOrigin.x + cos(angle) * laserLength,
            laserOrigin.y + sin(angle) * laserLength
        )
        val distance = pointToLineDistance(characterPos, laserOrigin, laserEnd)
        return distance < GameConstants.CHARACTER_RADIUS + 10f
    }

    private fun pointToLineDistance(
        point: com.gameway.domain.model.Vector2,
        lineStart: com.gameway.domain.model.Vector2,
        lineEnd: com.gameway.domain.model.Vector2
    ): Float {
        val A = point.x - lineStart.x
        val B = point.y - lineStart.y
        val C = lineEnd.x - lineStart.x
        val D = lineEnd.y - lineStart.y

        val dot = A * C + B * D
        val lenSq = C * C + D * D
        val param = if (lenSq != 0f) dot / lenSq else -1f

        val xx = when {
            param < 0 -> lineStart.x
            param > 1 -> lineEnd.x
            else -> lineStart.x + param * C
        }
        val yy = when {
            param < 0 -> lineStart.y
            param > 1 -> lineEnd.y
            else -> lineStart.y + param * D
        }

        val dx = point.x - xx
        val dy = point.y - yy
        return sqrt(dx * dx + dy * dy)
    }
}
