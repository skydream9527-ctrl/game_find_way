package com.gameway.domain.usecase

import com.gameway.domain.model.GameProgress
import com.gameway.domain.repository.GameProgressRepository

class SaveProgressUseCase(private val repository: GameProgressRepository) {
    suspend operator fun invoke(progress: GameProgress): Result<Unit> {
        return repository.saveProgress(progress)
    }
}