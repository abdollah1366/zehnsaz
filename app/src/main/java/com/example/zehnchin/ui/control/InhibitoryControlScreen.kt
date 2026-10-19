package com.example.zehnchin.ui.control

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.example.zehnchin.R
import com.example.zehnchin.domain.engine.GameStatus
import com.example.zehnchin.domain.engine.InhibitoryControlEngine
import com.example.zehnchin.domain.model.InhibitoryStimulusType
import com.example.zehnchin.domain.model.LevelConfig
import com.example.zehnchin.ui.attention.toComposeColor
import com.example.zehnchin.ui.common.FormatUtils
import com.example.zehnchin.ui.theme.LocalFeedbackColors

@Composable
fun InhibitoryControlScreen(
    viewModel: InhibitoryControlViewModel,
    onSessionComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.level?.config as? LevelConfig.InhibitoryControlConfig

    LaunchedEffect(Unit) {
        viewModel.navigationEffect.collect { effect ->
            when (effect) {
                ControlNavEffect.ShowResults -> onSessionComplete()
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
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.game_round_label, uiState.currentRound, config?.roundCount ?: 0),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Combo & Feedback
        Box(modifier = Modifier.height(60.dp), contentAlignment = Alignment.Center) {
            if (uiState.lastFeedback != null) {
                val color = if (uiState.lastFeedback == InhibitoryControlEngine.FeedbackType.CORRECT) LocalFeedbackColors.current.correct else LocalFeedbackColors.current.incorrect
                val text = if (uiState.lastFeedback == InhibitoryControlEngine.FeedbackType.CORRECT) 
                    stringResource(R.string.correct_feedback) else stringResource(R.string.incorrect_feedback)
                Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            } else {
                Text(
                    text = stringResource(R.string.game_combo_label, FormatUtils.formatNumber(uiState.currentCombo)),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFE65100)
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        // Stimulus Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(enabled = uiState.waitingForResponse) { viewModel.onUserClick() },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = uiState.isStimulusVisible,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                uiState.currentStimulus?.let { stimulus ->
                    InhibitoryStimulusDisplay(stimulusType = uiState.stimulusType, color = stimulus.color.toComposeColor())
                }
            }
            
            if (!uiState.isStimulusVisible && uiState.status == GameStatus.PLAYING) {
                Text(text = stringResource(R.string.get_ready), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        }

        // Instructions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.status == GameStatus.READY) {
                Button(
                    onClick = { viewModel.startTraining() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.btn_start))
                }
            } else {
                InstructionView()
            }
        }
    }
}

@Composable
fun InhibitoryStimulusDisplay(stimulusType: InhibitoryStimulusType, color: Color) {
    Surface(
        modifier = Modifier.size(120.dp),
        shape = if (stimulusType == InhibitoryStimulusType.GO) CircleShape else RoundedCornerShape(16.dp),
        color = color,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = if (stimulusType == InhibitoryStimulusType.GO) "✓" else "✕",
                fontSize = 48.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InstructionView() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Instructions name the shape, so the rule does not depend on colour.
        Text(text = stringResource(R.string.go_instruction), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = stringResource(R.string.stop_instruction), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}
