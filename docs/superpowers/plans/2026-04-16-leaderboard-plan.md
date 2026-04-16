# 排行榜系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现本地排行榜，记录玩家最高分、关卡进度、游玩时长、死亡次数

**Architecture:** LeaderboardRepository数据层 + UseCase业务层 + LeaderboardScreen UI层

**Tech Stack:** Kotlin, DataStore, Jetpack Compose, Koin DI

---

## 文件结构

```
domain/src/commonMain/kotlin/com/gameway/domain/
├── model/
│   └── LeaderboardEntry.kt        # 新建
├── repository/
│   └── LeaderboardRepository.kt   # 新建
├── usecase/
│   ├── SaveScoreUseCase.kt       # 新建
│   └── GetLeaderboardUseCase.kt   # 新建
data/src/main/kotlin/com/gameway/data/
└── repository/
    └── LeaderboardRepositoryImpl.kt  # 新建
presentation/src/main/kotlin/com/gameway/presentation/
├── screens/leaderboard/
│   ├── LeaderboardScreen.kt      # 新建
│   └── LeaderboardViewModel.kt   # 新建
└── navigation/
    └── AppNavigation.kt          # 修改
```

---

## Task 1: 创建数据模型和仓储接口

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/LeaderboardEntry.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/repository/LeaderboardRepository.kt`

- [ ] **Step 1: 创建 LeaderboardEntry.kt**

```kotlin
package com.gameway.domain.model

data class LeaderboardEntry(
    val id: String,
    val playerName: String,
    val highScore: Int,
    val highestChapter: Int,
    val highestLevel: Int,
    val totalPlayTime: Long,
    val totalDeaths: Int,
    val timestamp: Long
)
```

- [ ] **Step 2: 创建 LeaderboardRepository.kt**

```kotlin
package com.gameway.domain.repository

import com.gameway.domain.model.LeaderboardEntry

interface LeaderboardRepository {
    suspend fun saveScore(entry: LeaderboardEntry)
    suspend fun getLeaderboard(limit: Int = 10): List<LeaderboardEntry>
    suspend fun getPersonalBest(): LeaderboardEntry?
    suspend fun updatePlayTime(additionalTime: Long)
    suspend fun incrementDeaths()
    suspend fun clearLeaderboard()
}
```

- [ ] **Step 3: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/LeaderboardEntry.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/repository/LeaderboardRepository.kt
git commit -m "feat(leaderboard): add LeaderboardEntry and repository interface"
```

---

## Task 2: 创建仓储实现

**Files:**
- Create: `data/src/main/kotlin/com/gameway/data/repository/LeaderboardRepositoryImpl.kt`

- [ ] **Step 1: 创建 LeaderboardRepositoryImpl.kt**

```kotlin
package com.gameway.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.repository.LeaderboardRepository
import kotlinx.coroutines.flow.first

private val Context.leaderboardDataStore: DataStore<Preferences> by preferencesDataStore(name = "leaderboard")

class LeaderboardRepositoryImpl(
    private val context: Context
) : LeaderboardRepository {

    private object Keys {
        val LEADERBOARD = stringPreferencesKey("leaderboard_entries")
        val PERSONAL_BEST = stringPreferencesKey("personal_best")
        val TOTAL_PLAY_TIME = longPreferencesKey("total_play_time")
        val TOTAL_DEATHS = intPreferencesKey("total_deaths")
        val HIGHEST_CHAPTER = intPreferencesKey("highest_chapter")
        val HIGHEST_LEVEL = intPreferencesKey("highest_level")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.leaderboardDataStore

    override suspend fun saveScore(entry: LeaderboardEntry) {
        dataStore.edit { prefs ->
            val currentEntries = getLeaderboardFromPrefs(prefs).toMutableList()
            currentEntries.add(entry)
            currentEntries.sortByDescending { it.highScore }
            val top10 = currentEntries.take(10)

            prefs[Keys.LEADERBOARD] = Json.encodeToString(top10)

            val currentBest = getPersonalBestFromPrefs(prefs)
            if (currentBest == null || entry.highScore > currentBest.highScore) {
                prefs[Keys.PERSONAL_BEST] = Json.encodeToString(entry)
            }

            if (entry.highestChapter > (prefs[Keys.HIGHEST_CHAPTER] ?: 0)) {
                prefs[Keys.HIGHEST_CHAPTER] = entry.highestChapter
            }
            if (entry.highestLevel > (prefs[Keys.HIGHEST_LEVEL] ?: 0)) {
                prefs[Keys.HIGHEST_LEVEL] = entry.highestLevel
            }
        }
    }

    override suspend fun getLeaderboard(limit: Int): List<LeaderboardEntry> {
        return dataStore.data.first().let { prefs ->
            getLeaderboardFromPrefs(prefs).take(limit)
        }
    }

    override suspend fun getPersonalBest(): LeaderboardEntry? {
        return dataStore.data.first().let { prefs ->
            getPersonalBestFromPrefs(prefs)
        }
    }

    override suspend fun updatePlayTime(additionalTime: Long) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.TOTAL_PLAY_TIME] ?: 0L
            prefs[Keys.TOTAL_PLAY_TIME] = current + additionalTime
        }
    }

    override suspend fun incrementDeaths() {
        dataStore.edit { prefs ->
            val current = prefs[Keys.TOTAL_DEATHS] ?: 0
            prefs[Keys.TOTAL_DEATHS] = current + 1
        }
    }

    override suspend fun clearLeaderboard() {
        dataStore.edit { it.clear() }
    }

    private fun getLeaderboardFromPrefs(prefs: Preferences): List<LeaderboardEntry> {
        val json = prefs[Keys.LEADERBOARD] ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getPersonalBestFromPrefs(prefs: Preferences): LeaderboardEntry? {
        val json = prefs[Keys.PERSONAL_BEST] ?: return null
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            null
        }
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add data/src/main/kotlin/com/gameway/data/repository/LeaderboardRepositoryImpl.kt
git commit -m "feat(leaderboard): add LeaderboardRepositoryImpl with DataStore"
```

---

## Task 3: 创建UseCase

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/SaveScoreUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetLeaderboardUseCase.kt`

- [ ] **Step 1: 创建 SaveScoreUseCase.kt**

```kotlin
package com.gameway.domain.usecase

import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.repository.LeaderboardRepository
import java.util.UUID

class SaveScoreUseCase(
    private val repository: LeaderboardRepository
) {
    suspend operator fun invoke(
        score: Int,
        chapter: Int,
        level: Int,
        playerName: String = "Player"
    ): LeaderboardEntry {
        val entry = LeaderboardEntry(
            id = UUID.randomUUID().toString(),
            playerName = playerName,
            highScore = score,
            highestChapter = chapter,
            highestLevel = level,
            totalPlayTime = 0L,
            totalDeaths = 0,
            timestamp = System.currentTimeMillis()
        )
        repository.saveScore(entry)
        return entry
    }
}
```

- [ ] **Step 2: 创建 GetLeaderboardUseCase.kt**

```kotlin
package com.gameway.domain.usecase

import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.repository.LeaderboardRepository

class GetLeaderboardUseCase(
    private val repository: LeaderboardRepository
) {
    suspend operator fun invoke(limit: Int = 10): List<LeaderboardEntry> {
        return repository.getLeaderboard(limit)
    }

    suspend fun getPersonalBest(): LeaderboardEntry? {
        return repository.getPersonalBest()
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/SaveScoreUseCase.kt
git add domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetLeaderboardUseCase.kt
git commit -m "feat(leaderboard): add use cases"
```

---

## Task 4: 创建LeaderboardScreen和ViewModel

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/leaderboard/LeaderboardViewModel.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/leaderboard/LeaderboardScreen.kt`

- [ ] **Step 1: 创建 LeaderboardViewModel.kt**

```kotlin
package com.gameway.presentation.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.model.LeaderboardEntry
import com.gameway.domain.usecase.GetLeaderboardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val personalBest: LeaderboardEntry? = null,
    val isLoading: Boolean = false
)

class LeaderboardViewModel(
    private val getLeaderboardUseCase: GetLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val entries = getLeaderboardUseCase(10)
            val personalBest = getLeaderboardUseCase.getPersonalBest()
            _uiState.value = LeaderboardUiState(
                entries = entries,
                personalBest = personalBest,
                isLoading = false
            )
        }
    }
}
```

- [ ] **Step 2: 创建 LeaderboardScreen.kt**

```kotlin
package com.gameway.presentation.screens.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameway.domain.model.LeaderboardEntry

@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "排行榜",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(uiState.entries) { index, entry ->
                    LeaderboardItem(index + 1, entry)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.personalBest?.let { best ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("我的最佳", fontWeight = FontWeight.Bold)
                        Text("${best.highScore}分 (第${best.highestChapter}章)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("返回")
            }
        }
    }
}

@Composable
private fun LeaderboardItem(rank: Int, entry: LeaderboardEntry) {
    val medalEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "$rank."
    }

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
            Text(medalEmoji, fontSize = 24.sp)
            Column {
                Text(entry.playerName, fontWeight = FontWeight.Bold)
                Text("第${entry.highestChapter}章-${entry.highestLevel}关", fontSize = 12.sp)
            }
            Text("${entry.highScore}分", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/leaderboard/LeaderboardViewModel.kt
git add presentation/src/main/kotlin/com/gameway/presentation/screens/leaderboard/LeaderboardScreen.kt
git commit -m "feat(leaderboard): add LeaderboardScreen and ViewModel"
```

---

## Task 5: 集成到GameEngine和导航

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`

- [ ] **Step 1: 在GameEngine完成时保存分数**

In the `update` method when `GameState.Completed` is triggered:
```kotlin
// After calculating score
saveScoreUseCase(score, chapter, levelNumber)
```

- [ ] **Step 2: 更新导航添加Leaderboard路由**

Add to Screen sealed class:
```kotlin
object Leaderboard : Screen()
```

Add navigation route:
```kotlin
composable(Screen.Leaderboard.route) {
    LeaderboardScreen(
        viewModel = leaderboardViewModel,
        onBack = { navController.popBackStack() }
    )
}
```

- [ ] **Step 3: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt
git add presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt
git commit -m "feat(leaderboard): integrate into GameEngine and navigation"
```

---

## 实现顺序

1. Task 1: 数据模型和仓储接口
2. Task 2: 仓储实现
3. Task 3: UseCase
4. Task 4: LeaderboardScreen和ViewModel
5. Task 5: 集成到GameEngine和导航

---

## 验收标准

1. 每次过关时自动保存成绩
2. 排行榜显示前10名最高分
3. 显示个人最佳成绩
4. 按分数降序排列
5. 数据持久化存储

---

**Plan complete.**