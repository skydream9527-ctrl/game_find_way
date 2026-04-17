package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val id: String,
    val challengerId: String,
    val challengerName: String,
    val challengedId: String,
    val challengedName: String,
    val chapter: Int,
    val level: Int,
    val challengerScore: Int = 0,
    val challengedScore: Int = 0,
    val status: MatchStatus,
    val createdAt: Long,
    val completedAt: Long? = null
)
