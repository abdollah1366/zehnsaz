package com.example.zehnchin.domain.progression

import com.example.zehnchin.data.local.entities.LevelAttemptEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class WeeklyPlannerTest {

    private val weekStart = 1_000_000_000_000L
    private val earlier = weekStart - 1_000L
    private val thisWeek = weekStart + 1_000L

    private fun attempt(chapterId: Int, accuracy: Int, startTime: Long) = LevelAttemptEntity(
        playerId = "default_player",
        levelId = 1L,
        chapterId = chapterId,
        startTime = startTime,
        endTime = startTime,
        isWon = true,
        stars = 1,
        score = 0,
        accuracy = accuracy
    )

    @Test
    fun `the three weakest chapters come first and a never-played chapter counts as weakest`() {
        val attempts = listOf(
            attempt(1, accuracy = 90, startTime = earlier),
            attempt(2, accuracy = 40, startTime = earlier),
            attempt(3, accuracy = 70, startTime = earlier),
            attempt(5, accuracy = 60, startTime = earlier),
            attempt(6, accuracy = 80, startTime = earlier)
        )

        val plan = WeeklyPlanner.build(attempts, weekStart)

        assertEquals(listOf(4, 2, 5), plan.sessions.map { it.chapterId })
        assertEquals(0, plan.completedCount)
    }

    @Test
    fun `a planned chapter played this week is marked done`() {
        val attempts = listOf(
            attempt(2, accuracy = 40, startTime = earlier),
            attempt(2, accuracy = 55, startTime = thisWeek)
        )

        val plan = WeeklyPlanner.build(attempts, weekStart)
        val chapterTwo = plan.sessions.single { it.chapterId == 2 }

        assertEquals(true, chapterTwo.done)
        assertEquals(1, plan.completedCount)
    }

    @Test
    fun `playing this week does not change which chapters are planned`() {
        val before = listOf(attempt(2, accuracy = 40, startTime = earlier))
        val after = before + attempt(4, accuracy = 99, startTime = thisWeek)

        val planBefore = WeeklyPlanner.build(before, weekStart).sessions.map { it.chapterId }
        val planAfter = WeeklyPlanner.build(after, weekStart).sessions.map { it.chapterId }

        assertEquals(planBefore, planAfter)
    }

    @Test
    fun `week starts at midnight on the chosen first day`() {
        val wednesday = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, 21, 15, 30, 0)
        }.timeInMillis
        val monday = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, 19, 0, 0, 0)
        }.timeInMillis
        val saturday = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, 17, 0, 0, 0)
        }.timeInMillis

        assertEquals(monday, WeeklyPlanner.weekStart(wednesday, Calendar.MONDAY))
        assertEquals(saturday, WeeklyPlanner.weekStart(wednesday, Calendar.SATURDAY))
    }
}
