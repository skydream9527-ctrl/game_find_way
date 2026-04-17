package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OwnedSkin(
    val skinId: String,
    val purchasedAt: Long
)
