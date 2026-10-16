package com.example.model
import androidx.compose.ui.graphics.Color
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val primaryColor: Color,
    val secondaryColor: Color,
    val audioQuality: String = "Dolby Atmos • Hi-Res",
    val isFavorite: Boolean = false,
    val coverBitmap: android.graphics.Bitmap? = null,
    val coverArtUrl: String = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=500&auto=format&fit=crop&q=80"
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
