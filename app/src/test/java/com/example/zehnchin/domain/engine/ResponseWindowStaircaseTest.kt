package com.example.zehnchin.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseWindowStaircaseTest {

    @Test
    fun `a hit shrinks the window by step times one minus target`() {
        val next = ResponseWindowStaircase(windowMillis = 1000L).afterGoTrial(hit = true)
        assertEquals(990L, next.windowMillis)
    }

    @Test
    fun `a miss grows the window by step times target`() {
        val next = ResponseWindowStaircase(windowMillis = 1000L).afterGoTrial(hit = false)
        assertEquals(1040L, next.windowMillis)
    }

    @Test
    fun `window is stable when hits occur at the target rate`() {
        // 4 hits and 1 miss per block is an 80% hit rate, so the expected change is zero.
        var staircase = ResponseWindowStaircase(windowMillis = 1000L)
        repeat(5) {
            repeat(4) { staircase = staircase.afterGoTrial(hit = true) }
            staircase = staircase.afterGoTrial(hit = false)
        }
        assertEquals(1000L, staircase.windowMillis)
    }

    @Test
    fun `window never goes below the minimum`() {
        var staircase = ResponseWindowStaircase(windowMillis = 300L)
        repeat(100) { staircase = staircase.afterGoTrial(hit = true) }
        assertEquals(250L, staircase.windowMillis)
    }

    @Test
    fun `window never goes above the maximum`() {
        var staircase = ResponseWindowStaircase(windowMillis = 1900L)
        repeat(100) { staircase = staircase.afterGoTrial(hit = false) }
        assertEquals(2000L, staircase.windowMillis)
    }
}
