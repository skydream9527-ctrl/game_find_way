package com.gameway.domain.repository

import com.gameway.domain.model.GameProgress

interface GameProgressRepository {
    suspend fun getProgress(): Result<GameProgress>
    suspend fun saveProgress(progress: GameProgress): Result<Unit>
    suspend fun updateCoins(amount: Int): Result<Unit>
    suspend fun completeLevel(chapterId: Int, levelNumber: Int): Result<Unit>
}