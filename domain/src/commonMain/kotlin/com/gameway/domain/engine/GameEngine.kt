package com.gameway.domain.engine

import com.gameway.core.GameConstants
import com.gameway.data.audio.BGMManager
import com.gameway.domain.model.ActivePowerUp
import com.gameway.domain.model.Boss
import com.gameway.domain.model.Character
import com.gameway.domain.model.CharacterType
import com.gameway.domain.model.Difficulty
import com.gameway.domain.model.GameState
import com.gameway.domain.model.Level
import com.gameway.domain.model.PlatformType
import com.gameway.domain.model.PowerUpType
import com.gameway.domain.model.Projectile
import com.gameway.domain.usecase.SaveScoreUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GameEngine(
    private val saveScoreUseCase: SaveScoreUseCase,
    private val soundManager: SoundManager = SoundManager(),
    private val soundPlayer: SoundPlayer? = null
) {
    
    private val bgmManager = BGMManager()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var gameState: GameState = GameState.Loading
    private var character: Character = Character.createDefault()
    private var level: Level? = null
    private var scrollX: Float = 0f
    private var score: Int = 0
    private var lastUpdateTime: Long = 0L
    private var countdownEnd: Long = 0L
    private var previousGrounded: Boolean = false
    private var survivalTime: Float = 0f
    private var projectiles: List<Projectile> = emptyList()
    private var boss: Boss? = null
    private var chapter: Int = 1
    private var levelNumber: Int = 1
    private var screenShakeIntensity = 0f
    
    fun startLevel(newLevel: Level, characterType: CharacterType = CharacterType.CAT, bossLevel: Boss? = null, chapterNum: Int = 1, levelNum: Int = 1) {
        level = newLevel
        val firstPlatform = newLevel.platforms.firstOrNull()
        val startY = firstPlatform?.y ?: GameConstants.STARTING_PLATFORM_Y
        character = Character.createDefault(characterType).copy(
            position = com.gameway.domain.model.Vector2(GameConstants.STARTING_POSITION_X, startY - 5f)
        )
        scrollX = 0f
        score = 0
        survivalTime = 0f
        projectiles = emptyList()
        boss = bossLevel
        chapter = chapterNum
        levelNumber = levelNum
        gameState = GameState.Countdown
        countdownEnd = System.currentTimeMillis() + GameConstants.COUNTDOWN_DURATION
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
                    gameState = if (boss != null) GameState.BossActive(0f) else GameState.Playing
                }
                bgmManager.onGameStateChanged(gameState)
            }
            is GameState.Playing -> {
                updateGame(deltaTime, currentTime)
                bgmManager.onGameStateChanged(gameState)
            }
            is GameState.BossActive -> {
                updateBossBattle(deltaTime.toFloat())
                bgmManager.onGameStateChanged(gameState)
            }
            else -> {}
        }
        
        return gameState
    }
    
    private fun updateGame(deltaTime: Long, currentTime: Long) {
        val currentLevel = level ?: return
        
        previousGrounded = character.isGrounded
        character = PhysicsSystem.update(character, deltaTime)
        scrollX = character.position.x - GameConstants.SCROLL_OFFSET
        
        val updatedPlatforms = currentLevel.platforms.map { platform ->
            if (platform.type != PlatformType.STATIC) {
                val offset = kotlin.math.sin((currentTime / GameConstants.PLATFORM_MOVEMENT_SPEED_FACTOR) * platform.moveSpeed + platform.moveOffset) * platform.moveRange
                platform.copy(x = platform.originalX + offset * GameConstants.PLATFORM_MOVEMENT_SCALE)
            } else {
                platform
            }
        }
        
        val collisions = CollisionDetector.checkCollisions(character, updatedPlatforms, currentLevel.coins, currentLevel.powerUps)
        
        for (collision in collisions) {
            when (collision) {
                is CollisionResult.LandedOnPlatform -> {
                    if (!previousGrounded && character.velocity.y > 0) {
                        soundManager.play(GameSound.LAND, soundPlayer)
                    }
                    character = PhysicsSystem.landOnPlatform(character, collision.platform.y)
                    previousGrounded = true
                }
                is CollisionResult.HitSideOfPlatform -> {
                    gameState = if (character.hasShield) {
                        character = character.copy(activePowerUps = character.activePowerUps.filter { it.type != PowerUpType.SHIELD })
                        GameState.Playing
                    } else {
                        soundManager.play(GameSound.FAIL, soundPlayer)
                        GameState.Failed("撞到平台侧面")
                    }
                }
                is CollisionResult.CollectedCoin -> {
                    soundManager.play(GameSound.COIN, soundPlayer)
                    score += GameConstants.COIN_SCORE
                    character = character.copy(coinsCollected = character.coinsCollected + 1)
                }
                is CollisionResult.CollectedPowerUp -> {
                    soundManager.play(GameSound.POWERUP, soundPlayer)
                    val powerUp = collision.powerUp
                    character = character.copy(
                        activePowerUps = character.activePowerUps + ActivePowerUp.create(powerUp.type),
                        maxJumps = if (powerUp.type == PowerUpType.GEM) 2 else character.maxJumps
                    )
                    score += GameConstants.POWERUP_SCORE
                }
                is CollisionResult.FellOffScreen -> {
                    gameState = if (character.hasShield) {
                        character = character.copy(activePowerUps = character.activePowerUps.filter { it.type != PowerUpType.SHIELD })
                        GameState.Playing
                    } else {
                        soundManager.play(GameSound.FAIL, soundPlayer)
                        GameState.Failed("掉落屏幕")
                    }
                }
                CollisionResult.NoCollision -> {}
            }
        }
        
        val lastPlatform = updatedPlatforms.lastOrNull()
        if (lastPlatform != null && character.position.x > lastPlatform.x + lastPlatform.width) {
            soundManager.play(GameSound.COMPLETE, soundPlayer)
            gameState = GameState.Completed(score, character.coinsCollected)
            scope.launch { saveScoreUseCase(score, chapter, levelNumber) }
        }
    }

    private fun updateBossBattle(deltaTime: Float) {
        val currentBoss = boss ?: return

        survivalTime += deltaTime

        val bossResult = BossEngine.update(currentBoss, character, deltaTime)
        boss = bossResult.boss
        projectiles = bossResult.projectiles

        val collision = CollisionDetector.checkBossCollisions(
            character,
            currentBoss,
            projectiles,
            currentBoss.laserActive,
            currentBoss.laserAngle
        )

        if (collision is CollisionResult.Hit) {
            if (character.hasShield) {
                character = removeShield(character)
            } else {
                character = character.copy(health = character.health - collision.damage)
                triggerScreenShake(20f)
                if (character.health <= 0) {
                    soundManager.play(GameSound.FAIL, soundPlayer)
                    gameState = GameState.Failed("被Boss攻击命中！")
                    return
                }
            }
        }

        if (BossEngine.checkSurvival(survivalTime)) {
            triggerScreenShake(10f)
            soundManager.play(GameSound.COMPLETE, soundPlayer)
            gameState = GameState.Completed(score, character.coinsCollected)
            scope.launch { saveScoreUseCase(score, chapter, levelNumber) }
            return
        }

        gameState = GameState.BossActive(survivalTime)
    }

    private fun removeShield(char: Character): Character = char.copy(
        activePowerUps = char.activePowerUps.filter { it.type != PowerUpType.SHIELD }
    )
    
    fun jump() {
        if (gameState !is GameState.Playing && gameState !is GameState.BossActive) return

        if (character.isGrounded || character.jumpCount < character.maxJumps) {
            soundManager.play(GameSound.JUMP, soundPlayer)
            character = PhysicsSystem.applyJump(character)
        }
    }
    
    fun pause() {
        if (gameState is GameState.Playing) {
            gameState = GameState.Paused
            bgmManager.onGameStateChanged(gameState)
        }
    }
    
    fun resume() {
        if (gameState is GameState.Paused) {
            gameState = GameState.Playing
            lastUpdateTime = System.currentTimeMillis()
            bgmManager.onGameStateChanged(gameState)
        }
    }
    
    fun restart() {
        level?.let { startLevel(it, character.type) }
    }
    
    fun setDifficulty(difficulty: Difficulty) {
        bgmManager.setDifficulty(difficulty)
    }
    
    fun getCharacter(): Character = character
    fun getGameState(): GameState = gameState
    fun getScrollX(): Float = scrollX
    fun getScore(): Int = score
    fun getLevel(): Level? = level
    fun getBoss(): Boss? = boss
    fun getProjectiles(): List<Projectile> = projectiles
    fun getSurvivalTime(): Float = survivalTime
    fun getScreenShakeIntensity(): Float = screenShakeIntensity

    fun triggerScreenShake(intensity: Float) {
        screenShakeIntensity = intensity
    }

    fun updateScreenShake() {
        if (screenShakeIntensity > 0.5f) {
            screenShakeIntensity *= 0.9f
        } else {
            screenShakeIntensity = 0f
        }
    }
}