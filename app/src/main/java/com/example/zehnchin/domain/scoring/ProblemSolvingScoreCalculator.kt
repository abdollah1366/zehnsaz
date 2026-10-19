package com.example.zehnchin.domain.scoring

import kotlin.math.max

object ProblemSolvingScoreCalculator {

    data class PlanningScoreResult(
        val finalScore: Int,
        val efficiency: Int,
        val moves: Int,
        val optimalMoves: Int,
        val stars: Int
    )

    fun calculate(
        playerMoves: Int,
        optimalMoves: Int,
        difficultyLevel: Int,
        hintUsage: Int
    ): PlanningScoreResult {
        val efficiency = if (playerMoves > 0) (optimalMoves.toFloat() / playerMoves * 100).toInt() else 0
        
        // Base Score: Efficiency * 10 - (Hints * 20)
        val baseScore = (efficiency * 10) - (hintUsage * 20)
        val finalScore = (maxOf(0, baseScore) * (1 + (difficultyLevel * 0.2f))).toInt()

        val stars = when {
            efficiency >= 90 && hintUsage == 0 -> 3
            efficiency >= 75 -> 2
            efficiency >= 50 -> 1
            else -> 1 // Completed but inefficient
        }

        return PlanningScoreResult(
            finalScore = finalScore,
            efficiency = efficiency,
            moves = playerMoves,
            optimalMoves = optimalMoves,
            stars = stars
        )
    }
}