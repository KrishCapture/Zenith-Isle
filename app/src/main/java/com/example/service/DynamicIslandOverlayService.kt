package com.example.service

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.model.IslandExpansionMode
import com.example.model.LiveActivityState
import com.example.ui.components.DynamicIslandPill
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DynamicIslandViewModel
import com.example.viewmodel.GlobalIslandState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DynamicIslandOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null
    private var overlayLayoutParams: WindowManager.LayoutParams? = null

    fun ensureOverlayAttachedAndVisible() {
        val composeView = overlayComposeView ?: return
        val wm = windowManager ?: return
        val params = overlayLayoutParams ?: return
        try {
            if (!composeView.isAttachedToWindow) {
                try {
                    wm.removeView(composeView)
                } catch (ignored: Exception) {}
                try {
                    wm.addView(composeView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                wm.updateViewLayout(composeView, params)
                composeView.requestLayout()
                composeView.invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                wm.removeView(composeView)
                wm.addView(composeView, params)
            } catch (ignored: Exception) {}
        }
    }

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var accelSensor: Sensor? = null

    private var lastShakeTime = 0L
    private val shakeSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER || event.values.isEmpty()) return
            if (!GlobalIslandState.settings.value.enableShakeToClear) return
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            val gForce = kotlin.math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
            if (gForce > GlobalIslandState.settings.value.shakeThresholdG) {
                val now = SystemClock.uptimeMillis()
                if (now - lastShakeTime >= 800L) {
                    lastShakeTime = now
                    GlobalIslandState.dismissNonPersistentOnShake()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT && event.values.isNotEmpty()) {
                val lux = event.values[0]
                GlobalIslandState.updateAmbientLux(lux)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val screenUnlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> {
                    GlobalIslandState.updateDeviceLockedStatus(isLocked = false)
                    ensureOverlayAttachedAndVisible()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    GlobalIslandState.updateDeviceLockedStatus(isLocked = true)
                    ensureOverlayAttachedAndVisible()
                }
                Intent.ACTION_SCREEN_ON -> {
                    val keyguardManager = context?.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    GlobalIslandState.updateDeviceLockedStatus(isLocked = keyguardManager?.isKeyguardLocked == true)
                    ensureOverlayAttachedAndVisible()
                }
            }
        }
    }

    private val dockHandoffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.ACTION_DOCK_HANDOFF") {
                GlobalIslandState.triggerDockSettlement()
                GlobalIslandState.expansionMode.value = IslandExpansionMode.COMPACT
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        GlobalIslandState.initPersistence(applicationContext)
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

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

        val handoffFilter = IntentFilter("com.example.ACTION_DOCK_HANDOFF")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(dockHandoffReceiver, handoffFilter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(dockHandoffReceiver, handoffFilter)
            }
        } catch (e: Exception) {
            try {
                registerReceiver(dockHandoffReceiver, handoffFilter)
            } catch (ignored: Exception) {}
        }

        startForegroundServiceNotification()
        showOverlayWindow()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensorManager != null && lightSensor != null) {
            sensorManager?.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_UI)
        }
        accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager != null && accelSensor != null) {
            sensorManager?.registerListener(shakeSensorListener, accelSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()
        ensureOverlayAttachedAndVisible()
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "dynamic_island_overlay_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Dynamic Island Live Activity",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Persistent Dynamic Island status and controls"
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        } else {
            android.app.PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            pendingIntentFlags
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Dynamic Island Active")
            .setContentText("HyperIsland punch-hole overlay is running • Tap to open controls")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                1001,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(1001, notification)
        }
    }

    private fun showOverlayWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val density = resources.displayMetrics.density
        val viewModel = DynamicIslandViewModel()
        val initialOffsetYDp = viewModel.pillOffsetYDp.value

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = (initialOffsetYDp * density).toInt()
        }

        overlayLayoutParams = params
        val composeView = ComposeView(this).apply {
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            setViewTreeLifecycleOwner(this@DynamicIslandOverlayService)
            setViewTreeViewModelStoreOwner(this@DynamicIslandOverlayService)
            setViewTreeSavedStateRegistryOwner(this@DynamicIslandOverlayService)
            setContent {
                MyApplicationTheme {
                    val activeActivity = viewModel.activeActivity.collectAsState().value
                    val secondaryActivity = viewModel.secondaryActivity.collectAsState().value
                    val expansionMode = viewModel.expansionMode.collectAsState().value
                    val settings = viewModel.settings.collectAsState().value
                    val activeNotification = viewModel.activeNotification.collectAsState().value

                    // SERVICE LIFECYCLE & STATE HAND-OFF:
                    // Force state to COMPACT when active media/activity arrives while in HIDDEN state, unless snoozed
                    LaunchedEffect(activeActivity, activeNotification) {
                        if ((activeActivity !is LiveActivityState.Idle || activeNotification != null) && expansionMode == IslandExpansionMode.HIDDEN) {
                            val isSnoozedVideo = (activeActivity as? LiveActivityState.Music)?.let {
                                GlobalIslandState.dismissedVideoToken != null &&
                                        (it.song.title == GlobalIslandState.dismissedVideoToken || GlobalIslandState.dismissedVideoToken == "current_video")
                            } ?: false

                            if (!isSnoozedVideo) {
                                viewModel.setExpansionMode(IslandExpansionMode.COMPACT)
                            }
                        }
                    }

                    val isAccessibilityActive = viewModel.isAccessibilityServiceConnected.collectAsState().value

                    Box(
                        modifier = Modifier.wrapContentSize(Alignment.TopCenter),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (!isAccessibilityActive) {
                            DynamicIslandPill(
                                activeActivity = activeActivity,
                                secondaryActivity = secondaryActivity,
                                expansionMode = expansionMode,
                                settings = settings,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
        overlayComposeView = composeView

        composeView.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: android.view.View) {
                try {
                    val currentDensity = resources.displayMetrics.density
                    params.y = (viewModel.pillOffsetYDp.value * currentDensity).toInt()
                    windowManager?.updateViewLayout(composeView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            override fun onViewDetachedFromWindow(v: android.view.View) {}
        })

        try {
            windowManager?.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        lifecycleScope.launch {
            viewModel.pillOffsetYDp.collectLatest { offsetDp ->
                val displayDensity = resources.displayMetrics.density
                params.y = (offsetDp * displayDensity).toInt()
                if (composeView.isAttachedToWindow) {
                    try {
                        windowManager?.updateViewLayout(composeView, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        lifecycleScope.launch {
            GlobalIslandState.isCenterToNotchFlightActive.collectLatest { isFlightActive ->
                if (isFlightActive) {
                    params.width = WindowManager.LayoutParams.MATCH_PARENT
                    params.height = WindowManager.LayoutParams.MATCH_PARENT
                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                } else {
                    params.width = WindowManager.LayoutParams.WRAP_CONTENT
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                }
                if (composeView.isAttachedToWindow) {
                    try {
                        windowManager?.updateViewLayout(composeView, params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        lifecycleScope.launch {
            kotlinx.coroutines.flow.combine(viewModel.activeActivity, viewModel.activeNotification) { act, notif ->
                Pair(act, notif)
            }.collectLatest {
                ensureOverlayAttachedAndVisible()
            }
        }
    }

    override fun onDestroy() {
        try {
            sensorManager?.unregisterListener(lightSensorListener)
            sensorManager?.unregisterListener(shakeSensorListener)
        } catch (ignored: Exception) {}
        try {
            unregisterReceiver(screenUnlockReceiver)
        } catch (ignored: Exception) {}
        try {
            unregisterReceiver(dockHandoffReceiver)
        } catch (ignored: Exception) {}
        super.onDestroy()
        if (overlayComposeView != null) {
            try {
                overlayComposeView?.disposeComposition()
                windowManager?.removeView(overlayComposeView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayComposeView = null
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
