package com.gameway.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameway.domain.model.Skin
import com.gameway.domain.repository.SkinRepository
import kotlinx.coroutines.flow.first

private val Context.skinDataStore: DataStore<Preferences> by preferencesDataStore(name = "skins")

class SkinRepositoryImpl(
    private val context: Context
) : SkinRepository {

    private object Keys {
        val OWNED_SKINS = stringPreferencesKey("owned_skins")
        val EQUIPPED_SKIN = stringPreferencesKey("equipped_skin")
        val PLAYER_COINS = intPreferencesKey("player_coins")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.skinDataStore

    override suspend fun getAllSkins(): List<Skin> = Skin.getAllSkins()

    override suspend fun getOwnedSkins(): List<Skin> {
        val ownedIds = getOwnedSkinIds()
        return getAllSkins().filter { ownedIds.contains(it.id) || it.isDefault }
    }

    override suspend fun purchaseSkin(skinId: String) {
        dataStore.edit { prefs ->
            val current = getOwnedSkinIds(prefs).toMutableSet()
            current.add(skinId)
            prefs[Keys.OWNED_SKINS] = current.joinToString(",")
        }
    }

    override suspend fun equipSkin(skinId: String) {
        dataStore.edit { prefs ->
            prefs[Keys.EQUIPPED_SKIN] = skinId
        }
    }

    override suspend fun getEquippedSkin(): Skin? {
        val equippedId = dataStore.data.first()[Keys.EQUIPPED_SKIN]
        return if (equippedId != null) {
            getAllSkins().find { it.id == equippedId }
        } else {
            getAllSkins().find { it.isDefault }
        }
    }

    override suspend fun isSkinOwned(skinId: String): Boolean {
        val ownedIds = getOwnedSkinIds()
        return ownedIds.contains(skinId) || getAllSkins().find { it.id == skinId }?.isDefault == true
    }

    override suspend fun getPlayerCoins(): Int {
        return dataStore.data.first()[Keys.PLAYER_COINS] ?: 1000
    }

    override suspend fun deductCoins(amount: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.PLAYER_COINS] ?: 1000
            prefs[Keys.PLAYER_COINS] = (current - amount).coerceAtLeast(0)
        }
    }

    private suspend fun getOwnedSkinIds(): Set<String> {
        return dataStore.data.first().let { getOwnedSkinIds(it) }
    }

    private fun getOwnedSkinIds(prefs: Preferences): Set<String> {
        val stored = prefs[Keys.OWNED_SKINS] ?: return emptySet()
        return stored.split(",").filter { it.isNotBlank() }.toSet()
    }
}
