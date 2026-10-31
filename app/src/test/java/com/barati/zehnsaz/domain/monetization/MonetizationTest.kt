package com.barati.zehnsaz.domain.monetization

import com.barati.zehnsaz.domain.model.AdResult
import com.barati.zehnsaz.domain.model.PurchaseResult
import org.junit.Assert.assertEquals
import org.junit.Test

class MonetizationTest {

    @Test
    fun `test purchase result enum values`() {
        assertEquals("SUCCESS", PurchaseResult.SUCCESS.name)
        assertEquals("FAILED", PurchaseResult.FAILED.name)
    }

    @Test
    fun `test ad result enum values`() {
        assertEquals("COMPLETED", AdResult.COMPLETED.name)
        assertEquals("CANCELLED", AdResult.CANCELLED.name)
    }
}