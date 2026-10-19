package com.example.zehnchin.ui.problemsolving

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zehnchin.R
import com.example.zehnchin.domain.model.Tube
import com.example.zehnchin.ui.attention.toComposeColor
import com.example.zehnchin.ui.common.FormatUtils

@Composable
fun ProblemSolvingScreen(
    viewModel: ProblemSolvingViewModel,
    onLevelSolved: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    var hintIndices by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isHintDeadlock by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.planning), style = MaterialTheme.typography.headlineSmall)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.game_moves_label, FormatUtils.formatNumber(state.movesMade)),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.game_remaining_label, FormatUtils.formatNumber(state.remainingMoves)),
                    color = if (state.remainingMoves < 5) Color.Red else Color.Unspecified
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Grid of Tubes
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(state.tubes) { tube ->
                TubeView(
                    tube = tube,
                    isSelected = state.selectedTubeId == tube.id,
                    onClick = { viewModel.onTubeClick(tube.id) }
                )
            }
        }

        if (hintIndices != null) {
            Text(
                text = stringResource(
                    R.string.hint_text,
                    FormatUtils.formatNumber(hintIndices!!.first + 1),
                    FormatUtils.formatNumber(hintIndices!!.second + 1)
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Medium
            )
        } else if (isHintDeadlock) {
            Text(
                text = stringResource(R.string.deadlock_text),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Medium
            )
        }

        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.undo() }) {
                Text(stringResource(R.string.btn_undo))
            }
            Button(onClick = { 
                val hint = viewModel.getHint()
                if (hint != null) {
                    hintIndices = hint
                    isHintDeadlock = false
                } else {
                    hintIndices = null
                    isHintDeadlock = true
                }
            }) {
                Text(stringResource(R.string.btn_hint))
            }
            Button(onClick = { 
                viewModel.reset()
                hintIndices = null
                isHintDeadlock = false
            }) {
                Text(stringResource(R.string.btn_restart))
            }
        }
    }

    if (state.isSolved) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.puzzle_solved_title)) },
            text = { Text(stringResource(R.string.puzzle_solved_desc)) },
            confirmButton = {
                Button(onClick = onLevelSolved) {
                    Text(stringResource(R.string.view_results))
                }
            }
        )
    }

    if (state.isDeadlock) {
        AlertDialog(
            onDismissRequest = { viewModel.undo() },
            title = { Text(stringResource(R.string.deadlock_title)) },
            text = { Text(stringResource(R.string.deadlock_desc)) },
            confirmButton = {
                Button(onClick = { viewModel.undo() }) {
                    Text(stringResource(R.string.undo_move))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.reset() }) {
                    Text(stringResource(R.string.btn_try_again))
                }
            }
        )
    }
}

@Composable
fun TubeView(
    tube: Tube,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(140.dp)
                .border(
                    width = if (isSelected) 3.dp else 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .background(
                    color = Color.LightGray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                tube.layers.reversed().forEach { layer ->
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 28.dp)
                            .background(layer.color.toComposeColor(), RoundedCornerShape(4.dp))
                    )
                }
            }
        }
        Text(text = FormatUtils.formatNumber(tube.id + 1), fontSize = 12.sp, color = Color.Gray)
    }
}
