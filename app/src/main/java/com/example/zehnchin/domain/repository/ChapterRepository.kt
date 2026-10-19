package com.example.zehnchin.domain.repository

import com.example.zehnchin.R
import com.example.zehnchin.data.local.dao.LevelDao
import com.example.zehnchin.domain.economy.BalanceConfig
import com.example.zehnchin.domain.model.Chapter
import com.example.zehnchin.domain.model.CognitiveDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterRepository @Inject constructor(
    private val levelDao: LevelDao
) {
    private val chapters = listOf(
        Chapter(
            id = 0,
            titleRes = R.string.chapter_0_title,
            subtitleRes = R.string.chapter_0_subtitle,
            cognitiveDomain = CognitiveDomain.ATTENTION,
            descriptionRes = R.string.chapter_0_desc
        ),
        Chapter(
            id = 1,
            titleRes = R.string.chapter_1_title,
            subtitleRes = R.string.chapter_1_subtitle,
            cognitiveDomain = CognitiveDomain.ATTENTION,
            descriptionRes = R.string.chapter_1_desc
        ),
        Chapter(
            id = 2,
            titleRes = R.string.chapter_2_title,
            subtitleRes = R.string.chapter_2_subtitle,
            cognitiveDomain = CognitiveDomain.MEMORY,
            descriptionRes = R.string.chapter_2_desc
        ),
        Chapter(
            id = 3,
            titleRes = R.string.chapter_3_title,
            subtitleRes = R.string.chapter_3_subtitle,
            cognitiveDomain = CognitiveDomain.SPEED,
            descriptionRes = R.string.chapter_3_desc
        ),
        Chapter(
            id = 4,
            titleRes = R.string.chapter_4_title,
            subtitleRes = R.string.chapter_4_subtitle,
            cognitiveDomain = CognitiveDomain.EXECUTIVE_FUNCTION,
            descriptionRes = R.string.chapter_4_desc
        ),
        Chapter(
            id = 5,
            titleRes = R.string.chapter_5_title,
            subtitleRes = R.string.chapter_5_subtitle,
            cognitiveDomain = CognitiveDomain.LOGIC,
            descriptionRes = R.string.chapter_5_desc
        ),
        Chapter(
            id = 6,
            titleRes = R.string.chapter_6_title,
            subtitleRes = R.string.chapter_6_subtitle,
            cognitiveDomain = CognitiveDomain.LOGIC,
            descriptionRes = R.string.chapter_6_desc
        )
    )

    fun getChapters(): List<Chapter> = chapters

    fun isChapterUnlocked(chapterId: Int): Flow<Boolean> {
        if (chapterId <= 1) return kotlinx.coroutines.flow.flowOf(true)
        
        return levelDao.getAllProgress().map { progressList ->
            val completedCount = progressList.count { it.isCompleted }
            completedCount >= BalanceConfig.getChapterUnlockRequirement(chapterId)
        }
    }
}
