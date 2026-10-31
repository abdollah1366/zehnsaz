package com.barati.zehnsaz.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.barati.zehnsaz.domain.model.EntitlementType

@Entity(tableName = "entitlements")
data class EntitlementEntity(
    @PrimaryKey val type: EntitlementType,
    val isActive: Boolean = true,
    val unlockedAt: Long = System.currentTimeMillis()
)