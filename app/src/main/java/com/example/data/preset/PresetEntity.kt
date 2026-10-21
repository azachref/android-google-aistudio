package com.example.data.preset

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SoundPreset

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isBuiltIn: Boolean,
    val volumesEncoded: String
) {
    fun toSoundPreset(): SoundPreset {
        val map = mutableMapOf<String, Float>()
        if (volumesEncoded.isNotBlank()) {
            volumesEncoded.split(";").forEach { pair ->
                val parts = pair.split(":")
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].toFloatOrNull() ?: 0.5f
                    map[key] = value
                }
            }
        }
        return SoundPreset(
            id = id,
            name = name,
            isBuiltIn = isBuiltIn,
            soundVolumes = map
        )
    }

    companion object {
        fun fromSoundPreset(preset: SoundPreset): PresetEntity {
            val encoded = preset.soundVolumes.entries.joinToString(";") { "${it.key}:${it.value}" }
            return PresetEntity(
                id = preset.id,
                name = preset.name,
                isBuiltIn = preset.isBuiltIn,
                volumesEncoded = encoded
            )
        }
    }
}
