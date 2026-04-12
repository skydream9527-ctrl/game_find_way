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
import com.gameway.domain.model.Level
import com.gameway.domain.model.PowerUpType

@Composable
fun GameCanvas(viewModel: GameViewModel, chapterId: Int, levelNumber: Int) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Canvas(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(
                onTap = { viewModel.onTap() }
            )
        }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val scale = canvasWidth / 800f
        
        drawRect(color = Color(0xFF87CEEB), size = size)
        
        val viewportX = uiState.scrollX * scale
        
        val level = Level.generate(chapterId, levelNumber, 42L)
        for (platform in level.platforms) {
            val screenX = platform.x - viewportX
            if (screenX > -platform.width * scale && screenX < canvasWidth + platform.width * scale) {
                drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(screenX, platform.y * scale),
                    size = Size(platform.width * scale, platform.height * scale)
                )
            }
        }
        
        for (coin in level.coins) {
            if (!coin.collected) {
                val screenX = coin.x - viewportX
                if (screenX > -20f && screenX < canvasWidth + 20f) {
                    drawCircle(color = Color(0xFFFFD700), radius = 8f * scale, center = Offset(screenX, coin.y * scale))
                }
            }
        }
        
        for (powerUp in level.powerUps) {
            if (!powerUp.collected) {
                val screenX = powerUp.x - viewportX
                if (screenX > -20f && screenX < canvasWidth + 20f) {
                    val color = when (powerUp.type) {
                        PowerUpType.FEATHER -> Color(0xFF8BC34A)
                        PowerUpType.BUTTERFLY -> Color(0xFF03A9F4)
                        PowerUpType.LIGHTNING -> Color(0xFFFFEB3B)
                        PowerUpType.SHIELD -> Color(0xFF9E9E9E)
                        PowerUpType.GEM -> Color(0xFFE91E63)
                        PowerUpType.STAR -> Color(0xFFFF9800)
                    }
                    drawCircle(color = color, radius = 10f * scale, center = Offset(screenX, powerUp.y * scale))
                }
            }
        }
        
        val charScreenX = uiState.character.position.x - viewportX
        val charScreenY = uiState.character.position.y * scale
        
        drawCircle(color = Color(0xFF4A90D9), radius = 15f * scale, center = Offset(charScreenX, charScreenY - 10f * scale))
        drawCircle(color = Color(0xFFFFE0B2), radius = 12f * scale, center = Offset(charScreenX, charScreenY - 25f * scale))
        drawCircle(color = Color(0xFF333333), radius = 2f * scale, center = Offset(charScreenX - 4f * scale, charScreenY - 26f * scale))
        drawCircle(color = Color(0xFF333333), radius = 2f * scale, center = Offset(charScreenX + 4f * scale, charScreenY - 26f * scale))
    }
}