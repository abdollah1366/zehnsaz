package com.example.zehnchin.ui.speed

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import com.example.zehnchin.R
import com.example.zehnchin.domain.engine.GameStatus
import com.example.zehnchin.domain.engine.ProcessingSpeedEngine
import com.example.zehnchin.domain.model.LevelConfig
import com.example.zehnchin.ui.attention.toComposeColor
import com.example.zehnchin.ui.common.FormatUtils
import com.example.zehnchin.ui.common.GeometricShape
import com.example.zehnchin.domain.model.CognitiveScoreResult

@Composable
fun ProcessingSpeedScreen(
    viewModel: ProcessingSpeedViewModel,
    onNextLevel: (Int) -> Unit,
    onBackToLevels: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val viewPhase by viewModel.viewPhase.collectAsState()
    val countdown by viewModel.countdownValue.collectAsState()
    val remainingTime by viewModel.remainingTimeMillis.collectAsState()

    AnimatedContent(
        targetState = viewPhase,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "SpeedPhaseTransition"
    ) { phase ->
        when (phase) {
            SpeedViewPhase.INSTRUCTION -> {
                SpeedInstructionView(
                    target = uiState.targetCriteria,
                    onStart = { viewModel.startTraining() }
                )
            }
            SpeedViewPhase.COUNTDOWN -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (countdown > 0) FormatUtils.formatNumber(countdown) else stringResource(R.string.countdown_go),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            SpeedViewPhase.PLAYING -> {
                SpeedGameplayView(
                    uiState = uiState,
                    remainingTime = remainingTime,
                    onTargetClick = { viewModel.onAction() },
                    onScreenClick = { viewModel.onScreenClick() }
                )
            }
            SpeedViewPhase.RESULT_SUMMARY -> {
                SpeedResultSummaryView(
                    result = viewModel.calculateResults(),
                    levelNum = uiState.level?.levelNumber ?: 1,
                    onNextLevel = onNextLevel,
                    onBackToLevels = onBackToLevels
                )
            }
        }
    }
}

@Composable
fun SpeedResultSummaryView(
    result: CognitiveScoreResult,
    levelNum: Int,
    onNextLevel: (Int) -> Unit,
    onBackToLevels: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.speed_training_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ResultRow(label = stringResource(R.string.accuracy_label), value = FormatUtils.formatPercent(result.accuracy.toFloat()))
                ResultRow(
                    label = stringResource(R.string.avg_reaction_time_label, ""), 
                    value = "${FormatUtils.formatNumber(result.speed)} ${stringResource(R.string.ms_unit)}"
                )
                ResultRow(label = stringResource(R.string.final_score_label), value = FormatUtils.formatNumber(result.finalScore))
                ResultRow(label = stringResource(R.string.stars_label), value = "⭐".repeat(result.stars))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onNextLevel(levelNum + 1) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(R.string.btn_next_level))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackToLevels,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(R.string.btn_level_list))
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SpeedInstructionView(
    target: Pair<com.example.zehnchin.domain.model.PuzzleColor, com.example.zehnchin.domain.model.Shape>?,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.speed_training_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.speed_instruction_goal),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        target?.let { (color, shape) ->
            Text(text = stringResource(R.string.speed_target_label), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            GeometricShape(shape = shape, color = color.toComposeColor(), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.speed_instruction_action),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.speed_scoring_instruction),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(R.string.btn_start_speed), fontSize = 18.sp)
        }
    }
}

@Composable
fun SpeedGameplayView(
    uiState: ProcessingSpeedEngine.SpeedState,
    remainingTime: Long,
    onTargetClick: () -> Unit,
    onScreenClick: () -> Unit
) {
    val config = uiState.level?.config as? LevelConfig.ProcessingSpeedConfig

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = true, onClick = onScreenClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.game_round_label, uiState.currentRound, config?.roundCount ?: 0),
                style = MaterialTheme.typography.titleMedium
            )
            val timeColor = if (remainingTime < 5000) Color.Red else MaterialTheme.colorScheme.onSurface
            Text(
                text = stringResource(R.string.game_time_label, FormatUtils.formatNumber(remainingTime / 1000)),
                style = MaterialTheme.typography.titleMedium,
                color = timeColor,
                fontWeight = if (remainingTime < 5000) FontWeight.Bold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Feedback Text
        Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
            uiState.lastFeedback?.let { feedback ->
                val (textRes, color) = when (feedback) {
                    ProcessingSpeedEngine.FeedbackType.CORRECT -> R.string.correct_feedback to Color(0xFF4CAF50)
                    ProcessingSpeedEngine.FeedbackType.INCORRECT -> R.string.incorrect_feedback to Color.Red
                    ProcessingSpeedEngine.FeedbackType.FALSE_START -> R.string.false_start_msg to Color(0xFFFF9800)
                    ProcessingSpeedEngine.FeedbackType.TOO_SLOW -> R.string.game_remaining_label to Color.Gray // Placeholder
                }
                if (feedback != ProcessingSpeedEngine.FeedbackType.TOO_SLOW) {
                    Text(
                        text = stringResource(textRes),
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }

        // Gameplay Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.isStimulusVisible,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                uiState.currentStimulus?.let { stimulus ->
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.Gray.copy(alpha = 0.05f), CircleShape)
                            .clickable { onTargetClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        GeometricShape(
                            shape = stimulus.shape,
                            color = stimulus.color.toComposeColor(),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
        }
        
        // Bottom Hint (Static target for reference)
        uiState.targetCriteria?.let { (color, shape) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.speed_target_label), style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(8.dp))
                GeometricShape(shape = shape, color = color.toComposeColor(), modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
