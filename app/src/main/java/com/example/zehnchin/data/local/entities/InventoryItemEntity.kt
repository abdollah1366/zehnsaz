package com.example.zehnchin.data.local.entities

import androidx.room.Entity
import com.example.zehnchin.domain.model.ItemType

@Entity(tableName = "inventory_items", primaryKeys = ["playerId", "itemType"])
data class InventoryItemEntity(
    val playerId: String,
    val itemType: ItemType,
    val quantity: Int = 0
)