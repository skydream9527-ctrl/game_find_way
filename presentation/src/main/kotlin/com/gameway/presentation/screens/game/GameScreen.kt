package com.gameway.presentation.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gameway.domain.model.GameState
import com.gameway.presentation.screens.complete.LevelCompleteScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameScreen(chapterId: Int, levelNumber: Int, onLevelComplete: () -> Unit, onLevelFailed: () -> Unit, viewModel: GameViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(chapterId, levelNumber) { viewModel.loadLevel(chapterId, levelNumber) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        GameCanvas(viewModel = viewModel, chapterId = chapterId, levelNumber = levelNumber)
        
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "🪙 ${uiState.character.coinsCollected}", color = Color.White, fontSize = 16.sp)
            Text(text = "章节$chapterId - 关卡$levelNumber", color = Color.White, fontSize = 14.sp)
            Text(text = "分数: ${uiState.score}", color = Color.White, fontSize = 14.sp)
        }
        
        if (uiState.gameState is GameState.Countdown) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "准备...", color = Color.White, fontSize = 32.sp)
            }
        }
        
        when (val state = uiState.gameState) {
            is GameState.Completed -> {
                LevelCompleteScreen(
                    score = state.score,
                    coinsCollected = state.coinsCollected,
                    onNextLevel = { viewModel.completeLevel(); onLevelComplete() },
                    onRestart = { viewModel.restart() }
                )
            }
            is GameState.Failed -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("失败：${state.reason}", color = Color.Red, fontSize = 24.sp)
                }
            }
            else -> {}
        }
    }
}