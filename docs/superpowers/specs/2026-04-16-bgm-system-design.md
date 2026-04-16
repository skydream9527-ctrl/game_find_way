# BGM与音效系统设计文档

**日期:** 2026-04-16
**项目:** FindWay Game
**目标:** 实现程序生成的BGM（随难度变化）和场景切换音效

---

## 1. 概述

### 当前状态
- 有 AndroidSoundPlayer，但仅用于简单音效
- 无BGM系统
- 无程序生成音频能力

### 改进目标
1. 程序生成BGM（不同难度不同风格）
2. 场景切换时自动切换BGM
3. 保留音效接口供后续替换真实音频

---

## 2. 架构设计

### 2.1 BGM系统

```kotlin
interface BGMPlayer {
    fun playDifficultyBGM(difficulty: Difficulty)
    fun playBossBGM()
    fun stop()
    fun setVolume(volume: Float)
}

class ProceduralBGMPlayer : BGMPlayer {
    private val oscillator = Oscillator()
    private var currentPattern = BGMPattern.EASY

    override fun playDifficultyBGM(difficulty: Difficulty) {
        currentPattern = when (difficulty) {
            Difficulty.EASY -> BGMPattern.EASY
            Difficulty.MEDIUM -> BGMPattern.MEDIUM
            Difficulty.HARD -> BGMPattern.HARD
            Difficulty.EXPERT -> BGMPattern.EXPERT
        }
        playPattern(currentPattern)
    }

    override fun playBossBGM() {
        playPattern(BGMPattern.BOSS)
    }
}
```

### 2.2 BGM Pattern 定义

| 难度 | 风格 | 频率范围 | 节拍 |
|------|------|----------|------|
| EASY | 轻柔舒缓 | 220-440Hz | 60 BPM |
| MEDIUM | 节奏感 | 220-550Hz | 90 BPM |
| HARD | 紧张 | 175-550Hz | 120 BPM |
| EXPERT | 史诗 | 165-660Hz | 140 BPM |
| BOSS | Boss战 | 110-440Hz | 150 BPM |

### 2.3 场景切换逻辑

```kotlin
class BGMManager {
    private val bgmPlayer = ProceduralBGMPlayer()

    fun onGameStateChanged(state: GameState) {
        when (state) {
            is GameState.Playing -> bgmPlayer.playDifficultyBGM(currentDifficulty)
            is GameState.BossActive -> bgmPlayer.playBossBGM()
            is GameState.Paused -> bgmPlayer.setVolume(0.3f)
            is GameState.Completed, is GameState.Failed -> bgmPlayer.stop()
            else -> {}
        }
    }
}
```

### 2.4 程序生成音效原理

使用正弦波振荡器生成简单音调：

```kotlin
class Oscillator {
    private var phase = 0.0
    private var sampleRate = 44100

    fun generateTone(frequency: Float, durationMs: Int): ByteArray {
        val numSamples = (sampleRate * durationMs / 1000)
        val buffer = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val sample = (sin(phase * 2 * PI) * 32767).toInt().toShort()
            buffer[i * 2] = (sample and 0xFF).toByte()
            buffer[i * 2 + 1] = (sample shr 8 and 0xFF).toByte()
            phase += frequency / sampleRate
        }
        return buffer
    }

    fun playSequence(notes: List<Note>, bpm: Int) {
        val beatDuration = 60000 / bpm
        notes.forEach { note ->
            playTone(note.frequency, beatDuration)
            Thread.sleep(beatDuration.toLong())
        }
    }
}

data class Note(val frequency: Float, val duration: Int)
```

---

## 3. 实现步骤

### 阶段1：核心组件
1. 创建 `Oscillator` 类
2. 创建 `BGMPlayer` 接口
3. 创建 `ProceduralBGMPlayer` 实现

### 阶段2：集成
1. 在 GameEngine 中集成 BGMManager
2. 根据游戏状态切换BGM
3. 根据难度播放不同BGM

---

## 4. 文件修改清单

**新建文件：**
- `data/src/main/kotlin/com/gameway/data/audio/Oscillator.kt`
- `data/src/main/kotlin/com/gameway/data/audio/BGMPlayer.kt`
- `data/src/main/kotlin/com/gameway/data/audio/ProceduralBGMPlayer.kt`
- `data/src/main/kotlin/com/gameway/data/audio/BGMManager.kt`

**修改文件：**
- `domain/src/commonMain/kotlin/com/gameway/domain/engine/GameEngine.kt`

---

## 5. 验收标准

1. ✅ 游戏开始时根据难度播放对应BGM
2. ✅ Boss战时自动切换到Boss BGM
3. ✅ 暂停时BGM音量降低
4. ✅ 过关/失败时BGM停止
5. ✅ 可扩展支持真实音频文件

---

**文档状态:** 已完成
**下一步:** 用户审核后创建实现计划