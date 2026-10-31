package com.barati.zehnsaz.ui.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

private val GameBackdropTop = Color(0xFF1B2A44)
private val GameBackdropBottom = Color(0xFF0D1620)

/**
 * Dark, game-like backdrop for training screens: a deep vertical gradient with a soft glow
 * at the top in the chapter's accent colour. Drawn behind the game, so cards and stimuli
 * sit on top of it instead of on a white page.
 */
fun DrawScope.drawGameBackdrop(accent: Color) {
    drawRect(
        brush = Brush.verticalGradient(listOf(GameBackdropTop, GameBackdropBottom))
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.35f), Color.Transparent),
            center = Offset(size.width / 2f, 0f),
            radius = size.height * 0.8f
        )
    )
}
