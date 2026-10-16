package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IslandExpansionMode
import com.example.model.LiveActivityState
import com.example.ui.theme.FrostedGlassTokens
import com.example.ui.theme.PrimaryNeon
import com.example.viewmodel.DynamicIslandViewModel
import com.example.viewmodel.IslandSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhoneSimulatorCanvas(
    activeActivity: LiveActivityState,
    secondaryActivity: LiveActivityState?,
    expansionMode: IslandExpansionMode,
    settings: IslandSettings,
    viewModel: DynamicIslandViewModel,
    modifier: Modifier = Modifier
) {
    val currentTime = rememberTime()

    val isDarkMode = settings.isDarkMode
    val textPrimary = if (isDarkMode) Color(0xFFF0F0F2) else FrostedGlassTokens.textPrimary
    val textSecondary = if (isDarkMode) Color(0xFF8E8E96) else FrostedGlassTokens.textSecondary
    val canvasBorder = if (isDarkMode) Color(0xFF1E2130) else Color(0xFFE2E2E8)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (isDarkMode) {
                    Modifier.background(Color(0xFF0D0D11))
                } else {
                    Modifier.background(FrostedGlassTokens.cardBackground)
                }
            )
            .border(1.dp, if (isDarkMode) canvasBorder else Color.White.copy(alpha = 0.70f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preview Mode Chip Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PREVIEW:",
                    color = textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )

                PreviewChip(
                    label = "Music",
                    icon = Icons.Default.MusicNote,
                    isSelected = activeActivity is LiveActivityState.Music,
                    accentColor = PrimaryNeon,
                    onClick = { viewModel.switchToActivity("Music") }
                )

                PreviewChip(
                    label = "Strava Run",
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    isSelected = activeActivity is LiveActivityState.Fitness,
                    accentColor = Color(0xFFFC4C02),
                    onClick = { viewModel.switchToActivity("Fitness") }
                )

                PreviewChip(
                    label = "Timer",
                    icon = Icons.Default.Timer,
                    isSelected = activeActivity is LiveActivityState.Timer,
                    accentColor = PrimaryNeon,
                    onClick = { viewModel.switchToActivity("Timer") }
                )

                PreviewChip(
                    label = "Call",
                    icon = Icons.Default.Call,
                    isSelected = activeActivity is LiveActivityState.IncomingCall,
                    accentColor = PrimaryNeon,
                    onClick = { viewModel.switchToActivity("IncomingCall") }
                )

                PreviewChip(
                    label = "Nav",
                    icon = Icons.Default.Navigation,
                    isSelected = activeActivity is LiveActivityState.Navigation,
                    accentColor = PrimaryNeon,
                    onClick = { viewModel.switchToActivity("Navigation") }
                )

                PreviewChip(
                    label = "Delivery",
                    icon = Icons.Default.LocalShipping,
                    isSelected = activeActivity is LiveActivityState.Delivery,
                    accentColor = PrimaryNeon,
                    onClick = { viewModel.switchToActivity("Delivery") }
                )
            }

            // Top System Status Bar Row (Time & Battery/Wifi)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                // Time on left
                Text(
                    text = currentTime,
                    color = textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                // Dynamic Punch Hole Camera Lens dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Black, CircleShape)
                        .border(1.5.dp, if (isDarkMode) Color(0xFF1B1D28) else Color(0xFFE2E2E8), CircleShape)
                        .align(Alignment.Center)
                )

                // Status Icons on right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellular4Bar,
                        contentDescription = "Cellular",
                        tint = textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wifi",
                        tint = textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (settings.isCharging) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Charging",
                                tint = Color(0xFF34C759),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Text(
                            text = "${settings.batteryLevel}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.isCharging) Color(0xFF34C759) else textPrimary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = if (settings.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            contentDescription = "Battery",
                            tint = if (settings.isCharging) Color(0xFF34C759) else textPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Dynamic Island Overlay Pill
            Box(
                modifier = Modifier
                    .offset(y = settings.pillOffsetYDp.dp)
                    .wrapContentSize(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                DynamicIslandPill(
                    activeActivity = activeActivity,
                    secondaryActivity = secondaryActivity,
                    expansionMode = expansionMode,
                    settings = settings,
                    viewModel = viewModel
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dedicated Preview Display Card for active state
            if (activeActivity is LiveActivityState.Fitness) {
                StravaPreviewCard(
                    fitness = activeActivity,
                    isDarkMode = isDarkMode,
                    onTogglePause = { viewModel.toggleFitnessPause() }
                )
            } else {
                // Default Tap Hint Banner
                AnimatedVisibility(visible = expansionMode == IslandExpansionMode.COMPACT) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isDarkMode) {
                                    Modifier.background(Color(0xFF16161E).copy(alpha = 0.90f))
                                } else {
                                    Modifier
                                        .background(FrostedGlassTokens.cardBackground)
                                        .border(FrostedGlassTokens.border, RoundedCornerShape(16.dp))
                                }
                            )
                            .clickable { viewModel.toggleExpansion() }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("tap_hint_card")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Tap",
                                tint = PrimaryNeon,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Tap pill above to expand controls",
                                    color = textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Access live song time, skip, play/pause & activities",
                                    color = textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) accentColor else Color.White.copy(alpha = 0.20f))
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.40f),
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else FrostedGlassTokens.textPrimary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                color = if (isSelected) Color.White else FrostedGlassTokens.textPrimary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StravaPreviewCard(
    fitness: LiveActivityState.Fitness,
    isDarkMode: Boolean,
    onTogglePause: () -> Unit
) {
    val stravaOrange = Color(0xFFFC4C02)
    val textPrimary = if (isDarkMode) Color.White else FrostedGlassTokens.textPrimary
    val textSecondary = if (isDarkMode) Color(0xFF94A3B8) else FrostedGlassTokens.textSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isDarkMode) {
                    Modifier.background(Color(0xFF16161E))
                } else {
                    Modifier
                        .background(FrostedGlassTokens.cardBackground)
                        .border(FrostedGlassTokens.border, RoundedCornerShape(16.dp))
                }
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(stravaOrange.copy(alpha = 0.20f))
                        .border(1.dp, stravaOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = "Strava",
                        tint = stravaOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "STRAVA • ${fitness.activityName}",
                        color = stravaOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${fitness.effectiveDistance} • ${fitness.effectivePace} • ${fitness.effectiveHeartRate ?: 148} bpm",
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(stravaOrange)
                    .clickable { onTogglePause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (fitness.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (fitness.isPaused) "Resume" else "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberTime(): String {
    val time = remember {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        formatter.format(Date())
    }
    return time
}
