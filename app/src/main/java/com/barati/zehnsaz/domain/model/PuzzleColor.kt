package com.barati.zehnsaz.domain.model

enum class PuzzleColor {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    PURPLE,
    ORANGE,
    CYAN,
    PINK,
    BROWN,
    GRAY;

    companion object {
        fun fromInt(index: Int): PuzzleColor = entries[index % entries.size]
    }
}