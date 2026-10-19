package com.example.zehnchin.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v3 → v4: adds per-attempt and per-trial tables. Existing tables are left untouched,
 * so player progress is kept.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `level_attempts` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`playerId` TEXT NOT NULL, " +
                "`levelId` INTEGER NOT NULL, " +
                "`chapterId` INTEGER NOT NULL, " +
                "`startTime` INTEGER NOT NULL, " +
                "`endTime` INTEGER NOT NULL, " +
                "`isWon` INTEGER NOT NULL, " +
                "`stars` INTEGER NOT NULL, " +
                "`score` INTEGER NOT NULL, " +
                "`accuracy` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_level_attempts_playerId` ON `level_attempts` (`playerId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_level_attempts_levelId` ON `level_attempts` (`levelId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `trial_records` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`attemptId` INTEGER NOT NULL, " +
                "`trialIndex` INTEGER NOT NULL, " +
                "`stimulusLabel` TEXT NOT NULL, " +
                "`responseLabel` TEXT NOT NULL, " +
                "`isCorrect` INTEGER NOT NULL, " +
                "`reactionTimeMillis` INTEGER, " +
                "FOREIGN KEY(`attemptId`) REFERENCES `level_attempts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_trial_records_attemptId` ON `trial_records` (`attemptId`)")
    }
}
