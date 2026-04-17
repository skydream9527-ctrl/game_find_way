package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSkinData(
    val ownedSkins: List<OwnedSkin> = emptyList(),
    val equippedSkinId: String? = null
)
