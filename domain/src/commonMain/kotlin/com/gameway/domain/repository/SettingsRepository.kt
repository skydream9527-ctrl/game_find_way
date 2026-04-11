package com.gameway.domain.repository

data class GameSettings(
    val bgmVolume: Float = 0.8f,
    val sfxVolume: Float = 0.8f,
    val vibrate: Boolean = true,
    val language: String = "zh"
)

interface SettingsRepository {
    suspend fun getSettings(): Result<GameSettings>
    suspend fun saveSettings(settings: GameSettings): Result<Unit>
    suspend fun updateBgmVolume(volume: Float): Result<Unit>
    suspend fun updateSfxVolume(volume: Float): Result<Unit>
}