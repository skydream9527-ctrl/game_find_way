package com.gameway.domain.model

data class Coin(
    val id: Int,
    val x: Float,
    val y: Float,
    val collected: Boolean = false
)