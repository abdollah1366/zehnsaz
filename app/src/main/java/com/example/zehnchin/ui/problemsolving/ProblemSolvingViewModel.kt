package com.example.zehnchin.ui.problemsolving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.domain.engine.ProblemSolvingEngine
import com.example.zehnchin.domain.model.*
import com.example.zehnchin.domain.progression.GameSessionManager
import com.example.zehnchin.domain.scoring.ProblemSolvingScoreCalculator
import com.example.zehnchin.ui.common.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProblemSolvingViewModel @Inject constructor(
    private val sessionManager: GameSessionManager,
    private val soundManager: SoundManager
) : ViewModel() {

    private val engine = ProblemSolvingEngine()
    val gameState: StateFlow<PlanningPuzzleState> = engine.gameState

    private var currentLevel: Level? = null
    private var hintCount = 0
    private var isFinishing = false
    private var attemptStartTime = 0L

    init {
        viewModelScope.launch {
            gameState.collect { state ->
                if (state.isSolved && !isFinishing) {
                    isFinishing = true
                    val results = calculateResults()
                    currentLevel?.let {
                        // Only solved attempts are stored for now; the puzzle has no per-move log yet.
                        sessionManager.recordAttempt(it, isWon = true, startTime = attemptStartTime, results = results, trials = emptyList())
                        sessionManager.completeLevel(it, results.stars, results.finalScore)
                        soundManager.play(SoundManager.SoundEvent.LEVEL_COMPLETE)
                    }
                }
            }
        }
    }

    fun loadLevel(level: Level) {
        currentLevel = level
        isFinishing = false
        attemptStartTime = System.currentTimeMillis()
        engine.loadLevel(level)
        hintCount = 0
    }

    fun onTubeClick(tubeId: Int) {
        val state = gameState.value
        if (state.selectedTubeId == null) {
            soundManager.play(SoundManager.SoundEvent.BUTTON_CLICK)
        } else {
            // Check if move is valid (simplified logic for sound)
            val fromTube = state.tubes.find { it.id == state.selectedTubeId }
            val toTube = state.tubes.find { it.id == tubeId }
            
            if (fromTube != null && toTube != null && fromTube.id != toTube.id) {
                if (fromTube.layers.isNotEmpty() && toTube.layers.size < 4) {
                    soundManager.play(SoundManager.SoundEvent.CORRECT)
                } else {
                    soundManager.play(SoundManager.SoundEvent.INVALID_MOVE)
                }
            } else {
                soundManager.play(SoundManager.SoundEvent.BUTTON_CLICK)
            }
        }
        engine.onTubeSelected(tubeId)
    }

    fun undo() {
        soundManager.play(SoundManager.SoundEvent.UNDO)
        engine.undo()
    }

    fun reset() {
        soundManager.play(SoundManager.SoundEvent.BUTTON_CLICK)
        engine.reset()
        hintCount = 0
        isFinishing = false
    }

    fun getHint(): Pair<Int, Int>? {
        soundManager.play(SoundManager.SoundEvent.HINT)
        hintCount++
        return engine.getHint()
    }

    fun calculateResults(): CognitiveScoreResult {
        val state = gameState.value
        val level = currentLevel
        val config = level?.config as? LevelConfig.ProblemSolvingConfig
        
        val res = ProblemSolvingScoreCalculator.calculate(
            playerMoves = state.movesMade,
            optimalMoves = config?.optimalMoves ?: 10,
            difficultyLevel = level?.difficulty ?: 1,
            hintUsage = hintCount
        )
        return CognitiveScoreResult(res.finalScore, res.efficiency, res.moves, res.stars)
    }
}
