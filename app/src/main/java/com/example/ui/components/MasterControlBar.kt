package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SleepTimerState
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MidnightCardElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import kotlin.math.roundToInt

@Composable
fun MasterControlBar(
    isPlaying: Boolean,
    activeCount: Int,
    masterVolume: Float,
    timerState: SleepTimerState,
    onTogglePlay: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onOpenTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, ambientColor = NeonCyan.copy(alpha = 0.1f)),
        color = AmoledBlack,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Master Volume Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.VolumeDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )

                Slider(
                    value = masterVolume,
                    onValueChange = onMasterVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("master_volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = BorderSubtle
                    )
                )

                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${(masterVolume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.width(36.dp)
                )
            }

            // Bottom Actions Row: Active count pill, Main Play/Pause, Sleep Timer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Active Sounds Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MidnightCardElevated)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying && activeCount > 0) NeonCyan else TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeCount == 0) "No active sounds" else "$activeCount playing",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isPlaying && activeCount > 0) TextPrimary else TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Play / Pause Large Neon Button
                FloatingActionButton(
                    onClick = onTogglePlay,
                    shape = CircleShape,
                    containerColor = NeonCyan,
                    contentColor = AmoledBlack,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("master_play_pause_fab")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Sleep Timer Quick Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (timerState.isRunning) NeonViolet.copy(alpha = 0.2f) else MidnightCardElevated
                        )
                        .border(
                            1.dp,
                            if (timerState.isRunning) NeonViolet else BorderSubtle,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable(onClick = onOpenTimerClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("timer_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (timerState.isRunning) Icons.Filled.HourglassTop else Icons.Filled.Bedtime,
                            contentDescription = "Sleep timer",
                            tint = if (timerState.isFadingOut) WarmAmber else if (timerState.isRunning) NeonViolet else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (timerState.isRunning) timerState.formattedRemaining else "Timer",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (timerState.isFadingOut) WarmAmber else if (timerState.isRunning) NeonViolet else TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}
