package com.gameway.presentation.screens.match

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameway.domain.model.Match

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    viewModel: MatchViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("对战") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "发起对战")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.pendingMatches.isNotEmpty()) {
                        item {
                            Text("待接受", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(uiState.pendingMatches) { match ->
                            PendingMatchItem(
                                match = match,
                                onAccept = { viewModel.acceptMatch(match.id) },
                                onReject = { viewModel.rejectMatch(match.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (uiState.activeMatches.isNotEmpty()) {
                        item {
                            Text("进行中", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(uiState.activeMatches) { match ->
                            ActiveMatchItem(match = match, currentPlayerId = uiState.currentPlayerId)
                        }
                    }

                    if (uiState.pendingMatches.isEmpty() && uiState.activeMatches.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("暂无对战，点击右上角发起", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            CreateMatchDialog(
                friends = uiState.friends,
                onDismiss = { viewModel.hideCreateDialog() },
                onCreateMatch = { friendId, friendName, chapter, level ->
                    viewModel.createMatch(friendId, friendName, chapter, level)
                }
            )
        }

        uiState.matchResult?.let { result ->
            MatchResultDialog(
                result = result,
                currentPlayerId = uiState.currentPlayerId,
                onDismiss = { viewModel.dismissResult() }
            )
        }
    }
}

@Composable
private fun PendingMatchItem(match: Match, onAccept: () -> Unit, onReject: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("${match.challengerName} 邀请你", fontWeight = FontWeight.Bold)
                Text("第${match.chapter}章-${match.level}关", fontSize = 12.sp)
            }
            Row {
                TextButton(onClick = onAccept) { Text("接受") }
                TextButton(onClick = onReject) { Text("拒绝") }
            }
        }
    }
}

@Composable
private fun ActiveMatchItem(match: Match, currentPlayerId: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("vs ${if (currentPlayerId == match.challengerId) match.challengedName else match.challengerName}", fontWeight = FontWeight.Bold)
            Text("第${match.chapter}章-${match.level}关", fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            val myScore = if (currentPlayerId == match.challengerId) match.challengerScore else match.challengedScore
            val opponentScore = if (currentPlayerId == match.challengerId) match.challengedScore else match.challengerScore
            Text("你: ${myScore}分 vs 对手: ${opponentScore}分", fontSize = 14.sp)
        }
    }
}
