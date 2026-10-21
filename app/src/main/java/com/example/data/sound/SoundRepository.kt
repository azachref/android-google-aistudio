package com.example.data.sound

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.LocalSoundCatalog
import com.example.data.preset.AppDatabase
import com.example.model.SoundDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SoundRepository(
    private val context: Context,
    private val dao: CustomSoundDao
) {

    private val customSoundsDir by lazy {
        File(context.filesDir, "custom_sounds").apply {
            if (!exists()) mkdirs()
        }
    }

    val allSoundsFlow: Flow<List<SoundDefinition>> = dao.getAllCustomSoundsFlow().map { customEntities ->
        val customSounds = customEntities.map { it.toSoundDefinition() }
        LocalSoundCatalog.builtInSounds + customSounds
    }

    suspend fun addCustomSound(
        title: String,
        iconName: String,
        sourceUri: Uri
    ): Result<SoundDefinition> = withContext(Dispatchers.IO) {
        val soundId = "custom_${UUID.randomUUID()}"
        val targetFile = File(customSoundsDir, "$soundId.mp3")

        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Could not open audio file stream"))

            if (targetFile.length() < 1000) {
                targetFile.delete()
                return@withContext Result.failure(Exception("Selected file is invalid or empty"))
            }

            val entity = CustomSoundEntity(
                id = soundId,
                title = title.trim(),
                iconName = iconName,
                filePath = targetFile.absolutePath,
                dateAdded = System.currentTimeMillis()
            )

            dao.insertSound(entity)
            Log.i(TAG, "Successfully added custom sound: ${entity.title} (${targetFile.length()} bytes)")
            Result.success(entity.toSoundDefinition())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import custom sound: ${e.message}", e)
            if (targetFile.exists()) targetFile.delete()
            Result.failure(e)
        }
    }

    suspend fun deleteCustomSound(id: String) = withContext(Dispatchers.IO) {
        try {
            val entity = dao.getSoundById(id)
            if (entity != null) {
                val file = File(entity.filePath)
                if (file.exists()) file.delete()
                dao.deleteSound(id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting custom sound $id: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "SoundRepository"

        @Volatile
        private var instance: SoundRepository? = null

        fun getInstance(context: Context): SoundRepository {
            return instance ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                instance ?: SoundRepository(context.applicationContext, db.customSoundDao()).also { instance = it }
            }
        }
    }
}
