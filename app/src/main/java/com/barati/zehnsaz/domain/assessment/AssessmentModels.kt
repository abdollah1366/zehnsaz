package com.barati.zehnsaz.domain.assessment

/**
 * The short baseline battery. These tasks are kept apart from training games, so the
 * measured score is not inflated by practice on the exact game being trained.
 */
enum class AssessmentTask { GRAD_CPT, ANT, N_BACK }

/** One gradCPT trial. [isNoGo] marks the rare stimulus the player must withhold a response to. */
data class GradCptTrial(val isNoGo: Boolean, val responded: Boolean, val reactionTimeMillis: Long?)

data class GradCptScore(
    val dPrime: Double,
    val commissionErrorRate: Double?,
    val omissionErrorRate: Double?,
    val reactionTimeVariability: Double?
)

enum class AntCue { NONE, DOUBLE, CENTER, SPATIAL }

enum class AntFlanker { CONGRUENT, INCONGRUENT, NEUTRAL }

data class AntTrial(
    val cue: AntCue,
    val flanker: AntFlanker,
    val correct: Boolean,
    val reactionTimeMillis: Long
)

/**
 * Attention network effects in milliseconds, following Fan et al. (2002):
 * alerting = no cue − double cue, orienting = centre cue − spatial cue,
 * conflict = incongruent − congruent flankers. Larger alerting and orienting are better;
 * a smaller conflict effect is better. Null when a condition has no correct trials.
 */
data class AntScore(
    val alerting: Double?,
    val orienting: Double?,
    val conflict: Double?,
    val accuracy: Double?
)

/** One n-back trial. [isTarget] is true when the item matches the one [n] steps back. */
data class NBackTrial(val isTarget: Boolean, val respondedMatch: Boolean)

data class NBackScore(val dPrime: Double, val hitRate: Double?, val falseAlarmRate: Double?)
