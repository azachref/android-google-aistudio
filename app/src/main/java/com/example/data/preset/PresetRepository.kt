package com.example.data.preset

import android.content.Context
import com.example.data.LocalSoundCatalog
import com.example.model.SoundPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PresetRepository(
    private val dao: PresetDao,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    val allPresetsFlow: Flow<List<SoundPreset>> = dao.getAllPresetsFlow().map { entities ->
        entities.map { it.toSoundPreset() }
    }

    init {
        scope.launch {
            if (dao.getCount() == 0) {
                val defaultEntities = LocalSoundCatalog.defaultPresets.map {
                    PresetEntity.fromSoundPreset(it)
                }
                dao.insertAll(defaultEntities)
            }
        }
    }

    suspend fun saveCustomPreset(name: String, volumes: Map<String, Float>): SoundPreset = withContext(Dispatchers.IO) {
        val preset = SoundPreset(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            isBuiltIn = false,
            soundVolumes = volumes.filter { it.value > 0.01f }
        )
        dao.insertPreset(PresetEntity.fromSoundPreset(preset))
        preset
    }

    suspend fun deleteCustomPreset(id: String) = withContext(Dispatchers.IO) {
        dao.deleteCustomPreset(id)
    }

    companion object {
        @Volatile
        private var instance: PresetRepository? = null

        fun getInstance(context: Context): PresetRepository {
            return instance ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                instance ?: PresetRepository(db.presetDao()).also { instance = it }
            }
        }
    }
}
