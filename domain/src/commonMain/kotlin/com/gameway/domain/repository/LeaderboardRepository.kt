package com.gameway.domain.repository

import com.gameway.domain.model.LeaderboardEntry

interface LeaderboardRepository {
    suspend fun saveScore(entry: LeaderboardEntry)
    suspend fun getLeaderboard(limit: Int = 10): List<LeaderboardEntry>
    suspend fun getPersonalBest(): LeaderboardEntry?
    suspend fun updatePlayTime(additionalTime: Long)
    suspend fun incrementDeaths()
    suspend fun clearLeaderboard()
}
