package com.example.zehnchin.ui.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import com.example.zehnchin.ui.theme.BackdropBlush
import com.example.zehnchin.ui.theme.BackdropLavender
import com.example.zehnchin.ui.theme.BackdropMint
import com.example.zehnchin.ui.theme.BackdropSun
import com.example.zehnchin.ui.theme.Berry200
import com.example.zehnchin.ui.theme.Cream50
import com.example.zehnchin.ui.theme.Mint100

/**
 * Bright, cheerful backdrop for every menu screen: a blush → lavender → mint gradient with
 * soft sunny and pink glows. Screens draw on it with transparent scaffolds, so the colour
 * shows through around white cards. The colours are light enough that dark text keeps
 * more than 7:1 contrast anywhere on it.
 */
fun DrawScope.drawAppBackdrop() {
    drawRect(
        brush = Brush.verticalGradient(listOf(BackdropBlush, BackdropLavender, BackdropMint))
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(BackdropSun.copy(alpha = 0.55f), Color.Transparent),
            center = Offset(size.width, 0f),
            radius = size.width * 0.9f
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Berry200.copy(alpha = 0.30f), Color.Transparent),
            center = Offset(0f, size.height * 0.55f),
            radius = size.width * 0.8f
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Mint100.copy(alpha = 0.8f), Color.Transparent),
            center = Offset(size.width, size.height),
            radius = size.width
        )
    )
}

/**
 * Light, playful backdrop for training screens: a pastel wash of the chapter's accent colour
 * fading into warm cream, with a soft glow of the accent at the top. Stimuli and cards sit on
 * top of it. The wash is mixed mostly with white, so it never competes with the stimuli.
 */
fun DrawScope.drawGameBackdrop(accent: Color) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(lerp(accent, Color.White, 0.80f), lerp(accent, Color.White, 0.92f), Cream50)
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(size.width / 2f, 0f),
            radius = size.height * 0.6f
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(BackdropSun.copy(alpha = 0.35f), Color.Transparent),
            center = Offset(size.width, size.height),
            radius = size.width * 0.9f
        )
    )
}

/** Top bars are transparent so the colourful backdrop runs behind them. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun transparentTopBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = Color.Transparent,
    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    titleContentColor = MaterialTheme.colorScheme.onBackground
)
