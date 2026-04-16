package com.gameway.presentation.screens.game

import kotlin.random.Random

class ScreenShakeController(
    private val decay: Float = 0.9f,
    private val minIntensity: Float = 0.5f
) {
    private var offsetX = 0f
    private var offsetY = 0f
    private var intensity = 0f

    fun trigger(intensity: Float) {
        this.intensity = intensity
    }

    fun update(): Pair<Float, Float> {
        if (intensity > minIntensity) {
            offsetX = (Random.nextFloat() - 0.5f) * 2 * intensity
            offsetY = (Random.nextFloat() - 0.5f) * 2 * intensity
            intensity *= decay
        } else {
            offsetX = 0f
            offsetY = 0f
            intensity = 0f
        }
        return Pair(offsetX, offsetY)
    }

    fun getOffsetX(): Float = offsetX
    fun getOffsetY(): Float = offsetY
    fun isActive(): Boolean = intensity > minIntensity
}
