package com.barati.zehnsaz.domain.scoring

import com.barati.zehnsaz.domain.model.InhibitoryResponseType
import com.barati.zehnsaz.domain.model.InhibitoryRoundResult
import com.barati.zehnsaz.domain.model.InhibitoryStimulusType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalDetectionTest {

    @Test
    fun `chance performance gives d prime of zero`() {
        assertEquals(0.0, SignalDetection.dPrime(50, 50, 50, 50), 1e-9)
    }

    @Test
    fun `80 percent hits and 20 percent false alarms gives d prime near one point seven`() {
        // z(0.8) − z(0.2) ≈ 1.68; the log-linear correction moves it slightly.
        val dPrime = SignalDetection.dPrime(80, 20, 20, 80)
        assertTrue("was $dPrime", dPrime in 1.6..1.75)
    }

    @Test
    fun `perfect performance stays finite`() {
        val dPrime = SignalDetection.dPrime(10, 0, 0, 10)
        assertTrue(dPrime.isFinite())
        assertTrue(dPrime > 3.0)
    }

    @Test
    fun `inverse normal matches known quantiles`() {
        assertEquals(0.0, SignalDetection.inverseNormal(0.5), 1e-6)
        assertEquals(1.96, SignalDetection.inverseNormal(0.975), 0.001)
        assertEquals(-1.2816, SignalDetection.inverseNormal(0.1), 0.001)
    }

    @Test
    fun `coefficient of variation uses the sample standard deviation`() {
        // mean 500, sample SD sqrt(20000) ≈ 141.42, so CV ≈ 0.283
        assertEquals(0.2828, SignalDetection.coefficientOfVariation(listOf(400L, 600L))!!, 0.001)
    }

    @Test
    fun `coefficient of variation needs at least two values`() {
        assertNull(SignalDetection.coefficientOfVariation(emptyList()))
        assertNull(SignalDetection.coefficientOfVariation(listOf(500L)))
    }

    @Test
    fun `calculator reports d prime and reaction time variability`() {
        val results = listOf(
            InhibitoryRoundResult(1, InhibitoryStimulusType.NO_GO, InhibitoryResponseType.NO_GO_CORRECT, null, true, 0, 1),
            InhibitoryRoundResult(2, InhibitoryStimulusType.NO_GO, InhibitoryResponseType.NO_GO_CORRECT, null, true, 0, 2),
            InhibitoryRoundResult(3, InhibitoryStimulusType.GO, InhibitoryResponseType.GO_CORRECT, 400L, true, 0, 3),
            InhibitoryRoundResult(4, InhibitoryStimulusType.GO, InhibitoryResponseType.GO_CORRECT, 600L, true, 0, 4)
        )

        val res = InhibitoryControlScoreCalculator.calculate(results, 5, 1500L)

        assertTrue(res.dPrime > 1.0)
        assertEquals(0.2828, res.reactionTimeVariability!!, 0.001)
    }
}
