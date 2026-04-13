package com.gameway.domain.model

data class CharacterConfig(
    val type: CharacterType,
    val name: String,
    val emoji: String,
    val moveSpeedMultiplier: Float,
    val jumpPowerMultiplier: Float
) {
    companion object {
        val CAT = CharacterConfig(
            type = CharacterType.CAT,
            name = "小猫",
            emoji = "🐱",
            moveSpeedMultiplier = 1.0f,
            jumpPowerMultiplier = 1.0f
        )
        
        val DOG = CharacterConfig(
            type = CharacterType.DOG,
            name = "小狗",
            emoji = "🐕",
            moveSpeedMultiplier = 0.9f,
            jumpPowerMultiplier = 1.1f
        )
        
        val HORSE = CharacterConfig(
            type = CharacterType.HORSE,
            name = "小马",
            emoji = "🐴",
            moveSpeedMultiplier = 1.2f,
            jumpPowerMultiplier = 0.95f
        )
        
        fun getAll(): List<CharacterConfig> = listOf(CAT, DOG, HORSE)
        
        fun getByType(type: CharacterType): CharacterConfig {
            return getAll().find { it.type == type } ?: CAT
        }
    }
}