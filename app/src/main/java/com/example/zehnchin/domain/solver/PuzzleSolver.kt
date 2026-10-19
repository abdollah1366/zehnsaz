package com.example.zehnchin.domain.solver

import com.example.zehnchin.domain.model.Layer
import com.example.zehnchin.domain.model.Tube
import java.util.*

object PuzzleSolver {

    private class StateNode(
        val tubes: List<Tube>,
        val lastMove: Pair<Int, Int>? = null,
        val parent: StateNode? = null
    ) {
        // Cached normalized state for equality and hashing
        private val normalizedTubes: List<List<com.example.zehnchin.domain.model.PuzzleColor>> by lazy {
            tubes.map { tube -> tube.layers.map { it.color } }.sortedBy { it.toString() }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StateNode) return false
            return normalizedTubes == other.normalizedTubes
        }

        override fun hashCode(): Int {
            return normalizedTubes.hashCode()
        }
        
        fun getPath(): List<Pair<Int, Int>> {
            val path = mutableListOf<Pair<Int, Int>>()
            var current: StateNode? = this
            while (current != null) {
                current.lastMove?.let { path.add(it) }
                current = current.parent
            }
            return path.reversed()
        }
    }

    /**
     * Finds the shortest path to solve the puzzle using BFS.
     * Returns the sequence of moves (source index to target index).
     */
    fun solve(initialTubes: List<Tube>, maxStates: Int = 200000): List<Pair<Int, Int>>? {
        val queue: Queue<StateNode> = LinkedList()
        val visited = mutableSetOf<StateNode>()

        val initialState = StateNode(initialTubes)
        queue.add(initialState)
        visited.add(initialState)

        var statesExplored = 0
        while (queue.isNotEmpty() && statesExplored < maxStates) {
            val current = queue.poll() ?: break
            statesExplored++

            if (current.tubes.all { it.isSolved() }) {
                return current.getPath()
            }

            // Try all possible moves
            for (i in current.tubes.indices) {
                val source = current.tubes[i]
                if (source.isEmpty) continue
                
                // Optimization: Don't move from a solved tube
                if (source.isSolved()) continue

                val topLayer = source.topLayer!!

                for (j in current.tubes.indices) {
                    if (i == j) continue

                    val target = current.tubes[j]

                    if (target.canAccept(topLayer)) {
                        val newTubes = current.tubes.toMutableList()
                        newTubes[i] = source.removeTopLayer()
                        newTubes[j] = target.addLayer(topLayer)

                        val nextNode = StateNode(newTubes, i to j, current)
                        if (nextNode !in visited) {
                            visited.add(nextNode)
                            queue.add(nextNode)
                        }
                    }
                }
            }
        }

        return null // Unsolvable or limit reached
    }

    fun isSolvable(tubes: List<Tube>): Boolean {
        return solve(tubes, maxStates = 5000) != null
    }

    fun getNextOptimalMove(tubes: List<Tube>): Pair<Int, Int>? {
        return solve(tubes, maxStates = 10000)?.firstOrNull()
    }
}
