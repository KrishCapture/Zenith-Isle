package com.example.util

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PaletteExtractor {

    private fun isNearPureBlack(color: Color): Boolean {
        val luminance = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
        return luminance < 0.05f
    }

    /**
     * Extracts vibrant color from bitmap strictly inside Dispatchers.Default off the UI thread.
     * Downscales bitmap to max 96x96 to avoid CPU/memory spikes during palette analysis.
     */
    suspend fun extractVibrantColor(bitmap: Bitmap?): Color = withContext(Dispatchers.Default) {
        if (bitmap == null || bitmap.isRecycled) return@withContext Color(0xFF00E5FF)
        try {
            val scaled = if (bitmap.width > 96 || bitmap.height > 96) {
                Bitmap.createScaledBitmap(bitmap, 96, 96, false)
            } else {
                bitmap
            }
            val palette = Palette.from(scaled).clearFilters().generate()
            if (scaled != bitmap && !scaled.isRecycled) {
                scaled.recycle()
            }
            val dominant = palette.vibrantSwatch?.rgb 
                ?: palette.dominantSwatch?.rgb 
                ?: palette.mutedSwatch?.rgb
            dominant?.let { Color(it) } ?: Color(0xFF00E5FF)
        } catch (e: Throwable) {
            Color(0xFF00E5FF)
        }
    }

    /**
     * Extracts dynamic accent color STRICTLY from album artwork bitmap.
     * Executes strictly inside Dispatchers.Default off the UI thread.
     */
    suspend fun extractAlbumArtworkColor(bitmap: Bitmap?): Color? = withContext(Dispatchers.Default) {
        if (bitmap == null || bitmap.isRecycled) return@withContext null

        return@withContext try {
            val safeBitmap = try {
                if (bitmap.width > 128 || bitmap.height > 128) {
                    Bitmap.createScaledBitmap(bitmap, 96, 96, false)
                } else {
                    bitmap.copy(Bitmap.Config.ARGB_8888, false)
                }
            } catch (e: Throwable) {
                null
            } ?: return@withContext null

            if (safeBitmap.isRecycled) return@withContext null

            val palette = Palette.from(safeBitmap).clearFilters().generate()
            if (safeBitmap != bitmap && !safeBitmap.isRecycled) {
                safeBitmap.recycle()
            }

            val swatch = palette.vibrantSwatch
                ?: palette.lightVibrantSwatch
                ?: palette.dominantSwatch
                ?: palette.mutedSwatch

            val extracted = swatch?.rgb?.let { Color(it) } ?: return@withContext null

            if (isNearPureBlack(extracted)) {
                null
            } else {
                extracted
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Extracts app brand color strictly for notifications on Dispatchers.Default.
     */
    suspend fun extractAppBrandColor(
        context: Context?,
        packageName: String?,
        notificationColorInt: Int = 0
    ): Color = withContext(Dispatchers.Default) {
        val pkg = packageName?.lowercase() ?: ""

        // Supported brand fallbacks (strictly for notifications)
        when {
            pkg.contains("whatsapp") -> return@withContext Color(0xFF25D366)
            pkg.contains("instagram") -> return@withContext Color(0xFFE1306C)
            pkg.contains("telegram") -> return@withContext Color(0xFF24A1DE)
            pkg.contains("messaging") || pkg.contains("mms") || pkg.contains("sms") -> return@withContext Color(0xFF1A73E8)
            pkg.contains("gmail") || pkg.endsWith(".gm") -> return@withContext Color(0xFFEA4335)
            pkg.contains("slack") -> return@withContext Color(0xFF4A154B)
            pkg.contains("twitter") || pkg.contains("x") -> return@withContext Color(0xFF1DA1F2)
            pkg.contains("strava") -> return@withContext Color(0xFFFC4C02)
        }

        if (notificationColorInt != 0) {
            val c = Color(notificationColorInt)
            if (!isNearPureBlack(c)) {
                return@withContext c
            }
        }

        if (context != null && !packageName.isNullOrBlank()) {
            try {
                val iconDrawable = context.packageManager.getApplicationIcon(packageName)
                val iconBitmap = iconDrawable.toBitmap()
                val extracted = extractAlbumArtworkColor(iconBitmap)
                if (extracted != null) {
                    return@withContext extracted
                }
            } catch (e: Throwable) {
                // Ignore icon extraction failure
            }
        }

        return@withContext Color(0xFF3B82F6) // Clean Notification Blue default
    }

    // Deprecated/Compatibility overload
    suspend fun extractAccentColor(
        bitmap: Bitmap?,
        defaultColor: Color = Color(0xFF6366F1),
        packageName: String? = null
    ): Color = withContext(Dispatchers.Default) {
        extractAlbumArtworkColor(bitmap) ?: defaultColor
    }
}
