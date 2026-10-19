package com.example.zehnchin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.zehnchin.data.local.entities.AssessmentMeasureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {
    @Insert
    suspend fun insertMeasures(measures: List<AssessmentMeasureEntity>)

    @Query("SELECT * FROM assessment_measures WHERE playerId = :playerId AND task = :task ORDER BY takenAt ASC")
    fun getMeasures(playerId: String, task: String): Flow<List<AssessmentMeasureEntity>>

    /** When this task was last taken, or null if it never was. Drives the baseline schedule. */
    @Query("SELECT MAX(takenAt) FROM assessment_measures WHERE playerId = :playerId AND task = :task")
    fun getLastTakenAt(playerId: String, task: String): Flow<Long?>
}
