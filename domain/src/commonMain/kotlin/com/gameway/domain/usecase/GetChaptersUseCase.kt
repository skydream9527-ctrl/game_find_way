package com.gameway.domain.usecase

import com.gameway.domain.model.Chapter

class GetChaptersUseCase {
    operator fun invoke(): List<Chapter> = Chapter.getAllChapters()
}