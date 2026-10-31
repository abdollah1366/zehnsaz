package com.example.zehnchin.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import com.example.zehnchin.R
import com.example.zehnchin.domain.model.CognitiveScoreResult
import kotlinx.coroutines.delay

private const val AUTO_RETURN_DELAY_MILLIS = 6_000L

@Composable
fun CognitiveResultScreen(
    @StringRes titleRes: Int,
    result: CognitiveScoreResult,
    onNextLevel: () -> Unit,
    onBackToLevels: () -> Unit,
    onExit: () -> Unit
) {
    // After a stage ends, the player goes back to the home screen by itself.
    // Leaving this screen earlier (next level, level list) cancels the countdown.
    LaunchedEffect(Unit) {
        delay(AUTO_RETURN_DELAY_MILLIS)
        onExit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(titleRes), style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ResultRow(label = stringResource(R.string.accuracy_label), value = FormatUtils.formatPercent(result.accuracy.toFloat()))
                ResultRow(label = stringResource(R.string.reaction_speed_label), value = FormatUtils.formatNumber(result.speed))
                ResultRow(label = stringResource(R.string.final_score_label), value = FormatUtils.formatNumber(result.finalScore))
                ResultRow(label = stringResource(R.string.stars_label), value = "⭐".repeat(result.stars))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reward Section
        Text(
            text = stringResource(R.string.reward_section_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RewardChip(text = stringResource(R.string.xp_gain, FormatUtils.formatNumber(50)), color = MaterialTheme.colorScheme.primary)
            RewardChip(text = stringResource(R.string.coin_gain, FormatUtils.formatNumber(20)), color = MaterialTheme.colorScheme.tertiary)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNextLevel,
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

        TextButton(
            onClick = onExit,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.nav_home))
        }

        Text(
            text = stringResource(R.string.returning_home_hint),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RewardChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResultRow(label: String, value: String) {
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
