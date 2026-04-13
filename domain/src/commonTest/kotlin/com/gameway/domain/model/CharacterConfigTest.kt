package com.gameway.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CharacterConfigTest {
    
    @Test
    fun `getAll returns three character configs`() {
        val configs = CharacterConfig.getAll()
        assertEquals(3, configs.size)
    }
    
    @Test
    fun `CAT has correct attributes`() {
        val cat = CharacterConfig.CAT
        assertEquals(CharacterType.CAT, cat.type)
        assertEquals("小猫", cat.name)
        assertEquals("🐱", cat.emoji)
        assertEquals(1.0f, cat.moveSpeedMultiplier)
        assertEquals(1.0f, cat.jumpPowerMultiplier)
    }
    
    @Test
    fun `DOG has slower speed but higher jump`() {
        val dog = CharacterConfig.DOG
        assertEquals(0.9f, dog.moveSpeedMultiplier)
        assertEquals(1.1f, dog.jumpPowerMultiplier)
    }
    
    @Test
    fun `HORSE has faster speed but lower jump`() {
        val horse = CharacterConfig.HORSE
        assertEquals(1.2f, horse.moveSpeedMultiplier)
        assertEquals(0.95f, horse.jumpPowerMultiplier)
    }
    
    @Test
    fun `getByType returns correct config`() {
        val cat = CharacterConfig.getByType(CharacterType.CAT)
        assertEquals(CharacterConfig.CAT, cat)
        
        val dog = CharacterConfig.getByType(CharacterType.DOG)
        assertEquals(CharacterConfig.DOG, dog)
        
        val unknown = CharacterConfig.getByType(CharacterType.HORSE)
        assertNotNull(unknown)
    }
}