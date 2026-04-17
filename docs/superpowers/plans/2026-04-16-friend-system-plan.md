# 好友系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现好友系统，支持添加/删除好友，查看好友成绩，支持ID搜索和排行榜导入

**Architecture:** FriendRepository数据层 + UseCase业务层 + FriendScreen UI层（含AddFriendDialog）

**Tech Stack:** Kotlin, DataStore, Jetpack Compose, Koin DI

---

## 文件结构

```
domain/src/commonMain/kotlin/com/gameway/domain/
├── model/
│   └── Friend.kt              # 新建
├── repository/
│   └── FriendRepository.kt    # 新建
├── usecase/
│   ├── AddFriendUseCase.kt    # 新建
│   ├── GetFriendsUseCase.kt   # 新建
│   └── RemoveFriendUseCase.kt # 新建
data/src/main/kotlin/com/gameway/data/
└── repository/
    └── FriendRepositoryImpl.kt    # 新建
presentation/src/main/kotlin/com/gameway/presentation/
├── screens/friend/
│   ├── FriendScreen.kt        # 新建
│   ├── FriendViewModel.kt     # 新建
│   └── AddFriendDialog.kt      # 新建
└── navigation/
    └── AppNavigation.kt        # 修改
```

---

## Task 1: 创建数据模型和仓储接口

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Friend.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/repository/FriendRepository.kt`

- [ ] **Step 1: 创建 Friend.kt**

```kotlin
package com.gameway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    val id: String,
    val playerName: String,
    val playerId: String,
    val highScore: Int,
    val highestChapter: Int,
    val highestLevel: Int,
    val addedAt: Long,
    val note: String = ""
)
```

- [ ] **Step 2: 创建 FriendRepository.kt**

```kotlin
package com.gameway.domain.repository

import com.gameway.domain.model.Friend

interface FriendRepository {
    suspend fun addFriend(friend: Friend)
    suspend fun removeFriend(friendId: String)
    suspend fun updateFriend(friend: Friend)
    suspend fun getFriends(): List<Friend>
    suspend fun findFriendByPlayerId(playerId: String): Friend?
    suspend fun searchFriends(query: String): List<Friend>
}
```

- [ ] **Step 3: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Friend.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/repository/FriendRepository.kt
git commit -m "feat(friend): add Friend model and repository interface"
```

---

## Task 2: 创建仓储实现

**Files:**
- Create: `data/src/main/kotlin/com/gameway/data/repository/FriendRepositoryImpl.kt`

- [ ] **Step 1: 创建 FriendRepositoryImpl.kt**

```kotlin
package com.gameway.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameway.domain.model.Friend
import com.gameway.domain.repository.FriendRepository
import kotlinx.coroutines.flow.first

private val Context.friendDataStore: DataStore<Preferences> by preferencesDataStore(name = "friends")

class FriendRepositoryImpl(
    private val context: Context
) : FriendRepository {

    private object Keys {
        val FRIENDS = stringPreferencesKey("friends_list")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.friendDataStore

    override suspend fun addFriend(friend: Friend) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).toMutableList()
            if (current.none { it.playerId == friend.playerId }) {
                current.add(friend)
                prefs[Keys.FRIENDS] = Json.encodeToString(current)
            }
        }
    }

    override suspend fun removeFriend(friendId: String) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).filter { it.id != friendId }
            prefs[Keys.FRIENDS] = Json.encodeToString(current)
        }
    }

    override suspend fun updateFriend(friend: Friend) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).toMutableList()
            val index = current.indexOfFirst { it.id == friend.id }
            if (index >= 0) {
                current[index] = friend
                prefs[Keys.FRIENDS] = Json.encodeToString(current)
            }
        }
    }

    override suspend fun getFriends(): List<Friend> {
        return dataStore.data.first().let { getFriendList(it) }
    }

    override suspend fun findFriendByPlayerId(playerId: String): Friend? {
        return getFriends().find { it.playerId == playerId }
    }

    override suspend fun searchFriends(query: String): List<Friend> {
        return getFriends().filter {
            it.playerName.contains(query, ignoreCase = true) ||
            it.playerId.contains(query, ignoreCase = true)
        }
    }

    private fun getFriendList(prefs: Preferences): List<Friend> {
        val json = prefs[Keys.FRIENDS] ?: return emptyList()
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
git add data/src/main/kotlin/com/gameway/data/repository/FriendRepositoryImpl.kt
git commit -m "feat(friend): add FriendRepositoryImpl with DataStore"
```

---

## Task 3: 创建UseCase

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/AddFriendUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetFriendsUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/RemoveFriendUseCase.kt`

- [ ] **Step 1: 创建 AddFriendUseCase.kt**

```kotlin
package com.gameway.domain.usecase

import com.gameway.domain.model.Friend
import com.gameway.domain.repository.FriendRepository
import java.util.UUID

class AddFriendUseCase(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(
        playerName: String,
        playerId: String,
        highScore: Int = 0,
        highestChapter: Int = 0,
        highestLevel: Int = 0,
        note: String = ""
    ): Friend {
        val friend = Friend(
            id = UUID.randomUUID().toString(),
            playerName = playerName,
            playerId = playerId,
            highScore = highScore,
            highestChapter = highestChapter,
            highestLevel = highestLevel,
            addedAt = System.currentTimeMillis(),
            note = note
        )
        repository.addFriend(friend)
        return friend
    }
}
```

- [ ] **Step 2: 创建 GetFriendsUseCase.kt**

```kotlin
package com.gameway.domain.usecase

import com.gameway.domain.model.Friend
import com.gameway.domain.repository.FriendRepository

class GetFriendsUseCase(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(): List<Friend> {
        return repository.getFriends()
    }

    suspend fun search(query: String): List<Friend> {
        return repository.searchFriends(query)
    }
}
```

- [ ] **Step 3: 创建 RemoveFriendUseCase.kt**

```kotlin
package com.gameway.domain.usecase

import com.gameway.domain.repository.FriendRepository

class RemoveFriendUseCase(
    private val repository: FriendRepository
) {
    suspend operator fun invoke(friendId: String) {
        repository.removeFriend(friendId)
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/AddFriendUseCase.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetFriendsUseCase.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/RemoveFriendUseCase.kt
git commit -m "feat(friend): add friend use cases"
```

---

## Task 4: 创建FriendScreen和ViewModel

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/friend/FriendViewModel.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/friend/FriendScreen.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/friend/AddFriendDialog.kt`

- [ ] **Step 1: 创建 FriendViewModel.kt**

```kotlin
package com.gameway.presentation.screens.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.model.Friend
import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.usecase.AddFriendUseCase
import com.gameway.domain.usecase.GetFriendsUseCase
import com.gameway.domain.usecase.GetLeaderboardUseCase
import com.gameway.domain.usecase.RemoveFriendUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FriendUiState(
    val friends: List<Friend> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val leaderboardEntries: List<LeaderboardEntry> = emptyList()
)

class FriendViewModel(
    private val getFriendsUseCase: GetFriendsUseCase,
    private val addFriendUseCase: AddFriendUseCase,
    private val removeFriendUseCase: RemoveFriendUseCase,
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val friends = getFriendsUseCase()
            val leaderboard = getLeaderboardUseCase(20)
            _uiState.value = FriendUiState(
                friends = friends,
                leaderboardEntries = leaderboard,
                isLoading = false
            )
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun addFriend(playerName: String, playerId: String, note: String = "") {
        viewModelScope.launch {
            addFriendUseCase(
                playerName = playerName,
                playerId = playerId,
                note = note
            )
            loadFriends()
            hideAddDialog()
        }
    }

    fun addFriendFromLeaderboard(entry: LeaderboardEntry) {
        viewModelScope.launch {
            addFriendUseCase(
                playerName = entry.playerName,
                playerId = entry.id,
                highScore = entry.highScore,
                highestChapter = entry.highestChapter,
                highestLevel = entry.highestLevel
            )
            loadFriends()
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            removeFriendUseCase(friendId)
            loadFriends()
        }
    }
}
```

- [ ] **Step 2: 创建 FriendScreen.kt**

```kotlin
package com.gameway.presentation.screens.friend

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
import com.gameway.domain.model.Friend

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScreen(
    viewModel: FriendViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("好友") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "添加好友")
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
            } else if (uiState.friends.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("还没有好友，点击右上角添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.friends) { friend ->
                        FriendItem(
                            friend = friend,
                            onRemove = { viewModel.removeFriend(friend.id) }
                        )
                    }
                }
            }
        }

        if (uiState.showAddDialog) {
            AddFriendDialog(
                leaderboardEntries = uiState.leaderboardEntries,
                onDismiss = { viewModel.hideAddDialog() },
                onAddBySearch = { name, id -> viewModel.addFriend(name, id) },
                onAddFromLeaderboard = { entry -> viewModel.addFriendFromLeaderboard(entry) }
            )
        }
    }
}

@Composable
private fun FriendItem(
    friend: Friend,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(friend.playerName, fontWeight = FontWeight.Bold)
                Text("${friend.highScore}分 第${friend.highestChapter}章", fontSize = 12.sp)
                if (friend.note.isNotEmpty()) {
                    Text("备注: ${friend.note}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
```

- [ ] **Step 3: 创建 AddFriendDialog.kt**

```kotlin
package com.gameway.presentation.screens.friend

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("取消")
                }
            }
        }
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/friend/FriendViewModel.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/friend/FriendScreen.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/friend/AddFriendDialog.kt
git commit -m "feat(friend): add FriendScreen, ViewModel, and AddFriendDialog"
```

---

## Task 5: 集成到导航

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`

- [ ] **Step 1: 添加Friend路由**

Add to Screen sealed class:
```kotlin
object Friend : Screen()
```

Add navigation route:
```kotlin
composable(Screen.Friend.route) {
    FriendScreen(
        viewModel = friendViewModel,
        onBack = { navController.popBackStack() }
    )
}
```

Also add "好友" button to MainMenuScreen.

- [ ] **Step 2: 在Koin模块注册**

```kotlin
factory { FriendViewModel(get(), get(), get(), get()) }
```

- [ ] **Step 3: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt
git add presentation/src/main/kotlin/com/gameway/presentation/di/AppModule.kt
git commit -m "feat(friend): integrate FriendScreen into navigation"
```

---

## 实现顺序

1. Task 1: 数据模型和仓储接口
2. Task 2: 仓储实现
3. Task 3: UseCase
4. Task 4: FriendScreen、ViewModel、AddFriendDialog
5. Task 5: 集成到导航

---

## 验收标准

1. 可以通过输入ID/游戏名添加好友
2. 可以从排行榜导入好友
3. 可以删除好友
4. 好友数据显示最高分和关卡进度
5. 支持备注名
6. 数据持久化存储

---

**Plan complete.**