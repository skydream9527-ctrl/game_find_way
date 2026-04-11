package com.gameway.domain.usecase

import com.gameway.domain.model.CharacterStats
import com.gameway.domain.repository.GameProgressRepository

class GetCharacterStatsUseCase(private val repository: GameProgressRepository) {
    suspend operator fun invoke(): Result<CharacterStats> {
        return runCatching {
            repository.getProgress().getOrThrow()
            CharacterStats()
        }
    }
}