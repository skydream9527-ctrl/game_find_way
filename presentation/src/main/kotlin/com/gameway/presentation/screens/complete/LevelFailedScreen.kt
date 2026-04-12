package com.gameway.presentation.screens.complete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LevelFailedScreen(reason: String, onRestart: () -> Unit, onBackToMenu: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(0.8f).background(Color(0xFFB71C1C), RoundedCornerShape(16.dp)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "💀", fontSize = 48.sp)
            Text(text = "游戏失败", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 16.dp))
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = reason, fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) { Text("重新开始", fontSize = 16.sp) }
            
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBackToMenu, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) { Text("返回主页", fontSize = 16.sp) }
        }
    }
}