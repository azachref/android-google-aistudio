package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SoundPreset
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PresetsRow(
    presets: List<SoundPreset>,
    onSelectPreset: (SoundPreset) -> Unit,
    onSaveCurrentMixClick: () -> Unit,
    onDeleteCustomPreset: (String) -> Unit,
    hasActiveSounds: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ElectricBolt,
                    contentDescription = null,
                    tint = NeonViolet,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.favorites_title),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            OutlinedButton(
                onClick = onSaveCurrentMixClick,
                enabled = hasActiveSounds,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (hasActiveSounds) NeonCyan.copy(alpha = 0.6f) else BorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (hasActiveSounds) NeonCyan else TextMuted
                ),
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier.testTag("save_mix_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Save Mix",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Horizontal Row of Presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { preset ->
                PresetChip(
                    preset = preset,
                    onClick = { onSelectPreset(preset) },
                    onDelete = { onDeleteCustomPreset(preset.id) }
                )
            }
        }
    }
}

@Composable
fun PresetChip(
    preset: SoundPreset,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MidnightCard,
        border = BorderStroke(1.dp, if (preset.isBuiltIn) BorderSubtle else BorderActive),
        modifier = modifier.testTag("preset_chip_${preset.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = if (preset.isBuiltIn) NeonCyan else NeonViolet,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )

            if (!preset.isBuiltIn) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(18.dp)
                        .testTag("delete_preset_${preset.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Delete preset",
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
