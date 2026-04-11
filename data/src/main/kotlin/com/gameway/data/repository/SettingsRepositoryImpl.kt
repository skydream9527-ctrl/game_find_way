package com.gameway.data.repository

import com.gameway.data.local.DataStoreManager
import com.gameway.data.local.Keys
import com.gameway.domain.repository.GameSettings
import com.gameway.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val dataStoreManager: DataStoreManager) : SettingsRepository {
    
    override suspend fun getSettings(): Result<GameSettings> = runCatching {
        GameSettings(
            bgmVolume = dataStoreManager.getOnce(Keys.BGM_VOLUME, 0.8f),
            sfxVolume = dataStoreManager.getOnce(Keys.SFX_VOLUME, 0.8f),
            vibrate = dataStoreManager.getOnce(Keys.VIBRATE, true),
            language = dataStoreManager.getOnce(Keys.LANGUAGE, "zh")
        )
    }
    
    override suspend fun saveSettings(settings: GameSettings): Result<Unit> = runCatching {
        dataStoreManager.save(Keys.BGM_VOLUME, settings.bgmVolume)
        dataStoreManager.save(Keys.SFX_VOLUME, settings.sfxVolume)
        dataStoreManager.save(Keys.VIBRATE, settings.vibrate)
        dataStoreManager.save(Keys.LANGUAGE, settings.language)
    }
    
    override suspend fun updateBgmVolume(volume: Float): Result<Unit> = runCatching {
        dataStoreManager.save(Keys.BGM_VOLUME, volume.coerceIn(0f, 1f))
    }
    
    override suspend fun updateSfxVolume(volume: Float): Result<Unit> = runCatching {
        dataStoreManager.save(Keys.SFX_VOLUME, volume.coerceIn(0f, 1f))
    }
}