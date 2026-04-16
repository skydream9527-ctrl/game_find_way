package com.gameway.domain.usecase

import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.repository.LeaderboardRepository

class GetLeaderboardUseCase(
    private val repository: LeaderboardRepository
) {
    suspend operator fun invoke(limit: Int = 10): List<LeaderboardEntry> {
        return repository.getLeaderboard(limit)
    }

    suspend fun getPersonalBest(): LeaderboardEntry? {
        return repository.getPersonalBest()
    }
}
