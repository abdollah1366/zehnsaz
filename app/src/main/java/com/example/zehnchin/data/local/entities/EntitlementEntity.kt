package com.example.zehnchin.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zehnchin.domain.model.EntitlementType

@Entity(tableName = "entitlements")
data class EntitlementEntity(
    @PrimaryKey val type: EntitlementType,
    val isActive: Boolean = true,
    val unlockedAt: Long = System.currentTimeMillis()
)