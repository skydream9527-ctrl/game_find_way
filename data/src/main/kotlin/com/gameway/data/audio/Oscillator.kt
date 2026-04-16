package com.gameway.data.audio

import kotlin.math.PI
import kotlin.math.sin

class Oscillator(private val sampleRate: Int = 44100) {
    private var phase = 0.0

    fun generateTone(frequency: Float, durationMs: Int, volume: Float = 0.3f): ByteArray {
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val sample = (sin(phase * 2 * PI) * 32767 * volume).toInt().coerceIn(-32768, 32767).toShort()
            buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = (sample.toInt() shr 8 and 0xFF).toByte()
            phase += frequency / sampleRate
            if (phase > 1.0) phase -= 1.0
        }
        return buffer
    }

    fun reset() {
        phase = 0.0
    }
}

data class Note(val frequency: Float, val durationMs: Int)