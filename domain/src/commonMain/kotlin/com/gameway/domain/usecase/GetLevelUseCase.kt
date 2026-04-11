package com.gameway.domain.usecase

import com.gameway.domain.model.Level

class GetLevelUseCase {
    operator fun invoke(chapterId: Int, levelNumber: Int, seed: Long = System.currentTimeMillis()): Level {
        return Level.generate(chapterId, levelNumber, seed)
    }
}