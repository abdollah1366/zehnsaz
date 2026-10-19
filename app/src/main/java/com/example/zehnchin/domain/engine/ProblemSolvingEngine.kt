package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.*
import com.example.zehnchin.domain.solver.PuzzleSolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

class ProblemSolvingEngine : BaseGameEngine<PlanningPuzzleState> {

    private val _gameState = MutableStateFlow(PlanningPuzzleState(emptyList(), 0, 0))
    override val gameState: StateFlow<PlanningPuzzleState> = _gameState.asStateFlow()

    private val history = Stack<PlanningPuzzleState>()

    override fun loadLevel(level: Level) {
        val config = level.config as? LevelConfig.ProblemSolvingConfig ?: return

        val tubes = config.initialTubes.mapIndexed { index, colors ->
            Tube(
                id = index,
                capacity = config.tubeCapacity,
                layers = colors.map { Layer(it) }
            )
        }

        val initialState = PlanningPuzzleState(
            tubes = tubes,
            movesMade = 0,
            remainingMoves = config.maxMoves,
            isSolved = false,
            isDeadlock = false
        )

        history.clear()
        _gameState.value = initialState
    }

    fun onTubeSelected(tubeId: Int) {
        val state = _gameState.value
        if (state.isSolved || state.remainingMoves <= 0) return

        if (state.selectedTubeId == null) {
            // Select tube if not empty
            if (!state.tubes[tubeId].isEmpty) {
                _gameState.update { it.copy(selectedTubeId = tubeId) }
            }
        } else if (state.selectedTubeId == tubeId) {
            // Deselect
            _gameState.update { it.copy(selectedTubeId = null) }
        } else {
            // Try to move
            val sourceId = state.selectedTubeId
            val sourceTube = state.tubes[sourceId]
            val targetTube = state.tubes[tubeId]

            val layer = sourceTube.topLayer!!
            if (targetTube.canAccept(layer)) {
                performMove(sourceId, tubeId)
            } else {
                // Invalid move, just deselect
                _gameState.update { it.copy(selectedTubeId = null) }
            }
        }
    }

    private fun performMove(sourceId: Int, targetId: Int) {
        history.push(_gameState.value)
        
        _gameState.update { currentState ->
            val newTubes = currentState.tubes.toMutableList()
            val layer = newTubes[sourceId].topLayer!!
            
            newTubes[sourceId] = newTubes[sourceId].removeTopLayer()
            newTubes[targetId] = newTubes[targetId].addLayer(layer)

            val movesMade = currentState.movesMade + 1
            val remainingMoves = currentState.remainingMoves - 1
            val isSolved = newTubes.all { it.isSolved() }
            
            // Check for deadlock if not solved
            val isDeadlock = !isSolved && !PuzzleSolver.isSolvable(newTubes)

            currentState.copy(
                tubes = newTubes,
                movesMade = movesMade,
                remainingMoves = remainingMoves,
                selectedTubeId = null,
                isSolved = isSolved,
                isDeadlock = isDeadlock
            )
        }
    }

    fun undo() {
        if (history.isNotEmpty()) {
            _gameState.value = history.pop()
        }
    }

    fun getHint(): Pair<Int, Int>? {
        return PuzzleSolver.getNextOptimalMove(_gameState.value.tubes)
    }

    override fun reset() {
        // Reload current level if possible, or clear
        _gameState.value.let { state ->
            // In a real app, we'd store the initial state or the level object
            // For now, let's just clear if no history
            while (history.isNotEmpty()) {
                val first = history.firstElement()
                history.clear()
                _gameState.value = first
            }
        }
    }

    override fun pause() {}
    override fun resume() {}
}