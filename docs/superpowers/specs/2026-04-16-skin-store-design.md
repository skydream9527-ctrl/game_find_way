# 皮肤商店系统设计文档

**日期:** 2026-04-16
**项目:** FindWay Game
**目标:** 实现皮肤商店，支持角色皮肤购买和装备

---

## 1. 概述

### 当前状态
- 有3个基础角色：小猫、小狗、小马
- 无皮肤商店系统

### 改进目标
1. 皮肤商店展示所有角色皮肤
2. 支持金币购买和人民币购买
3. 基础角色（猫狗马）免费
4. 可装备已购买皮肤

---

## 2. 数据模型

### 2.1 Skin

```kotlin
@Serializable
data class Skin(
    val id: String,
    val name: String,
    val characterType: CharacterType,
    val emoji: String,
    val price: Int,
    val currency: Currency,
    val isDefault: Boolean = false
)

enum class Currency {
    COIN,   // 金币
    RMB     // 人民币
}
```

### 2.2 OwnedSkin

```kotlin
@Serializable
data class OwnedSkin(
    val skinId: String,
    val purchasedAt: Long
)
```

### 2.3 PlayerSkinData

```kotlin
@Serializable
data class PlayerSkinData(
    val ownedSkins: List<OwnedSkin> = emptyList(),
    val equippedSkinId: String? = null
)
```

---

## 3. 皮肤列表

| ID | 名称 | 角色 | Emoji | 价格 | 货币 | 默认 |
|----|------|------|-------|------|------|------|
| cat_default | 小猫 | CAT | 🐱 | 0 | - | ✅ |
| dog_default | 小狗 | DOG | 🐕 | 0 | - | ✅ |
| horse_default | 小马 | HORSE | 🐴 | 0 | - | ✅ |
| cat_ninja | 小猫忍者 | CAT | 🐱‍👤 | 500 | COIN | ❌ |
| dog_robot | 机器狗 | DOG | 🐕‍🦺 | 800 | COIN | ❌ |
| cat_princess | 小猫公主 | CAT | 👸 | 60 | RMB | ❌ |
| dog_superhero | 超级英雄狗 | DOG | 🦸 | 80 | RMB | ❌ |

---

## 4. 架构设计

### 4.1 SkinRepository

```kotlin
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

### 4.2 SkinStoreManager

```kotlin
class SkinStoreManager(
    private val skinRepository: SkinRepository
) {
    suspend fun purchaseWithCoins(skinId: String): PurchaseResult {
        val skin = skinRepository.getAllSkins().find { it.id == skinId }
            ?: return PurchaseResult.SKIN_NOT_FOUND

        if (skinRepository.isSkinOwned(skinId)) {
            return PurchaseResult.ALREADY_OWNED
        }

        val playerCoins = skinRepository.getPlayerCoins()
        if (playerCoins < skin.price) {
            return PurchaseResult.NOT_ENOUGH_COINS
        }

        skinRepository.deductCoins(skin.price)
        skinRepository.purchaseSkin(skinId)
        return PurchaseResult.SUCCESS
    }

    suspend fun purchaseWithRMB(skinId: String): PurchaseResult {
        // 人民币购买逻辑（实际需要接入支付SDK）
        skinRepository.purchaseSkin(skinId)
        return PurchaseResult.SUCCESS
    }
}

enum class PurchaseResult {
    SUCCESS,
    SKIN_NOT_FOUND,
    ALREADY_OWNED,
    NOT_ENOUGH_COINS,
    PAYMENT_FAILED
}
```

---

## 5. UI 设计

### 5.1 SkinStoreScreen

```
┌─────────────────────────────┐
│  皮肤商店           🪙 9999  │
├─────────────────────────────┤
│ 📦 已拥有 (3)                │
│ [小猫][小狗][小马]           │
├─────────────────────────────┤
│ 🛒 可购买                    │
│ ┌─────────────────────────┐ │
│ │ 🐱‍👤 小猫忍者              │ │
│ │ 500 🪙                   │ │
│ │ [购买]                   │ │
│ └─────────────────────────┘ │
│ ┌─────────────────────────┐ │
│ │ 🐕‍🦺 机器狗               │ │
│ │ 800 🪙                   │ │
│ │ [购买]                   │ │
│ └─────────────────────────┘ │
├─────────────────────────────┤
│ 💎 VIP专属                  │
│ ┌─────────────────────────┐ │
│ │ 👸 小猫公主              │ │
│ │ ¥6.00                   │ │
│ │ [购买]                   │ │
│ └─────────────────────────┘ │
└─────────────────────────────┘
```

### 5.2 SkinPreviewDialog

```
┌─────────────────────────────┐
│         皮肤预览             │
├─────────────────────────────┤
│                             │
│           🐱‍👤               │
│                             │
│       小猫忍者              │
│                             │
│   价格: 500 🪙               │
│                             │
│   [装备]  [取消]            │
└─────────────────────────────┘
```

---

## 6. 实现步骤

### 阶段1：数据层
1. 创建 `Skin` 和 `Currency` 数据类
2. 创建 `OwnedSkin` 和 `PlayerSkinData` 数据类
3. 创建 `SkinRepository` 接口
4. 创建 `SkinRepositoryImpl` 实现

### 阶段2：业务层
1. 创建 `SkinStoreManager`
2. 创建 `PurchaseSkinUseCase`
3. 创建 `EquipSkinUseCase`

### 阶段3：UI层
1. 创建 `SkinStoreScreen`
2. 创建 `SkinStoreViewModel`
3. 创建 `SkinPreviewDialog`

---

## 7. 文件修改清单

**新建文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Skin.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Currency.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/OwnedSkin.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/PlayerSkinData.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/PurchaseResult.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/repository/SkinRepository.kt`
- `data/src/main/kotlin/com/gameway/data/repository/SkinRepositoryImpl.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/SkinStoreManager.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/PurchaseSkinUseCase.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/EquipSkinUseCase.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinStoreScreen.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinStoreViewModel.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/skinstore/SkinPreviewDialog.kt`

**修改文件：**
- `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`

---

## 8. 验收标准

1. ✅ 皮肤商店展示所有皮肤（免费/金币/RMB）
2. ✅ 可用金币购买皮肤
3. ✅ RMB购买入口（实际支付需接SDK）
4. ✅ 基础角色免费
5. ✅ 可装备已购买皮肤
6. ✅ 装备后游戏使用对应皮肤

---

**文档状态:** 已完成
**下一步:** 用户审核后创建实现计划