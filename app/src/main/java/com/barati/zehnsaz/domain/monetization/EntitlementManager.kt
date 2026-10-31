package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.data.local.dao.PlayerDao
import com.barati.zehnsaz.data.local.entities.EntitlementEntity
import com.barati.zehnsaz.domain.model.EntitlementType
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