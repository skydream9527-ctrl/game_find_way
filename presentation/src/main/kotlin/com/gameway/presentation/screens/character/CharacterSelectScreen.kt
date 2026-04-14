package com.gameway.presentation.screens.character

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameway.domain.model.CharacterConfig
import com.gameway.domain.model.CharacterType

@Composable
fun CharacterSelectScreen(
    currentCharacter: CharacterType,
    onCharacterSelected: (CharacterType) -> Unit,
    onBack: () -> Unit
) {
    val characters = CharacterConfig.getAll()
    var selectedType by remember { mutableStateOf(currentCharacter) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF4A148C)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "选择你的角色",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                characters.forEach { config ->
                    CharacterCard(
                        config = config,
                        isSelected = selectedType == config.type,
                        onClick = { selectedType = config.type }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AttributeComparisonPanel(selectedType)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("返回")
                }
                
                Button(
                    onClick = { onCharacterSelected(selectedType) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("确认选择")
                }
            }
        }
    }
}

@Composable
fun CharacterCard(
    config: CharacterConfig,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(config.emoji, fontSize = 48.sp)
            Text(config.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("⚡", fontSize = 14.sp)
                Text("${(config.moveSpeedMultiplier * 100).toInt()}%", fontSize = 12.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("⬆", fontSize = 14.sp)
                Text("${(config.jumpPowerMultiplier * 100).toInt()}%", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AttributeComparisonPanel(selectedType: CharacterType) {
    val config = CharacterConfig.getByType(selectedType)
    
    Card(
        modifier = Modifier.fillMaxWidth(0.8f),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${config.emoji} ${config.name} 属性",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("速度", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        "${(config.moveSpeedMultiplier * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (config.moveSpeedMultiplier >= 1.0f) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("跳力", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        "${(config.jumpPowerMultiplier * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (config.jumpPowerMultiplier >= 1.0f) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}