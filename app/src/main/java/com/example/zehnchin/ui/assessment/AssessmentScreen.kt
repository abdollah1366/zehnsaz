package com.example.zehnchin.ui.assessment

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zehnchin.R
import com.example.zehnchin.domain.assessment.AssessmentSchedule
import com.example.zehnchin.domain.assessment.AssessmentTask

@Composable
fun AssessmentScreen(viewModel: AssessmentViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val lastTaken by viewModel.lastTaken.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (val current = state) {
            AssessmentUiState.Menu -> AssessmentMenu(
                lastTaken = lastTaken,
                onStart = viewModel::start,
                onBack = onBack
            )
            is AssessmentUiState.Running -> AssessmentRunning(
                task = current.task,
                stimulus = current.stimulus,
                onRespond = viewModel::respond
            )
            is AssessmentUiState.Finished -> AssessmentFinished(
                measures = current.measures,
                onDone = viewModel::backToMenu
            )
        }
    }
}

@Composable
private fun AssessmentMenu(
    lastTaken: Map<AssessmentTask, Long?>,
    onStart: (AssessmentTask) -> Unit,
    onBack: () -> Unit
) {
    val now = System.currentTimeMillis()
    Text(
        text = stringResource(R.string.assessment_intro),
        style = MaterialTheme.typography.bodyLarge
    )
    AssessmentTask.entries.forEach { task ->
        val due = AssessmentSchedule.isDue(now, lastTaken[task])
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(taskTitleRes(task)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(taskHintRes(task)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(if (due) R.string.assessment_due else R.string.assessment_not_due),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (due) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { onStart(task) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.assessment_start))
                }
            }
        }
    }
    TextButton(onClick = onBack) {
        Text(text = stringResource(R.string.assessment_back))
    }
}

@Composable
private fun ColumnScope.AssessmentRunning(
    task: AssessmentTask,
    stimulus: Stimulus,
    onRespond: (String) -> Unit
) {
    Text(
        text = stringResource(taskTitleRes(task)),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StimulusSlot(text = stimulus.top, large = false)
        StimulusSlot(text = stimulus.middle, large = true)
        StimulusSlot(text = stimulus.bottom, large = false)
    }

    when (task) {
        AssessmentTask.GRAD_CPT -> Button(
            onClick = { onRespond(AssessmentViewModel.KEY_TAP) },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = stringResource(R.string.respond_tap), fontSize = 22.sp)
        }
        AssessmentTask.ANT -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onRespond(AssessmentViewModel.KEY_LEFT) },
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "←", fontSize = 28.sp)
            }
            Button(
                onClick = { onRespond(AssessmentViewModel.KEY_RIGHT) },
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = "→", fontSize = 28.sp)
            }
        }
        AssessmentTask.N_BACK -> Button(
            onClick = { onRespond(AssessmentViewModel.KEY_MATCH) },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = stringResource(R.string.respond_match), fontSize = 22.sp)
        }
    }
}

@Composable
private fun StimulusSlot(text: String, large: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (large) 120.dp else 64.dp),
        contentAlignment = Alignment.Center
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                fontSize = if (large) 72.sp else 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun AssessmentFinished(measures: Map<String, Double>, onDone: () -> Unit) {
    Text(
        text = stringResource(R.string.assessment_done),
        style = MaterialTheme.typography.headlineSmall
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            measures.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(measureLabelRes(key)), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "%.2f".format(value),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    Button(
        onClick = onDone,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = stringResource(R.string.assessment_back))
    }
}

@StringRes
private fun taskTitleRes(task: AssessmentTask): Int = when (task) {
    AssessmentTask.GRAD_CPT -> R.string.task_grad_cpt_title
    AssessmentTask.ANT -> R.string.task_ant_title
    AssessmentTask.N_BACK -> R.string.task_n_back_title
}

@StringRes
private fun taskHintRes(task: AssessmentTask): Int = when (task) {
    AssessmentTask.GRAD_CPT -> R.string.task_grad_cpt_hint
    AssessmentTask.ANT -> R.string.task_ant_hint
    AssessmentTask.N_BACK -> R.string.task_n_back_hint
}

@StringRes
private fun measureLabelRes(key: String): Int = when (key) {
    "dPrime" -> R.string.measure_dprime
    "commissionErrorRate" -> R.string.measure_commission
    "omissionErrorRate" -> R.string.measure_omission
    "reactionTimeVariability" -> R.string.measure_rt_variability
    "alerting" -> R.string.measure_alerting
    "orienting" -> R.string.measure_orienting
    "conflict" -> R.string.measure_conflict
    "accuracy" -> R.string.measure_accuracy
    "hitRate" -> R.string.measure_hit_rate
    "falseAlarmRate" -> R.string.measure_false_alarm_rate
    else -> error("Unknown assessment measure: $key")
}
