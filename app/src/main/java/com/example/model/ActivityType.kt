package com.example.model

enum class LiveActivityType {
    CALL,
    FITNESS,
    ALARM,
    TIMER,
    MUSIC,
    WORKOUT,
    NAVIGATION,
    DELIVERY,
    NOTIFICATION
}

data class NotificationActionItem(
    val title: String,
    val intent: android.app.PendingIntent? = null
)

sealed class LiveActivityState {
    open val originatingPackageName: String? = null

    data object Idle : LiveActivityState()

    data class Music(
        val song: Song = com.example.model.Song(id = "0", title = "Unknown", artist = "Unknown", album = "Unknown", durationMs = 0L, primaryColor = androidx.compose.ui.graphics.Color.Gray, secondaryColor = androidx.compose.ui.graphics.Color.DarkGray),
        val isPlaying: Boolean = false,
        val positionMs: Long = 0L,
        val volumePercent: Float = 0.8f,
        val isFavorite: Boolean = true,
        val isShuffle: Boolean = false,
        val isRepeat: Boolean = false,
        val playbackSpeed: Float = 1.0f,
        val lastPositionUpdateTime: Long = android.os.SystemClock.elapsedRealtime(),
        override val originatingPackageName: String? = null
    ) : LiveActivityState() {
        val albumArtworkAccentColor: androidx.compose.ui.graphics.Color?
            get() = if (song.coverBitmap != null || song.primaryColor != androidx.compose.ui.graphics.Color.Gray) song.primaryColor else null
    }

    data class Workout(
        val activityName: String = "Outdoor Running",
        val elapsedSeconds: Long = 1425L, // 23:45
        val distanceKm: Float = 3.82f,
        val calories: Int = 284,
        val heartRate: Int = 148,
        val isPaused: Boolean = false,
        override val originatingPackageName: String? = "com.google.android.apps.fitness"
    ) : LiveActivityState()

    data class Fitness(
        val appName: String = "Strava",
        val activityName: String = "Morning Run",
        val elapsedSeconds: Long = 0L,
        val startTimeMs: Long = System.currentTimeMillis(),
        val distanceText: String = "5.20 km",
        val paceText: String = "4'52\" /km",
        val heartRate: Int = 148,
        val heartRateBpm: Int? = null,
        val isPaused: Boolean = false,
        val elapsedDurationMs: Long = 0L,
        val staticDisplayTime: String? = null,
        val distance: String? = null,
        val pace: String? = null,
        val brandColorLong: Long = 0xFFFC4C02, // Strava Orange
        override val originatingPackageName: String? = "com.strava"
    ) : LiveActivityState() {
        val effectiveDistance: String
            get() = distance ?: distanceText

        val effectivePace: String
            get() = pace ?: paceText

        val effectiveHeartRate: Int?
            get() = heartRateBpm ?: (if (heartRate > 0) heartRate else null)

        val effectiveDisplayTime: String?
            get() = staticDisplayTime ?: if (elapsedSeconds > 0) {
                val h = elapsedSeconds / 3600
                val m = (elapsedSeconds % 3600) / 60
                val s = elapsedSeconds % 60
                if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
            } else null
    }

    data class Alarm(
        val title: String = "Alarm",
        val timeText: String = "07:00 AM",
        val isRinging: Boolean = true,
        val dismissIntent: android.app.PendingIntent? = null,
        val snoozeIntent: android.app.PendingIntent? = null,
        val onDismiss: (() -> Unit)? = null,
        val onSnooze: (() -> Unit)? = null,
        override val originatingPackageName: String? = "com.google.android.deskclock"
    ) : LiveActivityState()

    data class Timer(
        val title: String = "Pomodoro Focus",
        val remainingSeconds: Long = 840L, // 14:00
        val totalSeconds: Long = 1500L, // 25:00
        val targetEndTimeMs: Long = System.currentTimeMillis() + 840L * 1000L,
        val isRunning: Boolean = false,
        override val originatingPackageName: String? = "com.google.android.deskclock"
    ) : LiveActivityState()

    data class Navigation(
        val destination: String = "Downtown Central Station",
        val currentInstruction: String = "In 200m, turn right onto Grand Avenue",
        val distanceText: String = "200m",
        val iconType: NavigationDirection = NavigationDirection.TURN_RIGHT,
        val etaMinutes: Int = 14,
        override val originatingPackageName: String? = "com.google.android.apps.maps"
    ) : LiveActivityState()

    data class Delivery(
        val restaurantName: String = "Artisan Pizza Co.",
        val statusText: String = "Driver is 5 mins away",
        val progressPercent: Float = 0.75f,
        val driverName: String = "Marco V.",
        val orderItemCount: Int = 3,
        val providerPackage: String? = "com.ubercab.eats",
        override val originatingPackageName: String? = providerPackage
    ) : LiveActivityState()

    data class IncomingCall(
        val contactName: String = "Sophia Chen",
        val subtext: String = "Incoming Video Call...",
        val elapsedSeconds: Long = 0L,
        val callStartTimeMs: Long = 0L,
        val isAnswered: Boolean = false,
        val answerIntent: android.app.PendingIntent? = null,
        val rejectIntent: android.app.PendingIntent? = null,
        val onAnswer: (() -> Unit)? = null,
        val onReject: (() -> Unit)? = null,
        val onEndCall: (() -> Unit)? = null,
        override val originatingPackageName: String? = "com.google.android.dialer"
    ) : LiveActivityState()

    data class Notification(
        val appName: String = "WhatsApp",
        val sender: String = "Sarah",
        val title: String = "New Message",
        val body: String = "Hey, are we still meeting today?",
        val iconName: String = "Message",
        val brandColorLong: Long = 0xFF25D366,
        val timestampMs: Long = System.currentTimeMillis(),
        val isSensitive: Boolean = false,
        val originalBody: String = "",
        val rawText: String = body,
        val maskedText: String = body,
        val actions: List<NotificationActionItem> = emptyList(),
        override val originatingPackageName: String? = null
    ) : LiveActivityState() {
        val appBrandColor: androidx.compose.ui.graphics.Color
            get() = androidx.compose.ui.graphics.Color(brandColorLong)
    }
}

enum class NavigationDirection {
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    U_TURN,
    ROUNDABOUT
}

enum class IslandExpansionMode {
    HIDDEN,       // Fully hidden (alpha 0, scaled down)
    COMPACT,      // Default black pill at top
    EXPANDED,     // Expanded interactive card view
    SPLIT         // Split pill into two mini bubbles (e.g. Music on left + Timer on right)
}
