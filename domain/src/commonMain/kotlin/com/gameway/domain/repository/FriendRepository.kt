package com.gameway.domain.repository

import com.gameway.domain.model.Friend

interface FriendRepository {
    suspend fun addFriend(friend: Friend)
    suspend fun removeFriend(friendId: String)
    suspend fun updateFriend(friend: Friend)
    suspend fun getFriends(): List<Friend>
    suspend fun findFriendByPlayerId(playerId: String): Friend?
    suspend fun searchFriends(query: String): List<Friend>
}