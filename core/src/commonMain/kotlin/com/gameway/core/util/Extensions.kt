package com.gameway.core.util

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

fun Float.clamp(min: Float, max: Float): Float = max(min, min(max, this))

fun Int.clamp(min: Int, max: Int): Int = max(min, min(max, this))

fun Random.nextFloat(min: Float, max: Float): Float = min + (max - min) * nextFloat()

inline fun <T> runCatchingAppError(block: () -> T): Result<T> = runCatching(block)