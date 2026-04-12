package com.gameway.data.repository

import com.gameway.data.local.DataStoreManager
import com.gameway.data.local.DataStoreManager.Keys
import com.gameway.domain.model.GameProgress
import com.gameway.domain.repository.GameProgressRepository

class GameProgressRepositoryImpl(private val dataStoreManager: DataStoreManager) : GameProgressRepository {
    
    override suspend fun getProgress(): Result<GameProgress> = runCatching {
        GameProgress(
            currentChapter = dataStoreManager.getOnce(Keys.CURRENT_CHAPTER, 1),
            totalCoins = dataStoreManager.getOnce(Keys.TOTAL_COINS, 0),
            completedLevels = parseCompletedLevels(dataStoreManager.getOnce(Keys.COMPLETED_LEVELS, ""))
        )
    }
    
    override suspend fun saveProgress(progress: GameProgress): Result<Unit> = runCatching {
        dataStoreManager.save(Keys.CURRENT_CHAPTER, progress.currentChapter)
        dataStoreManager.save(Keys.TOTAL_COINS, progress.totalCoins)
        dataStoreManager.save(Keys.COMPLETED_LEVELS, serializeCompletedLevels(progress.completedLevels))
    }
    
    override suspend fun updateCoins(amount: Int): Result<Unit> = runCatching {
        val current = dataStoreManager.getOnce(Keys.TOTAL_COINS, 0)
        dataStoreManager.save(Keys.TOTAL_COINS, current + amount)
    }
    
    override suspend fun completeLevel(chapterId: Int, levelNumber: Int): Result<Unit> = runCatching {
        val progress = getProgress().getOrThrow()
        val completedLevels = progress.completedLevels.toMutableMap()
        val chapterLevels = completedLevels[chapterId]?.toMutableList() ?: mutableListOf()
        if (!chapterLevels.contains(levelNumber)) {
            chapterLevels.add(levelNumber)
            completedLevels[chapterId] = chapterLevels
        }
        dataStoreManager.save(Keys.COMPLETED_LEVELS, serializeCompletedLevels(completedLevels))
    }
    
    private fun parseCompletedLevels(data: String): Map<Int, List<Int>> {
        if (data.isEmpty()) return emptyMap()
        return data.split(";").associate { pair ->
            val parts = pair.split(":")
            if (parts.size == 2) {
                parts[0].toInt() to parts[1].split(",").map { it.toInt() }
            } else {
                0 to emptyList()
            }
        }.filterKeys { it > 0 }
    }
    
    private fun serializeCompletedLevels(levels: Map<Int, List<Int>>): String {
        return levels.entries.joinToString(";") { (chapter, levelList) ->
            "$chapter:${levelList.joinToString(",")}"
        }
    }
}