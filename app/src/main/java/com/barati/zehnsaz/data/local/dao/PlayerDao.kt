package com.barati.zehnsaz.data.local.dao

import androidx.room.*
import com.barati.zehnsaz.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE playerId = :playerId")
    fun getPlayerProfile(playerId: String): Flow<PlayerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM inventory_items WHERE playerId = :playerId")
    fun getInventory(playerId: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInventoryItem(item: InventoryItemEntity)

    @Query("SELECT * FROM currency_transactions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTransactions(): Flow<List<CurrencyTransactionEntity>>

    @Insert
    suspend fun addTransaction(transaction: CurrencyTransactionEntity)

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("SELECT * FROM entitlements")
    fun getAllEntitlements(): Flow<List<EntitlementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEntitlement(entitlement: EntitlementEntity)
}