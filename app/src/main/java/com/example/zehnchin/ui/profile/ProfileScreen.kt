package com.example.zehnchin.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.zehnchin.R
import com.example.zehnchin.data.local.entities.PlayerProfileEntity
import com.example.zehnchin.ui.common.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: PlayerProfileEntity?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.profile_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "👤", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.level_label, FormatUtils.formatNumber(profile?.level ?: 1)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileStatRow(
                label = stringResource(R.string.completed_levels),
                value = FormatUtils.formatNumber(profile?.completedLevels ?: 0)
            )
            ProfileStatRow(
                label = stringResource(R.string.unlocked_chapters),
                value = FormatUtils.formatNumber(profile?.completedChapters ?: 0)
            )
            ProfileStatRow(
                label = stringResource(R.string.total_stars),
                value = FormatUtils.formatNumber(profile?.totalStars ?: 0)
            )
            ProfileStatRow(
                label = stringResource(R.string.longest_streak),
                value = FormatUtils.formatNumber(profile?.longestStreak ?: 0)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = stringResource(R.string.training_scores),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simplified Score list
            Text(text = stringResource(R.string.score_attention, FormatUtils.formatNumber(780)), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.score_memory, FormatUtils.formatNumber(650)), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.score_speed, FormatUtils.formatNumber(920)), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}