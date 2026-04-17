package com.gameway.presentation.screens.match

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gameway.domain.model.MatchResult

@Composable
fun MatchResultDialog(
    result: MatchResult,
    currentPlayerId: String,
    onDismiss: () -> Unit
) {
    val isWinner = result.winnerId == currentPlayerId
    val isDraw = result.isDraw

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when {
                        isDraw -> "平局!"
                        isWinner -> "🏆 你赢了!"
                        else -> "你输了"
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("你: ${result.challengerScore}分 vs ${result.winnerName ?: "对手"}: ${result.challengedScore}分", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss) { Text("关闭") }
                    Button(onClick = onDismiss) { Text("再来一局") }
                }
            }
        }
    }
}
