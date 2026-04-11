package com.gameway.domain.model

enum class PowerUpType {
    FEATHER, BUTTERFLY, LIGHTNING, SHIELD, GEM, STAR
}

data class PowerUp(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: PowerUpType,
    val isPermanent: Boolean,
    val collected: Boolean = false
) {
    val duration: Long
        get() = when (type) {
            PowerUpType.FEATHER -> 10000L
            PowerUpType.BUTTERFLY -> 15000L
            PowerUpType.LIGHTNING -> 8000L
            PowerUpType.SHIELD -> 0L
            PowerUpType.GEM -> 0L
            PowerUpType.STAR -> 0L
        }
}

data class ActivePowerUp(
    val type: PowerUpType,
    val activatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long
) {
    companion object {
        fun create(type: PowerUpType): ActivePowerUp {
            val duration = when (type) {
                PowerUpType.FEATHER -> 10000L
                PowerUpType.BUTTERFLY -> 15000L
                PowerUpType.LIGHTNING -> 8000L
                PowerUpType.SHIELD -> Long.MAX_VALUE
                PowerUpType.GEM -> Long.MAX_VALUE
                PowerUpType.STAR -> Long.MAX_VALUE
            }
            return ActivePowerUp(
                type = type,
                expiresAt = System.currentTimeMillis() + duration
            )
        }
    }
}