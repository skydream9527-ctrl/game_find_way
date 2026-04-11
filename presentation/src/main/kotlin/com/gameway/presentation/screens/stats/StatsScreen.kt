package com.gameway.presentation.screens.stats

import androidx.compose.foundation.background
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
fun StatsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A237E)).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(80.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(text = "🧙", fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "冒险者", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "等级: 5 | 金币: 1280", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        StatBar("❤️ 生命值", 0.8f, Color(0xFFF44336))
        StatBar("🦘 跳跃力", 0.6f, Color(0xFF2196F3))
        StatBar("🏃 移动速度", 0.7f, Color(0xFF4CAF50))
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "已解锁技能", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
        
        Row {
            Text(text = "✅ 二段跳", modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp), color = Color(0xFF2E7D32), fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "🔒 三段跳", modifier = Modifier.background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp), color = Color(0xFFE65100), fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) { Text("返回", fontSize = 16.sp) }
    }
}

@Composable
private fun StatBar(label: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 4.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))) {
            Box(modifier = Modifier.fillMaxWidth(progress).height(8.dp).background(color, RoundedCornerShape(4.dp)))
        }
    }
}