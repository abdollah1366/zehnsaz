package com.example.zehnchin.domain.assessment

import com.example.zehnchin.data.local.entities.AssessmentMeasureEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MeasureTrendsTest {

    private fun measure(task: AssessmentTask, name: String, takenAt: Long, value: Double) =
        AssessmentMeasureEntity(
            playerId = "default_player",
            task = task.name,
            takenAt = takenAt,
            measure = name,
            measureValue = value
        )

    @Test
    fun `points of one measure are grouped and sorted by time`() {
        val rows = listOf(
            measure(AssessmentTask.GRAD_CPT, "dPrime", takenAt = 200L, value = 2.0),
            measure(AssessmentTask.GRAD_CPT, "dPrime", takenAt = 100L, value = 1.5)
        )
        val trends = MeasureTrends.build(rows)

        assertEquals(1, trends.size)
        assertEquals(listOf(100L, 200L), trends.single().points.map { it.takenAt })
        assertEquals(listOf(1.5, 2.0), trends.single().points.map { it.value })
    }

    @Test
    fun `trends are ordered by task and then by measure name`() {
        val rows = listOf(
            measure(AssessmentTask.N_BACK, "dPrime", 1L, 1.0),
            measure(AssessmentTask.GRAD_CPT, "omissionErrorRate", 1L, 0.1),
            measure(AssessmentTask.GRAD_CPT, "dPrime", 1L, 2.0)
        )
        val trends = MeasureTrends.build(rows)

        assertEquals(
            listOf("GRAD_CPT dPrime", "GRAD_CPT omissionErrorRate", "N_BACK dPrime"),
            trends.map { "${it.task.name} ${it.measure}" }
        )
    }
}
