package com.example.zehnchin.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zehnchin.domain.model.CurrencyType
import com.example.zehnchin.domain.model.RewardSource
import com.example.zehnchin.domain.model.TransactionType

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