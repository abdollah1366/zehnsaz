package com.barati.zehnsaz.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barati.zehnsaz.data.local.entities.PurchaseRecordEntity

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchase_records WHERE purchaseToken = :purchaseToken")
    suspend fun getByToken(purchaseToken: String): PurchaseRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(record: PurchaseRecordEntity)
}
