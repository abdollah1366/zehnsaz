package com.barati.zehnsaz.domain.progression

import com.barati.zehnsaz.data.local.entities.LevelAttemptEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class AttemptSummaryTest {

    private fun attempt(chapterId: Int, score: Int, accuracy: Int, stars: Int) = LevelAttemptEntity(
        playerId = "default_player",
        levelId = 0,
        chapterId = chapterId,
        startTime = 0,
        endTime = 0,
        isWon = true,
        stars = stars,
        score = score,
        accuracy = accuracy
    )

    @Test
    fun `groups attempts by chapter and summarises each group`() {
        val summaries = AttemptSummary.summarize(
            listOf(
                attempt(chapterId = 1, score = 500, accuracy = 80, stars = 2),
                attempt(chapterId = 1, score = 700, accuracy = 90, stars = 1),
                attempt(chapterId = 2, score = 300, accuracy = 60, stars = 3)
            )
        )

        assertEquals(2, summaries.size)
        val attention = summaries[0]
        assertEquals(1, attention.chapterId)
        assertEquals(2, attention.attemptCount)
        assertEquals(700, attention.bestScore)
        assertEquals(85, attention.averageAccuracy)
        assertEquals(2, attention.bestStars)
        assertEquals(3, summaries[1].bestStars)
    }

    @Test
    fun `no attempts gives no summaries`() {
        assertEquals(emptyList<DomainSummary>(), AttemptSummary.summarize(emptyList()))
    }
}
