package com.gameway.domain.usecase

import com.gameway.domain.engine.MatchManager
import com.gameway.domain.model.MatchResult

class SubmitScoreUseCase(
    private val matchManager: MatchManager
) {
    suspend operator fun invoke(matchId: String, playerId: String, score: Int): MatchResult {
        return matchManager.submitScore(matchId, playerId, score)
    }
}
