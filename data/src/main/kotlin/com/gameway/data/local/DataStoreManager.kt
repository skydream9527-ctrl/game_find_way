package com.gameway.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "findway_settings")

class DataStoreManager(private val context: Context) {
    
    private val dataStore = context.dataStore
    
    object Keys {
        val CURRENT_CHAPTER = intPreferencesKey("current_chapter")
        val TOTAL_COINS = intPreferencesKey("total_coins")
        val COMPLETED_LEVELS = stringPreferencesKey("completed_levels")
        val BGM_VOLUME = floatPreferencesKey("bgm_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val VIBRATE = booleanPreferencesKey("vibrate")
        val LANGUAGE = stringPreferencesKey("language")
    }
    
    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
    
    fun <T> get(key: Preferences.Key<T>, default: T) = dataStore.data.map { preferences ->
        preferences[key] ?: default
    }
    
    suspend fun <T> getOnce(key: Preferences.Key<T>, default: T): T {
        return dataStore.data.map { preferences ->
            preferences[key] ?: default
        }.first()
    }
}