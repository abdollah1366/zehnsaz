package com.example.zehnchin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.zehnchin.data.local.entities.PersonalBestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalBestDao {
    @Query("SELECT * FROM personal_bests WHERE playerId = :playerId AND cognitiveDomain = :domain")
    fun getPersonalBest(playerId: String, domain: String): Flow<PersonalBestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePersonalBest(personalBest: PersonalBestEntity)
}