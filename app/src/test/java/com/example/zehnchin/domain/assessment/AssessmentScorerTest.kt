package com.example.zehnchin.domain.assessment

import com.example.zehnchin.domain.scoring.SignalDetection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentScorerTest {

    @Test
    fun `gradCPT reports commission and omission error rates`() {
        // 10 No-Go trials, 1 responded to: commission 0.1.
        // 90 Go trials, 9 omitted: omission 0.1.
        val noGo = List(10) { GradCptTrial(isNoGo = true, responded = it == 0, reactionTimeMillis = null) }
        val go = List(90) { GradCptTrial(isNoGo = false, responded = it >= 9, reactionTimeMillis = 400L + it) }
        val score = AssessmentScorer.gradCpt(noGo + go)

        assertEquals(0.1, score.commissionErrorRate!!, 1e-9)
        assertEquals(0.1, score.omissionErrorRate!!, 1e-9)
        assertTrue("was ${score.dPrime}", score.dPrime in 2.2..2.5)
    }

    @Test
    fun `gradCPT reaction time variability uses only responded Go trials`() {
        val go = listOf(
            GradCptTrial(isNoGo = false, responded = true, reactionTimeMillis = 400L),
            GradCptTrial(isNoGo = false, responded = true, reactionTimeMillis = 600L),
            GradCptTrial(isNoGo = false, responded = false, reactionTimeMillis = null)
        )
        val score = AssessmentScorer.gradCpt(go)

        assertEquals(
            SignalDetection.coefficientOfVariation(listOf(400L, 600L))!!,
            score.reactionTimeVariability!!,
            1e-9
        )
    }

    @Test
    fun `ANT network effects recover the built-in differences`() {
        // rt = 500 + cue effect + flanker effect, crossed over all cues and flankers, so the
        // expected effects are exact: alerting 50, orienting 60, conflict 80.
        val cueEffect = mapOf(
            AntCue.NONE to 0L,
            AntCue.DOUBLE to -50L,
            AntCue.CENTER to -20L,
            AntCue.SPATIAL to -80L
        )
        val trials = AntCue.entries.flatMap { cue ->
            listOf(AntFlanker.CONGRUENT, AntFlanker.INCONGRUENT).map { flanker ->
                val flankerEffect = if (flanker == AntFlanker.INCONGRUENT) 80L else 0L
                AntTrial(cue, flanker, correct = true, reactionTimeMillis = 500L + cueEffect.getValue(cue) + flankerEffect)
            }
        }
        val score = AssessmentScorer.ant(trials)

        assertEquals(50.0, score.alerting!!, 1e-9)
        assertEquals(60.0, score.orienting!!, 1e-9)
        assertEquals(80.0, score.conflict!!, 1e-9)
        assertEquals(1.0, score.accuracy!!, 1e-9)
    }

    @Test
    fun `ANT ignores incorrect trials in the network effects`() {
        val correct = listOf(
            AntTrial(AntCue.NONE, AntFlanker.CONGRUENT, correct = true, reactionTimeMillis = 500L),
            AntTrial(AntCue.DOUBLE, AntFlanker.CONGRUENT, correct = true, reactionTimeMillis = 450L)
        )
        val slowError = AntTrial(AntCue.NONE, AntFlanker.CONGRUENT, correct = false, reactionTimeMillis = 3000L)
        val score = AssessmentScorer.ant(correct + slowError)

        assertEquals(50.0, score.alerting!!, 1e-9)
        assertEquals(2.0 / 3.0, score.accuracy!!, 1e-9)
    }

    @Test
    fun `ANT effect is null when a condition has no correct trials`() {
        val onlyNoCue = listOf(AntTrial(AntCue.NONE, AntFlanker.CONGRUENT, correct = true, reactionTimeMillis = 500L))
        val score = AssessmentScorer.ant(onlyNoCue)

        assertNull(score.alerting)
        assertNull(score.conflict)
    }

    @Test
    fun `n-back d prime and rates come from hits and false alarms`() {
        // 2-back: 10 targets with 8 hits, 40 non-targets with 4 false alarms.
        val targets = List(10) { NBackTrial(isTarget = true, respondedMatch = it < 8) }
        val nonTargets = List(40) { NBackTrial(isTarget = false, respondedMatch = it < 4) }
        val score = AssessmentScorer.nBack(targets + nonTargets)

        assertEquals(SignalDetection.dPrime(8, 2, 4, 36), score.dPrime, 1e-9)
        assertEquals(0.8, score.hitRate!!, 1e-9)
        assertEquals(0.1, score.falseAlarmRate!!, 1e-9)
    }
}
