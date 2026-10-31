package com.example.zehnchin.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.zehnchin.R
import com.example.zehnchin.data.local.entities.PlayerProfileEntity
import com.example.zehnchin.ui.common.FormatUtils
import com.example.zehnchin.ui.theme.Sun200

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTraining: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAssessment: () -> Unit
) {
    val profile by viewModel.playerProfile.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            profile?.let { HomeHeader(it, onNavigateToSettings) }
        },
        bottomBar = {
            HomeBottomBar(
                onNavigateToHome = {},
                onNavigateToTraining = onNavigateToTraining,
                onNavigateToAchievements = onNavigateToAchievements,
                onNavigateToShop = onNavigateToShop,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.welcome_message),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onNavigateToTraining,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.continue_training), fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToAssessment,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.home_assessment), fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val streak = profile?.currentStreak ?: 0
            Text(
                text = stringResource(R.string.streak_days, FormatUtils.formatNumber(streak)),
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeHeader(profile: PlayerProfileEntity, onNavigateToSettings: () -> Unit) {
    // A violet-to-berry header band; white text keeps more than 5.8:1 contrast on both ends.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = Color.White,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToSettings) {
                    Text("⚙", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.level_label, FormatUtils.formatNumber(profile.level)),
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { 0.5f }, // Mock progress
                        modifier = Modifier.width(80.dp).height(8.dp),
                        color = Sun200,
                        trackColor = Color.White.copy(alpha = 0.35f)
                    )
                }
            }
            Row {
                Text(text = stringResource(R.string.coins_label, FormatUtils.formatNumber(profile.totalCoins)))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = stringResource(R.string.gems_label, FormatUtils.formatNumber(profile.totalGems)))
            }
        }
    }
}

@Composable
fun HomeBottomBar(
    onNavigateToHome: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(
            selected = true,
            onClick = onNavigateToHome,
            icon = { Text("🏠") },
            label = { Text(stringResource(R.string.nav_home)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToTraining,
            icon = { Text("🧠") },
            label = { Text(stringResource(R.string.nav_training)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToAchievements,
            icon = { Text("🏆") },
            label = { Text(stringResource(R.string.nav_achievements)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToShop,
            icon = { Text("🛒") },
            label = { Text(stringResource(R.string.nav_shop)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToProfile,
            icon = { Text("⚙") },
            label = { Text(stringResource(R.string.nav_profile)) }
        )
    }
}