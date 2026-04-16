package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val id: String,
    val playerName: String,
    val highScore: Int,
    val highestChapter: Int,
    val highestLevel: Int,
    val totalPlayTime: Long,
    val totalDeaths: Int,
    val timestamp: Long
)
