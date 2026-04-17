package com.gameway.presentation.screens.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.model.Friend
import com.gameway.domain.model.Match
import com.gameway.domain.model.MatchResult
import com.gameway.domain.usecase.AcceptMatchUseCase
import com.gameway.domain.usecase.CreateMatchUseCase
import com.gameway.domain.usecase.GetFriendsUseCase
import com.gameway.domain.usecase.SubmitScoreUseCase
import com.gameway.domain.engine.MatchManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MatchUiState(
    val pendingMatches: List<Match> = emptyList(),
    val activeMatches: List<Match> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val currentPlayerId: String = "local_player",
    val currentPlayerName: String = "我",
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val matchResult: MatchResult? = null
)

class MatchViewModel(
    private val matchManager: MatchManager,
    private val createMatchUseCase: CreateMatchUseCase,
    private val acceptMatchUseCase: AcceptMatchUseCase,
    private val submitScoreUseCase: SubmitScoreUseCase,
    private val getFriendsUseCase: GetFriendsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val pending = matchManager.getPendingMatches(_uiState.value.currentPlayerId)
            val active = matchManager.getActiveMatches()
            val friends = getFriendsUseCase()
            _uiState.value = MatchUiState(
                pendingMatches = pending,
                activeMatches = active,
                friends = friends,
                currentPlayerId = _uiState.value.currentPlayerId,
                currentPlayerName = _uiState.value.currentPlayerName,
                isLoading = false
            )
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun createMatch(challengedId: String, challengedName: String, chapter: Int, level: Int) {
        viewModelScope.launch {
            createMatchUseCase(
                challengerId = _uiState.value.currentPlayerId,
                challengerName = _uiState.value.currentPlayerName,
                challengedId = challengedId,
                challengedName = challengedName,
                chapter = chapter,
                level = level
            )
            hideCreateDialog()
            loadMatches()
        }
    }

    fun acceptMatch(matchId: String) {
        viewModelScope.launch {
            acceptMatchUseCase(matchId)
            loadMatches()
        }
    }

    fun rejectMatch(matchId: String) {
        viewModelScope.launch {
            acceptMatchUseCase.reject(matchId)
            loadMatches()
        }
    }

    fun submitScore(matchId: String, score: Int) {
        viewModelScope.launch {
            val result = submitScoreUseCase(matchId, _uiState.value.currentPlayerId, score)
            _uiState.value = _uiState.value.copy(matchResult = result)
            loadMatches()
        }
    }

    fun dismissResult() {
        _uiState.value = _uiState.value.copy(matchResult = null)
    }
}
