package com.example.zehnchin.ui.flexibility

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.zehnchin.R
import com.example.zehnchin.domain.engine.GameStatus
import com.example.zehnchin.domain.engine.CognitiveFlexibilityEngine
import com.example.zehnchin.domain.model.LevelConfig
import com.example.zehnchin.domain.model.PuzzleColor
import com.example.zehnchin.domain.model.RuleType
import com.example.zehnchin.domain.model.Shape
import com.example.zehnchin.ui.attention.StimulusItem
import com.example.zehnchin.ui.attention.toComposeColor
import com.example.zehnchin.ui.common.FormatUtils
import com.example.zehnchin.ui.common.GeometricShape
import com.example.zehnchin.ui.theme.LocalFeedbackColors

@Composable
fun FlexibilityScreen(
    viewModel: FlexibilityViewModel,
    onSessionComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.level?.config as? LevelConfig.CognitiveFlexibilityConfig

    LaunchedEffect(Unit) {
        viewModel.navigationEffect.collect { effect ->
            when (effect) {
                FlexibilityNavEffect.ShowResults -> onSessionComplete()
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
                text = stringResource(R.string.game_level_label, FormatUtils.formatNumber(uiState.level?.levelNumber ?: 0)),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.game_round_label, uiState.currentRound, config?.roundCount ?: 0),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rule Display
        uiState.currentRule?.let { rule ->
            RuleDisplay(
                ruleName = stringResource(rule.displayTextRes),
                isSwitching = uiState.isSwitchingRule
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback Text
        Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
            uiState.lastFeedback?.let { feedback ->
                val (textRes, color) = if (feedback == CognitiveFlexibilityEngine.FeedbackType.CORRECT) 
                    R.string.correct_feedback to LocalFeedbackColors.current.correct else R.string.incorrect_feedback to LocalFeedbackColors.current.incorrect
                Text(text = stringResource(textRes), color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stimulus Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.isStimulusVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                uiState.currentStimulus?.let { stimulus ->
                    StimulusItem(stimulus = stimulus) { }
                }
            }
        }

        // Response Options
        if (uiState.isStimulusVisible) {
            ResponseOptions(
                ruleType = uiState.currentRule?.type ?: RuleType.COLOR_MATCH,
                options = uiState.availableOptions,
                onResponse = { viewModel.onResponse(it) }
            )
        } else if (uiState.status == GameStatus.READY) {
            Button(
                onClick = { viewModel.startTraining() },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Text(text = stringResource(R.string.btn_start))
            }
        } else {
            // Space holder during feedback/delay
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun RuleDisplay(ruleName: String, isSwitching: Boolean) {
    val backgroundColor = if (isSwitching) LocalFeedbackColors.current.warning.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(if (isSwitching) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSwitching) stringResource(R.string.rule_changed) else "قانون فعلی:",
                color = if (isSwitching) LocalFeedbackColors.current.warning else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = if (isSwitching) 20.sp else 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = ruleName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ResponseOptions(
    ruleType: RuleType, 
    options: List<Any>,
    onResponse: (Any) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { option ->
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .then(
                        if (ruleType == RuleType.COLOR_MATCH && option is PuzzleColor) {
                            Modifier.background(option.toComposeColor(), CircleShape)
                        } else {
                            Modifier.background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        }
                    )
                    .clip(if (ruleType == RuleType.COLOR_MATCH) CircleShape else RoundedCornerShape(8.dp))
                    .clickable { onResponse(option) },
                contentAlignment = Alignment.Center
            ) {
                if (ruleType == RuleType.SHAPE_MATCH && option is Shape) {
                    GeometricShape(
                        shape = option,
                        color = Color.DarkGray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}
