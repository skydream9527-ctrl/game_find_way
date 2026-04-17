package com.gameway.domain.usecase

import com.gameway.domain.engine.MatchManager
import com.gameway.domain.model.Match

class AcceptMatchUseCase(
    private val matchManager: MatchManager
) {
    suspend operator fun invoke(matchId: String): Match {
        return matchManager.acceptMatch(matchId)
    }

    suspend fun reject(matchId: String) {
        matchManager.rejectMatch(matchId)
    }
}
