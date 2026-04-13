package com.gameway.presentation.screens.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gameway.core.GameConstants
import com.gameway.domain.model.Chapter
import com.gameway.domain.model.PowerUpType

@Composable
fun GameCanvas(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val level = uiState.level
    
    if (level == null) return
    
    val chapter = Chapter.getAllChapters().find { it.id == level.chapterId } ?: Chapter.getAllChapters().first()
    val theme = chapter.theme
    
    Canvas(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(
                onTap = { viewModel.onTap() }
            )
        }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val scaleX = canvasWidth / GameConstants.GAME_WORLD_WIDTH
        val scaleY = canvasHeight / GameConstants.GAME_WORLD_HEIGHT
        val scale = minOf(scaleX, scaleY)
        
        val offsetX = (canvasWidth - GameConstants.GAME_WORLD_WIDTH * scale) / 2f
        val offsetY = (canvasHeight - GameConstants.GAME_WORLD_HEIGHT * scale) / 2f
        
        drawRect(color = Color(theme.backgroundColor), size = size)
        
        val viewportX = uiState.scrollX
        
        for (platform in level.platforms) {
            val screenX = (platform.x - viewportX) * scale + offsetX
            val screenY = platform.y * scale + offsetY
            val platformWidth = platform.width * scale
            val platformHeight = platform.height * scale
            
            if (screenX > -platformWidth && screenX < canvasWidth + platformWidth) {
                drawRoundRect(
                    color = Color(theme.platformColor),
                    topLeft = Offset(screenX, screenY),
                    size = Size(platformWidth, platformHeight)
                )
            }
        }
        
        for (coin in level.coins) {
            if (!coin.collected) {
                val screenX = (coin.x - viewportX) * scale + offsetX
                val screenY = coin.y * scale + offsetY
                if (screenX > -20f * scale && screenX < canvasWidth + 20f * scale) {
                    drawCircle(color = Color(0xFFFFD700), radius = 8f * scale, center = Offset(screenX, screenY))
                }
            }
        }
        
        for (powerUp in level.powerUps) {
            if (!powerUp.collected) {
                val screenX = (powerUp.x - viewportX) * scale + offsetX
                val screenY = powerUp.y * scale + offsetY
                if (screenX > -20f * scale && screenX < canvasWidth + 20f * scale) {
                    val color = when (powerUp.type) {
                        PowerUpType.FEATHER -> Color(0xFF8BC34A)
                        PowerUpType.BUTTERFLY -> Color(0xFF03A9F4)
                        PowerUpType.LIGHTNING -> Color(0xFFFFEB3B)
                        PowerUpType.SHIELD -> Color(0xFF9E9E9E)
                        PowerUpType.GEM -> Color(0xFFE91E63)
                        PowerUpType.STAR -> Color(0xFFFF9800)
                    }
                    drawCircle(color = color, radius = 10f * scale, center = Offset(screenX, screenY))
                }
            }
        }
        
        val charScreenX = (uiState.character.position.x - viewportX) * scale + offsetX
        val charScreenY = uiState.character.position.y * scale + offsetY
        
        drawCircle(color = Color(0xFF4A90D9), radius = 15f * scale, center = Offset(charScreenX, charScreenY - 10f * scale))
        drawCircle(color = Color(0xFFFFE0B2), radius = 12f * scale, center = Offset(charScreenX, charScreenY - 25f * scale))
        drawCircle(color = Color(0xFF333333), radius = 2f * scale, center = Offset(charScreenX - 4f * scale, charScreenY - 26f * scale))
        drawCircle(color = Color(0xFF333333), radius = 2f * scale, center = Offset(charScreenX + 4f * scale, charScreenY - 26f * scale))
    }
}