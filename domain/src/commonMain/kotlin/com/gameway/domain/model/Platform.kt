package com.gameway.domain.model

enum class PlatformType {
    STATIC, MOVING_HORIZONTAL, MOVING_VERTICAL
}

data class Platform(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float = 12f,
    val type: PlatformType = PlatformType.STATIC,
    val moveRange: Float = 0f,
    val moveSpeed: Float = 0f,
    val moveOffset: Float = 0f
) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height
}