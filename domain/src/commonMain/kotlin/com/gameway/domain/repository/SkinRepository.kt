package com.gameway.domain.repository

import com.gameway.domain.model.Skin

interface SkinRepository {
    suspend fun getAllSkins(): List<Skin>
    suspend fun getOwnedSkins(): List<Skin>
    suspend fun purchaseSkin(skinId: String)
    suspend fun equipSkin(skinId: String)
    suspend fun getEquippedSkin(): Skin?
    suspend fun isSkinOwned(skinId: String): Boolean
    suspend fun getPlayerCoins(): Int
    suspend fun deductCoins(amount: Int)
}