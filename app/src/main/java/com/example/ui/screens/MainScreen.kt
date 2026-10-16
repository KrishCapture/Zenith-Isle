package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import com.example.viewmodel.MediaDisplayMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.LiveActivityState
import com.example.ui.components.PhoneSimulatorCanvas
import com.example.ui.components.SettingsDrawer
import com.example.viewmodel.DynamicIslandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DynamicIslandViewModel) {
    val activeActivity by viewModel.activeActivity.collectAsStateWithLifecycle(initialValue = LiveActivityState.Idle)
    val secondaryActivity by viewModel.secondaryActivity.collectAsStateWithLifecycle(initialValue = null)
    val expansionMode by viewModel.expansionMode.collectAsStateWithLifecycle(initialValue = com.example.model.IslandExpansionMode.COMPACT)
    val settings by viewModel.settings.collectAsStateWithLifecycle(initialValue = com.example.viewmodel.IslandSettings())
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle(initialValue = com.example.viewmodel.IslandSettings())
    val isPulseEnabled by viewModel.isPulseEnabled.collectAsStateWithLifecycle(initialValue = true)
    val isSpringEnabled by viewModel.isSpringEnabled.collectAsStateWithLifecycle(initialValue = true)
    val isDarkMode = settings.isDarkMode

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initPersistence(context)
    }

    var isSettingsExpanded by remember { mutableStateOf(true) }

    val transitionSpec = tween<Color>(durationMillis = 400, easing = androidx.compose.animation.core.FastOutSlowInEasing)

    val animatedTextPrimary by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFFEDEDED) else Color(0xFF19191D),
        animationSpec = transitionSpec,
        label = "TextPrimaryTransition"
    )

    val animatedTextSecondary by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF8E8E96) else Color(0xFF6B6B76),
        animationSpec = transitionSpec,
        label = "TextSecondaryTransition"
    )

    val animatedRootBg by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF0D0D11) else Color(0xFFF6F6F9),
        animationSpec = transitionSpec,
        label = "RootBgTransition"
    )

    val animatedCardBg by animateColorAsState(
        targetValue = if (isDarkMode) Color(0xFF16161E).copy(alpha = 0.90f) else Color(0xFFFFFFFF),
        animationSpec = transitionSpec,
        label = "CardBgTransition"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
        animationSpec = transitionSpec,
        label = "BorderColorTransition"
    )

    val animatedGlowAlpha by animateFloatAsState(
        targetValue = if (isDarkMode) 0.45f else 0.22f,
        animationSpec = tween(400, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "GlowAlphaTransition"
    )

    val textPrimaryColor = animatedTextPrimary
    val textSecondaryColor = animatedTextSecondary
    val rootBackgroundColor = animatedRootBg
    val cardContainerColor = animatedCardBg

    val resolvedAccentColor by viewModel.resolvedAccentColor.collectAsStateWithLifecycle(initialValue = Color(0xFF6366F1))

    val animatedAccentColor by animateColorAsState(
        targetValue = resolvedAccentColor,
        animationSpec = tween(durationMillis = 500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "dynamic_accent_sync"
    )

    val autoLightSensorTheme = settings.autoLightSensorTheme

    androidx.compose.runtime.DisposableEffect(autoLightSensorTheme) {
        if (!autoLightSensorTheme) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        val lightSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT)

        val sensorEventListener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event?.sensor?.type == android.hardware.Sensor.TYPE_LIGHT && event.values.isNotEmpty()) {
                    val lux = event.values[0]
                    viewModel.updateAmbientLux(lux)
                }
            }
            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && lightSensor != null) {
            sensorManager.registerListener(sensorEventListener, lightSensor, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(sensorEventListener)
        }
    }

    val enableShakeToClear = settings.enableShakeToClear
    val shakeThresholdG = settings.shakeThresholdG
    androidx.compose.runtime.DisposableEffect(enableShakeToClear, shakeThresholdG) {
        if (!enableShakeToClear) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as? android.hardware.SensorManager
        val accelSensor = sensorManager?.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

        var lastShakeTime = 0L
        val shakeListener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event?.sensor?.type != android.hardware.Sensor.TYPE_ACCELEROMETER || event.values.isEmpty()) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val gX = x / android.hardware.SensorManager.GRAVITY_EARTH
                val gY = y / android.hardware.SensorManager.GRAVITY_EARTH
                val gZ = z / android.hardware.SensorManager.GRAVITY_EARTH

                val gForce = kotlin.math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
                if (gForce > shakeThresholdG) {
                    val now = android.os.SystemClock.elapsedRealtime()
                    if (now - lastShakeTime >= 800L) {
                        lastShakeTime = now
                        viewModel.dismissNonPersistentOnShake()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && accelSensor != null) {
            sensorManager.registerListener(shakeListener, accelSensor, android.hardware.SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(shakeListener)
        }
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        val batteryReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == android.content.Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        val pct = (level * 100) / scale
                        viewModel.setBatteryLevel(pct)
                    }
                    val status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                    val isPlugged = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == android.os.BatteryManager.BATTERY_STATUS_FULL
                    if (settings.isCharging != isPlugged) {
                        viewModel.setCharging(isPlugged)
                    }
                }
            }
        }
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = context.registerReceiver(batteryReceiver, filter)
        if (stickyIntent != null) {
            val level = stickyIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale = stickyIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                val pct = (level * 100) / scale
                viewModel.setBatteryLevel(pct)
            }
            val status = stickyIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
            val isPlugged = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL
            viewModel.setCharging(isPlugged)
        }
        onDispose {
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = rootBackgroundColor,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rootBackgroundColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(if (isDarkMode) Color(0xFF13131A) else Color(0xFFE2E8F0))
                        .border(1.dp, animatedAccentColor.copy(alpha = 0.35f), CircleShape),
                    color = if (isDarkMode) Color(0xFF13131A) else Color(0xFFE2E8F0),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(animatedAccentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Dynamic Island",
                                    color = textPrimaryColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Live Activity & Notification Mirroring",
                                    color = textSecondaryColor,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { isSettingsExpanded = !isSettingsExpanded },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("toggle_settings_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSettingsExpanded) animatedAccentColor.copy(alpha = 0.25f) else (if (isDarkMode) Color(0xFF1E2130) else Color(0xFFCBD5E1))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (isSettingsExpanded) animatedAccentColor else textPrimaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(rootBackgroundColor)
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Phone Dynamic Island Canvas Area
            PhoneSimulatorCanvas(
                activeActivity = activeActivity,
                secondaryActivity = secondaryActivity,
                expansionMode = expansionMode,
                settings = settings,
                viewModel = viewModel
            )

            // Accessibility Overlay & Notch Calibration Card
            val isAccessibilityConnected by viewModel.isAccessibilityServiceConnected.collectAsStateWithLifecycle()
            val isHardwareCutoutDetected by viewModel.isHardwareCutoutDetected.collectAsStateWithLifecycle()
            val hardwareCutoutCenterYDp by viewModel.hardwareCutoutCenterYDp.collectAsStateWithLifecycle()
            val isAccessibilityEnabled = remember(isAccessibilityConnected) {
                viewModel.isAccessibilityServiceEnabled(context) || isAccessibilityConnected
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .testTag("accessibility_status_card"),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(animatedAccentColor.copy(alpha = 0.20f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Accessibility,
                                    contentDescription = "Accessibility Overlay",
                                    tint = animatedAccentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Accessibility Overlay & Notch Calibration",
                                    fontSize = 13.sp,
                                    color = textPrimaryColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "TYPE_ACCESSIBILITY_OVERLAY • Hardware DisplayCutout",
                                    fontSize = 10.sp,
                                    color = textSecondaryColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isAccessibilityEnabled) Color(0xFF10B981).copy(alpha = 0.20f) else Color(0xFFF59E0B).copy(alpha = 0.20f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAccessibilityEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isAccessibilityEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isAccessibilityEnabled) "Active" else "Setup Needed",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAccessibilityEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Renders flush over the hardware camera punch-hole cutout with zero jitter, and tracks window state changes (Spotify exit) for the continuous spring docking animation.",
                        fontSize = 11.sp,
                        color = textSecondaryColor,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = if (isDarkMode) Color(0xFF13131A) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHardwareCutoutDetected) "📐 Cutout: ${hardwareCutoutCenterYDp?.toInt() ?: 0}dp Y-Center" else "📐 Cutout: Auto-Calibrated",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = animatedAccentColor
                            )
                            Text(
                                text = "Zero-Jitter Compositor Flight",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = animatedAccentColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_accessibility_settings_button")
                        ) {
                            Text(
                                text = if (isAccessibilityEnabled) "Accessibility Configured" else "Enable Accessibility Service",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.simulateSpotifyExitDemo()
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, animatedAccentColor),
                            modifier = Modifier.testTag("test_spotify_exit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = animatedAccentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Test Dock Morph",
                                color = animatedAccentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Real-Time Ambient Light Sensor Auto-Theme & Appearance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Row 1: Real-Time Light Sensor Auto-Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Light Sensor Auto-Theme",
                                fontSize = 13.sp,
                                color = textPrimaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Uses device light sensor to switch dark/light frosted variants in real-time",
                                fontSize = 10.sp,
                                color = textSecondaryColor
                            )
                        }
                        Switch(
                            checked = settings.autoLightSensorTheme,
                            onCheckedChange = { viewModel.setAutoLightSensorTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = animatedAccentColor,
                                checkedTrackColor = animatedAccentColor.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color(0xFF8E8E93),
                                uncheckedTrackColor = Color(0xFFD1D1D6)
                            ),
                            modifier = Modifier.testTag("light_sensor_auto_theme_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real-Time Ambient Lux Status Badge
                    val currentLux = settings.ambientLuxValue
                    val isLowLight = currentLux < settings.lightSensorLuxThreshold
                    Surface(
                        color = if (isDarkMode) Color(0xFF13131A) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLowLight) "💡 ${currentLux.toInt()} Lux • Dim Light" else "☀️ ${currentLux.toInt()} Lux • Bright Light",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = animatedAccentColor
                            )
                            Text(
                                text = if (isDarkMode) "Obsidian Dark Glass" else "Frosted Light Glass",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimaryColor
                            )
                        }
                    }

                    // Interactive Lux Simulation Slider (Allows manual Lux testing)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SIMULATE AMBIENT LIGHT (LUX LEVEL)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedAccentColor
                    )
                    Slider(
                        value = currentLux,
                        onValueChange = { viewModel.updateAmbientLux(it) },
                        valueRange = 0f..150f,
                        colors = SliderDefaults.colors(
                            thumbColor = animatedAccentColor,
                            activeTrackColor = animatedAccentColor,
                            inactiveTrackColor = animatedAccentColor.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.testTag("lux_simulation_slider")
                    )

                    // Manual Theme Override Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manual Surface Variant Override",
                            fontSize = 11.sp,
                            color = textSecondaryColor
                        )
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = animatedAccentColor,
                                checkedTrackColor = animatedAccentColor.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color(0xFF8E8E93),
                                uncheckedTrackColor = Color(0xFFD1D1D6)
                            )
                        )
                    }
                }
            }

            // Settings Sheet (Collapsible / Stacked)
            AnimatedVisibility(
                visible = isSettingsExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SettingsDrawer(
                    settings = settingsState,
                    viewModel = viewModel,
                    animatedAccentColor = animatedAccentColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            var showResetDialog by remember { mutableStateOf(false) }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = {
                        Text(
                            text = "Reset All Configurations?",
                            fontWeight = FontWeight.Bold,
                            color = animatedTextPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "This will restore factory default settings including offsets, pill dimensions, aura effects, and auto-theme thresholds.",
                            color = animatedTextSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.resetToFactoryDefaults()
                                showResetDialog = false
                            }
                        ) {
                            Text("Reset", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancel", color = animatedTextSecondary)
                        }
                    },
                    containerColor = if (isDarkMode) Color(0xFF1E1E28) else Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            OutlinedButton(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF453A)
                ),
                border = BorderStroke(1.dp, Color(0xFFFF453A).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("reset_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Reset Settings",
                    tint = Color(0xFFFF453A)
                )
                Spacer(Modifier.width(8.dp))
                Text("Reset All Configurations to Default", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

