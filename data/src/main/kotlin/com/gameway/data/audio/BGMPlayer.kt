package com.gameway.data.audio

import com.gameway.domain.model.Difficulty

interface BGMPlayer {
    fun playDifficultyBGM(difficulty: Difficulty)
    fun playBossBGM()
    fun stop()
    fun pause()
    fun resume()
    fun setVolume(volume: Float)
}