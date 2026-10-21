package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector

enum class SoundCategory(val label: String, val icon: ImageVector) {
    RAIN("Rain", Icons.Filled.WaterDrop),
    THUNDER("Thunder", Icons.Filled.Thunderstorm),
    WIND("Wind", Icons.Filled.Air),
    CAFE("Cafe", Icons.Filled.Coffee),
    LOFI("Lofi", Icons.Filled.Headphones),
    NOISE("Noise", Icons.Filled.GraphicEq),
    TRAFFIC("Traffic", Icons.Filled.Traffic),
    CARS("Cars", Icons.Filled.DirectionsCar),
    RIVER("River", Icons.Filled.Waves),
    BIRDS("Birds", Icons.Filled.Park),
    FIRE("Fire", Icons.Filled.LocalFireDepartment),
    FOREST("Forest", Icons.Filled.Forest),
    BELL("Bell", Icons.Filled.Notifications),
    MUSIC("Music", Icons.Filled.MusicNote),
    CUSTOM("Custom", Icons.Filled.Audiotrack);

    companion object {
        fun fromName(name: String): SoundCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CUSTOM
        }
    }
}

data class SoundDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: SoundCategory,
    val rawResId: Int? = null,
    val customFilePath: String? = null,
    val isBuiltIn: Boolean = true,
    val defaultVolume: Float = 0.5f
)

data class SoundTrackState(
    val definition: SoundDefinition,
    val isPlaying: Boolean = false,
    val volume: Float = 0.5f,
    val isMuted: Boolean = false
) {
    val effectiveVolume: Float
        get() = if (isMuted || !isPlaying) 0f else volume
}

data class SoundPreset(
    val id: String,
    val name: String,
    val isBuiltIn: Boolean,
    val soundVolumes: Map<String, Float> // soundId -> volume (0.0 to 1.0)
)

data class SleepTimerState(
    val totalSeconds: Long = 0L,
    val remainingSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val isFadingOut: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    val formattedRemaining: String
        get() {
            if (remainingSeconds <= 0) return "00:00"
            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}
