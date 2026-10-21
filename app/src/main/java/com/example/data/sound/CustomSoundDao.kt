package com.example.data.sound

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomSoundDao {

    @Query("SELECT * FROM custom_sounds ORDER BY dateAdded DESC")
    fun getAllCustomSoundsFlow(): Flow<List<CustomSoundEntity>>

    @Query("SELECT * FROM custom_sounds WHERE id = :id")
    suspend fun getSoundById(id: String): CustomSoundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: CustomSoundEntity)

    @Query("DELETE FROM custom_sounds WHERE id = :id")
    suspend fun deleteSound(id: String)
}
