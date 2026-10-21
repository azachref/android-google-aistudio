package com.example.data

import com.example.R
import com.example.model.SoundCategory
import com.example.model.SoundDefinition
import com.example.model.SoundPreset

object LocalSoundCatalog {

    val builtInSounds: List<SoundDefinition> = listOf(
        SoundDefinition(
            id = "rain",
            title = "Rain",
            description = "Gentle soothing rainfall",
            category = SoundCategory.RAIN,
            rawResId = R.raw.rain,
            isBuiltIn = true,
            defaultVolume = 0.65f
        ),
        SoundDefinition(
            id = "thunder",
            title = "Thunder",
            description = "Distant rolling rumbles",
            category = SoundCategory.THUNDER,
            rawResId = R.raw.thunder,
            isBuiltIn = true,
            defaultVolume = 0.40f
        ),
        SoundDefinition(
            id = "wind",
            title = "Wind",
            description = "Soft nocturnal breeze",
            category = SoundCategory.WIND,
            rawResId = R.raw.wind,
            isBuiltIn = true,
            defaultVolume = 0.45f
        ),
        SoundDefinition(
            id = "river",
            title = "River Stream",
            description = "Flowing mountain brook",
            category = SoundCategory.RIVER,
            rawResId = R.raw.river,
            isBuiltIn = true,
            defaultVolume = 0.55f
        ),
        SoundDefinition(
            id = "birds",
            title = "Forest Birds",
            description = "Gentle morning birdsong",
            category = SoundCategory.BIRDS,
            rawResId = R.raw.birds,
            isBuiltIn = true,
            defaultVolume = 0.45f
        ),
        SoundDefinition(
            id = "cafe",
            title = "Cafe",
            description = "Warm coffeehouse ambiance",
            category = SoundCategory.CAFE,
            rawResId = R.raw.cafe,
            isBuiltIn = true,
            defaultVolume = 0.50f
        ),
        SoundDefinition(
            id = "lofi",
            title = "Lofi",
            description = "Cozy chill study beats",
            category = SoundCategory.LOFI,
            rawResId = R.raw.lofi,
            isBuiltIn = true,
            defaultVolume = 0.55f
        ),
        SoundDefinition(
            id = "noise",
            title = "White Noise",
            description = "Pure steady static frequency",
            category = SoundCategory.NOISE,
            rawResId = R.raw.noise,
            isBuiltIn = true,
            defaultVolume = 0.50f
        ),
        SoundDefinition(
            id = "traffic",
            title = "City Traffic",
            description = "Distant urban flow",
            category = SoundCategory.TRAFFIC,
            rawResId = R.raw.traffic,
            isBuiltIn = true,
            defaultVolume = 0.35f
        ),
        SoundDefinition(
            id = "cars",
            title = "Cars Passing",
            description = "Rhythmic highway glide",
            category = SoundCategory.CARS,
            rawResId = R.raw.cars,
            isBuiltIn = true,
            defaultVolume = 0.35f
        )
    )

    val defaultPresets: List<SoundPreset> = listOf(
        SoundPreset(
            id = "preset_sleep",
            name = "Deep Sleep Rain",
            isBuiltIn = true,
            soundVolumes = mapOf(
                "rain" to 0.70f,
                "wind" to 0.30f
            )
        ),
        SoundPreset(
            id = "preset_nature",
            name = "Nature Haven",
            isBuiltIn = true,
            soundVolumes = mapOf(
                "river" to 0.60f,
                "birds" to 0.45f,
                "wind" to 0.30f
            )
        ),
        SoundPreset(
            id = "preset_storm",
            name = "Stormy Night",
            isBuiltIn = true,
            soundVolumes = mapOf(
                "rain" to 0.65f,
                "thunder" to 0.50f,
                "wind" to 0.40f
            )
        ),
        SoundPreset(
            id = "preset_cafe",
            name = "Cafe Focus",
            isBuiltIn = true,
            soundVolumes = mapOf(
                "cafe" to 0.60f,
                "rain" to 0.25f
            )
        ),
        SoundPreset(
            id = "preset_study",
            name = "Chill Study",
            isBuiltIn = true,
            soundVolumes = mapOf(
                "lofi" to 0.60f,
                "rain" to 0.30f
            )
        ),
        SoundPreset(
            id = "preset_static",
            name = "Pure Static",
            isBuiltIn = true,
            soundVolumes = mapOf(
                "noise" to 0.60f
            )
        )
    )
}
