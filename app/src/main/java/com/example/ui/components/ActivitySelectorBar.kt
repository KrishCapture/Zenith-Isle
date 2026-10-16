package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.IslandExpansionMode
import com.example.model.LiveActivityState
import com.example.viewmodel.DynamicIslandViewModel
import com.example.viewmodel.IslandSettings

@Composable
fun ActivitySelectorBar(
    activeActivity: LiveActivityState,
    secondaryActivity: LiveActivityState?,
    expansionMode: IslandExpansionMode,
    viewModel: DynamicIslandViewModel,
    animatedAccentColor: Color = Color(0xFF6366F1),
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val settings by viewModel.settingsState.collectAsStateWithLifecycle(initialValue = IslandSettings())
    val isDarkMode = settings.isDarkMode

    val sheetBackgroundColor = if (isDarkMode) Color(0xFF13131A) else Color(0xFFFFFFFF)
    val textPrimaryColor = if (isDarkMode) Color(0xFFF0F0F2) else Color(0xFF19191D)
    val textSecondaryColor = if (isDarkMode) Color(0xFF8E8E96) else Color(0xFF6B6B76)
    val cardBg = if (isDarkMode) Color(0xFF1C1E2B) else Color(0xFFF1F5F9)
    val chipInactiveBg = if (isDarkMode) Color(0xFF1E2130) else Color(0xFFE2E8F0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(sheetBackgroundColor)
            .border(
                width = 1.dp,
                color = if (isDarkMode) animatedAccentColor.copy(alpha = 0.25f) else Color(0xFFE2E2E8),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(top = 10.dp, bottom = 16.dp)
    ) {
        // Drag Handle Bar
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(if (isDarkMode) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.2f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIVE ACTIVITIES",
                color = textSecondaryColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            // Split Mode Badge Toggle
            val isSplitActive = expansionMode == IslandExpansionMode.SPLIT
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSplitActive) animatedAccentColor.copy(alpha = 0.22f) else chipInactiveBg)
                    .border(
                        1.dp,
                        if (isSplitActive) animatedAccentColor else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        if (isSplitActive) viewModel.disableSplitMode() else viewModel.enableSplitMode("Timer")
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("split_mode_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = "Split",
                        tint = if (isSplitActive) animatedAccentColor else Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (isSplitActive) "Split Mode Active" else "Split Mode",
                        color = if (isSplitActive) animatedAccentColor else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Activity Chips Horizontal Scroll Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityChip(
                label = "Music Player",
                icon = Icons.Default.MusicNote,
                isSelected = activeActivity is LiveActivityState.Music,
                accentColor = animatedAccentColor,
                onClick = { viewModel.switchToActivity("Music") },
                testTag = "activity_music_chip"
            )

            ActivityChip(
                label = "Strava Run",
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                isSelected = activeActivity is LiveActivityState.Fitness,
                accentColor = Color(0xFFFC4C02),
                onClick = { viewModel.switchToActivity("Fitness") },
                testTag = "activity_fitness_chip"
            )

            ActivityChip(
                label = "Alarm",
                icon = Icons.Default.Alarm,
                isSelected = activeActivity is LiveActivityState.Alarm,
                accentColor = Color(0xFFEF4444),
                onClick = { viewModel.switchToActivity("Alarm") },
                testTag = "activity_alarm_chip"
            )

            ActivityChip(
                label = "Workout",
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                isSelected = activeActivity is LiveActivityState.Workout,
                accentColor = animatedAccentColor,
                onClick = { viewModel.switchToActivity("Workout") },
                testTag = "activity_workout_chip"
            )

            ActivityChip(
                label = "Timer",
                icon = Icons.Default.Timer,
                isSelected = activeActivity is LiveActivityState.Timer,
                accentColor = animatedAccentColor,
                onClick = { viewModel.switchToActivity("Timer") },
                testTag = "activity_timer_chip"
            )

            ActivityChip(
                label = "Navigation",
                icon = Icons.Default.Navigation,
                isSelected = activeActivity is LiveActivityState.Navigation,
                accentColor = animatedAccentColor,
                onClick = { viewModel.switchToActivity("Navigation") },
                testTag = "activity_nav_chip"
            )

            ActivityChip(
                label = "Food Delivery",
                icon = Icons.Default.LocalShipping,
                isSelected = activeActivity is LiveActivityState.Delivery,
                accentColor = animatedAccentColor,
                onClick = { viewModel.switchToActivity("Delivery") },
                testTag = "activity_delivery_chip"
            )

            ActivityChip(
                label = "Phone Call",
                icon = Icons.Default.Call,
                isSelected = activeActivity is LiveActivityState.IncomingCall,
                accentColor = animatedAccentColor,
                onClick = { viewModel.switchToActivity("IncomingCall") },
                testTag = "activity_call_chip"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Sheet Interactive Controls Card ("COME UP LIKE MUSIC PLAYER")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF181822)),
            shape = RoundedCornerShape(18.dp)
        ) {
            AnimatedContent(
                targetState = activeActivity,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "bottom_sheet_controls_transition"
            ) { activity ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    when (activity) {
                        is LiveActivityState.Music -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AlbumArtDisplay(
                                        song = activity.song,
                                        accentColor = animatedAccentColor,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = activity.song.title,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = activity.song.artist,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(onClick = { viewModel.skipPreviousSong() }) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "Previous",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(animatedAccentColor)
                                            .clickable { viewModel.togglePlayPause() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (activity.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (activity.isPlaying) "Pause" else "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.skipNextSong() }) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                                        Icon(
                                            imageVector = if (activity.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (activity.isFavorite) animatedAccentColor else Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        is LiveActivityState.Fitness -> {
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
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFC4C02).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                            contentDescription = "Strava",
                                            tint = Color(0xFFFC4C02),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Strava • ${activity.activityName}",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${activity.distanceText} • ${activity.paceText} • ${activity.heartRate} bpm",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFC4C02))
                                        .clickable { viewModel.toggleFitnessPause() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (activity.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = "Toggle Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        is LiveActivityState.Alarm -> {
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
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = "Alarm",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = activity.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${activity.timeText} • ${if (activity.isRinging) "Ringing" else "Active"}",
                                            color = Color(0xFFEF4444),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        onClick = { viewModel.snoozeAlarm() },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                                    ) {
                                        Text(
                                            text = "Snooze",
                                            color = Color(0xFFF59E0B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                    Surface(
                                        onClick = { viewModel.dismissAlarm() },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFEF4444)
                                    ) {
                                        Text(
                                            text = "Dismiss",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        is LiveActivityState.Workout -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(animatedAccentColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                            contentDescription = null,
                                            tint = animatedAccentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = activity.activityName,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${activity.distanceKm} km • ${activity.calories} kcal • ${activity.heartRate} bpm",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    onClick = { viewModel.toggleWorkoutPause() },
                                    shape = RoundedCornerShape(20.dp),
                                    color = animatedAccentColor.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, animatedAccentColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (activity.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                            contentDescription = "Pause",
                                            tint = animatedAccentColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (activity.isPaused) "Resume" else "Pause",
                                            color = animatedAccentColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        is LiveActivityState.Timer -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Timer",
                                        tint = animatedAccentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = activity.title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = String.format("%02d:%02d", activity.remainingSeconds / 60, activity.remainingSeconds % 60),
                                            color = animatedAccentColor,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { viewModel.addTimerMinute() },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF262838))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "+1 min",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.resetTimer() },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF262838))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Reset",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(animatedAccentColor)
                                            .clickable { viewModel.toggleTimerRun() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (activity.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Toggle",
                                            tint = Color.Black,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        is LiveActivityState.Navigation -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Nav",
                                        tint = animatedAccentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = activity.currentInstruction,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${activity.distanceText} • ETA ${activity.etaMinutes} mins",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        is LiveActivityState.Delivery -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = "Delivery",
                                        tint = animatedAccentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = activity.restaurantName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = activity.statusText,
                                            color = animatedAccentColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        is LiveActivityState.IncomingCall -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = animatedAccentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = activity.contactName,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = activity.subtext,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!activity.isAnswered) {
                                        Surface(
                                            onClick = { viewModel.answerCall() },
                                            shape = CircleShape,
                                            color = Color(0xFF10B981)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "Answer",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .size(18.dp)
                                            )
                                        }
                                    }
                                    Surface(
                                        onClick = { viewModel.endCall() },
                                        shape = CircleShape,
                                        color = Color(0xFFEF4444)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CallEnd,
                                            contentDescription = "End Call",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select an activity to view controls",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
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
private fun ActivityChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.22f) else Color(0xFF1E2130),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, accentColor) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) accentColor else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
