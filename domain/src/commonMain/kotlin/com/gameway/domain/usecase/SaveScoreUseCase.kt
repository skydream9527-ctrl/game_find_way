package com.gameway.domain.usecase

import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.repository.LeaderboardRepository
import java.util.UUID

class SaveScoreUseCase(
    private val repository: LeaderboardRepository
) {
    suspend operator fun invoke(
        score: Int,
        chapter: Int,
        level: Int,
        playerName: String = "Player"
    ): LeaderboardEntry {
        val entry = LeaderboardEntry(
            id = UUID.randomUUID().toString(),
            playerName = playerName,
            highScore = score,
            highestChapter = chapter,
            highestLevel = level,
            totalPlayTime = 0L,
            totalDeaths = 0,
            timestamp = System.currentTimeMillis()
        )
        repository.saveScore(entry)
        return entry
    }
}
