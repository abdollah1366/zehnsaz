package com.barati.zehnsaz.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.barati.zehnsaz.R
import com.barati.zehnsaz.data.local.entities.PlayerProfileEntity
import com.barati.zehnsaz.ui.common.FormatUtils

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
                style = MaterialTheme.typography.headlineMedium
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
                color = Color(0xFFE65100),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeHeader(profile: PlayerProfileEntity, onNavigateToSettings: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                        modifier = Modifier.width(80.dp).height(8.dp)
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
    NavigationBar {
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