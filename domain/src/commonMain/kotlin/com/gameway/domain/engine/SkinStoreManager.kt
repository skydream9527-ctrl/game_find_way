package com.gameway.domain.engine

import com.gameway.domain.model.PurchaseResult
import com.gameway.domain.model.Skin
import com.gameway.domain.repository.SkinRepository

class SkinStoreManager(
    private val skinRepository: SkinRepository
) {
    suspend fun purchaseWithCoins(skinId: String): PurchaseResult {
        val skin = skinRepository.getAllSkins().find { it.id == skinId }
            ?: return PurchaseResult.SKIN_NOT_FOUND

        if (skin.isDefault) return PurchaseResult.ALREADY_OWNED
        if (skinRepository.isSkinOwned(skinId)) return PurchaseResult.ALREADY_OWNED
        if (skin.currency != com.gameway.domain.model.Currency.COIN) return PurchaseResult.PAYMENT_FAILED

        val playerCoins = skinRepository.getPlayerCoins()
        if (playerCoins < skin.price) return PurchaseResult.NOT_ENOUGH_COINS

        skinRepository.deductCoins(skin.price)
        skinRepository.purchaseSkin(skinId)
        return PurchaseResult.SUCCESS
    }

    suspend fun purchaseWithRMB(skinId: String): PurchaseResult {
        val skin = skinRepository.getAllSkins().find { it.id == skinId }
            ?: return PurchaseResult.SKIN_NOT_FOUND

        if (skin.isDefault) return PurchaseResult.ALREADY_OWNED
        if (skinRepository.isSkinOwned(skinId)) return PurchaseResult.ALREADY_OWNED
        if (skin.currency != com.gameway.domain.model.Currency.RMB) return PurchaseResult.PAYMENT_FAILED

        skinRepository.purchaseSkin(skinId)
        return PurchaseResult.SUCCESS
    }

    suspend fun equipSkin(skinId: String): Boolean {
        if (!skinRepository.isSkinOwned(skinId)) return false
        skinRepository.equipSkin(skinId)
        return true
    }

    suspend fun getAllSkins(): List<Skin> = skinRepository.getAllSkins()
    suspend fun getOwnedSkins(): List<Skin> = skinRepository.getOwnedSkins()
    suspend fun getEquippedSkin(): Skin? = skinRepository.getEquippedSkin()
    suspend fun isSkinOwned(skinId: String): Boolean = skinRepository.isSkinOwned(skinId)
    suspend fun getPlayerCoins(): Int = skinRepository.getPlayerCoins()
}
