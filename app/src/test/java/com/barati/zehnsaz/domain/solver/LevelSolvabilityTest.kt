package com.barati.zehnsaz.domain.solver

import com.barati.zehnsaz.data.local.ProblemSolvingLevels
import com.barati.zehnsaz.domain.model.Layer
import com.barati.zehnsaz.domain.model.LevelConfig
import com.barati.zehnsaz.domain.model.Tube
import org.junit.Assert.assertNotNull
import org.junit.Test

class LevelSolvabilityTest {

    @Test
    fun `test levels 1 to 15 are solvable`() {
        ProblemSolvingLevels.levels.take(15).forEach { level ->
            val config = level.config as LevelConfig.ProblemSolvingConfig
            val tubes = config.initialTubes.mapIndexed { index, colors ->
                Tube(id = index, capacity = config.tubeCapacity, layers = colors.map { Layer(it) })
            }
            
            val solution = PuzzleSolver.solve(tubes)
            assertNotNull("Level ${level.levelNumber} is unsolvable!", solution)
        }
    }
}