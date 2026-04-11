package com.gameway.core.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtensionsTest {
    
    @Test
    fun `lerp returns start when fraction is 0`() {
        assertEquals(10f, lerp(10f, 20f, 0f))
    }
    
    @Test
    fun `lerp returns end when fraction is 1`() {
        assertEquals(20f, lerp(10f, 20f, 1f))
    }
    
    @Test
    fun `lerp returns midpoint when fraction is 0_5`() {
        assertEquals(15f, lerp(10f, 20f, 0.5f))
    }
    
    @Test
    fun `clamp Float keeps value in range`() {
        assertEquals(5f, 5f.clamp(0f, 10f))
        assertEquals(0f, (-5f).clamp(0f, 10f))
        assertEquals(10f, 15f.clamp(0f, 10f))
    }
    
    @Test
    fun `clamp Int keeps value in range`() {
        assertEquals(5, 5.clamp(0, 10))
        assertEquals(0, (-5).clamp(0, 10))
        assertEquals(10, 15.clamp(0, 10))
    }
    
    @Test
    fun `nextInt returns value in range`() {
        val random = Random(42)
        repeat(100) {
            val value = random.nextInt(5, 15)
            assertTrue(value in 5..15)
        }
    }
    
    @Test
    fun `nextFloat returns value in range`() {
        val random = Random(42)
        repeat(100) {
            val value = random.nextFloat(1.0f, 5.0f)
            assertTrue(value in 1.0f..5.0f)
        }
    }
}