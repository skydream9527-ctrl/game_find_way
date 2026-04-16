package com.gameway.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.gameway.domain.model.Difficulty
import kotlinx.coroutines.*

class ProceduralBGMPlayer : BGMPlayer {
    private val oscillator = Oscillator()
    private var currentJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var volume = 1.0f
    private var isPaused = false

    private val difficultyPatterns = mapOf(
        Difficulty.EASY to BGMPattern.EASY,
        Difficulty.MEDIUM to BGMPattern.MEDIUM,
        Difficulty.HARD to BGMPattern.HARD,
        Difficulty.EXPERT to BGMPattern.EXPERT
    )

    override fun playDifficultyBGM(difficulty: Difficulty) {
        val pattern = difficultyPatterns[difficulty] ?: BGMPattern.EASY
        playPattern(pattern)
    }

    override fun playBossBGM() {
        playPattern(BGMPattern.BOSS)
    }

    private fun playPattern(pattern: BGMPattern) {
        stop()
        currentJob = scope.launch {
            while (isActive && !isPaused) {
                for (note in pattern.notes) {
                    if (!isActive || isPaused) break
                    playNote(note)
                }
            }
        }
    }

    private fun playNote(note: Note) {
        val buffer = oscillator.generateTone(note.frequency, note.durationMs, volume)
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(buffer.size)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        Thread.sleep(note.durationMs.toLong())
        audioTrack.stop()
        audioTrack.release()
        oscillator.reset()
    }

    override fun stop() {
        currentJob?.cancel()
        currentJob = null
        isPaused = false
    }

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        if (isPaused) {
            isPaused = false
        }
    }

    override fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
    }
}
