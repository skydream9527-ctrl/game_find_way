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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gameway.core.GameConstants
import com.gameway.domain.model.Chapter
import com.gameway.domain.model.CharacterState
import com.gameway.domain.model.CharacterType
import com.gameway.domain.model.PowerUpType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
        
        val CHARACTER_SCREEN_X = canvasWidth * 0.15f
        val CHARACTER_SCREEN_Y = canvasHeight * 0.5f
        
        val viewportX = uiState.character.position.x - (CHARACTER_SCREEN_X - offsetX) / scale
        
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
        
        val charScreenX = CHARACTER_SCREEN_X
        val charScreenY = CHARACTER_SCREEN_Y
        
        drawCharacterWithAnimation(
            character = uiState.character,
            screenX = charScreenX,
            screenY = charScreenY,
            scale = scale,
            animationFrame = uiState.animationFrame
        )
    }
}

private fun calculateLegEnd(start: Offset, angle: Float, length: Float): Offset {
    val radians = (angle * PI / 180f).toFloat()
    return Offset(
        start.x + sin(radians).toFloat() * length,
        start.y + cos(radians).toFloat() * length
    )
}

private fun DrawScope.drawCharacterWithAnimation(
    character: com.gameway.domain.model.Character,
    screenX: Float,
    screenY: Float,
    scale: Float,
    animationFrame: Int
) {
    val legAngle = if (character.state == CharacterState.RUNNING) {
        val cycle = (animationFrame % 8) / 8f
        (sin(cycle * PI * 2) * 15f).toFloat()
    } else {
        0f
    }
    
    val bodyColor = when (character.type) {
        CharacterType.CAT -> Color(0xFFFFB74D)
        CharacterType.DOG -> Color(0xFF8D6E63)
        CharacterType.HORSE -> Color(0xFF9E9E9E)
    }
    
    val headColor = when (character.type) {
        CharacterType.CAT -> Color(0xFFFFE0B2)
        CharacterType.DOG -> Color(0xFFD7CCC8)
        CharacterType.HORSE -> Color(0xFFBDBDBD)
    }
    
    val legColor = Color(0xFF333333)
    
    drawCircle(bodyColor, radius = 20f * scale, center = Offset(screenX, screenY - 15f * scale))
    drawCircle(headColor, radius = 15f * scale, center = Offset(screenX, screenY - 35f * scale))
    drawCircle(Color.Black, radius = 3f * scale, center = Offset(screenX - 6f * scale, screenY - 38f * scale))
    drawCircle(Color.Black, radius = 3f * scale, center = Offset(screenX + 6f * scale, screenY - 38f * scale))
    
    val leftLegStart = Offset(screenX - 8f * scale, screenY)
    val leftLegEnd = calculateLegEnd(leftLegStart, legAngle, 25f * scale)
    drawLine(legColor, leftLegStart, leftLegEnd, strokeWidth = 6f * scale)
    
    val rightLegStart = Offset(screenX + 8f * scale, screenY)
    val rightLegEnd = calculateLegEnd(rightLegStart, -legAngle, 25f * scale)
    drawLine(legColor, rightLegStart, rightLegEnd, strokeWidth = 6f * scale)
}