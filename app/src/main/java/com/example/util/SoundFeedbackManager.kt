package com.example.util

import android.content.Context
import android.util.Log
import com.example.model.LiveActivityState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object SoundFeedbackManager {

    private const val TAG = "SoundFeedbackManager"
    private var audioFeedbackManager: AudioFeedbackManager? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var isSoundEnabled: Boolean = true
    var soundProfile: AudioFeedbackManager.SoundProfile = AudioFeedbackManager.SoundProfile.SOFT_POP
    var soundVolume: Float = 0.20f

    fun init(context: Context) {
        if (audioFeedbackManager == null) {
            scope.launch {
                audioFeedbackManager = AudioFeedbackManager.getInstance(context)
            }
        }
    }

    fun updateSettings(profile: AudioFeedbackManager.SoundProfile, volume: Float, enabled: Boolean) {
        soundProfile = profile
        soundVolume = volume
        isSoundEnabled = enabled
    }

    enum class MediaAction {
        PLAY_PAUSE,
        SKIP_NEXT,
        SKIP_PREVIOUS
    }

    fun playFeedback(profile: AudioFeedbackManager.SoundProfile = soundProfile, volume: Float = soundVolume) {
        if (!isSoundEnabled) return
        scope.launch {
            audioFeedbackManager?.playFeedback(profile, volume)
        }
    }

    fun playActivitySwitchSound(activity: LiveActivityState) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                audioFeedbackManager?.playFeedback(soundProfile, soundVolume)
            } catch (e: Exception) {
                Log.w(TAG, "Error playing activity switch sound", e)
            }
        }
    }

    fun playMediaActionSound(action: MediaAction) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                audioFeedbackManager?.playFeedback(soundProfile, soundVolume)
            } catch (e: Exception) {
                Log.w(TAG, "Error playing media action sound", e)
            }
        }
    }

    fun playPillExpansionSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                audioFeedbackManager?.playFeedback(soundProfile, soundVolume)
            } catch (e: Exception) {
                Log.w(TAG, "Error playing expansion sound", e)
            }
        }
    }

    fun playPillCollapseSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                audioFeedbackManager?.playFeedback(soundProfile, soundVolume)
            } catch (e: Exception) {
                Log.w(TAG, "Error playing collapse sound", e)
            }
        }
    }

    fun playChargingConnectedSound() {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                audioFeedbackManager?.playFeedback(soundProfile, soundVolume)
            } catch (e: Exception) {
                Log.w(TAG, "Error playing charging sound", e)
            }
        }
    }
}
