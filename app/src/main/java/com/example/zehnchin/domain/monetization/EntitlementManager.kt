package com.example.zehnchin.domain.monetization

import com.example.zehnchin.data.local.dao.PlayerDao
import com.example.zehnchin.data.local.entities.EntitlementEntity
import com.example.zehnchin.domain.model.EntitlementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EntitlementManager @Inject constructor(
    private val playerDao: PlayerDao
) {
    fun hasEntitlement(type: EntitlementType): Flow<Boolean> {
        return playerDao.getAllEntitlements().map { list ->
            list.any { it.type == type && it.isActive }
        }
    }

    open suspend fun grantEntitlement(type: EntitlementType) {
        playerDao.saveEntitlement(EntitlementEntity(type = type, isActive = true))
    }

    suspend fun revokeEntitlement(type: EntitlementType) {
        playerDao.saveEntitlement(EntitlementEntity(type = type, isActive = false))
    }
}