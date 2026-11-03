package com.barati.zehnsaz.domain.engine

import com.barati.zehnsaz.domain.model.Level
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