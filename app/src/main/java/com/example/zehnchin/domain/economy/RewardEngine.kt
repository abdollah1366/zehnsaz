package com.example.zehnchin.domain.economy

import com.example.zehnchin.data.local.dao.PlayerDao
import com.example.zehnchin.data.local.entities.CurrencyTransactionEntity
import com.example.zehnchin.data.local.entities.InventoryItemEntity
import com.example.zehnchin.data.local.entities.PlayerProfileEntity
import com.example.zehnchin.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class RewardEngine @Inject constructor(
    private val playerDao: PlayerDao
) {
    private val mutex = Mutex()

    open suspend fun applyReward(reward: Reward, source: RewardSource, playerId: String = "default_player") {
        mutex.withLock {
            val currentProfile = playerDao.getPlayerProfile(playerId).first() ?: PlayerProfileEntity(playerId)
            
            // Update XP and Level
            var newXP = currentProfile.xp + reward.xp
            var newLevel = currentProfile.level
            
            while (newXP >= EconomyConfig.calculateXPForLevel(newLevel)) {
                newXP -= EconomyConfig.calculateXPForLevel(newLevel)
                newLevel++
            }
            
            // Update Currencies
            val newCoins = currentProfile.totalCoins + reward.coins
            val newGems = currentProfile.totalGems + reward.gems
            
            val updatedProfile = currentProfile.copy(
                level = newLevel,
                xp = newXP,
                totalCoins = newCoins,
                totalGems = newGems
            )
            
            playerDao.savePlayerProfile(updatedProfile)
            
            // Record Transactions
            if (reward.coins > 0) {
                playerDao.addTransaction(CurrencyTransactionEntity(
                    playerId = playerId, currencyType = CurrencyType.COIN, 
                    amount = reward.coins, transactionType = TransactionType.EARN, source = source
                ))
            }
            if (reward.gems > 0) {
                playerDao.addTransaction(CurrencyTransactionEntity(
                    playerId = playerId, currencyType = CurrencyType.GEM, 
                    amount = reward.gems, transactionType = TransactionType.EARN, source = source
                ))
            }
            
            // Update Inventory
            reward.items.forEach { (itemType, quantity) ->
                val currentInventory = playerDao.getInventory(playerId).first()
                val currentItem = currentInventory.find { it.itemType == itemType }
                val newQuantity = (currentItem?.quantity ?: 0) + quantity
                playerDao.saveInventoryItem(InventoryItemEntity(playerId, itemType, newQuantity))
            }
        }
    }

    open suspend fun spendCoins(amount: Int, source: RewardSource, playerId: String = "default_player"): Boolean {
        mutex.withLock {
            val currentProfile = playerDao.getPlayerProfile(playerId).first() ?: return false
            if (currentProfile.totalCoins < amount) return false

            val updatedProfile = currentProfile.copy(totalCoins = currentProfile.totalCoins - amount)
            playerDao.savePlayerProfile(updatedProfile)

            playerDao.addTransaction(CurrencyTransactionEntity(
                playerId = playerId, currencyType = CurrencyType.COIN, 
                amount = -amount, transactionType = TransactionType.SPEND, source = source
            ))
            return true
        }
    }
}
