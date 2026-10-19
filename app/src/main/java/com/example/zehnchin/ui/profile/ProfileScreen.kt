package com.example.zehnchin.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.zehnchin.R
import com.example.zehnchin.data.local.entities.PlayerProfileEntity
import com.example.zehnchin.domain.progression.DomainSummary
import com.example.zehnchin.ui.common.FormatUtils
import com.example.zehnchin.ui.theme.domainColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: PlayerProfileEntity?,
    domains: List<DomainSummary>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.profile_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
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

            if (domains.isEmpty()) {
                Text(text = stringResource(R.string.profile_no_data), style = MaterialTheme.typography.bodyMedium)
            }
            domains.forEach { domain ->
                DomainScoreCard(domain)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun DomainScoreCard(domain: DomainSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(domainColor(domain.chapterId))
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(chapterTitleRes(domain.chapterId)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.profile_best_score, FormatUtils.formatNumber(domain.bestScore)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.profile_avg_accuracy, FormatUtils.formatNumber(domain.averageAccuracy)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.profile_attempts, FormatUtils.formatNumber(domain.attemptCount)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun chapterTitleRes(chapterId: Int): Int = when (chapterId) {
    0 -> R.string.chapter_0_title
    1 -> R.string.chapter_1_title
    2 -> R.string.chapter_2_title
    3 -> R.string.chapter_3_title
    4 -> R.string.chapter_4_title
    5 -> R.string.chapter_5_title
    else -> R.string.chapter_6_title
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
