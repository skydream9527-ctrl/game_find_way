# 好友系统设计文档

**日期:** 2026-04-16
**项目:** FindWay Game
**目标:** 实现好友系统，支持添加/删除好友，查看好友成绩

---

## 1. 概述

### 当前状态
- 有排行榜系统
- 无好友系统

### 改进目标
1. 支持通过游戏名/ID添加好友
2. 支持从排行榜导入好友
3. 存储好友详细信息（最高分、关卡进度、添加时间、备注）
4. 支持删除好友

---

## 2. 数据模型

### 2.1 Friend

```kotlin
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

### 2.2 FriendList

```kotlin
@Serializable
data class FriendList(
    val friends: List<Friend> = emptyList()
)
```

---

## 3. 架构设计

### 3.1 FriendRepository

```kotlin
interface FriendRepository {
    suspend fun addFriend(friend: Friend)
    suspend fun removeFriend(friendId: String)
    suspend fun updateFriend(friend: Friend)
    suspend fun getFriends(): List<Friend>
    suspend fun findFriendByPlayerId(playerId: String): Friend?
    suspend fun searchFriends(query: String): List<Friend>
}
```

### 3.2 DataStore 实现

```kotlin
class FriendRepositoryImpl(
    private val context: Context
) : FriendRepository {

    private val dataStore = context.friendDataStore

    override suspend fun addFriend(friend: Friend) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).toMutableList()
            if (current.none { it.playerId == friend.playerId }) {
                current.add(friend)
                prefs[FRIENDS_KEY] = Json.encodeToString(current)
            }
        }
    }

    override suspend fun removeFriend(friendId: String) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).filter { it.id != friendId }
            prefs[FRIENDS_KEY] = Json.encodeToString(current)
        }
    }

    override suspend fun updateFriend(friend: Friend) {
        dataStore.edit { prefs ->
            val current = getFriendList(prefs).toMutableList()
            val index = current.indexOfFirst { it.id == friend.id }
            if (index >= 0) {
                current[index] = friend
                prefs[FRIENDS_KEY] = Json.encodeToString(current)
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
}
```

---

## 4. UI 设计

### 4.1 FriendScreen

```
┌─────────────────────────────┐
│  好友           [+添加] [🔍] │
├─────────────────────────────┤
│ 👤 玩家名                   │
│    99999分 第5章            │
│    备注: 大神               │
│                      [删除] │
├─────────────────────────────┤
│ 👤 玩家名                   │
│    88888分 第4章            │
│                      [删除] │
└─────────────────────────────┘
```

### 4.2 AddFriendDialog

```
┌─────────────────────────────┐
│         添加好友              │
├─────────────────────────────┤
│ [输入玩家ID或游戏名...]      │
│                             │
│ 从排行榜添加:                │
│ [玩家1] [添加]              │
│ [玩家2] [添加]              │
├─────────────────────────────┤
│ [取消]              [搜索]   │
└─────────────────────────────┘
```

---

## 5. 实现步骤

### 阶段1：数据层
1. 创建 `Friend` 数据类
2. 创建 `FriendRepository` 接口
3. 创建 `FriendRepositoryImpl` 实现

### 阶段2：业务层
1. 创建 `AddFriendUseCase`
2. 创建 `GetFriendsUseCase`
3. 创建 `RemoveFriendUseCase`

### 阶段3：UI层
1. 创建 `FriendScreen`
2. 创建 `FriendViewModel`
3. 创建 `AddFriendDialog`

---

## 6. 文件修改清单

**新建文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Friend.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/repository/FriendRepository.kt`
- `data/src/main/kotlin/com/gameway/data/repository/FriendRepositoryImpl.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/AddFriendUseCase.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetFriendsUseCase.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/usecase/RemoveFriendUseCase.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/friend/FriendScreen.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/friend/FriendViewModel.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/friend/AddFriendDialog.kt`

**修改文件：**
- `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`

---

## 7. 验收标准

1. ✅ 可以通过输入ID/游戏名添加好友
2. ✅ 可以从排行榜导入好友
3. ✅ 可以删除好友
4. ✅ 好友数据显示最高分和关卡进度
5. ✅ 支持备注名
6. ✅ 数据持久化存储

---

**文档状态:** 已完成
**下一步:** 用户审核后创建实现计划