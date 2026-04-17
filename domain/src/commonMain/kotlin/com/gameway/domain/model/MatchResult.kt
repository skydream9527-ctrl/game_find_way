package com.gameway.domain.model

data class MatchResult(
    val matchId: String,
    val winnerId: String?,
    val winnerName: String?,
    val isDraw: Boolean,
    val challengerScore: Int,
    val challengedScore: Int
)
