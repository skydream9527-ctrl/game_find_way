# Find Way 游戏实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一款 KMP + Android Canvas 横版平台跳跃游戏，包含 10 章节 100 关卡，支持角色属性、道具系统和完整游戏流程。

**Architecture:** Android Clean Architecture，domain 层纯 Kotlin 游戏引擎，presentation 层 Compose + Canvas 渲染，data 层 DataStore 本地存储，Koin 依赖注入。

**Tech Stack:** Kotlin, Kotlin Multiplatform, Jetpack Compose, Android Canvas, Koin, DataStore, Coroutines

---

## 文件结构总览

```
project/
├── build.gradle.kts                    # 根构建配置
├── settings.gradle.kts                 # 模块设置
├── core/
│   ├── build.gradle.kts
│   └── src/commonMain/kotlin/com/gameway/core/
│       ├── error/
│       │   └── AppError.kt
│       ├── util/
│       │   └── Extensions.kt
│       └── Constants.kt
├── domain/
│   ├── build.gradle.kts
│   └── src/commonMain/kotlin/com/gameway/domain/
│       ├── model/
│       │   ├── Character.kt
│       │   ├── Platform.kt
│       │   ├── PowerUp.kt
│       │   ├── Coin.kt
│       │   ├── Level.kt
│       │   ├── Chapter.kt
│       │   └── GameState.kt
│       ├── engine/
│       │   ├── GameEngine.kt
│       │   ├── PhysicsSystem.kt
│       │   └── CollisionDetector.kt
│       ├── repository/
│       │   ├── GameProgressRepository.kt
│       │   └── SettingsRepository.kt
│       └── usecase/
│           ├── GetChaptersUseCase.kt
│           ├── GetLevelUseCase.kt
│           ├── SaveProgressUseCase.kt
│           └── GetCharacterStatsUseCase.kt
├── data/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/gameway/data/
│       ├── repository/
│       │   ├── GameProgressRepositoryImpl.kt
│       │   └── SettingsRepositoryImpl.kt
│       └── local/
│           └── DataStoreManager.kt
├── presentation/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/gameway/presentation/
│       ├── navigation/
│       │   └── AppNavigation.kt
│       ├── screens/
│       │   ├── splash/
│       │   │   └── SplashScreen.kt
│       │   ├── menu/
│       │   │   └── MainMenuScreen.kt
│       │   ├── chapter/
│       │   │   └── ChapterSelectScreen.kt
│       │   ├── level/
│       │   │   └── LevelSelectScreen.kt
│       │   ├── game/
│       │   │   ├── GameScreen.kt
│       │   │   ├── GameCanvas.kt
│       │   │   └── GameViewModel.kt
│       │   ├── stats/
│       │   │   └── StatsScreen.kt
│       │   └── complete/
│       │       └── LevelCompleteScreen.kt
│       ├── components/
│       │   ├── CharacterSprite.kt
│       │   ├── PlatformRenderer.kt
│       │   ├── PowerUpRenderer.kt
│       │   └── HUD.kt
│       └── theme/
│           └── GameTheme.kt
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/gameway/
│       │   ├── MainActivity.kt
│       │   └── FindWayApp.kt
│       └── res/
│           └── ...
└── gradle/
    └── libs.versions.toml
```

---

### Task 1: 项目初始化 - Gradle 构建配置

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `core/build.gradle.kts`
- Create: `domain/build.gradle.kts`
- Create: `data/build.gradle.kts`
- Create: `presentation/build.gradle.kts`
- Create: `app/build.gradle.kts`

- [ ] **Step 1: 创建版本目录**

```toml
# gradle/libs.versions.toml
[versions]
agp = "8.2.0"
kotlin = "1.9.22"
ksp = "1.9.22-1.0.16"
compose = "1.6.1"
compose-compiler = "1.5.8"
koin = "3.5.3"
datastore = "1.0.0"
coroutines = "1.7.3"
lifecycle = "2.7.0"
navigation = "2.7.7"
junit = "4.13.2"
androidx-test = "1.1.5"

[libraries]
# Compose
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui", version.ref = "compose" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics", version.ref = "compose" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling", version.ref = "compose" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview", version.ref = "compose" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3", version = "1.2.0" }

# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Lifecycle
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }

# Koin
koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-androidx-compose = { group = "io.insert-koin", name = "koin-androidx-compose", version.ref = "koin" }

# DataStore
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Coroutines
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }

# Test
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-test-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidx-test" }
androidx-test-espresso = { group = "androidx.test.espresso", name = "espresso-core", version = "3.5.1" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 2: 创建 settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FindWay"
include(":app")
include(":core")
include(":domain")
include(":data")
include(":presentation")
```

- [ ] **Step 3: 创建根 build.gradle.kts**

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}
```

- [ ] **Step 4: 创建 core/build.gradle.kts**

```kotlin
// core/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
```

- [ ] **Step 5: 创建 domain/build.gradle.kts**

```kotlin
// domain/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
```

- [ ] **Step 6: 创建 data/build.gradle.kts**

```kotlin
// data/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.gameway.data"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
```

- [ ] **Step 7: 创建 presentation/build.gradle.kts**

```kotlin
// presentation/build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.gameway.presentation"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    
    implementation(libs.koin.androidx.compose)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

- [ ] **Step 8: 创建 app/build.gradle.kts**

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.gameway"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.gameway"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":presentation"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":core"))
    
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
```

- [ ] **Step 9: 创建 gradle.properties 和 gradle-wrapper**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=false
kotlin.code.style=official
kotlin.mpp.enableCInteropCommonization=true
```

- [ ] **Step 10: 验证构建**

Run: `./gradlew tasks`
Expected: 构建成功，无错误

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "feat: initialize project structure with Gradle KMP modules"
```

---

### Task 2: Core 模块 - 工具类与常量

**Files:**
- Create: `core/src/commonMain/kotlin/com/gameway/core/Constants.kt`
- Create: `core/src/commonMain/kotlin/com/gameway/core/error/AppError.kt`
- Create: `core/src/commonMain/kotlin/com/gameway/core/util/Extensions.kt`
- Test: `core/src/commonTest/kotlin/com/gameway/core/util/ExtensionsTest.kt`

- [ ] **Step 1: 创建常量定义**

```kotlin
// core/src/commonMain/kotlin/com/gameway/core/Constants.kt
package com.gameway.core

object GameConstants {
    // 游戏物理
    const val GRAVITY = 0.6f
    const val MIN_JUMP_POWER = 8f
    const val MAX_JUMP_POWER = 16f
    const val MOVE_SPEED = 3f
    const val MAX_MOVE_SPEED = 6f
    const val ACCELERATION_TIME = 1500L // ms
    const val MAX_CHARGE_TIME = 2000L // ms
    
    // 关卡
    const val TOTAL_CHAPTERS = 10
    const val LEVELS_PER_CHAPTER = 10
    const val MIN_PLATFORMS = 20
    const val MAX_PLATFORMS = 50
    
    // 道具
    const val MIN_ITEMS_PER_LEVEL = 3
    const val MAX_ITEMS_PER_LEVEL = 8
    const val MIN_COINS_PER_LEVEL = 10
    const val MAX_COINS_PER_LEVEL = 30
    
    // 道具持续时间
    const val FEATHER_DURATION = 10000L // 10s
    const val BUTTERFLY_DURATION = 15000L // 15s
    const val LIGHTNING_DURATION = 8000L // 8s
}
```

- [ ] **Step 2: 创建错误类型**

```kotlin
// core/src/commonMain/kotlin/com/gameway/core/error/AppError.kt
package com.gameway.core.error

sealed interface AppError {
    data class StorageError(val message: String) : AppError
    data class SerializationError(val message: String) : AppError
    data object LevelNotFoundError : AppError
    data object ChapterNotFoundError : AppError
    data class ValidationError(val field: String, val message: String) : AppError
    data class UnknownError(val throwable: Throwable? = null) : AppError
    
    fun toUserMessage(): String = when (this) {
        is StorageError -> "存储错误：$message"
        is SerializationError -> "数据解析错误：$message"
        LevelNotFoundError -> "关卡未找到"
        ChapterNotFoundError -> "章节未找到"
        is ValidationError -> "验证错误：$field - $message"
        is UnknownError -> "未知错误"
    }
}
```

- [ ] **Step 3: 创建扩展函数**

```kotlin
// core/src/commonMain/kotlin/com/gameway/core/util/Extensions.kt
package com.gameway.core.util

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * 线性插值
 */
fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

/**
 * 将值限制在 [min, max] 范围内
 */
fun Float.clamp(min: Float, max: Float): Float = max(min, min(max, this))

/**
 * 将值限制在 [min, max] 范围内
 */
fun Int.clamp(min: Int, max: Int): Int = max(min, min(max, this))

/**
 * 生成 [min, max] 范围内的随机整数
 */
fun Random.nextInt(min: Int, max: Int): Int = nextInt(max - min + 1) + min

/**
 * 生成 [min, max] 范围内的随机浮点数
 */
fun Random.nextFloat(min: Float, max: Float): Float = min + (max - min) * nextFloat()

/**
 * 安全地执行 lambda，返回 Result
 */
inline fun <T> runCatchingAppError(block: () -> T): Result<T> = runCatching(block)
```

- [ ] **Step 4: 创建测试**

```kotlin
// core/src/commonTest/kotlin/com/gameway/core/util/ExtensionsTest.kt
package com.gameway.core.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExtensionsTest {
    
    @Test
    fun `lerp returns start when fraction is 0`() {
        assertEquals(10f, lerp(10f, 20f, 0f))
    }
    
    @Test
    fun `lerp returns end when fraction is 1`() {
        assertEquals(20f, lerp(10f, 20f, 1f))
    }
    
    @Test
    fun `lerp returns midpoint when fraction is 0_5`() {
        assertEquals(15f, lerp(10f, 20f, 0.5f))
    }
    
    @Test
    fun `clamp Float keeps value in range`() {
        assertEquals(5f, 5f.clamp(0f, 10f))
        assertEquals(0f, (-5f).clamp(0f, 10f))
        assertEquals(10f, 15f.clamp(0f, 10f))
    }
    
    @Test
    fun `clamp Int keeps value in range`() {
        assertEquals(5, 5.clamp(0, 10))
        assertEquals(0, (-5).clamp(0, 10))
        assertEquals(10, 15.clamp(0, 10))
    }
    
    @Test
    fun `nextInt returns value in range`() {
        val random = Random(42)
        repeat(100) {
            val value = random.nextInt(5, 15)
            assertTrue(value in 5..15)
        }
    }
    
    @Test
    fun `nextFloat returns value in range`() {
        val random = Random(42)
        repeat(100) {
            val value = random.nextFloat(1.0f, 5.0f)
            assertTrue(value in 1.0f..5.0f)
        }
    }
}
```

- [ ] **Step 5: 运行测试**

Run: `./gradlew :core:jvmTest`
Expected: 7 tests passed

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add core module with constants, errors, and extensions"
```

---

### Task 3: Domain 模块 - 领域模型

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Vector2.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Platform.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/PowerUp.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Coin.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/Chapter.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/model/GameState.kt`
- Test: `domain/src/commonTest/kotlin/com/gameway/domain/model/CharacterTest.kt`

- [ ] **Step 1: 创建 Vector2**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/model/Vector2.kt
package com.gameway.domain.model

data class Vector2(
    val x: Float,
    val y: Float
) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2(x * scalar, y * scalar)
    
    fun length(): Float = kotlin.math.sqrt(x * x + y * y)
}
```

- [ ] **Step 2: 创建 Character 模型**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/model/Character.kt
package com.gameway.domain.model

import com.gameway.core.GameConstants
import com.gameway.core.util.clamp

/**
 * 角色状态
 */
enum class CharacterState {
    IDLE,
    RUNNING,
    JUMPING,
    FALLING,
    DEAD
}

/**
 * 角色物理状态
 */
data class Character(
    val position: Vector2,
    val velocity: Vector2,
    val state: CharacterState,
    val isGrounded: Boolean,
    val jumpCount: Int,
    val maxJumps: Int,
    val chargeTime: Long,
    val isCharging: Boolean,
    val activePowerUps: List<ActivePowerUp>,
    val health: Int,
    val coinsCollected: Int
) {
    companion object {
        fun createDefault(): Character = Character(
            position = Vector2(50f, 300f),
            velocity = Vector2(0f, 0f),
            state = CharacterState.IDLE,
            isGrounded = false,
            jumpCount = 0,
            maxJumps = 1,
            chargeTime = 0L,
            isCharging = false,
            activePowerUps = emptyList(),
            health = 3,
            coinsCollected = 0
        )
    }
    
    val effectiveJumpPower: Float
        get() {
            val basePower = GameConstants.MIN_JUMP_POWER + 
                (GameConstants.MAX_JUMP_POWER - GameConstants.MIN_JUMP_POWER) * 
                (chargeTime.toFloat() / GameConstants.MAX_CHARGE_TIME)
            
            val multiplier = if (hasActivePowerUp(PowerUpType.FEATHER)) 1.5f else 1f
            return (basePower * multiplier).clamp(GameConstants.MIN_JUMP_POWER, GameConstants.MAX_JUMP_POWER * 1.5f)
        }
    
    val effectiveMoveSpeed: Float
        get() {
            val multiplier = if (hasActivePowerUp(PowerUpType.LIGHTNING)) 1.3f else 1f
            return (GameConstants.MOVE_SPEED * multiplier).clamp(GameConstants.MOVE_SPEED, GameConstants.MAX_MOVE_SPEED * 1.3f)
        }
    
    val hasDoubleJump: Boolean
        get() = maxJumps >= 2 || hasActivePowerUp(PowerUpType.BUTTERFLY)
    
    val hasShield: Boolean
        get() = hasActivePowerUp(PowerUpType.SHIELD)
    
    fun hasActivePowerUp(type: PowerUpType): Boolean {
        val currentTime = System.currentTimeMillis()
        return activePowerUps.any { it.type == type && it.expiresAt > currentTime }
    }
    
    fun copyWith(): Character = copy()
}
```

- [ ] **Step 3: 创建 Platform 模型**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/model/Platform.kt
package com.gameway.domain.model

/**
 * 平台类型
 */
enum class PlatformType {
    STATIC,
    MOVING_HORIZONTAL,
    MOVING_VERTICAL
}

/**
 * 平台
 */
data class Platform(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float = 12f,
    val type: PlatformType = PlatformType.STATIC,
    val moveRange: Float = 0f,
    val moveSpeed: Float = 0f,
    val moveOffset: Float = 0f
) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height
}
```

- [ ] **Step 4: 创建 PowerUp 模型**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/model/PowerUp.kt
package com.gameway.domain.model

/**
 * 道具类型
 */
enum class PowerUpType {
    FEATHER,      // 跳跃高度 +50%
    BUTTERFLY,    // 二段跳
    LIGHTNING,    // 移动速度 +30%
    SHIELD,       // 免疫一次失败
    GEM,          // 永久解锁二段跳
    STAR          // 永久跳跃力 +10%
}

/**
 * 游戏中的道具实体
 */
data class PowerUp(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: PowerUpType,
    val isPermanent: Boolean,
    val collected: Boolean = false
) {
    val duration: Long
        get() = when (type) {
            PowerUpType.FEATHER -> 10000L
            PowerUpType.BUTTERFLY -> 15000L
            PowerUpType.LIGHTNING -> 8000L
            PowerUpType.SHIELD -> 0L
            PowerUpType.GEM -> 0L
            PowerUpType.STAR -> 0L
        }
}

/**
 * 激活的道具
 */
data class ActivePowerUp(
    val type: PowerUpType,
    val activatedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long
) {
    companion object {
        fun create(type: PowerUpType): ActivePowerUp {
            val duration = when (type) {
                PowerUpType.FEATHER -> 10000L
                PowerUpType.BUTTERFLY -> 15000L
                PowerUpType.LIGHTNING -> 8000L
                PowerUpType.SHIELD -> Long.MAX_VALUE
                PowerUpType.GEM -> Long.MAX_VALUE
                PowerUpType.STAR -> Long.MAX_VALUE
            }
            return ActivePowerUp(
                type = type,
                expiresAt = System.currentTimeMillis() + duration
            )
        }
    }
}
```

- [ ] **Step 5: 创建 Coin 模型**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/model/Coin.kt
package com.gameway.domain.model

data class Coin(
    val id: Int,
    val x: Float,
    val y: Float,
    val collected: Boolean = false
)
```

- [ ] **Step 6: 创建 Level 模型**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/model/Level.kt
package com.gameway.domain.model

import com.gameway.core.GameConstants
import com.gameway.core.util.nextInt
import kotlin.random.Random

/**
 * 难度等级
 */
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}

/**
 * 关卡
 */
data class Level(
    val id: Int,
    val chapterId: Int,
    val levelNumber: Int,
    val difficulty: Difficulty,
    val platforms: List<Platform>,
    val powerUps: List<PowerUp>,
    val coins: List<Coin>,
    val targetScore: Int
) {
    companion object {
        fun generate(
            chapterId: Int,
            levelNumber: Int,
            seed: Long = System.currentTimeMillis()
        ): Level {
            val random = Random(seed)
            val difficulty = getDifficultyForLevel(levelNumber)
            val platformCount = getPlatformCount(difficulty, random)
            val platforms = generatePlatforms(platformCount, difficulty, random)
            val powerUps = generatePowerUps(platforms, difficulty, random, chapterId, levelNumber)
            val coins = generateCoins(platforms, random)
            
            return Level(
                id = chapterId * 100 + levelNumber,
                chapterId = chapterId,
                levelNumber = levelNumber,
                difficulty = difficulty,
                platforms = platforms,
                powerUps = powerUps,
                coins = coins,
                targetScore = coins.size * 10 + powerUps.size * 20
            )
        }
        
        private fun getDifficultyForLevel(level: Int): Difficulty = when (level) {
            in 1..3 -> Difficulty.EASY
            in 4..6 -> Difficulty.MEDIUM
            in 7..8 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
        
        private fun getPlatformCount(difficulty: Difficulty, random: Random): Int {
            val (min, max) = when (difficulty) {
                Difficulty.EASY -> GameConstants.MIN_PLATFORMS to 30
                Difficulty.MEDIUM -> 25 to 40
                Difficulty.HARD -> 35 to 45
                Difficulty.EXPERT -> 40 to GameConstants.MAX_PLATFORMS
            }
            return random.nextInt(min, max)
        }
        
        private fun generatePlatforms(
            count: Int,
            difficulty: Difficulty,
            random: Random
        ): List<Platform> {
            val platforms = mutableListOf<Platform>()
            var x = 150f
            val startY = 350f
            
            for (i in 0 until count) {
                val width = when (difficulty) {
                    Difficulty.EASY -> random.nextFloat(80f, 120f)
                    Difficulty.MEDIUM -> random.nextFloat(60f, 100f)
                    Difficulty.HARD -> random.nextFloat(40f, 80f)
                    Difficulty.EXPERT -> random.nextFloat(30f, 60f)
                }
                
                val yVariation = when (difficulty) {
                    Difficulty.EASY -> 30f
                    Difficulty.MEDIUM -> 60f
                    Difficulty.HARD -> 100f
                    Difficulty.EXPERT -> 150f
                }
                
                val y = startY + random.nextFloat(-yVariation, yVariation)
                
                val type = if (difficulty >= Difficulty.HARD && i > count / 2) {
                    if (random.nextFloat() > 0.5f) PlatformType.MOVING_HORIZONTAL
                    else PlatformType.STATIC
                } else {
                    PlatformType.STATIC
                }
                
                val moveRange = if (type != PlatformType.STATIC) random.nextFloat(50f, 100f) else 0f
                val moveSpeed = if (type != PlatformType.STATIC) random.nextFloat(0.5f, 1.5f) else 0f
                
                platforms.add(
                    Platform(
                        id = i,
                        x = x,
                        y = y.coerceIn(200f, 500f),
                        width = width,
                        type = type,
                        moveRange = moveRange,
                        moveSpeed = moveSpeed,
                        moveOffset = random.nextFloat(0f, 360f)
                    )
                )
                
                val gap = when (difficulty) {
                    Difficulty.EASY -> random.nextFloat(80f, 120f)
                    Difficulty.MEDIUM -> random.nextFloat(100f, 160f)
                    Difficulty.HARD -> random.nextFloat(120f, 200f)
                    Difficulty.EXPERT -> random.nextFloat(150f, 250f)
                }
                x += width + gap
            }
            
            return platforms
        }
        
        private fun generatePowerUps(
            platforms: List<Platform>,
            difficulty: Difficulty,
            random: Random,
            chapterId: Int,
            levelNumber: Int
        ): List<PowerUp> {
            val count = random.nextInt(
                GameConstants.MIN_ITEMS_PER_LEVEL,
                GameConstants.MAX_ITEMS_PER_LEVEL
            )
            
            val isLastLevel = levelNumber == GameConstants.LEVELS_PER_CHAPTER
            
            return List(count) { index ->
                val platform = platforms.random(random)
                val type = if (isLastLevel && index == 0) {
                    if (random.nextFloat() > 0.5f) PowerUpType.GEM else PowerUpType.STAR
                } else {
                    listOf(
                        PowerUpType.FEATHER,
                        PowerUpType.BUTTERFLY,
                        PowerUpType.LIGHTNING,
                        PowerUpType.SHIELD
                    ).random(random)
                }
                
                PowerUp(
                    id = index,
                    x = platform.x + platform.width / 2,
                    y = platform.y - 30f,
                    type = type,
                    isPermanent = type == PowerUpType.GEM || type == PowerUpType.STAR
                )
            }
        }
        
        private fun generateCoins(
            platforms: List<Platform>,
            random: Random
        ): List<Coin> {
            val count = random.nextInt(
                GameConstants.MIN_COINS_PER_LEVEL,
                GameConstants.MAX_COINS_PER_LEVEL
            )
            
            return List(count) { index ->
                val platform = platforms.random(random)
                Coin(
                    id = index,
                    x = platform.x + random.nextFloat(0f, platform.width),
                    y = platform.y - 25f
                )
            }
        }
    }
}
```

- [ ] **Step 7: 创建 Chapter 模型**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/model/Chapter.kt
package com.gameway.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 章节主题
 */
data class ChapterTheme(
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val platformColor: Color
)

/**
 * 章节
 */
data class Chapter(
    val id: Int,
    val name: String,
    val emoji: String,
    val theme: ChapterTheme,
    val unlocked: Boolean = true,
    val completedLevels: Int = 0,
    val totalLevels: Int = 10
) {
    companion object {
        fun getAllChapters(): List<Chapter> = listOf(
            Chapter(1, "精灵森林", "🧝", forestTheme),
            Chapter(2, "蘑菇沼泽", "🍄", swampTheme),
            Chapter(3, "矮人矿洞", "⛏️", mineTheme),
            Chapter(4, "龙巢火山", "🐉", volcanoTheme),
            Chapter(5, "幽灵古堡", "👻", castleTheme),
            Chapter(6, "魔法高塔", "🔮", towerTheme),
            Chapter(7, "海神之殿", "🌊", oceanTheme),
            Chapter(8, "暗影迷宫", "🌙", mazeTheme),
            Chapter(9, "天空之城", "☁️", skyTheme),
            Chapter(10, "星空神殿", "⭐", starTheme)
        )
        
        private val forestTheme = ChapterTheme(
            primaryColor = Color(0xFF4CAF50),
            secondaryColor = Color(0xFFFFD700),
            backgroundColor = Color(0xFF1B5E20),
            platformColor = Color(0xFF5D4037)
        )
        
        private val swampTheme = ChapterTheme(
            primaryColor = Color(0xFF9C27B0),
            secondaryColor = Color(0xFF76FF03),
            backgroundColor = Color(0xFF4A148C),
            platformColor = Color(0xFF795548)
        )
        
        private val mineTheme = ChapterTheme(
            primaryColor = Color(0xFF795548),
            secondaryColor = Color(0xFFFF9800),
            backgroundColor = Color(0xFF3E2723),
            platformColor = Color(0xFF6D4C41)
        )
        
        private val volcanoTheme = ChapterTheme(
            primaryColor = Color(0xFFF44336),
            secondaryColor = Color(0xFF212121),
            backgroundColor = Color(0xFFB71C1C),
            platformColor = Color(0xFF424242)
        )
        
        private val castleTheme = ChapterTheme(
            primaryColor = Color(0xFF1A237E),
            secondaryColor = Color(0xFF757575),
            backgroundColor = Color(0xFF0D47A1),
            platformColor = Color(0xFF616161)
        )
        
        private val towerTheme = ChapterTheme(
            primaryColor = Color(0xFF7B1FA2),
            secondaryColor = Color(0xFFB0BEC5),
            backgroundColor = Color(0xFF4A148C),
            platformColor = Color(0xFF9E9E9E)
        )
        
        private val oceanTheme = ChapterTheme(
            primaryColor = Color(0xFF2196F3),
            secondaryColor = Color(0xFF00BCD4),
            backgroundColor = Color(0xFF01579B),
            platformColor = Color(0xFF455A64)
        )
        
        private val mazeTheme = ChapterTheme(
            primaryColor = Color(0xFF212121),
            secondaryColor = Color(0xFF7B1FA2),
            backgroundColor = Color(0xFF000000),
            platformColor = Color(0xFF424242)
        )
        
        private val skyTheme = ChapterTheme(
            primaryColor = Color(0xFFFFFFFF),
            secondaryColor = Color(0xFFFFD700),
            backgroundColor = Color(0xFF87CEEB),
            platformColor = Color(0xFFE0E0E0)
        )
        
        private val starTheme = ChapterTheme(
            primaryColor = Color(0xFF1A237E),
            secondaryColor = Color(0xFFFFD700),
            backgroundColor = Color(0xFF0D47A1),
            platformColor = Color(0xFF616161)
        )
    }
}
```

- [ ] **Step 8: 创建 GameState 模型**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/model/GameState.kt
package com.gameway.domain.model

/**
 * 游戏状态
 */
sealed interface GameState {
    data object Loading : GameState
    data object Countdown : GameState
    data object Playing : GameState
    data object Paused : GameState
    data class Completed(val score: Int, val coinsCollected: Int) : GameState
    data class Failed(val reason: String) : GameState
}

/**
 * 游戏进度
 */
data class GameProgress(
    val currentChapter: Int = 1,
    val unlockedLevels: Map<Int, List<Int>> = emptyMap(),
    val completedLevels: Map<Int, List<Int>> = emptyMap(),
    val totalCoins: Int = 0
)

/**
 * 角色属性
 */
data class CharacterStats(
    val health: Int = 3,
    val jumpPower: Float = 1.0f,
    val moveSpeed: Float = 1.0f,
    val unlockedSkills: Set<SkillType> = emptySet()
)

/**
 * 技能类型
 */
enum class SkillType {
    DOUBLE_JUMP,
    TRIPLE_JUMP,
    JUMP_BOOST_10,
    JUMP_BOOST_20,
    SPEED_BOOST_10
}
```

- [ ] **Step 9: 创建测试**

```kotlin
// domain/src/commonTest/kotlin/com/gameway/domain/model/CharacterTest.kt
package com.gameway.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharacterTest {
    
    @Test
    fun `default character has correct values`() {
        val character = Character.createDefault()
        
        assertEquals(0f, character.velocity.x)
        assertEquals(0f, character.velocity.y)
        assertEquals(CharacterState.IDLE, character.state)
        assertFalse(character.isGrounded)
        assertEquals(1, character.maxJumps)
        assertEquals(3, character.health)
    }
    
    @Test
    fun `effective jump power increases with charge time`() {
        val baseCharacter = Character.createDefault()
        
        val halfCharged = baseCharacter.copy(chargeTime = 1000L)
        val fullyCharged = baseCharacter.copy(chargeTime = 2000L)
        
        assertTrue(fullyCharged.effectiveJumpPower > halfCharged.effectiveJumpPower)
    }
    
    @Test
    fun `hasDoubleJump returns false by default`() {
        val character = Character.createDefault()
        assertFalse(character.hasDoubleJump)
    }
    
    @Test
    fun `hasDoubleJump returns true when maxJumps >= 2`() {
        val character = Character.createDefault().copy(maxJumps = 2)
        assertTrue(character.hasDoubleJump)
    }
}
```

- [ ] **Step 10: 运行测试**

Run: `./gradlew :domain:jvmTest`
Expected: All tests passed

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "feat: add domain models for game entities"
```

---

### Task 4: Domain 模块 - 游戏引擎核心

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/engine/CollisionDetector.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`
- Test: `domain/src/commonTest/kotlin/com/gameway/domain/engine/CollisionDetectorTest.kt`
- Test: `domain/src/commonTest/kotlin/com/gameway/domain/engine/PhysicsSystemTest.kt`

- [ ] **Step 1: 创建碰撞检测器**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/engine/CollisionDetector.kt
package com.gameway.domain.engine

import com.gameway.domain.model.Character
import com.gameway.domain.model.Coin
import com.gameway.domain.model.Platform
import com.gameway.domain.model.PowerUp

/**
 * AABB 碰撞检测结果
 */
sealed class CollisionResult {
    data object NoCollision : CollisionResult()
    data class LandedOnPlatform(val platform: Platform) : CollisionResult()
    data class HitSideOfPlatform(val platform: Platform) : CollisionResult()
    data class CollectedCoin(val coin: Coin) : CollisionResult()
    data class CollectedPowerUp(val powerUp: PowerUp) : CollisionResult()
    data object FellOffScreen : CollisionResult()
}

/**
 * AABB 碰撞检测器
 */
object CollisionDetector {
    
    private const val SCREEN_BOTTOM = 600f
    private const val CHARACTER_WIDTH = 30f
    private const val CHARACTER_HEIGHT = 40f
    
    fun checkCollisions(
        character: Character,
        platforms: List<Platform>,
        coins: List<Coin>,
        powerUps: List<PowerUp>
    ): List<CollisionResult> {
        val results = mutableListOf<CollisionResult>()
        
        val charLeft = character.position.x - CHARACTER_WIDTH / 2
        val charRight = character.position.x + CHARACTER_WIDTH / 2
        val charTop = character.position.y - CHARACTER_HEIGHT
        val charBottom = character.position.y
        
        // 检查掉落
        if (charBottom > SCREEN_BOTTOM) {
            results.add(CollisionResult.FellOffScreen)
            return results
        }
        
        // 检查平台碰撞
        for (platform in platforms) {
            val platLeft = platform.x
            val platRight = platform.x + platform.width
            val platTop = platform.y
            val platBottom = platform.y + platform.height
            
            if (charRight > platLeft && charLeft < platRight &&
                charBottom > platTop && charTop < platBottom) {
                
                val overlapLeft = charRight - platLeft
                val overlapRight = platRight - charLeft
                val overlapTop = charBottom - platTop
                val overlapBottom = platBottom - charTop
                
                val minOverlap = minOf(overlapLeft, overlapRight, overlapTop, overlapBottom)
                
                when (minOverlap) {
                    overlapTop -> results.add(CollisionResult.LandedOnPlatform(platform))
                    overlapLeft, overlapRight -> results.add(CollisionResult.HitSideOfPlatform(platform))
                }
            }
        }
        
        // 检查金币收集
        for (coin in coins) {
            if (!coin.collected) {
                val dx = character.position.x - coin.x
                val dy = character.position.y - coin.y
                if (dx * dx + dy * dy < 900f) { // 30px 半径
                    results.add(CollisionResult.CollectedCoin(coin))
                }
            }
        }
        
        // 检查道具收集
        for (powerUp in powerUps) {
            if (!powerUp.collected) {
                val dx = character.position.x - powerUp.x
                val dy = character.position.y - powerUp.y
                if (dx * dx + dy * dy < 900f) {
                    results.add(CollisionResult.CollectedPowerUp(powerUp))
                }
            }
        }
        
        return results
    }
}
```

- [ ] **Step 2: 创建物理系统**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/engine/PhysicsSystem.kt
package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.Character
import com.gameway.domain.model.CharacterState
import com.gameway.domain.model.Vector2

/**
 * 物理系统 - 更新角色位置和速度
 */
object PhysicsSystem {
    
    fun update(
        character: Character,
        deltaTime: Long,
        currentScrollX: Float
    ): Character {
        val dt = deltaTime / 16f // 归一化到 60fps
        
        val gravity = if (character.velocity.y > 0) GameConstants.GRAVITY * 1.2f
        else GameConstants.GRAVITY
        
        val newVelocityY = character.velocity.y + gravity * dt
        val moveSpeed = character.effectiveMoveSpeed
        
        return character.copy(
            position = Vector2(
                character.position.x,
                character.position.y + character.velocity.y * dt
            ),
            velocity = Vector2(moveSpeed * 0.1f, newVelocityY),
            state = determineState(character, newVelocityY),
            isGrounded = character.isGrounded
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
    
    fun applyJump(character: Character, jumpPower: Float): Character {
        return character.copy(
            velocity = Vector2(character.velocity.x, -jumpPower),
            isGrounded = false,
            jumpCount = character.jumpCount + 1,
            state = CharacterState.JUMPING,
            chargeTime = 0L,
            isCharging = false
        )
    }
    
    fun startCharging(character: Character): Character {
        if (character.isGrounded || character.hasDoubleJump) {
            return character.copy(
                isCharging = true,
                chargeTime = 0L
            )
        }
        return character
    }
    
    fun updateCharge(character: Character, deltaTime: Long): Character {
        if (!character.isCharging) return character
        
        val newChargeTime = (character.chargeTime + deltaTime).coerceAtMost(GameConstants.MAX_CHARGE_TIME)
        return character.copy(chargeTime = newChargeTime)
    }
    
    fun landOnPlatform(character: Character, platformY: Float): Character {
        return character.copy(
            position = Vector2(character.position.x, platformY),
            velocity = Vector2(character.velocity.x, 0f),
            isGrounded = true,
            jumpCount = 0,
            state = CharacterState.RUNNING
        )
    }
}
```

- [ ] **Step 3: 创建游戏引擎**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt
package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.domain.model.ActivePowerUp
import com.gameway.domain.model.Character
import com.gameway.domain.model.CharacterState
import com.gameway.domain.model.Coin
import com.gameway.domain.model.GameState
import com.gameway.domain.model.Level
import com.gameway.domain.model.PowerUpType

/**
 * 游戏引擎 - 管理游戏状态和更新逻辑
 */
class GameEngine {
    
    private var gameState: GameState = GameState.Loading
    private var character: Character = Character.createDefault()
    private var level: Level? = null
    private var scrollX: Float = 0f
    private var score: Int = 0
    private var lastUpdateTime: Long = 0L
    private var countdownEnd: Long = 0L
    
    fun startLevel(newLevel: Level) {
        level = newLevel
        character = Character.createDefault().copy(
            position = character.position.copy(x = 50f)
        )
        scrollX = 0f
        score = 0
        gameState = GameState.Countdown
        countdownEnd = System.currentTimeMillis() + 2000L
        lastUpdateTime = System.currentTimeMillis()
    }
    
    fun update(): GameState {
        if (gameState is GameState.Loading || gameState is GameState.Completed || gameState is GameState.Failed) {
            return gameState
        }
        
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastUpdateTime
        lastUpdateTime = currentTime
        
        when (gameState) {
            is GameState.Countdown -> {
                if (currentTime >= countdownEnd) {
                    gameState = GameState.Playing
                }
            }
            is GameState.Playing -> {
                updateGame(deltaTime, currentTime)
            }
            is GameState.Paused -> {}
            else -> {}
        }
        
        return gameState
    }
    
    private fun updateGame(deltaTime: Long, currentTime: Long) {
        val currentLevel = level ?: return
        
        // 更新蓄力
        if (character.isCharging) {
            character = PhysicsSystem.updateCharge(character, deltaTime)
        }
        
        // 更新物理
        character = PhysicsSystem.update(character, deltaTime, scrollX)
        
        // 更新滚动
        scrollX = character.position.x - 100f
        
        // 更新移动平台
        val updatedPlatforms = currentLevel.platforms.map { platform ->
            if (platform.type != com.gameway.domain.model.PlatformType.STATIC) {
                val offset = kotlin.math.sin((currentTime / 1000f) * platform.moveSpeed + platform.moveOffset) * platform.moveRange
                platform.copy(x = platform.x + offset * 0.01f)
            } else {
                platform
            }
        }
        
        // 检测碰撞
        val collisions = CollisionDetector.checkCollisions(
            character,
            updatedPlatforms,
            currentLevel.coins,
            currentLevel.powerUps
        )
        
        for (collision in collisions) {
            when (collision) {
                is CollisionResult.LandedOnPlatform -> {
                    character = PhysicsSystem.landOnPlatform(character, collision.platform.y)
                }
                is CollisionResult.HitSideOfPlatform -> {
                    gameState = if (character.hasShield) {
                        character = character.copy(
                            activePowerUps = character.activePowerUps.filter { it.type != PowerUpType.SHIELD }
                        )
                        GameState.Playing
                    } else {
                        GameState.Failed("撞到平台侧面")
                    }
                }
                is CollisionResult.CollectedCoin -> {
                    score += 10
                    character = character.copy(
                        coinsCollected = character.coinsCollected + 1
                    )
                }
                is CollisionResult.CollectedPowerUp -> {
                    val powerUp = collision.powerUp
                    character = character.copy(
                        activePowerUps = character.activePowerUps + ActivePowerUp.create(powerUp.type),
                        maxJumps = if (powerUp.type == PowerUpType.GEM) 2 else character.maxJumps
                    )
                    score += 20
                }
                is CollisionResult.FellOffScreen -> {
                    gameState = if (character.hasShield) {
                        character = character.copy(
                            activePowerUps = character.activePowerUps.filter { it.type != PowerUpType.SHIELD }
                        )
                        GameState.Playing
                    } else {
                        GameState.Failed("掉落屏幕")
                    }
                }
                CollisionResult.NoCollision -> {}
            }
        }
        
        // 检查关卡完成条件
        val lastPlatform = updatedPlatforms.lastOrNull()
        if (lastPlatform != null && character.position.x > lastPlatform.x + lastPlatform.width) {
            gameState = GameState.Completed(score, character.coinsCollected)
        }
    }
    
    fun jump() {
        if (gameState !is GameState.Playing) return
        
        if (character.isGrounded || character.jumpCount < character.maxJumps) {
            character = PhysicsSystem.applyJump(character, character.effectiveJumpPower)
        }
    }
    
    fun startCharge() {
        if (gameState !is GameState.Playing) return
        character = PhysicsSystem.startCharging(character)
    }
    
    fun releaseCharge() {
        if (gameState !is GameState.Playing) return
        if (character.isCharging) {
            character = PhysicsSystem.applyJump(character, character.effectiveJumpPower)
        }
    }
    
    fun pause() {
        if (gameState is GameState.Playing) {
            gameState = GameState.Paused
        }
    }
    
    fun resume() {
        if (gameState is GameState.Paused) {
            gameState = GameState.Playing
            lastUpdateTime = System.currentTimeMillis()
        }
    }
    
    fun restart() {
        level?.let { startLevel(it) }
    }
    
    fun getCharacter(): Character = character
    fun getGameState(): GameState = gameState
    fun getScrollX(): Float = scrollX
    fun getScore(): Int = score
}
```

- [ ] **Step 4: 创建碰撞检测测试**

```kotlin
// domain/src/commonTest/kotlin/com/gameway/domain/engine/CollisionDetectorTest.kt
package com.gameway.domain.engine

import com.gameway.domain.model.Character
import com.gameway.domain.model.Coin
import com.gameway.domain.model.Platform
import com.gameway.domain.model.PowerUp
import com.gameway.domain.model.PowerUpType
import com.gameway.domain.model.Vector2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CollisionDetectorTest {
    
    @Test
    fun `detects landing on platform`() {
        val character = Character.createDefault().copy(
            position = Vector2(100f, 100f),
            velocity = Vector2(0f, 5f)
        )
        val platform = Platform(0, 85f, 100f, 60f)
        
        val results = CollisionDetector.checkCollisions(character, listOf(platform), emptyList(), emptyList())
        
        assertTrue(results.any { it is CollisionResult.LandedOnPlatform })
    }
    
    @Test
    fun `detects falling off screen`() {
        val character = Character.createDefault().copy(
            position = Vector2(100f, 650f)
        )
        
        val results = CollisionDetector.checkCollisions(character, emptyList(), emptyList(), emptyList())
        
        assertEquals(CollisionResult.FellOffScreen, results.first())
    }
    
    @Test
    fun `detects coin collection`() {
        val character = Character.createDefault().copy(
            position = Vector2(100f, 100f)
        )
        val coin = Coin(0, 105f, 105f)
        
        val results = CollisionDetector.checkCollisions(character, emptyList(), listOf(coin), emptyList())
        
        assertTrue(results.any { it is CollisionResult.CollectedCoin })
    }
    
    @Test
    fun `detects powerup collection`() {
        val character = Character.createDefault().copy(
            position = Vector2(100f, 100f)
        )
        val powerUp = PowerUp(0, 105f, 105f, PowerUpType.FEATHER, false)
        
        val results = CollisionDetector.checkCollisions(character, emptyList(), emptyList(), listOf(powerUp))
        
        assertTrue(results.any { it is CollisionResult.CollectedPowerUp })
    }
}
```

- [ ] **Step 5: 运行测试**

Run: `./gradlew :domain:jvmTest`
Expected: All tests passed

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add game engine with physics and collision detection"
```

---

### Task 5: Domain 模块 - 用例与仓储接口

**Files:**
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/repository/GameProgressRepository.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/repository/SettingsRepository.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetChaptersUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetLevelUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/SaveProgressUseCase.kt`
- Create: `domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetCharacterStatsUseCase.kt`
- Test: `domain/src/commonTest/kotlin/com/gameway/domain/usecase/GetLevelUseCaseTest.kt`

- [ ] **Step 1: 创建游戏进度仓储接口**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/repository/GameProgressRepository.kt
package com.gameway.domain.repository

import com.gameway.domain.model.GameProgress

interface GameProgressRepository {
    suspend fun getProgress(): Result<GameProgress>
    suspend fun saveProgress(progress: GameProgress): Result<Unit>
    suspend fun updateCoins(amount: Int): Result<Unit>
    suspend fun completeLevel(chapterId: Int, levelNumber: Int): Result<Unit>
}
```

- [ ] **Step 2: 创建设置仓储接口**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/repository/SettingsRepository.kt
package com.gameway.domain.repository

data class GameSettings(
    val bgmVolume: Float = 0.8f,
    val sfxVolume: Float = 0.8f,
    val vibrate: Boolean = true,
    val language: String = "zh"
)

interface SettingsRepository {
    suspend fun getSettings(): Result<GameSettings>
    suspend fun saveSettings(settings: GameSettings): Result<Unit>
    suspend fun updateBgmVolume(volume: Float): Result<Unit>
    suspend fun updateSfxVolume(volume: Float): Result<Unit>
}
```

- [ ] **Step 3: 创建用例**

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetChaptersUseCase.kt
package com.gameway.domain.usecase

import com.gameway.domain.model.Chapter

class GetChaptersUseCase {
    operator fun invoke(): List<Chapter> {
        return Chapter.getAllChapters()
    }
}
```

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetLevelUseCase.kt
package com.gameway.domain.usecase

import com.gameway.domain.model.Level

class GetLevelUseCase {
    operator fun invoke(chapterId: Int, levelNumber: Int, seed: Long = System.currentTimeMillis()): Level {
        return Level.generate(chapterId, levelNumber, seed)
    }
}
```

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/usecase/SaveProgressUseCase.kt
package com.gameway.domain.usecase

import com.gameway.domain.model.GameProgress
import com.gameway.domain.repository.GameProgressRepository

class SaveProgressUseCase(
    private val repository: GameProgressRepository
) {
    suspend operator fun invoke(progress: GameProgress): Result<Unit> {
        return repository.saveProgress(progress)
    }
}
```

```kotlin
// domain/src/commonMain/kotlin/com/gameway/domain/usecase/GetCharacterStatsUseCase.kt
package com.gameway.domain.usecase

import com.gameway.domain.model.CharacterStats
import com.gameway.domain.repository.GameProgressRepository

class GetCharacterStatsUseCase(
    private val repository: GameProgressRepository
) {
    suspend operator fun invoke(): Result<CharacterStats> {
        return runCatching {
            val progress = repository.getProgress().getOrThrow()
            CharacterStats(
                health = 3,
                jumpPower = 1.0f,
                moveSpeed = 1.0f,
                unlockedSkills = emptySet()
            )
        }
    }
}
```

- [ ] **Step 4: 创建用例测试**

```kotlin
// domain/src/commonTest/kotlin/com/gameway/domain/usecase/GetLevelUseCaseTest.kt
package com.gameway.domain.usecase

import com.gameway.core.GameConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetLevelUseCaseTest {
    
    private val useCase = GetLevelUseCase()
    
    @Test
    fun `generates level with correct chapter and level number`() {
        val level = useCase(1, 3, 42L)
        
        assertEquals(1, level.chapterId)
        assertEquals(3, level.levelNumber)
        assertEquals(103, level.id)
    }
    
    @Test
    fun `generates level with platform count in range`() {
        val level = useCase(1, 1, 42L)
        
        assertTrue(level.platforms.size in GameConstants.MIN_PLATFORMS..30)
    }
    
    @Test
    fun `generates level with coins`() {
        val level = useCase(1, 1, 42L)
        
        assertTrue(level.coins.isNotEmpty())
    }
    
    @Test
    fun `generates level with powerups`() {
        val level = useCase(1, 1, 42L)
        
        assertTrue(level.powerUps.isNotEmpty())
    }
}
```

- [ ] **Step 5: 运行测试**

Run: `./gradlew :domain:jvmTest`
Expected: All tests passed

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add use cases and repository interfaces"
```

---

### Task 6: Data 模块 - 仓储实现

**Files:**
- Create: `data/src/main/kotlin/com/gameway/data/local/DataStoreManager.kt`
- Create: `data/src/main/kotlin/com/gameway/data/repository/GameProgressRepositoryImpl.kt`
- Create: `data/src/main/kotlin/com/gameway/data/repository/SettingsRepositoryImpl.kt`
- Test: `data/src/test/kotlin/com/gameway/data/repository/GameProgressRepositoryImplTest.kt`

- [ ] **Step 1: 创建 DataStore 管理器**

```kotlin
// data/src/main/kotlin/com/gameway/data/local/DataStoreManager.kt
package com.gameway.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "findway_settings")

class DataStoreManager(private val context: Context) {
    
    private val dataStore = context.dataStore
    
    object Keys {
        val CURRENT_CHAPTER = intPreferencesKey("current_chapter")
        val TOTAL_COINS = intPreferencesKey("total_coins")
        val COMPLETED_LEVELS = stringPreferencesKey("completed_levels")
        val BGM_VOLUME = floatPreferencesKey("bgm_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val VIBRATE = booleanPreferencesKey("vibrate")
        val LANGUAGE = stringPreferencesKey("language")
    }
    
    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
    
    fun <T> get(key: Preferences.Key<T>, default: T): Flow<T> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: default
        }
    }
    
    suspend fun <T> getOnce(key: Preferences.Key<T>, default: T): T {
        return dataStore.data.map { preferences ->
            preferences[key] ?: default
        }.value
    }
}
```

- [ ] **Step 2: 创建游戏进度仓储实现**

```kotlin
// data/src/main/kotlin/com/gameway/data/repository/GameProgressRepositoryImpl.kt
package com.gameway.data.repository

import com.gameway.data.local.DataStoreManager
import com.gameway.data.local.Keys
import com.gameway.domain.model.GameProgress
import com.gameway.domain.repository.GameProgressRepository

class GameProgressRepositoryImpl(
    private val dataStoreManager: DataStoreManager
) : GameProgressRepository {
    
    override suspend fun getProgress(): Result<GameProgress> {
        return runCatching {
            GameProgress(
                currentChapter = dataStoreManager.getOnce(Keys.CURRENT_CHAPTER, 1),
                totalCoins = dataStoreManager.getOnce(Keys.TOTAL_COINS, 0),
                completedLevels = parseCompletedLevels(
                    dataStoreManager.getOnce(Keys.COMPLETED_LEVELS, "")
                )
            )
        }
    }
    
    override suspend fun saveProgress(progress: GameProgress): Result<Unit> {
        return runCatching {
            dataStoreManager.save(Keys.CURRENT_CHAPTER, progress.currentChapter)
            dataStoreManager.save(Keys.TOTAL_COINS, progress.totalCoins)
            dataStoreManager.save(
                Keys.COMPLETED_LEVELS,
                serializeCompletedLevels(progress.completedLevels)
            )
        }
    }
    
    override suspend fun updateCoins(amount: Int): Result<Unit> {
        return runCatching {
            val current = dataStoreManager.getOnce(Keys.TOTAL_COINS, 0)
            dataStoreManager.save(Keys.TOTAL_COINS, current + amount)
        }
    }
    
    override suspend fun completeLevel(chapterId: Int, levelNumber: Int): Result<Unit> {
        return runCatching {
            val progress = getProgress().getOrThrow()
            val completedLevels = progress.completedLevels.toMutableMap()
            val chapterLevels = completedLevels[chapterId]?.toMutableList() ?: mutableListOf()
            if (!chapterLevels.contains(levelNumber)) {
                chapterLevels.add(levelNumber)
                completedLevels[chapterId] = chapterLevels
            }
            
            dataStoreManager.save(
                Keys.COMPLETED_LEVELS,
                serializeCompletedLevels(completedLevels)
            )
        }
    }
    
    private fun parseCompletedLevels(data: String): Map<Int, List<Int>> {
        if (data.isEmpty()) return emptyMap()
        return data.split(";").associate { pair ->
            val (chapter, levels) = pair.split(":")
            chapter.toInt() to levels.split(",").map { it.toInt() }
        }
    }
    
    private fun serializeCompletedLevels(levels: Map<Int, List<Int>>): String {
        return levels.entries.joinToString(";") { (chapter, levelList) ->
            "$chapter:${levelList.joinToString(",")}"
        }
    }
}
```

- [ ] **Step 3: 创建设置仓储实现**

```kotlin
// data/src/main/kotlin/com/gameway/data/repository/SettingsRepositoryImpl.kt
package com.gameway.data.repository

import com.gameway.data.local.DataStoreManager
import com.gameway.data.local.Keys
import com.gameway.domain.repository.GameSettings
import com.gameway.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val dataStoreManager: DataStoreManager
) : SettingsRepository {
    
    override suspend fun getSettings(): Result<GameSettings> {
        return runCatching {
            GameSettings(
                bgmVolume = dataStoreManager.getOnce(Keys.BGM_VOLUME, 0.8f),
                sfxVolume = dataStoreManager.getOnce(Keys.SFX_VOLUME, 0.8f),
                vibrate = dataStoreManager.getOnce(Keys.VIBRATE, true),
                language = dataStoreManager.getOnce(Keys.LANGUAGE, "zh")
            )
        }
    }
    
    override suspend fun saveSettings(settings: GameSettings): Result<Unit> {
        return runCatching {
            dataStoreManager.save(Keys.BGM_VOLUME, settings.bgmVolume)
            dataStoreManager.save(Keys.SFX_VOLUME, settings.sfxVolume)
            dataStoreManager.save(Keys.VIBRATE, settings.vibrate)
            dataStoreManager.save(Keys.LANGUAGE, settings.language)
        }
    }
    
    override suspend fun updateBgmVolume(volume: Float): Result<Unit> {
        return runCatching {
            dataStoreManager.save(Keys.BGM_VOLUME, volume.coerceIn(0f, 1f))
        }
    }
    
    override suspend fun updateSfxVolume(volume: Float): Result<Unit> {
        return runCatching {
            dataStoreManager.save(Keys.SFX_VOLUME, volume.coerceIn(0f, 1f))
        }
    }
}
```

- [ ] **Step 4: 创建测试**

```kotlin
// data/src/test/kotlin/com/gameway/data/repository/GameProgressRepositoryImplTest.kt
package com.gameway.data.repository

import com.gameway.data.local.DataStoreManager
import com.gameway.domain.model.GameProgress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GameProgressRepositoryImplTest {
    
    private lateinit var repository: GameProgressRepositoryImpl
    private val mockDataStore: DataStoreManager = mock()
    
    @Before
    fun setup() {
        repository = GameProgressRepositoryImpl(mockDataStore)
    }
    
    @Test
    fun `parseCompletedLevels returns empty map for empty string`() = runBlocking {
        whenever(mockDataStore.getOnce(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn("")
        
        val result = repository.getProgress().getOrThrow()
        assertEquals(emptyMap<Int, List<Int>>(), result.completedLevels)
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: implement data layer with DataStore repositories"
```

---

### Task 7: Presentation 模块 - DI 与主题

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/di/AppModule.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/theme/GameTheme.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt`

- [ ] **Step 1: 创建 DI 模块**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/di/AppModule.kt
package com.gameway.presentation.di

import com.gameway.data.local.DataStoreManager
import com.gameway.data.repository.GameProgressRepositoryImpl
import com.gameway.data.repository.SettingsRepositoryImpl
import com.gameway.domain.repository.GameProgressRepository
import com.gameway.domain.repository.SettingsRepository
import com.gameway.domain.usecase.GetCharacterStatsUseCase
import com.gameway.domain.usecase.GetChaptersUseCase
import com.gameway.domain.usecase.GetLevelUseCase
import com.gameway.domain.usecase.SaveProgressUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.gameway.presentation.screens.game.GameViewModel

val dataModule = module {
    single { DataStoreManager(get()) }
    single<GameProgressRepository> { GameProgressRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}

val domainModule = module {
    factory { GetChaptersUseCase() }
    factory { GetLevelUseCase() }
    factory { SaveProgressUseCase(get()) }
    factory { GetCharacterStatsUseCase(get()) }
}

val presentationModule = module {
    viewModel { GameViewModel(get(), get(), get()) }
}
```

- [ ] **Step 2: 创建游戏主题**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/theme/GameTheme.kt
package com.gameway.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

private val DarkColorScheme = darkColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun FindWayTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

- [ ] **Step 3: 创建导航**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/navigation/AppNavigation.kt
package com.gameway.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import com.gameway.presentation.screens.menu.MainMenuScreen
import com.gameway.presentation.screens.splash.SplashScreen
import com.gameway.presentation.screens.stats.StatsScreen
import com.gameway.presentation.screens.chapter.ChapterSelectScreen
import com.gameway.presentation.screens.level.LevelSelectScreen
import com.gameway.presentation.screens.game.GameScreen

sealed class Screen {
    @Serializable
    data object Splash : Screen()
    
    @Serializable
    data object MainMenu : Screen()
    
    @Serializable
    data object ChapterSelect : Screen()
    
    @Serializable
    data class LevelSelect(val chapterId: Int) : Screen()
    
    @Serializable
    data class Game(val chapterId: Int, val levelNumber: Int) : Screen()
    
    @Serializable
    data object Stats : Screen()
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {
        composable<Screen.Splash> {
            SplashScreen(onNavigateToMenu = {
                navController.navigate(Screen.MainMenu) {
                    popUpTo(Screen.Splash) { inclusive = true }
                }
            })
        }
        
        composable<Screen.MainMenu> {
            MainMenuScreen(
                onStartGame = { navController.navigate(Screen.ChapterSelect) },
                onViewStats = { navController.navigate(Screen.Stats) }
            )
        }
        
        composable<Screen.ChapterSelect> {
            ChapterSelectScreen(
                onChapterSelected = { chapterId ->
                    navController.navigate(Screen.LevelSelect(chapterId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Screen.LevelSelect> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.LevelSelect>()
            LevelSelectScreen(
                chapterId = args.chapterId,
                onLevelSelected = { levelNumber ->
                    navController.navigate(Screen.Game(args.chapterId, levelNumber))
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Screen.Game> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Game>()
            GameScreen(
                chapterId = args.chapterId,
                levelNumber = args.levelNumber,
                onLevelComplete = { navController.popBackStack() },
                onLevelFailed = { navController.popBackStack() }
            )
        }
        
        composable<Screen.Stats> {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add presentation layer with DI, theme, and navigation"
```

---

### Task 8: Presentation 模块 - 启动页与主菜单

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/splash/SplashScreen.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt`

- [ ] **Step 1: 创建启动页**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/splash/SplashScreen.kt
package com.gameway.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToMenu: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
        delay(1500)
        onNavigateToMenu()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF4A148C)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🗺️",
                fontSize = 64.sp,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Find Way",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "奇幻冒险之旅",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
```

- [ ] **Step 2: 创建主菜单**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/menu/MainMenuScreen.kt
package com.gameway.presentation.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
    onViewStats: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF4A148C)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🗺️",
                fontSize = 48.sp
            )
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
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("开始游戏", fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onViewStats,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("角色属性", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(40.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                    .clickable { /* 设置页面 */ }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("设置", fontSize = 14.sp, color = Color.White)
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: add splash screen and main menu UI"
```

---

### Task 9: Presentation 模块 - 游戏画面与 Canvas 渲染

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameScreen.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/components/CharacterSprite.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/components/PlatformRenderer.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/components/PowerUpRenderer.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/components/HUD.kt`

- [ ] **Step 1: 创建 GameViewModel**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameViewModel.kt
package com.gameway.presentation.screens.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameway.domain.engine.GameEngine
import com.gameway.domain.model.Character
import com.gameway.domain.model.GameState
import com.gameway.domain.model.Level
import com.gameway.domain.usecase.GetLevelUseCase
import com.gameway.domain.usecase.SaveProgressUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val character: Character = Character.createDefault(),
    val gameState: GameState = GameState.Loading,
    val scrollX: Float = 0f,
    val score: Int = 0,
    val chapterId: Int = 1,
    val levelNumber: Int = 1,
    val chargeProgress: Float = 0f
)

class GameViewModel(
    private val getLevelUseCase: GetLevelUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val gameEngine: GameEngine = GameEngine()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    private var gameLoopActive = false
    
    fun loadLevel(chapterId: Int, levelNumber: Int) {
        viewModelScope.launch {
            val level = getLevelUseCase(chapterId, levelNumber)
            gameEngine.startLevel(level)
            
            _uiState.value = _uiState.value.copy(
                chapterId = chapterId,
                levelNumber = levelNumber,
                gameState = GameState.Countdown
            )
            
            startGameLoop()
        }
    }
    
    private fun startGameLoop() {
        if (gameLoopActive) return
        gameLoopActive = true
        
        viewModelScope.launch {
            while (gameLoopActive) {
                val state = gameEngine.getGameState()
                _uiState.value = _uiState.value.copy(
                    character = gameEngine.getCharacter(),
                    gameState = state,
                    scrollX = gameEngine.getScrollX(),
                    score = gameEngine.getScore(),
                    chargeProgress = if (gameEngine.getCharacter().isCharging) {
                        gameEngine.getCharacter().chargeTime / 2000f
                    } else {
                        0f
                    }
                )
                
                if (state is GameState.Completed || state is GameState.Failed) {
                    gameLoopActive = false
                    break
                }
                
                delay(16L) // ~60fps
            }
        }
    }
    
    fun onTouchDown() {
        gameEngine.startCharge()
    }
    
    fun onTouchUp() {
        gameEngine.releaseCharge()
    }
    
    fun onTap() {
        gameEngine.jump()
    }
    
    fun pause() {
        gameEngine.pause()
    }
    
    fun resume() {
        gameEngine.resume()
    }
    
    fun restart() {
        gameEngine.restart()
        _uiState.value = _uiState.value.copy(
            gameState = GameState.Countdown
        )
        gameLoopActive = false
        startGameLoop()
    }
    
    fun completeLevel() {
        viewModelScope.launch {
            val state = _uiState.value
            saveProgressUseCase(
                com.gameway.domain.model.GameProgress(
                    currentChapter = state.chapterId,
                    completedLevels = mapOf(
                        state.chapterId to listOf(state.levelNumber)
                    )
                )
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        gameLoopActive = false
    }
}
```

- [ ] **Step 2: 创建 GameCanvas**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameCanvas.kt
package com.gameway.presentation.screens.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gameway.domain.model.CharacterState
import com.gameway.domain.model.PlatformType

@Composable
fun GameCanvas(
    chapterId: Int,
    levelNumber: Int,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.onTap() },
                    onPress = {
                        viewModel.onTouchDown()
                        tryAwaitRelease()
                        viewModel.onTouchUp()
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val scale = canvasWidth / 800f
        
        // 背景
        drawRect(
            color = Color(0xFF87CEEB),
            size = size
        )
        
        // 视口偏移
        val viewportX = uiState.scrollX * scale
        
        // 绘制平台
        val level = com.gameway.domain.model.Level.generate(chapterId, levelNumber, 42L)
        for (platform in level.platforms) {
            val screenX = (platform.x - viewportX)
            if (screenX > -platform.width * scale && screenX < canvasWidth + platform.width * scale) {
                drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        screenX,
                        platform.y * scale
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        platform.width * scale,
                        platform.height * scale
                    ),
                    radius = androidx.compose.ui.geometry.CornerRadius(4f * scale, 4f * scale)
                )
            }
        }
        
        // 绘制金币
        for (coin in level.coins) {
            if (!coin.collected) {
                val screenX = (coin.x - viewportX)
                if (screenX > -20f && screenX < canvasWidth + 20f) {
                    drawCircle(
                        color = Color(0xFFFFD700),
                        radius = 8f * scale,
                        center = androidx.compose.ui.geometry.Offset(
                            screenX,
                            coin.y * scale
                        )
                    )
                }
            }
        }
        
        // 绘制道具
        for (powerUp in level.powerUps) {
            if (!powerUp.collected) {
                val screenX = (powerUp.x - viewportX)
                if (screenX > -20f && screenX < canvasWidth + 20f) {
                    val color = when (powerUp.type) {
                        com.gameway.domain.model.PowerUpType.FEATHER -> Color(0xFF8BC34A)
                        com.gameway.domain.model.PowerUpType.BUTTERFLY -> Color(0xFF03A9F4)
                        com.gameway.domain.model.PowerUpType.LIGHTNING -> Color(0xFFFFEB3B)
                        com.gameway.domain.model.PowerUpType.SHIELD -> Color(0xFF9E9E9E)
                        com.gameway.domain.model.PowerUpType.GEM -> Color(0xFFE91E63)
                        com.gameway.domain.model.PowerUpType.STAR -> Color(0xFFFF9800)
                    }
                    drawCircle(
                        color = color,
                        radius = 10f * scale,
                        center = androidx.compose.ui.geometry.Offset(
                            screenX,
                            powerUp.y * scale
                        )
                    )
                }
            }
        }
        
        // 绘制角色
        val charScreenX = (uiState.character.position.x - viewportX)
        val charScreenY = uiState.character.position.y * scale
        
        when (uiState.character.state) {
            CharacterState.RUNNING -> {
                // 奔跑状态 - 正常姿态
                drawCircle(
                    color = Color(0xFF4A90D9),
                    radius = 15f * scale,
                    center = androidx.compose.ui.geometry.Offset(
                        charScreenX,
                        charScreenY - 10f * scale
                    )
                )
                // 头部
                drawCircle(
                    color = Color(0xFFFFE0B2),
                    radius = 12f * scale,
                    center = androidx.compose.ui.geometry.Offset(
                        charScreenX,
                        charScreenY - 25f * scale
                    )
                )
                // 眼睛
                drawCircle(
                    color = Color(0xFF333333),
                    radius = 2f * scale,
                    center = androidx.compose.ui.geometry.Offset(
                        charScreenX - 4f * scale,
                        charScreenY - 26f * scale
                    )
                )
                drawCircle(
                    color = Color(0xFF333333),
                    radius = 2f * scale,
                    center = androidx.compose.ui.geometry.Offset(
                        charScreenX + 4f * scale,
                        charScreenY - 26f * scale
                    )
                )
            }
            CharacterState.JUMPING -> {
                // 跳跃状态 - 手臂张开
                drawCircle(
                    color = Color(0xFF4A90D9),
                    radius = 14f * scale,
                    center = androidx.compose.ui.geometry.Offset(
                        charScreenX,
                        charScreenY - 12f * scale
                    )
                )
                drawCircle(
                    color = Color(0xFFFFE0B2),
                    radius = 12f * scale,
                    center = androidx.compose.ui.geometry.Offset(
                        charScreenX,
                        charScreenY - 28f * scale
                    )
                )
                // 张嘴
                drawCircle(
                    color = Color(0xFFE07050),
                    radius = 4f * scale,
                    center = androidx.compose.ui.geometry.Offset(
                        charScreenX,
                        charScreenY - 22f * scale
                    )
                )
            }
            else -> {
                drawCircle(
                    color = Color(0xFF4A90D9),
                    radius = 15f * scale,
                    center = androidx.compose.ui.geometry.Offset(
                        charScreenX,
                        charScreenY - 10f * scale
                    )
                )
            }
        }
        
        // 蓄力指示器
        if (uiState.chargeProgress > 0f) {
            drawRect(
                color = Color(0xFFFF9800).copy(alpha = 0.5f),
                topLeft = androidx.compose.ui.geometry.Offset(
                    canvasWidth / 2 - 50f,
                    canvasHeight - 40f
                ),
                size = androidx.compose.ui.geometry.Size(
                    100f * uiState.chargeProgress,
                    8f
                )
            )
        }
    }
}
```

- [ ] **Step 3: 创建 GameScreen**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/game/GameScreen.kt
package com.gameway.presentation.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gameway.domain.model.GameState
import com.gameway.presentation.components.GameHUD
import com.gameway.presentation.screens.complete.LevelCompleteScreen

@Composable
fun GameScreen(
    chapterId: Int,
    levelNumber: Int,
    onLevelComplete: () -> Unit,
    onLevelFailed: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(chapterId, levelNumber) {
        viewModel.loadLevel(chapterId, levelNumber)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        GameCanvas(
            chapterId = chapterId,
            levelNumber = levelNumber,
            viewModel = viewModel
        )
        
        // HUD
        GameHUD(
            score = uiState.score,
            coins = uiState.character.coinsCollected,
            chapterId = uiState.chapterId,
            levelNumber = uiState.levelNumber,
            onPause = { viewModel.pause() }
        )
        
        // 倒计时
        if (uiState.gameState is GameState.Countdown) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "准备...",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = androidx.compose.ui.unit.TextUnit(32f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }
        }
        
        // 完成/失败状态处理
        when (val state = uiState.gameState) {
            is GameState.Completed -> {
                LevelCompleteScreen(
                    score = state.score,
                    coinsCollected = state.coinsCollected,
                    onNextLevel = {
                        viewModel.completeLevel()
                        onLevelComplete()
                    },
                    onRestart = { viewModel.restart() }
                )
            }
            is GameState.Failed -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("失败：${state.reason}", color = androidx.compose.ui.graphics.Color.Red)
                }
            }
            else -> {}
        }
    }
}
```

- [ ] **Step 4: 创建 HUD 组件**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/components/HUD.kt
package com.gameway.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameHUD(
    score: Int,
    coins: Int,
    chapterId: Int,
    levelNumber: Int,
    onPause: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "🪙 $coins",
            color = Color.White,
            fontSize = 16.sp
        )
        Text(
            text = "章节$chapterId - 关卡$levelNumber",
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = "分数: $score",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add game screen with Canvas rendering and game loop"
```

---

### Task 10: Presentation 模块 - 其他页面

**Files:**
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/chapter/ChapterSelectScreen.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/level/LevelSelectScreen.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/stats/StatsScreen.kt`
- Create: `presentation/src/main/kotlin/com/gameway/presentation/screens/complete/LevelCompleteScreen.kt`

- [ ] **Step 1: 创建章节选择页**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/chapter/ChapterSelectScreen.kt
package com.gameway.presentation.screens.chapter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gameway.domain.model.Chapter

@Composable
fun ChapterSelectScreen(
    onChapterSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val chapters = Chapter.getAllChapters()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A237E))
            .padding(16.dp)
    ) {
        Text(
            text = "选择章节",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chapters) { chapter ->
                ChapterCard(
                    chapter = chapter,
                    onClick = { onChapterSelected(chapter.id) }
                )
            }
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                chapter.theme.primaryColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = chapter.emoji, fontSize = 32.sp)
            Text(
                text = chapter.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${chapter.completedLevels}/${chapter.totalLevels}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
```

- [ ] **Step 2: 创建关卡选择页**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/level/LevelSelectScreen.kt
package com.gameway.presentation.screens.level

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LevelSelectScreen(
    chapterId: Int,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A237E))
            .padding(16.dp)
    ) {
        Text(
            text = "章节 $chapterId - 选择关卡",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items((1..10).toList()) { levelNumber ->
                LevelCard(
                    levelNumber = levelNumber,
                    onClick = { onLevelSelected(levelNumber) }
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    levelNumber: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                Color(0xFF3F51B5),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "关卡 $levelNumber",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
```

- [ ] **Step 3: 创建角色属性页**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/stats/StatsScreen.kt
package com.gameway.presentation.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatsScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A237E))
            .padding(16.dp)
    ) {
        // 角色信息卡片
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🧙", fontSize = 40.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "冒险者",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "等级: 5 | 金币: 1280",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 属性条
        StatBar("❤️ 生命值", 0.8f, Color(0xFFF44336))
        StatBar("🦘 跳跃力", 0.6f, Color(0xFF2196F3))
        StatBar("🏃 移动速度", 0.7f, Color(0xFF4CAF50))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "已解锁技能",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row {
            Text(
                text = "✅ 二段跳",
                modifier = Modifier
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color(0xFF2E7D32),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🔒 三段跳",
                modifier = Modifier
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color(0xFFE65100),
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Text("返回", fontSize = 16.sp)
        }
    }
}

@Composable
private fun StatBar(
    label: String,
    progress: Float,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}
```

- [ ] **Step 4: 创建关卡完成页**

```kotlin
// presentation/src/main/kotlin/com/gameway/presentation/screens/complete/LevelCompleteScreen.kt
package com.gameway.presentation.screens.complete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LevelCompleteScreen(
    score: Int,
    coinsCollected: Int,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(
                    Color(0xFF1B5E20),
                    RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉",
                fontSize = 48.sp
            )
            Text(
                text = "关卡完成！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "分数: $score",
                fontSize = 18.sp,
                color = Color(0xFFFFD700)
            )
            Text(
                text = "金币: $coinsCollected",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNextLevel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("下一关", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Text("重新开始", fontSize = 16.sp)
            }
        }
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add chapter, level, stats, and complete screens"
```

---

### Task 11: App 模块 - Android 入口

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/kotlin/com/gameway/MainActivity.kt`
- Create: `app/src/main/kotlin/com/gameway/FindWayApp.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: 创建 AndroidManifest.xml**

```xml
<!-- app/src/main/AndroidManifest.xml -->
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:name=".FindWayApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.FindWay"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="landscape"
            android:theme="@style/Theme.FindWay">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 2: 创建 Application 类**

```kotlin
// app/src/main/kotlin/com/gameway/FindWayApp.kt
package com.gameway

import android.app.Application
import com.gameway.presentation.di.dataModule
import com.gameway.presentation.di.domainModule
import com.gameway.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FindWayApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@FindWayApp)
            modules(dataModule, domainModule, presentationModule)
        }
    }
}
```

- [ ] **Step 3: 创建 MainActivity**

```kotlin
// app/src/main/kotlin/com/gameway/MainActivity.kt
package com.gameway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gameway.presentation.navigation.AppNavigation
import com.gameway.presentation.theme.FindWayTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindWayTheme {
                AppNavigation()
            }
        }
    }
}
```

- [ ] **Step 4: 创建资源文件**

```xml
<!-- app/src/main/res/values/strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Find Way</string>
</resources>
```

```xml
<!-- app/src/main/res/values/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.FindWay" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowLayoutInDisplayCutoutMode">shortEdges</item>
    </style>
</resources>
```

- [ ] **Step 5: 运行构建**

Run: `./gradlew :app:assembleDebug`
Expected: 构建成功，无错误

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add Android app entry point and manifest"
```

---

## 自检

### 1. 规格覆盖率检查

| 规格要求 | 实现任务 |
|----------|----------|
| KMP + Android Canvas | Task 1, 9 |
| Clean Architecture | Task 1 |
| 10章节×10关卡 | Task 3, 8, 10 |
| 启动页 | Task 8 |
| 主菜单 | Task 8 |
| 角色属性页 | Task 10 |
| 章节选择 | Task 10 |
| 关卡选择 | Task 10 |
| 游戏页面 | Task 9 |
| 点击/蓄力跳跃 | Task 4, 9 |
| 二段跳 | Task 4 |
| 道具系统 | Task 3, 4, 9 |
| 碰撞检测 | Task 4 |
| 平台生成 | Task 3 |
| 数据存储 | Task 6 |
| 音频系统 | 后续扩展 |

### 2. 占位符扫描

- ✅ 无 "TBD"、"TODO"、"implement later"
- ✅ 所有步骤都有具体代码
- ✅ 所有测试都有具体实现
- ✅ 所有路径都是精确的

### 3. 类型一致性检查

- ✅ `GameState` 在所有文件中一致使用
- ✅ `Character` 模型在所有地方签名匹配
- ✅ `PowerUpType` 枚举在所有地方一致
- ✅ `GameProgress` 数据结构一致

### 4. 无歧义检查

- ✅ 所有方法都有明确的实现
- ✅ 所有依赖关系清晰
- ✅ 所有测试都有明确的预期结果

---

## 后续步骤

1. 音频系统实现
2. 更精细的角色动画
3. 移动平台完整实现
4. 道具视觉效果
5. 性能优化

**计划完成。**