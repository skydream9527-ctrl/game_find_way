package com.gameway.presentation.screens.friend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gameway.domain.model.LeaderboardEntry

@Composable
fun AddFriendDialog(
    leaderboardEntries: List<LeaderboardEntry>,
    onDismiss: () -> Unit,
    onAddBySearch: (playerName: String, playerId: String) -> Unit,
    onAddFromLeaderboard: (LeaderboardEntry) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var playerName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("添加好友", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("玩家名") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("玩家ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onAddBySearch(playerName, searchQuery) },
                    modifier = Modifier.align(Alignment.End),
                    enabled = playerName.isNotBlank() && searchQuery.isNotBlank()
                ) {
                    Text("添加")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("从排行榜添加:", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(leaderboardEntries) { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(entry.playerName)
                                Text("${entry.highScore}分", style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { onAddFromLeaderboard(entry) }) {
                                Text("添加")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("取消")
                }
            }
        }
    }
}
