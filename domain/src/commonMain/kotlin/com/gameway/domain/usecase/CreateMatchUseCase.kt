package com.gameway.domain.usecase

import com.gameway.domain.engine.MatchManager
import com.gameway.domain.model.Match

class CreateMatchUseCase(
    private val matchManager: MatchManager
) {
    suspend operator fun invoke(
        challengerId: String,
        challengerName: String,
        challengedId: String,
        challengedName: String,
        chapter: Int,
        level: Int
    ): Match {
        return matchManager.createMatchRequest(
            challengerId, challengerName, challengedId, challengedName, chapter, level
        )
    }
}
