package com.example.model

import android.graphics.Bitmap
import android.media.session.MediaController
import androidx.compose.ui.graphics.Color

data class MediaUiState(
    val trackId: String = "",
    val title: String = "",
    val artist: String = "",
    val artwork: Bitmap? = null,
    val accentColor: Color = Color(0xFF00E5FF),
    val durationMs: Long = 180000L,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val lastPositionUpdateTime: Long = 0L,
    val packageName: String? = null,
    val controller: MediaController? = null
)
