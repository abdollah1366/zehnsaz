package com.example.zehnchin.domain.assessment

/**
 * Flattens a task's score into named measures for storage.
 * A measure that could not be computed (null) is left out instead of being stored as zero.
 */
object AssessmentMeasures {

    fun of(score: GradCptScore): Map<String, Double> = buildMap {
        put("dPrime", score.dPrime)
        score.commissionErrorRate?.let { put("commissionErrorRate", it) }
        score.omissionErrorRate?.let { put("omissionErrorRate", it) }
        score.reactionTimeVariability?.let { put("reactionTimeVariability", it) }
    }

    fun of(score: AntScore): Map<String, Double> = buildMap {
        score.alerting?.let { put("alerting", it) }
        score.orienting?.let { put("orienting", it) }
        score.conflict?.let { put("conflict", it) }
        score.accuracy?.let { put("accuracy", it) }
    }

    fun of(score: NBackScore): Map<String, Double> = buildMap {
        put("dPrime", score.dPrime)
        score.hitRate?.let { put("hitRate", it) }
        score.falseAlarmRate?.let { put("falseAlarmRate", it) }
    }
}
