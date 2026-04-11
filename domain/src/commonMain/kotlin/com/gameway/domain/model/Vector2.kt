package com.gameway.domain.model

data class Vector2(
    val x: Float,
    val y: Float
) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2(x * scalar, y * scalar)
    
    fun length(): Float = kotlin.math.sqrt(x * x + y * y)
}