package com.example.data.sound

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SoundCategory
import com.example.model.SoundDefinition

@Entity(tableName = "custom_sounds")
data class CustomSoundEntity(
    @PrimaryKey val id: String,
    val title: String,
    val iconName: String,
    val filePath: String,
    val dateAdded: Long = System.currentTimeMillis()
) {
    fun toSoundDefinition(): SoundDefinition {
        return SoundDefinition(
            id = id,
            title = title,
            description = "Custom uploaded sound",
            category = SoundCategory.fromName(iconName),
            rawResId = null,
            customFilePath = filePath,
            isBuiltIn = false,
            defaultVolume = 0.5f
        )
    }
}
