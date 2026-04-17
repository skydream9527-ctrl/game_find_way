package com.gameway.domain.usecase

import com.gameway.domain.model.Friend
import com.gameway.domain.repository.FriendRepository
import java.util.UUID

class AddFriendUseCase(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(
        playerName: String,
        playerId: String,
        highScore: Int = 0,
        highestChapter: Int = 0,
        highestLevel: Int = 0,
        note: String = ""
    ): Friend {
        val friend = Friend(
            id = UUID.randomUUID().toString(),
            playerName = playerName,
            playerId = playerId,
            highScore = highScore,
            highestChapter = highestChapter,
            highestLevel = highestLevel,
            addedAt = System.currentTimeMillis(),
            note = note
        )
        repository.addFriend(friend)
        return friend
    }
}
