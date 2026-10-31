package com.barati.zehnsaz.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barati.zehnsaz.data.local.dao.AssessmentDao
import com.barati.zehnsaz.data.local.dao.AttemptDao
import com.barati.zehnsaz.domain.assessment.AssessmentTask
import com.barati.zehnsaz.domain.assessment.MeasureTrend
import com.barati.zehnsaz.domain.assessment.MeasureTrends
import com.barati.zehnsaz.domain.progression.AttemptSummary
import com.barati.zehnsaz.domain.progression.DomainSummary
import com.barati.zehnsaz.domain.progression.WeeklyPlan
import com.barati.zehnsaz.domain.progression.WeeklyPlanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    attemptDao: AttemptDao,
    assessmentDao: AssessmentDao
) : ViewModel() {

    val domains: StateFlow<List<DomainSummary>> = attemptDao.getAttempts("default_player")
        .map { AttemptSummary.summarize(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** This week's training plan, built from the attempts so far. */
    val weeklyPlan: StateFlow<WeeklyPlan> = attemptDao.getAttempts("default_player")
        .map { attempts -> WeeklyPlanner.build(attempts, WeeklyPlanner.weekStart(System.currentTimeMillis())) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklyPlan(emptyList()))

    /** Baseline assessment measures over time, one trend per task and measure. */
    val assessmentTrends: StateFlow<List<MeasureTrend>> = combine(
        AssessmentTask.entries.map { assessmentDao.getMeasures("default_player", it.name) }
    ) { lists -> MeasureTrends.build(lists.flatMap { it }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
