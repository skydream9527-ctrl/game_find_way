package com.gameway.domain.repository

import com.gameway.domain.model.Match

interface MatchRepository {
    suspend fun createMatch(match: Match)
    suspend fun updateMatch(match: Match)
    suspend fun getMatch(matchId: String): Match?
    suspend fun getPendingMatchesForPlayer(playerId: String): List<Match>
    suspend fun getActiveMatches(): List<Match>
    suspend fun getCompletedMatches(limit: Int): List<Match>
    suspend fun deleteMatch(matchId: String)
}
