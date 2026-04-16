package com.gameway.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.repository.LeaderboardRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.leaderboardDataStore: DataStore<Preferences> by preferencesDataStore(name = "leaderboard")

class LeaderboardRepositoryImpl(
    private val context: Context
) : LeaderboardRepository {

    private object Keys {
        val LEADERBOARD = stringPreferencesKey("leaderboard_entries")
        val PERSONAL_BEST = stringPreferencesKey("personal_best")
        val TOTAL_PLAY_TIME = longPreferencesKey("total_play_time")
        val TOTAL_DEATHS = intPreferencesKey("total_deaths")
        val HIGHEST_CHAPTER = intPreferencesKey("highest_chapter")
        val HIGHEST_LEVEL = intPreferencesKey("highest_level")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.leaderboardDataStore

    override suspend fun saveScore(entry: LeaderboardEntry) {
        dataStore.edit { prefs ->
            val currentEntries = getLeaderboardFromPrefs(prefs).toMutableList()
            currentEntries.add(entry)
            currentEntries.sortByDescending { it.highScore }
            val top10 = currentEntries.take(10)

            prefs[Keys.LEADERBOARD] = Json.encodeToString(top10)

            val currentBest = getPersonalBestFromPrefs(prefs)
            if (currentBest == null || entry.highScore > currentBest.highScore) {
                prefs[Keys.PERSONAL_BEST] = Json.encodeToString(entry)
            }

            if (entry.highestChapter > (prefs[Keys.HIGHEST_CHAPTER] ?: 0)) {
                prefs[Keys.HIGHEST_CHAPTER] = entry.highestChapter
            }
            if (entry.highestLevel > (prefs[Keys.HIGHEST_LEVEL] ?: 0)) {
                prefs[Keys.HIGHEST_LEVEL] = entry.highestLevel
            }
        }
    }

    override suspend fun getLeaderboard(limit: Int): List<LeaderboardEntry> {
        return dataStore.data.first().let { prefs ->
            getLeaderboardFromPrefs(prefs).take(limit)
        }
    }

    override suspend fun getPersonalBest(): LeaderboardEntry? {
        return dataStore.data.first().let { prefs ->
            getPersonalBestFromPrefs(prefs)
        }
    }

    override suspend fun updatePlayTime(additionalTime: Long) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.TOTAL_PLAY_TIME] ?: 0L
            prefs[Keys.TOTAL_PLAY_TIME] = current + additionalTime
        }
    }

    override suspend fun incrementDeaths() {
        dataStore.edit { prefs ->
            val current = prefs[Keys.TOTAL_DEATHS] ?: 0
            prefs[Keys.TOTAL_DEATHS] = current + 1
        }
    }

    override suspend fun clearLeaderboard() {
        dataStore.edit { it.clear() }
    }

    private fun getLeaderboardFromPrefs(prefs: Preferences): List<LeaderboardEntry> {
        val json = prefs[Keys.LEADERBOARD] ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getPersonalBestFromPrefs(prefs: Preferences): LeaderboardEntry? {
        val json = prefs[Keys.PERSONAL_BEST] ?: return null
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            null
        }
    }
}
