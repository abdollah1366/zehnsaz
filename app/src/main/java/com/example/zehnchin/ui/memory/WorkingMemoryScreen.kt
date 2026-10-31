package com.example.zehnchin.ui.memory

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextAlign
import com.example.zehnchin.R
import com.example.zehnchin.domain.engine.GameStatus
import com.example.zehnchin.domain.engine.WorkingMemoryEngine
import com.example.zehnchin.domain.model.AttentionStimulus
import com.example.zehnchin.domain.model.PuzzleColor
import com.example.zehnchin.domain.model.Shape
import com.example.zehnchin.ui.attention.toComposeColor
import com.example.zehnchin.ui.common.FormatUtils
import com.example.zehnchin.ui.common.GeometricShape
import kotlinx.coroutines.flow.collectLatest
import com.example.zehnchin.ui.theme.LocalFeedbackColors

@Composable
fun WorkingMemoryScreen(
    viewModel: WorkingMemoryViewModel,
    onNextLevel: (Int) -> Unit,
    onBackToLevels: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timer by viewModel.timerValue.collectAsState()
    val isNextUnlocked by viewModel.isNextLevelUnlocked.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEffect.collectLatest { effect ->
            when (effect) {
                is MemoryNavEffect.AutoNextLevel -> onNextLevel(effect.levelNum)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.game_level_label, FormatUtils.formatNumber(uiState.level?.levelNumber ?: 0)),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.game_time_label, FormatUtils.formatNumber(timer / 1000)),
                color = if (timer < 3000) LocalFeedbackColors.current.warning else Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = uiState.phase,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "PhaseTransition"
        ) { phase ->
            when (phase) {
                WorkingMemoryEngine.MemoryPhase.ENCODING -> {
                    EncodingView(uiState.targetSequence, timer, uiState.currentRule)
                }
                WorkingMemoryEngine.MemoryPhase.RECALL -> {
                    RecallView(
                        userSequence = uiState.userSequence,
                        options = uiState.availableOptions,
                        onOptionSelected = { viewModel.onOptionSelected(it) },
                        onRemoveLast = { viewModel.removeLastSelected() },
                        onSubmit = { viewModel.submitAnswer() },
                        rule = uiState.currentRule
                    )
                }
                WorkingMemoryEngine.MemoryPhase.EVALUATION -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (uiState.status == GameStatus.WON) {
                            Text(
                                text = stringResource(R.string.memory_correct_feedback),
                                style = MaterialTheme.typography.headlineMedium,
                                color = LocalFeedbackColors.current.correct,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    if (uiState.status == GameStatus.LOST) {
        val errorText = when (uiState.lastError) {
            WorkingMemoryEngine.ErrorType.SHAPE -> stringResource(R.string.memory_error_shape)
            WorkingMemoryEngine.ErrorType.COLOR -> stringResource(R.string.memory_error_color)
            WorkingMemoryEngine.ErrorType.SIZE -> stringResource(R.string.memory_error_size)
            WorkingMemoryEngine.ErrorType.LENGTH -> stringResource(R.string.memory_error_length)
            else -> stringResource(R.string.memory_wrong_msg)
        }

        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.memory_wrong_title)) },
            text = {
                Column {
                    Text(errorText)
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
                    Text(stringResource(R.string.memory_retry_btn))
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

    // Initial Start
    LaunchedEffect(uiState.level) {
        if (uiState.status == GameStatus.READY) {
            viewModel.startSession()
        }
    }
}

@Composable
fun EncodingView(
    sequence: List<AttentionStimulus>, 
    remainingTime: Long,
    rule: WorkingMemoryEngine.MemoryRule
) {
    val instructionRes = when (rule) {
        WorkingMemoryEngine.MemoryRule.SHAPE_ONLY -> R.string.memory_rule_shape
        WorkingMemoryEngine.MemoryRule.SHAPE_COLOR -> R.string.memory_rule_shape_color
        WorkingMemoryEngine.MemoryRule.SHAPE_COLOR_SIZE,
        WorkingMemoryEngine.MemoryRule.ALL -> R.string.memory_rule_all
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(instructionRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Card(
            modifier = Modifier.padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.memory_rule_title),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                MemoryRuleIndicator(rule)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.memory_memorize_time_label, FormatUtils.formatNumber(remainingTime / 1000)),
            style = MaterialTheme.typography.headlineLarge,
            color = if (remainingTime < 3000) LocalFeedbackColors.current.warning else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        SequenceLayout(sequence = sequence)

        Text(
            text = stringResource(R.string.memory_direction_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
fun SequenceLayout(sequence: List<AttentionStimulus>) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val size = sequence.size
        when {
            size <= 4 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sequence.forEachIndexed { index, stimulus ->
                        StimulusDisplay(stimulus, size = 60.dp)
                        if (index < sequence.size - 1) {
                            DirectionArrow()
                        }
                    }
                }
            }
            size <= 6 -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val firstRow = sequence.take(3)
                    val secondRow = sequence.drop(3)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        firstRow.forEachIndexed { index, stimulus ->
                            StimulusDisplay(stimulus, size = 50.dp)
                            if (index < firstRow.size - 1) DirectionArrow()
                        }
                        DirectionArrow()
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        secondRow.forEachIndexed { index, stimulus ->
                            StimulusDisplay(stimulus, size = 50.dp)
                            if (index < secondRow.size - 1) DirectionArrow()
                        }
                    }
                }
            }
            else -> {
                // 7-8 items: 4x2 grid style
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val firstRow = sequence.take(4)
                    val secondRow = sequence.drop(4)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        firstRow.forEachIndexed { index, stimulus ->
                            StimulusDisplay(stimulus, size = 40.dp)
                            if (index < firstRow.size - 1) DirectionArrow()
                        }
                        DirectionArrow()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        secondRow.forEachIndexed { index, stimulus ->
                            StimulusDisplay(stimulus, size = 40.dp)
                            if (index < secondRow.size - 1) DirectionArrow()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectionArrow() {
    Text(
        text = "←",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp),
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
fun MemoryRuleIndicator(rule: WorkingMemoryEngine.MemoryRule) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
            Text("⬡ شکل")
        }
        if (rule != WorkingMemoryEngine.MemoryRule.SHAPE_ONLY) {
            Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                Text("🎨 رنگ")
            }
        }
        if (rule == WorkingMemoryEngine.MemoryRule.SHAPE_COLOR_SIZE || rule == WorkingMemoryEngine.MemoryRule.ALL) {
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text("📏 اندازه")
            }
        }
    }
}

@Composable
fun RecallView(
    userSequence: List<AttentionStimulus>,
    options: List<AttentionStimulus>,
    onOptionSelected: (AttentionStimulus) -> Unit,
    onRemoveLast: () -> Unit,
    onSubmit: () -> Unit,
    rule: WorkingMemoryEngine.MemoryRule
) {
    val rulePrompt = when (rule) {
        WorkingMemoryEngine.MemoryRule.SHAPE_ONLY -> "فقط شکل"
        WorkingMemoryEngine.MemoryRule.SHAPE_COLOR -> "شکل و رنگ"
        else -> "شکل، رنگ و اندازه"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "همان $rulePrompt را پیدا کنید",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.memory_recall_instruction),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Current Answer Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            SequenceLayout(sequence = userSequence)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(onClick = onRemoveLast) {
                Text(text = stringResource(R.string.clear_last))
            }
            Button(onClick = onSubmit) {
                Text(text = stringResource(R.string.submit_answer))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Options Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(options) { stimulus ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onOptionSelected(stimulus) },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        StimulusDisplay(stimulus, size = 48.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun StimulusDisplay(stimulus: AttentionStimulus, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .scale(stimulus.scale)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        GeometricShape(
            shape = stimulus.shape,
            color = stimulus.color.toComposeColor(),
            modifier = Modifier.size(size * 0.7f)
        )
    }
}
