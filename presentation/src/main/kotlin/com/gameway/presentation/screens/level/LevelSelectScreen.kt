package com.gameway.presentation.screens.level

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LevelSelectScreen(chapterId: Int, onLevelSelected: (Int) -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A237E)).padding(16.dp)) {
        Text(text = "章节 $chapterId - 选择关卡", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
        
        LazyVerticalGrid(columns = GridCells.Fixed(5), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items((1..10).toList()) { levelNumber ->
                LevelCard(levelNumber = levelNumber, onClick = { onLevelSelected(levelNumber) })
            }
        }
    }
}

@Composable
private fun LevelCard(levelNumber: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(60.dp)
            .background(Color(0xFF3F51B5), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "关卡 $levelNumber", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}