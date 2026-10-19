package com.example.zehnchin.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One measure from one baseline assessment, e.g. the d′ of a gradCPT session.
 * Stored one row per measure, so a measure can be followed over time without changing the schema.
 */
@Entity(
    tableName = "assessment_measures",
    indices = [Index("playerId", "task", "takenAt")]
)
data class AssessmentMeasureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: String,
    val task: String,
    val takenAt: Long,
    val measure: String,
    val measureValue: Double
)
