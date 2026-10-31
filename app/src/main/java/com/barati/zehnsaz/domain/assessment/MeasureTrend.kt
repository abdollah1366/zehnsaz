package com.barati.zehnsaz.domain.assessment

import com.barati.zehnsaz.data.local.entities.AssessmentMeasureEntity

data class TrendPoint(val takenAt: Long, val value: Double)

/** One measure of one task, followed over the sessions in which it was taken, oldest first. */
data class MeasureTrend(val task: AssessmentTask, val measure: String, val points: List<TrendPoint>)

object MeasureTrends {

    fun build(measures: List<AssessmentMeasureEntity>): List<MeasureTrend> {
        return measures
            .groupBy { it.task to it.measure }
            .map { (key, group) ->
                MeasureTrend(
                    task = AssessmentTask.valueOf(key.first),
                    measure = key.second,
                    points = group
                        .sortedBy { it.takenAt }
                        .map { TrendPoint(takenAt = it.takenAt, value = it.measureValue) }
                )
            }
            .sortedWith(compareBy({ it.task.ordinal }, { it.measure }))
    }
}
