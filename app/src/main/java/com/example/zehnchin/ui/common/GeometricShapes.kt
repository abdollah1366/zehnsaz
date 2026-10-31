package com.example.zehnchin.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zehnchin.domain.model.Shape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Thin dark rim drawn around every stimulus, so light colours (yellow, white) stay visible on the bright backgrounds. */
val StimulusOutline = Color(0x8C1E1B2E)

@Composable
fun GeometricShape(
    shape: Shape,
    color: Color,
    modifier: Modifier = Modifier.size(40.dp),
    outline: Color = StimulusOutline
) {
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val center = center
        val stroke = 1.5.dp.toPx()
        // Inset by half the rim so the outline stays inside the canvas.
        val radius = size / 2 - stroke / 2

        val path = when (shape) {
            Shape.CIRCLE -> Path().apply {
                addOval(Rect(center = center, radius = radius))
            }
            Shape.SQUARE -> Path().apply {
                addRect(Rect(center = center, radius = radius))
            }
            Shape.TRIANGLE -> Path().apply {
                moveTo(center.x, center.y - radius)
                lineTo(center.x + radius, center.y + radius)
                lineTo(center.x - radius, center.y + radius)
                close()
            }
            Shape.DIAMOND -> Path().apply {
                moveTo(center.x, center.y - radius)
                lineTo(center.x + radius, center.y)
                lineTo(center.x, center.y + radius)
                lineTo(center.x - radius, center.y)
                close()
            }
            Shape.HEXAGON -> Path().apply {
                for (i in 0 until 6) {
                    val angle = i * PI / 3 - PI / 2
                    val x = center.x + radius * cos(angle).toFloat()
                    val y = center.y + radius * sin(angle).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            Shape.STAR -> Path().apply {
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
        }
        drawPath(path, color, style = Fill)
        drawPath(path, outline, style = Stroke(width = stroke))
    }
}
