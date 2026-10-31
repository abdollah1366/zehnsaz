package com.barati.zehnsaz.ui.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.barati.zehnsaz.R
import com.barati.zehnsaz.data.local.entities.AchievementEntity
import com.barati.zehnsaz.ui.common.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    achievements: List<AchievementEntity>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.my_achievements)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: AchievementEntity) {
    val title = when (achievement.id) {
        "FIRST_STEP" -> stringResource(R.string.achievement_first_step)
        "COMPLETE_10_LEVELS" -> stringResource(R.string.achievement_10_levels)
        "THREE_STAR_LEVEL" -> stringResource(R.string.achievement_3_stars)
        "NO_HINT_LEVEL" -> stringResource(R.string.achievement_independent_mind)
        else -> achievement.title
    }

    val description = when (achievement.id) {
        "FIRST_STEP" -> stringResource(R.string.achievement_first_step_desc)
        "COMPLETE_10_LEVELS" -> stringResource(R.string.achievement_10_levels_desc)
        "THREE_STAR_LEVEL" -> stringResource(R.string.achievement_3_stars_desc)
        "NO_HINT_LEVEL" -> stringResource(R.string.achievement_independent_mind_desc)
        else -> achievement.description
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) MaterialTheme.colorScheme.primaryContainer else Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (achievement.isUnlocked) "🔓" else "🔒", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall)
                if (!achievement.isUnlocked) {
                    LinearProgressIndicator(
                        progress = { achievement.currentProgress.toFloat() / achievement.targetProgress },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
