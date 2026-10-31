package com.barati.zehnsaz.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.barati.zehnsaz.R
import com.barati.zehnsaz.domain.model.Chapter
import com.barati.zehnsaz.domain.model.CognitiveDomain
import com.barati.zehnsaz.domain.repository.ChapterRepository
import com.barati.zehnsaz.ui.common.FormatUtils
import kotlinx.coroutines.flow.first

@Composable
fun ChapterListScreen(
    repository: ChapterRepository,
    onChapterSelected: (Chapter) -> Unit
) {
    val chapters = remember { repository.getChapters() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.training_chapters),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(chapters) { chapter ->
                var isUnlocked by remember { mutableStateOf(false) }
                
                LaunchedEffect(chapter.id) {
                    isUnlocked = repository.isChapterUnlocked(chapter.id).first()
                }

                ChapterItem(
                    chapter = chapter,
                    isUnlocked = isUnlocked,
                    onClick = { if (isUnlocked) onChapterSelected(chapter) }
                )
            }
        }
    }
}

@Composable
fun ChapterItem(
    chapter: Chapter,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { onClick() },
        elevation = CardDefaults.cardElevation(if (isUnlocked) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else Color.LightGray.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(chapter.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(chapter.subtitleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val domainName = when (chapter.cognitiveDomain) {
                    CognitiveDomain.ATTENTION -> stringResource(R.string.domain_attention)
                    CognitiveDomain.MEMORY -> stringResource(R.string.domain_memory)
                    CognitiveDomain.EXECUTIVE_FUNCTION -> stringResource(R.string.domain_executive)
                    CognitiveDomain.SPEED -> stringResource(R.string.domain_speed)
                    CognitiveDomain.LOGIC -> stringResource(R.string.domain_logic)
                }
                
                Text(
                    text = stringResource(R.string.cognitive_domain_label, domainName),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!isUnlocked) {
                Text(text = "🔒", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}