package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SleepTimerState
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightCardElevated
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SleepTimerModal(
    timerState: SleepTimerState,
    onStartTimer: (Int) -> Unit,
    onStopTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customMinutes by remember { mutableFloatStateOf(45f) }

    val presetDurations = listOf(15, 30, 45, 60, 90, 120)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightDark,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bedtime,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.sleep_timer),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Auto-stop with 30s gentle fade-out",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_timer_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // If Timer is running, show countdown circle and cancel button
            if (timerState.isRunning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MidnightCardElevated)
                        .border(1.dp, BorderActive, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { timerState.progress },
                                modifier = Modifier.size(100.dp),
                                strokeWidth = 6.dp,
                                color = if (timerState.isFadingOut) WarmAmber else NeonViolet,
                                trackColor = BorderSubtle
                            )
                            Text(
                                text = timerState.formattedRemaining,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (timerState.isFadingOut) WarmAmber else TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (timerState.isFadingOut) {
                            Text(
                                text = stringResource(R.string.fade_out_active),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = WarmAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        } else {
                            Text(
                                text = "Playing peacefully&#8230;",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = onStopTimer,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier.testTag("cancel_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Turn Off Timer")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Or choose a new duration:",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Quick Preset Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetDurations.forEach { minutes ->
                    Surface(
                        onClick = { onStartTimer(minutes) },
                        shape = RoundedCornerShape(12.dp),
                        color = MidnightCard,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.testTag("timer_preset_${minutes}m")
                    ) {
                        Text(
                            text = if (minutes >= 60) "${minutes / 60}h ${if (minutes % 60 > 0) "${minutes % 60}m" else ""}".trim() else "${minutes}m",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Custom Duration Slider
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MidnightCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.custom_timer),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "${customMinutes.roundToInt()} minutes",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = NeonViolet,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Slider(
                    value = customMinutes,
                    onValueChange = { customMinutes = it },
                    valueRange = 5f..180f,
                    steps = 34, // ~5-minute increments
                    modifier = Modifier.fillMaxWidth().testTag("custom_timer_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonViolet,
                        activeTrackColor = NeonViolet,
                        inactiveTrackColor = BorderSubtle
                    )
                )

                Button(
                    onClick = { onStartTimer(customMinutes.roundToInt()) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonViolet,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("start_custom_timer_btn")
                ) {
                    Text(
                        text = "Set Timer for ${customMinutes.roundToInt()} min",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep Fade Out Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmoledBlack)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sound volume will gently decrease over the final 30 seconds before stopping completely.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
