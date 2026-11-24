package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChapterEntity
import com.example.data.CommentEntity
import com.example.data.ReaderFontStyle
import com.example.data.ReaderThemeMode
import com.example.data.StoryEntity
import com.example.data.UserPreferences
import com.example.data.formatCompactCount
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ReaderMidnightBg
import com.example.ui.theme.ReaderMidnightFg
import com.example.ui.theme.ReaderOledBg
import com.example.ui.theme.ReaderOledFg
import com.example.ui.theme.ReaderPaperBg
import com.example.ui.theme.ReaderPaperFg
import com.example.ui.theme.ReaderSepiaBg
import com.example.ui.theme.ReaderSepiaFg
import com.example.ui.theme.WarmGold
import kotlin.math.roundToInt

@Composable
fun ReaderScreen(
    story: StoryEntity?,
    chapters: List<ChapterEntity>,
    currentChapterIndex: Int,
    comments: List<CommentEntity>,
    preferences: UserPreferences,
    onBack: () -> Unit,
    onSelectChapterIndex: (Int) -> Unit,
    onToggleVote: (Long, Long) -> Unit,
    onPostComment: (Long, Long, String, Int) -> Unit,
    onToggleCommentLike: (Long) -> Unit,
    onProgressChanged: (Int, Float) -> Unit,
    onRecordRead: (Long) -> Unit,
    onSetReaderTheme: (ReaderThemeMode) -> Unit,
    onSetReaderFont: (ReaderFontStyle) -> Unit,
    onSetFontSize: (Float) -> Unit,
    onSetLineHeight: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeIndex = currentChapterIndex.coerceIn(0, (chapters.size - 1).coerceAtLeast(0))
    val currentChapter = chapters.getOrNull(safeIndex)

    if (story == null || currentChapter == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chapter unavailable")
        }
        return
    }

    val (bgColor, fgColor, subtleColor) = when (preferences.readerTheme) {
        ReaderThemeMode.PAPER -> Triple(ReaderPaperBg, ReaderPaperFg, Color(0xFF6E655A))
        ReaderThemeMode.SEPIA -> Triple(ReaderSepiaBg, ReaderSepiaFg, Color(0xFF7A634C))
        ReaderThemeMode.MIDNIGHT -> Triple(ReaderMidnightBg, ReaderMidnightFg, Color(0xFF949BB0))
        ReaderThemeMode.OLED -> Triple(ReaderOledBg, ReaderOledFg, Color(0xFF8C8C8C))
    }

    val readerFontFamily = when (preferences.readerFont) {
        ReaderFontStyle.SERIF -> MerriweatherFontFamily
        ReaderFontStyle.SANS -> PlusJakartaSansFontFamily
    }

    var chromeVisible by remember { mutableStateOf(true) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showChaptersSheet by remember { mutableStateOf(false) }
    var activeCommentParagraph by remember { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()
    val paragraphs = remember(currentChapter.id, currentChapter.body) {
        currentChapter.body
            .split(Regex("\\n\\s*\\n|\\n"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    // Record read once per story session
    LaunchedEffect(story.id) {
        onRecordRead(story.id)
    }

    // Reset scroll when switching chapters
    LaunchedEffect(currentChapter.id) {
        listState.scrollToItem(0)
    }

    val scrollProgress by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount.coerceAtLeast(1)
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            ((lastVisible + 1).toFloat() / total.toFloat()).coerceIn(0.05f, 1f)
        }
    }

    LaunchedEffect(safeIndex, scrollProgress) {
        onProgressChanged(safeIndex, scrollProgress)
    }

    val commentsByParagraph = remember(comments) {
        comments.groupBy { it.paragraphIndex }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { chromeVisible = !chromeVisible },
            contentPadding = PaddingValues(
                top = 92.dp,
                bottom = 112.dp,
                start = 22.dp,
                end = 22.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chapter Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PART ${currentChapter.chapterNumber} OF ${chapters.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentChapter.title,
                        fontFamily = PlayfairDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        color = fgColor
                    )
                    Text(
                        text = "by ${story.authorName} • ${currentChapter.wordCount} words",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtleColor
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = subtleColor.copy(alpha = 0.25f)
                    )
                }
            }

            // Paragraphs with inline Wattpad-style comment bubbles
            itemsIndexed(
                items = paragraphs,
                key = { idx, _ -> "para_${currentChapter.id}_$idx" }
            ) { index, paragraphText ->
                val paraComments = commentsByParagraph[index].orEmpty()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = paragraphText,
                        fontFamily = readerFontFamily,
                        fontSize = preferences.fontSizeSp.sp,
                        lineHeight = (preferences.fontSizeSp * preferences.lineHeightMultiplier).sp,
                        color = fgColor,
                        modifier = Modifier.weight(1f)
                    )

                    // Inline paragraph comment pill (always >= 48dp touch target)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { activeCommentParagraph = index }
                            .testTag("para_comment_btn_$index"),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (paraComments.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ChatBubbleOutline,
                                        contentDescription = "${paraComments.size} paragraph comments",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = paraComments.size.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.ChatBubbleOutline,
                                contentDescription = "Comment on paragraph",
                                tint = subtleColor.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(15.dp)
                            )
                        }
                    }
                }
            }

            // Author's Note if present
            if (currentChapter.authorNote.isNotBlank()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 680.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = subtleColor.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Author's Note",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = currentChapter.authorNote,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                color = fgColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // End-of-Chapter Engagement Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = subtleColor.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onToggleVote(currentChapter.id, story.id) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("reader_vote_button")
                        ) {
                            Icon(
                                imageVector = if (currentChapter.isVoted) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = null,
                                tint = if (currentChapter.isVoted) WarmGold else fgColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentChapter.isVoted) {
                                    "Voted (${formatCompactCount(currentChapter.votesCount)})"
                                } else {
                                    "Vote (${formatCompactCount(currentChapter.votesCount)})"
                                },
                                color = fgColor
                            )
                        }

                        OutlinedButton(
                            onClick = { activeCommentParagraph = -1 },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("reader_comments_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubbleOutline,
                                contentDescription = null,
                                tint = fgColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Comments (${comments.size})",
                                color = fgColor
                            )
                        }
                    }

                    if (safeIndex < chapters.lastIndex) {
                        Button(
                            onClick = { onSelectChapterIndex(safeIndex + 1) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("reader_next_chapter_button")
                        ) {
                            Text("Continue to Next Part")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (story.status == "Completed") "End of Story" else "You're Caught Up!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Leave a vote or comment to support ${story.authorName}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Reader Chrome
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = bgColor.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("reader_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit reader",
                                tint = fgColor
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showChaptersSheet = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = story.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = fgColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentChapter.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = subtleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { showChaptersSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("reader_toc_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                contentDescription = "Table of contents",
                                tint = fgColor
                            )
                        }

                        IconButton(
                            onClick = { showSettingsSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("reader_typography_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FormatSize,
                                contentDescription = "Reader appearance settings",
                                tint = fgColor
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { scrollProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = subtleColor.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Bottom Quick Action Bar
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = bgColor.copy(alpha = 0.94f),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onToggleVote(currentChapter.id, story.id) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (currentChapter.isVoted) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Vote chapter",
                            tint = if (currentChapter.isVoted) WarmGold else fgColor
                        )
                    }

                    IconButton(
                        onClick = { activeCommentParagraph = -1 },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = "Chapter comments",
                            tint = fgColor
                        )
                    }

                    Text(
                        text = "${(scrollProgress * 100).roundToInt()}% read",
                        style = MaterialTheme.typography.labelMedium,
                        color = subtleColor
                    )

                    IconButton(
                        onClick = { if (safeIndex < chapters.lastIndex) onSelectChapterIndex(safeIndex + 1) },
                        enabled = safeIndex < chapters.lastIndex,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next chapter",
                            tint = if (safeIndex < chapters.lastIndex) fgColor else subtleColor.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }

    if (showSettingsSheet) {
        ReaderSettingsSheet(
            preferences = preferences,
            onDismiss = { showSettingsSheet = false },
            onSelectTheme = onSetReaderTheme,
            onSelectFont = onSetReaderFont,
            onFontSizeChange = onSetFontSize,
            onLineHeightChange = onSetLineHeight
        )
    }

    if (showChaptersSheet) {
        ChapterPickerSheet(
            chapters = chapters,
            currentIndex = safeIndex,
            onDismiss = { showChaptersSheet = false },
            onSelect = { idx ->
                showChaptersSheet = false
                onSelectChapterIndex(idx)
            }
        )
    }

    activeCommentParagraph?.let { paraIdx ->
        val filteredComments = if (paraIdx >= 0) {
            comments.filter { it.paragraphIndex == paraIdx }
        } else {
            comments
        }
        val quotePreview = if (paraIdx >= 0) paragraphs.getOrNull(paraIdx) else null

        CommentsBottomSheet(
            title = if (paraIdx >= 0) "Inline Comments (Para ${paraIdx + 1})" else "Chapter Comments",
            quotePreview = quotePreview,
            comments = filteredComments,
            onDismiss = { activeCommentParagraph = null },
            onPost = { text ->
                onPostComment(currentChapter.id, story.id, text, paraIdx)
            },
            onToggleLike = onToggleCommentLike
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(
    preferences: UserPreferences,
    onDismiss: () -> Unit,
    onSelectTheme: (ReaderThemeMode) -> Unit,
    onSelectFont: (ReaderFontStyle) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reading Experience",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(text = "Page Theme", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReaderThemeMode.entries.forEach { mode ->
                    val swatchBg = when (mode) {
                        ReaderThemeMode.PAPER -> ReaderPaperBg
                        ReaderThemeMode.SEPIA -> ReaderSepiaBg
                        ReaderThemeMode.MIDNIGHT -> ReaderMidnightBg
                        ReaderThemeMode.OLED -> ReaderOledBg
                    }
                    val swatchFg = when (mode) {
                        ReaderThemeMode.PAPER -> ReaderPaperFg
                        ReaderThemeMode.SEPIA -> ReaderSepiaFg
                        ReaderThemeMode.MIDNIGHT -> ReaderMidnightFg
                        ReaderThemeMode.OLED -> ReaderOledFg
                    }
                    val selected = preferences.readerTheme == mode

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(swatchBg)
                            .clickable { onSelectTheme(mode) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aa",
                            style = MaterialTheme.typography.titleMedium,
                            color = swatchFg
                        )
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = swatchFg
                        )
                    }
                }
            }

            Text(text = "Typeface", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReaderFontStyle.entries.forEach { font ->
                    FilterChip(
                        selected = preferences.readerFont == font,
                        onClick = { onSelectFont(font) },
                        label = { Text(font.label) }
                    )
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Text Size", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${preferences.fontSizeSp.roundToInt()} sp",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Slider(
                    value = preferences.fontSizeSp,
                    onValueChange = onFontSizeChange,
                    valueRange = 14f..24f
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Line Spacing", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Compact" to 1.4f,
                        "Balanced" to 1.65f,
                        "Relaxed" to 1.85f
                    ).forEach { (label, mult) ->
                        FilterChip(
                            selected = kotlin.math.abs(preferences.lineHeightMultiplier - mult) < 0.08f,
                            onClick = { onLineHeightChange(mult) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterPickerSheet(
    chapters: List<ChapterEntity>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Chapters (${chapters.size})",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(chapters) { idx, ch ->
                    val selected = idx == currentIndex
                    Surface(
                        onClick = { onSelect(idx) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ch.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${ch.wordCount}w",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    title: String,
    quotePreview: String?,
    comments: List<CommentEntity>,
    onDismiss: () -> Unit,
    onPost: (String) -> Unit,
    onToggleLike: (Long) -> Unit
) {
    var commentInput by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)

            if (!quotePreview.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"$quotePreview\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (comments.isEmpty()) {
                Text(
                    text = "No comments here yet. Start the conversation!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { "cmt_${it.id}" }) { comment ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = comment.authorInitials,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(
                                onClick = { onToggleLike(comment.id) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Like comment",
                                        tint = if (comment.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = comment.likesCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    placeholder = { Text("Write a reaction...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_text_input")
                )
                Surface(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            onPost(commentInput)
                            commentInput = ""
                        }
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("comment_send_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Post comment",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
