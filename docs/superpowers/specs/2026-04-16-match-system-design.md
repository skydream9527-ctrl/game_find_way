# 好友对战系统设计文档

**日期:** 2026-04-16
**项目:** FindWay Game
**目标:** 实现好友对战系统，支持发起/接受/拒绝对战，固定关卡比分数

---

## 1. 概述

### 当前状态
- 有好友系统
- 无对战功能

### 改进目标
1. 发起对战请求（选择关卡）
2. 接收/拒绝对战
3. 同步比分展示
4. 固定关卡对比，判定胜负

---

## 2. 数据模型

### 2.1 Match

```kotlin
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

enum class MatchStatus {
    PENDING,      // 待接受
    ACCEPTED,     // 已接受（进行中）
    REJECTED,    // 已拒绝
    COMPLETED,   // 已完成
    EXPIRED      // 已过期
}
```

### 2.2 MatchResult

```kotlin
data class MatchResult(
    val matchId: String,
    val winnerId: String?,
    val winnerName: String?,
    val isDraw: Boolean,
    val challengerScore: Int,
    val challengedScore: Int
)
```

---

## 3. 架构设计

### 3.1 MatchRepository

```kotlin
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

### 3.2 MatchManager

处理对战逻辑：

```kotlin
class MatchManager(
    private val matchRepository: MatchRepository,
    private val leaderboardRepository: LeaderboardRepository
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

        // 检查是否双方都已提交分数
        if (finalMatch.challengerScore > 0 && finalMatch.challengedScore > 0) {
            return calculateResult(finalMatch)
        }

        return MatchResult(matchId, null, null, false,
            finalMatch.challengerScore, finalMatch.challengedScore)
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
}
```

---

## 4. UI 设计

### 4.1 MatchScreen

```
┌─────────────────────────────┐
│  对战            [发起对战]  │
├─────────────────────────────┤
│ 📋 待接受(2)                │
│ ├─ 玩家A 邀请 第3章-1关    │
│ │           [接受] [拒绝]  │
│ └─ 玩家B 邀请 第5章-2关    │
│           [接受] [拒绝]    │
├─────────────────────────────┤
│ 🏆 进行中(1)               │
│ ├─ vs 玩家C 第3章-1关      │
│ │   你: 500分 vs 玩家C: 480分│
└─────────────────────────────┘
```

### 4.2 CreateMatchDialog

```
┌─────────────────────────────┐
│        发起对战             │
├─────────────────────────────┤
│ 选择关卡:                   │
│ 第 [3] 章 第 [1] 关         │
├─────────────────────────────┤
│ 选择对手:                   │
│ [玩家A] [玩家B] [玩家C]     │
├─────────────────────────────┤
│ [取消]           [发起]     │
└─────────────────────────────┘
```

### 4.3 MatchResultDialog

```
┌─────────────────────────────┐
│        对战结果              │
├─────────────────────────────┤
│         🏆 你赢了!          │
│                             │
│    你: 520分 vs 玩家A: 480分  │
│                             │
│        [再来一局] [关闭]     │
└─────────────────────────────┘
```

---

## 5. 实现步骤

### 阶段1：数据层
1. 创建 `Match` 和 `MatchStatus` 数据类
2. 创建 `MatchResult` 数据类
3. 创建 `MatchRepository` 接口
4. 创建 `MatchRepositoryImpl` 实现

### 阶段2：业务层
1. 创建 `MatchManager`
2. 创建 `CreateMatchUseCase`
3. 创建 `AcceptMatchUseCase`
4. 创建 `SubmitScoreUseCase`

### 阶段3：UI层
1. 创建 `MatchScreen`
2. 创建 `MatchViewModel`
3. 创建 `CreateMatchDialog`
4. 创建 `MatchResultDialog`

---

## 6. 文件修改清单

**新建文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Match.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/MatchResult.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/MatchStatus.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/repository/MatchRepository.kt`
- `data/src/main/kotlin/com/gameway/data/repository/MatchRepositoryImpl.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/MatchManager.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/CreateMatchUseCase.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/AcceptMatchUseCase.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/SubmitScoreUseCase.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchScreen.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchViewModel.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/match/CreateMatchDialog.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/match/MatchResultDialog.kt`

**修改文件：**
- `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`

---

## 7. 验收标准

1. ✅ 可以发起对战请求（选择关卡和对手）
2. ✅ 可以接收/拒绝对战
3. ✅ 对战进行中显示双方分数
4. ✅ 固定关卡对比，判定胜负
5. ✅ 显示对战结果对话框
6. ✅ 数据持久化存储

---

**文档状态:** 已完成
**下一步:** 用户审核后创建实现计划