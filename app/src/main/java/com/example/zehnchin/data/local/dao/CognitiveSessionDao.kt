package com.example.zehnchin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zehnchin.data.local.entities.CognitiveSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CognitiveSessionDao {
    @Query("SELECT * FROM cognitive_sessions WHERE playerId = :playerId ORDER BY startTime DESC")
    fun getPlayerSessions(playerId: String): Flow<List<CognitiveSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: CognitiveSessionEntity)
}