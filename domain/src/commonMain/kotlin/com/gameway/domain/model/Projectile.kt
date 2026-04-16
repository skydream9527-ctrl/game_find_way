package com.gameway.domain.model

data class Projectile(
    val position: Vector2,
    val velocity: Vector2,
    val radius: Float,
    val damage: Int = 1
)