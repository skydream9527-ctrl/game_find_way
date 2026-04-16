# BGM系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现程序生成的BGM系统，根据难度和场景自动切换

**Architecture:** BGMPlayer接口 + ProceduralBGMPlayer实现 + BGMManager管理场景切换

**Tech Stack:** Kotlin, Android MediaPlayer, Jetpack Compose

---

## 文件结构

```
data/src/main/kotlin/com/gameway/data/audio/
├── Oscillator.kt              # 新建
├── BGMPlayer.kt              # 新建
├── BGMPattern.kt             # 新建
├── ProceduralBGMPlayer.kt    # 新建
└── BGMManager.kt             # 新建
domain/src/commonMain/kotlin/com/gameway/domain/engine/
└── GameEngine.kt             # 修改
```

---

## Task 1: 创建核心组件

**Files:**
- Create: `data/src/main/kotlin/com/gameway/data/audio/Oscillator.kt`
- Create: `data/src/main/kotlin/com/gameway/data/audio/BGMPattern.kt`
- Create: `data/src/main/kotlin/com/gameway/data/audio/BGMPlayer.kt`

- [ ] **Step 1: 创建 Oscillator.kt**

```kotlin
package com.gameway.data.audio

import kotlin.math.PI
import kotlin.math.sin

class Oscillator(private val sampleRate: Int = 44100) {
    private var phase = 0.0

    fun generateTone(frequency: Float, durationMs: Int, volume: Float = 0.3f): ByteArray {
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val sample = (sin(phase * 2 * PI) * 32767 * volume).toInt().coerceIn(-32768, 32767).toShort()
            buffer[i * 2] = (sample.toInt() and 0xFF).toByte()
            buffer[i * 2 + 1] = (sample.toInt() shr 8 and 0xFF).toByte()
            phase += frequency / sampleRate
            if (phase > 1.0) phase -= 1.0
        }
        return buffer
    }

    fun reset() {
        phase = 0.0
    }
}

data class Note(val frequency: Float, val durationMs: Int)
```

- [ ] **Step 2: 创建 BGMPattern.kt**

```kotlin
package com.gameway.data.audio

enum class BGMPattern(
    val notes: List<Note>,
    val bpm: Int
) {
    EASY(listOf(
        Note(261.63f, 500), Note(293.66f, 500), Note(329.63f, 500), Note(349.23f, 500),
        Note(392.00f, 500), Note(349.23f, 500), Note(329.63f, 500), Note(293.66f, 500)
    ), 60),

    MEDIUM(listOf(
        Note(293.66f, 333), Note(329.63f, 333), Note(349.23f, 333), Note(392.00f, 333),
        Note(440.00f, 333), Note(392.00f, 333), Note(349.23f, 333), Note(329.63f, 333),
        Note(293.66f, 333), Note(261.63f, 333), Note(293.66f, 333), Note(329.63f, 333)
    ), 90),

    HARD(listOf(
        Note(220.00f, 250), Note(261.63f, 250), Note(293.66f, 250), Note(329.63f, 250),
        Note(349.23f, 250), Note(392.00f, 250), Note(440.00f, 250), Note(493.88f, 250),
        Note(523.25f, 250), Note(493.88f, 250), Note(440.00f, 250), Note(392.00f, 250)
    ), 120),

    EXPERT(listOf(
        Note(164.81f, 214), Note(196.00f, 214), Note(220.00f, 214), Note(246.94f, 214),
        Note(261.63f, 214), Note(293.66f, 214), Note(329.63f, 214), Note(349.23f, 214),
        Note(392.00f, 214), Note(440.00f, 214), Note(493.88f, 214), Note(523.25f, 214),
        Note(587.33f, 214), Note(659.25f, 214)
    ), 140),

    BOSS(listOf(
        Note(110.00f, 200), Note(130.81f, 200), Note(146.83f, 200), Note(164.81f, 200),
        Note(174.61f, 200), Note(196.00f, 200), Note(220.00f, 200), Note(246.94f, 200),
        Note(261.63f, 200), Note(293.66f, 200), Note(329.63f, 200), Note(349.23f, 200)
    ), 150)
}
```

- [ ] **Step 3: 创建 BGMPlayer.kt**

```kotlin
package com.gameway.data.audio

import com.gameway.domain.model.Difficulty

interface BGMPlayer {
    fun playDifficultyBGM(difficulty: Difficulty)
    fun playBossBGM()
    fun stop()
    fun pause()
    fun resume()
    fun setVolume(volume: Float)
}
```

- [ ] **Step 4: 提交**

```bash
git add data/src/main/kotlin/com/gameway/data/audio/Oscillator.kt
git add data/src/main/kotlin/com/gameway/data/audio/BGMPattern.kt
git add data/src/main/kotlin/com/gameway/data/audio/BGMPlayer.kt
git commit -m "feat(bgm): add BGM core components"
```

---

## Task 2: 创建 ProceduralBGMPlayer

**Files:**
- Create: `data/src/main/kotlin/com/gameway/data/audio/ProceduralBGMPlayer.kt`

- [ ] **Step 1: 创建 ProceduralBGMPlayer.kt**

```kotlin
package com.gameway.data.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.gameway.domain.model.Difficulty
import kotlinx.coroutines.*

class ProceduralBGMPlayer : BGMPlayer {
    private val oscillator = Oscillator()
    private var currentJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var volume = 1.0f
    private var isPaused = false

    private val difficultyPatterns = mapOf(
        Difficulty.EASY to BGMPattern.EASY,
        Difficulty.MEDIUM to BGMPattern.MEDIUM,
        Difficulty.HARD to BGMPattern.HARD,
        Difficulty.EXPERT to BGMPattern.EXPERT
    )

    override fun playDifficultyBGM(difficulty: Difficulty) {
        val pattern = difficultyPatterns[difficulty] ?: BGMPattern.EASY
        playPattern(pattern)
    }

    override fun playBossBGM() {
        playPattern(BGMPattern.BOSS)
    }

    private fun playPattern(pattern: BGMPattern) {
        stop()
        currentJob = scope.launch {
            while (isActive && !isPaused) {
                for (note in pattern.notes) {
                    if (!isActive || isPaused) break
                    playNote(note)
                }
            }
        }
    }

    private fun playNote(note: Note) {
        val buffer = oscillator.generateTone(note.frequency, note.durationMs, volume)
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(buffer.size)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        Thread.sleep(note.durationMs.toLong())
        audioTrack.stop()
        audioTrack.release()
        oscillator.reset()
    }

    override fun stop() {
        currentJob?.cancel()
        currentJob = null
        isPaused = false
    }

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        if (isPaused) {
            isPaused = false
        }
    }

    override fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add data/src/main/kotlin/com/gameway/data/audio/ProceduralBGMPlayer.kt
git commit -m "feat(bgm): add ProceduralBGMPlayer"
```

---

## Task 3: 创建 BGMManager

**Files:**
- Create: `data/src/main/kotlin/com/gameway/data/audio/BGMManager.kt`

- [ ] **Step 1: 创建 BGMManager.kt**

```kotlin
package com.gameway.data.audio

import com.gameway.domain.model.Difficulty
import com.gameway.domain.model.GameState

class BGMManager(
    private val bgmPlayer: BGMPlayer = ProceduralBGMPlayer()
) {
    private var currentDifficulty: Difficulty = Difficulty.EASY

    fun onGameStateChanged(state: GameState) {
        when (state) {
            is GameState.Playing -> bgmPlayer.playDifficultyBGM(currentDifficulty)
            is GameState.BossActive -> bgmPlayer.playBossBGM()
            is GameState.Paused -> bgmPlayer.setVolume(0.3f)
            is GameState.Countdown -> bgmPlayer.setVolume(0.5f)
            is GameState.Completed, is GameState.Failed -> {
                bgmPlayer.stop()
            }
            else -> {}
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        currentDifficulty = difficulty
    }

    fun onResume() {
        bgmPlayer.setVolume(1.0f)
        bgmPlayer.resume()
    }

    fun release() {
        bgmPlayer.stop()
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add data/src/main/kotlin/com/gameway/data/audio/BGMManager.kt
git commit -m "feat(bgm): add BGMManager for scene transitions"
```

---

## Task 4: 集成到 GameEngine

**Files:**
- Modify: `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`

- [ ] **Step 1: 添加 BGMManager 集成**

Add BGMManager field and update game state handling:

```kotlin
private val bgmManager = BGMManager()

fun update(deltaTime: Float) {
    when (val state = uiState.value.gameState) {
        is GameState.Playing -> {
            // existing logic
            bgmManager.onGameStateChanged(state)
        }
        is GameState.BossActive -> {
            // existing logic
            bgmManager.onGameStateChanged(state)
        }
        is GameState.Paused -> {
            bgmManager.onGameStateChanged(state)
        }
        is GameState.Countdown -> {
            bgmManager.onGameStateChanged(state)
        }
        is GameState.Completed, is GameState.Failed -> {
            bgmManager.onGameStateChanged(state)
        }
        else -> {}
    }
}

fun setDifficulty(difficulty: Difficulty) {
    bgmManager.setDifficulty(difficulty)
}
```

- [ ] **Step 2: 提交**

```bash
git add domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt
git commit -m "feat(bgm): integrate BGMManager into GameEngine"
```

---

## 实现顺序

1. Task 1: 创建核心组件 (Oscillator, BGMPattern, BGMPlayer)
2. Task 2: 创建 ProceduralBGMPlayer
3. Task 3: 创建 BGMManager
4. Task 4: 集成到 GameEngine

---

## 验收标准

1. 游戏开始时根据难度播放对应BGM
2. Boss战时自动切换到Boss BGM
3. 暂停时BGM音量降低
4. 过关/失败时BGM停止
5. 可扩展支持真实音频文件

---

**Plan complete.**