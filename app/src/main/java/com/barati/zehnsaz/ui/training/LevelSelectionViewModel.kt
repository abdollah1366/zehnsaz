package com.barati.zehnsaz.ui.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barati.zehnsaz.data.local.dao.LevelDao
import com.barati.zehnsaz.data.local.entities.LevelProgressEntity
import com.barati.zehnsaz.domain.model.Level
import com.barati.zehnsaz.domain.repository.LevelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LevelSelectionViewModel @Inject constructor(
    private val levelRepository: LevelRepository,
    private val levelDao: LevelDao
) : ViewModel() {

    private val _chapterId = MutableStateFlow<Int?>(null)
    
    val uiState: StateFlow<LevelSelectionUiState> = _chapterId
        .filterNotNull()
        .flatMapLatest { id ->
            combine(
                flowOf(levelRepository.getLevelsForChapter(id)),
                levelDao.getAllProgress()
            ) { levels, progress ->
                val levelStates = levels.map { level ->
                    val levelProgress = progress.find { it.levelId == level.id }
                    val isUnlocked = level.levelNumber == 1 || 
                        progress.any { it.levelId == level.id - 1 && it.isCompleted }
                    
                    LevelItemState(
                        level = level,
                        isUnlocked = isUnlocked,
                        isCompleted = levelProgress?.isCompleted ?: false,
                        stars = levelProgress?.stars ?: 0
                    )
                }
                LevelSelectionUiState(levelStates)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LevelSelectionUiState())

    fun setChapter(chapterId: Int) {
        _chapterId.value = chapterId
    }
}

data class LevelSelectionUiState(
    val levels: List<LevelItemState> = emptyList()
)

data class LevelItemState(
    val level: Level,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val stars: Int
)
