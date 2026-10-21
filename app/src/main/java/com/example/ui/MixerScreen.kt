package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AddSoundDialog
import com.example.ui.components.ManageSoundsSheet
import com.example.ui.components.MasterControlBar
import com.example.ui.components.PresetsRow
import com.example.ui.components.SavePresetDialog
import com.example.ui.components.SleepTimerModal
import com.example.ui.components.SoundCard
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.SoundMixerViewModel

@Composable
fun MixerScreen(
    viewModel: SoundMixerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tracks by viewModel.tracksList.collectAsStateWithLifecycle()
    val isMasterPlaying by viewModel.isMasterPlaying.collectAsStateWithLifecycle()
    val masterVolume by viewModel.masterVolume.collectAsStateWithLifecycle()
    val sleepTimerState by viewModel.sleepTimerState.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeCount.collectAsStateWithLifecycle()

    val showSavePresetDialog by viewModel.showSavePresetDialog.collectAsStateWithLifecycle()
    val showTimerSheet by viewModel.showTimerSheet.collectAsStateWithLifecycle()
    val showManageSoundsSheet by viewModel.showManageSoundsSheet.collectAsStateWithLifecycle()
    val showAddSoundDialog by viewModel.showAddSoundDialog.collectAsStateWithLifecycle()

    // Request notification permission on Android 13+ for media controls notification
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Handled */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AmoledBlack,
        bottomBar = {
            MasterControlBar(
                isPlaying = isMasterPlaying,
                activeCount = activeCount,
                masterVolume = masterVolume,
                timerState = sleepTimerState,
                onTogglePlay = { viewModel.toggleMasterPlay() },
                onMasterVolumeChange = { viewModel.setMasterVolume(it) },
                onOpenTimerClick = { viewModel.openTimerSheet() }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("sound_mixer_grid"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header span
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Nightlight,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.app_tagline),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Manage Sounds Button
                            IconButton(
                                onClick = { viewModel.openManageSounds() },
                                modifier = Modifier.testTag("top_bar_manage_sounds_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = stringResource(R.string.manage_sounds),
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Stop All button when playing
                            if (isMasterPlaying || activeCount > 0) {
                                IconButton(
                                    onClick = { viewModel.stopAll() },
                                    modifier = Modifier.testTag("stop_all_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.StopCircle,
                                        contentDescription = "Stop all playback",
                                        tint = WarmAmber,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Saved Presets Row
            item(span = { GridItemSpan(maxLineSpan) }) {
                PresetsRow(
                    presets = presets,
                    onSelectPreset = { viewModel.applyPreset(it) },
                    onSaveCurrentMixClick = { viewModel.openSavePresetDialog() },
                    onDeleteCustomPreset = { viewModel.deleteCustomPreset(it) },
                    hasActiveSounds = activeCount > 0
                )
            }

            // Sounds Section Header + Add/Manage Button
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Soundscape Layers",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "${tracks.size} sounds (100% offline)",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.openManageSounds() },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonViolet),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("manage_sounds_section_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Manage",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // Sound Cards (Default + Custom sounds)
            items(tracks, key = { it.definition.id }) { trackState ->
                SoundCard(
                    trackState = trackState,
                    onTogglePlay = { viewModel.toggleSound(trackState.definition.id) },
                    onVolumeChange = { viewModel.setSoundVolume(trackState.definition.id, it) },
                    onToggleMute = { viewModel.toggleMute(trackState.definition.id) }
                )
            }
        }
    }

    // Dialogs & Sheets
    if (showSavePresetDialog) {
        SavePresetDialog(
            onSave = { name -> viewModel.saveCurrentMix(name) },
            onDismiss = { viewModel.closeSavePresetDialog() }
        )
    }

    if (showTimerSheet) {
        SleepTimerModal(
            timerState = sleepTimerState,
            onStartTimer = { minutes -> viewModel.startTimer(minutes) },
            onStopTimer = { viewModel.stopTimer() },
            onDismiss = { viewModel.closeTimerSheet() }
        )
    }

    if (showManageSoundsSheet) {
        ManageSoundsSheet(
            tracks = tracks,
            onTogglePlay = { viewModel.toggleSound(it) },
            onDeleteCustomSound = { viewModel.deleteCustomSound(it) },
            onOpenAddSound = { viewModel.openAddSoundDialog() },
            onDismiss = { viewModel.closeManageSounds() }
        )
    }

    if (showAddSoundDialog) {
        AddSoundDialog(
            onAddSound = { title, iconName, uri ->
                viewModel.addCustomSound(title, iconName, uri) { success, error ->
                    if (success) {
                        Toast.makeText(context, "Added \"$title\" to your sounds!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, error ?: "Failed to import sound", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { viewModel.closeAddSoundDialog() }
        )
    }
}
