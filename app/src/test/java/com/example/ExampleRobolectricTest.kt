package com.example

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.service.notification.StatusBarNotification
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.model.LiveActivityState
import com.example.service.MediaNotificationListenerService
import com.example.util.PaletteExtractor
import com.example.viewmodel.DynamicIslandViewModel
import com.example.viewmodel.GlobalIslandState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Before
    fun setup() {
        GlobalIslandState.resetAll()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Dynamic Island", appName)
    }

    @Test
    fun `paletteExtractor handles null bitmap safely without crash`() = kotlinx.coroutines.runBlocking {
        val color = PaletteExtractor.extractAccentColor(
            bitmap = null,
            defaultColor = Color(0xFF00E5FF),
            packageName = "com.google.android.youtube"
        )
        // YouTube fallback should return vibrant red
        assertEquals(Color(0xFFFF0000), color)
    }

    @Test
    fun `paletteExtractor handles pure black bitmap with vibrant fallback`() = kotlinx.coroutines.runBlocking {
        val pureBlackBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888).apply {
            eraseColor(AndroidColor.BLACK)
        }
        val defaultColor = Color(0xFF10B981)
        val extracted = PaletteExtractor.extractAccentColor(
            bitmap = pureBlackBitmap,
            defaultColor = defaultColor,
            packageName = "com.spotify.music"
        )
        // Ensure black is not returned (luminance fallback triggers)
        assertNotEquals(Color.Black, extracted)
        assertEquals(defaultColor, extracted)
    }

    @Test
    fun `globalIslandState prioritizes incoming call over media`() {
        // Trigger media
        GlobalIslandState.updateMusicActivity(
            title = "Test Song",
            artist = "Test Artist",
            bitmap = null,
            isPlaying = true,
            packageName = "com.spotify.music",
            durationMs = 210000L,
            positionMs = 30000L
        )
        Thread.sleep(100)
        assertTrue(GlobalIslandState.activeActivity.value is LiveActivityState.Music)

        // Trigger Call
        GlobalIslandState.updateCallActivity(
            LiveActivityState.IncomingCall(
                contactName = "Alice",
                subtext = "Incoming Voice Call"
            )
        )
        // Call must supersede Music in priority
        assertTrue(GlobalIslandState.activeActivity.value is LiveActivityState.IncomingCall)
    }

    @Test
    fun `youtube video snooze hides island and clears on new track`() {
        // Start playing YouTube video
        GlobalIslandState.updateMusicActivity(
            title = "YouTube Video A",
            artist = "Creator One",
            bitmap = null,
            isPlaying = true,
            packageName = "com.google.android.youtube",
            durationMs = 600000L,
            positionMs = 12000L
        )
        Thread.sleep(100)
        assertEquals(com.example.model.IslandExpansionMode.COMPACT, GlobalIslandState.expansionMode.value)

        // Snooze current video
        GlobalIslandState.snoozeCurrentVideo()
        assertEquals(com.example.model.IslandExpansionMode.HIDDEN, GlobalIslandState.expansionMode.value)
        assertEquals("YouTube Video A", GlobalIslandState.dismissedVideoToken)

        // Same video progress update should keep island HIDDEN
        GlobalIslandState.updateMusicActivity(
            title = "YouTube Video A",
            artist = "Creator One",
            bitmap = null,
            isPlaying = true,
            packageName = "com.google.android.youtube",
            durationMs = 600000L,
            positionMs = 15000L
        )
        Thread.sleep(100)
        assertEquals(com.example.model.IslandExpansionMode.HIDDEN, GlobalIslandState.expansionMode.value)

        // When video changes (e.g., user picks next video), snooze is cleared and island reappears
        GlobalIslandState.updateMusicActivity(
            title = "YouTube Video B",
            artist = "Creator Two",
            bitmap = null,
            isPlaying = true,
            packageName = "com.google.android.youtube",
            durationMs = 400000L,
            positionMs = 1000L
        )
        Thread.sleep(100)
        assertEquals(null, GlobalIslandState.dismissedVideoToken)
        assertEquals(com.example.model.IslandExpansionMode.COMPACT, GlobalIslandState.expansionMode.value)
    }

    @Test
    fun `higher priority activity wakes up snoozed island`() {
        // Start and snooze a video
        GlobalIslandState.updateMusicActivity(
            title = "Snoozed Podcast",
            artist = "Host",
            bitmap = null,
            isPlaying = true,
            packageName = "com.google.android.youtube",
            durationMs = 3000000L,
            positionMs = 10000L
        )
        GlobalIslandState.snoozeCurrentVideo()
        assertEquals(com.example.model.IslandExpansionMode.HIDDEN, GlobalIslandState.expansionMode.value)

        // Trigger a timer - higher priority activity forces wake up
        GlobalIslandState.updateTimerActivity(
            LiveActivityState.Timer(
                title = "Tea Timer",
                remainingSeconds = 60L,
                totalSeconds = 60L,
                isRunning = true
            )
        )
        assertEquals(com.example.model.IslandExpansionMode.COMPACT, GlobalIslandState.expansionMode.value)
        assertTrue(GlobalIslandState.activeActivity.value is LiveActivityState.Timer)
    }

    @Test
    fun `fitness activity accurately reflects live heart rate or null fallback`() {
        val fitnessWithHr = LiveActivityState.Fitness(
            appName = "Strava",
            activityName = "Afternoon Jog",
            elapsedSeconds = 900L,
            distanceText = "3.20 km",
            paceText = "4'45\" /km",
            heartRate = 158,
            heartRateBpm = 158
        )
        assertEquals(158, fitnessWithHr.effectiveHeartRate)

        val fitnessWithoutHr = LiveActivityState.Fitness(
            appName = "Wear OS",
            activityName = "Walking",
            elapsedSeconds = 300L,
            distanceText = "1.10 km",
            paceText = "6'10\" /km",
            heartRate = 0,
            heartRateBpm = null
        )
        assertEquals(null, fitnessWithoutHr.effectiveHeartRate)
    }

    @Test
    fun `strava paused state detected from resume action and frozen duration preserved`() {
        val service = Robolectric.buildService(MediaNotificationListenerService::class.java).create().get()

        val notification = Notification.Builder(service, "fitness")
            .setContentTitle("Morning Run")
            .setContentText("24:15  5.20 km  4'52\" /km")
            .addAction(Notification.Action.Builder(null, "Resume", null).build())
            .build()

        val sbn = StatusBarNotification(
            "com.strava",
            "com.strava",
            101,
            "strava_tag",
            1000,
            0,
            0,
            notification,
            android.os.Process.myUserHandle(),
            System.currentTimeMillis()
        )

        service.onNotificationPosted(sbn)
        Thread.sleep(100)

        val fitness = GlobalIslandState.fitnessActivityState.value
        assertNotNull(fitness)
        assertTrue(fitness!!.isPaused)
        assertEquals("24:15", fitness.staticDisplayTime)
        assertEquals(1455L, fitness.elapsedSeconds)
        assertEquals(1455000L, fitness.elapsedDurationMs)
        assertEquals("5.20 km", fitness.effectiveDistance)
        assertEquals("4'52\" /km", fitness.effectivePace)
    }

    @Test
    fun `strava paused state detected from text and subtext`() {
        val service = Robolectric.buildService(MediaNotificationListenerService::class.java).create().get()

        val notification = Notification.Builder(service, "fitness")
            .setContentTitle("Morning Run - Paused")
            .setContentText("1:12:30  12.4 km  5'50\" /km")
            .setSubText("Paused")
            .build()

        val sbn = StatusBarNotification(
            "com.strava",
            "com.strava",
            102,
            "strava_tag",
            1000,
            0,
            0,
            notification,
            android.os.Process.myUserHandle(),
            System.currentTimeMillis()
        )

        service.onNotificationPosted(sbn)
        Thread.sleep(100)

        val fitness = GlobalIslandState.fitnessActivityState.value
        assertNotNull(fitness)
        assertTrue(fitness!!.isPaused)
        assertEquals("1:12:30", fitness.staticDisplayTime)
        assertEquals(4350L, fitness.elapsedSeconds)
        assertEquals(4350000L, fitness.elapsedDurationMs)
    }

    @Test
    fun `strava active running state calculates live duration and is not paused`() {
        val service = Robolectric.buildService(MediaNotificationListenerService::class.java).create().get()

        val notification = Notification.Builder(service, "fitness")
            .setContentTitle("Morning Run")
            .setContentText("25:00  5.30 km  4'43\" /km")
            .addAction(Notification.Action.Builder(null, "Pause", null).build())
            .build()

        val sbn = StatusBarNotification(
            "com.strava",
            "com.strava",
            103,
            "strava_tag",
            1000,
            0,
            0,
            notification,
            android.os.Process.myUserHandle(),
            System.currentTimeMillis()
        )

        service.onNotificationPosted(sbn)
        Thread.sleep(100)

        val fitness = GlobalIslandState.fitnessActivityState.value
        assertNotNull(fitness)
        assertFalse(fitness!!.isPaused)
        assertEquals(null, fitness.staticDisplayTime)
        assertEquals(1500L, fitness.elapsedSeconds)
    }

    @Test
    fun `toggleFitnessPause halts and freezes workout duration correctly`() {
        val vm = DynamicIslandViewModel()
        vm.triggerStravaFitnessDemo(withHeartRate = true, isPaused = false)

        val active = GlobalIslandState.fitnessActivityState.value
        assertNotNull(active)
        assertFalse(active!!.isPaused)
        assertEquals(null, active.staticDisplayTime)

        vm.toggleFitnessPause()

        val paused = GlobalIslandState.fitnessActivityState.value
        assertNotNull(paused)
        assertTrue(paused!!.isPaused)
        assertNotNull(paused.staticDisplayTime)
        assertEquals("23:40", paused.staticDisplayTime)
        assertEquals(1420L, paused.elapsedSeconds)

        vm.toggleFitnessPause()

        val resumed = GlobalIslandState.fitnessActivityState.value
        assertNotNull(resumed)
        assertFalse(resumed!!.isPaused)
        assertEquals(null, resumed.staticDisplayTime)
    }

    @Test
    fun `standard chat notification is not flagged as sensitive`() {
        val service = Robolectric.buildService(MediaNotificationListenerService::class.java).create().get()

        val notification = Notification.Builder(service, "messages")
            .setContentTitle("Alex")
            .setContentText("Hey, see you at the cafe at 3!")
            .build()

        val sbn = StatusBarNotification(
            "org.telegram.messenger",
            "org.telegram.messenger",
            201,
            "tag_telegram",
            1000,
            0,
            0,
            notification,
            android.os.Process.myUserHandle(),
            System.currentTimeMillis()
        )

        service.onNotificationPosted(sbn)
        Thread.sleep(100)

        val notif = GlobalIslandState.notificationActivityState.value
        assertNotNull(notif)
        assertFalse(notif!!.isSensitive)
        assertEquals("Hey, see you at the cafe at 3!", notif.body)
        assertEquals("Hey, see you at the cafe at 3!", notif.originalBody)
    }

    @Test
    fun `banking or otp notification is flagged as sensitive and unmasks upon device unlock`() {
        val vm = DynamicIslandViewModel()
        val rawBody = "Your authentication code is 492019. Do not share."

        // Initially device is locked
        GlobalIslandState.updateDeviceLockedStatus(isLocked = true)
        assertTrue(GlobalIslandState.isDeviceLocked.value)

        vm.triggerNotification(
            appName = "Chase",
            sender = "Chase Bank",
            title = "Security Alert",
            body = rawBody,
            isSensitive = true
        )

        val notif = GlobalIslandState.notificationActivityState.value
        assertNotNull(notif)
        assertTrue(notif!!.isSensitive)
        assertEquals(rawBody, notif.originalBody)

        // Device is unlocked (ACTION_USER_PRESENT)
        GlobalIslandState.updateDeviceLockedStatus(isLocked = false)
        assertFalse(GlobalIslandState.isDeviceLocked.value)

        // Content condition: activity.isSensitive && settings.hideSensitiveContent && isDeviceLocked
        val shouldMaskWhenUnlocked = notif.isSensitive &&
                GlobalIslandState.settings.value.hideSensitiveContent &&
                GlobalIslandState.isDeviceLocked.value
        assertFalse(shouldMaskWhenUnlocked)

        // Re-locking device (ACTION_SCREEN_OFF)
        GlobalIslandState.updateDeviceLockedStatus(isLocked = true)
        val shouldMaskWhenReLocked = notif.isSensitive &&
                GlobalIslandState.settings.value.hideSensitiveContent &&
                GlobalIslandState.isDeviceLocked.value
        assertTrue(shouldMaskWhenReLocked)
    }

    @Test
    fun `spotify exit to background triggers center to notch flight morph`() {
        val vm = DynamicIslandViewModel()
        GlobalIslandState.updateMusicActivity(
            title = "Starboy",
            artist = "The Weeknd",
            bitmap = null,
            isPlaying = true,
            packageName = "com.spotify.music",
            durationMs = 230000L,
            positionMs = 45000L
        )

        // Enter Spotify
        vm.onSpotifyEnteredForeground()
        assertTrue(vm.isSpotifyInForeground.value)

        val triggerBefore = vm.flightTrigger.value

        // Exit Spotify
        vm.onSpotifyExitedToBackground()
        assertFalse(vm.isSpotifyInForeground.value)
        assertTrue(vm.flightTrigger.value >= triggerBefore)

        // Set flight active toggles state
        vm.setFlightActive(true)
        assertTrue(vm.isCenterToNotchFlightActive.value)
        vm.setFlightActive(false)
        assertFalse(vm.isCenterToNotchFlightActive.value)
    }
}
