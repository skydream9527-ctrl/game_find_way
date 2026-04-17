package com.gameway.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameway.domain.model.Friend
import com.gameway.domain.repository.FriendRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.friendDataStore: DataStore<Preferences> by preferencesDataStore(name = "friends")

class FriendRepositoryImpl(
    private val context: Context
) : FriendRepository {

    private object Keys {
        val FRIENDS = stringPreferencesKey("friends_list")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.friendDataStore

    override suspend fun addFriend(friend: Friend) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).toMutableList()
            if (current.none { it.playerId == friend.playerId }) {
                current.add(friend)
                prefs[Keys.FRIENDS] = Json.encodeToString(current)
            }
        }
    }

    override suspend fun removeFriend(friendId: String) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).filter { it.id != friendId }
            prefs[Keys.FRIENDS] = Json.encodeToString(current)
        }
    }

    override suspend fun updateFriend(friend: Friend) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).toMutableList()
            val index = current.indexOfFirst { it.id == friend.id }
            if (index >= 0) {
                current[index] = friend
                prefs[Keys.FRIENDS] = Json.encodeToString(current)
            }
        }
    }

    override suspend fun getFriends(): List<Friend> {
        return dataStore.data.first().let { getFriendList(it) }
    }

    override suspend fun findFriendByPlayerId(playerId: String): Friend? {
        return getFriends().find { it.playerId == playerId }
    }

    override suspend fun searchFriends(query: String): List<Friend> {
        return getFriends().filter {
            it.playerName.contains(query, ignoreCase = true) ||
            it.playerId.contains(query, ignoreCase = true)
        }
    }

    private fun getFriendList(prefs: Preferences): List<Friend> {
        val json = prefs[Keys.FRIENDS] ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
