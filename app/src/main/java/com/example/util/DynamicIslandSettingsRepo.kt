package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.IslandThemeMode
import com.example.model.VisualizerMode
import com.example.viewmodel.AuraLightingEffect
import com.example.viewmodel.IslandSettings
import com.example.viewmodel.MediaDisplayMode

class DynamicIslandSettingsRepo(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "dynamic_island_settings"

        // Keys
        private const val KEY_Y_OFFSET = "y_offset_dp"
        private const val KEY_PILL_OFFSET_Y = "pill_offset_y_dp"
        private const val KEY_PILL_SCALE_WIDTH = "pill_scale_width"
        private const val KEY_PILL_WIDTH = "pill_width_dp"
        private const val KEY_PILL_HEIGHT = "pill_height_dp"

        private const val KEY_AURA_BREATHING = "aura_breathing_enabled"
        private const val KEY_AURA_EDGE_SHEEN = "aura_edge_sheen_enabled"
        private const val KEY_AURA_BEAT_PULSE = "aura_beat_pulse_enabled"
        private const val KEY_AURA_INTENSITY = "aura_intensity"
        private const val KEY_ENABLE_GLOW = "enable_glow"
        private const val KEY_GLOW_INTENSITY = "glow_intensity"
        private const val KEY_AURA_LIGHTING_EFFECT = "aura_lighting_effect"
        private const val KEY_SPECULAR_SHEEN = "specular_sheen_enabled"

        private const val KEY_PULSE_ENABLED = "pulse_enabled"
        private const val KEY_SPRING_ENABLED = "spring_enabled"
        private const val KEY_ENABLE_HAPTICS = "enable_haptics"
        private const val KEY_SHOW_SYSTEM_OVERLAY_BTN = "show_system_overlay_btn"
        private const val KEY_AUTO_COLLAPSE_ENABLED = "auto_collapse_enabled"
        private const val KEY_AUTO_COLLAPSE_DELAY = "auto_collapse_delay_sec"

        private const val KEY_MEDIA_DISPLAY_MODE = "media_display_mode"
        private const val KEY_VISUALIZER_MODE = "visualizer_mode"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_CUSTOM_ACCENT_COLOR = "custom_accent_color_long"
        private const val KEY_SHOW_COMPACT_SONG_TITLE = "show_compact_song_title"
        private const val KEY_ENABLE_DYNAMIC_THEME = "enable_dynamic_theme"
        private const val KEY_ENABLE_HYPER_SMOOTH = "enable_hyper_smooth"

        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_DARK_BG_OPACITY = "dark_bg_opacity"
        private const val KEY_DARK_BLUR_LEVEL = "dark_blur_level_dp"
        private const val KEY_LIGHT_BG_OPACITY = "light_bg_opacity"
        private const val KEY_LIGHT_BLUR_LEVEL = "light_blur_level_dp"

        private const val KEY_NOTIF_MIRRORING = "notification_mirroring_enabled"
        private const val KEY_NOTIF_DURATION = "notification_duration_sec"
        private const val KEY_HIDE_SENSITIVE = "hide_sensitive_content"

        private const val KEY_AUTO_LIGHT_SENSOR_THEME = "auto_light_sensor_theme"
        private const val KEY_LIGHT_SENSOR_THRESHOLD = "light_sensor_lux_threshold"
        private const val KEY_DARK_THRESHOLD_LX = "dark_threshold_lx"
        private const val KEY_LIGHT_THRESHOLD_LX = "light_threshold_lx"

        private const val KEY_SHOW_BATTERY = "show_battery_indicator"
        private const val KEY_ENABLE_SHAKE_CLEAR = "enable_shake_clear"
        private const val KEY_SHAKE_THRESHOLD_G = "shake_threshold_g"

        private const val KEY_SOUND_PROFILE = "sound_profile"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_ENABLE_SOUND = "enable_sound"
    }

    private val defaultSettings = IslandSettings()

    fun loadSettings(): IslandSettings {
        val auraEffectStr = prefs.getString(KEY_AURA_LIGHTING_EFFECT, defaultSettings.auraLightingEffect.name)
        val auraEffect = try {
            AuraLightingEffect.valueOf(auraEffectStr ?: defaultSettings.auraLightingEffect.name)
        } catch (e: Exception) {
            defaultSettings.auraLightingEffect
        }

        val mediaModeStr = prefs.getString(KEY_MEDIA_DISPLAY_MODE, defaultSettings.mediaDisplayMode.name)
        val mediaMode = try {
            MediaDisplayMode.valueOf(mediaModeStr ?: defaultSettings.mediaDisplayMode.name)
        } catch (e: Exception) {
            defaultSettings.mediaDisplayMode
        }

        val vizModeStr = prefs.getString(KEY_VISUALIZER_MODE, defaultSettings.visualizerMode.name)
        val vizMode = try {
            VisualizerMode.valueOf(vizModeStr ?: defaultSettings.visualizerMode.name)
        } catch (e: Exception) {
            defaultSettings.visualizerMode
        }

        val themeModeStr = prefs.getString(KEY_THEME_MODE, defaultSettings.themeMode.name)
        val themeMode = try {
            IslandThemeMode.valueOf(themeModeStr ?: defaultSettings.themeMode.name)
        } catch (e: Exception) {
            defaultSettings.themeMode
        }

        return IslandSettings(
            yOffsetDp = prefs.getFloat(KEY_Y_OFFSET, defaultSettings.yOffsetDp),
            pillOffsetYDp = prefs.getFloat(KEY_PILL_OFFSET_Y, defaultSettings.pillOffsetYDp),
            pillScaleWidth = prefs.getFloat(KEY_PILL_SCALE_WIDTH, defaultSettings.pillScaleWidth),
            pillWidthDp = prefs.getFloat(KEY_PILL_WIDTH, defaultSettings.pillWidthDp),
            pillHeightDp = prefs.getFloat(KEY_PILL_HEIGHT, defaultSettings.pillHeightDp),
            isBreathingGlowEnabled = prefs.getBoolean(KEY_AURA_BREATHING, defaultSettings.isBreathingGlowEnabled),
            isEdgeSheenEnabled = prefs.getBoolean(KEY_AURA_EDGE_SHEEN, defaultSettings.isEdgeSheenEnabled),
            isBeatPulseEnabled = prefs.getBoolean(KEY_AURA_BEAT_PULSE, defaultSettings.isBeatPulseEnabled),
            auraIntensity = prefs.getFloat(KEY_AURA_INTENSITY, defaultSettings.auraIntensity),
            enableGlow = prefs.getBoolean(KEY_ENABLE_GLOW, defaultSettings.enableGlow),
            glowIntensity = prefs.getFloat(KEY_GLOW_INTENSITY, defaultSettings.glowIntensity),
            auraLightingEffect = auraEffect,
            specularSheenEnabled = prefs.getBoolean(KEY_SPECULAR_SHEEN, defaultSettings.specularSheenEnabled),
            isPulseEnabled = prefs.getBoolean(KEY_PULSE_ENABLED, defaultSettings.isPulseEnabled),
            isSpringEnabled = prefs.getBoolean(KEY_SPRING_ENABLED, defaultSettings.isSpringEnabled),
            enableHaptics = prefs.getBoolean(KEY_ENABLE_HAPTICS, defaultSettings.enableHaptics),
            showSystemOverlayButton = prefs.getBoolean(KEY_SHOW_SYSTEM_OVERLAY_BTN, defaultSettings.showSystemOverlayButton),
            enableAutoCollapse = prefs.getBoolean(KEY_AUTO_COLLAPSE_ENABLED, defaultSettings.enableAutoCollapse),
            autoCollapseDelaySeconds = prefs.getInt(KEY_AUTO_COLLAPSE_DELAY, defaultSettings.autoCollapseDelaySeconds),
            mediaDisplayMode = mediaMode,
            visualizerMode = vizMode,
            themeMode = themeMode,
            customAccentColorLong = prefs.getLong(KEY_CUSTOM_ACCENT_COLOR, defaultSettings.customAccentColorLong),
            showCompactSongTitle = prefs.getBoolean(KEY_SHOW_COMPACT_SONG_TITLE, defaultSettings.showCompactSongTitle),
            enableDynamicTheme = prefs.getBoolean(KEY_ENABLE_DYNAMIC_THEME, defaultSettings.enableDynamicTheme),
            enableHyperSmoothMode = prefs.getBoolean(KEY_ENABLE_HYPER_SMOOTH, defaultSettings.enableHyperSmoothMode),
            isDarkMode = prefs.getBoolean(KEY_IS_DARK_MODE, defaultSettings.isDarkMode),
            darkBgOpacity = prefs.getFloat(KEY_DARK_BG_OPACITY, defaultSettings.darkBgOpacity),
            darkBlurLevelDp = prefs.getFloat(KEY_DARK_BLUR_LEVEL, defaultSettings.darkBlurLevelDp),
            lightBgOpacity = prefs.getFloat(KEY_LIGHT_BG_OPACITY, defaultSettings.lightBgOpacity),
            lightBlurLevelDp = prefs.getFloat(KEY_LIGHT_BLUR_LEVEL, defaultSettings.lightBlurLevelDp),
            enableNotificationMirroring = prefs.getBoolean(KEY_NOTIF_MIRRORING, defaultSettings.enableNotificationMirroring),
            notificationDurationSeconds = prefs.getInt(KEY_NOTIF_DURATION, defaultSettings.notificationDurationSeconds),
            hideSensitiveContent = prefs.getBoolean(KEY_HIDE_SENSITIVE, defaultSettings.hideSensitiveContent),
            autoLightSensorTheme = prefs.getBoolean(KEY_AUTO_LIGHT_SENSOR_THEME, defaultSettings.autoLightSensorTheme),
            lightSensorLuxThreshold = prefs.getFloat(KEY_LIGHT_SENSOR_THRESHOLD, defaultSettings.lightSensorLuxThreshold),
            darkThresholdLx = prefs.getFloat(KEY_DARK_THRESHOLD_LX, defaultSettings.darkThresholdLx),
            lightThresholdLx = prefs.getFloat(KEY_LIGHT_THRESHOLD_LX, defaultSettings.lightThresholdLx),
            showBatteryIndicator = prefs.getBoolean(KEY_SHOW_BATTERY, defaultSettings.showBatteryIndicator),
            enableShakeToClear = prefs.getBoolean(KEY_ENABLE_SHAKE_CLEAR, defaultSettings.enableShakeToClear),
            shakeThresholdG = prefs.getFloat(KEY_SHAKE_THRESHOLD_G, defaultSettings.shakeThresholdG),
            soundProfile = prefs.getString(KEY_SOUND_PROFILE, defaultSettings.soundProfile) ?: defaultSettings.soundProfile,
            soundVolume = prefs.getFloat(KEY_SOUND_VOLUME, defaultSettings.soundVolume),
            isSoundEnabled = prefs.getBoolean(KEY_ENABLE_SOUND, defaultSettings.isSoundEnabled)
        )
    }

    fun saveSettings(settings: IslandSettings) {
        prefs.edit()
            .putFloat(KEY_Y_OFFSET, settings.yOffsetDp)
            .putFloat(KEY_PILL_OFFSET_Y, settings.pillOffsetYDp)
            .putFloat(KEY_PILL_SCALE_WIDTH, settings.pillScaleWidth)
            .putFloat(KEY_PILL_WIDTH, settings.pillWidthDp)
            .putFloat(KEY_PILL_HEIGHT, settings.pillHeightDp)
            .putBoolean(KEY_AURA_BREATHING, settings.isBreathingGlowEnabled)
            .putBoolean(KEY_AURA_EDGE_SHEEN, settings.isEdgeSheenEnabled)
            .putBoolean(KEY_AURA_BEAT_PULSE, settings.isBeatPulseEnabled)
            .putFloat(KEY_AURA_INTENSITY, settings.auraIntensity)
            .putBoolean(KEY_ENABLE_GLOW, settings.enableGlow)
            .putFloat(KEY_GLOW_INTENSITY, settings.glowIntensity)
            .putString(KEY_AURA_LIGHTING_EFFECT, settings.auraLightingEffect.name)
            .putBoolean(KEY_SPECULAR_SHEEN, settings.specularSheenEnabled)
            .putBoolean(KEY_PULSE_ENABLED, settings.isPulseEnabled)
            .putBoolean(KEY_SPRING_ENABLED, settings.isSpringEnabled)
            .putBoolean(KEY_ENABLE_HAPTICS, settings.enableHaptics)
            .putBoolean(KEY_SHOW_SYSTEM_OVERLAY_BTN, settings.showSystemOverlayButton)
            .putBoolean(KEY_AUTO_COLLAPSE_ENABLED, settings.enableAutoCollapse)
            .putInt(KEY_AUTO_COLLAPSE_DELAY, settings.autoCollapseDelaySeconds)
            .putString(KEY_MEDIA_DISPLAY_MODE, settings.mediaDisplayMode.name)
            .putString(KEY_VISUALIZER_MODE, settings.visualizerMode.name)
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putLong(KEY_CUSTOM_ACCENT_COLOR, settings.customAccentColorLong)
            .putBoolean(KEY_SHOW_COMPACT_SONG_TITLE, settings.showCompactSongTitle)
            .putBoolean(KEY_ENABLE_DYNAMIC_THEME, settings.enableDynamicTheme)
            .putBoolean(KEY_ENABLE_HYPER_SMOOTH, settings.enableHyperSmoothMode)
            .putBoolean(KEY_IS_DARK_MODE, settings.isDarkMode)
            .putFloat(KEY_DARK_BG_OPACITY, settings.darkBgOpacity)
            .putFloat(KEY_DARK_BLUR_LEVEL, settings.darkBlurLevelDp)
            .putFloat(KEY_LIGHT_BG_OPACITY, settings.lightBgOpacity)
            .putFloat(KEY_LIGHT_BLUR_LEVEL, settings.lightBlurLevelDp)
            .putBoolean(KEY_NOTIF_MIRRORING, settings.enableNotificationMirroring)
            .putInt(KEY_NOTIF_DURATION, settings.notificationDurationSeconds)
            .putBoolean(KEY_HIDE_SENSITIVE, settings.hideSensitiveContent)
            .putBoolean(KEY_AUTO_LIGHT_SENSOR_THEME, settings.autoLightSensorTheme)
            .putFloat(KEY_LIGHT_SENSOR_THRESHOLD, settings.lightSensorLuxThreshold)
            .putFloat(KEY_DARK_THRESHOLD_LX, settings.darkThresholdLx)
            .putFloat(KEY_LIGHT_THRESHOLD_LX, settings.lightThresholdLx)
            .putBoolean(KEY_SHOW_BATTERY, settings.showBatteryIndicator)
            .putBoolean(KEY_ENABLE_SHAKE_CLEAR, settings.enableShakeToClear)
            .putFloat(KEY_SHAKE_THRESHOLD_G, settings.shakeThresholdG)
            .putString(KEY_SOUND_PROFILE, settings.soundProfile)
            .putFloat(KEY_SOUND_VOLUME, settings.soundVolume)
            .putBoolean(KEY_ENABLE_SOUND, settings.isSoundEnabled)
            .apply()
    }

    fun clearAllSettings() {
        prefs.edit().clear().apply()
    }

    // Helper methods matching architectural spec
    val defaultYOffset = 12
    val defaultWidth = 200
    val defaultHeight = 42
    val defaultAuraIntensity = 0.40f

    fun saveYOffset(offset: Int) = prefs.edit().putInt(KEY_Y_OFFSET, offset).apply()
    fun getYOffset(): Int = prefs.getInt(KEY_Y_OFFSET, defaultYOffset)

    fun savePillDimensions(width: Int, height: Int) {
        prefs.edit().putInt(KEY_PILL_WIDTH, width).putInt(KEY_PILL_HEIGHT, height).apply()
    }
    fun getPillWidth(): Int = prefs.getInt(KEY_PILL_WIDTH, defaultWidth)
    fun getPillHeight(): Int = prefs.getInt(KEY_PILL_HEIGHT, defaultHeight)

    fun saveAuraToggles(breathing: Boolean, edgeSheen: Boolean, shockwave: Boolean, intensity: Float) {
        prefs.edit()
            .putBoolean(KEY_AURA_BREATHING, breathing)
            .putBoolean(KEY_AURA_EDGE_SHEEN, edgeSheen)
            .putBoolean(KEY_AURA_BEAT_PULSE, shockwave)
            .putFloat(KEY_AURA_INTENSITY, intensity)
            .apply()
    }

    fun getAuraBreathing(): Boolean = prefs.getBoolean(KEY_AURA_BREATHING, true)
    fun getAuraEdgeSheen(): Boolean = prefs.getBoolean(KEY_AURA_EDGE_SHEEN, true)
    fun getAuraShockwave(): Boolean = prefs.getBoolean(KEY_AURA_BEAT_PULSE, false)
    fun getAuraIntensity(): Float = prefs.getFloat(KEY_AURA_INTENSITY, defaultAuraIntensity)
}
