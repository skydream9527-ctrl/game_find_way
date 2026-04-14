package com.gameway.domain.model

import com.gameway.core.GameConstants
import kotlin.test.Test
import kotlin.test.assertTrue

class LevelGenerationTest {
    
    @Test
    fun testAllPlatformsReachableInEasyLevel() {
        val level = Level.generate(chapterId = 1, levelNumber = 1, seed = 12345L)
        
        for (i in 0 until level.platforms.size - 1) {
            val current = level.platforms[i]
            val next = level.platforms[i + 1]
            assertTrue(
                Platform.isReachable(current, next),
                "Platform ${i + 1} should be reachable from platform $i"
            )
        }
    }
    
    @Test
    fun testAllPlatformsReachableInMediumLevel() {
        val level = Level.generate(chapterId = 1, levelNumber = 5, seed = 23456L)
        verifyAllPlatformsReachable(level)
    }
    
    @Test
    fun testAllPlatformsReachableInHardLevel() {
        val level = Level.generate(chapterId = 1, levelNumber = 7, seed = 34567L)
        verifyAllPlatformsReachable(level)
    }
    
    @Test
    fun testAllPlatformsReachableInExpertLevel() {
        val level = Level.generate(chapterId = 1, levelNumber = 10, seed = 45678L)
        verifyAllPlatformsReachable(level)
    }
    
    @Test
    fun testMultipleSeedsGenerateReachableLevels() {
        for (seed in 10000L..10010L) {
            val level = Level.generate(chapterId = 1, levelNumber = 3, seed = seed)
            verifyAllPlatformsReachable(level)
        }
    }
    
    @Test
    fun `generated platforms have no overlap`() {
        val level = Level.generate(chapterId = 1, levelNumber = 1, seed = 12345)
        
        for (i in 0 until level.platforms.size - 1) {
            val current = level.platforms[i]
            val next = level.platforms[i + 1]
            
            assertTrue(current.right <= next.left)
            
            val gap = next.left - current.right
            assertTrue(gap >= GameConstants.MIN_HORIZONTAL_GAP)
        }
    }
    
    @Test
    fun `platform gaps are within valid range`() {
        val level = Level.generate(chapterId = 1, levelNumber = 1, seed = 12345)
        val difficulty = level.difficulty
        val maxGap = getMaxGapForDifficulty(difficulty)
        
        for (i in 0 until level.platforms.size - 1) {
            val current = level.platforms[i]
            val next = level.platforms[i + 1]
            val gap = next.left - current.right
            
            assertTrue(gap <= maxGap)
        }
    }
    
    private fun verifyAllPlatformsReachable(level: Level) {
        for (i in 0 until level.platforms.size - 1) {
            assertTrue(
                Platform.isReachable(level.platforms[i], level.platforms[i + 1]),
                "Platform ${i + 1} unreachable from platform $i"
            )
        }
    }
    
    private fun getMaxGapForDifficulty(difficulty: Difficulty): Float {
        return when (difficulty) {
            Difficulty.EASY -> GameConstants.MAX_HORIZONTAL_GAP_EASY
            Difficulty.MEDIUM -> GameConstants.MAX_HORIZONTAL_GAP_MEDIUM
            Difficulty.HARD -> GameConstants.MAX_HORIZONTAL_GAP_HARD
            Difficulty.EXPERT -> GameConstants.MAX_HORIZONTAL_GAP_EXPERT
        }
    }
}