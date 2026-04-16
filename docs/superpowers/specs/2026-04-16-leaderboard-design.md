# 排行榜系统设计文档

**日期:** 2026-04-16
**项目:** FindWay Game
**目标:** 实现本地排行榜，记录玩家成绩

---

## 1. 概述

### 当前状态
- 有 GameProgressRepository 管理关卡完成进度
- 无排行榜功能

### 改进目标
1. 记录玩家最高分、关卡进度、游玩时长、死亡次数
2. 本地排行榜展示
3. 分数优先排序

---

## 2. 数据模型

### 2.1 LeaderboardEntry

```kotlin
data class LeaderboardEntry(
    val id: String,
    val playerName: String,
    val highScore: Int,
    val highestChapter: Int,
    val highestLevel: Int,
    val totalPlayTime: Long,      // 毫秒
    val totalDeaths: Int,
    val timestamp: Long
)
```

### 2.2 LeaderboardData

```kotlin
data class LeaderboardData(
    val entries: List<LeaderboardEntry>,
    val personalBest: LeaderboardEntry?
)
```

---

## 3. 架构设计

### 3.1 LeaderboardRepository

```kotlin
interface LeaderboardRepository {
    suspend fun saveScore(entry: LeaderboardEntry)
    suspend fun getLeaderboard(limit: Int = 10): List<LeaderboardEntry>
    suspend fun getPersonalBest(): LeaderboardEntry?
    suspend fun clearLeaderboard()
}
```

### 3.2 DataStore 实现

```kotlin
class LeaderboardRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : LeaderboardRepository {

    private val LEADERBOARD_KEY = stringPreferencesKey("leaderboard")

    override suspend fun saveScore(entry: LeaderboardEntry) {
        val current = getLeaderboard(100).toMutableList()
        current.add(entry)
        current.sortByDescending { it.highScore }
        val top10 = current.take(10)

        dataStore.edit { prefs ->
            prefs[LEADERBOARD_KEY] = Json.encodeToString(top10)
        }
    }

    override suspend fun getLeaderboard(limit: Int): List<LeaderboardEntry> {
        // 从DataStore读取并排序
    }

    override suspend fun getPersonalBest(): LeaderboardEntry? {
        return getLeaderboard(100).firstOrNull()
    }
}
```

### 3.3 更新成绩时机

在 `GameState.Completed` 时更新排行榜：
- 最高分：`score`
- 当前章节：`chapter`
- 当前关卡：`levelNumber`
- 累加游戏时长和死亡次数

---

## 4. UI 设计

### 4.1 LeaderboardScreen

```
┌─────────────────────────────┐
│         排行榜              │
├─────────────────────────────┤
│ 🥇 1. 玩家名    99999分     │
│ 🥈 2. 玩家名    88888分     │
│ 🥉 3. 玩家名    77777分     │
│    4. 玩家名    66666分     │
│    5. 玩家名    55555分     │
├─────────────────────────────┤
│ 我的最佳: 99999分 (第5章)    │
└─────────────────────────────┘
```

---

## 5. 实现步骤

### 阶段1：数据层
1. 创建 `LeaderboardEntry` 数据类
2. 创建 `LeaderboardRepository` 接口
3. 创建 `LeaderboardRepositoryImpl` 实现

### 阶段2：业务层
1. 创建 `SaveScoreUseCase`
2. 创建 `GetLeaderboardUseCase`

### 阶段3：UI层
1. 创建 `LeaderboardScreen`
2. 创建 `LeaderboardViewModel`
3. 更新导航

---

## 6. 文件修改清单

**新建文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/LeaderboardEntry.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/repository/LeaderboardRepository.kt`
- `data/src/main/kotlin/com/gameway/data/repository/LeaderboardRepositoryImpl.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/SaveScoreUseCase.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetLeaderboardUseCase.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/leaderboard/LeaderboardScreen.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/leaderboard/LeaderboardViewModel.kt`

**修改文件：**
- `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`

---

## 7. 验收标准

1. ✅ 每次过关时自动保存成绩
2. ✅ 排行榜显示前10名最高分
3. ✅ 显示个人最佳成绩
4. ✅ 按分数降序排列
5. ✅ 数据持久化存储

---

**文档状态:** 已完成
**下一步:** 用户审核后创建实现计划