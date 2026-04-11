package com.gameway.domain.engine

import com.gameway.domain.model.Character
import com.gameway.domain.model.Coin
import com.gameway.domain.model.Platform
import com.gameway.domain.model.PowerUp

sealed class CollisionResult {
    data object NoCollision : CollisionResult()
    data class LandedOnPlatform(val platform: Platform) : CollisionResult()
    data class HitSideOfPlatform(val platform: Platform) : CollisionResult()
    data class CollectedCoin(val coin: Coin) : CollisionResult()
    data class CollectedPowerUp(val powerUp: PowerUp) : CollisionResult()
    data object FellOffScreen : CollisionResult()
}

object CollisionDetector {
    
    private const val SCREEN_BOTTOM = 600f
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
        
        if (charBottom > SCREEN_BOTTOM) {
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
                    overlapTop -> results.add(CollisionResult.LandedOnPlatform(platform))
                    overlapLeft, overlapRight -> results.add(CollisionResult.HitSideOfPlatform(platform))
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
}