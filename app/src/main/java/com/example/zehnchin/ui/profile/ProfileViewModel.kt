package com.example.zehnchin.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zehnchin.data.local.dao.AttemptDao
import com.example.zehnchin.domain.progression.AttemptSummary
import com.example.zehnchin.domain.progression.DomainSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    attemptDao: AttemptDao
) : ViewModel() {

    val domains: StateFlow<List<DomainSummary>> = attemptDao.getAttempts("default_player")
        .map { AttemptSummary.summarize(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
