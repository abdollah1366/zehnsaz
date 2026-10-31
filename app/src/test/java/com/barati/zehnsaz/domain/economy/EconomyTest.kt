package com.barati.zehnsaz.domain.economy

import org.junit.Assert.assertEquals
import org.junit.Test

class EconomyTest {

    @Test
    fun `test XP for levels calculation`() {
        assertEquals(100, EconomyConfig.calculateXPForLevel(1))
        assertEquals(125, EconomyConfig.calculateXPForLevel(2))
        assertEquals(156, EconomyConfig.calculateXPForLevel(3))
    }
}
