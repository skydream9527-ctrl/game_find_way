# 游戏画面与操作细节改进设计文档

**日期:** 2026-04-13  
**项目:** FindWay Game  
**目标:** 增加角色选择、固定角色位置、腿部跑动动画、优化平台布局

---

## 1. 概述

### 当前状态
- 角色固定为一个类型，无选择功能
- 角色在屏幕上随视口移动，位置不固定
- 角色动画简单，无腿部跑动效果
- 平台生成存在潜在重叠问题

### 改进目标
1. 用户可选择角色（小猫、小狗、小马）
2. 角色固定在屏幕左侧垂直居中
3. 角色奔跑时腿部有摆动动画
4. 平台位置清晰、不重叠、间隙合理

---

## 2. 角色系统设计

### 2.1 角色类型枚举

```kotlin
enum class CharacterType {
    CAT,    // 小猫
    DOG,    // 小狗
    HORSE   // 小马
}
```

### 2.2 角色配置数据

每个角色有轻微属性差异：

| 角色 | 名称 | Emoji | 速度倍数 | 跳跃力倍数 |
|------|------|-------|---------|-----------|
| 小猫 | 小猫 | 🐱 | 1.0 | 1.0 |
| 小狗 | 小狗 | 🐕 | 0.9 | 1.1 |
| 小马 | 小马 | 🐴 | 1.2 | 0.95 |

```kotlin
data class CharacterConfig(
    val type: CharacterType,
    val name: String,
    val emoji: String,
    val moveSpeedMultiplier: Float,
    val jumpPowerMultiplier: Float
) {
    companion object {
        val CAT = CharacterConfig(CharacterType.CAT, "小猫", "🐱", 1.0f, 1.0f)
        val DOG = CharacterConfig(CharacterType.DOG, "小狗", "🐕", 0.9f, 1.1f)
        val HORSE = CharacterConfig(CharacterType.HORSE, "小马", "🐴", 1.2f, 0.95f)
        
        fun getAll() = listOf(CAT, DOG, HORSE)
        fun getByType(type: CharacterType) = getAll().find { it.type == type } ?: CAT
    }
}
```

### 2.3 Character模型更新

```kotlin
data class Character(
    val position: Vector2,
    val velocity: Vector2,
    val state: CharacterState,
    val type: CharacterType,          // 新增
    val config: CharacterConfig,      // 新增
    val isGrounded: Boolean,
    val jumpCount: Int,
    val maxJumps: Int,
    val activePowerUps: List<ActivePowerUp>,
    val health: Int,
    val coinsCollected: Int
) {
    val effectiveMoveSpeed: Float
        get() = GameConstants.MOVE_SPEED * config.moveSpeedMultiplier *
                (if (hasActivePowerUp(PowerUpType.LIGHTNING)) 1.3f else 1.0f)
    
    val effectiveJumpPower: Float
        get() = GameConstants.JUMP_POWER * config.jumpPowerMultiplier
    
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
}
```

### 2.4 PhysicsSystem更新

```kotlin
object PhysicsSystem {
    fun applyJump(character: Character): Character {
        return character.copy(
            velocity = Vector2(character.effectiveMoveSpeed, -character.effectiveJumpPower),
            isGrounded = false,
            jumpCount = character.jumpCount + 1,
            state = CharacterState.JUMPING
        )
    }
    
    // 其他方法使用 character.effectiveMoveSpeed 和 character.effectiveJumpPower
}
```

---

## 3. 角色选择界面

### 3.1 界面位置
- 在主菜单添加"选择角色"按钮
- 点击后进入独立的角色选择界面

### 3.2 CharacterSelectScreen

```kotlin
@Composable
fun CharacterSelectScreen(
    currentCharacter: CharacterType,
    onCharacterSelected: (CharacterType) -> Unit,
    onBack: () -> Unit
) {
    val characters = CharacterConfig.getAll()
    var selectedType by remember { mutableStateOf(currentCharacter) }
    
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF4A148C))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("选择你的角色", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                characters.forEach { config ->
                    CharacterCard(
                        config = config,
                        isSelected = selectedType == config.type,
                        onClick = { selectedType = config.type }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 属性对比
            AttributeComparisonPanel(selectedType)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("返回")
                }
                Button(onClick = { onCharacterSelected(selectedType) }, 
                       colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("确认选择")
                }
            }
        }
    }
}
```

### 3.3 CharacterCard组件

```kotlin
@Composable
fun CharacterCard(config: CharacterConfig, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(width = 100.dp, height = 140.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(config.emoji, fontSize = 48.sp)
            Text(config.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 属性简要显示
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsRun, contentDescription = "速度", modifier = Modifier.size(16.dp))
                Text("${(config.moveSpeedMultiplier * 100).toInt()}%", fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "跳力", modifier = Modifier.size(16.dp))
                Text("${(config.jumpPowerMultiplier * 100).toInt()}%", fontSize = 12.sp)
            }
        }
    }
}
```

### 3.4 主菜单更新

```kotlin
@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onSelectCharacter: () -> Unit,
    onViewStats: () -> Unit
) {
    // 添加"选择角色"按钮在"开始游戏"和"角色属性"之间
    Button(onClick = onStartGame) { Text("开始游戏") }
    Button(onClick = onSelectCharacter, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))) { 
        Text("选择角色") 
    }
    Button(onClick = onViewStats) { Text("角色属性") }
}
```

### 3.5 导航路由更新

```kotlin
sealed class Screen {
    object MainMenu : Screen()
    object CharacterSelect : Screen()  // 新增
    object ChapterSelect : Screen()
    object LevelSelect : Screen()
    object Game : Screen(chapterId: Int, levelNumber: Int, characterType: CharacterType)  // 更新参数
    object Stats : Screen()
}
```

---

## 4. 游戏画面渲染改进

### 4.1 角色固定屏幕左侧

**核心原理：**
- 角色世界坐标 `character.position.x` 正常增加（物理系统不变）
- 渲染时角色屏幕坐标固定在左侧垂直居中
- 视口滚动跟随角色世界坐标

```kotlin
@Composable
fun GameCanvas(viewModel: GameViewModel) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // 角色固定屏幕位置
        val CHARACTER_SCREEN_X = canvasWidth * 0.15f
        val CHARACTER_SCREEN_Y = canvasHeight * 0.5f
        
        // 视口基于角色世界坐标
        val viewportX = uiState.character.position.x - CHARACTER_SCREEN_X
        
        // 渲染平台（相对于视口）
        for (platform in level.platforms) {
            val screenX = platform.x - viewportX
            val platformWidth = platform.width
            
            // 只渲染可见范围内的平台（角色前后）
            if (screenX > -platformWidth && screenX < canvasWidth + platformWidth) {
                drawPlatform(screenX, platform.y, platform.width, theme.platformColor)
            }
        }
        
        // 渲染角色（固定位置）
        drawCharacter(uiState.character, CHARACTER_SCREEN_X, CHARACTER_SCREEN_Y, animationFrame)
        
        // 渲染金币和道具（相对于视口）
        // ...
    }
}
```

### 4.2 腿部跑动动画

**动画参数：**
- 周期：8帧（约133ms）
- 角度范围：-15° 到 +15°
- 仅在 RUNNING 状态时动画

```kotlin
fun drawCharacter(
    character: Character,
    screenX: Float,
    screenY: Float,
    animationFrame: Int
) {
    val config = character.config
    
    // 计算腿部角度（仅在奔跑时）
    val legAngle = if (character.state == CharacterState.RUNNING) {
        val cycle = (animationFrame % 8) / 8f
        sin(cycle * PI * 2) * 15f  // -15° 到 +15°
    } else {
        0f
    }
    
    // 根据角色类型设置颜色
    val bodyColor = when (character.type) {
        CharacterType.CAT -> Color(0xFFFFB74D)  // 橙色
        CharacterType.DOG -> Color(0xFF8D6E63)  // 棕色
        CharacterType.HORSE -> Color(0xFF9E9E9E) // 灰色
    }
    
    val headColor = when (character.type) {
        CharacterType.CAT -> Color(0xFFFFE0B2)
        CharacterType.DOG -> Color(0xFFD7CCC8)
        CharacterType.HORSE -> Color(0xFFBDBDBD)
    }
    
    // 绘制身体
    drawCircle(bodyColor, radius = 20f, center = Offset(screenX, screenY - 15f))
    
    // 绘制头部
    drawCircle(headColor, radius = 15f, center = Offset(screenX, screenY - 35f))
    
    // 绘制眼睛
    drawCircle(Color.Black, radius = 3f, center = Offset(screenX - 6f, screenY - 38f))
    drawCircle(Color.Black, radius = 3f, center = Offset(screenX + 6f, screenY - 38f))
    
    // 绘制腿部（带动画）
    val leftLegStart = Offset(screenX - 8f, screenY)
    val leftLegEnd = calculateLegEnd(leftLegStart, legAngle, 25f)
    drawLine(Color(0xFF333333), leftLegStart, leftLegEnd, strokeWidth = 6f)
    
    val rightLegStart = Offset(screenX + 8f, screenY)
    val rightLegEnd = calculateLegEnd(rightLegStart, -legAngle, 25f)
    drawLine(Color(0xFF333333), rightLegStart, rightLegEnd, strokeWidth = 6f)
}

fun calculateLegEnd(start: Offset, angle: Float, length: Float): Offset {
    val radians = angle * PI / 180f
    return Offset(
        start.x + sin(radians) * length,
        start.y + cos(radians) * length
    )
}
```

### 4.3 动画帧管理

```kotlin
data class GameUiState(
    val character: Character = Character.createDefault(),
    val gameState: GameState = GameState.Loading,
    val scrollX: Float = 0f,
    val score: Int = 0,
    val animationFrame: Int = 0,  // 新增
    // ...
)

class GameViewModel {
    private var animationFrame = 0
    
    private fun startGameLoop() {
        viewModelScope.launch {
            while (gameLoopActive) {
                gameEngine.update()
                animationFrame++
                
                _uiState.value = GameUiState(
                    character = gameEngine.getCharacter(),
                    animationFrame = animationFrame,
                    // ...
                )
                
                delay(16L)
            }
        }
    }
}
```

---

## 5. 平台生成优化

### 5.1 平台不重叠检测

```kotlin
private fun generatePlatforms(count: Int, difficulty: Difficulty, random: Random): List<Platform> {
    val platforms = mutableListOf<Platform>()
    
    for (i in 0 until count) {
        var attempts = 0
        var validPlatform: Platform? = null
        
        while (validPlatform == null && attempts < 10) {
            val candidate = generateCandidatePlatform(i, platforms, difficulty, random)
            
            if (!hasOverlap(candidate, platforms) && ensureReachability(candidate, platforms.lastOrNull())) {
                validPlatform = candidate
            }
            attempts++
        }
        
        validPlatform?.let { platforms.add(it) }
    }
    
    return platforms
}

private fun hasOverlap(candidate: Platform, existing: List<Platform>): Boolean {
    val verticalOverlapRange = 50f
    
    return existing.any { platform ->
        val horizontalOverlap = candidate.left < platform.right && candidate.right > platform.left
        val verticalClose = abs(candidate.y - platform.y) < verticalOverlapRange
        horizontalOverlap && verticalClose
    }
}
```

### 5.2 平台间隙规则

**Constants.kt 新增：**

```kotlin
const val MIN_HORIZONTAL_GAP = 30f
const val MAX_HORIZONTAL_GAP_EASY = 120f
const val MAX_HORIZONTAL_GAP_MEDIUM = 160f
const val MAX_HORIZONTAL_GAP_HARD = 180f
const val MAX_HORIZONTAL_GAP_EXPERT = 200f
```

**间隙计算：**

```kotlin
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
    val yVariation = random.nextFloat(-60f, 80f)
    val y = (previous?.y?.plus(yVariation) ?: GameConstants.STARTING_PLATFORM_Y)
        .coerceIn(GameConstants.MIN_PLATFORM_Y, GameConstants.MAX_PLATFORM_Y)
    
    val width = getWidthForDifficulty(difficulty, random)
    
    return Platform(
        id = index,
        x = x,
        y = y,
        width = width,
        type = determineType(difficulty, index, count, random),
        // ...
    )
}

private fun getMaxGapForDifficulty(difficulty: Difficulty): Float {
    return when (difficulty) {
        Difficulty.EASY -> GameConstants.MAX_HORIZONTAL_GAP_EASY
        Difficulty.MEDIUM -> GameConstants.MAX_HORIZONTAL_GAP_MEDIUM
        Difficulty.HARD -> GameConstants.MAX_HORIZONTAL_GAP_HARD
        Difficulty.EXPERT -> GameConstants.MAX_HORIZONTAL_GAP_EXPERT
    }
}
```

### 5.3 平台可达性验证

```kotlin
private fun ensureReachability(candidate: Platform, previous: Platform?): Boolean {
    if (previous == null) return true
    
    val horizontalGap = candidate.left - previous.right
    val verticalDiff = previous.y - candidate.y
    
    return horizontalGap <= GameConstants.MAX_HORIZONTAL_JUMP_DISTANCE &&
           verticalDiff <= GameConstants.MAX_VERTICAL_JUMP_HEIGHT
}
```

---

## 6. 实现步骤（渐进式改造）

### 阶段1：角色系统数据层
1. 创建 `CharacterType` 枚举
2. 创建 `CharacterConfig` 数据类
3. 更新 `Character` 模型添加 `type` 和 `config`
4. 更新 `PhysicsSystem` 使用 `effectiveJumpPower`
5. 运行单元测试验证

### 阶段2：角色选择界面
1. 创建 `CharacterSelectScreen.kt`
2. 创建 `CharacterCard` 和 `AttributeComparisonPanel` 组件
3. 更新 `MainMenuScreen` 添加按钮
4. 更新导航路由
5. 手动测试界面流程

### 阶段3：游戏画面渲染
1. 更新 `GameCanvas` 角色固定位置逻辑
2. 实现腿部动画绘制函数
3. 更新 `GameViewModel` 添加动画帧
4. 手动测试视觉效果

### 阶段4：平台生成优化
1. 更新 `Constants.kt` 间隙常量
2. 更新 `Level.kt` 平台生成逻辑
3. 运行 `PlatformReachabilityTest` 验证
4. 手动测试关卡

---

## 7. 文件修改清单

**新建文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/CharacterType.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/CharacterConfig.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/character/CharacterSelectScreen.kt`

**修改文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt`
- `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`
- `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt`
- `core/src/commonMain/kotlin/com/gameway/core/Constants.kt`

---

## 8. 验收标准

1. ✅ 用户可在主菜单点击"选择角色"
2. ✅ 角色选择界面显示小猫、小狗、小马
3. ✅ 选择角色后游戏使用对应角色
4. ✅ 角色固定在屏幕左侧15%、垂直居中位置
5. ✅ 角色奔跑时腿部有交替摆动动画
6. ✅ 不同角色有轻微属性差异
7. ✅ 平台不重叠
8. ✅ 平台间隙清晰、最小30像素
9. ✅ 所有平台可达

---

**文档状态:** 已完成  
**下一步:** 用户审核设计文档，然后调用 writing-plans skill 创建实现计划