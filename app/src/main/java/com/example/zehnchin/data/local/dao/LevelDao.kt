package com.example.zehnchin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zehnchin.data.local.entities.LevelProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    fun getLevelProgress(levelId: Long): Flow<LevelProgressEntity?>

    @Query("SELECT * FROM level_progress")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: LevelProgressEntity)
}