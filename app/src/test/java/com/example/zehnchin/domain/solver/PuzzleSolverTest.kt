package com.example.zehnchin.domain.solver

import com.example.zehnchin.domain.model.Layer
import com.example.zehnchin.domain.model.PuzzleColor
import com.example.zehnchin.domain.model.Tube
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleSolverTest {

    @Test
    fun `test solver finds solution for simple puzzle`() {
        val tubes = listOf(
            Tube(id = 0, capacity = 2, layers = listOf(Layer(PuzzleColor.RED), Layer(PuzzleColor.BLUE))),
            Tube(id = 1, capacity = 2, layers = listOf(Layer(PuzzleColor.RED), Layer(PuzzleColor.BLUE))),
            Tube(id = 2, capacity = 2, layers = emptyList())
        )
        
        val solution = PuzzleSolver.solve(tubes)
        assertNotNull(solution)
        assertTrue(solution!!.size >= 2)
    }

    @Test
    fun `test solver identifies optimal moves`() {
        // Red, Blue | Red, Blue | Empty
        // Optimal: 
        // 1. Move Blue from Tube 0 to Tube 2
        // 2. Move Blue from Tube 1 to Tube 2
        // 3. Move Red from Tube 0 to Tube 1
        // Result: Empty | Red, Red | Blue, Blue (solved)
        
        val tubes = listOf(
            Tube(id = 0, capacity = 2, layers = listOf(Layer(PuzzleColor.RED), Layer(PuzzleColor.BLUE))),
            Tube(id = 1, capacity = 2, layers = listOf(Layer(PuzzleColor.RED), Layer(PuzzleColor.BLUE))),
            Tube(id = 2, capacity = 2, layers = emptyList())
        )
        
        val optimalMoves = PuzzleSolver.solve(tubes)
        assertEquals(3, optimalMoves?.size)
    }
}