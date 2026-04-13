# 游戏画面与操作细节改进实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现角色选择系统、固定角色屏幕位置、腿部跑动动画、优化平台生成避免重叠

**Architecture:** 渐进式改造，分4阶段实现：角色系统数据层 → 角色选择界面 → 游戏画面渲染 → 平台生成优化。每个阶段独立提交，确保可测试和回滚。

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, MVVM架构

---

## 文件结构

**新建文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/CharacterType.kt` - 角色类型枚举
- `domain/src/commonMain/kotlin/com/gameway/domain/model/CharacterConfig.kt` - 角色配置数据
- `presentation/src/main/kotlin/com/gameway/presentation/screens/character/CharacterSelectScreen.kt` - 角色选择界面
- `domain/src/commonTest/kotlin/com/gameway/domain/model/CharacterConfigTest.kt` - 角色配置测试

**修改文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt` - 添加type和config属性
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt` - 使用effectiveJumpPower
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt` - 角色固定位置、腿部动画
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt` - 动画帧、角色类型参数
- `presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt` - 添加选择角色按钮
- `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt` - 添加CharacterSelect路由
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt` - 平台生成优化（重叠检测）
- `core/src/commonMain/kotlin/com/gameway/core/Constants.kt` - 添加间隙常量
- `domain/src/commonTest/kotlin/com/gameway/domain/engine/PhysicsSystemTest.kt` - 更新测试
- `domain/src/commonTest/kotlin/com/gameway/domain/model/LevelGenerationTest.kt` - 添加重叠测试

---

## 阶段1：角色系统数据层

### Task 1.1: 创建CharacterType枚举

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/CharacterType.kt`

- [ ] **Step 1: 创建CharacterType枚举文件**

```kotlin
package com.gameway.domain.model

enum class CharacterType {
    CAT,
    DOG,
    HORSE
}
```

- [ ] **Step 2: Commit**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/CharacterType.kt
git commit -m "feat: add CharacterType enum for character selection"
```

---

### Task 1.2: 创建CharacterConfig数据类

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/CharacterConfig.kt`
- Create: `domain/src/commonTest/kotlin/com/gameway/domain/model/CharacterConfigTest.kt`

- [ ] **Step 1: 写CharacterConfig测试**

```kotlin
package com.gameway.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CharacterConfigTest {
    
    @Test
    fun `getAll returns three character configs`() {
        val configs = CharacterConfig.getAll()
        assertEquals(3, configs.size)
    }
    
    @Test
    fun `CAT has correct attributes`() {
        val cat = CharacterConfig.CAT
        assertEquals(CharacterType.CAT, cat.type)
        assertEquals("小猫", cat.name)
        assertEquals("🐱", cat.emoji)
        assertEquals(1.0f, cat.moveSpeedMultiplier)
        assertEquals(1.0f, cat.jumpPowerMultiplier)
    }
    
    @Test
    fun `DOG has slower speed but higher jump`() {
        val dog = CharacterConfig.DOG
        assertEquals(0.9f, dog.moveSpeedMultiplier)
        assertEquals(1.1f, dog.jumpPowerMultiplier)
    }
    
    @Test
    fun `HORSE has faster speed but lower jump`() {
        val horse = CharacterConfig.HORSE
        assertEquals(1.2f, horse.moveSpeedMultiplier)
        assertEquals(0.95f, horse.jumpPowerMultiplier)
    }
    
    @Test
    fun `getByType returns correct config`() {
        val cat = CharacterConfig.getByType(CharacterType.CAT)
        assertEquals(CharacterConfig.CAT, cat)
        
        val dog = CharacterConfig.getByType(CharacterType.DOG)
        assertEquals(CharacterConfig.DOG, dog)
        
        val unknown = CharacterConfig.getByType(CharacterType.HORSE)
        assertNotNull(unknown)
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `./gradlew :domain:commonTest --tests "com.gameway.domain.model.CharacterConfigTest"`
Expected: FAIL with "Unresolved reference: CharacterConfig"

- [ ] **Step 3: 创建CharacterConfig实现**

```kotlin
package com.gameway.domain.model

data class CharacterConfig(
    val type: CharacterType,
    val name: String,
    val emoji: String,
    val moveSpeedMultiplier: Float,
    val jumpPowerMultiplier: Float
) {
    companion object {
        val CAT = CharacterConfig(
            type = CharacterType.CAT,
            name = "小猫",
            emoji = "🐱",
            moveSpeedMultiplier = 1.0f,
            jumpPowerMultiplier = 1.0f
        )
        
        val DOG = CharacterConfig(
            type = CharacterType.DOG,
            name = "小狗",
            emoji = "🐕",
            moveSpeedMultiplier = 0.9f,
            jumpPowerMultiplier = 1.1f
        )
        
        val HORSE = CharacterConfig(
            type = CharacterType.HORSE,
            name = "小马",
            emoji = "🐴",
            moveSpeedMultiplier = 1.2f,
            jumpPowerMultiplier = 0.95f
        )
        
        fun getAll(): List<CharacterConfig> = listOf(CAT, DOG, HORSE)
        
        fun getByType(type: CharacterType): CharacterConfig {
            return getAll().find { it.type == type } ?: CAT
        }
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `./gradlew :domain:commonTest --tests "com.gameway.domain.model.CharacterConfigTest"`
Expected: PASS (5 tests)

- [ ] **Step 5: Commit**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/CharacterConfig.kt domain/src/commonTest/kotlin/com/gameway/domain/model/CharacterConfigTest.kt
git commit -m "feat: add CharacterConfig with TDD tests"
```

---

### Task 1.3: 更新Character模型

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt`

- [ ] **Step 1: 读取当前Character.kt文件**

Run: Read the file to understand current structure

- [ ] **Step 2: 更新Character添加type和config属性**

```kotlin
package com.gameway.domain.model

import com.gameway.core.GameConstants

enum class CharacterState {
    IDLE, RUNNING, JUMPING, FALLING, DEAD
}

data class Character(
    val position: Vector2,
    val velocity: Vector2,
    val state: CharacterState,
    val type: CharacterType,
    val config: CharacterConfig,
    val isGrounded: Boolean,
    val jumpCount: Int,
    val maxJumps: Int,
    val activePowerUps: List<ActivePowerUp>,
    val health: Int,
    val coinsCollected: Int
) {
    companion object {
        fun createDefault(type: CharacterType = CharacterType.CAT): Character {
            val config = CharacterConfig.getByType(type)
            return Character(
                position = Vector2(GameConstants.STARTING_POSITION_X, GameConstants.STARTING_PLATFORM_Y - 5f),
                velocity = Vector2(config.moveSpeedMultiplier * GameConstants.MOVE_SPEED, 0f),
                state = CharacterState.IDLE,
                type = type,
                config = config,
                isGrounded = false,
                jumpCount = 0,
                maxJumps = 1,
                activePowerUps = emptyList(),
                health = 3,
                coinsCollected = 0
            )
        }
    }
    
    val effectiveMoveSpeed: Float
        get() = GameConstants.MOVE_SPEED * config.moveSpeedMultiplier *
                (if (hasActivePowerUp(PowerUpType.LIGHTNING)) 1.3f else 1.0f)
    
    val effectiveJumpPower: Float
        get() = GameConstants.JUMP_POWER * config.jumpPowerMultiplier
    
    val effectiveGravity: Float
        get() = if (hasActivePowerUp(PowerUpType.FEATHER))
            GameConstants.GRAVITY * 0.5f else GameConstants.GRAVITY
    
    val hasDoubleJump: Boolean
        get() = maxJumps >= 2 || hasActivePowerUp(PowerUpType.BUTTERFLY)
    
    val hasShield: Boolean
        get() = hasActivePowerUp(PowerUpType.SHIELD)
    
    fun hasActivePowerUp(type: PowerUpType): Boolean {
        val currentTime = System.currentTimeMillis()
        return activePowerUps.any { it.type == type && it.expiresAt > currentTime }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt
git commit -m "feat: add type and config to Character model"
```

---

### Task 1.4: 更新PhysicsSystem使用effectiveJumpPower

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt`
- Modify: `domain/src/commonTest/kotlin/com/gameway/domain/engine/PhysicsSystemTest.kt`

- [ ] **Step 1: 读取当前PhysicsSystem.kt**

Run: Read the file to understand current implementation

- [ ] **Step 2: 更新PhysicsSystem.applyJump使用effectiveJumpPower**

```kotlin
package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.Character
import com.gameway.domain.model.CharacterState
import com.gameway.domain.model.Vector2

object PhysicsSystem {
    
    fun update(character: Character, deltaTime: Long): Character {
        val dt = deltaTime / 1000f
        
        val gravity = character.effectiveGravity
        val newVelocityY = character.velocity.y + gravity * dt * 60f
        val moveSpeed = character.effectiveMoveSpeed
        
        val newX = character.position.x + moveSpeed * dt * 60f
        val newY = character.position.y + character.velocity.y * dt * 60f
        
        return character.copy(
            position = Vector2(newX, newY),
            velocity = Vector2(moveSpeed, newVelocityY),
            state = determineState(character, newVelocityY),
            isGrounded = false
        )
    }
    
    private fun determineState(character: Character, velocityY: Float): CharacterState {
        return when {
            character.isGrounded -> CharacterState.RUNNING
            velocityY < 0 -> CharacterState.JUMPING
            velocityY > 0 -> CharacterState.FALLING
            else -> character.state
        }
    }
    
    fun applyJump(character: Character): Character {
        return character.copy(
            velocity = Vector2(character.effectiveMoveSpeed, -character.effectiveJumpPower),
            isGrounded = false,
            jumpCount = character.jumpCount + 1,
            state = CharacterState.JUMPING
        )
    }
    
    fun landOnPlatform(character: Character, platformY: Float): Character {
        return character.copy(
            position = Vector2(character.position.x, platformY),
            velocity = Vector2(character.effectiveMoveSpeed, 0f),
            isGrounded = true,
            jumpCount = 0,
            state = CharacterState.RUNNING
        )
    }
}
```

- [ ] **Step 3: 读取PhysicsSystemTest.kt**

Run: Read the test file to see current tests

- [ ] **Step 4: 添加角色类型差异测试**

在现有测试文件中添加：

```kotlin
@Test
fun `DOG has lower effective jump power than CAT`() {
    val cat = Character.createDefault(CharacterType.CAT)
    val dog = Character.createDefault(CharacterType.DOG)
    
    assertTrue(cat.effectiveJumpPower > dog.effectiveJumpPower)
}

@Test
fun `HORSE has higher effective move speed than CAT`() {
    val cat = Character.createDefault(CharacterType.CAT)
    val horse = Character.createDefault(CharacterType.HORSE)
    
    assertTrue(horse.effectiveMoveSpeed > cat.effectiveMoveSpeed)
}
```

- [ ] **Step 5: 运行测试验证**

Run: `./gradlew :domain:commonTest --tests "com.gameway.domain.engine.PhysicsSystemTest"`
Expected: PASS (including new tests)

- [ ] **Step 6: Commit**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt domain/src/commonTest/kotlin/com/gameway/domain/engine/PhysicsSystemTest.kt
git commit -m "feat: PhysicsSystem uses effectiveJumpPower from CharacterConfig"
```

---

## 阶段2：角色选择界面

### Task 2.1: 创建CharacterSelectScreen

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/character/CharacterSelectScreen.kt`

- [ ] **Step 1: 创建CharacterSelectScreen文件**

```kotlin
package com.gameway.presentation.screens.character

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameway.domain.model.CharacterConfig
import com.gameway.domain.model.CharacterType

@Composable
fun CharacterSelectScreen(
    currentCharacter: CharacterType,
    onCharacterSelected: (CharacterType) -> Unit,
    onBack: () -> Unit
) {
    val characters = CharacterConfig.getAll()
    var selectedType by remember { mutableStateOf(currentCharacter) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF4A148C))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "选择你的角色",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                characters.forEach { config ->
                    CharacterCard(
                        config = config,
                        isSelected = selectedType == config.type,
                        onClick = { selectedType = config.type }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AttributeComparisonPanel(selectedType)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("返回")
                }
                
                Button(
                    onClick = { onCharacterSelected(selectedType) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("确认选择")
                }
            }
        }
    }
}

@Composable
fun CharacterCard(
    config: CharacterConfig,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 140.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(config.emoji, fontSize = 48.sp)
            Text(config.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("⚡", fontSize = 14.sp)
                Text("${(config.moveSpeedMultiplier * 100).toInt()}%", fontSize = 12.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("⬆", fontSize = 14.sp)
                Text("${(config.jumpPowerMultiplier * 100).toInt()}%", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AttributeComparisonPanel(selectedType: CharacterType) {
    val config = CharacterConfig.getByType(selectedType)
    
    Card(
        modifier = Modifier.fillMaxWidth(0.8f),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${config.emoji} ${config.name} 属性",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("速度", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        "${(config.moveSpeedMultiplier * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (config.moveSpeedMultiplier >= 1.0f) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("跳力", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        "${(config.jumpPowerMultiplier * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (config.jumpPowerMultiplier >= 1.0f) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/character/CharacterSelectScreen.kt
git commit -m "feat: add CharacterSelectScreen with card UI"
```

---

### Task 2.2: 更新MainMenuScreen添加按钮

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt`

- [ ] **Step 1: 读取当前MainMenuScreen.kt**

Run: Read the file to see current structure

- [ ] **Step 2: 更新MainMenuScreen添加onSelectCharacter参数和按钮**

```kotlin
package com.gameway.presentation.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onSelectCharacter: () -> Unit,
    onViewStats: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF4A148C))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🗺️", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Find Way",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("开始游戏", fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onSelectCharacter,
                modifier = Modifier.fillMaxWidth(0.7f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
            ) {
                Text("选择角色", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onViewStats,
                modifier = Modifier.fillMaxWidth(0.7f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("角色属性", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(0.7f).height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Text("设置", fontSize = 14.sp, color = Color.White)
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt
git commit -m "feat: add Select Character button to MainMenuScreen"
```

---

### Task 2.3: 更新导航路由

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`

- [ ] **Step 1: 读取当前AppNavigation.kt**

Run: Read the file to understand current navigation structure

- [ ] **Step 2: 添加CharacterSelect路由和状态管理**

需要添加：
- CharacterSelect screen路由
- 存储当前选中的角色类型（在ViewModel或State中）
- 游戏开始时传入角色类型

```kotlin
// 在AppNavigation.kt中添加
sealed class Screen {
    object MainMenu : Screen()
    object CharacterSelect : Screen()
    object ChapterSelect : Screen()
    object LevelSelect : Screen(chapterId: Int)
    object Game : Screen(chapterId: Int, levelNumber: Int, characterType: CharacterType)
    object Stats : Screen()
}

// 在NavHost中添加CharacterSelect路由
composable(Screen.CharacterSelect) {
    CharacterSelectScreen(
        currentCharacter = selectedCharacterType,
        onCharacterSelected = { type ->
            selectedCharacterType = type
            navController.popBackStack()
        },
        onBack = { navController.popBackStack() }
    )
}

// 更新Game路由传递characterType
composable(
    route = "game/{chapterId}/{levelNumber}/{characterType}",
    arguments = listOf(
        navArgument("chapterId") { type = NavType.IntType },
        navArgument("levelNumber") { type = NavType.IntType },
        navArgument("characterType") { type = NavType.StringType }
    )
) { backStackEntry ->
    val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 1
    val levelNumber = backStackEntry.arguments?.getInt("levelNumber") ?: 1
    val characterTypeString = backStackEntry.arguments?.getString("characterType") ?: "CAT"
    val characterType = CharacterType.valueOf(characterTypeString)
    
    GameScreen(
        chapterId = chapterId,
        levelNumber = levelNumber,
        characterType = characterType,
        onComplete = { ... }
    )
}
```

- [ ] **Step 3: Commit**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt
git commit -m "feat: add CharacterSelect route and pass characterType to Game"
```

---

### Task 2.4: 更新GameViewModel接受角色类型

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt`
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameScreen.kt`

- [ ] **Step 1: 读取GameViewModel.kt**

Run: Read the file to understand current structure

- [ ] **Step 2: 更新GameViewModel.loadLevel接受角色类型参数**

```kotlin
class GameViewModel(
    private val getLevelUseCase: GetLevelUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val gameEngine: GameEngine = GameEngine()
) : ViewModel() {
    
    fun loadLevel(chapterId: Int, levelNumber: Int, characterType: CharacterType) {
        viewModelScope.launch {
            val level = getLevelUseCase(chapterId, levelNumber)
            gameEngine.startLevel(level, characterType)
            _uiState.value = _uiState.value.copy(
                chapterId = chapterId,
                levelNumber = levelNumber,
                level = level,
                gameState = GameState.Countdown,
                character = Character.createDefault(characterType)
            )
            startGameLoop()
        }
    }
    
    // ... 其他方法保持不变
}
```

- [ ] **Step 3: 更新GameEngine.startLevel接受角色类型**

```kotlin
class GameEngine {
    fun startLevel(newLevel: Level, characterType: CharacterType = CharacterType.CAT) {
        level = newLevel
        val firstPlatform = newLevel.platforms.firstOrNull()
        val startY = firstPlatform?.y ?: GameConstants.STARTING_PLATFORM_Y
        character = Character.createDefault(characterType).copy(
            position = Vector2(GameConstants.STARTING_POSITION_X, startY - 5f)
        )
        // ... 其他初始化
    }
}
```

- [ ] **Step 4: 更新GameScreen接受角色类型参数**

```kotlin
@Composable
fun GameScreen(
    chapterId: Int,
    levelNumber: Int,
    characterType: CharacterType,
    onComplete: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    LaunchedEffect(chapterId, levelNumber, characterType) {
        viewModel.loadLevel(chapterId, levelNumber, characterType)
    }
    
    // ... 其他内容保持不变
}
```

- [ ] **Step 5: Commit**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameScreen.kt domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt
git commit -m "feat: Game accepts characterType parameter"
```

---

## 阶段3：游戏画面渲染改进

### Task 3.1: 更新GameCanvas角色固定位置

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt`

- [ ] **Step 1: 读取当前GameCanvas.kt**

Run: Read the file to understand current rendering logic

- [ ] **Step 2: 更新GameCanvas实现角色固定屏幕左侧**

修改关键渲染逻辑：

```kotlin
@Composable
fun GameCanvas(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val level = uiState.level
    
    if (level == null) return
    
    val chapter = Chapter.getAllChapters().find { it.id == level.chapterId } ?: Chapter.getAllChapters().first()
    val theme = chapter.theme
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.onTap() }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val scaleX = canvasWidth / GameConstants.GAME_WORLD_WIDTH
        val scaleY = canvasHeight / GameConstants.GAME_WORLD_HEIGHT
        val scale = minOf(scaleX, scaleY)
        
        val offsetX = (canvasWidth - GameConstants.GAME_WORLD_WIDTH * scale) / 2f
        val offsetY = (canvasHeight - GameConstants.GAME_WORLD_HEIGHT * scale) / 2f
        
        drawRect(color = Color(theme.backgroundColor), size = size)
        
        // 角色固定屏幕位置（左侧15%水平居中，垂直居中）
        val CHARACTER_SCREEN_X = canvasWidth * 0.15f
        val CHARACTER_SCREEN_Y = canvasHeight * 0.5f
        
        // 视口基于角色世界坐标
        val viewportX = uiState.character.position.x - CHARACTER_SCREEN_X
        
        // 渲染平台（相对于视口）
        for (platform in level.platforms) {
            val screenX = (platform.x - viewportX) * scale + offsetX
            val screenY = platform.y * scale + offsetY
            val platformWidth = platform.width * scale
            val platformHeight = platform.height * scale
            
            // 只渲染可见范围内的平台
            if (screenX > -platformWidth && screenX < canvasWidth + platformWidth) {
                drawRoundRect(
                    color = Color(theme.platformColor),
                    topLeft = Offset(screenX, screenY),
                    size = Size(platformWidth, platformHeight)
                )
            }
        }
        
        // 渲染金币（相对于视口）
        for (coin in level.coins) {
            if (!coin.collected) {
                val screenX = (coin.x - viewportX) * scale + offsetX
                val screenY = coin.y * scale + offsetY
                if (screenX > -20f * scale && screenX < canvasWidth + 20f * scale) {
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 8f * scale,
                        center = Offset(screenX, screenY)
                    )
                }
            }
        }
        
        // 渲染道具（相对于视口）
        for (powerUp in level.powerUps) {
            if (!powerUp.collected) {
                val screenX = (powerUp.x - viewportX) * scale + offsetX
                val screenY = powerUp.y * scale + offsetY
                if (screenX > -20f * scale && screenX < canvasWidth + 20f * scale) {
                    val color = when (powerUp.type) {
                        PowerUpType.FEATHER -> Color(0xFF8BC34A)
                        PowerUpType.BUTTERFLY -> Color(0xFF03A9F4)
                        PowerUpType.LIGHTNING -> Color(0xFFFFEB3B)
                        PowerUpType.SHIELD -> Color(0xFF9E9E9E)
                        PowerUpType.GEM -> Color(0xFFE91E63)
                        PowerUpType.STAR -> Color(0xFFFF9800)
                    }
                    drawCircle(color = color, radius = 10f * scale, center = Offset(screenX, screenY))
                }
            }
        }
        
        // 渲染角色（固定屏幕位置）
        drawCharacterWithAnimation(
            character = uiState.character,
            screenX = CHARACTER_SCREEN_X,
            screenY = CHARACTER_SCREEN_Y,
            scale = scale,
            animationFrame = uiState.animationFrame
        )
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt
git commit -m "feat: character fixed at screen left-center position"
```

---

### Task 3.2: 实现腿部跑动动画

**Files:**
- Modify: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt`

- [ ] **Step 1: 在GameCanvas中添加drawCharacterWithAnimation函数**

```kotlin
fun drawCharacterWithAnimation(
    character: Character,
    screenX: Float,
    screenY: Float,
    scale: Float,
    animationFrame: Int
) {
    val config = character.config
    
    // 计算腿部角度（仅在奔跑时）
    val legAngle = if (character.state == CharacterState.RUNNING) {
        val cycle = (animationFrame % 8) / 8f
        kotlin.math.sin(cycle * kotlin.math.PI * 2) * 15f
    } else {
        0f
    }
    
    // 根据角色类型设置颜色
    val bodyColor = when (character.type) {
        CharacterType.CAT -> Color(0xFFFFB74D)
        CharacterType.DOG -> Color(0xFF8D6E63)
        CharacterType.HORSE -> Color(0xFF9E9E9E)
    }
    
    val headColor = when (character.type) {
        CharacterType.CAT -> Color(0xFFFFE0B2)
        CharacterType.DOG -> Color(0xFFD7CCC8)
        CharacterType.HORSE -> Color(0xFFBDBDBD)
    }
    
    val legColor = Color(0xFF333333)
    
    // 绘制身体
    drawCircle(
        color = bodyColor,
        radius = 20f * scale,
        center = Offset(screenX, screenY - 15f * scale)
    )
    
    // 绘制头部
    drawCircle(
        color = headColor,
        radius = 15f * scale,
        center = Offset(screenX, screenY - 35f * scale)
    )
    
    // 绘制眼睛
    drawCircle(
        color = Color.Black,
        radius = 3f * scale,
        center = Offset(screenX - 6f * scale, screenY - 38f * scale)
    )
    drawCircle(
        color = Color.Black,
        radius = 3f * scale,
        center = Offset(screenX + 6f * scale, screenY - 38f * scale)
    )
    
    // 绘制左腿（带动画）
    val leftLegStart = Offset(screenX - 8f * scale, screenY)
    val leftLegEnd = calculateLegEnd(leftLegStart, legAngle, 25f * scale)
    drawLine(
        color = legColor,
        start = leftLegStart,
        end = leftLegEnd,
        strokeWidth = 6f * scale
    )
    
    // 绘制右腿（反向动画）
    val rightLegStart = Offset(screenX + 8f * scale, screenY)
    val rightLegEnd = calculateLegEnd(rightLegStart, -legAngle, 25f * scale)
    drawLine(
        color = legColor,
        start = rightLegStart,
        end = rightLegEnd,
        strokeWidth = 6f * scale
    )
}

fun calculateLegEnd(start: Offset, angle: Float, length: Float): Offset {
    val radians = angle * kotlin.math.PI / 180f
    return Offset(
        start.x + kotlin.math.sin(radians) * length,
        start.y + kotlin.math.cos(radians) * length
    )
}
```

- [ ] **Step 2: 更新GameUiState添加animationFrame**

```kotlin
data class GameUiState(
    val character: Character = Character.createDefault(),
    val gameState: GameState = GameState.Loading,
    val scrollX: Float = 0f,
    val score: Int = 0,
    val animationFrame: Int = 0,
    val chapterId: Int = 1,
    val levelNumber: Int = 1,
    val level: com.gameway.domain.model.Level? = null
)
```

- [ ] **Step 3: 更新GameViewModel添加动画帧计数**

```kotlin
class GameViewModel {
    private var animationFrame = 0
    
    private fun startGameLoop() {
        if (gameLoopActive) return
        gameLoopActive = true
        
        viewModelScope.launch {
            while (gameLoopActive) {
                gameEngine.update()
                animationFrame++
                
                val state = gameEngine.getGameState()
                _uiState.value = _uiState.value.copy(
                    character = gameEngine.getCharacter(),
                    gameState = state,
                    scrollX = gameEngine.getScrollX(),
                    score = gameEngine.getScore(),
                    animationFrame = animationFrame
                )
                
                if (state is GameState.Completed || state is GameState.Failed) {
                    gameLoopActive = false
                    break
                }
                
                delay(16L)
            }
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt
git commit -m "feat: add leg running animation to character"
```

---

## 阶段4：平台生成优化

### Task 4.1: 添加间隙常量到Constants.kt

**Files:**
- Modify: `core/src/commonMain/kotlin/com/gameway/core/Constants.kt`

- [ ] **Step 1: 读取Constants.kt**

Run: Read the file to see current constants

- [ ] **Step 2: 添加间隙常量**

```kotlin
package com.gameway.core

object GameConstants {
    const val GRAVITY = 0.6f
    const val JUMP_POWER = 12f
    const val MOVE_SPEED = 5f
    const val MAX_MOVE_SPEED = 6f
    
    const val GAME_WORLD_WIDTH = 800f
    const val GAME_WORLD_HEIGHT = 600f
    
    const val MAX_HORIZONTAL_JUMP_DISTANCE = 200f
    const val MAX_VERTICAL_JUMP_HEIGHT = 120f
    
    // 新增：平台间隙常量
    const val MIN_HORIZONTAL_GAP = 30f
    const val MAX_HORIZONTAL_GAP_EASY = 120f
    const val MAX_HORIZONTAL_GAP_MEDIUM = 160f
    const val MAX_HORIZONTAL_GAP_HARD = 180f
    const val MAX_HORIZONTAL_GAP_EXPERT = 200f
    
    const val TOTAL_CHAPTERS = 10
    const val LEVELS_PER_CHAPTER = 10
    const val MIN_PLATFORMS = 20
    const val MAX_PLATFORMS = 50
    
    const val MIN_ITEMS_PER_LEVEL = 3
    const val MAX_ITEMS_PER_LEVEL = 8
    const val MIN_COINS_PER_LEVEL = 10
    const val MAX_COINS_PER_LEVEL = 30
    
    const val FEATHER_DURATION = 10000L
    const val BUTTERFLY_DURATION = 15000L
    const val LIGHTNING_DURATION = 8000L
    
    const val MILLIS_PER_FRAME = 16f
    
    const val COUNTDOWN_DURATION = 2000L
    const val SCROLL_OFFSET = 100f
    const val COIN_SCORE = 10
    const val POWERUP_SCORE = 20
    const val PLATFORM_MOVEMENT_SPEED_FACTOR = 1000f
    const val PLATFORM_MOVEMENT_SCALE = 0.01f
    const val STARTING_POSITION_X = 50f
    
    const val STARTING_PLATFORM_Y = 350f
    const val MIN_PLATFORM_Y = 200f
    const val MAX_PLATFORM_Y = 500f
    const val POWERUP_Y_OFFSET = 30f
    const val COIN_Y_OFFSET = 25f
    const val MOVING_PLATFORM_PROBABILITY = 0.5f
    const val PLATFORM_MIN_MOVE_SPEED = 0.5f
    const val PLATFORM_MAX_MOVE_SPEED = 1.5f
}
```

- [ ] **Step 3: Commit**

```bash
git add core/src/commonMain/kotlin/com/gameway/core/Constants.kt
git commit -m "feat: add platform gap constants"
```

---

### Task 4.2: 更新Level.kt平台生成逻辑

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt`
- Modify: `domain/src/commonTest/kotlin/com/gameway/domain/model/LevelGenerationTest.kt`

- [ ] **Step 1: 读取Level.kt**

Run: Read the file to understand current platform generation

- [ ] **Step 2: 添加重叠检测函数和更新平台生成**

在Level.kt中添加：

```kotlin
private fun hasOverlap(candidate: Platform, existing: List<Platform>): Boolean {
    val verticalOverlapRange = 50f
    
    return existing.any { platform ->
        val horizontalOverlap = candidate.left < platform.right && candidate.right > platform.left
        val verticalClose = kotlin.math.abs(candidate.y - platform.y) < verticalOverlapRange
        horizontalOverlap && verticalClose
    }
}

private fun getMaxGapForDifficulty(difficulty: Difficulty): Float {
    return when (difficulty) {
        Difficulty.EASY -> GameConstants.MAX_HORIZONTAL_GAP_EASY
        Difficulty.MEDIUM -> GameConstants.MAX_HORIZONTAL_GAP_MEDIUM
        Difficulty.HARD -> GameConstants.MAX_HORIZONTAL_GAP_HARD
        Difficulty.EXPERT -> GameConstants.MAX_HORIZONTAL_GAP_EXPERT
    }
}

private fun generatePlatformsWithOverlapCheck(
    count: Int,
    difficulty: Difficulty,
    random: Random
): List<Platform> {
    val platforms = mutableListOf<Platform>()
    
    for (i in 0 until count) {
        var attempts = 0
        var validPlatform: Platform? = null
        
        while (validPlatform == null && attempts < 10) {
            val candidate = generateCandidatePlatform(i, platforms, difficulty, random)
            
            val previous = platforms.lastOrNull()
            val noOverlap = !hasOverlap(candidate, platforms)
            val isReachable = ensureReachability(candidate, previous)
            
            if (noOverlap && isReachable) {
                validPlatform = candidate
            }
            attempts++
        }
        
        validPlatform?.let { platforms.add(it) }
    }
    
    return platforms
}

private fun generateCandidatePlatform(
    index: Int,
    platforms: List<Platform>,
    difficulty: Difficulty,
    random: Random
): Platform {
    val previous = platforms.lastOrNull()
    
    val minGap = GameConstants.MIN_HORIZONTAL_GAP
    val maxGap = getMaxGapForDifficulty(difficulty)
    val gap = random.nextFloat(minGap, maxGap)
    
    val x = previous?.right?.plus(gap) ?: GameConstants.STARTING_POSITION_X
    
    // 中等起伏的纵向变化
    val yVariation = if (index == 0) {
        0f
    } else {
        random.nextFloat(-60f, 80f)
    }
    
    val y = (previous?.y?.plus(yVariation) ?: GameConstants.STARTING_PLATFORM_Y)
        .coerceIn(GameConstants.MIN_PLATFORM_Y, GameConstants.MAX_PLATFORM_Y)
    
    val width = when (difficulty) {
        Difficulty.EASY -> random.nextFloat(80f, 120f)
        Difficulty.MEDIUM -> random.nextFloat(60f, 100f)
        Difficulty.HARD -> random.nextFloat(40f, 80f)
        Difficulty.EXPERT -> random.nextFloat(30f, 60f)
    }
    
    val type = if (difficulty >= Difficulty.HARD && index > count / 2) {
        if (random.nextFloat() > GameConstants.MOVING_PLATFORM_PROBABILITY) PlatformType.MOVING_HORIZONTAL
        else PlatformType.STATIC
    } else {
        PlatformType.STATIC
    }
    
    val moveRange = if (type != PlatformType.STATIC) random.nextFloat(50f, 100f) else 0f
    val moveSpeed = if (type != PlatformType.STATIC) random.nextFloat(GameConstants.PLATFORM_MIN_MOVE_SPEED, GameConstants.PLATFORM_MAX_MOVE_SPEED) else 0f
    
    return Platform(
        id = index,
        x = x,
        y = y,
        width = width,
        type = type,
        moveRange = moveRange,
        moveSpeed = moveSpeed,
        moveOffset = random.nextFloat(0f, 360f)
    )
}

private fun ensureReachability(candidate: Platform, previous: Platform?): Boolean {
    if (previous == null) return true
    
    val horizontalGap = candidate.left - previous.right
    val verticalDiff = previous.y - candidate.y
    
    return horizontalGap <= GameConstants.MAX_HORIZONTAL_JUMP_DISTANCE &&
           verticalDiff <= GameConstants.MAX_VERTICAL_JUMP_HEIGHT
}
```

更新generatePlatforms调用：

```kotlin
private fun generatePlatforms(count: Int, difficulty: Difficulty, random: Random): List<Platform> {
    return generatePlatformsWithOverlapCheck(count, difficulty, random)
}
```

- [ ] **Step 3: 读取LevelGenerationTest.kt**

Run: Read the test file to see current tests

- [ ] **Step 4: 添加重叠检测测试**

```kotlin
@Test
fun `generated platforms have no overlap`() {
    val level = Level.generate(chapterId = 1, levelNumber = 1, seed = 12345)
    
    for (i in 0 until level.platforms.size - 1) {
        val current = level.platforms[i]
        val next = level.platforms[i + 1]
        
        // 检查横向不重叠
        assertTrue(current.right <= next.left || current.left >= next.right)
        
        // 检查间隙不小于MIN_HORIZONTAL_GAP
        val gap = next.left - current.right
        assertTrue(gap >= GameConstants.MIN_HORIZONTAL_GAP)
    }
}

@Test
fun `platform gaps are within valid range`() {
    val level = Level.generate(chapterId = 1, levelNumber = 1, seed = 12345)
    val difficulty = level.difficulty
    val maxGap = when (difficulty) {
        Difficulty.EASY -> GameConstants.MAX_HORIZONTAL_GAP_EASY
        Difficulty.MEDIUM -> GameConstants.MAX_HORIZONTAL_GAP_MEDIUM
        Difficulty.HARD -> GameConstants.MAX_HORIZONTAL_GAP_HARD
        Difficulty.EXPERT -> GameConstants.MAX_HORIZONTAL_GAP_EXPERT
    }
    
    for (i in 0 until level.platforms.size - 1) {
        val current = level.platforms[i]
        val next = level.platforms[i + 1]
        val gap = next.left - current.right
        
        assertTrue(gap <= maxGap)
    }
}
```

- [ ] **Step 5: 运行测试验证**

Run: `./gradlew :domain:commonTest --tests "com.gameway.domain.model.LevelGenerationTest"`
Expected: PASS (including new tests)

- [ ] **Step 6: Commit**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt domain/src/commonTest/kotlin/com/gameway/domain/model/LevelGenerationTest.kt
git commit -m "feat: platform generation with overlap check and gap validation"
```

---

### Task 4.3: 手动测试所有关卡可达

**Files:**
- None (manual testing)

- [ ] **Step 1: 构建并运行应用**

Run: `./gradlew :app:assembleDebug`
Expected: Build succeeds

- [ ] **Step 2: 安装到设备或模拟器**

Run: `adb install app/build/outputs/apk/debug/app-debug.apk`
Expected: Installation succeeds

- [ ] **Step 3: 手动测试游戏流程**

测试步骤：
1. 启动应用，进入主菜单
2. 点击"选择角色"，选择小猫、小狗、小马各一次
3. 开始游戏，验证角色固定在左侧
4. 观察角色奔跑时腿部动画
5. 完成关卡1-1到1-3，验证所有平台可达
6. 验证平台间隙清晰、无重叠

- [ ] **Step 4: 记录测试结果**

如果发现问题，记录并修复。

---

## 实现完成

所有任务完成后，进行最终提交：

```bash
git add -A
git commit -m "feat: complete game visual improvements

- Add character selection (CAT, DOG, HORSE)
- Character fixed at screen left-center position
- Add leg running animation
- Optimize platform generation (no overlap, clear gaps)"
git push
```

---

**计划状态:** 已完成  
**下一步:** 执行计划（Subagent-Driven或Inline Execution）