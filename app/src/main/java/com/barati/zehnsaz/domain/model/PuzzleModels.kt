package com.barati.zehnsaz.domain.model

data class Layer(
    val color: PuzzleColor
)

data class Tube(
    val id: Int,
    val capacity: Int = 4,
    val layers: List<Layer> = emptyList()
) {
    val isFull: Boolean get() = layers.size >= capacity
    val isEmpty: Boolean get() = layers.isEmpty()
    
    val topLayer: Layer? get() = layers.lastOrNull()
    
    fun isSolved(): Boolean {
        if (isEmpty) return true
        if (layers.size < capacity) return false
        val firstColor = layers.first().color
        return layers.all { it.color == firstColor }
    }

    fun canAccept(layer: Layer): Boolean {
        if (isFull) return false
        if (isEmpty) return true
        return topLayer?.color == layer.color
    }
    
    fun addLayer(layer: Layer): Tube = copy(layers = layers + layer)
    fun removeTopLayer(): Tube = copy(layers = layers.dropLast(1))
}