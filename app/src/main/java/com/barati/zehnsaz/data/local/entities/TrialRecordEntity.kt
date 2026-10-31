package com.barati.zehnsaz.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trial_records",
    foreignKeys = [
        ForeignKey(
            entity = LevelAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("attemptId")]
)
data class TrialRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,
    val trialIndex: Int,
    val stimulusLabel: String,
    val responseLabel: String,
    val isCorrect: Boolean,
    val reactionTimeMillis: Long?
)
