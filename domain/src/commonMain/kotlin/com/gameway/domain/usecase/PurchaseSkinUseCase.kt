package com.gameway.domain.usecase

import com.gameway.domain.engine.SkinStoreManager
import com.gameway.domain.model.PurchaseResult

class PurchaseSkinUseCase(
    private val skinStoreManager: SkinStoreManager
) {
    suspend fun purchaseWithCoins(skinId: String): PurchaseResult {
        return skinStoreManager.purchaseWithCoins(skinId)
    }

    suspend fun purchaseWithRMB(skinId: String): PurchaseResult {
        return skinStoreManager.purchaseWithRMB(skinId)
    }
}
