package com.barati.zehnsaz.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.barati.zehnsaz.domain.model.Shape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GeometricShape(
    shape: Shape,
    color: Color,
    modifier: Modifier = Modifier.size(40.dp)
) {
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val center = center
        val radius = size / 2

        when (shape) {
            Shape.CIRCLE -> {
                drawCircle(color = color, radius = radius, center = center)
            }
            Shape.SQUARE -> {
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(size, size)
                )
            }
            Shape.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x + radius, center.y + radius)
                    lineTo(center.x - radius, center.y + radius)
                    close()
                }
                drawPath(path, color)
            }
            Shape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x + radius, center.y)
                    lineTo(center.x, center.y + radius)
                    lineTo(center.x - radius, center.y)
                    close()
                }
                drawPath(path, color)
            }
            Shape.HEXAGON -> {
                val path = Path().apply {
                    for (i in 0 until 6) {
                        val angle = i * PI / 3 - PI / 2
                        val x = center.x + radius * cos(angle).toFloat()
                        val y = center.y + radius * sin(angle).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, color)
            }
            Shape.STAR -> {
                val path = Path().apply {
                    val innerRadius = radius / 2.5f
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) radius else innerRadius
                        val angle = i * PI / 5 - PI / 2
                        val x = center.x + r * cos(angle).toFloat()
                        val y = center.y + r * sin(angle).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, color)
            }
        }
    }
}
