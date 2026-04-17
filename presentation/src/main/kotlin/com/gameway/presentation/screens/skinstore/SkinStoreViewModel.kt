package com.gameway.presentation.screens.skinstore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.engine.SkinStoreManager
import com.gameway.domain.model.PurchaseResult
import com.gameway.domain.model.Skin
import com.gameway.domain.usecase.EquipSkinUseCase
import com.gameway.domain.usecase.PurchaseSkinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SkinStoreUiState(
    val allSkins: List<Skin> = emptyList(),
    val ownedSkins: List<Skin> = emptyList(),
    val equippedSkin: Skin? = null,
    val playerCoins: Int = 0,
    val isLoading: Boolean = false,
    val selectedSkin: Skin? = null,
    val showPreviewDialog: Boolean = false,
    val purchaseMessage: String? = null
)

class SkinStoreViewModel(
    private val skinStoreManager: SkinStoreManager,
    private val purchaseSkinUseCase: PurchaseSkinUseCase,
    private val equipSkinUseCase: EquipSkinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkinStoreUiState())
    val uiState: StateFlow<SkinStoreUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val allSkins = skinStoreManager.getAllSkins()
            val ownedSkins = skinStoreManager.getOwnedSkins()
            val equippedSkin = skinStoreManager.getEquippedSkin()
            val coins = skinStoreManager.getPlayerCoins()
            _uiState.value = SkinStoreUiState(
                allSkins = allSkins,
                ownedSkins = ownedSkins,
                equippedSkin = equippedSkin,
                playerCoins = coins,
                isLoading = false
            )
        }
    }

    fun showPreview(skin: Skin) {
        _uiState.value = _uiState.value.copy(selectedSkin = skin, showPreviewDialog = true)
    }

    fun hidePreview() {
        _uiState.value = _uiState.value.copy(showPreviewDialog = false)
    }

    fun purchaseWithCoins(skinId: String) {
        viewModelScope.launch {
            val result = purchaseSkinUseCase.purchaseWithCoins(skinId)
            val message = when (result) {
                PurchaseResult.SUCCESS -> "购买成功!"
                PurchaseResult.NOT_ENOUGH_COINS -> "金币不足"
                PurchaseResult.ALREADY_OWNED -> "已拥有"
                else -> "购买失败"
            }
            _uiState.value = _uiState.value.copy(purchaseMessage = message)
            loadData()
        }
    }

    fun purchaseWithRMB(skinId: String) {
        viewModelScope.launch {
            val result = purchaseSkinUseCase.purchaseWithRMB(skinId)
            val message = when (result) {
                PurchaseResult.SUCCESS -> "购买成功!"
                PurchaseResult.ALREADY_OWNED -> "已拥有"
                else -> "购买失败"
            }
            _uiState.value = _uiState.value.copy(purchaseMessage = message)
            loadData()
        }
    }

    fun equipSkin(skinId: String) {
        viewModelScope.launch {
            equipSkinUseCase(skinId)
            hidePreview()
            loadData()
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(purchaseMessage = null)
    }
}
