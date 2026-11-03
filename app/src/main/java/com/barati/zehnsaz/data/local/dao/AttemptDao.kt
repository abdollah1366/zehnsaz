package com.barati.zehnsaz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.barati.zehnsaz.data.local.entities.LevelAttemptEntity
import com.barati.zehnsaz.data.local.entities.TrialRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {
    @Insert
    suspend fun insertAttempt(attempt: LevelAttemptEntity): Long

    @Insert
    suspend fun insertTrials(trials: List<TrialRecordEntity>)

    @Query("SELECT * FROM level_attempts WHERE playerId = :playerId ORDER BY startTime DESC")
    fun getAttempts(playerId: String): Flow<List<LevelAttemptEntity>>

    @Query("SELECT * FROM trial_records WHERE attemptId = :attemptId ORDER BY trialIndex ASC")
    suspend fun getTrials(attemptId: Long): List<TrialRecordEntity>
}
