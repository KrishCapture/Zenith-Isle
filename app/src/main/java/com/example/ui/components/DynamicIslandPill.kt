package com.example.ui.components

import com.example.ui.theme.FrostedGlassTokens
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.NavigationDirection
import com.example.model.VisualizerMode
import com.example.model.IslandThemeMode
import com.example.viewmodel.AuraLightingEffect
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.model.IslandExpansionMode
import com.example.model.LiveActivityState
import com.example.model.Song
import com.example.viewmodel.DynamicIslandViewModel
import com.example.viewmodel.IslandSettings
import com.example.viewmodel.MediaDisplayMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicIslandPill(
    activeActivity: LiveActivityState,
    secondaryActivity: LiveActivityState?,
    expansionMode: IslandExpansionMode,
    settings: IslandSettings,
    viewModel: DynamicIslandViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeActivitiesList by viewModel.activeActivitiesList.collectAsStateWithLifecycle()
    val activities = if (activeActivitiesList.isNotEmpty()) activeActivitiesList else listOf(activeActivity)
    val pagerState = rememberPagerState(pageCount = { activities.size })
    val currentActivity = activities.getOrNull(pagerState.currentPage) ?: activeActivity

    LaunchedEffect(pagerState.currentPage) {
        val pageActivity = activities.getOrNull(pagerState.currentPage)
        if (pageActivity != null) {
            com.example.util.SoundFeedbackManager.playActivitySwitchSound(pageActivity)
        }
    }

    val isExpanded = expansionMode == IslandExpansionMode.EXPANDED
    val isHidden = expansionMode == IslandExpansionMode.HIDDEN
    val isSettling by com.example.viewmodel.GlobalIslandState.isSettling.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val pillOffsetYDp by viewModel.pillOffsetYDp.collectAsStateWithLifecycle()
    val topCutoutYOffsetPx = with(density) { pillOffsetYDp.dp.toPx() }

    // Center-to-Notch Flight State & Animatable
    val flightTriggerTime by viewModel.flightTrigger.collectAsStateWithLifecycle()
    val flightProgress = remember { Animatable(1f) }
    var isFlightInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(flightTriggerTime) {
        if (flightTriggerTime > 0L) {
            isFlightInProgress = true
            viewModel.setFlightActive(true)

            // Drive entire 3-phase sequence via single Animatable with spring damping=0.78, stiffness=340
            flightProgress.snapTo(0f)
            flightProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 340f)
            )

            isFlightInProgress = false
            viewModel.setFlightActive(false)
        }
    }

    val progressVal = flightProgress.value
    val flightPhase1 = (progressVal / 0.75f).coerceIn(0f, 1f)
    val flightPhase2 = ((progressVal - 0.75f) / 0.25f).coerceIn(0f, 1f)

    // Phase 1 (0.0f -> 0.75f): Center of screen trajectory straight up to 0f
    val startTranslationY = ((screenHeightPx / 2f) - topCutoutYOffsetPx).coerceAtLeast(0f)
    val currentFlightTranslationY = if (isFlightInProgress) {
        (1f - flightPhase1) * startTranslationY
    } else 0f

    val currentFlightScale = if (isFlightInProgress) {
        1.15f - (0.15f * flightPhase1)
    } else 1.0f

    // Width: 220.dp narrows to 76.dp capsule, then springs horizontally to docked width
    val targetDockedWidth = if (isSettling) 120.dp else settings.pillWidthDp.dp
    val flightWidthDp = if (progressVal <= 0.75f) {
        androidx.compose.ui.unit.lerp(220.dp, 76.dp, flightPhase1)
    } else {
        androidx.compose.ui.unit.lerp(76.dp, targetDockedWidth, flightPhase2)
    }

    // Height: 68.dp morphs to 36.dp
    val flightHeightDp = if (progressVal <= 0.75f) {
        androidx.compose.ui.unit.lerp(68.dp, 36.dp, flightPhase1)
    } else {
        settings.pillHeightDp.dp
    }

    // Corner radius: 28.dp down to 18.dp, then latches to 28.dp
    val flightCornerRadiusDp = if (progressVal <= 0.75f) {
        androidx.compose.ui.unit.lerp(28.dp, 18.dp, flightPhase1)
    } else {
        androidx.compose.ui.unit.lerp(18.dp, 28.dp, flightPhase2)
    }

    val flightContentAlpha = if (isFlightInProgress) flightPhase2 else 1.0f

    val targetWidth: Dp = when {
        isHidden -> 0.dp
        isFlightInProgress -> flightWidthDp
        isExpanded -> when (currentActivity) {
            is LiveActivityState.Music -> 340.dp
            is LiveActivityState.Fitness -> 320.dp
            is LiveActivityState.Alarm -> 310.dp
            is LiveActivityState.Workout -> 300.dp
            is LiveActivityState.Timer -> 280.dp
            is LiveActivityState.IncomingCall -> 310.dp
            is LiveActivityState.Notification -> 320.dp
            else -> 280.dp
        }
        isSettling -> 120.dp
        else -> settings.pillWidthDp.dp
    }

    val targetHeight: Dp = when {
        isHidden -> 0.dp
        isFlightInProgress -> flightHeightDp
        isExpanded -> when (currentActivity) {
            is LiveActivityState.Music -> 180.dp
            is LiveActivityState.Fitness -> 160.dp
            is LiveActivityState.Alarm -> 130.dp
            is LiveActivityState.Workout -> 100.dp
            is LiveActivityState.Timer -> 100.dp
            is LiveActivityState.IncomingCall -> 100.dp
            is LiveActivityState.Notification -> 130.dp
            else -> 90.dp
        }
        else -> settings.pillHeightDp.dp
    }

    // Conditional animation spec based on settings.isSpringEnabled
    val dpMorphSpec: AnimationSpec<Dp> = if (settings.isSpringEnabled) {
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    } else {
        tween(durationMillis = 300, easing = FastOutSlowInEasing)
    }

    val floatMorphSpec: AnimationSpec<Float> = if (settings.isSpringEnabled) {
        spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    } else {
        tween(durationMillis = 300, easing = FastOutSlowInEasing)
    }

    val dockExpansionSpec: AnimationSpec<Dp> = if (isFlightInProgress) {
        spring(dampingRatio = 0.75f, stiffness = 400f)
    } else if (isSettling) {
        spring(dampingRatio = 0.75f, stiffness = 420f)
    } else dpMorphSpec

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = dockExpansionSpec,
        label = "DockExpansion"
    )
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = dpMorphSpec,
        label = "pill_height"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isHidden) 0f else 1f,
        animationSpec = floatMorphSpec,
        label = "pill_alpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isHidden) 0.5f else 1f,
        animationSpec = floatMorphSpec,
        label = "pill_scale"
    )

    val isDarkMode = settings.isDarkMode
    val targetSurfaceColor = if (isDarkMode) Color(0xFF0D0D11).copy(alpha = 0.95f) else Color.Transparent
    val animatedSurfaceColor by animateColorAsState(
        targetValue = targetSurfaceColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "pill_surface_color"
    )

    val targetBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.65f)
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "pill_border_color"
    )

    val isBreathingGlow = settings.isBreathingGlowEnabled && !isHidden
    val isEdgeSheen = settings.isEdgeSheenEnabled
    val isBeatPulse = settings.isBeatPulseEnabled
    val auraIntensity = settings.auraIntensity.coerceIn(0.15f, 0.80f)

    // A. Dual-Phase Organic Breathing Halo
    val breathingTransition = rememberInfiniteTransition(label = "BreathingHalo")
    val haloScale by breathingTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )
    val haloAlpha by breathingTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloAlpha"
    )

    // B. Sonic Beat / Event Shockwave Ping Animatable
    val pingAnimatable = remember { Animatable(0f) }
    val sonicPingTriggerKey = remember(currentActivity) {
        when (currentActivity) {
            is LiveActivityState.Music -> currentActivity.song.title
            is LiveActivityState.Notification -> "${currentActivity.appName}_${currentActivity.title}"
            is LiveActivityState.Fitness -> "${currentActivity.activityName}_${currentActivity.effectiveDistance}"
            is LiveActivityState.Workout -> "${currentActivity.activityName}_${currentActivity.distanceKm}"
            else -> currentActivity::class.java.simpleName
        }
    }

    LaunchedEffect(sonicPingTriggerKey) {
        if (isBeatPulse) {
            pingAnimatable.snapTo(0f)
            pingAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutLinearInEasing)
            )
        }
    }

    // C. Specular Glass Edge Sheen Shimmer
    val shimmerOffset by breathingTransition.animateFloat(
        initialValue = -250f,
        targetValue = 700f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, delayMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BorderSheen"
    )

    val specularBorderBrush = if (isEdgeSheen) {
        Brush.linearGradient(
            colors = listOf(
                animatedBorderColor,
                Color.White.copy(alpha = if (isDarkMode) 0.65f else 0.85f),
                animatedBorderColor
            ),
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 140f, 100f)
        )
    } else {
        SolidColor(animatedBorderColor)
    }

    val surfaceColor = animatedSurfaceColor
    val surfaceBorder = BorderStroke(1.dp, specularBorderBrush)
    val morphingShape = RoundedCornerShape(if (isFlightInProgress) flightCornerRadiusDp else (if (isExpanded) 20.dp else 28.dp))

    val currentActivityAccentColor = remember(currentActivity, settings.themeMode, settings.customAccentColorLong) {
        DynamicIslandViewModel.getActivityAccentColor(currentActivity, settings.themeMode, settings.customAccentColorLong)
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = currentActivityAccentColor,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "SmoothColorDissolve"
    )

    val expansionFraction by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "expansion_fraction"
    )

    val activeProgress = when (val act = currentActivity) {
        is LiveActivityState.Music -> {
            val validDuration = act.song.durationMs.coerceAtLeast(1000L)
            val interpolatedPositionMs = com.example.viewmodel.GlobalIslandState.calculateCurrentPosition(
                basePosition = act.positionMs,
                lastPositionUpdateTime = act.lastPositionUpdateTime,
                playbackSpeed = act.playbackSpeed,
                isPlaying = act.isPlaying,
                durationMs = validDuration
            )
            (interpolatedPositionMs.toFloat() / validDuration.toFloat()).coerceIn(0.0f, 1.0f)
        }
        is LiveActivityState.Timer -> {
            if (act.totalSeconds > 0) {
                ((act.totalSeconds - act.remainingSeconds).toFloat() / act.totalSeconds.toFloat()).coerceIn(0f, 1f)
            } else 0f
        }
        is LiveActivityState.Delivery -> act.progressPercent.coerceIn(0f, 1f)
        else -> 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = activeProgress,
        animationSpec = tween(durationMillis = 250, easing = LinearEasing),
        label = "EdgeProgress"
    )

    // D. Asymmetric Color Dissolve State
    var previousAccentColor by remember { mutableStateOf(currentActivityAccentColor) }
    var targetAccentColor by remember { mutableStateOf(currentActivityAccentColor) }

    val dissolveAnimatable = remember { Animatable(1f) }

    LaunchedEffect(currentActivityAccentColor) {
        if (currentActivityAccentColor != targetAccentColor) {
            previousAccentColor = targetAccentColor
            targetAccentColor = currentActivityAccentColor
            dissolveAnimatable.snapTo(0f)
            dissolveAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    val animatedGlowAlpha by animateFloatAsState(
        targetValue = if (isDarkMode) 0.45f else 0.22f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "GlowAlphaTransition"
    )

    // 1. Root Box that holds everything, allowing bleed space
    Box(
        modifier = modifier
            .wrapContentSize(Alignment.Center) // Let it grow to fit the padding
            .graphicsLayer {
                translationY = currentFlightTranslationY
                scaleX = currentFlightScale
                scaleY = currentFlightScale
            }
            .padding(16.dp), // This 16.dp padding acts as the unclipped bleed area for the blur
        contentAlignment = Alignment.Center
    ) {
        // 2. The Multi-Style Ambient Glow Layer
        if (isBreathingGlow) {
            val baseIntensity = auraIntensity * (animatedGlowAlpha / 0.45f)
            val blurPx = (auraIntensity * 48f).coerceIn(12f, 48f)

            // Asymmetric Dissolve Underlayer (Old Color contracting)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val progress = dissolveAnimatable.value
                        val oldAlpha = (1f - progress) * baseIntensity
                        val oldScale = 1.05f - (progress * 0.10f)
                        scaleX = oldScale
                        scaleY = oldScale
                        alpha = if (progress >= 0.99f) 0f else oldAlpha
                        clip = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                blurPx, blurPx, android.graphics.Shader.TileMode.DECAL
                            ).asComposeRenderEffect()
                        } else {
                            shadowElevation = 18f
                        }
                    }
                    .background(previousAccentColor, shape = morphingShape)
            )

            // Asymmetric Dissolve Main Layer (New Incoming Color expanding)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val progress = dissolveAnimatable.value
                        val newAlpha = progress * baseIntensity
                        val newScale = 0.90f + (progress * 0.15f)
                        scaleX = haloScale * newScale
                        scaleY = haloScale * newScale
                        alpha = haloAlpha * newAlpha
                        clip = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                blurPx, blurPx, android.graphics.Shader.TileMode.DECAL
                            ).asComposeRenderEffect()
                        } else {
                            shadowElevation = 24f
                        }
                    }
                    .background(animatedAccentColor, shape = morphingShape)
            )
        }

        // Sonic Beat / Event Shockwave Ping Ring
        if (isBeatPulse) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        val pingVal = pingAnimatable.value
                        if (pingVal in 0.01f..0.99f) {
                            scaleX = 1.0f + (pingVal * 0.30f)
                            scaleY = 1.0f + (pingVal * 0.30f)
                            alpha = (1f - pingVal) * 0.65f * (auraIntensity / 0.40f)
                        } else {
                            alpha = 0f
                        }
                        clip = false
                    }
                    .border(
                        width = 2.dp,
                        color = animatedAccentColor,
                        shape = morphingShape
                    )
            )
        }

        // 3. The Core Pill Surface (Sits exactly on top of the unblurred center of the glow layer)
        Surface(
            modifier = Modifier
                .testTag("dynamic_island_pill")
                .width(if (isFlightInProgress) flightWidthDp else animatedWidth)
                .height(if (isFlightInProgress) flightHeightDp else animatedHeight)
                .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                .graphicsLayer {
                    alpha = animatedAlpha
                    scaleX = animatedScale
                    scaleY = animatedScale
                    shadowElevation = if (isExpanded) 16f else (if (isDarkMode) 0f else 4f)
                    shape = morphingShape
                    clip = true
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.onUserInteracted()
                        viewModel.toggleExpansion()
                    },
                    onLongClick = {
                        viewModel.onUserInteracted()
                        val pkg = currentActivity.originatingPackageName
                            ?: (currentActivity as? LiveActivityState.Music)?.song?.album
                        if (!pkg.isNullOrBlank()) {
                            try {
                                val intent = context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                                }
                                intent?.let { context.startActivity(it) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                ),
            shape = morphingShape,
            color = surfaceColor,
            border = surfaceBorder
        ) {
            if (!isHidden) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (animatedProgress > 0.005f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidthPx = 1.5.dp.toPx()
                            val compactRadius = size.height / 2f
                            val expandedRadius = 28.dp.toPx()
                            val currentRadius = (compactRadius + (expandedRadius - compactRadius) * expansionFraction).coerceAtMost(size.height / 2f)
                            val rect = Rect(Offset.Zero, size)

                            val fullPath = Path().apply {
                                addRoundRect(RoundRect(rect, CornerRadius(currentRadius, currentRadius)))
                            }
                            val pathMeasure = PathMeasure()
                            pathMeasure.setPath(fullPath, false)
                            val totalLen = pathMeasure.length
                            val segmentLen = totalLen * animatedProgress.coerceIn(0f, 1f)

                            if (segmentLen > 0f) {
                                val progressPath = Path()
                                pathMeasure.getSegment(0f, segmentLen, progressPath, true)

                                val dynamicProgressBrush = Brush.linearGradient(
                                    colors = listOf(
                                        animatedAccentColor.copy(alpha = 0.85f),
                                        animatedAccentColor,
                                        Color.White.copy(alpha = 0.40f)
                                    )
                                )

                                drawPath(
                                    path = progressPath,
                                    brush = dynamicProgressBrush,
                                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                )

                                val headPos = pathMeasure.getPosition(segmentLen)
                                drawCircle(
                                    color = animatedAccentColor,
                                    radius = 4.dp.toPx(),
                                    center = headPos,
                                    alpha = 0.8f
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = headPos,
                                    alpha = 0.95f
                                )
                            }
                        }
                    }

                    val lightOverlayAlpha by animateFloatAsState(
                        targetValue = if (!isDarkMode) 1f else 0f,
                        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                        label = "frosted_light_overlay_alpha"
                    )
                    if (lightOverlayAlpha > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = lightOverlayAlpha }
                                .background(FrostedGlassTokens.pillBackground)
                        )
                    }
                    if (isFlightInProgress && progressVal < 0.75f) {
                        val miniAlpha = (1f - (flightPhase1 * 1.4f)).coerceIn(0f, 1f)
                        if (miniAlpha > 0f) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = miniAlpha }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val music = currentActivity as? LiveActivityState.Music
                                if (music != null) {
                                    AlbumArtDisplay(
                                        song = music.song,
                                        accentColor = animatedAccentColor,
                                        modifier = Modifier.size(44.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(animatedAccentColor.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = "Music",
                                            tint = animatedAccentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = music?.song?.title ?: "Playing Music",
                                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isDarkMode) Color.White else Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = music?.song?.artist ?: "Spotify",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        color = (if (isDarkMode) Color.White else Color.Black).copy(alpha = 0.65f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                WaveformEqualizer(
                                    isPlaying = music?.isPlaying ?: true,
                                    accentColor = animatedAccentColor,
                                    modifier = Modifier.size(width = 24.dp, height = 20.dp)
                                )
                            }
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = if (isFlightInProgress) flightContentAlpha else 1.0f
                            }
                    ) { pageIndex ->
                        val activity = activities.getOrNull(pageIndex) ?: currentActivity
                        val itemAccentColor = remember(activity, settings.themeMode, settings.customAccentColorLong) {
                            DynamicIslandViewModel.getActivityAccentColor(activity, settings.themeMode, settings.customAccentColorLong)
                        }
                        Crossfade(
                            targetState = isDarkMode,
                            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                            label = "theme_crossfade"
                        ) { _ ->
                            if (isExpanded) {
                                ExpandedContent(activity, viewModel, itemAccentColor, settings)
                            } else {
                                CompactContent(activity, settings, itemAccentColor, viewModel)
                            }
                        }
                    }

                    if (activities.size > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp)
                        ) {
                            activities.forEachIndexed { index, activity ->
                                val isSelected = index == pagerState.currentPage
                                val targetColor = if (isSelected) {
                                    DynamicIslandViewModel.getActivityAccentColor(activity, settings.themeMode, settings.customAccentColorLong)
                                } else {
                                    if (isDarkMode) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.18f)
                                }
                                val dotColor by animateColorAsState(
                                    targetValue = targetColor,
                                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                    label = "DotColorAnimation"
                                )

                                val targetWidth = if (isSelected) 14.dp else 4.dp
                                val animatedWidth by animateDpAsState(
                                    targetValue = targetWidth,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                    label = "DotWidthAnimation"
                                )

                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(animatedWidth)
                                        .clip(CircleShape)
                                        .background(dotColor)
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
fun PulsingHeartIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFFFF3B30)) {
    val infiniteTransition = rememberInfiniteTransition(label = "heart_beat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_pulse"
    )
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Live Heart Rate",
        tint = tint,
        modifier = modifier.scale(scale)
    )
}

@Composable
fun AlbumArtDisplay(
    song: Song,
    accentColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(accentColor.copy(alpha = 0.25f))
            .border(0.5.dp, accentColor.copy(alpha = 0.5f), shape),
        contentAlignment = Alignment.Center
    ) {
        val cover = song.coverBitmap
        val imageBitmap = remember(cover) {
            if (cover != null && !cover.isRecycled) {
                try {
                    cover.asImageBitmap()
                } catch (e: Throwable) {
                    null
                }
            } else {
                null
            }
        }

        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Album Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (song.coverArtUrl.isNotBlank()) {
            AsyncImage(
                model = song.coverArtUrl,
                contentDescription = "Album Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = rememberVectorPainter(Icons.Default.MusicNote),
                placeholder = rememberVectorPainter(Icons.Default.MusicNote)
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }
}

@Composable
fun WaveformEqualizer(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 4
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val initialHeight = 6f + (index * 3) % 8
            val targetHeight = 14f + (index * 5) % 10
            val animState = infiniteTransition.animateFloat(
                initialValue = initialHeight,
                targetValue = targetHeight,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 300 + index * 120, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_height_$index"
            )
            val currentHeightDp = if (isPlaying) animState.value.dp else 4.dp
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(currentHeightDp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
        }
    }
}

@Composable
fun FluidPulseVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_pulse_anim")
    val pulseScale by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        rememberUpdatedState(1.0f)
    }

    Box(
        modifier = modifier.size(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(pulseScale)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = if (isPlaying) 0.35f else 0.15f))
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
    }
}

@Composable
fun MinimalDotsVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    dotCount: Int = 3
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_anim")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val animScale = if (isPlaying) {
                infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400 + index * 150, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_scale_$index"
                ).value
            } else {
                0.8f
            }

            Box(
                modifier = Modifier
                    .size(5.dp)
                    .scale(animScale)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
    }
}

@Composable
fun MediaVisualizerHost(
    mode: VisualizerMode,
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 3
) {
    when (mode) {
        VisualizerMode.EQUALIZER_BARS -> WaveformEqualizer(isPlaying, accentColor, modifier, barCount)
        VisualizerMode.FLUID_PULSE -> FluidPulseVisualizer(isPlaying, accentColor, modifier)
        VisualizerMode.MINIMAL_DOTS -> MinimalDotsVisualizer(isPlaying, accentColor, modifier, barCount)
        VisualizerMode.OFF -> {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = accentColor,
                modifier = modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CallChronometerTimerText(
    callStartTimeMs: Long,
    textColor: Color,
    fontSize: TextUnit = 12.sp,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember(callStartTimeMs) {
        val initial = if (callStartTimeMs in 1..System.currentTimeMillis()) {
            (System.currentTimeMillis() - callStartTimeMs) / 1000L
        } else {
            0L
        }
        mutableLongStateOf(maxOf(0L, initial))
    }

    LaunchedEffect(callStartTimeMs) {
        while (true) {
            delay(1000L)
            val current = if (callStartTimeMs in 1..System.currentTimeMillis()) {
                (System.currentTimeMillis() - callStartTimeMs) / 1000L
            } else {
                elapsedSeconds + 1
            }
            elapsedSeconds = maxOf(0L, current)
        }
    }

    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val hours = elapsedSeconds / 3600

    val formatted = if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }

    Text(
        text = "Active Call • $formatted",
        color = textColor,
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
    )
}

@Composable
fun LiveTimerText(
    targetEndTimeMs: Long,
    fallbackSeconds: Long,
    isRunning: Boolean,
    fontSize: TextUnit,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by remember(targetEndTimeMs, fallbackSeconds, isRunning) {
        val calculated = if (targetEndTimeMs > System.currentTimeMillis()) {
            (targetEndTimeMs - System.currentTimeMillis()) / 1000L
        } else {
            fallbackSeconds
        }
        mutableLongStateOf(maxOf(0L, calculated))
    }

    LaunchedEffect(targetEndTimeMs, fallbackSeconds, isRunning) {
        if (isRunning) {
            while (remainingSeconds > 0) {
                delay(1000L)
                val currentRemaining = if (targetEndTimeMs > System.currentTimeMillis()) {
                    (targetEndTimeMs - System.currentTimeMillis()) / 1000L
                } else {
                    remainingSeconds - 1
                }
                remainingSeconds = maxOf(0L, currentRemaining)
            }
        }
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    Text(
        text = "%02d:%02d".format(minutes, seconds),
        color = textColor,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
    )
}

@Composable
fun LiveWorkoutTimerText(
    startTimeMs: Long,
    fallbackSeconds: Long,
    isPaused: Boolean,
    staticDisplayTime: String? = null,
    fontSize: TextUnit,
    textColor: Color,
    showPausedBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    var elapsedSeconds by remember(startTimeMs, fallbackSeconds, isPaused) {
        val calculated = if (isPaused) {
            fallbackSeconds
        } else if (startTimeMs in 1..System.currentTimeMillis()) {
            (System.currentTimeMillis() - startTimeMs) / 1000L
        } else {
            fallbackSeconds
        }
        mutableLongStateOf(maxOf(0L, calculated))
    }

    LaunchedEffect(startTimeMs, fallbackSeconds, isPaused) {
        if (!isPaused) {
            while (true) {
                delay(1000L)
                val current = if (startTimeMs in 1..System.currentTimeMillis()) {
                    (System.currentTimeMillis() - startTimeMs) / 1000L
                } else {
                    elapsedSeconds + 1
                }
                elapsedSeconds = maxOf(0L, current)
            }
        }
    }

    val displayFormatted = if (isPaused && !staticDisplayTime.isNullOrBlank()) {
        staticDisplayTime
    } else {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (isPaused && showPausedBadge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFFC107).copy(alpha = 0.2f))
                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = "PAUSED",
                    color = Color(0xFFFFC107),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = displayFormatted,
            color = if (isPaused) Color(0xFFFFC107) else textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ExpandedContent(
    activity: LiveActivityState,
    viewModel: DynamicIslandViewModel,
    accentColor: Color,
    settings: IslandSettings
) {
    val context = LocalContext.current
    val isDarkMode = settings.isDarkMode
    val targetPrimaryTextColor = if (isDarkMode) Color(0xFFF0F0F2) else FrostedGlassTokens.textPrimary
    val targetSecondaryTextColor = if (isDarkMode) Color(0xFF8E8E96) else FrostedGlassTokens.textSecondary
    val primaryTextColor by animateColorAsState(
        targetValue = targetPrimaryTextColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "expanded_primary_text"
    )
    val secondaryTextColor by animateColorAsState(
        targetValue = targetSecondaryTextColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "expanded_secondary_text"
    )
    val isDeviceLocked by viewModel.isDeviceLocked.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        when (activity) {
            is LiveActivityState.Music -> {
                val displayMode = settings.mediaDisplayMode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (displayMode == MediaDisplayMode.BOTH || displayMode == MediaDisplayMode.COVER_ONLY) {
                        AlbumArtDisplay(
                            song = activity.song,
                            accentColor = accentColor,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(52.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(
                            text = activity.song.title,
                            color = primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activity.song.artist,
                            color = secondaryTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (displayMode == MediaDisplayMode.BOTH || displayMode == MediaDisplayMode.WAVEFORM_ONLY) {
                            MediaVisualizerHost(
                                mode = settings.visualizerMode,
                                isPlaying = activity.isPlaying,
                                accentColor = accentColor,
                                barCount = 3,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .testTag("snooze_video_action_chip")
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { viewModel.snoozeCurrentVideo() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide for this video",
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Hide",
                                    color = primaryTextColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DecoupledPlaybackTimeDisplay(
                    basePositionMs = activity.positionMs,
                    lastPositionUpdateTime = activity.lastPositionUpdateTime,
                    playbackSpeed = activity.playbackSpeed,
                    totalTimeMs = activity.song.durationMs,
                    accentColor = accentColor,
                    primaryTextColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    isPlaying = activity.isPlaying
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (activity.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (activity.isFavorite) accentColor else secondaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.skipPreviousSong(context) }) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.togglePlayPause(context) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    ) {
                        Icon(
                            imageVector = if (activity.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (activity.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.skipNextSong(context) }) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (activity.isShuffle) accentColor else secondaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            is LiveActivityState.Fitness -> {
                // Fitness Workout Dashboard with Live Heart Rate
                val fitnessColor = Color(activity.brandColorLong)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(fitnessColor.copy(alpha = 0.2f))
                                .border(1.dp, fitnessColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                contentDescription = activity.appName,
                                tint = fitnessColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${activity.appName.uppercase()} • ${activity.activityName.uppercase()}",
                                color = fitnessColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (activity.isPaused) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFFFC107).copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "PAUSED",
                                            color = Color(0xFFFFC107),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Recording Active",
                                        color = secondaryTextColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    LiveWorkoutTimerText(
                        startTimeMs = activity.startTimeMs,
                        fallbackSeconds = activity.elapsedSeconds,
                        isPaused = activity.isPaused,
                        staticDisplayTime = activity.staticDisplayTime,
                        fontSize = 20.sp,
                        textColor = if (activity.isPaused) Color(0xFFFFC107) else primaryTextColor,
                        showPausedBadge = false
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val hrValue = activity.effectiveHeartRate

                // Stats row: Distance | Pace | Heart Rate (if connected)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DISTANCE",
                            color = secondaryTextColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activity.effectiveDistance,
                            color = primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PACE",
                            color = secondaryTextColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activity.effectivePace,
                            color = primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (hrValue != null) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PulsingHeartIcon(
                                    modifier = Modifier.size(11.dp),
                                    tint = Color(0xFFFF3B30)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "HEART RATE",
                                    color = secondaryTextColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$hrValue bpm",
                                color = Color(0xFFFF3B30),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { viewModel.toggleFitnessPause() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(fitnessColor.copy(alpha = 0.25f))
                    ) {
                        Icon(
                            imageVector = if (activity.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (activity.isPaused) "Resume" else "Pause",
                            tint = fitnessColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            is LiveActivityState.Alarm -> {
                // Interactive Alarm Card with Dismiss & Snooze
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFFEF4444), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Alarm",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = activity.title,
                                color = primaryTextColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (activity.isRinging) "Alarm Firing • Ringing" else "Upcoming Alarm",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = activity.timeText,
                        color = primaryTextColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.snoozeAlarm() },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("snooze_alarm_button"),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B))
                    ) {
                        Text("Snooze (10m)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.dismissAlarm() },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("dismiss_alarm_button"),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Dismiss", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            is LiveActivityState.Timer -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                    LiveTimerText(
                        targetEndTimeMs = activity.targetEndTimeMs,
                        fallbackSeconds = activity.remainingSeconds,
                        isRunning = activity.isRunning,
                        fontSize = 28.sp,
                        textColor = primaryTextColor
                    )
                    IconButton(
                        onClick = { viewModel.toggleTimerRun() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (activity.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (activity.isRunning) "Pause" else "Start",
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.resetTimer() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            is LiveActivityState.Notification -> {
                // Expanded Notification Drawer with Sensitive Data Masking
                val shouldMaskContent = activity.isSensitive && settings.hideSensitiveContent && isDeviceLocked

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (shouldMaskContent) Color(0xFFEF4444).copy(alpha = 0.25f)
                                    else activity.appBrandColor.copy(alpha = 0.25f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (shouldMaskContent) Icons.Default.Lock else Icons.Default.Notifications,
                                contentDescription = "App Icon",
                                tint = if (shouldMaskContent) Color(0xFFEF4444) else activity.appBrandColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activity.appName,
                            color = primaryTextColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.dismissNotification() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = activity.title,
                    color = if (shouldMaskContent) primaryTextColor else activity.appBrandColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                AnimatedContent(
                    targetState = shouldMaskContent,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "expanded_notification_body"
                ) { isMasked ->
                    if (isMasked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Protected",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sensitive Notification • Unlock device to view",
                                color = secondaryTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        val displayBody = when {
                            activity.maskedText.isNotBlank() && activity.maskedText != activity.rawText -> activity.maskedText
                            activity.maskedText.isNotBlank() -> activity.maskedText
                            else -> activity.originalBody.ifEmpty { activity.body }
                        }
                        Text(
                            text = displayBody,
                            color = secondaryTextColor,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val validActions = activity.actions.filter { !it.title.isNullOrBlank() && it.intent != null }
                if (!shouldMaskContent && validActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        validActions.take(3).forEach { action ->
                            Button(
                                onClick = {
                                    action.intent?.let {
                                        try { it.send() } catch (e: Exception) { e.printStackTrace() }
                                    }
                                    viewModel.dismissNotification()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 32.dp)
                                    .height(32.dp),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                                    contentColor = if (isDarkMode) Color.White else Color.Black
                                )
                            ) {
                                Text(
                                    text = action.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            is LiveActivityState.Workout -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = activity.activityName,
                            color = primaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activity.calories} kcal • ${activity.distanceKm} km",
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleWorkoutPause() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = if (activity.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (activity.isPaused) "Resume" else "Pause",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            is LiveActivityState.IncomingCall -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activity.contactName,
                            color = primaryTextColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (activity.isAnswered) {
                            CallChronometerTimerText(
                                callStartTimeMs = activity.callStartTimeMs,
                                textColor = secondaryTextColor,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = activity.subtext,
                                color = secondaryTextColor,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!activity.isAnswered) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Decline Button (Red)
                            IconButton(
                                onClick = { viewModel.endCall(context) },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), CircleShape)
                                    .testTag("reject_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Decline Call",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            // Answer Button (Green)
                            IconButton(
                                onClick = { viewModel.answerCall(context) },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.6f), CircleShape)
                                    .testTag("answer_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Answer Call",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // End Call Button (Red)
                        IconButton(
                            onClick = { viewModel.endCall(context) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), CircleShape)
                                .testTag("end_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            is LiveActivityState.Navigation -> {
                val navColor = Color(0xFF00E5FF)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(navColor.copy(alpha = 0.2f))
                                .border(1.dp, navColor.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (activity.iconType) {
                                    NavigationDirection.ROUNDABOUT, NavigationDirection.U_TURN -> Icons.Default.Sync
                                    else -> Icons.Default.Navigation
                                },
                                contentDescription = "Turn Instruction",
                                tint = navColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activity.destination,
                                color = primaryTextColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Google Maps • Navigation",
                                color = secondaryTextColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(navColor.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${activity.etaMinutes} min ETA",
                            color = navColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activity.currentInstruction,
                            color = primaryTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "In ${activity.distanceText}",
                            color = navColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            is LiveActivityState.Delivery -> {
                val deliveryColor = if (activity.originatingPackageName?.contains("zomato") == true) Color(0xFFE23744)
                else if (activity.originatingPackageName?.contains("swiggy") == true) Color(0xFFFC8019)
                else Color(0xFF10B981)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(deliveryColor.copy(alpha = 0.2f))
                                .border(1.dp, deliveryColor.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Delivery",
                                tint = deliveryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activity.restaurantName,
                                color = primaryTextColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = activity.statusText,
                                color = secondaryTextColor,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Order Progress", color = secondaryTextColor, fontSize = 11.sp)
                        Text(text = "${(activity.progressPercent * 100).toInt()}%", color = deliveryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(activity.progressPercent)
                                .clip(RoundedCornerShape(3.dp))
                                .background(deliveryColor)
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dynamic Island Active",
                        color = primaryTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DiscreetBatteryIndicator(
    batteryLevel: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
    showPercentageText: Boolean = true
) {
    val levelColor = when {
        isCharging -> Color(0xFF34C759)
        batteryLevel <= 15 -> Color(0xFFEF4444)
        batteryLevel <= 30 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val animatedColor by animateColorAsState(
        targetValue = levelColor,
        animationSpec = tween(300),
        label = "battery_color_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "battery_pulse")
    val boltAlpha by if (isCharging) {
        infiniteTransition.animateFloat(
            initialValue = 0.40f,
            targetValue = 1.00f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bolt_pulse_alpha"
        )
    } else {
        rememberUpdatedState(1.0f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(0.5.dp, animatedColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        if (isCharging) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Charging Bolt",
                tint = animatedColor,
                modifier = Modifier
                    .size(11.dp)
                    .graphicsLayer { alpha = boltAlpha }
            )
        }

        // Battery Capsule Container
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(9.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, animatedColor.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                .padding(1.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val fillFraction = (batteryLevel / 100f).coerceIn(0.08f, 1.0f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fillFraction)
                    .clip(RoundedCornerShape(1.dp))
                    .background(animatedColor)
            )
        }

        if (showPercentageText) {
            Text(
                text = "$batteryLevel%",
                color = animatedColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-0.3).sp
            )
        }
    }
}

@Composable
fun CompactContent(
    activity: LiveActivityState,
    settings: IslandSettings,
    accentColor: Color,
    viewModel: DynamicIslandViewModel
) {
    val context = LocalContext.current
    val targetPrimaryTextColor = if (settings.isDarkMode) Color(0xFFF0F0F2) else FrostedGlassTokens.textPrimary
    val targetSecondaryTextColor = if (settings.isDarkMode) Color(0xFF8E8E96) else FrostedGlassTokens.textSecondary
    val primaryTextColor by animateColorAsState(
        targetValue = targetPrimaryTextColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "compact_primary_text"
    )
    val secondaryTextColor by animateColorAsState(
        targetValue = targetSecondaryTextColor,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "compact_secondary_text"
    )
    val isDeviceLocked by viewModel.isDeviceLocked.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        when (activity) {
            is LiveActivityState.Music -> {
                AnimatedContent(
                    targetState = activity.song.id,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "MusicTrackCrossfade"
                ) { _ ->
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val displayMode = settings.mediaDisplayMode

                        // Leading Slot
                        if (displayMode == MediaDisplayMode.BOTH || displayMode == MediaDisplayMode.COVER_ONLY) {
                            AlbumArtDisplay(
                                song = activity.song,
                                accentColor = accentColor,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Center Title
                        if (settings.showCompactSongTitle || displayMode == MediaDisplayMode.COVER_ONLY) {
                            Text(
                                text = activity.song.title,
                                color = primaryTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = (-0.2).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        // Trailing Slot
                        if (displayMode == MediaDisplayMode.BOTH || displayMode == MediaDisplayMode.WAVEFORM_ONLY) {
                            MediaVisualizerHost(
                                mode = settings.visualizerMode,
                                isPlaying = activity.isPlaying,
                                accentColor = accentColor,
                                barCount = 3
                            )
                        } else {
                            Text(
                                text = activity.song.artist,
                                color = secondaryTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            is LiveActivityState.Fitness -> {
                val fitnessColor = Color(activity.brandColorLong)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = activity.appName,
                    tint = if (activity.isPaused) Color(0xFFFFC107) else fitnessColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = activity.effectiveDistance,
                    color = primaryTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                val hrValue = activity.effectiveHeartRate
                if (hrValue != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingHeartIcon(
                            modifier = Modifier.size(10.dp),
                            tint = Color(0xFFFF3B30)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$hrValue",
                            color = Color(0xFFFF3B30),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                LiveWorkoutTimerText(
                    startTimeMs = activity.startTimeMs,
                    fallbackSeconds = activity.elapsedSeconds,
                    isPaused = activity.isPaused,
                    staticDisplayTime = activity.staticDisplayTime,
                    fontSize = 11.sp,
                    textColor = if (activity.isPaused) Color(0xFFFFC107) else fitnessColor,
                    showPausedBadge = true
                )
            }

            is LiveActivityState.Alarm -> {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Alarm",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activity.timeText,
                    color = primaryTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444))
                        .clickable { viewModel.dismissAlarm() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Dismiss", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            is LiveActivityState.Timer -> {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                LiveTimerText(
                    targetEndTimeMs = activity.targetEndTimeMs,
                    fallbackSeconds = activity.remainingSeconds,
                    isRunning = activity.isRunning,
                    fontSize = 12.sp,
                    textColor = primaryTextColor
                )
            }

            is LiveActivityState.Notification -> {
                val shouldMaskContent = activity.isSensitive && settings.hideSensitiveContent && isDeviceLocked

                Icon(
                    imageVector = if (shouldMaskContent) Icons.Default.Lock else Icons.Default.Notifications,
                    contentDescription = "Notification",
                    tint = if (shouldMaskContent) Color(0xFFEF4444) else activity.appBrandColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                AnimatedContent(
                    targetState = shouldMaskContent,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                    },
                    label = "compact_notification_text"
                ) { isMasked ->
                    Text(
                        text = if (isMasked) {
                            "••••••"
                        } else if (activity.maskedText != activity.rawText && activity.maskedText.isNotBlank()) {
                            activity.maskedText
                        } else {
                            activity.sender.ifEmpty { activity.appName }
                        },
                        color = primaryTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            is LiveActivityState.Workout -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = "Workout",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${activity.calories} kcal",
                    color = primaryTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            is LiveActivityState.IncomingCall -> {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = if (activity.isAnswered) Color(0xFF10B981) else Color(0xFF3B82F6),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.contactName,
                        color = primaryTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (activity.isAnswered) {
                        CallChronometerTimerText(
                            callStartTimeMs = activity.callStartTimeMs,
                            textColor = secondaryTextColor,
                            fontSize = 9.sp
                        )
                    }
                }

                if (!activity.isAnswered) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .clickable { viewModel.endCall(context) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Decline",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .clickable { viewModel.answerCall(context) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Answer",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .clickable { viewModel.endCall(context) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            is LiveActivityState.Navigation -> {
                val navColor = Color(0xFF00E5FF)
                Icon(
                    imageVector = when (activity.iconType) {
                        NavigationDirection.ROUNDABOUT, NavigationDirection.U_TURN -> Icons.Default.Sync
                        else -> Icons.Default.Navigation
                    },
                    contentDescription = "Direction",
                    tint = navColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.currentInstruction,
                        color = primaryTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${activity.distanceText} • ${activity.etaMinutes} min ETA",
                        color = secondaryTextColor,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
                if (settings.isCharging && settings.showBatteryIndicator) {
                    Spacer(modifier = Modifier.width(6.dp))
                    DiscreetBatteryIndicator(
                        batteryLevel = settings.batteryLevel,
                        isCharging = settings.isCharging,
                        showPercentageText = true
                    )
                }
            }

            is LiveActivityState.Delivery -> {
                val deliveryColor = if (activity.originatingPackageName?.contains("zomato") == true) Color(0xFFE23744)
                else if (activity.originatingPackageName?.contains("swiggy") == true) Color(0xFFFC8019)
                else Color(0xFF10B981)

                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Delivery",
                    tint = deliveryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.restaurantName,
                        color = primaryTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activity.statusText,
                        color = secondaryTextColor,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (settings.isCharging && settings.showBatteryIndicator) {
                    Spacer(modifier = Modifier.width(6.dp))
                    DiscreetBatteryIndicator(
                        batteryLevel = settings.batteryLevel,
                        isCharging = settings.isCharging,
                        showPercentageText = true
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(deliveryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${(activity.progressPercent * 100).toInt()}%",
                            color = deliveryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            else -> {
                if (settings.showBatteryIndicator) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (settings.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                contentDescription = "Battery Status",
                                tint = if (settings.isCharging) Color(0xFF34C759) else secondaryTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (settings.isCharging) "Charging" else "Battery",
                                color = primaryTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        DiscreetBatteryIndicator(
                            batteryLevel = settings.batteryLevel,
                            isCharging = settings.isCharging,
                            showPercentageText = true
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DecoupledPlaybackTimeDisplay(
    basePositionMs: Long,
    lastPositionUpdateTime: Long,
    playbackSpeed: Float,
    totalTimeMs: Long,
    accentColor: Color,
    primaryTextColor: Color,
    secondaryTextColor: Color,
    isPlaying: Boolean = false
) {
    var currentPosMs by remember(basePositionMs, lastPositionUpdateTime, playbackSpeed, isPlaying) {
        val initial = com.example.viewmodel.GlobalIslandState.calculateCurrentPosition(
            basePosition = basePositionMs,
            lastPositionUpdateTime = lastPositionUpdateTime,
            playbackSpeed = playbackSpeed,
            isPlaying = isPlaying,
            durationMs = totalTimeMs
        )
        mutableLongStateOf(initial)
    }

    LaunchedEffect(basePositionMs, lastPositionUpdateTime, playbackSpeed, isPlaying, totalTimeMs) {
        if (isPlaying) {
            while (true) {
                currentPosMs = com.example.viewmodel.GlobalIslandState.calculateCurrentPosition(
                    basePosition = basePositionMs,
                    lastPositionUpdateTime = lastPositionUpdateTime,
                    playbackSpeed = playbackSpeed,
                    isPlaying = true,
                    durationMs = totalTimeMs
                )
                delay(200L) // smooth 5Hz ticker for accurate progress tracking
            }
        } else {
            currentPosMs = basePositionMs
        }
    }

    val progress = if (totalTimeMs > 0) currentPosMs.toFloat() / totalTimeMs.toFloat() else 0f

    val formatMs: (Long) -> String = { ms ->
        val totalSec = maxOf(0L, ms / 1000)
        val min = totalSec / 60
        val sec = totalSec % 60
        String.format("%d:%02d", min, sec)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${formatMs(currentPosMs)} / ${formatMs(totalTimeMs)}",
                color = secondaryTextColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (totalTimeMs > 0) "-${formatMs(maxOf(0L, totalTimeMs - currentPosMs))}" else "",
                color = secondaryTextColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
