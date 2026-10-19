package com.example.zehnchin.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.zehnchin.R
import com.example.zehnchin.domain.economy.EconomyConfig
import com.example.zehnchin.ui.common.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRewardScreen(
    currentDay: Int,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.daily_reward_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🎁", style = MaterialTheme.typography.displayLarge)
            Text(text = stringResource(R.string.daily_reward_msg), style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(EconomyConfig.DAILY_REWARDS) { index, reward ->
                    RewardDayCard(
                        day = index + 1,
                        isCurrent = (index + 1) == currentDay,
                        isClaimed = (index + 1) < currentDay || ((index + 1) == currentDay && isClaimed)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onClaim,
                enabled = !isClaimed,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = if (isClaimed) stringResource(R.string.reward_claimed) else stringResource(R.string.claim_reward),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RewardDayCard(day: Int, isCurrent: Boolean, isClaimed: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.LightGray.copy(alpha = 0.2f)
        ),
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.day_n, FormatUtils.formatNumber(day)),
                style = MaterialTheme.typography.labelSmall
            )
            Text(text = if (isClaimed) "✅" else "💰", style = MaterialTheme.typography.titleLarge)
        }
    }
}
