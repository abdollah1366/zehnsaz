package com.example.zehnchin.domain.assessment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentMeasuresTest {

    @Test
    fun `missing measures are left out instead of stored as zero`() {
        val score = GradCptScore(
            dPrime = 2.0,
            commissionErrorRate = null,
            omissionErrorRate = 0.1,
            reactionTimeVariability = null
        )
        val measures = AssessmentMeasures.of(score)

        assertEquals(setOf("dPrime", "omissionErrorRate"), measures.keys)
        assertFalse(measures.containsKey("commissionErrorRate"))
        assertEquals(2.0, measures.getValue("dPrime"), 1e-9)
    }

    @Test
    fun `ANT measures keep all four effects when they are available`() {
        val score = AntScore(alerting = 50.0, orienting = 60.0, conflict = 80.0, accuracy = 0.95)
        val measures = AssessmentMeasures.of(score)

        assertEquals(4, measures.size)
        assertTrue(measures.containsKey("conflict"))
    }
}
