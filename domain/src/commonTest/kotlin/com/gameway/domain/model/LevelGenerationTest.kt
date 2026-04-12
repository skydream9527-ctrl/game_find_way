package com.gameway.domain.model

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
    
    private fun verifyAllPlatformsReachable(level: Level) {
        for (i in 0 until level.platforms.size - 1) {
            assertTrue(
                Platform.isReachable(level.platforms[i], level.platforms[i + 1]),
                "Platform ${i + 1} unreachable from platform $i"
            )
        }
    }
}