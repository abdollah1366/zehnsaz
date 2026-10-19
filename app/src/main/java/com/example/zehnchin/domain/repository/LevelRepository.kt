package com.example.zehnchin.domain.repository

import com.example.zehnchin.data.local.ProblemSolvingLevels
import com.example.zehnchin.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

import java.util.Random

@Singleton
class LevelRepository @Inject constructor() {

    fun getLevelsForChapter(chapterId: Int): List<Level> {
        return when (chapterId) {
            0 -> generateTutorialLevels()
            1 -> (1..20).map { generateAttentionLevel(it) }
            2 -> (1..20).map { generateMemoryLevel(it) }
            3 -> (1..20).map { generateSpeedLevel(it) }
            4 -> (1..20).map { generateControlLevel(it) }
            5 -> (1..20).map { generateFlexibilityLevel(it) }
            6 -> ProblemSolvingLevels.levels
            else -> emptyList()
        }
    }

    fun getLevel(chapterId: Int, levelNumber: Int): Level? {
        return when (chapterId) {
            0 -> generateTutorialLevels().find { it.levelNumber == levelNumber }
            1 -> generateAttentionLevel(levelNumber)
            2 -> generateMemoryLevel(levelNumber)
            3 -> generateSpeedLevel(levelNumber)
            4 -> generateControlLevel(levelNumber)
            5 -> generateFlexibilityLevel(levelNumber)
            6 -> ProblemSolvingLevels.levels.find { it.levelNumber == levelNumber }
            else -> null
        }
    }

    private fun generateTutorialLevels(): List<Level> {
        // Levels to teach Color, Shape, Size
        return listOf(
            Level(
                id = 1, chapterId = 0, levelNumber = 1, difficulty = 1,
                config = LevelConfig.SelectiveAttentionConfig(
                    targetColor = PuzzleColor.RED, targetShape = Shape.CIRCLE, distractorCount = 1,
                    similarityLevel = SimilarityLevel.LOW, gridRows = 1, gridCols = 2, trialCount = 2
                )
            ),
            Level(
                id = 2, chapterId = 0, levelNumber = 2, difficulty = 1,
                config = LevelConfig.SelectiveAttentionConfig(
                    targetColor = PuzzleColor.BLUE, targetShape = Shape.SQUARE, distractorCount = 3,
                    similarityLevel = SimilarityLevel.LOW, gridRows = 2, gridCols = 2, trialCount = 3
                )
            ),
            Level(
                id = 3, chapterId = 0, levelNumber = 3, difficulty = 1,
                config = LevelConfig.SelectiveAttentionConfig(
                    targetColor = PuzzleColor.GREEN, targetShape = Shape.TRIANGLE, distractorCount = 3,
                    similarityLevel = SimilarityLevel.LOW, gridRows = 2, gridCols = 2, trialCount = 3
                )
            ),
            Level(
                id = 4, chapterId = 0, levelNumber = 4, difficulty = 1,
                config = LevelConfig.SelectiveAttentionConfig(
                    targetColor = PuzzleColor.YELLOW, targetShape = Shape.STAR, distractorCount = 3,
                    similarityLevel = SimilarityLevel.LOW, gridRows = 2, gridCols = 2, trialCount = 3,
                    targetScale = 0.7f // Small
                )
            )
        )
    }

    private fun generateAttentionLevel(n: Int): Level {
        val random = Random(n.toLong() + 1000) // Deterministic seed
        val colors = PuzzleColor.entries
        val shapes = Shape.entries
        
        return Level(
            id = 1000L + n,
            chapterId = 1,
            levelNumber = n,
            difficulty = (n + 1) / 2,
            config = LevelConfig.SelectiveAttentionConfig(
                targetColor = colors[random.nextInt(colors.size)],
                targetShape = shapes[random.nextInt(shapes.size)],
                distractorCount = 4 + n,
                similarityLevel = if (n < 10) SimilarityLevel.LOW else SimilarityLevel.MEDIUM,
                gridRows = 3 + (n / 10),
                gridCols = 3 + (n / 10),
                trialCount = 10
            )
        )
    }

    private fun generateMemoryLevel(n: Int): Level {
        val sequenceLength = when (n) {
            in 1..3 -> 3
            in 4..6 -> 4
            in 7..10 -> 5
            in 11..14 -> 6
            in 15..17 -> 7
            else -> 8
        }
        val memorizationTime = when (n) {
            in 1..3 -> 3000L
            in 4..7 -> 4500L
            in 8..12 -> 6000L
            in 13..16 -> 7500L
            else -> 9000L
        }
        
        return Level(
            id = 2000L + n,
            chapterId = 2,
            levelNumber = n,
            difficulty = (n + 1) / 2,
            config = LevelConfig.WorkingMemoryConfig(
                memoryLength = sequenceLength,
                encodingDuration = memorizationTime,
                recallTimeLimit = 15000L,
                numberOfChoices = (4 + (n / 2)).coerceAtMost(12)
            )
        )
    }

    private fun generateSpeedLevel(n: Int): Level {
        return Level(
            id = 3000L + n,
            chapterId = 3,
            levelNumber = n,
            difficulty = (n + 1) / 2,
            config = LevelConfig.ProcessingSpeedConfig(
                roundCount = 10 + n,
                stimulusDuration = (1500 - (n * 50L)).coerceAtLeast(500L),
                responseTimeLimit = (2000 - (n * 40L)).coerceAtLeast(800L)
            )
        )
    }

    private fun generateControlLevel(n: Int): Level {
        return Level(
            id = 4000L + n,
            chapterId = 4,
            levelNumber = n,
            difficulty = (n + 1) / 2,
            config = LevelConfig.InhibitoryControlConfig(
                roundCount = 10 + n,
                // Go trials are frequent (about 80%), which builds the prepotent response that No-Go must override.
                goProbability = 0.8f,
                noGoProbability = 0.2f,
                stimulusDuration = (1200 - (n * 30L)).coerceAtLeast(600L),
                responseWindow = (1500 - (n * 40L)).coerceAtLeast(700L)
            )
        )
    }

    private fun generateFlexibilityLevel(n: Int): Level {
        return Level(
            id = 5000L + n,
            chapterId = 5,
            levelNumber = n,
            difficulty = (n + 1) / 2,
            config = LevelConfig.CognitiveFlexibilityConfig(
                roundCount = 10 + n,
                ruleTypes = listOf(RuleType.COLOR_MATCH, RuleType.SHAPE_MATCH),
                switchInterval = if (n < 10) 4 else 3,
                responseTimeLimit = (3500 - (n * 100L)).coerceAtLeast(1500L)
            )
        )
    }
}
