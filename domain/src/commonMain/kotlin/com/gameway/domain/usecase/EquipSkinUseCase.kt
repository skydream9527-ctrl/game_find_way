package com.gameway.domain.usecase

import com.gameway.domain.engine.SkinStoreManager
import com.gameway.domain.model.Skin

class EquipSkinUseCase(
    private val skinStoreManager: SkinStoreManager
) {
    suspend operator fun invoke(skinId: String): Boolean {
        return skinStoreManager.equipSkin(skinId)
    }

    suspend fun getEquippedSkin(): Skin? {
        return skinStoreManager.getEquippedSkin()
    }
}
