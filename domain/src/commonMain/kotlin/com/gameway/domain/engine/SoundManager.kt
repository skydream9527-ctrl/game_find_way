package com.gameway.domain.engine

enum class GameSound {
    JUMP, LAND, COIN, POWERUP, FAIL, COMPLETE, COUNTDOWN_TICK
}

interface SoundPlayer {
    fun play(sound: GameSound)
    fun setVolume(volume: Float)
    fun mute()
    fun unmute()
    fun isMuted(): Boolean
}

class SoundManager {
    private var volume: Float = 1.0f
    private var muted: Boolean = false
    
    fun play(sound: GameSound, player: SoundPlayer? = null) {
        if (muted || player == null) return
        player.play(sound)
    }
    
    fun setVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0f, 1f)
    }
    
    fun mute() { muted = true }
    fun unmute() { muted = false }
    fun isMuted(): Boolean = muted
    fun getVolume(): Float = volume
}