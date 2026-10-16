package com.example.viewmodel

import android.app.KeyguardManager
import android.content.Context
import android.media.session.MediaController
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.IncomingNotification
import com.example.model.IslandExpansionMode
import com.example.model.IslandThemeMode
import com.example.model.LiveActivityState
import com.example.model.NavigationDirection
import com.example.model.NotificationActionItem
import com.example.model.OtpSanitizer
import com.example.model.Song
import com.example.model.VisualizerMode
import com.example.util.DynamicIslandSettingsRepo
import com.example.util.PaletteExtractor
import com.example.util.SoundFeedbackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MediaDisplayMode {
    BOTH,          // Leading: Album Art | Trailing: Waveform Equalizer
    COVER_ONLY,    // Leading: Album Art | Trailing: Track & Artist Text (No waveform)
    WAVEFORM_ONLY  // Leading: Music Note/Track Info | Trailing: Waveform Equalizer (No album art)
}

enum class AuraLightingEffect {
    BREATHING_HALO,      // Dual-phase organic breathing
    SONIC_BEAT_PING,     // Rhythmic shockwave pulse
    SPECULAR_SHEEN_ONLY, // Clean border sheen with static halo
    HYBRID_PREMIUM       // Breathing halo + periodic specular border glint
}

data class IslandSettings(
    val yOffsetDp: Float = 12f,
    val pillOffsetYDp: Float = 0f,
    val pillScaleWidth: Float = 1.0f,
    val pillWidthDp: Float = 200f,
    val pillHeightDp: Float = 42f,
    val isBreathingGlowEnabled: Boolean = true,
    val isEdgeSheenEnabled: Boolean = true,
    val isBeatPulseEnabled: Boolean = false,
    val auraIntensity: Float = 0.40f,
    val enableGlow: Boolean = true,
    val glowIntensity: Float = 0.40f,
    val auraLightingEffect: AuraLightingEffect = AuraLightingEffect.HYBRID_PREMIUM,
    val specularSheenEnabled: Boolean = true,
    val isPulseEnabled: Boolean = false,
    val isSpringEnabled: Boolean = true,
    val enableHaptics: Boolean = true,
    val showSystemOverlayButton: Boolean = true,
    val isSystemOverlayActive: Boolean = false,
    val enableAutoCollapse: Boolean = true,
    val autoCollapseDelaySeconds: Int = 5,
    val mediaDisplayMode: MediaDisplayMode = MediaDisplayMode.BOTH,
    val visualizerMode: VisualizerMode = VisualizerMode.EQUALIZER_BARS,
    val themeMode: IslandThemeMode = IslandThemeMode.ADAPTIVE_ARTWORK,
    val customAccentColorLong: Long = 0xFF8B5CF6,
    val showCompactSongTitle: Boolean = false,
    val enableDynamicTheme: Boolean = true,
    val enableHyperSmoothMode: Boolean = false,
    val isDarkMode: Boolean = true,
    val darkBgOpacity: Float = 0.95f,
    val darkBlurLevelDp: Float = 0f,
    val lightBgOpacity: Float = 0.80f,
    val lightBlurLevelDp: Float = 16f,
    val enableNotificationMirroring: Boolean = true,
    val notificationDurationSeconds: Int = 7,
    val hideSensitiveContent: Boolean = true,
    val autoLightSensorTheme: Boolean = true,
    val ambientLuxValue: Float = 35f,
    val lightSensorLuxThreshold: Float = 40f,
    val darkThresholdLx: Float = 35f,
    val lightThresholdLx: Float = 85f,
    val showBatteryIndicator: Boolean = true,
    val isCharging: Boolean = true,
    val batteryLevel: Int = 85,
    val enableShakeToClear: Boolean = true,
    val shakeThresholdG: Float = 2.7f,
    val soundProfile: String = "SOFT_POP",
    val soundVolume: Float = 0.20f,
    val isSoundEnabled: Boolean = true
)

object GlobalIslandState {

    private var settingsRepo: DynamicIslandSettingsRepo? = null
    private var persistenceScope: CoroutineScope? = null

    fun initPersistence(context: Context) {
        if (settingsRepo == null) {
            val repo = DynamicIslandSettingsRepo(context.applicationContext)
            settingsRepo = repo
            val loaded = repo.loadSettings()
            settings.value = loaded
            pillOffsetYDp.value = loaded.pillOffsetYDp

            com.example.util.SoundFeedbackManager.init(context)
            val profile = try {
                com.example.util.AudioFeedbackManager.SoundProfile.valueOf(loaded.soundProfile)
            } catch (e: Exception) {
                com.example.util.AudioFeedbackManager.SoundProfile.SOFT_POP
            }
            com.example.util.SoundFeedbackManager.updateSettings(profile, loaded.soundVolume, loaded.isSoundEnabled)

            val scope = CoroutineScope(Dispatchers.IO)
            persistenceScope = scope
            scope.launch {
                settings.collect { currentSettings ->
                    settingsRepo?.saveSettings(currentSettings)
                }
            }
        }
    }

    fun resetToFactoryDefaults() {
        settingsRepo?.clearAllSettings()
        val defaults = IslandSettings()
        settings.value = defaults
        pillOffsetYDp.value = defaults.pillOffsetYDp
        settingsRepo?.saveSettings(defaults)
    }

    val currentLux = MutableStateFlow(35f)
    var lastValidArtworkColor: Color = Color(0xFF00E5FF)

    private var luxDebounceJob: kotlinx.coroutines.Job? = null
    private var pendingTargetIsDark: Boolean? = null

    fun updateAmbientLux(lux: Float) {
        currentLux.value = lux
        val currentSettings = settings.value
        if (currentSettings.autoLightSensorTheme) {
            val darkThresh = currentSettings.darkThresholdLx
            val lightThresh = currentSettings.lightThresholdLx

            if (lux > lightThresh && currentSettings.isDarkMode) {
                if (pendingTargetIsDark != false) {
                    pendingTargetIsDark = false
                    luxDebounceJob?.cancel()
                    luxDebounceJob = kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
                        delay(1500L)
                        settings.update { it.copy(isDarkMode = false) }
                        pendingTargetIsDark = null
                    }
                }
            } else if (lux < darkThresh && !currentSettings.isDarkMode) {
                if (pendingTargetIsDark != true) {
                    pendingTargetIsDark = true
                    luxDebounceJob?.cancel()
                    luxDebounceJob = kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
                        delay(1500L)
                        settings.update { it.copy(isDarkMode = true) }
                        pendingTargetIsDark = null
                    }
                }
            } else if (lux >= darkThresh && lux <= lightThresh) {
                luxDebounceJob?.cancel()
                luxDebounceJob = null
                pendingTargetIsDark = null
            }
        } else {
            luxDebounceJob?.cancel()
            luxDebounceJob = null
            pendingTargetIsDark = null
        }
        settings.update { it.copy(ambientLuxValue = lux) }
    }

    val activeActivity = MutableStateFlow<LiveActivityState>(LiveActivityState.Idle)
    val secondaryActivity = MutableStateFlow<LiveActivityState?>(null)
    val activeActivitiesList = MutableStateFlow<List<LiveActivityState>>(emptyList())
    val activeNotification = MutableStateFlow<IncomingNotification?>(null)
    val expansionMode = MutableStateFlow(IslandExpansionMode.COMPACT)
    val settings = MutableStateFlow(IslandSettings())
    val pillOffsetYDp = MutableStateFlow(settings.value.pillOffsetYDp)
    val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()
    var notificationRemainingSeconds = 0
    var lastInteractionTimestamp = System.currentTimeMillis()
    var mediaController: android.media.session.MediaController? = null
    var dismissedVideoToken: String? = null
    var dismissedPackageName: String? = null

    fun calculateCurrentPosition(
        basePosition: Long,
        lastPositionUpdateTime: Long,
        playbackSpeed: Float,
        isPlaying: Boolean,
        durationMs: Long
    ): Long {
        val validDuration = durationMs.coerceAtLeast(1000L)
        if (!isPlaying) return basePosition.coerceIn(0L, validDuration)

        val lastUpdate = if (lastPositionUpdateTime > 0L) lastPositionUpdateTime else android.os.SystemClock.elapsedRealtime()
        val now = android.os.SystemClock.elapsedRealtime()
        val timeDelta = (now - lastUpdate).coerceAtLeast(0L)

        return (basePosition + (timeDelta * playbackSpeed).toLong()).coerceIn(0L, validDuration)
    }

    fun updatePlaybackPosition(
        positionMs: Long,
        lastPositionUpdateTime: Long,
        playbackSpeed: Float,
        isPlaying: Boolean
    ) {
        val current = mediaActivityState.value ?: return
        val updated = current.copy(
            positionMs = positionMs,
            lastPositionUpdateTime = if (lastPositionUpdateTime > 0L) lastPositionUpdateTime else android.os.SystemClock.elapsedRealtime(),
            playbackSpeed = playbackSpeed,
            isPlaying = isPlaying
        )
        mediaActivityState.value = updated
        if (activeActivity.value is LiveActivityState.Music) {
            activeActivity.value = updated
        }
        _currentPositionMs.value = calculateCurrentPosition(
            basePosition = positionMs,
            lastPositionUpdateTime = updated.lastPositionUpdateTime,
            playbackSpeed = playbackSpeed,
            isPlaying = isPlaying,
            durationMs = current.song.durationMs
        )
    }

    fun onPlaybackStateReceived(state: android.media.session.PlaybackState?) {
        if (state == null) return
        val isPlaying = state.state == android.media.session.PlaybackState.STATE_PLAYING ||
                state.state == android.media.session.PlaybackState.STATE_FAST_FORWARDING ||
                state.state == android.media.session.PlaybackState.STATE_REWINDING
        val basePos = state.position
        val lastUpdate = if (state.lastPositionUpdateTime > 0L) state.lastPositionUpdateTime else android.os.SystemClock.elapsedRealtime()
        val speed = if (state.playbackSpeed > 0f) state.playbackSpeed else 1.0f

        updatePlaybackPosition(
            positionMs = basePos,
            lastPositionUpdateTime = lastUpdate,
            playbackSpeed = speed,
            isPlaying = isPlaying
        )
    }

    fun snoozeCurrentVideo() {
        val current = (activeActivity.value as? LiveActivityState.Music) ?: mediaActivityState.value
        val token = current?.song?.title ?: "current_video"
        dismissedVideoToken = token
        dismissedPackageName = current?.song?.album
        expansionMode.value = IslandExpansionMode.HIDDEN
    }

    fun clearDismissedVideo() {
        dismissedVideoToken = null
        dismissedPackageName = null
    }

    fun dismissNonPersistentOnShake(): Boolean {
        var clearedAny = false

        if (activeNotification.value != null || notificationActivityState.value != null) {
            activeNotification.value = null
            notificationActivityState.value = null
            notificationRemainingSeconds = 0
            clearedAny = true
        }

        if (timerActivityState.value != null) {
            timerActivityState.value = null
            clearedAny = true
        }

        if (alarmActivityState.value != null) {
            alarmActivityState.value = null
            clearedAny = true
        }

        if (clearedAny) {
            reevaluatePriority()
        }

        return clearedAny
    }

    fun resetAll() {
        callActivityState.value = null
        alarmActivityState.value = null
        timerActivityState.value = null
        fitnessActivityState.value = null
        mediaActivityState.value = null
        workoutActivityState.value = null
        navigationActivityState.value = null
        deliveryActivityState.value = null
        notificationActivityState.value = null
        activeNotification.value = null
        clearDismissedVideo()
        expansionMode.value = IslandExpansionMode.COMPACT
        reevaluatePriority()
    }

    // Track active states per category for priority routing
    val callActivityState = MutableStateFlow<LiveActivityState.IncomingCall?>(null)
    val alarmActivityState = MutableStateFlow<LiveActivityState.Alarm?>(null)
    val timerActivityState = MutableStateFlow<LiveActivityState.Timer?>(null)
    val fitnessActivityState = MutableStateFlow<LiveActivityState.Fitness?>(null)
    val mediaActivityState = MutableStateFlow<LiveActivityState.Music?>(null)
    val workoutActivityState = MutableStateFlow<LiveActivityState.Workout?>(null)
    val navigationActivityState = MutableStateFlow<LiveActivityState.Navigation?>(null)
    val deliveryActivityState = MutableStateFlow<LiveActivityState.Delivery?>(null)
    val notificationActivityState = MutableStateFlow<LiveActivityState.Notification?>(null)

    val isDeviceLocked = MutableStateFlow(false)
    val isSettling = MutableStateFlow(false)

    // Hardware Cutout Detection (DisplayCutout)
    val hardwareCutoutTopDp = MutableStateFlow<Float?>(null)
    val hardwareCutoutCenterYDp = MutableStateFlow<Float?>(null)
    val isHardwareCutoutDetected = MutableStateFlow(false)

    fun updateHardwareCutout(topDp: Float, centerYDp: Float) {
        hardwareCutoutTopDp.value = topDp
        hardwareCutoutCenterYDp.value = centerYDp
        isHardwareCutoutDetected.value = true
        // If user hasn't explicitly customized pillOffsetYDp away from 0dp, align with cutout
        if (settings.value.pillOffsetYDp == 0f) {
            pillOffsetYDp.value = topDp.coerceAtLeast(0f)
        }
    }

    // Spotify & Window State Tracking
    val isSpotifyInForeground = MutableStateFlow(false)
    val isAccessibilityServiceConnected = MutableStateFlow(false)
    val isCenterToNotchFlightActive = MutableStateFlow(false)
    val centerToNotchTrigger = MutableStateFlow(0L)
    val flightTrigger: StateFlow<Long> = centerToNotchTrigger.asStateFlow()

    fun setAccessibilityServiceConnected(connected: Boolean) {
        isAccessibilityServiceConnected.value = connected
    }

    fun onSpotifyEnteredForeground() {
        isSpotifyInForeground.value = true
        // Retract or hide pill so it doesn't collide with Spotify full-screen player
        val music = mediaActivityState.value
        if (music != null) {
            expansionMode.value = IslandExpansionMode.HIDDEN
        }
    }

    fun onSpotifyExitedToBackground() {
        isSpotifyInForeground.value = false
        val music = mediaActivityState.value
        if (music != null) {
            expansionMode.value = IslandExpansionMode.COMPACT
            triggerCenterToNotchMorph()
        }
    }

    val isAnimatingFlight: StateFlow<Boolean> = isCenterToNotchFlightActive.asStateFlow()

    fun startCenterToTopFlight() {
        centerToNotchTrigger.value = System.currentTimeMillis()
        expansionMode.value = IslandExpansionMode.COMPACT
        triggerDockSettlement()
    }

    fun triggerCenterToNotchMorph() {
        startCenterToTopFlight()
    }

    fun triggerDockSettlement() {
        // Only trigger settling if island is hidden or already settling, preventing bouncing when already docked
        if (expansionMode.value == IslandExpansionMode.HIDDEN || isSettling.value) {
            islandScope.launch {
                isSettling.value = true
                delay(750L)
                isSettling.value = false
            }
        }
    }

    fun updateDeviceLockedStatus(isLocked: Boolean) {
        isDeviceLocked.value = isLocked
    }

    fun isDeviceUnlocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return keyguardManager?.isKeyguardLocked == false
    }

    fun removeActivityByType(type: com.example.model.LiveActivityType) {
        when (type) {
            com.example.model.LiveActivityType.CALL -> callActivityState.value = null
            com.example.model.LiveActivityType.FITNESS -> fitnessActivityState.value = null
            com.example.model.LiveActivityType.ALARM -> alarmActivityState.value = null
            com.example.model.LiveActivityType.TIMER -> timerActivityState.value = null
            com.example.model.LiveActivityType.MUSIC -> mediaActivityState.value = null
            com.example.model.LiveActivityType.WORKOUT -> workoutActivityState.value = null
            com.example.model.LiveActivityType.NAVIGATION -> navigationActivityState.value = null
            com.example.model.LiveActivityType.DELIVERY -> deliveryActivityState.value = null
            com.example.model.LiveActivityType.NOTIFICATION -> notificationActivityState.value = null
        }
        reevaluatePriority()
    }

    fun updateCallActivity(call: LiveActivityState.IncomingCall?) {
        callActivityState.value = call
        reevaluatePriority()
    }

    fun updateAlarmActivity(alarm: LiveActivityState.Alarm?) {
        alarmActivityState.value = alarm
        reevaluatePriority()
    }

    fun updateTimerActivity(timer: LiveActivityState.Timer?) {
        timerActivityState.value = timer
        reevaluatePriority()
    }

    fun updateFitnessActivity(fitness: LiveActivityState.Fitness?) {
        fitnessActivityState.value = fitness
        reevaluatePriority()
    }

    fun updateNavigationActivity(nav: LiveActivityState.Navigation?) {
        navigationActivityState.value = nav
        reevaluatePriority()
    }

    fun updateDeliveryActivity(delivery: LiveActivityState.Delivery?) {
        deliveryActivityState.value = delivery
        reevaluatePriority()
    }

    private val islandScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.SupervisorJob())

    fun updateMusicActivityFromState(mediaUiState: com.example.model.MediaUiState) {
        islandScope.launch {
            val current = mediaActivityState.value ?: (activeActivity.value as? LiveActivityState.Music)
            val extractedArtworkColor = PaletteExtractor.extractAlbumArtworkColor(mediaUiState.artwork)
            val primaryColor = if (extractedArtworkColor != null) {
                GlobalIslandState.lastValidArtworkColor = extractedArtworkColor
                extractedArtworkColor
            } else {
                GlobalIslandState.lastValidArtworkColor
            }

            val trackId = mediaUiState.trackId.ifEmpty {
                "${mediaUiState.title}_${mediaUiState.artist}_${mediaUiState.packageName}".hashCode().toString()
            }

            val song = Song(
                id = trackId,
                title = mediaUiState.title,
                artist = mediaUiState.artist,
                album = mediaUiState.packageName ?: "Unknown",
                durationMs = mediaUiState.durationMs,
                primaryColor = primaryColor,
                coverBitmap = mediaUiState.artwork,
                secondaryColor = primaryColor.copy(alpha = 0.7f)
            )

            val isFavorite = current?.isFavorite ?: true
            val isShuffle = current?.isShuffle ?: false
            val isRepeat = current?.isRepeat ?: false

            val musicState = LiveActivityState.Music(
                song = song,
                isPlaying = mediaUiState.isPlaying,
                positionMs = mediaUiState.positionMs,
                isFavorite = isFavorite,
                isShuffle = isShuffle,
                isRepeat = isRepeat,
                playbackSpeed = mediaUiState.playbackSpeed,
                lastPositionUpdateTime = mediaUiState.lastPositionUpdateTime,
                originatingPackageName = mediaUiState.packageName
            )

            mediaActivityState.value = musicState
            if (mediaUiState.controller != null) {
                mediaController = mediaUiState.controller
            }

            if (dismissedVideoToken != null) {
                if (!mediaUiState.isPlaying) {
                    clearDismissedVideo()
                } else if (mediaUiState.title != dismissedVideoToken || (mediaUiState.packageName != null && dismissedPackageName != null && mediaUiState.packageName != dismissedPackageName)) {
                    clearDismissedVideo()
                    if (expansionMode.value == IslandExpansionMode.HIDDEN) {
                        expansionMode.value = IslandExpansionMode.COMPACT
                    }
                }
            }

            reevaluatePriority()
        }
    }

    fun updateMusicActivity(
        title: String,
        artist: String,
        bitmap: android.graphics.Bitmap?,
        isPlaying: Boolean,
        packageName: String?,
        durationMs: Long,
        positionMs: Long,
        playbackSpeed: Float = 1.0f,
        lastPositionUpdateTime: Long = android.os.SystemClock.elapsedRealtime(),
        controller: android.media.session.MediaController? = null
    ) {
        val trackId = "${title}_${artist}_${packageName}".hashCode().toString()
        val mediaState = com.example.model.MediaUiState(
            trackId = trackId,
            title = title,
            artist = artist,
            artwork = bitmap,
            isPlaying = isPlaying,
            packageName = packageName,
            durationMs = durationMs,
            positionMs = positionMs,
            playbackSpeed = playbackSpeed,
            lastPositionUpdateTime = lastPositionUpdateTime,
            controller = controller
        )
        updateMusicActivityFromState(mediaState)
    }

    fun triggerNotification(
        appName: String,
        sender: String,
        title: String,
        body: String,
        iconName: String = "Message",
        brandColorLong: Long = 0xFF3B82F6,
        isSensitive: Boolean = false,
        actions: List<NotificationActionItem> = emptyList(),
        rawText: String = body,
        maskedText: String = body,
        originatingPackageName: String? = null
    ) {
        if (!settings.value.enableNotificationMirroring) return

        val (isOtp, sanitizedBody) = OtpSanitizer.sanitize(appName, title, body)
        val finalIsSensitive = isSensitive || isOtp

        val effectiveMasked = if (maskedText != body && maskedText.isNotBlank()) {
            maskedText
        } else if (finalIsSensitive) {
            sanitizedBody
        } else {
            body
        }

        val notification = IncomingNotification(
            appName = appName,
            sender = sender,
            title = title,
            rawBody = body,
            sanitizedBody = effectiveMasked,
            isOtpSensitive = finalIsSensitive,
            iconName = iconName,
            brandColorLong = brandColorLong
        )

        activeNotification.value = notification
        notificationRemainingSeconds = settings.value.notificationDurationSeconds

        val notifState = LiveActivityState.Notification(
            appName = appName,
            sender = sender,
            title = title,
            body = body,
            iconName = iconName,
            brandColorLong = brandColorLong,
            isSensitive = finalIsSensitive,
            originalBody = body,
            rawText = rawText,
            maskedText = effectiveMasked,
            actions = actions,
            originatingPackageName = originatingPackageName
        )
        notificationActivityState.value = notifState

        if (expansionMode.value == IslandExpansionMode.HIDDEN) {
            expansionMode.value = IslandExpansionMode.COMPACT
        }
        reevaluatePriority()
    }

    fun reevaluatePriority() {
        val call = callActivityState.value
        val alarm = alarmActivityState.value
        val timer = timerActivityState.value
        val fitness = fitnessActivityState.value
        val media = mediaActivityState.value
        val workout = workoutActivityState.value
        val nav = navigationActivityState.value
        val delivery = deliveryActivityState.value
        val notif = notificationActivityState.value

        val list = mutableListOf<LiveActivityState>()
        if (call != null) list.add(call)
        if (alarm != null) list.add(alarm)
        if (timer != null && (timer.isRunning || timer.remainingSeconds > 0)) list.add(timer)
        if (fitness != null) list.add(fitness)
        if (media != null) list.add(media)
        if (workout != null) list.add(workout)
        if (nav != null) list.add(nav)
        if (delivery != null) list.add(delivery)
        if (notif != null) list.add(notif)

        activeActivitiesList.value = list

        // Priority Hierarchy: Phone Calls > Alarms > Active Timers > Strava Fitness > Active Media > General Notifications
        val primary: LiveActivityState = when {
            call != null -> call
            alarm != null -> alarm
            timer != null && timer.isRunning -> timer
            fitness != null && !fitness.isPaused -> fitness
            media != null && media.isPlaying -> media
            workout != null && !workout.isPaused -> workout
            nav != null -> nav
            delivery != null -> delivery
            notif != null -> notif
            fitness != null -> fitness
            timer != null -> timer
            media != null -> media
            workout != null -> workout
            else -> LiveActivityState.Idle
        }

        val secondary: LiveActivityState? = when {
            primary is LiveActivityState.IncomingCall -> {
                alarm ?: (if (timer != null && timer.isRunning) timer else if (media != null && media.isPlaying) media else null)
            }
            primary is LiveActivityState.Alarm -> {
                if (timer != null && timer.isRunning) timer
                else if (fitness != null) fitness
                else if (media != null && media.isPlaying) media
                else null
            }
            primary is LiveActivityState.Timer -> {
                if (fitness != null) fitness
                else if (media != null && media.isPlaying) media
                else null
            }
            primary is LiveActivityState.Fitness -> {
                if (media != null && media.isPlaying) media
                else null
            }
            else -> null
        }

        val previousPrimary = activeActivity.value
        if (previousPrimary::class != primary::class ||
            (primary is LiveActivityState.Music && previousPrimary is LiveActivityState.Music && primary.song.title != previousPrimary.song.title)
        ) {
            SoundFeedbackManager.playActivitySwitchSound(primary)
        }

        activeActivity.value = primary
        secondaryActivity.value = secondary

        // Wake up condition c: A higher-priority activity occurs (Incoming Call, Alarm, Timer, Strava workout)
        val isHigherPriorityActive = call != null || alarm != null || (timer != null && timer.isRunning) || (fitness != null && !fitness.isPaused)
        if (isHigherPriorityActive) {
            if (expansionMode.value == IslandExpansionMode.HIDDEN) {
                expansionMode.value = IslandExpansionMode.COMPACT
            }
        } else if (primary is LiveActivityState.Music && dismissedVideoToken != null) {
            // If primary is the snoozed video, keep island hidden; if video title changed, clear snooze
            if (primary.song.title != dismissedVideoToken && dismissedVideoToken != "current_video") {
                clearDismissedVideo()
                if (expansionMode.value == IslandExpansionMode.HIDDEN) {
                    expansionMode.value = IslandExpansionMode.COMPACT
                }
            }
        } else if (primary !is LiveActivityState.Idle && expansionMode.value == IslandExpansionMode.HIDDEN) {
            expansionMode.value = IslandExpansionMode.COMPACT
        }
    }
}

class DynamicIslandViewModel : ViewModel() {

    private var lastInteractionTimestamp: Long = System.currentTimeMillis()

    val activeActivity: StateFlow<LiveActivityState> = GlobalIslandState.activeActivity.asStateFlow()
    val secondaryActivity: StateFlow<LiveActivityState?> = GlobalIslandState.secondaryActivity.asStateFlow()
    val activeActivitiesList: StateFlow<List<LiveActivityState>> = GlobalIslandState.activeActivitiesList.asStateFlow()
    val activeNotification: StateFlow<IncomingNotification?> = GlobalIslandState.activeNotification.asStateFlow()
    val expansionMode: StateFlow<IslandExpansionMode> = GlobalIslandState.expansionMode.asStateFlow()
    val settings: StateFlow<IslandSettings> = GlobalIslandState.settings.asStateFlow()
    val settingsState: StateFlow<IslandSettings> = GlobalIslandState.settings.asStateFlow()

    private val _isPulseEnabled = MutableStateFlow(GlobalIslandState.settings.value.isPulseEnabled)
    val isPulseEnabled: StateFlow<Boolean> = _isPulseEnabled.asStateFlow()

    private val _isSpringEnabled = MutableStateFlow(GlobalIslandState.settings.value.isSpringEnabled)
    val isSpringEnabled: StateFlow<Boolean> = _isSpringEnabled.asStateFlow()

    val pillOffsetYDp: StateFlow<Float> = GlobalIslandState.pillOffsetYDp.asStateFlow()
    val currentPositionMs: StateFlow<Long> = GlobalIslandState.currentPositionMs

    fun onPlaybackStateReceived(state: android.media.session.PlaybackState?) {
        GlobalIslandState.onPlaybackStateReceived(state)
    }

    val isDarkMode: StateFlow<Boolean> = GlobalIslandState.settings
        .map { it.isDarkMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val mediaDisplayMode: StateFlow<MediaDisplayMode> = GlobalIslandState.settings
        .map { it.mediaDisplayMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MediaDisplayMode.BOTH)

    val resolvedAccentColor: StateFlow<Color> = combine(
        GlobalIslandState.activeActivity,
        GlobalIslandState.settings
    ) { activity, settings ->
        getActivityAccentColor(activity, settings.themeMode, settings.customAccentColorLong)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Color(0xFF6366F1))

    init {
        startInactivityAutoCollapseCheck()
        startRealtimePositionTicker()
    }

    private fun startRealtimePositionTicker() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            while (isActive) {
                val currentMedia = GlobalIslandState.mediaActivityState.value
                if (currentMedia != null) {
                    val livePos = GlobalIslandState.calculateCurrentPosition(
                        basePosition = currentMedia.positionMs,
                        lastPositionUpdateTime = currentMedia.lastPositionUpdateTime,
                        playbackSpeed = currentMedia.playbackSpeed,
                        isPlaying = currentMedia.isPlaying,
                        durationMs = currentMedia.song.durationMs
                    )
                    GlobalIslandState._currentPositionMs.value = livePos
                }
                delay(250L)
            }
        }
    }

    private fun startInactivityAutoCollapseCheck() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val settings = GlobalIslandState.settings.value
                val currentState = GlobalIslandState.activeActivity.value
                val mode = GlobalIslandState.expansionMode.value

                if (settings.enableAutoCollapse && mode == IslandExpansionMode.EXPANDED) {
                    val isMediaPlaying = (currentState as? LiveActivityState.Music)?.isPlaying == true
                    val isTimerRunning = (currentState as? LiveActivityState.Timer)?.isRunning == true
                    val isCallActive = (currentState as? LiveActivityState.IncomingCall)?.isAnswered == true
                    val isFitnessActive = (currentState as? LiveActivityState.Fitness)?.isPaused == false

                    if (!isMediaPlaying && !isTimerRunning && !isCallActive && !isFitnessActive) {
                        val elapsed = (System.currentTimeMillis() - GlobalIslandState.lastInteractionTimestamp) / 1000L
                        if (elapsed >= settings.autoCollapseDelaySeconds) {
                            GlobalIslandState.expansionMode.value = IslandExpansionMode.COMPACT
                        }
                    }
                }

                if (GlobalIslandState.activeNotification.value != null && GlobalIslandState.notificationRemainingSeconds > 0) {
                    GlobalIslandState.notificationRemainingSeconds--
                    if (GlobalIslandState.notificationRemainingSeconds <= 0) {
                        GlobalIslandState.activeNotification.value = null
                    }
                }
            }
        }
    }

    fun tickNotification() {
        if (GlobalIslandState.activeNotification.value != null && GlobalIslandState.notificationRemainingSeconds > 0) {
            GlobalIslandState.notificationRemainingSeconds--
            if (GlobalIslandState.notificationRemainingSeconds <= 0) {
                GlobalIslandState.activeNotification.value = null
            }
        }
    }

    // Expansion State
    fun toggleExpansion() {
        onInteraction()
        val current = GlobalIslandState.expansionMode.value
        if (current == IslandExpansionMode.COMPACT) {
            SoundFeedbackManager.playPillExpansionSound()
            GlobalIslandState.expansionMode.value = IslandExpansionMode.EXPANDED
        } else {
            SoundFeedbackManager.playPillCollapseSound()
            GlobalIslandState.expansionMode.value = IslandExpansionMode.COMPACT
        }
    }

    fun setExpansionMode(mode: IslandExpansionMode) {
        onInteraction()
        GlobalIslandState.expansionMode.value = mode
    }

    // Activity Switching
    fun switchToActivity(type: String) {
        onInteraction()
        when (type) {
            "IncomingCall" -> {
                val call = LiveActivityState.IncomingCall()
                GlobalIslandState.updateCallActivity(call)
            }
            "Alarm" -> {
                triggerAlarmDemo()
            }
            "Timer" -> {
                val timer = LiveActivityState.Timer()
                GlobalIslandState.updateTimerActivity(timer)
            }
            "Fitness" -> {
                triggerStravaFitnessDemo()
            }
            "Music" -> {
                val music = LiveActivityState.Music()
                GlobalIslandState.mediaActivityState.value = music
                GlobalIslandState.reevaluatePriority()
            }
            "Workout" -> {
                val workout = LiveActivityState.Workout()
                GlobalIslandState.workoutActivityState.value = workout
                GlobalIslandState.reevaluatePriority()
            }
            "Navigation" -> {
                val nav = LiveActivityState.Navigation()
                GlobalIslandState.navigationActivityState.value = nav
                GlobalIslandState.reevaluatePriority()
            }
            "Delivery" -> {
                val del = LiveActivityState.Delivery()
                GlobalIslandState.deliveryActivityState.value = del
                GlobalIslandState.reevaluatePriority()
            }
            else -> {
                val music = LiveActivityState.Music()
                GlobalIslandState.mediaActivityState.value = music
                GlobalIslandState.reevaluatePriority()
            }
        }
        GlobalIslandState.expansionMode.value = IslandExpansionMode.COMPACT
    }

    fun enableSplitMode(secondaryType: String) {
        onInteraction()
        val secondary: LiveActivityState = when (secondaryType) {
            "Timer" -> LiveActivityState.Timer()
            "Workout" -> LiveActivityState.Workout()
            "Navigation" -> LiveActivityState.Navigation()
            "Fitness" -> LiveActivityState.Fitness()
            else -> LiveActivityState.Timer()
        }
        GlobalIslandState.secondaryActivity.value = secondary
        GlobalIslandState.expansionMode.value = IslandExpansionMode.SPLIT
    }

    fun disableSplitMode() {
        onInteraction()
        GlobalIslandState.secondaryActivity.value = null
        GlobalIslandState.expansionMode.value = IslandExpansionMode.COMPACT
    }

    fun resumePlayback(controller: MediaController?, context: Context? = null) {
        com.example.service.MediaNotificationListenerService.resumePlayback(controller, context)
    }

    fun pausePlayback(controller: MediaController?, context: Context? = null) {
        com.example.service.MediaNotificationListenerService.pausePlayback(controller, context)
    }

    // Music Player Actions
    fun togglePlayPause(context: Context? = null) {
        onInteraction()
        SoundFeedbackManager.playMediaActionSound(SoundFeedbackManager.MediaAction.PLAY_PAUSE)
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Music) {
            val nextIsPlaying = !state.isPlaying
            val now = android.os.SystemClock.elapsedRealtime()
            val currentPos = GlobalIslandState.calculateCurrentPosition(
                basePosition = state.positionMs,
                lastPositionUpdateTime = state.lastPositionUpdateTime,
                playbackSpeed = state.playbackSpeed,
                isPlaying = state.isPlaying,
                durationMs = state.song.durationMs
            )

            GlobalIslandState.updatePlaybackPosition(
                positionMs = currentPos,
                lastPositionUpdateTime = now,
                playbackSpeed = state.playbackSpeed,
                isPlaying = nextIsPlaying
            )
            GlobalIslandState.reevaluatePriority()
        }

        if (context != null) {
            com.example.service.MediaPlaybackBridge(context).togglePlayPause()
        } else {
            com.example.service.MediaNotificationListenerService.togglePlayPauseGlobal(null)
        }
    }

    fun seekTo(positionMs: Long) {
        onInteraction()
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Music) {
            val now = android.os.SystemClock.elapsedRealtime()
            GlobalIslandState.updatePlaybackPosition(
                positionMs = positionMs,
                lastPositionUpdateTime = now,
                playbackSpeed = state.playbackSpeed,
                isPlaying = state.isPlaying
            )
            GlobalIslandState.mediaController?.transportControls?.seekTo(positionMs)
        }
    }

    fun skipNextSong(context: Context? = null) {
        onInteraction()
        SoundFeedbackManager.playMediaActionSound(SoundFeedbackManager.MediaAction.SKIP_NEXT)
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Music) {
            val updated = state.copy(
                positionMs = 0L,
                lastPositionUpdateTime = android.os.SystemClock.elapsedRealtime()
            )
            GlobalIslandState.mediaActivityState.value = updated
            GlobalIslandState.reevaluatePriority()
        }
        if (context != null) {
            com.example.service.MediaPlaybackBridge(context).skipNext()
        } else {
            val controller = GlobalIslandState.mediaController
            com.example.service.MediaNotificationListenerService.dispatchMediaAction(
                controller,
                com.example.service.MediaAction.NEXT,
                null
            )
        }
    }

    fun skipPreviousSong(context: Context? = null) {
        onInteraction()
        SoundFeedbackManager.playMediaActionSound(SoundFeedbackManager.MediaAction.SKIP_PREVIOUS)
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Music) {
            val updated = state.copy(
                positionMs = 0L,
                lastPositionUpdateTime = android.os.SystemClock.elapsedRealtime()
            )
            GlobalIslandState.mediaActivityState.value = updated
            GlobalIslandState.reevaluatePriority()
        }
        if (context != null) {
            com.example.service.MediaPlaybackBridge(context).skipPrevious()
        } else {
            val controller = GlobalIslandState.mediaController
            com.example.service.MediaNotificationListenerService.dispatchMediaAction(
                controller,
                com.example.service.MediaAction.PREVIOUS,
                null
            )
        }
    }

    fun toggleFavorite() {
        onInteraction()
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Music) {
            val updated = state.copy(isFavorite = !state.isFavorite)
            GlobalIslandState.mediaActivityState.value = updated
            GlobalIslandState.reevaluatePriority()
        }
    }

    fun toggleShuffle() {
        onInteraction()
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Music) {
            val updated = state.copy(isShuffle = !state.isShuffle)
            GlobalIslandState.mediaActivityState.value = updated
            GlobalIslandState.reevaluatePriority()
        }
    }

    fun toggleRepeat() {
        onInteraction()
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Music) {
            val updated = state.copy(isRepeat = !state.isRepeat)
            GlobalIslandState.mediaActivityState.value = updated
            GlobalIslandState.reevaluatePriority()
        }
    }

    // YouTube / Media Session Dismissal & Snooze
    fun snoozeCurrentVideo() {
        onInteraction()
        GlobalIslandState.snoozeCurrentVideo()
    }

    fun dismissVideoForSession(token: String? = null) {
        onInteraction()
        if (token != null) {
            GlobalIslandState.dismissedVideoToken = token
            GlobalIslandState.expansionMode.value = IslandExpansionMode.HIDDEN
        } else {
            GlobalIslandState.snoozeCurrentVideo()
        }
    }

    // Fitness Actions
    fun toggleFitnessPause() {
        onInteraction()
        val state = GlobalIslandState.fitnessActivityState.value ?: (GlobalIslandState.activeActivity.value as? LiveActivityState.Fitness)
        if (state != null) {
            val updated = if (!state.isPaused) {
                val currentSeconds = if (state.startTimeMs in 1..System.currentTimeMillis()) {
                    maxOf(0L, (System.currentTimeMillis() - state.startTimeMs) / 1000L)
                } else {
                    state.elapsedSeconds
                }
                val h = currentSeconds / 3600
                val m = (currentSeconds % 3600) / 60
                val s = currentSeconds % 60
                val formatted = if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
                state.copy(
                    isPaused = true,
                    elapsedSeconds = currentSeconds,
                    elapsedDurationMs = currentSeconds * 1000L,
                    staticDisplayTime = formatted
                )
            } else {
                val resumeStart = System.currentTimeMillis() - state.elapsedSeconds * 1000L
                state.copy(
                    isPaused = false,
                    startTimeMs = resumeStart,
                    elapsedDurationMs = state.elapsedSeconds * 1000L,
                    staticDisplayTime = null
                )
            }
            GlobalIslandState.updateFitnessActivity(updated)
        }
    }

    fun triggerStravaFitnessDemo(withHeartRate: Boolean = true, isPaused: Boolean = false) {
        onInteraction()
        val elapsed = 1420L
        val h = elapsed / 3600
        val m = (elapsed % 3600) / 60
        val s = elapsed % 60
        val formatted = if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
        GlobalIslandState.updateFitnessActivity(
            LiveActivityState.Fitness(
                appName = "Strava",
                activityName = "Morning Tempo Run",
                elapsedSeconds = elapsed,
                startTimeMs = System.currentTimeMillis() - elapsed * 1000L,
                distanceText = "5.24 km",
                paceText = "4'36\" /km",
                heartRate = 156,
                heartRateBpm = if (withHeartRate) 156 else null,
                isPaused = isPaused,
                elapsedDurationMs = elapsed * 1000L,
                staticDisplayTime = if (isPaused) formatted else null,
                distance = "5.24 km",
                pace = "4'36\" /km",
                brandColorLong = 0xFFFC4C02
            )
        )
    }

    fun triggerWearOsHealthDemo() {
        onInteraction()
        GlobalIslandState.updateFitnessActivity(
            LiveActivityState.Fitness(
                appName = "Wear OS",
                activityName = "Heart Rate & Cardio",
                elapsedSeconds = 840L,
                startTimeMs = System.currentTimeMillis() - 840L * 1000L,
                distanceText = "3.10 km",
                paceText = "5'12\" /km",
                heartRate = 162,
                heartRateBpm = 162,
                isPaused = false,
                brandColorLong = 0xFF4285F4
            )
        )
    }

    // Alarm Actions
    fun dismissAlarm() {
        onInteraction()
        val alarm = GlobalIslandState.alarmActivityState.value ?: (GlobalIslandState.activeActivity.value as? LiveActivityState.Alarm)
        alarm?.dismissIntent?.let {
            try { it.send() } catch (e: Exception) { e.printStackTrace() }
        }
        alarm?.onDismiss?.invoke()
        GlobalIslandState.updateAlarmActivity(null)
    }

    fun snoozeAlarm() {
        onInteraction()
        val alarm = GlobalIslandState.alarmActivityState.value ?: (GlobalIslandState.activeActivity.value as? LiveActivityState.Alarm)
        alarm?.snoozeIntent?.let {
            try { it.send() } catch (e: Exception) { e.printStackTrace() }
        }
        alarm?.onSnooze?.invoke()
        GlobalIslandState.updateAlarmActivity(null)
    }

    fun triggerAlarmDemo() {
        onInteraction()
        GlobalIslandState.updateAlarmActivity(
            LiveActivityState.Alarm(
                title = "Morning Wake Up",
                timeText = "07:00 AM",
                isRinging = true
            )
        )
    }

    // Workout Actions
    fun toggleWorkoutPause() {
        onInteraction()
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Workout) {
            val updated = state.copy(isPaused = !state.isPaused)
            GlobalIslandState.workoutActivityState.value = updated
            GlobalIslandState.reevaluatePriority()
        }
    }

    // Timer Actions
    fun toggleTimerRun() {
        onInteraction()
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Timer) {
            val updated = state.copy(isRunning = !state.isRunning)
            GlobalIslandState.updateTimerActivity(updated)
        }
    }

    fun resetTimer() {
        onInteraction()
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Timer) {
            val updated = state.copy(remainingSeconds = state.totalSeconds, isRunning = false)
            GlobalIslandState.updateTimerActivity(updated)
        }
    }

    fun addTimerMinute() {
        onInteraction()
        val state = GlobalIslandState.activeActivity.value
        if (state is LiveActivityState.Timer) {
            val updated = state.copy(
                remainingSeconds = state.remainingSeconds + 60L,
                totalSeconds = state.totalSeconds + 60L
            )
            GlobalIslandState.updateTimerActivity(updated)
        }
    }

    fun removeActivityByType(type: com.example.model.LiveActivityType) {
        onInteraction()
        GlobalIslandState.removeActivityByType(type)
    }

    // Call Actions
    fun answerCall(context: Context? = null) {
        onInteraction()
        val state = GlobalIslandState.callActivityState.value ?: (GlobalIslandState.activeActivity.value as? LiveActivityState.IncomingCall)
        if (state != null) {
            if (context != null && state.answerIntent != null) {
                com.example.util.PendingIntentExecutor.executeCallAction(state.answerIntent, context)
            } else {
                state.answerIntent?.let {
                    try { it.send() } catch (e: Exception) { e.printStackTrace() }
                }
            }
            state.onAnswer?.invoke()
            val updated = state.copy(
                isAnswered = true,
                subtext = "Active Call",
                callStartTimeMs = if (state.callStartTimeMs > 0L) state.callStartTimeMs else System.currentTimeMillis()
            )
            GlobalIslandState.updateCallActivity(updated)
        }
    }

    fun endCall(context: Context? = null) {
        onInteraction()
        val state = GlobalIslandState.callActivityState.value ?: (GlobalIslandState.activeActivity.value as? LiveActivityState.IncomingCall)
        if (state != null) {
            val intentToDispatch = if (state.isAnswered) (state.rejectIntent ?: state.answerIntent) else state.rejectIntent
            if (context != null && intentToDispatch != null) {
                com.example.util.PendingIntentExecutor.executeCallAction(intentToDispatch, context)
            } else {
                intentToDispatch?.let {
                    try { it.send() } catch (e: Exception) { e.printStackTrace() }
                }
            }
            state.onReject?.invoke()
            state.onEndCall?.invoke()
        }
        com.example.service.MediaNotificationListenerService.endCall(null, context)
        GlobalIslandState.updateCallActivity(null)
    }

    // Settings adjustments
    fun setPillWidth(widthDp: Float) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(pillWidthDp = widthDp) }
    }

    fun setPillHeight(heightDp: Float) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(pillHeightDp = heightDp) }
    }

    fun updatePillOffsetY(offset: Float) {
        onInteraction()
        GlobalIslandState.pillOffsetYDp.value = offset
        GlobalIslandState.settings.update { it.copy(pillOffsetYDp = offset, yOffsetDp = offset) }
    }

    fun updateYOffset(offset: Float) {
        updatePillOffsetY(offset)
    }

    fun updatePillScale(scale: Float) {
        GlobalIslandState.settings.update { it.copy(pillScaleWidth = scale) }
    }

    fun setBreathingGlowEnabled(enabled: Boolean) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(isBreathingGlowEnabled = enabled, enableGlow = enabled) }
    }

    fun setEdgeSheenEnabled(enabled: Boolean) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(isEdgeSheenEnabled = enabled, specularSheenEnabled = enabled) }
    }

    fun setBeatPulseEnabled(enabled: Boolean) {
        onInteraction()
        _isPulseEnabled.value = enabled
        GlobalIslandState.settings.update { it.copy(isBeatPulseEnabled = enabled, isPulseEnabled = enabled) }
    }

    fun setAuraIntensity(intensity: Float) {
        onInteraction()
        val clamped = intensity.coerceIn(0.15f, 0.80f)
        GlobalIslandState.settings.update { it.copy(auraIntensity = clamped, glowIntensity = clamped) }
    }

    fun toggleGlow(enabled: Boolean) {
        setBreathingGlowEnabled(enabled)
    }

    fun togglePulseEffect(enabled: Boolean) {
        setBeatPulseEnabled(enabled)
    }

    fun toggleSpringAnimation(enabled: Boolean) {
        _isSpringEnabled.value = enabled
        GlobalIslandState.settings.update { it.copy(isSpringEnabled = enabled) }
    }

    fun toggleHideSensitiveContent(enabled: Boolean) {
        GlobalIslandState.settings.update { it.copy(hideSensitiveContent = enabled) }
    }

    fun setDarkThresholdLx(threshold: Float) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(darkThresholdLx = threshold) }
    }

    fun setLightThresholdLx(threshold: Float) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(lightThresholdLx = threshold) }
    }

    fun setSystemOverlayActive(active: Boolean) {
        GlobalIslandState.settings.update { it.copy(isSystemOverlayActive = active) }
    }

    fun onInteraction() {
        lastInteractionTimestamp = System.currentTimeMillis()
        GlobalIslandState.lastInteractionTimestamp = lastInteractionTimestamp
        if (GlobalIslandState.expansionMode.value == IslandExpansionMode.HIDDEN) {
            GlobalIslandState.expansionMode.value = IslandExpansionMode.COMPACT
        }
    }

    fun onUserInteracted() {
        onInteraction()
    }

    fun resetInactivityTimer() {
        lastInteractionTimestamp = System.currentTimeMillis()
        GlobalIslandState.lastInteractionTimestamp = lastInteractionTimestamp
    }

    fun toggleAutoCollapse(enabled: Boolean) {
        GlobalIslandState.settings.update { it.copy(enableAutoCollapse = enabled) }
    }

    fun updateAutoCollapseDelay(delaySeconds: Int) {
        GlobalIslandState.settings.update { it.copy(autoCollapseDelaySeconds = delaySeconds) }
    }

    fun setShowCompactSongTitle(show: Boolean) {
        GlobalIslandState.settings.value = GlobalIslandState.settings.value.copy(showCompactSongTitle = show)
    }

    fun setMediaDisplayMode(mode: MediaDisplayMode) {
        GlobalIslandState.settings.update { it.copy(mediaDisplayMode = mode) }
    }

    fun setVisualizerMode(mode: VisualizerMode) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(visualizerMode = mode) }
    }

    fun setThemeMode(mode: IslandThemeMode) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(themeMode = mode) }
    }

    fun setCustomAccentColor(colorLong: Long) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(customAccentColorLong = colorLong) }
    }

    fun setGlowIntensity(intensity: Float) {
        setAuraIntensity(intensity)
    }

    fun setAuraLightingEffect(effect: AuraLightingEffect) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(auraLightingEffect = effect) }
    }

    fun setSpecularSheenEnabled(enabled: Boolean) {
        setEdgeSheenEnabled(enabled)
    }

    fun setDarkMode(enabled: Boolean) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(isDarkMode = enabled) }
    }

    fun updateAmbientLux(lux: Float) {
        GlobalIslandState.updateAmbientLux(lux)
    }

    fun setAutoLightSensorTheme(enabled: Boolean) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(autoLightSensorTheme = enabled) }
        if (enabled) {
            GlobalIslandState.updateAmbientLux(GlobalIslandState.currentLux.value)
        }
    }

    fun setShowBatteryIndicator(enabled: Boolean) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(showBatteryIndicator = enabled) }
    }

    fun setCharging(isCharging: Boolean) {
        onInteraction()
        val previousCharging = GlobalIslandState.settings.value.isCharging
        GlobalIslandState.settings.update { it.copy(isCharging = isCharging) }
        if (isCharging && !previousCharging) {
            SoundFeedbackManager.playChargingConnectedSound()
            triggerChargingAlert()
        }
    }

    fun setBatteryLevel(level: Int) {
        val clamped = level.coerceIn(1, 100)
        GlobalIslandState.settings.update { it.copy(batteryLevel = clamped) }
    }

    fun triggerChargingAlert() {
        val currentLevel = GlobalIslandState.settings.value.batteryLevel
        triggerNotification(
            appName = "Power Connected",
            sender = "Battery Manager",
            title = "Charging • $currentLevel%",
            body = "Fast Charging connected • $currentLevel%",
            iconName = "BatteryChargingFull",
            brandColorLong = 0xFF34C759,
            isSensitive = false,
            rawText = "Charging • $currentLevel%",
            maskedText = "Charging • $currentLevel%"
        )
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000L)
            val currentNotif = GlobalIslandState.activeNotification.value
            if (currentNotif?.appName == "Power Connected") {
                dismissNotification()
            }
        }
    }

    fun setLightSensorThreshold(threshold: Float) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(lightSensorLuxThreshold = threshold) }
        GlobalIslandState.updateAmbientLux(GlobalIslandState.currentLux.value)
    }

    fun toggleDynamicTheme(enabled: Boolean) {
        GlobalIslandState.settings.update { it.copy(enableDynamicTheme = enabled) }
    }

    fun toggleHyperSmoothMode(enabled: Boolean) {
        GlobalIslandState.settings.update { it.copy(enableHyperSmoothMode = enabled) }
    }

    fun updateDarkThemeSettings(opacity: Float, blurDp: Float) {
        GlobalIslandState.settings.update { it.copy(darkBgOpacity = opacity, darkBlurLevelDp = blurDp) }
    }

    fun updateLightThemeSettings(opacity: Float, blurDp: Float) {
        GlobalIslandState.settings.update { it.copy(lightBgOpacity = opacity, lightBlurLevelDp = blurDp) }
    }

    fun toggleNotificationMirroring(enabled: Boolean) {
        GlobalIslandState.settings.update { it.copy(enableNotificationMirroring = enabled) }
        if (!enabled) {
            dismissNotification()
        }
    }

    fun triggerNotification(
        appName: String,
        sender: String,
        title: String,
        body: String,
        iconName: String = "Message",
        brandColorLong: Long = 0xFF3B82F6,
        isSensitive: Boolean = false,
        actions: List<NotificationActionItem> = emptyList(),
        rawText: String = body,
        maskedText: String = body,
        originatingPackageName: String? = null
    ) {
        GlobalIslandState.triggerNotification(
            appName = appName,
            sender = sender,
            title = title,
            body = body,
            iconName = iconName,
            brandColorLong = brandColorLong,
            isSensitive = isSensitive,
            actions = actions,
            rawText = rawText,
            maskedText = maskedText,
            originatingPackageName = originatingPackageName
        )
    }

    fun dismissNotification() {
        onInteraction()
        GlobalIslandState.activeNotification.value = null
        GlobalIslandState.notificationRemainingSeconds = 0
    }

    fun toggleShakeToClear(enabled: Boolean) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(enableShakeToClear = enabled) }
    }

    fun setShakeThresholdG(threshold: Float) {
        onInteraction()
        GlobalIslandState.settings.update { it.copy(shakeThresholdG = threshold) }
    }

    fun dismissNonPersistentOnShake(): Boolean {
        onInteraction()
        val cleared = GlobalIslandState.dismissNonPersistentOnShake()
        if (cleared) {
            SoundFeedbackManager.playMediaActionSound(SoundFeedbackManager.MediaAction.PLAY_PAUSE)
        }
        return cleared
    }

    fun initPersistence(context: Context) {
        GlobalIslandState.initPersistence(context)
        _isPulseEnabled.value = GlobalIslandState.settings.value.isPulseEnabled
        _isSpringEnabled.value = GlobalIslandState.settings.value.isSpringEnabled
    }

    fun resetToFactoryDefaults() {
        onInteraction()
        GlobalIslandState.resetToFactoryDefaults()
        _isPulseEnabled.value = GlobalIslandState.settings.value.isPulseEnabled
        _isSpringEnabled.value = GlobalIslandState.settings.value.isSpringEnabled
        SoundFeedbackManager.playMediaActionSound(SoundFeedbackManager.MediaAction.PLAY_PAUSE)
    }

    val isDeviceLocked: StateFlow<Boolean> = GlobalIslandState.isDeviceLocked.asStateFlow()

    fun updateDeviceLockedStatus(isLocked: Boolean) {
        GlobalIslandState.updateDeviceLockedStatus(isLocked)
    }

    fun isDeviceUnlocked(context: Context): Boolean {
        return GlobalIslandState.isDeviceUnlocked(context)
    }

    fun setSoundProfile(profile: com.example.util.AudioFeedbackManager.SoundProfile) {
        GlobalIslandState.settings.update { it.copy(soundProfile = profile.name) }
        com.example.util.SoundFeedbackManager.updateSettings(
            profile,
            GlobalIslandState.settings.value.soundVolume,
            GlobalIslandState.settings.value.isSoundEnabled
        )
        com.example.util.SoundFeedbackManager.playFeedback(profile)
    }

    fun setSoundVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        GlobalIslandState.settings.update { it.copy(soundVolume = clamped) }
        val currentProfile = try {
            com.example.util.AudioFeedbackManager.SoundProfile.valueOf(GlobalIslandState.settings.value.soundProfile)
        } catch (e: Exception) {
            com.example.util.AudioFeedbackManager.SoundProfile.SOFT_POP
        }
        com.example.util.SoundFeedbackManager.updateSettings(
            currentProfile,
            clamped,
            GlobalIslandState.settings.value.isSoundEnabled
        )
    }

    fun setSoundEnabled(enabled: Boolean) {
        GlobalIslandState.settings.update { it.copy(isSoundEnabled = enabled) }
        val currentProfile = try {
            com.example.util.AudioFeedbackManager.SoundProfile.valueOf(GlobalIslandState.settings.value.soundProfile)
        } catch (e: Exception) {
            com.example.util.AudioFeedbackManager.SoundProfile.SOFT_POP
        }
        com.example.util.SoundFeedbackManager.updateSettings(
            currentProfile,
            GlobalIslandState.settings.value.soundVolume,
            enabled
        )
        if (enabled) {
            com.example.util.SoundFeedbackManager.playFeedback(currentProfile)
        }
    }

    val isSpotifyInForeground: StateFlow<Boolean> = GlobalIslandState.isSpotifyInForeground.asStateFlow()
    val isAccessibilityServiceConnected: StateFlow<Boolean> = GlobalIslandState.isAccessibilityServiceConnected.asStateFlow()
    val isCenterToNotchFlightActive: StateFlow<Boolean> = GlobalIslandState.isCenterToNotchFlightActive.asStateFlow()
    val centerToNotchTrigger: StateFlow<Long> = GlobalIslandState.centerToNotchTrigger.asStateFlow()
    val flightTrigger: StateFlow<Long> = GlobalIslandState.flightTrigger
    val isHardwareCutoutDetected: StateFlow<Boolean> = GlobalIslandState.isHardwareCutoutDetected.asStateFlow()
    val hardwareCutoutCenterYDp: StateFlow<Float?> = GlobalIslandState.hardwareCutoutCenterYDp.asStateFlow()

    fun onSpotifyEnteredForeground() {
        GlobalIslandState.onSpotifyEnteredForeground()
    }

    fun onSpotifyExitedToBackground() {
        GlobalIslandState.onSpotifyExitedToBackground()
    }

    val isAnimatingFlight: StateFlow<Boolean> = GlobalIslandState.isAnimatingFlight

    fun startCenterToTopFlight() {
        GlobalIslandState.startCenterToTopFlight()
    }

    fun setFlightActive(active: Boolean) {
        GlobalIslandState.isCenterToNotchFlightActive.value = active
    }

    fun simulateSpotifyExitDemo() {
        if (GlobalIslandState.mediaActivityState.value == null) {
            GlobalIslandState.updateMusicActivity(
                title = "Starboy",
                artist = "The Weeknd",
                bitmap = null,
                isPlaying = true,
                packageName = "com.spotify.music",
                durationMs = 230000L,
                positionMs = 45000L
            )
        }
        GlobalIslandState.onSpotifyExitedToBackground()
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
        val enabledServices = am?.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val fromManager = enabledServices?.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
                    it.resolveInfo.serviceInfo.name.contains("DynamicIslandAccessibilityService")
        } == true

        if (fromManager) return true

        val expectedComponentName = android.content.ComponentName(context, com.example.service.DynamicIslandAccessibilityService::class.java)
        val enabledServicesSetting = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = android.content.ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }

    companion object {
        fun resolveCurrentAuraColor(
            activeActivity: LiveActivityState?,
            defaultAccent: Color = Color(0xFF6366F1)
        ): Color {
            return when (activeActivity) {
                is LiveActivityState.Music -> {
                    activeActivity.albumArtworkAccentColor ?: GlobalIslandState.lastValidArtworkColor
                }
                is LiveActivityState.Notification -> {
                    activeActivity.appBrandColor
                }
                is LiveActivityState.Delivery -> {
                    val pkg = activeActivity.providerPackage ?: activeActivity.originatingPackageName ?: ""
                    when {
                        pkg == "com.application.zomato" || pkg.contains("zomato", ignoreCase = true) -> Color(0xFFE23744)
                        pkg == "in.swiggy.android" || pkg.contains("swiggy", ignoreCase = true) -> Color(0xFFFC8019)
                        pkg == "com.ubercab" || pkg == "com.ubercab.eats" || pkg.contains("ubercab", ignoreCase = true) || pkg.contains("eats", ignoreCase = true) -> Color(0xFF06C167)
                        else -> Color(0xFFFF8000)
                    }
                }
                is LiveActivityState.Navigation -> Color(0xFF4285F4)
                is LiveActivityState.Fitness -> Color(0xFFFC4C02)
                is LiveActivityState.Workout -> Color(0xFFFC4C02)
                is LiveActivityState.IncomingCall -> Color(0xFF34C759)
                is LiveActivityState.Alarm -> Color(0xFFFF9500)
                is LiveActivityState.Timer -> Color(0xFFFF9500)
                else -> defaultAccent
            }
        }

        fun getActivityAccentColor(
            activity: LiveActivityState,
            themeMode: IslandThemeMode = IslandThemeMode.ADAPTIVE_ARTWORK,
            customAccentColorLong: Long = 0xFF6366F1
        ): Color {
            val defaultThemeAccent = Color(0xFF6366F1)
            return when (themeMode) {
                IslandThemeMode.OBSIDIAN_MONOCHROME -> Color(0xFFE0E0E0)
                IslandThemeMode.CUSTOM_ACCENT -> Color(customAccentColorLong)
                IslandThemeMode.ADAPTIVE_ARTWORK -> resolveCurrentAuraColor(activity, defaultThemeAccent)
            }
        }
    }
}

@Composable
fun resolveCurrentAuraColor(
    activeActivity: LiveActivityState?,
    defaultAccent: Color = Color(0xFF6366F1)
): Color {
    return DynamicIslandViewModel.resolveCurrentAuraColor(activeActivity, defaultAccent)
}
