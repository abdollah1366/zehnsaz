package com.barati.zehnsaz.data.local.entities

import androidx.room.Entity
import com.barati.zehnsaz.domain.model.ItemType

@Entity(tableName = "inventory_items", primaryKeys = ["playerId", "itemType"])
data class InventoryItemEntity(
    val playerId: String,
    val itemType: ItemType,
    val quantity: Int = 0
)