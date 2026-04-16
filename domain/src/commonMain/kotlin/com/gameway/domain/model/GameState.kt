package com.gameway.domain.model

sealed interface GameState {
    data object Loading : GameState
    data object Countdown : GameState
    data object Playing : GameState
    data class BossActive(val survivalTime: Float) : GameState
    data object Paused : GameState
    data class Completed(val score: Int, val coinsCollected: Int) : GameState
    data class Failed(val reason: String) : GameState
}

data class GameProgress(
    val currentChapter: Int = 1,
    val unlockedLevels: Map<Int, List<Int>> = emptyMap(),
    val completedLevels: Map<Int, List<Int>> = emptyMap(),
    val totalCoins: Int = 0
)

data class CharacterStats(
    val health: Int = 3,
    val jumpPower: Float = 1.0f,
    val moveSpeed: Float = 1.0f,
    val unlockedSkills: Set<SkillType> = emptySet()
)

enum class SkillType {
    DOUBLE_JUMP, TRIPLE_JUMP, JUMP_BOOST_10, JUMP_BOOST_20, SPEED_BOOST_10
}