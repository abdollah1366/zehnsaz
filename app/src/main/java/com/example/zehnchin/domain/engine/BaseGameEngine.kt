package com.example.zehnchin.domain.engine

import com.example.zehnchin.domain.model.Level
import kotlinx.coroutines.flow.StateFlow

interface BaseGameEngine<T> {
    val gameState: StateFlow<T>
    
    fun loadLevel(level: Level)
    fun reset()
    fun pause()
    fun resume()
}

enum class GameStatus {
    READY,
    PLAYING,
    PAUSED,
    WON,
    LOST
}