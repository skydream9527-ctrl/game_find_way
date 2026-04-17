package com.gameway.presentation.screens.match

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gameway.domain.model.Friend

@Composable
fun CreateMatchDialog(
    friends: List<Friend>,
    onDismiss: () -> Unit,
    onCreateMatch: (friendId: String, friendName: String, chapter: Int, level: Int) -> Unit
) {
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var chapter by remember { mutableStateOf("1") }
    var level by remember { mutableStateOf("1") }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("发起对战", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("选择对手:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(friends) { friend ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (selectedFriend?.id == friend.id)
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else CardDefaults.cardColors()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(friend.playerName, fontWeight = FontWeight.Bold)
                                    Text("${friend.highScore}分", fontSize = 12.sp)
                                }
                                RadioButton(selected = selectedFriend?.id == friend.id, onClick = { selectedFriend = friend })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("选择关卡:", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = chapter, onValueChange = { chapter = it }, label = { Text("章节") }, modifier = Modifier.width(80.dp))
                    Text(" 章 ", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text("关卡") }, modifier = Modifier.width(80.dp))
                    Text(" 关", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(onClick = { selectedFriend?.let { onCreateMatch(it.id, it.playerName, chapter.toIntOrNull() ?: 1, level.toIntOrNull() ?: 1) } }, enabled = selectedFriend != null) { Text("发起") }
                }
            }
        }
    }
}
