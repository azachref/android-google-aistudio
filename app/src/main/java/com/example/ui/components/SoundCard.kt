package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SoundTrackState
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightCardElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMint
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun SoundCard(
    trackState: SoundTrackState,
    onTogglePlay: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sound = trackState.definition
    val isPlaying = trackState.isPlaying
    val isMuted = trackState.isMuted

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_${sound.id}")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha_${sound.id}"
    )

    val cardBorder = if (isPlaying && !isMuted) {
        BorderStroke(1.5.dp, NeonCyan.copy(alpha = pulseAlpha))
    } else if (isPlaying) {
        BorderStroke(1.dp, BorderActive)
    } else {
        BorderStroke(1.dp, BorderSubtle)
    }

    val cardBackground = if (isPlaying) MidnightCardElevated else MidnightCard

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sound_card_${sound.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon, Title, Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onTogglePlay)
                ) {
                    // Sound Icon Container
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlaying && !isMuted) {
                                    Brush.radialGradient(
                                        listOf(NeonCyan.copy(alpha = 0.35f), Color.Transparent)
                                    )
                                } else {
                                    Brush.radialGradient(
                                        listOf(BorderSubtle, Color.Transparent)
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = sound.category.icon,
                            contentDescription = sound.title,
                            tint = if (isPlaying && !isMuted) NeonCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sound.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPlaying) TextPrimary else TextSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Animated Waveform Indicator when playing
                            if (isPlaying && !isMuted) {
                                Spacer(modifier = Modifier.width(6.dp))
                                AnimatedSoundWave(alpha = pulseAlpha)
                            }
                        }

                        Text(
                            text = sound.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Play/Pause Switch
                Switch(
                    checked = isPlaying,
                    onCheckedChange = { onTogglePlay() },
                    modifier = Modifier.testTag("switch_${sound.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = BorderSubtle,
                        uncheckedBorderColor = BorderSubtle
                    )
                )
            }

            // Controls Row (Visible when playing)
            AnimatedVisibility(
                visible = isPlaying,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute button
                        IconButton(
                            onClick = onToggleMute,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("mute_btn_${sound.id}")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = if (isMuted) NeonViolet else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Track Volume Slider
                        Slider(
                            value = trackState.volume,
                            onValueChange = onVolumeChange,
                            valueRange = 0f..1f,
                            enabled = !isMuted,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("slider_${sound.id}"),
                            colors = SliderDefaults.colors(
                                thumbColor = if (isMuted) TextMuted else NeonCyan,
                                activeTrackColor = if (isMuted) BorderSubtle else NeonCyan,
                                inactiveTrackColor = BorderSubtle
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isMuted) "Mute" else "${(trackState.volume * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isMuted) NeonViolet else TextSecondary,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }

            // Offline & Badge footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge
                Text(
                    text = if (sound.isBuiltIn) "Default" else "Custom",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (sound.isBuiltIn) TextMuted else NeonViolet,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Offline ready",
                        tint = NeonMint.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% Offline",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonMint.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedSoundWave(alpha: Float, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((8 + (6 * alpha)).dp)
                .clip(RoundedCornerShape(1.dp))
                .background(NeonCyan)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((14 - (8 * alpha)).dp)
                .clip(RoundedCornerShape(1.dp))
                .background(NeonCyan)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((6 + (8 * alpha)).dp)
                .clip(RoundedCornerShape(1.dp))
                .background(NeonCyan)
        )
    }
}
