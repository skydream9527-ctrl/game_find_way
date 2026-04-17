# 皮肤商店系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现皮肤商店，支持角色皮肤购买和装备，基础角色免费，支持金币和RMB购买

**Architecture:** SkinRepository数据层 + SkinStoreManager业务层 + SkinStoreScreen UI层

**Tech Stack:** Kotlin, DataStore, Jetpack Compose, Koin DI

---

## 文件结构

```
domain/src/commonMain/kotlin/com/gameway/domain/
├── model/
│   ├── Skin.kt               # 新建
│   ├── Currency.kt           # 新建
│   ├── OwnedSkin.kt          # 新建
│   ├── PlayerSkinData.kt     # 新建
│   └── PurchaseResult.kt     # 新建
├── repository/
│   └── SkinRepository.kt     # 新建
├── engine/
│   └── SkinStoreManager.kt   # 新建
├── usecase/
│   ├── PurchaseSkinUseCase.kt # 新建
│   └── EquipSkinUseCase.kt    # 新建
data/src/main/kotlin/com/gameway/data/
└── repository/
    └── SkinRepositoryImpl.kt  # 新建
presentation/src/main/kotlin/com/gameway/presentation/
├── screens/skinstore/
│   ├── SkinStoreScreen.kt     # 新建
│   ├── SkinStoreViewModel.kt  # 新建
│   └── SkinPreviewDialog.kt   # 新建
└── navigation/
    └── AppNavigation.kt       # 修改
```

---

## Task 1: 创建数据模型

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Currency.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Skin.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/OwnedSkin.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/PlayerSkinData.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/PurchaseResult.kt`

- [ ] **Step 1: 创建 Currency.kt**

```kotlin
package com.gameway.domain.model

enum class Currency {
    COIN,
    RMB
}
```

- [ ] **Step 2: 创建 Skin.kt**

```kotlin
package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Skin(
    val id: String,
    val name: String,
    val characterType: CharacterType,
    val emoji: String,
    val price: Int,
    val currency: Currency,
    val isDefault: Boolean = false
) {
    companion object {
        val DEFAULT_SKINS = listOf(
            Skin("cat_default", "小猫", CharacterType.CAT, "🐱", 0, Currency.COIN, true),
            Skin("dog_default", "小狗", CharacterType.DOG, "🐕", 0, Currency.COIN, true),
            Skin("horse_default", "小马", CharacterType.HORSE, "🐴", 0, Currency.COIN, true)
        )

        val COIN_SKINS = listOf(
            Skin("cat_ninja", "小猫忍者", CharacterType.CAT, "🐱‍👤", 500, Currency.COIN),
            Skin("dog_robot", "机器狗", CharacterType.DOG, "🫎", 800, Currency.COIN)
        )

        val RMB_SKINS = listOf(
            Skin("cat_princess", "小猫公主", CharacterType.CAT, "👸", 60, Currency.RMB),
            Skin("dog_superhero", "超级英雄狗", CharacterType.DOG, "🦸", 80, Currency.RMB)
        )

        fun getAllSkins(): List<Skin> = DEFAULT_SKINS + COIN_SKINS + RMB_SKINS
    }
}
```

- [ ] **Step 3: 创建 OwnedSkin.kt**

```kotlin
package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OwnedSkin(
    val skinId: String,
    val purchasedAt: Long
)
```

- [ ] **Step 4: 创建 PlayerSkinData.kt**

```kotlin
package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSkinData(
    val ownedSkins: List<OwnedSkin> = emptyList(),
    val equippedSkinId: String? = null
)
```

- [ ] **Step 5: 创建 PurchaseResult.kt**

```kotlin
package com.gameway.domain.model

enum class PurchaseResult {
    SUCCESS,
    SKIN_NOT_FOUND,
    ALREADY_OWNED,
    NOT_ENOUGH_COINS,
    PAYMENT_FAILED
}
```

- [ ] **Step 6: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Currency.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Skin.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/model/OwnedSkin.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/model/PlayerSkinData.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/model/PurchaseResult.kt
git commit -m "feat(skin): add Skin models"
```

---

## Task 2: 创建SkinRepository

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/repository/SkinRepository.kt`

- [ ] **Step 1: 创建 SkinRepository.kt**

```kotlin
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
```

- [ ] **Step 2: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/repository/SkinRepository.kt
git commit -m "feat(skin): add SkinRepository interface"
```

---

## Task 3: 创建SkinRepositoryImpl

**Files:**
- Create: `data/src/main/kotlin/com/gameway/data/repository/SkinRepositoryImpl.kt`

- [ ] **Step 1: 创建 SkinRepositoryImpl.kt**

```kotlin
package com.gameway.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameway.domain.model.*
import kotlinx.coroutines.flow.first

private val Context.skinDataStore: DataStore<Preferences> by preferencesDataStore(name = "skins")

class SkinRepositoryImpl(
    private val context: Context
) : SkinRepository {

    private object Keys {
        val OWNED_SKINS = stringPreferencesKey("owned_skins")
        val EQUIPPED_SKIN_ID = stringPreferencesKey("equipped_skin_id")
        val PLAYER_COINS = intPreferencesKey("player_coins")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.skinDataStore

    override suspend fun getAllSkins(): List<Skin> = Skin.getAllSkins()

    override suspend fun getOwnedSkins(): List<Skin> {
        val ownedIds = getOwnedSkinIds()
        return getAllSkins().filter { it.id in ownedIds }
    }

    override suspend fun purchaseSkin(skinId: String) {
        dataStore.edit { prefs ->
            val current = getOwnedSkinIds(prefs).toMutableList()
            if (skinId !in current) {
                current.add(skinId)
                prefs[Keys.OWNED_SKINS] = Json.encodeToString(current)
            }
        }
    }

    override suspend fun equipSkin(skinId: String) {
        dataStore.edit { prefs ->
            prefs[Keys.EQUIPPED_SKIN_ID] = skinId
        }
    }

    override suspend fun getEquippedSkin(): Skin? {
        val skinId = dataStore.data.first()[Keys.EQUIPPED_SKIN_ID]
        return skinId?.let { id -> getAllSkins().find { it.id == id } }
    }

    override suspend fun isSkinOwned(skinId: String): Boolean {
        return skinId in getOwnedSkinIds()
    }

    override suspend fun getPlayerCoins(): Int {
        return dataStore.data.first()[Keys.PLAYER_COINS] ?: 0
    }

    override suspend fun deductCoins(amount: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.PLAYER_COINS] ?: 0
            prefs[Keys.PLAYER_COINS] = (current - amount).coerceAtLeast(0)
        }
    }

    private suspend fun getOwnedSkinIds(): List<String> {
        return getOwnedSkinIds(dataStore.data.first())
    }

    private fun getOwnedSkinIds(prefs: Preferences): List<String> {
        val json = prefs[Keys.OWNED_SKINS] ?: return Skin.DEFAULT_SKINS.map { it.id }
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            Skin.DEFAULT_SKINS.map { it.id }
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add data/src/main/kotlin/com/gameway/data/repository/SkinRepositoryImpl.kt
git commit -m "feat(skin): add SkinRepositoryImpl"
```

---

## Task 4: 创建SkinStoreManager和UseCase

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/engine/SkinStoreManager.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/PurchaseSkinUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/EquipSkinUseCase.kt`

- [ ] **Step 1: 创建 SkinStoreManager.kt**

```kotlin
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
```

- [ ] **Step 2: 创建 PurchaseSkinUseCase.kt**

```kotlin
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
```

- [ ] **Step 3: 创建 EquipSkinUseCase.kt**

```kotlin
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
```

- [ ] **Step 4: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/SkinStoreManager.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/PurchaseSkinUseCase.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/EquipSkinUseCase.kt
git commit -m "feat(skin): add SkinStoreManager and use cases"
```

---

## Task 5: 创建SkinStoreScreen和ViewModel

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinStoreViewModel.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinStoreScreen.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinPreviewDialog.kt`

- [ ] **Step 1: 创建 SkinStoreViewModel.kt**

```kotlin
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
```

- [ ] **Step 2: 创建 SkinStoreScreen.kt**

```kotlin
package com.gameway.presentation.screens.skinstore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameway.domain.model.Currency
import com.gameway.domain.model.Skin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinStoreScreen(
    viewModel: SkinStoreViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("皮肤商店") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "金币")
                        Text("${uiState.playerCoins}", fontSize = 16.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Text("已拥有", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.ownedSkins) { skin ->
                                OwnedSkinItem(
                                    skin = skin,
                                    isEquipped = uiState.equippedSkin?.id == skin.id,
                                    onClick = { viewModel.showPreview(skin) }
                                )
                            }
                        }
                    }

                    val coinSkins = uiState.allSkins.filter { it.currency == Currency.COIN && it.id !in uiState.ownedSkins.map { s -> s.id } }
                    if (coinSkins.isNotEmpty()) {
                        item {
                            Text("可购买 (金币)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(coinSkins) { skin ->
                            PurchasableSkinItem(
                                skin = skin,
                                onPurchase = { viewModel.purchaseWithCoins(skin.id) },
                                onClick = { viewModel.showPreview(skin) }
                            )
                        }
                    }

                    val rmbSkins = uiState.allSkins.filter { it.currency == Currency.RMB && it.id !in uiState.ownedSkins.map { s -> s.id } }
                    if (rmbSkins.isNotEmpty()) {
                        item {
                            Text("VIP专属", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(rmbSkins) { skin ->
                            PurchasableSkinItem(
                                skin = skin,
                                onPurchase = { viewModel.purchaseWithRMB(skin.id) },
                                onClick = { viewModel.showPreview(skin) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showPreviewDialog && uiState.selectedSkin != null) {
            SkinPreviewDialog(
                skin = uiState.selectedSkin!!,
                isOwned = uiState.selectedSkin!!.id in uiState.ownedSkins.map { it.id },
                onDismiss = { viewModel.hidePreview() },
                onPurchase = { isRmb ->
                    if (isRmb) viewModel.purchaseWithRMB(uiState.selectedSkin!!.id)
                    else viewModel.purchaseWithCoins(uiState.selectedSkin!!.id)
                },
                onEquip = { viewModel.equipSkin(uiState.selectedSkin!!.id) }
            )
        }

        uiState.purchaseMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearMessage()
            }
        }
    }
}

@Composable
private fun OwnedSkinItem(skin: Skin, isEquipped: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(80.dp).clickable(onClick = onClick),
        colors = if (isEquipped) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(skin.emoji, fontSize = 32.sp)
                if (isEquipped) Text("已装备", fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun PurchasableSkinItem(skin: Skin, onPurchase: () -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(skin.emoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(skin.name, fontWeight = FontWeight.Bold)
                    Text(if (skin.currency == Currency.COIN) "${skin.price} 🪙" else "¥${skin.price / 10.0}", fontSize = 12.sp)
                }
            }
            Button(onClick = onPurchase) { Text("购买") }
        }
    }
}
```

- [ ] **Step 3: 创建 SkinPreviewDialog.kt**

```kotlin
package com.gameway.presentation.screens.skinstore

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gameway.domain.model.Currency
import com.gameway.domain.model.Skin

@Composable
fun SkinPreviewDialog(
    skin: Skin,
    isOwned: Boolean,
    onDismiss: () -> Unit,
    onPurchase: (isRmb: Boolean) -> Unit,
    onEquip: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("皮肤预览", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Text(skin.emoji, fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(skin.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (skin.currency == Currency.COIN) "${skin.price} 🪙" else "¥${skin.price / 10.0}",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (isOwned) {
                    Button(onClick = onEquip, modifier = Modifier.fillMaxWidth()) {
                        Text("装备")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (skin.currency == Currency.RMB) {
                            Button(onClick = { onPurchase(true) }) {
                                Text("¥购买")
                            }
                        } else {
                            Button(onClick = { onPurchase(false) }) {
                                Text("🪙购买")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinStoreViewModel.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinStoreScreen.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinPreviewDialog.kt
git commit -m "feat(skin): add SkinStoreScreen, ViewModel, and dialog"
```

---

## Task 6: 集成到导航

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/di/AppModule.kt`
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt`

- [ ] **Step 1: 添加SkinStore路由**

Add to Screen sealed class:
```kotlin
object SkinStore : Screen()
```

Add navigation route:
```kotlin
composable(Screen.SkinStore.route) {
    SkinStoreScreen(
        viewModel = skinStoreViewModel,
        onBack = { navController.popBackStack() }
    )
}
```

**Step 2: 在Koin模块注册**

```kotlin
factory { SkinStoreManager(get()) }
factory { PurchaseSkinUseCase(get()) }
factory { EquipSkinUseCase(get()) }
factory { SkinStoreViewModel(get(), get(), get()) }
```

Also add SkinRepository:
```kotlin
factory { SkinRepositoryImpl(get()) }
```

**Step 3: 在MainMenuScreen添"商店"按钮**

Add a button that navigates to SkinStore screen:
```kotlin
Button(onClick = { onNavigateToSkinStore() }) {
    Text("商店")
}
```

Add the callback to MainMenuScreen function signature.

**Step 4: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt
git add presentation/src/main/kotlin/com/gameway/presentation/di/AppModule.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt
git commit -m "feat(skin): integrate SkinStoreScreen into navigation"
```

---

## 实现顺序

1. Task 1: 数据模型
2. Task 2: SkinRepository接口
3. Task 3: SkinRepositoryImpl
4. Task 4: SkinStoreManager和UseCase
5. Task 5: SkinStoreScreen、ViewModel、Dialog
6. Task 6: 集成到导航

---

## 验收标准

1. 皮肤商店展示所有皮肤（免费/金币/RMB）
2. 可用金币购买皮肤
3. RMB购买入口
4. 基础角色免费
5. 可装备已购买皮肤
6. 装备后游戏使用对应皮肤

---

**Plan complete.**