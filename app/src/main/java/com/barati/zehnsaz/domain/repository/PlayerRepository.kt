package com.barati.zehnsaz.domain.repository

import com.barati.zehnsaz.data.local.dao.PlayerDao
import com.barati.zehnsaz.data.local.entities.AchievementEntity
import com.barati.zehnsaz.data.local.entities.InventoryItemEntity
import com.barati.zehnsaz.data.local.entities.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val playerDao: PlayerDao
) {
    fun getPlayerProfile(playerId: String = "default_player"): Flow<PlayerProfileEntity?> {
        return playerDao.getPlayerProfile(playerId)
    }

    fun getInventory(playerId: String = "default_player"): Flow<List<InventoryItemEntity>> {
        return playerDao.getInventory(playerId)
    }

    fun getAllAchievements(): Flow<List<AchievementEntity>> {
        return playerDao.getAllAchievements()
    }
}