package com.example.zehnchin.domain.model

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

enum class GameType {
    COLOR_SORT,
    SELECTIVE_ATTENTION,
    WORKING_MEMORY,
    PROCESSING_SPEED,
    INHIBITORY_CONTROL,
    COGNITIVE_FLEXIBILITY,
    PROBLEM_SOLVING
}

enum class CognitiveDomain {
    ATTENTION,
    MEMORY,
    EXECUTIVE_FUNCTION,
    SPEED,
    LOGIC
}

enum class Difficulty {
    EASY, MEDIUM, HARD, EXPERT
}

@Serializable
sealed class LevelConfig {
    abstract val type: GameType

    @Serializable
    data class ColorSortConfig(
        override val type: GameType = GameType.COLOR_SORT,
        val tubeCapacity: Int = 4,
        val numberOfColors: Int,
        val numberOfEmptyTubes: Int = 2,
        val initialTubes: List<List<PuzzleColor>> = emptyList()
    ) : LevelConfig()

    @Serializable
    data class SelectiveAttentionConfig(
        override val type: GameType = GameType.SELECTIVE_ATTENTION,
        val targetColor: PuzzleColor,
        val targetShape: Shape,
        val distractorCount: Int,
        val similarityLevel: SimilarityLevel,
        val targetCount: Int = 1,
        val trialCount: Int = 10,
        val responseTimeLimit: Long = 10000, // ms
        val gridRows: Int = 3,
        val gridCols: Int = 3,
        val penaltyMillis: Long = 1000,
        val targetScale: Float? = null // null means size doesn't matter
    ) : LevelConfig()

    @Serializable
    data class WorkingMemoryConfig(
        override val type: GameType = GameType.WORKING_MEMORY,
        val taskType: MemoryTaskType = MemoryTaskType.SEQUENCE_RECALL,
        val memoryLength: Int,
        val encodingDuration: Long, // ms
        val recallTimeLimit: Long, // ms
        val numberOfChoices: Int,
        val stimuliColors: List<PuzzleColor> = PuzzleColor.entries,
        val stimuliShapes: List<Shape> = Shape.entries
    ) : LevelConfig()

    @Serializable
    data class ProcessingSpeedConfig(
        override val type: GameType = GameType.PROCESSING_SPEED,
        val roundCount: Int,
        val stimulusDuration: Long, // ms
        val responseTimeLimit: Long, // ms
        val targetProbability: Float = 0.5f,
        val distractorCount: Int = 3
    ) : LevelConfig()

    @Serializable
    data class InhibitoryControlConfig(
        override val type: GameType = GameType.INHIBITORY_CONTROL,
        val roundCount: Int,
        val goProbability: Float,
        val noGoProbability: Float,
        val stimulusDuration: Long, // ms
        val responseWindow: Long, // ms
        val stimulusSimilarity: SimilarityLevel = SimilarityLevel.LOW
    ) : LevelConfig()

    @Serializable
    data class CognitiveFlexibilityConfig(
        override val type: GameType = GameType.COGNITIVE_FLEXIBILITY,
        val roundCount: Int,
        val ruleTypes: List<RuleType>,
        val switchInterval: Int, // rounds between switches
        val responseTimeLimit: Long,
        val ruleChangeWarning: Boolean = true
    ) : LevelConfig()

    @Serializable
    data class ProblemSolvingConfig(
        override val type: GameType = GameType.PROBLEM_SOLVING,
        val initialTubes: List<List<PuzzleColor>>,
        val tubeCapacity: Int = 4,
        val maxMoves: Int,
        val optimalMoves: Int
    ) : LevelConfig()
}

enum class RuleType {
    COLOR_MATCH,
    SHAPE_MATCH,
    COLOR_OPPOSITE,
    SHAPE_OPPOSITE
}

enum class MemoryTaskType {
    SEQUENCE_RECALL,
    POSITION_RECALL
}

enum class SimilarityLevel {
    LOW, MEDIUM, HIGH
}

data class Level(
    val id: Long = 0,
    val chapterId: Int,
    val levelNumber: Int,
    val difficulty: Int, // 1 to 10
    val config: LevelConfig,
    val rewardCoins: Int = 10,
    val rewardGems: Int = 0,
    val starsRequired: Int = 0
)

data class Chapter(
    val id: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val cognitiveDomain: CognitiveDomain,
    @StringRes val descriptionRes: Int = 0
)