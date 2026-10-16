package com.example.service

import android.app.KeyguardManager
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.model.IslandExpansionMode
import com.example.model.LiveActivityState
import com.example.model.NotificationActionItem
import com.example.model.OtpSanitizer
import com.example.viewmodel.GlobalIslandState
import androidx.compose.ui.graphics.toArgb
import com.example.model.MediaUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class MediaAction { PLAY, PAUSE, NEXT, PREVIOUS }

class MediaNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var mediaSessionManager: MediaSessionManager? = null
    private var currentController: MediaController? = null

    private val mediaUpdateFlow = MutableSharedFlow<MediaUiState>(
        replay = 1,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        @OptIn(FlowPreview::class)
        mediaUpdateFlow
            .debounce(200L)
            .distinctUntilChanged { old: MediaUiState, new: MediaUiState ->
                old.trackId == new.trackId &&
                old.isPlaying == new.isPlaying &&
                old.title == new.title &&
                old.artist == new.artist &&
                old.durationMs == new.durationMs
            }
            .onEach { batchedState: MediaUiState ->
                GlobalIslandState.updateMusicActivityFromState(batchedState)
            }
            .launchIn(serviceScope)
    }

    private val sessionListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            val active = controllers?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: controllers?.firstOrNull()
            bindToController(active)
        }

    private val mediaCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            val playbackState = state?.state

            // Direct non-debounced position & seek update
            if (state != null) {
                GlobalIslandState.onPlaybackStateReceived(state)
            }

            if (playbackState == PlaybackState.STATE_PLAYING || playbackState == PlaybackState.STATE_PAUSED) {
                syncMediaState(currentController, isMetadataChanged = false)
                if (playbackState == PlaybackState.STATE_PLAYING) {
                    if (GlobalIslandState.expansionMode.value == IslandExpansionMode.HIDDEN) {
                        GlobalIslandState.triggerDockSettlement()
                        GlobalIslandState.expansionMode.value = com.example.model.IslandExpansionMode.COMPACT
                    }
                }
            } else if (playbackState == PlaybackState.STATE_STOPPED || playbackState == PlaybackState.STATE_NONE) {
                GlobalIslandState.clearDismissedVideo()
                syncMediaState(currentController, isMetadataChanged = false)
            } else {
                syncMediaState(currentController, isMetadataChanged = false)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            syncMediaState(currentController, isMetadataChanged = true)
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            currentController = null
            GlobalIslandState.clearDismissedVideo()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("MediaListenerService", "NotificationListenerService onListenerConnected")
        setupMediaSessionManager()
    }

    private val screenUnlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT, Intent.ACTION_SCREEN_ON -> {
                    val keyguardManager = context?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    GlobalIslandState.updateDeviceLockedStatus(isLocked = keyguardManager?.isKeyguardLocked == true)

                    // Re-bind listener on Doze wake / screen unlock
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            requestRebind(ComponentName(this@MediaNotificationListenerService, MediaNotificationListenerService::class.java))
                        }
                    } catch (e: Exception) {
                        Log.w("MediaListenerService", "requestRebind error on wake", e)
                    }
                    setupMediaSessionManager()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    GlobalIslandState.updateDeviceLockedStatus(isLocked = true)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        setupMediaSessionManager()

        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        GlobalIslandState.updateDeviceLockedStatus(isLocked = km?.isKeyguardLocked == true)

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT).apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenUnlockReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenUnlockReceiver, filter)
            }
        } catch (e: Exception) {
            try {
                registerReceiver(screenUnlockReceiver, filter)
            } catch (ignored: Exception) {}
        }
    }

    override fun onDestroy() {
        cleanupMediaSessionManager()
        try {
            unregisterReceiver(screenUnlockReceiver)
        } catch (ignored: Exception) {}
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: Bundle()
        val packageName = sbn.packageName ?: ""
        val category = notification.category ?: ""

        // 0. Re-bind Media Session / Media Controller from notification or MediaSessionManager
        val hasMediaAction = notification.actions?.any { action ->
            val act = action.title?.toString()?.lowercase() ?: ""
            act.contains("pause") || act.contains("play") || act.contains("skip") || act.contains("next") || act.contains("previous")
        } == true

        val mediaToken = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, android.media.session.MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? android.media.session.MediaSession.Token
        }

        if (mediaToken != null) {
            try {
                val controller = MediaController(this, mediaToken)
                bindToController(controller)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (category == Notification.CATEGORY_TRANSPORT || hasMediaAction) {
            try {
                val component = ComponentName(this, MediaNotificationListenerService::class.java)
                val controllers = mediaSessionManager?.getActiveSessions(component)
                val matching = controllers?.firstOrNull { it.packageName == packageName }
                    ?: controllers?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                    ?: controllers?.firstOrNull()
                if (matching != null) {
                    bindToController(matching)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                val component = ComponentName(this, MediaNotificationListenerService::class.java)
                val controllers = mediaSessionManager?.getActiveSessions(component)
                val playing = controllers?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                if (playing != null && (currentController == null || currentController?.playbackState?.state != PlaybackState.STATE_PLAYING)) {
                    bindToController(playing)
                }
            } catch (ignored: Exception) {}
        }

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
            ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString()
            ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

        val appDisplayName = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }

        // 1. Phone Call Category
        val isCallCategory = category == Notification.CATEGORY_CALL
        val isPhonePackage = packageName.contains("telecom", ignoreCase = true) ||
                packageName.contains("phone", ignoreCase = true) ||
                packageName.contains("dialer", ignoreCase = true) ||
                packageName.contains("incallui", ignoreCase = true)

        if (isCallCategory || isPhonePackage) {
            val contactName = if (title.isNotEmpty()) title else "Caller"
            var answerIntent: android.app.PendingIntent? = null
            var rejectIntent: android.app.PendingIntent? = null
            var hasAnswerAction = false
            var hasEndOrHangupAction = false

            notification.actions?.forEach { action ->
                val actionTitle = action.title?.toString()?.lowercase() ?: ""
                if (actionTitle.contains("answer") || actionTitle.contains("accept")) {
                    answerIntent = action.actionIntent
                    hasAnswerAction = true
                } else if (actionTitle.contains("decline") || actionTitle.contains("reject") || actionTitle.contains("dismiss")) {
                    rejectIntent = action.actionIntent
                } else if (actionTitle.contains("end") || actionTitle.contains("hang") || actionTitle.contains("disconnect")) {
                    rejectIntent = action.actionIntent
                    hasEndOrHangupAction = true
                }
            }

            val isCallActive = (sbn.isOngoing && (!hasAnswerAction || hasEndOrHangupAction)) ||
                    text.contains("active call", ignoreCase = true) ||
                    text.contains("call in progress", ignoreCase = true) ||
                    text.contains("ongoing call", ignoreCase = true)

            val callStartTime = if (notification.`when` in 1..System.currentTimeMillis()) {
                notification.`when`
            } else {
                System.currentTimeMillis()
            }

            val subtext = if (isCallActive) "Active Call" else if (text.isNotEmpty()) text else "Incoming Phone Call"

            GlobalIslandState.updateCallActivity(
                LiveActivityState.IncomingCall(
                    contactName = contactName,
                    subtext = subtext,
                    isAnswered = isCallActive,
                    callStartTimeMs = callStartTime,
                    answerIntent = answerIntent,
                    rejectIntent = rejectIntent,
                    originatingPackageName = packageName.ifBlank { "com.google.android.dialer" }
                )
            )
            return
        }

        // 2. Real Fitness Tracking with Strava & Wear OS / Smartwatch Companions
        val isStrava = packageName == "com.strava" || packageName.contains("strava", ignoreCase = true)
        val isSmartwatchOrHealthApp = isStrava ||
                packageName.contains("shealth", ignoreCase = true) ||
                packageName.contains("samsung.android.app.health", ignoreCase = true) ||
                packageName.contains("fitness", ignoreCase = true) ||
                packageName.contains("fitbit", ignoreCase = true) ||
                packageName.contains("garmin", ignoreCase = true) ||
                packageName.contains("polar", ignoreCase = true) ||
                packageName.contains("coros", ignoreCase = true) ||
                packageName.contains("wahoofitness", ignoreCase = true) ||
                packageName.contains("wearable", ignoreCase = true) ||
                packageName.contains("healthdata", ignoreCase = true) ||
                (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && category == Notification.CATEGORY_WORKOUT)

        val fullText = "$title $text $subText"
        val hasResumeActionEarly = notification.actions?.any { action ->
            action.title?.toString()?.contains("Resume", ignoreCase = true) == true
        } ?: false
        val hasPauseActionEarly = notification.actions?.any { action ->
            action.title?.toString()?.contains("Pause", ignoreCase = true) == true
        } ?: false
        val isPausedStrava = fullText.contains("Paused", ignoreCase = true) || title.contains("Paused", ignoreCase = true) || hasResumeActionEarly

        if (isStrava && !sbn.isOngoing && !isPausedStrava && !hasPauseActionEarly) {
            GlobalIslandState.updateFitnessActivity(null)
            return
        }
        val hasBpmIndicator = Regex("""(\d{2,3})\s*(?:bpm|BPM)""").containsMatchIn(fullText) ||
                Regex("""(?:HR|Heart Rate|Heartrate|Pulse)[:\s]+(\d{2,3})""", RegexOption.IGNORE_CASE).containsMatchIn(fullText)
        val hasWorkoutIndicator = fullText.contains("workout", ignoreCase = true) ||
                fullText.contains("running", ignoreCase = true) ||
                fullText.contains("cycling", ignoreCase = true) ||
                fullText.contains("exercise", ignoreCase = true) ||
                fullText.contains("distance", ignoreCase = true) ||
                fullText.contains("pace", ignoreCase = true)

        if (isSmartwatchOrHealthApp || ((hasBpmIndicator || hasWorkoutIndicator) && (category == Notification.CATEGORY_PROGRESS || category == Notification.CATEGORY_STATUS))) {
            if (!sbn.isOngoing && !fullText.contains("Paused", ignoreCase = true) && !hasResumeActionEarly && !hasPauseActionEarly) {
                // If Strava workout is stopped or not ongoing and not paused, remove from active activities
                GlobalIslandState.updateFitnessActivity(null)
                return
            }

            // Parse distance (e.g., "5.24 km", "3.1 mi")
            val distanceRegex = Regex("""(\d+(?:\.\d+)?)\s*(km|mi|miles)\b""", RegexOption.IGNORE_CASE)
            val distanceMatch = distanceRegex.find(fullText)
            val distanceText = distanceMatch?.let {
                val num = it.groupValues[1]
                val unit = it.groupValues[2].lowercase()
                "$num $unit"
            } ?: "5.20 km"

            // Parse pace (e.g., "4'52\" /km", "4'52\"", "7:30 /mi")
            val paceRegex = Regex("""(\d{1,2}'\d{2}\"?(?:\s*(?:\/km|\/mi))?|\d{1,2}:\d{2}\s*(?:\/km|\/mi))""", RegexOption.IGNORE_CASE)
            val paceMatch = paceRegex.find(fullText)
            val paceText = paceMatch?.value?.trim() ?: "4'52\" /km"

            // Parse live heart rate from Wear OS / companion health notifications or bundle extras
            val hrRegex = Regex("""(\d{2,3})\s*(?:bpm|BPM)""")
            val hrMatch = hrRegex.find(fullText)
            val hrFromRegex = hrMatch?.groupValues?.get(1)?.toIntOrNull()
                ?: Regex("""(?:HR|Heart Rate|Heartrate|Pulse)[:\s]+(\d{2,3})""", RegexOption.IGNORE_CASE).find(fullText)?.groupValues?.get(1)?.toIntOrNull()

            val extraHr = extras.getInt("heart_rate", 0).let { if (it > 0) it else null }
                ?: extras.getInt("heartRate", 0).let { if (it > 0) it else null }
                ?: extras.getInt("bpm", 0).let { if (it > 0) it else null }

            val heartRateBpm = hrFromRegex ?: extraHr
            val heartRate = heartRateBpm ?: 148

            // Determine paused state via Action buttons ("Resume"), text, subtext, and title
            val hasResumeAction = notification.actions?.any { action ->
                action.title?.toString()?.contains("Resume", ignoreCase = true) == true
            } ?: false

            val isPausedState = hasResumeAction ||
                    title.contains("Paused", ignoreCase = true) ||
                    text.contains("Paused", ignoreCase = true) ||
                    subText.contains("Paused", ignoreCase = true) ||
                    fullText.contains("Paused", ignoreCase = true)

            // Extract static duration string directly from notification (e.g. "24:15" or "1:12:30")
            val durationRegex = Regex("""(?<![:\d])(\d{1,2}:\d{2}(?::\d{2})?)(?!\s*[\/']|\s*(?:km|mi|min))""")
            val durationMatch = durationRegex.find(fullText)
            val staticDisplayTime = durationMatch?.value

            val parsedDurationSeconds: Long? = staticDisplayTime?.let { timeStr ->
                val parts = timeStr.split(":").mapNotNull { it.toLongOrNull() }
                when (parts.size) {
                    2 -> parts[0] * 60L + parts[1]
                    3 -> parts[0] * 3600L + parts[1] * 60L + parts[2]
                    else -> null
                }
            }

            val elapsedSeconds = if (parsedDurationSeconds != null) {
                parsedDurationSeconds
            } else if (notification.`when` in 1..System.currentTimeMillis() && (System.currentTimeMillis() - notification.`when`) > 10_000L) {
                maxOf(0L, (System.currentTimeMillis() - notification.`when`) / 1000L)
            } else {
                1420L
            }

            val elapsedDurationMs = elapsedSeconds * 1000L
            val startTimeMs = if (isPausedState) {
                System.currentTimeMillis() - elapsedDurationMs
            } else if (parsedDurationSeconds != null) {
                System.currentTimeMillis() - elapsedDurationMs
            } else if (notification.`when` in 1..System.currentTimeMillis() && (System.currentTimeMillis() - notification.`when`) > 10_000L) {
                notification.`when`
            } else {
                System.currentTimeMillis() - elapsedDurationMs
            }

            val resolvedStaticDisplay = if (isPausedState) {
                staticDisplayTime ?: run {
                    val h = elapsedSeconds / 3600
                    val m = (elapsedSeconds % 3600) / 60
                    val s = elapsedSeconds % 60
                    if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
                }
            } else {
                null
            }

            val brandColorLong = when {
                isStrava -> 0xFFFC4C02
                packageName.contains("shealth", ignoreCase = true) || packageName.contains("health", ignoreCase = true) -> 0xFF2DB253
                packageName.contains("garmin", ignoreCase = true) -> 0xFF007CC3
                packageName.contains("fitbit", ignoreCase = true) -> 0xFF00B0B9
                packageName.contains("google", ignoreCase = true) -> 0xFF4285F4
                else -> 0xFFFC4C02
            }

            val appName = when {
                isStrava -> "Strava"
                packageName.contains("shealth", ignoreCase = true) -> "Samsung Health"
                packageName.contains("garmin", ignoreCase = true) -> "Garmin Connect"
                packageName.contains("fitbit", ignoreCase = true) -> "Fitbit"
                packageName.contains("fitness", ignoreCase = true) -> "Google Fit"
                packageName.contains("wearable", ignoreCase = true) -> "Wear OS"
                else -> appDisplayName.ifEmpty { "Fitness" }
            }

            GlobalIslandState.updateFitnessActivity(
                LiveActivityState.Fitness(
                    appName = appName,
                    activityName = if (title.isNotBlank() && !title.equals(appName, ignoreCase = true)) title else "Active Workout",
                    elapsedSeconds = elapsedSeconds,
                    startTimeMs = startTimeMs,
                    distanceText = distanceText,
                    paceText = paceText,
                    heartRate = heartRate,
                    heartRateBpm = heartRateBpm,
                    isPaused = isPausedState,
                    elapsedDurationMs = elapsedDurationMs,
                    staticDisplayTime = resolvedStaticDisplay,
                    distance = distanceText,
                    pace = paceText,
                    brandColorLong = brandColorLong,
                    originatingPackageName = packageName.ifBlank { "com.strava" }
                )
            )
            return
        }

        // 3. Alarm Clock Notifications & Ringing State Detection
        val clockPackages = setOf(
            "com.google.android.deskclock",
            "com.google.android.desktopclock",
            "com.sec.android.app.clockpackage",
            "com.android.deskclock"
        )
        val isClockApp = clockPackages.contains(packageName) || packageName.contains("clock", ignoreCase = true)
        val isAlarmCategory = category == Notification.CATEGORY_ALARM

        val hasAlarmKeyword = title.contains("alarm", ignoreCase = true) ||
                text.contains("alarm", ignoreCase = true) ||
                subText.contains("alarm", ignoreCase = true)

        var alarmDismissIntent: android.app.PendingIntent? = null
        var alarmSnoozeIntent: android.app.PendingIntent? = null

        notification.actions?.forEach { action ->
            val actTitle = action.title?.toString()?.lowercase() ?: ""
            if (actTitle.contains("dismiss") || actTitle.contains("stop") || actTitle.contains("off") || actTitle.contains("cancel")) {
                alarmDismissIntent = action.actionIntent
            } else if (actTitle.contains("snooze")) {
                alarmSnoozeIntent = action.actionIntent
            }
        }

        if (isAlarmCategory || (isClockApp && (hasAlarmKeyword || alarmDismissIntent != null || alarmSnoozeIntent != null))) {
            val isRinging = sbn.isOngoing || notification.actions != null
            val timeMatch = Regex("""\d{1,2}:\d{2}\s*(?:AM|PM)?""", RegexOption.IGNORE_CASE).find("$title $text")
            val timeText = timeMatch?.value ?: if (title.contains(":")) title else "07:00 AM"

            GlobalIslandState.updateAlarmActivity(
                LiveActivityState.Alarm(
                    title = if (title.isNotBlank()) title else "Alarm",
                    timeText = timeText,
                    isRinging = isRinging,
                    dismissIntent = alarmDismissIntent,
                    snoozeIntent = alarmSnoozeIntent,
                    originatingPackageName = packageName.ifBlank { "com.google.android.deskclock" }
                )
            )
            return
        }

        // 4. Timer / Countdown Notifications
        val isTimerCategory = category == Notification.CATEGORY_PROGRESS || category == Notification.CATEGORY_STATUS
        val hasTimerKeyword = title.contains("timer", ignoreCase = true) ||
                text.contains("timer", ignoreCase = true) ||
                title.contains("stopwatch", ignoreCase = true) ||
                text.contains("stopwatch", ignoreCase = true)

        if (isClockApp || (isTimerCategory && hasTimerKeyword)) {
            val remainingSec = parseRemainingSecondsFromText(text) ?: parseRemainingSecondsFromText(title)
            if (remainingSec != null && remainingSec > 0) {
                GlobalIslandState.updateTimerActivity(
                    LiveActivityState.Timer(
                        title = if (title.isNotEmpty()) title else "Timer",
                        remainingSeconds = remainingSec,
                        totalSeconds = remainingSec,
                        targetEndTimeMs = System.currentTimeMillis() + remainingSec * 1000L,
                        isRunning = true,
                        originatingPackageName = packageName.ifBlank { "com.google.android.deskclock" }
                    )
                )
                return
            }
        }

        // 5. Live Navigation (Google Maps, Waze, CATEGORY_NAVIGATION)
        val isNavApp = packageName == "com.google.android.apps.maps" ||
                packageName == "com.waze" ||
                category == Notification.CATEGORY_NAVIGATION ||
                packageName.contains("maps", ignoreCase = true) ||
                packageName.contains("navigation", ignoreCase = true)

        if (isNavApp) {
            val direction = when {
                fullText.contains("left", ignoreCase = true) -> com.example.model.NavigationDirection.TURN_LEFT
                fullText.contains("right", ignoreCase = true) -> com.example.model.NavigationDirection.TURN_RIGHT
                fullText.contains("u-turn", ignoreCase = true) -> com.example.model.NavigationDirection.U_TURN
                fullText.contains("roundabout", ignoreCase = true) -> com.example.model.NavigationDirection.ROUNDABOUT
                else -> com.example.model.NavigationDirection.STRAIGHT
            }

            val distanceRegex = Regex("""(\d+(?:\.\d+)?\s*(?:m|km|ft|mi))""", RegexOption.IGNORE_CASE)
            val distMatch = distanceRegex.find(fullText)
            val distanceStr = distMatch?.value ?: "200m"

            val etaRegex = Regex("""(\d{1,2})\s*(?:min|mins|minute|minutes)""", RegexOption.IGNORE_CASE)
            val etaMatch = etaRegex.find(fullText)
            val etaMins = etaMatch?.groupValues?.get(1)?.toIntOrNull() ?: 14

            GlobalIslandState.updateNavigationActivity(
                LiveActivityState.Navigation(
                    destination = if (title.isNotBlank()) title else "Navigation Active",
                    currentInstruction = if (text.isNotBlank()) text else "Follow route on map",
                    distanceText = distanceStr,
                    iconType = direction,
                    etaMinutes = etaMins,
                    originatingPackageName = packageName.ifBlank { "com.google.android.apps.maps" }
                )
            )
            return
        }

        // 6. Food Delivery & Rideshare Progress Tracking
        val isDeliveryApp = packageName == "com.application.zomato" ||
                packageName == "in.swiggy.android" ||
                packageName.contains("ubercab", ignoreCase = true) ||
                packageName.contains("eats", ignoreCase = true) ||
                packageName.contains("doordash", ignoreCase = true) ||
                packageName.contains("grubhub", ignoreCase = true) ||
                packageName.contains("instacart", ignoreCase = true) ||
                packageName.contains("postmates", ignoreCase = true) ||
                packageName.contains("deliveroo", ignoreCase = true)

        if (isDeliveryApp) {
            val progCurrent = extras.getInt(Notification.EXTRA_PROGRESS, 0)
            val progMax = extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
            val calculatedProgress = if (progMax > 0) (progCurrent.toFloat() / progMax.toFloat()).coerceIn(0.1f, 1.0f) else 0.75f

            GlobalIslandState.updateDeliveryActivity(
                LiveActivityState.Delivery(
                    restaurantName = if (title.isNotBlank()) title else appDisplayName,
                    statusText = if (text.isNotBlank()) text else "Order progress updating",
                    progressPercent = calculatedProgress,
                    driverName = "Delivery Partner",
                    orderItemCount = 1,
                    providerPackage = packageName,
                    originatingPackageName = packageName
                )
            )
            return
        }

        // 7. Expanded Notification Drawer & Privacy Filtering with OTP & Sensitive Content Masking
        if (title.isNotEmpty() || text.isNotEmpty()) {
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            val isLocked = km?.isKeyguardLocked == true || GlobalIslandState.isDeviceLocked.value

            val isSecret = notification.visibility == Notification.VISIBILITY_SECRET
            val isSensitiveApp = isBankingOrCredentialPackage(packageName)

            val otpRegex = Regex("""(?i)\b(?:otp|code|verification|passcode|secret|pin)\b.*?\b(\d{4,8})\b""")
            val standaloneCodeRegex = Regex("""\b\d{4,8}\b""")

            val fullContent = "$title $text $subText"
            val isOtp = otpRegex.containsMatchIn(fullContent) || OtpSanitizer.isHighConfidenceOtp(appDisplayName, title, text)
            val isSensitive = isSecret || isSensitiveApp || isOtp

            val maskedText = when {
                isSensitiveApp && isLocked -> "Sensitive Information • ••••••"
                isSecret && isLocked -> "••••••••••••"
                isOtp -> text.replace(standaloneCodeRegex, "••••••")
                else -> text
            }

            val validActions = sbn.notification.actions?.filter { action ->
                !action.title.isNullOrBlank() && action.actionIntent != null
            } ?: emptyList()

            val actionItems = validActions.map { action ->
                NotificationActionItem(title = action.title.toString().trim(), intent = action.actionIntent)
            }

            serviceScope.launch(Dispatchers.Default) {
                val brandColorLong = if (isSensitive) {
                    0xFFE11D48
                } else {
                    val extractedBrandColor = com.example.util.PaletteExtractor.extractAppBrandColor(
                        context = this@MediaNotificationListenerService,
                        packageName = packageName,
                        notificationColorInt = notification.color
                    )
                    extractedBrandColor.toArgb().toLong() and 0xFFFFFFFFL
                }

                GlobalIslandState.triggerNotification(
                    appName = if (appDisplayName.isNotEmpty()) appDisplayName else "System",
                    sender = if (title.isNotEmpty()) title else appDisplayName,
                    title = title,
                    body = text,
                    iconName = if (packageName.contains("whatsapp", ignoreCase = true)) "WhatsApp" else "Message",
                    brandColorLong = brandColorLong,
                    isSensitive = isSensitive,
                    actions = actionItems,
                    rawText = text,
                    maskedText = maskedText,
                    originatingPackageName = packageName
                )
            }
        }
    }

    private fun parseRemainingSecondsFromText(text: String): Long? {
        val match = Regex("""(\d{1,2}):(\d{2})(?::(\d{2}))?""").find(text) ?: return null
        val parts = match.groupValues
        return try {
            if (parts[3].isNotEmpty()) {
                val h = parts[1].toLong()
                val m = parts[2].toLong()
                val s = parts[3].toLong()
                h * 3600 + m * 60 + s
            } else {
                val m = parts[1].toLong()
                val s = parts[2].toLong()
                m * 60 + s
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName ?: ""
        val category = sbn.notification?.category ?: ""

        val isCallCategory = category == Notification.CATEGORY_CALL
        val isPhonePackage = packageName.contains("telecom", ignoreCase = true) ||
                packageName.contains("phone", ignoreCase = true) ||
                packageName.contains("dialer", ignoreCase = true) ||
                packageName.contains("incallui", ignoreCase = true)

        if (isCallCategory || isPhonePackage) {
            GlobalIslandState.updateCallActivity(null)
            return
        }

        val isFitnessApp = packageName == "com.strava" ||
                packageName.contains("strava", ignoreCase = true) ||
                packageName.contains("shealth", ignoreCase = true) ||
                packageName.contains("fitness", ignoreCase = true) ||
                packageName.contains("garmin", ignoreCase = true) ||
                packageName.contains("fitbit", ignoreCase = true) ||
                packageName.contains("wearable", ignoreCase = true)

        if (isFitnessApp) {
            GlobalIslandState.updateFitnessActivity(null)
            return
        }

        val clockPackages = setOf(
            "com.google.android.deskclock",
            "com.google.android.desktopclock",
            "com.sec.android.app.clockpackage",
            "com.android.deskclock"
        )
        val isClockApp = clockPackages.contains(packageName) || packageName.contains("clock", ignoreCase = true)
        val isAlarmCategory = category == Notification.CATEGORY_ALARM

        if (isAlarmCategory) {
            GlobalIslandState.updateAlarmActivity(null)
            return
        }

        val isNavApp = packageName == "com.google.android.apps.maps" ||
                packageName == "com.waze" ||
                category == Notification.CATEGORY_NAVIGATION ||
                packageName.contains("maps", ignoreCase = true)

        if (isNavApp) {
            GlobalIslandState.updateNavigationActivity(null)
            return
        }

        val isDeliveryApp = packageName == "com.application.zomato" ||
                packageName == "in.swiggy.android" ||
                packageName.contains("ubercab", ignoreCase = true) ||
                packageName.contains("doordash", ignoreCase = true) ||
                packageName.contains("grubhub", ignoreCase = true)

        if (isDeliveryApp) {
            GlobalIslandState.updateDeliveryActivity(null)
            return
        }

        if (isClockApp) {
            GlobalIslandState.updateTimerActivity(null)
            GlobalIslandState.updateAlarmActivity(null)
            return
        }
    }

    private fun setupMediaSessionManager() {
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        val component = ComponentName(this, MediaNotificationListenerService::class.java)
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
        } catch (ignored: Exception) {}
        try {
            mediaSessionManager?.addOnActiveSessionsChangedListener(sessionListener, component)
            val controllers = mediaSessionManager?.getActiveSessions(component)
            val active = controllers?.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: controllers?.firstOrNull()
            bindToController(active)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun cleanupMediaSessionManager() {
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
            currentController?.unregisterCallback(mediaCallback)
            currentController = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bindToController(controller: MediaController?) {
        if (currentController == controller && controller != null) {
            syncMediaState(currentController)
            return
        }
        try {
            currentController?.unregisterCallback(mediaCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        currentController = controller
        try {
            currentController?.registerCallback(mediaCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        syncMediaState(currentController)
    }

    private fun syncMediaState(controller: MediaController?, isMetadataChanged: Boolean = false) {
        val ctrl = controller ?: currentController ?: return
        try {
            val metadata = try { ctrl.metadata } catch (e: Exception) { null }
            val state = try { ctrl.playbackState } catch (e: Exception) { null }

            val isPlaying = state?.state == PlaybackState.STATE_PLAYING ||
                    state?.state == PlaybackState.STATE_FAST_FORWARDING ||
                    state?.state == PlaybackState.STATE_REWINDING

            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                ?: metadata?.description?.title?.toString()
                ?: "Unknown Track"

            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: metadata?.description?.subtitle?.toString()
                ?: ctrl.packageName?.substringAfterLast('.')
                ?: "Unknown Artist"

            val rawBitmap = try {
                metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    ?: metadata?.description?.iconBitmap
            } catch (e: Throwable) {
                null
            }

            val safeBitmap = if (rawBitmap != null && !rawBitmap.isRecycled) {
                try {
                    rawBitmap.copy(Bitmap.Config.ARGB_8888, false)
                } catch (e: Throwable) {
                    null
                }
            } else {
                null
            }

            val durationMs = try {
                val d = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
                if (d > 0L) d else 180000L
            } catch (e: Exception) {
                180000L
            }

            val currentMusic = GlobalIslandState.mediaActivityState.value
            val isNewTrack = isMetadataChanged || (currentMusic != null && currentMusic.song.title != title)

            val positionMs = if (isNewTrack) {
                0L
            } else {
                try {
                    state?.position ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }

            val playbackSpeed = try {
                val speed = state?.playbackSpeed ?: 1.0f
                if (speed > 0f) speed else 1.0f
            } catch (e: Exception) {
                1.0f
            }

            val lastPositionUpdateTime = try {
                val time = state?.lastPositionUpdateTime ?: 0L
                if (time > 0L) time else SystemClock.elapsedRealtime()
            } catch (e: Exception) {
                SystemClock.elapsedRealtime()
            }

            val trackId = "${title}_${artist}_${ctrl.packageName}".hashCode().toString()
            val mediaUiState = com.example.model.MediaUiState(
                trackId = trackId,
                title = title,
                artist = artist,
                artwork = safeBitmap,
                isPlaying = isPlaying,
                packageName = ctrl.packageName,
                durationMs = durationMs,
                positionMs = positionMs,
                playbackSpeed = playbackSpeed,
                lastPositionUpdateTime = lastPositionUpdateTime,
                controller = ctrl
            )

            mediaUpdateFlow.tryEmit(mediaUiState)

            if (isPlaying && GlobalIslandState.expansionMode.value == IslandExpansionMode.HIDDEN) {
                GlobalIslandState.triggerDockSettlement()
                GlobalIslandState.expansionMode.value = IslandExpansionMode.COMPACT
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                GlobalIslandState.updateMusicActivity(
                    title = "Media Session",
                    artist = ctrl.packageName?.substringAfterLast('.') ?: "Player",
                    bitmap = null,
                    isPlaying = true,
                    packageName = ctrl.packageName,
                    durationMs = 180000L,
                    positionMs = 0L,
                    playbackSpeed = 1.0f,
                    lastPositionUpdateTime = SystemClock.elapsedRealtime(),
                    controller = ctrl
                )
            } catch (re: Throwable) {
                re.printStackTrace()
            }
        }
    }

    private fun isBankingOrCredentialPackage(packageName: String): Boolean {
        val p = packageName.lowercase()
        return p.contains("bank") ||
                p.contains("chase") ||
                p.contains("wellsfargo") ||
                p.contains("citi") ||
                p.contains("fidelity") ||
                p.contains("vanguard") ||
                p.contains("capitalone") ||
                p.contains("authenticator") ||
                p.contains("bitwarden") ||
                p.contains("1password") ||
                p.contains("keychain")
    }

    companion object {
        fun togglePlayPauseGlobal(context: Context? = null) {
            if (context != null) {
                MediaPlaybackBridge(context).togglePlayPause()
            } else {
                val controller = GlobalIslandState.mediaController
                if (controller != null) {
                    when (controller.playbackState?.state) {
                        PlaybackState.STATE_PLAYING -> dispatchMediaAction(controller, MediaAction.PAUSE, context)
                        else -> dispatchMediaAction(controller, MediaAction.PLAY, context)
                    }
                } else {
                    dispatchMediaAction(null, MediaAction.PLAY, context)
                }
            }
        }

        fun dispatchMediaAction(controller: MediaController?, action: MediaAction, context: Context? = null) {
            controller?.let { mc ->
                val keyCode = when (action) {
                    MediaAction.PLAY -> {
                        try { mc.transportControls.play() } catch (e: Exception) { Log.w("MediaControl", "Direct play failed", e) }
                        KeyEvent.KEYCODE_MEDIA_PLAY
                    }
                    MediaAction.PAUSE -> {
                        try { mc.transportControls.pause() } catch (e: Exception) { Log.w("MediaControl", "Direct pause failed", e) }
                        KeyEvent.KEYCODE_MEDIA_PAUSE
                    }
                    MediaAction.NEXT -> {
                        try { mc.transportControls.skipToNext() } catch (e: Exception) { Log.w("MediaControl", "Direct next failed", e) }
                        KeyEvent.KEYCODE_MEDIA_NEXT
                    }
                    MediaAction.PREVIOUS -> {
                        try { mc.transportControls.skipToPrevious() } catch (e: Exception) { Log.w("MediaControl", "Direct previous failed", e) }
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS
                    }
                }
                val downTime = SystemClock.uptimeMillis()
                val downEvent = KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0)
                val upEvent = KeyEvent(downTime, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0)
                try {
                    mc.dispatchMediaButtonEvent(downEvent)
                    mc.dispatchMediaButtonEvent(upEvent)
                } catch (e: Exception) {
                    Log.w("MediaControl", "Key event dispatch failed for $action", e)
                }
            } ?: run {
                if (context != null) {
                    try {
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                        val keyCode = when (action) {
                            MediaAction.PLAY -> KeyEvent.KEYCODE_MEDIA_PLAY
                            MediaAction.PAUSE -> KeyEvent.KEYCODE_MEDIA_PAUSE
                            MediaAction.NEXT -> KeyEvent.KEYCODE_MEDIA_NEXT
                            MediaAction.PREVIOUS -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
                        }
                        val now = SystemClock.uptimeMillis()
                        audioManager?.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
                        audioManager?.dispatchMediaKeyEvent(KeyEvent(now, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0))
                    } catch (e: Exception) {
                        Log.w("MediaControl", "AudioManager fallback dispatch failed for $action", e)
                    }
                }
            }
        }

        fun resumePlayback(controller: MediaController?, context: Context? = null) {
            if (context != null) {
                MediaPlaybackBridge(context).play()
            } else {
                dispatchMediaAction(controller, MediaAction.PLAY, context)
            }
        }

        fun pausePlayback(controller: MediaController?, context: Context? = null) {
            if (context != null) {
                MediaPlaybackBridge(context).pause()
            } else {
                dispatchMediaAction(controller, MediaAction.PAUSE, context)
            }
        }

        fun endCall(sbn: StatusBarNotification? = null, context: Context? = null) {
            sbn?.notification?.actions?.firstOrNull { action ->
                val title = action.title?.toString()?.lowercase().orEmpty()
                title.contains("end") || title.contains("decline") || title.contains("reject") || title.contains("hang up") || title.contains("dismiss")
            }?.actionIntent?.let { intent ->
                try { intent.send() } catch (e: Exception) { Log.e("MediaControl", "Call disconnect intent canceled", e) }
            }
            context?.let { ctx ->
                try {
                    val telecomManager = ctx.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
                    @Suppress("DEPRECATION")
                    telecomManager?.endCall()
                } catch (e: Exception) {
                    Log.w("MediaControl", "TelecomManager endCall fallback failed/unsupported", e)
                }
            }
        }
    }
}

class MediaPlaybackBridge(private val context: Context) {
    private val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
    private var activeController: MediaController? = null

    fun bindActiveSession(): MediaController? {
        val component = ComponentName(context, MediaNotificationListenerService::class.java)
        val controllers = try {
            sessionManager?.getActiveSessions(component)
        } catch (e: Exception) {
            null
        }
        val active = controllers?.firstOrNull { 
            it.playbackState?.state == PlaybackState.STATE_PLAYING 
        } ?: controllers?.firstOrNull() ?: GlobalIslandState.mediaController
        activeController = active
        return active
    }

    fun togglePlayPause() {
        val controller = bindActiveSession()
        if (controller != null) {
            when (controller.playbackState?.state) {
                PlaybackState.STATE_PLAYING -> {
                    try { controller.transportControls.pause() } catch (e: Exception) { dispatchMediaButtonKey(controller, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
                }
                PlaybackState.STATE_PAUSED, PlaybackState.STATE_STOPPED -> {
                    try { controller.transportControls.play() } catch (e: Exception) { dispatchMediaButtonKey(controller, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
                }
                else -> {
                    dispatchMediaButtonKey(controller, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                }
            }
        } else {
            dispatchGlobalAudioManagerKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    fun play() {
        val controller = bindActiveSession()
        if (controller != null) {
            try { controller.transportControls.play() } catch (e: Exception) { dispatchMediaButtonKey(controller, KeyEvent.KEYCODE_MEDIA_PLAY) }
        } else {
            dispatchGlobalAudioManagerKey(KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    fun pause() {
        val controller = bindActiveSession()
        if (controller != null) {
            try { controller.transportControls.pause() } catch (e: Exception) { dispatchMediaButtonKey(controller, KeyEvent.KEYCODE_MEDIA_PAUSE) }
        } else {
            dispatchGlobalAudioManagerKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
        }
    }

    fun skipNext() {
        val controller = bindActiveSession()
        if (controller != null) {
            try { controller.transportControls.skipToNext() } catch (e: Exception) { dispatchMediaButtonKey(controller, KeyEvent.KEYCODE_MEDIA_NEXT) }
        } else {
            dispatchGlobalAudioManagerKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        }
    }

    fun skipPrevious() {
        val controller = bindActiveSession()
        if (controller != null) {
            try { controller.transportControls.skipToPrevious() } catch (e: Exception) { dispatchMediaButtonKey(controller, KeyEvent.KEYCODE_MEDIA_PREVIOUS) }
        } else {
            dispatchGlobalAudioManagerKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
    }

    private fun dispatchMediaButtonKey(controller: MediaController, keyCode: Int) {
        try {
            val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            controller.dispatchMediaButtonEvent(down)
            controller.dispatchMediaButtonEvent(up)
        } catch (e: Exception) {
            dispatchGlobalAudioManagerKey(keyCode)
        }
    }

    private fun dispatchGlobalAudioManagerKey(keyCode: Int) {
        try {
            val now = SystemClock.uptimeMillis()
            audioManager?.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
            audioManager?.dispatchMediaKeyEvent(KeyEvent(now, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, keyCode, 0))
        } catch (e: Exception) {
            Log.e("MediaPlaybackBridge", "Failed to dispatch media key $keyCode", e)
        }
    }
}
