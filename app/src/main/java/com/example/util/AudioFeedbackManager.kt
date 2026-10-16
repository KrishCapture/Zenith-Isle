package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AudioFeedbackManager(private val context: Context) {

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundProfile, Int>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    enum class SoundProfile(val displayName: String) {
        SOFT_POP("Soft Pill Pop"),
        SUBTLE_TICK("Haptic Click"),
        CHIME("Modern Glass Chime"),
        SILENT("Silent")
    }

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()

        scope.launch {
            try {
                val popId = soundPool.load(context, R.raw.pop_subtle, 1)
                val tickId = soundPool.load(context, R.raw.tick_subtle, 1)
                val chimeId = soundPool.load(context, R.raw.chime_glass, 1)
                synchronized(soundMap) {
                    soundMap[SoundProfile.SOFT_POP] = popId
                    soundMap[SoundProfile.SUBTLE_TICK] = tickId
                    soundMap[SoundProfile.CHIME] = chimeId
                }
            } catch (e: Exception) {
                Log.e("AudioFeedbackManager", "Error pre-loading sound assets", e)
            }
        }
    }

    fun playFeedback(profile: SoundProfile, volume: Float = 0.20f) {
        if (profile == SoundProfile.SILENT) return
        scope.launch {
            val soundId = synchronized(soundMap) { soundMap[profile] } ?: return@launch
            val clampedVol = volume.coerceIn(0.0f, 1.0f)
            soundPool.play(soundId, clampedVol, clampedVol, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }

    companion object {
        @Volatile
        private var instance: AudioFeedbackManager? = null

        fun getInstance(context: Context): AudioFeedbackManager {
            return instance ?: synchronized(this) {
                instance ?: AudioFeedbackManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
