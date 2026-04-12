package com.gameway.domain.model

import com.gameway.core.GameConstants

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
    val moveOffset: Float = 0f,
    val originalX: Float = x
) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height
    
    companion object {
        // Note: Assumes target platform is to the right (character only moves forward)
        fun isReachable(from: Platform, to: Platform): Boolean {
            val horizontalGap = to.left - from.right  // gap from right edge of 'from' to left edge of 'to'
            val verticalDiff = from.y - to.y  // positive = target is higher
            
            val horizontalReachable = horizontalGap <= GameConstants.MAX_HORIZONTAL_JUMP_DISTANCE
            val verticalReachable = verticalDiff <= GameConstants.MAX_VERTICAL_JUMP_HEIGHT
            
            return horizontalReachable && verticalReachable
        }
    }
}