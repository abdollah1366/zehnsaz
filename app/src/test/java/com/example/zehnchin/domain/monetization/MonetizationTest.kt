package com.example.zehnchin.domain.monetization

import com.example.zehnchin.domain.model.AdResult
import com.example.zehnchin.domain.model.PurchaseResult
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