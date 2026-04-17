# 好友对战系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现好友对战系统，支持发起/接受/拒绝对战，固定关卡比分数，判定胜负

**Architecture:** MatchRepository数据层 + MatchManager业务层 + MatchScreen UI层

**Tech Stack:** Kotlin, DataStore, Jetpack Compose, Koin DI

---

## 文件结构

```
domain/src/commonMain/kotlin/com/gameway/domain/
├── model/
│   ├── Match.kt               # 新建
│   ├── MatchStatus.kt         # 新建
│   └── MatchResult.kt         # 新建
├── repository/
│   └── MatchRepository.kt     # 新建
├── engine/
│   └── MatchManager.kt        # 新建
├── usecase/
│   ├── CreateMatchUseCase.kt  # 新建
│   ├── AcceptMatchUseCase.kt  # 新建
│   └── SubmitScoreUseCase.kt  # 新建
data/src/main/kotlin/com/gameway/data/
└── repository/
    └── MatchRepositoryImpl.kt    # 新建
presentation/src/main/kotlin/com/gameway/presentation/
├── screens/match/
│   ├── MatchScreen.kt         # 新建
│   ├── MatchViewModel.kt      # 新建
│   ├── CreateMatchDialog.kt   # 新建
│   └── MatchResultDialog.kt   # 新建
└── navigation/
    └── AppNavigation.kt        # 修改
```

---

## Task 1: 创建数据模型和仓储接口

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/MatchStatus.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Match.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/MatchResult.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/repository/MatchRepository.kt`

- [ ] **Step 1: 创建 MatchStatus.kt**

```kotlin
package com.gameway.domain.model

enum class MatchStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COMPLETED,
    EXPIRED
}
```

- [ ] **Step 2: 创建 Match.kt**

```kotlin
package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val id: String,
    val challengerId: String,
    val challengerName: String,
    val challengedId: String,
    val challengedName: String,
    val chapter: Int,
    val level: Int,
    val challengerScore: Int = 0,
    val challengedScore: Int = 0,
    val status: MatchStatus,
    val createdAt: Long,
    val completedAt: Long? = null
)
```

- [ ] **Step 3: 创建 MatchResult.kt**

```kotlin
package com.gameway.domain.model

data class MatchResult(
    val matchId: String,
    val winnerId: String?,
    val winnerName: String?,
    val isDraw: Boolean,
    val challengerScore: Int,
    val challengedScore: Int
)
```

- [ ] **Step 4: 创建 MatchRepository.kt**

```kotlin
package com.gameway.domain.repository

import com.gameway.domain.model.Match

interface MatchRepository {
    suspend fun createMatch(match: Match)
    suspend fun updateMatch(match: Match)
    suspend fun getMatch(matchId: String): Match?
    suspend fun getPendingMatchesForPlayer(playerId: String): List<Match>
    suspend fun getActiveMatches(): List<Match>
    suspend fun getCompletedMatches(limit: Int): List<Match>
    suspend fun deleteMatch(matchId: String)
}
```

- [ ] **Step 5: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/MatchStatus.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Match.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/model/MatchResult.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/repository/MatchRepository.kt
git commit -m "feat(match): add Match models and repository interface"
```

---

## Task 2: 创建仓储实现

**Files:**
- Create: `data/src/main/kotlin/com/gameway/data/repository/MatchRepositoryImpl.kt`

- [ ] **Step 1: 创建 MatchRepositoryImpl.kt**

```kotlin
package com.gameway.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameway.domain.model.Match
import com.gameway.domain.model.MatchStatus
import com.gameway.domain.repository.MatchRepository
import kotlinx.coroutines.flow.first

private val Context.matchDataStore: DataStore<Preferences> by preferencesDataStore(name = "matches")

class MatchRepositoryImpl(
    private val context: Context
) : MatchRepository {

    private object Keys {
        val MATCHES = stringPreferencesKey("matches_list")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.matchDataStore

    override suspend fun createMatch(match: Match) {
        dataStore.edit { prefs ->
            val current = getMatchList(prefs).toMutableList()
            current.add(match)
            prefs[Keys.MATCHES] = Json.encodeToString(current)
        }
    }

    override suspend fun updateMatch(match: Match) {
        dataStore.edit { prefs ->
            val current = getMatchList(prefs).toMutableList()
            val index = current.indexOfFirst { it.id == match.id }
            if (index >= 0) {
                current[index] = match
                prefs[Keys.MATCHES] = Json.encodeToString(current)
            }
        }
    }

    override suspend fun getMatch(matchId: String): Match? {
        return getMatchList(dataStore.data.first()).find { it.id == matchId }
    }

    override suspend fun getPendingMatchesForPlayer(playerId: String): List<Match> {
        return getMatchList(dataStore.data.first()).filter {
            it.status == MatchStatus.PENDING && it.challengedId == playerId
        }
    }

    override suspend fun getActiveMatches(): List<Match> {
        return getMatchList(dataStore.data.first()).filter {
            it.status == MatchStatus.ACCEPTED
        }
    }

    override suspend fun getCompletedMatches(limit: Int): List<Match> {
        return getMatchList(dataStore.data.first())
            .filter { it.status == MatchStatus.COMPLETED }
            .sortedByDescending { it.completedAt }
            .take(limit)
    }

    override suspend fun deleteMatch(matchId: String) {
        dataStore.edit { prefs ->
            val current = getMatchList(prefs).filter { it.id != matchId }
            prefs[Keys.MATCHES] = Json.encodeToString(current)
        }
    }

    private fun getMatchList(prefs: Preferences): List<Match> {
        val json = prefs[Keys.MATCHES] ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add data/src/main/kotlin/com/gameway/data/repository/MatchRepositoryImpl.kt
git commit -m "feat(match): add MatchRepositoryImpl with DataStore"
```

---

## Task 3: 创建MatchManager和UseCase

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/engine/MatchManager.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/CreateMatchUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/AcceptMatchUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/SubmitScoreUseCase.kt`

- [ ] **Step 1: 创建 MatchManager.kt**

```kotlin
package com.gameway.domain.engine

import com.gameway.domain.model.Match
import com.gameway.domain.model.MatchResult
import com.gameway.domain.model.MatchStatus
import com.gameway.domain.repository.MatchRepository
import java.util.UUID

class MatchManager(
    private val matchRepository: MatchRepository
) {
    suspend fun createMatchRequest(
        challengerId: String,
        challengerName: String,
        challengedId: String,
        challengedName: String,
        chapter: Int,
        level: Int
    ): Match {
        val match = Match(
            id = UUID.randomUUID().toString(),
            challengerId = challengerId,
            challengerName = challengerName,
            challengedId = challengedId,
            challengedName = challengedName,
            chapter = chapter,
            level = level,
            status = MatchStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        matchRepository.createMatch(match)
        return match
    }

    suspend fun acceptMatch(matchId: String): Match {
        val match = matchRepository.getMatch(matchId)!!
        val updated = match.copy(status = MatchStatus.ACCEPTED)
        matchRepository.updateMatch(updated)
        return updated
    }

    suspend fun rejectMatch(matchId: String) {
        val match = matchRepository.getMatch(matchId)!!
        val updated = match.copy(status = MatchStatus.REJECTED)
        matchRepository.updateMatch(updated)
    }

    suspend fun submitScore(matchId: String, playerId: String, score: Int): MatchResult {
        val match = matchRepository.getMatch(matchId)!!

        val updated = if (playerId == match.challengerId) {
            match.copy(challengerScore = score)
        } else {
            match.copy(challengedScore = score)
        }

        val finalMatch = matchRepository.updateMatch(updated)

        if (finalMatch.challengerScore > 0 && finalMatch.challengedScore > 0) {
            return calculateResult(finalMatch)
        }

        return MatchResult(
            matchId = matchId,
            winnerId = null,
            winnerName = null,
            isDraw = false,
            challengerScore = finalMatch.challengerScore,
            challengedScore = finalMatch.challengedScore
        )
    }

    private suspend fun calculateResult(match: Match): MatchResult {
        val winnerId: String?
        val winnerName: String?
        val isDraw = match.challengerScore == match.challengedScore

        if (isDraw) {
            winnerId = null
            winnerName = null
        } else if (match.challengerScore > match.challengedScore) {
            winnerId = match.challengerId
            winnerName = match.challengerName
        } else {
            winnerId = match.challengedId
            winnerName = match.challengedName
        }

        val completed = match.copy(
            status = MatchStatus.COMPLETED,
            completedAt = System.currentTimeMillis()
        )
        matchRepository.updateMatch(completed)

        return MatchResult(
            matchId = match.id,
            winnerId = winnerId,
            winnerName = winnerName,
            isDraw = isDraw,
            challengerScore = match.challengerScore,
            challengedScore = match.challengedScore
        )
    }

    suspend fun getPendingMatches(playerId: String): List<Match> {
        return matchRepository.getPendingMatchesForPlayer(playerId)
    }

    suspend fun getActiveMatches(): List<Match> {
        return matchRepository.getActiveMatches()
    }
}
```

- [ ] **Step 2: 创建 CreateMatchUseCase.kt**

```kotlin
package com.gameway.domain.usecase

import com.gameway.domain.engine.MatchManager
import com.gameway.domain.model.Match

class CreateMatchUseCase(
    private val matchManager: MatchManager
) {
    suspend operator fun invoke(
        challengerId: String,
        challengerName: String,
        challengedId: String,
        challengedName: String,
        chapter: Int,
        level: Int
    ): Match {
        return matchManager.createMatchRequest(
            challengerId, challengerName, challengedId, challengedName, chapter, level
        )
    }
}
```

- [ ] **Step 3: 创建 AcceptMatchUseCase.kt**

```kotlin
package com.gameway.domain.usecase

import com.gameway.domain.engine.MatchManager
import com.gameway.domain.model.Match

class AcceptMatchUseCase(
    private val matchManager: MatchManager
) {
    suspend operator fun invoke(matchId: String): Match {
        return matchManager.acceptMatch(matchId)
    }

    suspend fun reject(matchId: String) {
        matchManager.rejectMatch(matchId)
    }
}
```

- [ ] **Step 4: 创建 SubmitScoreUseCase.kt**

```kotlin
package com.gameway.domain.usecase

import com.gameway.domain.engine.MatchManager
import com.gameway.domain.model.MatchResult

class SubmitScoreUseCase(
    private val matchManager: MatchManager
) {
    suspend operator fun invoke(matchId: String, playerId: String, score: Int): MatchResult {
        return matchManager.submitScore(matchId, playerId, score)
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/MatchManager.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/CreateMatchUseCase.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/AcceptMatchUseCase.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/SubmitScoreUseCase.kt
git commit -m "feat(match): add MatchManager and use cases"
```

---

## Task 4: 创建MatchScreen和ViewModel

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchViewModel.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchScreen.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/match/CreateMatchDialog.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchResultDialog.kt`

- [ ] **Step 1: 创建 MatchViewModel.kt**

```kotlin
package com.gameway.presentation.screens.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.model.Friend
import com.gameway.domain.model.Match
import com.gameway.domain.model.MatchResult
import com.gameway.domain.usecase.AcceptMatchUseCase
import com.gameway.domain.usecase.CreateMatchUseCase
import com.gameway.domain.usecase.GetFriendsUseCase
import com.gameway.domain.usecase.SubmitScoreUseCase
import com.gameway.domain.engine.MatchManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MatchUiState(
    val pendingMatches: List<Match> = emptyList(),
    val activeMatches: List<Match> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val currentPlayerId: String = "local_player",
    val currentPlayerName: String = "我",
    val isLoading: Boolean = false,
    val showCreateDialog: Boolean = false,
    val matchResult: MatchResult? = null
)

class MatchViewModel(
    private val matchManager: MatchManager,
    private val createMatchUseCase: CreateMatchUseCase,
    private val acceptMatchUseCase: AcceptMatchUseCase,
    private val submitScoreUseCase: SubmitScoreUseCase,
    private val getFriendsUseCase: GetFriendsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val pending = matchManager.getPendingMatches(_uiState.value.currentPlayerId)
            val active = matchManager.getActiveMatches()
            val friends = getFriendsUseCase()
            _uiState.value = MatchUiState(
                pendingMatches = pending,
                activeMatches = active,
                friends = friends,
                currentPlayerId = _uiState.value.currentPlayerId,
                currentPlayerName = _uiState.value.currentPlayerName,
                isLoading = false
            )
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun createMatch(challengedId: String, challengedName: String, chapter: Int, level: Int) {
        viewModelScope.launch {
            createMatchUseCase(
                challengerId = _uiState.value.currentPlayerId,
                challengerName = _uiState.value.currentPlayerName,
                challengedId = challengedId,
                challengedName = challengedName,
                chapter = chapter,
                level = level
            )
            hideCreateDialog()
            loadMatches()
        }
    }

    fun acceptMatch(matchId: String) {
        viewModelScope.launch {
            acceptMatchUseCase(matchId)
            loadMatches()
        }
    }

    fun rejectMatch(matchId: String) {
        viewModelScope.launch {
            acceptMatchUseCase.reject(matchId)
            loadMatches()
        }
    }

    fun submitScore(matchId: String, score: Int) {
        viewModelScope.launch {
            val result = submitScoreUseCase(matchId, _uiState.value.currentPlayerId, score)
            _uiState.value = _uiState.value.copy(matchResult = result)
            loadMatches()
        }
    }

    fun dismissResult() {
        _uiState.value = _uiState.value.copy(matchResult = null)
    }
}
```

- [ ] **Step 2: 创建 MatchScreen.kt**

```kotlin
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
private fun PendingMatchItem(
    match: Match,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
```

- [ ] **Step 3: 创建 CreateMatchDialog.kt**

```kotlin
package com.gameway.presentation.screens.match

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(friends) { friend ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = if (selectedFriend?.id == friend.id)
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else CardDefaults.cardColors()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(friend.playerName, fontWeight = FontWeight.Bold)
                                    Text("${friend.highScore}分", fontSize = 12.sp)
                                }
                                RadioButton(
                                    selected = selectedFriend?.id == friend.id,
                                    onClick = { selectedFriend = friend }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("选择关卡:", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { chapter = it },
                        label = { Text("章节") },
                        modifier = Modifier.width(80.dp)
                    )
                    Text(" 章 ", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = level,
                        onValueChange = { level = it },
                        label = { Text("关卡") },
                        modifier = Modifier.width(80.dp)
                    )
                    Text(" 关", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Button(
                        onClick = {
                            selectedFriend?.let { friend ->
                                onCreateMatch(friend.id, friend.playerName, chapter.toIntOrNull() ?: 1, level.toIntOrNull() ?: 1)
                            }
                        },
                        enabled = selectedFriend != null
                    ) { Text("发起") }
                }
            }
        }
    }
}
```

- [ ] **Step 4: 创建 MatchResultDialog.kt**

```kotlin
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
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Text(
                    text = "你: ${result.challengerScore}分 vs ${result.winnerName ?: "对手"}: ${result.challengedScore}分",
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss) { Text("关闭") }
                    Button(onClick = onDismiss) { Text("再来一局") }
                }
            }
        }
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchViewModel.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchScreen.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/match/CreateMatchDialog.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchResultDialog.kt
git commit -m "feat(match): add MatchScreen, ViewModel, and dialogs"
```

---

## Task 5: 集成到导航

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/di/AppModule.kt`
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt`

- [ ] **Step 1: 添加Match路由**

Add to Screen sealed class:
```kotlin
object Match : Screen()
```

Add navigation route:
```kotlin
composable(Screen.Match.route) {
    MatchScreen(
        viewModel = matchViewModel,
        onBack = { navController.popBackStack() }
    )
}
```

**Step 2: 在Koin模块注册**

```kotlin
factory { MatchManager(get()) }
factory { CreateMatchUseCase(get()) }
factory { AcceptMatchUseCase(get()) }
factory { SubmitScoreUseCase(get()) }
factory { MatchViewModel(get(), get(), get(), get(), get()) }
```

Also add MatchRepository:
```kotlin
factory { MatchRepositoryImpl(get()) }
```

**Step 3: 在MainMenuScreen添"对战"按钮**

Add a button that navigates to Match screen:
```kotlin
Button(onClick = { onNavigateToMatch() }) {
    Text("对战")
}
```

Add the callback to MainMenuScreen function signature.

**Step 4: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt
git add presentation/src/main/kotlin/com/gameway/presentation/di/AppModule.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt
git commit -m "feat(match): integrate MatchScreen into navigation"
```

---

## 实现顺序

1. Task 1: 数据模型和仓储接口
2. Task 2: 仓储实现
3. Task 3: MatchManager和UseCase
4. Task 4: MatchScreen、ViewModel、Dialogs
5. Task 5: 集成到导航

---

## 验收标准

1. 可以发起对战请求（选择关卡和对手）
2. 可以接收/拒绝对战
3. 对战进行中显示双方分数
4. 固定关卡对比，判定胜负
5. 显示对战结果对话框
6. 数据持久化存储

---

**Plan complete.**