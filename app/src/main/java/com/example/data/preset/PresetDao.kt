package com.example.data.preset

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Query("SELECT * FROM presets ORDER BY isBuiltIn DESC, name ASC")
    fun getAllPresetsFlow(): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(presets: List<PresetEntity>)

    @Query("DELETE FROM presets WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustomPreset(id: String)

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun getCount(): Int
}
