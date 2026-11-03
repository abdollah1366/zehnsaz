package com.barati.zehnsaz.domain.assessment

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentScheduleTest {

    private val dayMillis = 24L * 60 * 60 * 1000

    @Test
    fun `a battery never taken is due`() {
        assertTrue(AssessmentSchedule.isDue(nowMillis = 0L, lastCompletedMillis = null))
    }

    @Test
    fun `a battery taken 13 days ago is not yet due`() {
        assertFalse(AssessmentSchedule.isDue(nowMillis = 13 * dayMillis, lastCompletedMillis = 0L))
    }

    @Test
    fun `a battery is due once the interval has passed`() {
        assertTrue(AssessmentSchedule.isDue(nowMillis = 14 * dayMillis, lastCompletedMillis = 0L))
    }
}
