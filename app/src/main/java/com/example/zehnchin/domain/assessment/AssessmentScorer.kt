package com.example.zehnchin.domain.assessment

import com.example.zehnchin.domain.scoring.SignalDetection

/** Turns raw assessment trials into the measures the baseline reports. */
object AssessmentScorer {

    /**
     * d′ treats the rare No-Go stimulus as the signal: withholding a response to it is a hit,
     * and responding to it is a miss (a commission error).
     */
    fun gradCpt(trials: List<GradCptTrial>): GradCptScore {
        val noGo = trials.filter { it.isNoGo }
        val go = trials.filter { !it.isNoGo }
        val noGoResponded = noGo.count { it.responded }
        val goOmitted = go.count { !it.responded }
        val goRts = go.filter { it.responded }.mapNotNull { it.reactionTimeMillis }
        return GradCptScore(
            dPrime = SignalDetection.dPrime(
                hits = noGo.size - noGoResponded,
                misses = noGoResponded,
                falseAlarms = goOmitted,
                correctRejections = go.size - goOmitted
            ),
            commissionErrorRate = rate(noGoResponded, noGo.size),
            omissionErrorRate = rate(goOmitted, go.size),
            reactionTimeVariability = SignalDetection.coefficientOfVariation(goRts)
        )
    }

    /** Network effects use correct trials only, so a slow error does not distort a condition's mean. */
    fun ant(trials: List<AntTrial>): AntScore {
        val correct = trials.filter { it.correct }

        fun meanRt(condition: (AntTrial) -> Boolean): Double? {
            val rts = correct.filter(condition).map { it.reactionTimeMillis }
            return if (rts.isEmpty()) null else rts.average()
        }

        val noCue = meanRt { it.cue == AntCue.NONE }
        val doubleCue = meanRt { it.cue == AntCue.DOUBLE }
        val centerCue = meanRt { it.cue == AntCue.CENTER }
        val spatialCue = meanRt { it.cue == AntCue.SPATIAL }
        val congruent = meanRt { it.flanker == AntFlanker.CONGRUENT }
        val incongruent = meanRt { it.flanker == AntFlanker.INCONGRUENT }

        return AntScore(
            alerting = difference(noCue, doubleCue),
            orienting = difference(centerCue, spatialCue),
            conflict = difference(incongruent, congruent),
            accuracy = rate(correct.size, trials.size)
        )
    }

    fun nBack(trials: List<NBackTrial>): NBackScore {
        val targets = trials.filter { it.isTarget }
        val nonTargets = trials.filter { !it.isTarget }
        val hits = targets.count { it.respondedMatch }
        val falseAlarms = nonTargets.count { it.respondedMatch }
        return NBackScore(
            dPrime = SignalDetection.dPrime(
                hits = hits,
                misses = targets.size - hits,
                falseAlarms = falseAlarms,
                correctRejections = nonTargets.size - falseAlarms
            ),
            hitRate = rate(hits, targets.size),
            falseAlarmRate = rate(falseAlarms, nonTargets.size)
        )
    }

    private fun rate(count: Int, total: Int): Double? =
        if (total == 0) null else count.toDouble() / total

    private fun difference(slower: Double?, faster: Double?): Double? =
        if (slower == null || faster == null) null else slower - faster
}
