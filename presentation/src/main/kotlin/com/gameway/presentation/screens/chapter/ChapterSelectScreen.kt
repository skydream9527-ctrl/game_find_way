package com.gameway.presentation.screens.chapter

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
import com.gameway.domain.model.Chapter

@Composable
fun ChapterSelectScreen(onChapterSelected: (Int) -> Unit, onBack: () -> Unit) {
    val chapters = Chapter.getAllChapters()
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A237E)).padding(16.dp)) {
        Text(text = "选择章节", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
        
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(chapters) { chapter ->
                ChapterCard(chapter = chapter, onClick = { onChapterSelected(chapter.id) })
            }
        }
    }
}

@Composable
private fun ChapterCard(chapter: Chapter, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(120.dp)
            .background(Color(chapter.theme.primaryColor), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = chapter.emoji, fontSize = 32.sp)
            Text(text = chapter.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "${chapter.completedLevels}/${chapter.totalLevels}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}