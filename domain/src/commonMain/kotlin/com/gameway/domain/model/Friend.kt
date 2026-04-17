package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    val id: String,
    val playerName: String,
    val playerId: String,
    val highScore: Int,
    val highestChapter: Int,
    val highestLevel: Int,
    val addedAt: Long,
    val note: String = ""
)