package com.gameway.domain.usecase

import com.gameway.domain.model.Friend
import com.gameway.domain.repository.FriendRepository

class GetFriendsUseCase(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(): List<Friend> {
        return repository.getFriends()
    }

    suspend fun search(query: String): List<Friend> {
        return repository.searchFriends(query)
    }
}
