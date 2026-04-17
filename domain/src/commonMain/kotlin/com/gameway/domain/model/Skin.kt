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
