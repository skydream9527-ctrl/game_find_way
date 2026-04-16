package com.gameway.data.audio

import com.gameway.domain.model.Difficulty
import com.gameway.domain.model.GameState

class BGMManager(
    private val bgmPlayer: BGMPlayer = ProceduralBGMPlayer()
) {
    private var currentDifficulty: Difficulty = Difficulty.EASY

    fun onGameStateChanged(state: GameState) {
        when (state) {
            is GameState.Playing -> bgmPlayer.playDifficultyBGM(currentDifficulty)
            is GameState.BossActive -> bgmPlayer.playBossBGM()
            is GameState.Paused -> bgmPlayer.setVolume(0.3f)
            is GameState.Countdown -> bgmPlayer.setVolume(0.5f)
            is GameState.Completed, is GameState.Failed -> {
                bgmPlayer.stop()
            }
            else -> {}
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        currentDifficulty = difficulty
    }

    fun onResume() {
        bgmPlayer.setVolume(1.0f)
        bgmPlayer.resume()
    }

    fun release() {
        bgmPlayer.stop()
    }
}
