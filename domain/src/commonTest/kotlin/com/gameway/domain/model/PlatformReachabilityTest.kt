package com.gameway.domain.model

import com.gameway.core.GameConstants
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformReachabilityTest {
    
    @Test
    fun testHorizontalReachabilityWithinRange() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 150f, y = 350f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testHorizontalUnreachableTooFar() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 250f, y = 350f, width = 80f)
        
        assertFalse(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testVerticalReachabilityGoingDown() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 100f, y = 450f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testVerticalReachabilityGoingUpWithinLimit() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 100f, y = 350f - 100f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testVerticalUnreachableTooHigh() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 100f, y = 350f - 150f, width = 80f)
        
        assertFalse(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testCombinedReachability() {
        val platform1 = Platform(id = 1, x = 150f, y = 350f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f + 100f + 180f, y = 350f - 110f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testExactHorizontalBoundary() {
        val platform1 = Platform(id = 1, x = 100f, y = 300f, width = 100f)
        val platform2 = Platform(id = 2, x = 100f + 100f + GameConstants.MAX_HORIZONTAL_JUMP_DISTANCE, y = 300f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testExactVerticalBoundary() {
        val platform1 = Platform(id = 1, x = 100f, y = 300f, width = 100f)
        val platform2 = Platform(id = 2, x = 200f, y = 300f - GameConstants.MAX_VERTICAL_JUMP_HEIGHT, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
    
    @Test
    fun testOverlappingPlatforms() {
        val platform1 = Platform(id = 1, x = 100f, y = 300f, width = 100f)
        val platform2 = Platform(id = 2, x = 150f, y = 300f, width = 80f)
        
        assertTrue(Platform.isReachable(platform1, platform2))
    }
}