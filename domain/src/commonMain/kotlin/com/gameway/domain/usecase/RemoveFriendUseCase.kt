package com.gameway.domain.usecase

import com.gameway.domain.repository.FriendRepository

class RemoveFriendUseCase(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(friendId: String) {
        repository.removeFriend(friendId)
    }
}
