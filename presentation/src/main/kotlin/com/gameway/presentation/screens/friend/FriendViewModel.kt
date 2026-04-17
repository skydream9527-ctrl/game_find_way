package com.gameway.presentation.screens.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.model.Friend
import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.usecase.AddFriendUseCase
import com.gameway.domain.usecase.GetFriendsUseCase
import com.gameway.domain.usecase.GetLeaderboardUseCase
import com.gameway.domain.usecase.RemoveFriendUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FriendUiState(
    val friends: List<Friend> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val leaderboardEntries: List<LeaderboardEntry> = emptyList()
)

class FriendViewModel(
    private val getFriendsUseCase: GetFriendsUseCase,
    private val addFriendUseCase: AddFriendUseCase,
    private val removeFriendUseCase: RemoveFriendUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val friends = getFriendsUseCase()
            val leaderboard = getLeaderboardUseCase(20)
            _uiState.value = FriendUiState(
                friends = friends,
                leaderboardEntries = leaderboard,
                isLoading = false
            )
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addFriend(playerName: String, playerId: String, note: String = "") {
        viewModelScope.launch {
            addFriendUseCase(playerName = playerName, playerId = playerId, note = note)
            loadFriends()
            hideAddDialog()
        }
    }

    fun addFriendFromLeaderboard(entry: LeaderboardEntry) {
        viewModelScope.launch {
            addFriendUseCase(
                playerName = entry.playerName,
                playerId = entry.id,
                highScore = entry.highScore,
                highestChapter = entry.highestChapter,
                highestLevel = entry.highestLevel
            )
            loadFriends()
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            removeFriendUseCase(friendId)
            loadFriends()
        }
    }
}
