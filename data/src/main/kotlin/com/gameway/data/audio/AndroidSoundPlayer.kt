package com.gameway.data.audio

import android.content.Context
import android.media.SoundPool
import com.gameway.domain.engine.GameSound
import com.gameway.domain.engine.SoundPlayer

class AndroidSoundPlayer(context: Context) : SoundPlayer {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(5).build()
    private val soundIds: MutableMap<GameSound, Int> = mutableMapOf()
    private var volume: Float = 1.0f
    private var muted: Boolean = false
    
    init {
        loadSounds(context)
    }
    
    private fun loadSounds(context: Context) {
        // Sound files would be in res/raw/
        // For now, use placeholder IDs - actual implementation would load from resources
        // soundIds[GameSound.JUMP] = soundPool.load(context, R.raw.jump, 1)
        // etc.
    }
    
    override fun play(sound: GameSound) {
        if (muted) return
        val soundId = soundIds[sound] ?: return
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }
    
    override fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
    }
    
    override fun mute() { muted = true }
    override fun unmute() { muted = false }
    override fun isMuted(): Boolean = muted
    
    fun release() {
        soundPool.release()
    }
}