package com.example.zehnchin.domain.model

data class PlanningPuzzleState(
    val tubes: List<Tube>,
    val movesMade: Int = 0,
    val remainingMoves: Int,
    val selectedTubeId: Int? = null,
    val isSolved: Boolean = false,
    val isDeadlock: Boolean = false
) {
    fun isWinningState(): Boolean {
        return tubes.all { it.isSolved() }
    }
}