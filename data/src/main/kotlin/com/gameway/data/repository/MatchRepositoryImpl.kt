package com.gameway.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameway.domain.model.Match
import com.gameway.domain.model.MatchStatus
import com.gameway.domain.repository.MatchRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.matchDataStore: DataStore<Preferences> by preferencesDataStore(name = "matches")

class MatchRepositoryImpl(
    private val context: Context
) : MatchRepository {

    private object Keys {
        val MATCHES = stringPreferencesKey("matches_list")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.matchDataStore

    override suspend fun createMatch(match: Match) {
        dataStore.edit { prefs ->
            val current = getMatchList(prefs).toMutableList()
            current.add(match)
            prefs[Keys.MATCHES] = Json.encodeToString(current)
        }
    }

    override suspend fun updateMatch(match: Match) {
        dataStore.edit { prefs ->
            val current = getMatchList(prefs).toMutableList()
            val index = current.indexOfFirst { it.id == match.id }
            if (index >= 0) {
                current[index] = match
                prefs[Keys.MATCHES] = Json.encodeToString(current)
            }
        }
    }

    override suspend fun getMatch(matchId: String): Match? {
        return getMatchList(dataStore.data.first()).find { it.id == matchId }
    }

    override suspend fun getPendingMatchesForPlayer(playerId: String): List<Match> {
        return getMatchList(dataStore.data.first()).filter {
            it.status == MatchStatus.PENDING && it.challengedId == playerId
        }
    }

    override suspend fun getActiveMatches(): List<Match> {
        return getMatchList(dataStore.data.first()).filter {
            it.status == MatchStatus.ACCEPTED
        }
    }

    override suspend fun getCompletedMatches(limit: Int): List<Match> {
        return getMatchList(dataStore.data.first())
            .filter { it.status == MatchStatus.COMPLETED }
            .sortedByDescending { it.completedAt }
            .take(limit)
    }

    override suspend fun deleteMatch(matchId: String) {
        dataStore.edit { prefs ->
            val current = getMatchList(prefs).filter { it.id != matchId }
            prefs[Keys.MATCHES] = Json.encodeToString(current)
        }
    }

    private fun getMatchList(prefs: Preferences): List<Match> {
        val json = prefs[Keys.MATCHES] ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
