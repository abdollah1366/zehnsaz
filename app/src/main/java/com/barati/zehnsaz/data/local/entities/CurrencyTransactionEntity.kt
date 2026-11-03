package com.barati.zehnsaz.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.barati.zehnsaz.domain.model.CurrencyType
import com.barati.zehnsaz.domain.model.RewardSource
import com.barati.zehnsaz.domain.model.TransactionType

@Entity(tableName = "currency_transactions")
data class CurrencyTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerId: String,
    val currencyType: CurrencyType,
    val amount: Int,
    val transactionType: TransactionType,
    val source: RewardSource,
    val timestamp: Long = System.currentTimeMillis()
)