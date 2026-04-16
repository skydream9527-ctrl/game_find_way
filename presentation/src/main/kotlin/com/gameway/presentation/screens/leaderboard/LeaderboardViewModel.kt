package com.gameway.presentation.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.usecase.GetLeaderboardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val personalBest: LeaderboardEntry? = null,
    val isLoading: Boolean = false
)

class LeaderboardViewModel(
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val entries = getLeaderboardUseCase(10)
            val personalBest = getLeaderboardUseCase.getPersonalBest()
            _uiState.value = LeaderboardUiState(
                entries = entries,
                personalBest = personalBest,
                isLoading = false
            )
        }
    }
}