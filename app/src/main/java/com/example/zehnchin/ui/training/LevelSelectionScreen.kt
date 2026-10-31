package com.example.zehnchin.ui.training

import com.example.zehnchin.ui.common.transparentTopBarColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zehnchin.R
import com.example.zehnchin.domain.model.Level
import com.example.zehnchin.ui.common.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    chapterId: Int,
    viewModel: LevelSelectionViewModel,
    onLevelSelected: (Level) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(chapterId) {
        viewModel.setChapter(chapterId)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = transparentTopBarColors(),
                title = { Text(stringResource(R.string.training_chapters)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("⬅", fontSize = 24.sp)
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.levels) { itemState ->
                LevelItem(itemState) {
                    if (itemState.isUnlocked) {
                        onLevelSelected(itemState.level)
                    }
                }
            }
        }
    }
}

@Composable
fun LevelItem(
    state: LevelItemState,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        state.isCompleted -> MaterialTheme.colorScheme.primaryContainer
        state.isUnlocked -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = state.isUnlocked) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.isUnlocked) {
                Text(
                    text = FormatUtils.formatNumber(state.level.levelNumber),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (state.isCompleted) {
                    Text(
                        text = "⭐".repeat(state.stars),
                        fontSize = 10.sp
                    )
                }
            } else {
                Text("🔒", fontSize = 20.sp)
            }
        }
    }
}
