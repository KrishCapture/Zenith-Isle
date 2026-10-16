package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.service.DynamicIslandOverlayService
import com.example.model.VisualizerMode
import com.example.model.IslandThemeMode
import com.example.viewmodel.AuraLightingEffect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.viewmodel.MediaDisplayMode
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IslandBlack
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.DynamicIslandViewModel
import com.example.viewmodel.IslandSettings

@Composable
fun SettingsDrawer(
    settings: IslandSettings,
    viewModel: DynamicIslandViewModel,
    modifier: Modifier = Modifier,
    animatedAccentColor: Color = PrimaryNeon
) {
    val context = LocalContext.current
    val isDarkMode = settings.isDarkMode
    val drawerBg = if (isDarkMode) Color(0xFF0D0D11) else Color(0xFFF6F6F9)
    val cardBg = if (isDarkMode) Color(0xFF16161E) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkMode) Color(0xFF262630) else Color(0xFFE2E2E8)
    val textPrimary = if (isDarkMode) Color(0xFFF0F0F2) else Color(0xFF19191D)
    val textSecondary = if (isDarkMode) Color(0xFF8E8E96) else Color(0xFF6B6B76)
    val inactiveTrack = if (isDarkMode) Color(0xFF262630) else Color(0xFFE2E2E8)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(drawerBg)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = animatedAccentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "ISLAND TUNING & OVERLAY",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Effects & Animation Physics Settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "VISUAL EFFECTS & PHYSICS",
                    color = animatedAccentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Toggle 1: Ambient Electric Pulse Effect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ambient Electric Pulse Effect",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Soft alpha breathing pulse while active",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = settings.isPulseEnabled,
                        onCheckedChange = { viewModel.togglePulseEffect(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("pulse_effect_switch")
                    )
                }

                // Toggle 2: Bouncy Spring Animations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spring Morph Animations",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Bouncy spring physics morphing vs linear tween",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = settings.isSpringEnabled,
                        onCheckedChange = { viewModel.toggleSpringAnimation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("spring_animation_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System Overlay Toggle Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Show on Home Screen",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Draw over other apps",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = settings.isSystemOverlayActive,
                        onCheckedChange = { isChecked ->
                            try {
                                if (isChecked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        val serviceIntent = Intent(context, DynamicIslandOverlayService::class.java)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(serviceIntent)
                                        } else {
                                            context.startService(serviceIntent)
                                        }
                                        viewModel.setSystemOverlayActive(true)
                                    }
                                } else {
                                    val serviceIntent = Intent(context, DynamicIslandOverlayService::class.java)
                                    context.stopService(serviceIntent)
                                    viewModel.setSystemOverlayActive(false)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("overlay_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Add Media Integration button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = animatedAccentColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Real Media Tracking (Spotify)", color = Color.Black, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
                } else {
                    true
                }

                Button(
                    onClick = {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (ignored: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIgnoringBatteryOptimizations) EmeraldGreen else animatedAccentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("battery_optimization_button")
                ) {
                    Text(
                        text = if (isIgnoringBatteryOptimizations) "✓ Doze Exempt (Battery Unrestricted)" else "Exempt from Doze (Prevent Idle Kill)",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val isAccessibilityActive = viewModel.isAccessibilityServiceConnected.collectAsState().value ||
                        viewModel.isAccessibilityServiceEnabled(context)

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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAccessibilityActive) EmeraldGreen else animatedAccentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("drawer_accessibility_button")
                ) {
                    Text(
                        text = if (isAccessibilityActive) "✓ Notch Overlay Active (Accessibility)" else "Enable Notch Overlay (Accessibility Service)",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Position Y Offset Slider (Punch-Hole Alignment)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Y-Axis Vertical Offset (Punch-Hole Alignment)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${settings.pillOffsetYDp.toInt()} dp",
                        color = animatedAccentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = settings.pillOffsetYDp,
                    onValueChange = { viewModel.updatePillOffsetY(it) },
                    valueRange = -40f..80f,
                    colors = SliderDefaults.colors(
                        thumbColor = animatedAccentColor,
                        activeTrackColor = animatedAccentColor,
                        inactiveTrackColor = inactiveTrack
                    ),
                    modifier = Modifier.testTag("y_offset_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pill Dimensions Adjustment Card (Width & Height)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Width Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Pill Width Adjustment",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${settings.pillWidthDp.toInt()} dp",
                        color = animatedAccentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = settings.pillWidthDp,
                    onValueChange = { viewModel.setPillWidth(it) },
                    valueRange = 140f..320f,
                    colors = SliderDefaults.colors(
                        thumbColor = animatedAccentColor,
                        activeTrackColor = animatedAccentColor,
                        inactiveTrackColor = inactiveTrack
                    ),
                    modifier = Modifier.testTag("pill_width_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Height Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Pill Height Adjustment",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${settings.pillHeightDp.toInt()} dp",
                        color = animatedAccentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = settings.pillHeightDp,
                    onValueChange = { viewModel.setPillHeight(it) },
                    valueRange = 28f..80f,
                    colors = SliderDefaults.colors(
                        thumbColor = animatedAccentColor,
                        activeTrackColor = animatedAccentColor,
                        inactiveTrackColor = inactiveTrack
                    ),
                    modifier = Modifier.testTag("pill_height_slider")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ambient Glow Toggle & Style Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Aura & Ambient Lighting",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Independent lighting layers & master glow control",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Master Intensity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Master Aura Intensity",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(settings.auraIntensity * 100).toInt()}%",
                        color = animatedAccentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = settings.auraIntensity,
                    onValueChange = { viewModel.setAuraIntensity(it) },
                    valueRange = 0.15f..0.80f,
                    colors = SliderDefaults.colors(
                        thumbColor = animatedAccentColor,
                        activeTrackColor = animatedAccentColor,
                        inactiveTrackColor = animatedAccentColor.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("aura_intensity_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle 1: Ambient Breathing Glow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ambient Breathing Glow",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Soft organic pulse matching your music or app color",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = settings.isBreathingGlowEnabled,
                        onCheckedChange = { viewModel.setBreathingGlowEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("breathing_glow_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle 2: Glass Edge Glint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Glass Edge Glint",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Subtle liquid-light shine traveling along the pill border",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = settings.isEdgeSheenEnabled,
                        onCheckedChange = { viewModel.setEdgeSheenEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("edge_glint_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle 3: Track & Alert Shockwave
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Track & Alert Shockwave",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Quick ripple burst when songs change or alerts arrive",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = settings.isBeatPulseEnabled,
                        onCheckedChange = { viewModel.setBeatPulseEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("shockwave_pulse_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High-Fidelity UI Audio Feedback Card
        val selectedProfile = try {
            com.example.util.AudioFeedbackManager.SoundProfile.valueOf(settings.soundProfile)
        } catch (e: Exception) {
            com.example.util.AudioFeedbackManager.SoundProfile.SOFT_POP
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Audio Feedback Tones",
                                color = textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Low-latency activity switch & media action chimes",
                                color = textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = settings.isSoundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("sound_feedback_switch")
                    )
                }

                if (settings.isSoundEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "SOUND PROFILE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedAccentColor,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.util.AudioFeedbackManager.SoundProfile.values().forEach { profile ->
                            val isSelected = selectedProfile == profile
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setSoundProfile(profile) }
                                    .testTag("sound_profile_chip_${profile.name.lowercase()}"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) animatedAccentColor.copy(alpha = 0.2f) else if (isDarkMode) Color(0xFF1E1E28) else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) animatedAccentColor else if (isDarkMode) Color(0xFF333342) else Color(0xFFCBD5E1)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profile.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) animatedAccentColor else textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Feedback Volume Level",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = textPrimary
                        )
                        Text(
                            text = "${(settings.soundVolume * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedAccentColor
                        )
                    }

                    Slider(
                        value = settings.soundVolume,
                        onValueChange = { viewModel.setSoundVolume(it) },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = animatedAccentColor,
                            activeTrackColor = animatedAccentColor,
                            inactiveTrackColor = animatedAccentColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("sound_volume_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic Battery & Power Indicator Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Dynamic Battery & Charging",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Displays charging state & discreet pill indicator",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = settings.showBatteryIndicator,
                        onCheckedChange = { viewModel.setShowBatteryIndicator(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF34C759),
                            checkedTrackColor = Color(0xFF34C759).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("battery_indicator_switch")
                    )
                }

                if (settings.showBatteryIndicator) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Plugged In / Charging Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (settings.isCharging) Color(0xFF34C759) else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Plugged In / Charging State",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = settings.isCharging,
                            onCheckedChange = { viewModel.setCharging(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF34C759),
                                checkedTrackColor = Color(0xFF34C759).copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("battery_charging_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Battery Level Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Battery Level Percentage",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (settings.isCharging) Color(0xFF34C759).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                            border = BorderStroke(0.5.dp, if (settings.isCharging) Color(0xFF34C759) else TextMuted)
                        ) {
                            Text(
                                text = if (settings.isCharging) "⚡ ${settings.batteryLevel}%" else "${settings.batteryLevel}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (settings.isCharging) Color(0xFF34C759) else TextPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Slider(
                        value = settings.batteryLevel.toFloat(),
                        onValueChange = { viewModel.setBatteryLevel(it.toInt()) },
                        valueRange = 1f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (settings.isCharging) Color(0xFF34C759) else animatedAccentColor,
                            activeTrackColor = if (settings.isCharging) Color(0xFF34C759) else animatedAccentColor,
                            inactiveTrackColor = (if (settings.isCharging) Color(0xFF34C759) else animatedAccentColor).copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("battery_level_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Auto-Collapse on Inactivity Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Auto-Collapse on Inactivity",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Collapses when no active media or timer is running",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = settings.enableAutoCollapse,
                        onCheckedChange = { viewModel.toggleAutoCollapse(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("auto_collapse_switch")
                    )
                }

                if (settings.enableAutoCollapse) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Inactivity Delay Duration",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${settings.autoCollapseDelaySeconds}s",
                            color = animatedAccentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = settings.autoCollapseDelaySeconds.toFloat(),
                        onValueChange = { viewModel.updateAutoCollapseDelay(it.toInt()) },
                        valueRange = 2f..15f,
                        steps = 12,
                        colors = SliderDefaults.colors(
                            thumbColor = animatedAccentColor,
                            activeTrackColor = animatedAccentColor,
                            inactiveTrackColor = inactiveTrack
                        ),
                        modifier = Modifier.testTag("auto_collapse_delay_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Notification Mirroring & OTP Privacy Guard Card
        var customApp by remember { mutableStateOf("Chase Bank") }
        var customText by remember { mutableStateOf("Your Chase bank verification code is 738201") }

        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = animatedAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Notification Mirroring",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Mirror incoming apps with auto OTP privacy shield",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = settings.enableNotificationMirroring,
                        onCheckedChange = { viewModel.toggleNotificationMirroring(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("notification_mirroring_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hide Sensitive Content",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Mask OTPs, passwords, and banking notifications in pill",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = settings.hideSensitiveContent,
                        onCheckedChange = { viewModel.toggleHideSensitiveContent(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = animatedAccentColor,
                            checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("hide_sensitive_content_switch")
                    )
                }

                if (settings.enableNotificationMirroring) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2B2113))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Bank OTPs, 2FA codes, and PINs are strictly redacted before mirroring to the island.",
                                color = Color(0xFFFBBF24),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "TEST CUSTOM NOTIFICATION FILTER",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        label = { Text("Notification text (Try typing an OTP code)", fontSize = 10.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = animatedAccentColor,
                            unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_notification_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.triggerNotification(
                                appName = customApp,
                                sender = "Security Service",
                                title = "Incoming Message",
                                body = customText,
                                iconName = if (customApp.contains("Bank", ignoreCase = true)) "Bank" else "Message",
                                brandColorLong = 0xFF117ACA
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = animatedAccentColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_custom_notification_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = IslandBlack,
                                modifier = Modifier.size(14.dp)
                            )
                            Text("Simulate Incoming Notification", color = IslandBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))


        // Show Song Title Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = animatedAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Show Song Title in Pill",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Display text in compact mode",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = settings.showCompactSongTitle,
                    onCheckedChange = { viewModel.setShowCompactSongTitle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = animatedAccentColor,
                        checkedTrackColor = animatedAccentColor.copy(alpha = 0.3f),
                        uncheckedThumbColor = textSecondary,
                        uncheckedTrackColor = inactiveTrack
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Media Visualizer Mode & Visualizer Style Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = animatedAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Media Visualizer Mode",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Choose artwork, sound waveform, or both",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val currentMode = settings.mediaDisplayMode
                    listOf(
                        Triple(MediaDisplayMode.BOTH, "Both", "Cover + Wave"),
                        Triple(MediaDisplayMode.COVER_ONLY, "Cover Only", "Artwork"),
                        Triple(MediaDisplayMode.WAVEFORM_ONLY, "Waveform", "Sound Wave")
                    ).forEach { (mode, label, sub) ->
                        val isSelected = currentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) animatedAccentColor else inactiveTrack)
                                .clickable { viewModel.setMediaDisplayMode(mode) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = sub,
                                    color = if (isSelected) Color.Black.copy(alpha = 0.8f) else textSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VISUALIZER STYLE",
                    color = animatedAccentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val activeVis = settings.visualizerMode
                    listOf(
                        Triple(VisualizerMode.EQUALIZER_BARS, "Equalizer", "Wave Bars"),
                        Triple(VisualizerMode.FLUID_PULSE, "Fluid", "Pulse Ring"),
                        Triple(VisualizerMode.MINIMAL_DOTS, "Minimal", "Dot Wave"),
                        Triple(VisualizerMode.OFF, "Disabled", "Off")
                    ).forEach { (vMode, vTitle, vSub) ->
                        val isVisSelected = activeVis == vMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isVisSelected) animatedAccentColor else inactiveTrack)
                                .clickable { viewModel.setVisualizerMode(vMode) }
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = vTitle,
                                    color = if (isVisSelected) Color.Black else textPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = vSub,
                                    color = if (isVisSelected) Color.Black.copy(alpha = 0.8f) else textSecondary,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dedicated Theming & Accents Suite Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val isDarkSystem = isSystemInDarkTheme()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = animatedAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Theming & Accents Suite",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Select dynamic palette, monochrome obsidian, or custom accent glow",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "THEME MODE",
                    color = animatedAccentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val currentThemeMode = settings.themeMode
                    listOf(
                        Triple(IslandThemeMode.ADAPTIVE_ARTWORK, "Adaptive", "Album Art"),
                        Triple(IslandThemeMode.OBSIDIAN_MONOCHROME, "Obsidian", "Monochrome"),
                        Triple(IslandThemeMode.CUSTOM_ACCENT, "Custom Accent", "User Color")
                    ).forEach { (tMode, tTitle, tSub) ->
                        val isThemeSelected = currentThemeMode == tMode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isThemeSelected) animatedAccentColor else inactiveTrack)
                                .clickable { viewModel.setThemeMode(tMode) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tTitle,
                                    color = if (isThemeSelected) Color.Black else textPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tSub,
                                    color = if (isThemeSelected) Color.Black.copy(alpha = 0.8f) else textSecondary,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                if (settings.themeMode == IslandThemeMode.CUSTOM_ACCENT) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CUSTOM ACCENT COLOR",
                        color = animatedAccentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val customColors = listOf(
                            Triple(0xFF06B6D4L, "Cyan", Color(0xFF06B6D4)),
                            Triple(0xFF8B5CF6L, "Purple", Color(0xFF8B5CF6)),
                            Triple(0xFF10B981L, "Emerald", Color(0xFF10B981)),
                            Triple(0xFFF59E0BL, "Amber", Color(0xFFF59E0B)),
                            Triple(0xFFF43F5EL, "Rose", Color(0xFFF43F5E))
                        )
                        customColors.forEach { (colorLong, name, colorVal) ->
                            val isColorSelected = settings.customAccentColorLong == colorLong
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorVal)
                                    .border(
                                        width = if (isColorSelected) 3.dp else 0.dp,
                                        color = if (isColorSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setCustomAccentColor(colorLong) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ambient Glow Intensity / Alpha",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(settings.glowIntensity * 100).toInt()}%",
                        color = animatedAccentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = settings.glowIntensity,
                    onValueChange = { viewModel.setGlowIntensity(it) },
                    valueRange = 0.10f..0.80f,
                    colors = SliderDefaults.colors(
                        thumbColor = animatedAccentColor,
                        activeTrackColor = animatedAccentColor,
                        inactiveTrackColor = inactiveTrack
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real-Time Ambient Light Sensor Auto-Theme Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF13131A) else Color(0xFFF1F5F9)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = "Light Sensor",
                                    tint = animatedAccentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Ambient Light Sensor Auto-Theme",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Real-time Lux adaptive dark/light frosted variants",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Switch(
                                checked = settings.autoLightSensorTheme,
                                onCheckedChange = { viewModel.setAutoLightSensorTheme(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = animatedAccentColor,
                                    uncheckedThumbColor = textSecondary,
                                    uncheckedTrackColor = inactiveTrack
                                ),
                                modifier = Modifier.testTag("drawer_light_sensor_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val currentLux = settings.ambientLuxValue
                        val isLowLight = currentLux < settings.lightSensorLuxThreshold
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isLowLight) "💡 ${currentLux.toInt()} Lux • Low Light" else "☀️ ${currentLux.toInt()} Lux • Daylight",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = animatedAccentColor
                            )
                            Text(
                                text = if (isDarkMode) "Obsidian Dark Glass" else "Frosted Light Glass",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Slider(
                            value = currentLux,
                            onValueChange = { viewModel.updateAmbientLux(it) },
                            valueRange = 0f..150f,
                            colors = SliderDefaults.colors(
                                thumbColor = animatedAccentColor,
                                activeTrackColor = animatedAccentColor,
                                inactiveTrackColor = inactiveTrack
                            ),
                            modifier = Modifier.testTag("drawer_lux_slider")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Shake-to-Clear Gesture Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Shake Gesture",
                                    tint = animatedAccentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Shake-to-Clear Gesture",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Shake device to clear non-persistent items",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Switch(
                                checked = settings.enableShakeToClear,
                                onCheckedChange = { viewModel.toggleShakeToClear(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = animatedAccentColor,
                                    uncheckedThumbColor = textSecondary,
                                    uncheckedTrackColor = inactiveTrack
                                ),
                                modifier = Modifier.testTag("drawer_shake_to_clear_switch")
                            )
                        }

                        if (settings.autoLightSensorTheme) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Dark Mode Threshold",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "< ${settings.darkThresholdLx.toInt()} Lux (1.5s Debounce)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = animatedAccentColor
                                )
                            }
                            Slider(
                                value = settings.darkThresholdLx,
                                onValueChange = { viewModel.setDarkThresholdLx(it) },
                                valueRange = 5f..60f,
                                colors = SliderDefaults.colors(
                                    thumbColor = animatedAccentColor,
                                    activeTrackColor = animatedAccentColor,
                                    inactiveTrackColor = inactiveTrack
                                ),
                                modifier = Modifier.testTag("dark_threshold_slider")
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Light Mode Threshold",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "> ${settings.lightThresholdLx.toInt()} Lux (1.5s Debounce)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = animatedAccentColor
                                )
                            }
                            Slider(
                                value = settings.lightThresholdLx,
                                onValueChange = { viewModel.setLightThresholdLx(it) },
                                valueRange = 60f..200f,
                                colors = SliderDefaults.colors(
                                    thumbColor = animatedAccentColor,
                                    activeTrackColor = animatedAccentColor,
                                    inactiveTrackColor = inactiveTrack
                                ),
                                modifier = Modifier.testTag("light_threshold_slider")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Light/Dark Adaptation",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Auto opacity shift based on system mode",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = settings.enableDynamicTheme,
                        onCheckedChange = { viewModel.toggleDynamicTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = animatedAccentColor,
                            uncheckedThumbColor = textSecondary,
                            uncheckedTrackColor = inactiveTrack
                        )
                    )
                }

                if (settings.enableDynamicTheme) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "DARK MODE SETTINGS",
                        color = animatedAccentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Opacity: ${(settings.darkBgOpacity * 100).toInt()}%",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Slider(
                        value = settings.darkBgOpacity,
                        onValueChange = { viewModel.updateDarkThemeSettings(it, settings.darkBlurLevelDp) },
                        valueRange = 0.4f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = animatedAccentColor,
                            activeTrackColor = animatedAccentColor,
                            inactiveTrackColor = inactiveTrack
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Blur: ${settings.darkBlurLevelDp.toInt()} dp",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Slider(
                        value = settings.darkBlurLevelDp,
                        onValueChange = { viewModel.updateDarkThemeSettings(settings.darkBgOpacity, it) },
                        valueRange = 0f..25f,
                        colors = SliderDefaults.colors(
                            thumbColor = animatedAccentColor,
                            activeTrackColor = animatedAccentColor,
                            inactiveTrackColor = inactiveTrack
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "LIGHT MODE SETTINGS",
                        color = animatedAccentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Light Opacity: ${(settings.lightBgOpacity * 100).toInt()}%",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Slider(
                        value = settings.lightBgOpacity,
                        onValueChange = { viewModel.updateLightThemeSettings(it, settings.lightBlurLevelDp) },
                        valueRange = 0.4f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = animatedAccentColor,
                            activeTrackColor = animatedAccentColor,
                            inactiveTrackColor = inactiveTrack
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Light Blur: ${settings.lightBlurLevelDp.toInt()} dp",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Slider(
                        value = settings.lightBlurLevelDp,
                        onValueChange = { viewModel.updateLightThemeSettings(settings.lightBgOpacity, it) },
                        valueRange = 0f..25f,
                        colors = SliderDefaults.colors(
                            thumbColor = animatedAccentColor,
                            activeTrackColor = animatedAccentColor,
                            inactiveTrackColor = inactiveTrack
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System Overlay Permission Launcher Card
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "System Floating Overlay Service",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (Settings.canDrawOverlays(context)) "Permission Granted" else "Requires 'Draw over other apps' permission",
                                color = if (Settings.canDrawOverlays(context)) EmeraldGreen else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            if (!Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } else {
                                val newState = !settings.isSystemOverlayActive
                                viewModel.setSystemOverlayActive(newState)
                                val serviceIntent = Intent(context, DynamicIslandOverlayService::class.java)
                                if (newState) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(serviceIntent)
                                    } else {
                                        context.startService(serviceIntent)
                                    }
                                } else {
                                    context.stopService(serviceIntent)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (Settings.canDrawOverlays(context)) EmeraldGreen.copy(alpha = 0.2f) else animatedAccentColor,
                        modifier = Modifier.testTag("overlay_permission_button")
                    ) {
                        Text(
                            text = if (!Settings.canDrawOverlays(context)) "Enable" else (if (settings.isSystemOverlayActive) "Stop Service" else "Start Service"),
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
