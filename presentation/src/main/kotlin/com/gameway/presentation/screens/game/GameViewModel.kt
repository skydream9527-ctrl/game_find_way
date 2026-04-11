package com.gameway.presentation.screens.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.engine.GameEngine
import com.gameway.domain.model.Character
import com.gameway.domain.model.GameState
import com.gameway.domain.usecase.GetLevelUseCase
import com.gameway.domain.usecase.SaveProgressUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val character: Character = Character.createDefault(),
    val gameState: GameState = GameState.Loading,
    val scrollX: Float = 0f,
    val score: Int = 0,
    val chapterId: Int = 1,
    val levelNumber: Int = 1,
    val chargeProgress: Float = 0f
)

class GameViewModel(
    private val getLevelUseCase: GetLevelUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val gameEngine: GameEngine = GameEngine()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    private var gameLoopActive = false
    
    fun loadLevel(chapterId: Int, levelNumber: Int) {
        viewModelScope.launch {
            val level = getLevelUseCase(chapterId, levelNumber)
            gameEngine.startLevel(level)
            _uiState.value = _uiState.value.copy(chapterId = chapterId, levelNumber = levelNumber, gameState = GameState.Countdown)
            startGameLoop()
        }
    }
    
    private fun startGameLoop() {
        if (gameLoopActive) return
        gameLoopActive = true
        
        viewModelScope.launch {
            while (gameLoopActive) {
                val state = gameEngine.getGameState()
                _uiState.value = _uiState.value.copy(
                    character = gameEngine.getCharacter(),
                    gameState = state,
                    scrollX = gameEngine.getScrollX(),
                    score = gameEngine.getScore(),
                    chargeProgress = if (gameEngine.getCharacter().isCharging) gameEngine.getCharacter().chargeTime / 2000f else 0f
                )
                
                if (state is GameState.Completed || state is GameState.Failed) {
                    gameLoopActive = false
                    break
                }
                
                delay(16L)
            }
        }
    }
    
    fun onTouchDown() { gameEngine.startCharge() }
    fun onTouchUp() { gameEngine.releaseCharge() }
    fun onTap() { gameEngine.jump() }
    
    fun restart() {
        gameEngine.restart()
        _uiState.value = _uiState.value.copy(gameState = GameState.Countdown)
        gameLoopActive = false
        startGameLoop()
    }
    
    fun completeLevel() {
        viewModelScope.launch {
            val state = _uiState.value
            saveProgressUseCase(com.gameway.domain.model.GameProgress(currentChapter = state.chapterId, completedLevels = mapOf(state.chapterId to listOf(state.levelNumber))))
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        gameLoopActive = false
    }
}