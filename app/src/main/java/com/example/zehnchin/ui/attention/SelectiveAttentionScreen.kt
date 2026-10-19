package com.example.zehnchin.ui.attention

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.zehnchin.R
import com.example.zehnchin.domain.engine.SelectiveAttentionEngine
import com.example.zehnchin.domain.model.AttentionStimulus
import com.example.zehnchin.domain.model.PuzzleColor
import com.example.zehnchin.domain.model.Shape
import com.example.zehnchin.domain.model.LevelConfig
import com.example.zehnchin.ui.common.FormatUtils
import com.example.zehnchin.ui.common.GeometricShape
import kotlinx.coroutines.flow.collectLatest
import com.example.zehnchin.ui.theme.LocalFeedbackColors

@Composable
fun SelectiveAttentionScreen(
    viewModel: SelectiveAttentionViewModel,
    onNextLevel: (Int) -> Unit,
    onBackToLevels: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timer by viewModel.timerValue.collectAsState()
    val isNextUnlocked by viewModel.isNextLevelUnlocked.collectAsState()
    val isTutorial = uiState.level?.chapterId == 0

    val config = uiState.level?.config as? LevelConfig.SelectiveAttentionConfig

    LaunchedEffect(Unit) {
        viewModel.navigationEffect.collectLatest { effect ->
            when (effect) {
                AttentionNavEffect.ShowResults -> { }
                is AttentionNavEffect.AutoNextLevel -> onNextLevel(effect.levelNum)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isTutorial) "آموزش اصول" else stringResource(R.string.game_level_label, FormatUtils.formatNumber(uiState.level?.levelNumber ?: 0)),
                style = MaterialTheme.typography.titleLarge
            )
            if (!isTutorial) {
                Text(
                    text = stringResource(R.string.game_time_label, FormatUtils.formatNumber(timer / 1000)),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trial Count
        Text(
            text = stringResource(R.string.game_round_label, uiState.currentTrial + 1, config?.trialCount ?: 10),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Instruction
        if (config != null) {
            val colorName = when (config.targetColor) {
                PuzzleColor.RED -> stringResource(R.string.color_red)
                PuzzleColor.BLUE -> stringResource(R.string.color_blue)
                PuzzleColor.GREEN -> stringResource(R.string.color_green)
                PuzzleColor.YELLOW -> stringResource(R.string.color_yellow)
                PuzzleColor.PURPLE -> stringResource(R.string.color_purple)
                PuzzleColor.ORANGE -> stringResource(R.string.color_orange)
                PuzzleColor.CYAN -> stringResource(R.string.color_cyan)
                PuzzleColor.PINK -> stringResource(R.string.color_pink)
                PuzzleColor.BROWN -> stringResource(R.string.color_brown)
                PuzzleColor.GRAY -> stringResource(R.string.color_gray)
            }
            val shapeName = config.targetShape.toLocalizedName()
            val sizeName = when {
                config.targetScale == null -> ""
                config.targetScale < 0.8f -> "کوچک"
                config.targetScale > 1.2f -> "بزرگ"
                else -> "متوسط"
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.attention_target_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GeometricShape(
                            shape = config.targetShape, 
                            color = config.targetColor.toComposeColor(), 
                            modifier = Modifier.size(if (config.targetScale != null) (32.dp.value * config.targetScale).dp else 32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$colorName $shapeName $sizeName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback Area
        Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
            if (uiState.lastFeedback != null) {
                val color = if (uiState.lastFeedback == SelectiveAttentionEngine.FeedbackType.CORRECT) LocalFeedbackColors.current.correct else LocalFeedbackColors.current.incorrect
                val text = if (uiState.lastFeedback == SelectiveAttentionEngine.FeedbackType.CORRECT) 
                    stringResource(R.string.correct_feedback) else stringResource(R.string.incorrect_feedback)
                Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid
        if (config != null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(config.gridCols),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.stimuli) { stimulus ->
                    StimulusItem(stimulus = stimulus) {
                        viewModel.onStimulusClick(stimulus.id)
                    }
                }
            }
        }
    }

    // Fail Dialog
    if (uiState.status == com.example.zehnchin.domain.engine.GameStatus.LOST) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.attention_fail_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.attention_fail_msg))
                    if (!isNextUnlocked) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.attention_skip_unlock_msg, FormatUtils.formatNumber(50)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.retryLevel() }) {
                    Text(stringResource(R.string.btn_restart))
                }
            },
            dismissButton = {
                if (isNextUnlocked) {
                    TextButton(onClick = {
                        onNextLevel((uiState.level?.levelNumber ?: 0) + 1)
                    }) {
                        Text(stringResource(R.string.btn_next_level))
                    }
                } else {
                    Row {
                        TextButton(onClick = { viewModel.skipLevelWithCoins() }) {
                            Text(stringResource(R.string.btn_unlock_coins))
                        }
                        TextButton(onClick = { viewModel.skipLevelWithAd() }) {
                            Text(stringResource(R.string.btn_watch_ad_unlock))
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun Shape.toLocalizedName(): String {
    return when (this) {
        Shape.CIRCLE -> stringResource(R.string.shape_circle)
        Shape.SQUARE -> stringResource(R.string.shape_square)
        Shape.TRIANGLE -> stringResource(R.string.shape_triangle)
        Shape.STAR -> stringResource(R.string.shape_star)
        Shape.HEXAGON -> stringResource(R.string.shape_hexagon)
        Shape.DIAMOND -> stringResource(R.string.shape_diamond)
    }
}

@Composable
fun StimulusItem(stimulus: AttentionStimulus, onClick: () -> Unit) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale.value)
            .background(Color.LightGray.copy(alpha = 0.2f), shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        GeometricShape(
            shape = stimulus.shape,
            color = stimulus.color.toComposeColor(),
            modifier = Modifier.size(36.dp)
        )
    }
}

fun PuzzleColor.toComposeColor(): Color {
    return when (this) {
        PuzzleColor.RED -> Color.Red
        PuzzleColor.BLUE -> Color.Blue
        PuzzleColor.GREEN -> Color.Green
        PuzzleColor.YELLOW -> Color.Yellow
        PuzzleColor.PURPLE -> Color(0xFF9C27B0)
        PuzzleColor.ORANGE -> Color(0xFFFF9800)
        PuzzleColor.CYAN -> Color(0xFF00BCD4)
        PuzzleColor.PINK -> Color(0xFFE91E63)
        PuzzleColor.BROWN -> Color(0xFF795548)
        PuzzleColor.GRAY -> Color(0xFF9E9E9E)
    }
}
