package com.example.zehnchin.data.local

import com.example.zehnchin.domain.model.Level
import com.example.zehnchin.domain.model.LevelConfig
import com.example.zehnchin.domain.model.PuzzleColor

object ProblemSolvingLevels {
    val levels = listOf(
        Level(
            id = 6001, chapterId = 6, levelNumber = 1, difficulty = 1,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE), listOf(PuzzleColor.RED, PuzzleColor.BLUE), emptyList()),
                tubeCapacity = 2, maxMoves = 10, optimalMoves = 4
            )
        ),
        Level(
            id = 6002, chapterId = 6, levelNumber = 2, difficulty = 1,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW), listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW), emptyList()),
                tubeCapacity = 2, maxMoves = 10, optimalMoves = 4
            )
        ),
        Level(
            id = 6003, chapterId = 6, levelNumber = 3, difficulty = 2,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.RED), listOf(PuzzleColor.BLUE, PuzzleColor.RED, PuzzleColor.BLUE), emptyList()),
                tubeCapacity = 3, maxMoves = 15, optimalMoves = 6
            )
        ),
        Level(
            id = 6004, chapterId = 6, levelNumber = 4, difficulty = 2,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.GREEN), listOf(PuzzleColor.YELLOW, PuzzleColor.GREEN, PuzzleColor.YELLOW), emptyList()),
                tubeCapacity = 3, maxMoves = 15, optimalMoves = 6
            )
        ),
        Level(
            id = 6005, chapterId = 6, levelNumber = 5, difficulty = 3,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE), listOf(PuzzleColor.BLUE, PuzzleColor.RED), emptyList()),
                tubeCapacity = 2, maxMoves = 10, optimalMoves = 4
            )
        ),
        Level(
            id = 6006, chapterId = 6, levelNumber = 6, difficulty = 3,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW), listOf(PuzzleColor.YELLOW, PuzzleColor.GREEN), emptyList()),
                tubeCapacity = 2, maxMoves = 10, optimalMoves = 4
            )
        ),
        Level(
            id = 6007, chapterId = 6, levelNumber = 7, difficulty = 4,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.RED, PuzzleColor.BLUE), listOf(PuzzleColor.BLUE, PuzzleColor.BLUE, PuzzleColor.RED), emptyList()),
                tubeCapacity = 3, maxMoves = 15, optimalMoves = 6
            )
        ),
        Level(
            id = 6008, chapterId = 6, levelNumber = 8, difficulty = 4,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.GREEN, PuzzleColor.GREEN, PuzzleColor.YELLOW), listOf(PuzzleColor.YELLOW, PuzzleColor.YELLOW, PuzzleColor.GREEN), emptyList()),
                tubeCapacity = 3, maxMoves = 15, optimalMoves = 6
            )
        ),
        Level(
            id = 6009, chapterId = 6, levelNumber = 9, difficulty = 5,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN), listOf(PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.RED), listOf(PuzzleColor.GREEN, PuzzleColor.RED, PuzzleColor.BLUE), emptyList(), emptyList()),
                tubeCapacity = 3, maxMoves = 20, optimalMoves = 12
            )
        ),
        Level(
            id = 6010, chapterId = 6, levelNumber = 10, difficulty = 5,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.YELLOW, PuzzleColor.PURPLE, PuzzleColor.CYAN), listOf(PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.YELLOW), listOf(PuzzleColor.CYAN, PuzzleColor.YELLOW, PuzzleColor.PURPLE), emptyList(), emptyList()),
                tubeCapacity = 3, maxMoves = 20, optimalMoves = 12
            )
        ),
        Level(
            id = 6011, chapterId = 6, levelNumber = 11, difficulty = 6,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.RED, PuzzleColor.BLUE), listOf(PuzzleColor.BLUE, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.RED), emptyList()),
                tubeCapacity = 4, maxMoves = 25, optimalMoves = 14
            )
        ),
        Level(
            id = 6012, chapterId = 6, levelNumber = 12, difficulty = 6,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.GREEN, PuzzleColor.YELLOW), listOf(PuzzleColor.YELLOW, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.GREEN), emptyList()),
                tubeCapacity = 4, maxMoves = 25, optimalMoves = 14
            )
        ),
        Level(
            id = 6013, chapterId = 6, levelNumber = 13, difficulty = 7,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW), listOf(PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.RED), listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.RED, PuzzleColor.BLUE), listOf(PuzzleColor.YELLOW, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN), emptyList(), emptyList()),
                tubeCapacity = 4, maxMoves = 30, optimalMoves = 18
            )
        ),
        Level(
            id = 6014, chapterId = 6, levelNumber = 14, difficulty = 7,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.ORANGE, PuzzleColor.PINK), listOf(PuzzleColor.CYAN, PuzzleColor.ORANGE, PuzzleColor.PINK, PuzzleColor.PURPLE), listOf(PuzzleColor.ORANGE, PuzzleColor.PINK, PuzzleColor.PURPLE, PuzzleColor.CYAN), listOf(PuzzleColor.PINK, PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.ORANGE), emptyList(), emptyList()),
                tubeCapacity = 4, maxMoves = 30, optimalMoves = 18
            )
        ),
        Level(
            id = 6015, chapterId = 6, levelNumber = 15, difficulty = 8,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(
                    listOf(PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.RED, PuzzleColor.BLUE),
                    listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.GREEN, PuzzleColor.YELLOW),
                    listOf(PuzzleColor.RED, PuzzleColor.GREEN, PuzzleColor.RED, PuzzleColor.GREEN),
                    listOf(PuzzleColor.BLUE, PuzzleColor.YELLOW, PuzzleColor.BLUE, PuzzleColor.YELLOW),
                    emptyList(), emptyList()
                ),
                tubeCapacity = 4, maxMoves = 35, optimalMoves = 20
            )
        ),
        Level(
            id = 6016, chapterId = 6, levelNumber = 16, difficulty = 8,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(
                    listOf(PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.PURPLE, PuzzleColor.CYAN),
                    listOf(PuzzleColor.ORANGE, PuzzleColor.PINK, PuzzleColor.ORANGE, PuzzleColor.PINK),
                    listOf(PuzzleColor.PURPLE, PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.ORANGE),
                    listOf(PuzzleColor.CYAN, PuzzleColor.PINK, PuzzleColor.CYAN, PuzzleColor.PINK),
                    emptyList(), emptyList()
                ),
                tubeCapacity = 4, maxMoves = 35, optimalMoves = 20
            )
        ),
        Level(
            id = 6017, chapterId = 6, levelNumber = 17, difficulty = 9,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE), listOf(PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.RED), listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.RED, PuzzleColor.BLUE), listOf(PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN), listOf(PuzzleColor.ORANGE, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW), emptyList(), emptyList()),
                tubeCapacity = 5, maxMoves = 45, optimalMoves = 28
            )
        ),
        Level(
            id = 6018, chapterId = 6, levelNumber = 18, difficulty = 9,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.PINK, PuzzleColor.BROWN, PuzzleColor.GRAY), listOf(PuzzleColor.CYAN, PuzzleColor.PINK, PuzzleColor.BROWN, PuzzleColor.GRAY, PuzzleColor.PURPLE), listOf(PuzzleColor.PINK, PuzzleColor.BROWN, PuzzleColor.GRAY, PuzzleColor.PURPLE, PuzzleColor.CYAN), listOf(PuzzleColor.BROWN, PuzzleColor.GRAY, PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.PINK), listOf(PuzzleColor.GRAY, PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.PINK, PuzzleColor.BROWN), emptyList(), emptyList()),
                tubeCapacity = 5, maxMoves = 45, optimalMoves = 28
            )
        ),
        Level(
            id = 6019, chapterId = 6, levelNumber = 19, difficulty = 10,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE), listOf(PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.RED), listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.RED, PuzzleColor.BLUE), listOf(PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN), listOf(PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW), listOf(PuzzleColor.PURPLE, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE), emptyList(), emptyList()),
                tubeCapacity = 6, maxMoves = 60, optimalMoves = 35
            )
        ),
        Level(
            id = 6020, chapterId = 6, levelNumber = 20, difficulty = 10,
            config = LevelConfig.ProblemSolvingConfig(
                initialTubes = listOf(listOf(PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.CYAN), listOf(PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.RED), listOf(PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.RED, PuzzleColor.BLUE), listOf(PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN), listOf(PuzzleColor.ORANGE, PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW), listOf(PuzzleColor.PURPLE, PuzzleColor.CYAN, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE), listOf(PuzzleColor.CYAN, PuzzleColor.RED, PuzzleColor.BLUE, PuzzleColor.GREEN, PuzzleColor.YELLOW, PuzzleColor.ORANGE, PuzzleColor.PURPLE), emptyList(), emptyList()),
                tubeCapacity = 7, maxMoves = 75, optimalMoves = 45
            )
        )
    )
    
    fun getLevel(number: Int): Level? = levels.find { it.levelNumber == number }
}