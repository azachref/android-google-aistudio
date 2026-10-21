package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.model.SoundCategory
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
import com.example.ui.theme.WarmAmber

@Composable
fun AddSoundDialog(
    onAddSound: (title: String, iconName: String, uri: Uri) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SoundCategory.CUSTOM) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "audio_sample.mp3"
            errorMessage = null
        }
    }

    val availableCategories = listOf(
        SoundCategory.CUSTOM,
        SoundCategory.FIRE,
        SoundCategory.FOREST,
        SoundCategory.BIRDS,
        SoundCategory.RIVER,
        SoundCategory.BELL,
        SoundCategory.MUSIC,
        SoundCategory.CAFE,
        SoundCategory.RAIN,
        SoundCategory.THUNDER,
        SoundCategory.WIND,
        SoundCategory.NOISE
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.add_new_sound_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Import any audio file from your device (MP3, WAV, etc.) to mix with your soundscapes offline.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // File Picker Button
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("audio/*") },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (selectedUri != null) NeonMint else NeonCyan.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedUri != null) NeonMint.copy(alpha = 0.1f) else MidnightCardElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choose_file_btn")
                ) {
                    Icon(
                        imageVector = if (selectedUri != null) Icons.Filled.Check else Icons.Filled.UploadFile,
                        contentDescription = null,
                        tint = if (selectedUri != null) NeonMint else NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedUri != null) (fileName ?: "Audio selected") else stringResource(R.string.select_audio_file),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (selectedUri != null) NeonMint else TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Name Input
                Text(
                    text = "Sound Name",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.sound_name_hint),
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderSubtle,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_sound_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Category Selector
                Text(
                    text = stringResource(R.string.choose_icon),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else MidnightCardElevated)
                                .border(
                                    1.5.dp,
                                    if (isSelected) NeonViolet else BorderSubtle,
                                    CircleShape
                                )
                                .clickable { selectedCategory = category },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.label,
                                tint = if (isSelected) NeonViolet else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = WarmAmber)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val uri = selectedUri
                    if (uri == null) {
                        errorMessage = "Please select an audio file"
                        return@Button
                    }
                    if (title.isBlank()) {
                        errorMessage = "Please enter a sound name"
                        return@Button
                    }
                    onAddSound(title, selectedCategory.name, uri)
                },
                enabled = selectedUri != null && title.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("confirm_add_sound_btn")
            ) {
                Text(
                    text = stringResource(R.string.add_sound),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = TextSecondary
                )
            }
        }
    )
}
