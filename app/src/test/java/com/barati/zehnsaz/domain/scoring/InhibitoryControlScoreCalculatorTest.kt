package com.barati.zehnsaz.domain.scoring

import com.barati.zehnsaz.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InhibitoryControlScoreCalculatorTest {

    @Test
    fun `test high no-go accuracy gives more stars`() {
        val results = listOf(
            InhibitoryRoundResult(1, InhibitoryStimulusType.NO_GO, InhibitoryResponseType.NO_GO_CORRECT, null, true, 0, 1),
            InhibitoryRoundResult(2, InhibitoryStimulusType.NO_GO, InhibitoryResponseType.NO_GO_CORRECT, null, true, 0, 2),
            InhibitoryRoundResult(3, InhibitoryStimulusType.GO, InhibitoryResponseType.GO_CORRECT, 400L, true, 0, 3)
        )
        
        val res = InhibitoryControlScoreCalculator.calculate(results, 5, 1500L)
        
        assertEquals(100, res.noGoAccuracy)
        assertEquals(100, res.goAccuracy)
        assertEquals(3, res.stars)
    }

    @Test
    fun `test false alarms penalty stars`() {
        val results = listOf(
            InhibitoryRoundResult(1, InhibitoryStimulusType.NO_GO, InhibitoryResponseType.NO_GO_INCORRECT, 400L, false, 0, 0),
            InhibitoryRoundResult(2, InhibitoryStimulusType.GO, InhibitoryResponseType.GO_CORRECT, 500L, true, 0, 1)
        )
        
        val res = InhibitoryControlScoreCalculator.calculate(results, 1, 1500L)
        
        assertEquals(0, res.noGoAccuracy)
        assertEquals(100, res.falseAlarmRate)
        assertEquals(0, res.stars)
    }
}