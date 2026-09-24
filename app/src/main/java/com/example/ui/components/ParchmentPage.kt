package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.DiaryEntity
import com.example.domain.MoodAnalysisResult
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.DancingScriptFontFamily
import com.example.ui.theme.DarkInkGold
import com.example.ui.theme.DarkParchmentSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.GreatVibesFontFamily
import com.example.ui.theme.InkBlueBlack
import com.example.ui.theme.InkChestnut
import com.example.ui.theme.InkSepiaDark
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.LeatherWarm
import com.example.ui.theme.ParchmentDark
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium
import com.example.ui.theme.WaxRed

@Composable
fun ParchmentPage(
    currentPage: DiaryEntity,
    totalPages: Int,
    content: String,
    onContentChange: (String) -> Unit,
    onChapterClick: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onNewPage: () -> Unit,
    onDeletePage: () -> Unit,
    onExportMarkdown: () -> Unit,
    onOpenReminder: () -> Unit = {},
    onOpenMoodTimeline: () -> Unit = {},
    isWritingAnimation: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    // Determine ink color from page setting
    val inkColor = when (currentPage.inkStyle) {
        "BlueBlack" -> if (isDark) Color(0xFF90B4CE) else InkBlueBlack
        "Chestnut" -> if (isDark) Color(0xFFD4A373) else InkChestnut
        else -> if (isDark) DarkInkGold else InkSepiaDark
    }

    // Outer Vintage Book Cover & Page Container
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F0805) else LeatherDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("parchment_page_container")
    ) {
        // Antique Parchment Sheet
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(LeatherDark, GoldAccentDark, Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                ),
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            color = if (isDark) DarkParchmentSurface else ParchmentLight
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background aged texture image if available
                Image(
                    painter = painterResource(id = R.drawable.parchment_bg),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = if (isDark) 0.15f else 0.45f
                )

                // Lined notebook lines + left red margin + leather spine shadow
                ParchmentBackgroundLines(
                    lineSpacingDp = 34f,
                    topMarginDp = 130f,
                    leftMarginDp = 44f,
                    isDarkMode = isDark
                )

                // Animated Fountain Pen Ink Glow
                FountainPenInkGlow(
                    isWriting = isWritingAnimation
                )

                // Page Content (Header, Text area, Navigation)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 10.dp)
                ) {
                    // Top Chapter Bar & Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chapter Title Pill (Clickable to switch chapters)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onChapterClick() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("chapter_header_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Kapitelübersicht",
                                tint = if (isDark) GoldAccentLight else LeatherBrown,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentPage.chapterTitle,
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) GoldAccentLight else LeatherDark
                            )
                        }

                        // Top right icons: Timeline, Reminder, Add Page, Share, Delete
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenMoodTimeline,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("mood_timeline_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                    contentDescription = "Seelen-Chronik (Stimmungsverlauf)",
                                    tint = if (isDark) GoldAccentLight else LeatherBrown,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            IconButton(
                                onClick = onOpenReminder,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("reminder_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Tägliche Erinnerung",
                                    tint = if (isDark) GoldAccentLight else LeatherBrown,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onNewPage,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("add_page_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = "Neue Seite",
                                    tint = if (isDark) GoldAccentLight else LeatherBrown,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onExportMarkdown,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("export_page_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Als Pergament-PDF exportieren",
                                    tint = if (isDark) GoldAccentLight else LeatherBrown,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            if (totalPages > 1) {
                                IconButton(
                                    onClick = onDeletePage,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("delete_page_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Seite löschen",
                                        tint = WaxRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Date & Ornamental Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentPage.dateString.isNotBlank()) currentPage.dateString else "Datum eintragen…",
                            fontFamily = GreatVibesFontFamily,
                            fontSize = 20.sp,
                            color = if (isDark) GoldAccentLight else GoldAccentDark
                        )

                        // Interactive Mood Badge linking to Mood Timeline Chart
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Color(MoodAnalysisResult.getMoodColorHex(currentPage.mood)).copy(
                                        alpha = if (isDark) 0.25f else 0.15f
                                    )
                                )
                                .border(
                                    1.dp,
                                    Color(MoodAnalysisResult.getMoodColorHex(currentPage.mood)).copy(
                                        alpha = if (isDark) 0.8f else 0.6f
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onOpenMoodTimeline() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                .testTag("mood_badge_btn")
                        ) {
                            Text(
                                text = MoodAnalysisResult.getMoodEmoji(currentPage.mood),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentPage.mood,
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isDark) GoldAccentLight else LeatherDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = "Stimmungsverlauf öffnen",
                                tint = if (isDark) GoldAccentLight else GoldAccentDark,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Gold flourish divider line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, GoldAccent, Color.Transparent)
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Main Text Area (Direct Calligraphic Writing)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(start = 24.dp, end = 6.dp)
                    ) {
                        BasicTextField(
                            value = content,
                            onValueChange = onContentChange,
                            textStyle = TextStyle(
                                fontFamily = DancingScriptFontFamily,
                                fontSize = 23.sp,
                                lineHeight = 34.sp,
                                letterSpacing = 0.3.sp,
                                color = inkColor
                            ),
                            cursorBrush = SolidColor(if (isDark) GoldAccent else LeatherBrown),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("diary_text_input"),
                            decorationBox = { innerTextField ->
                                if (content.isEmpty()) {
                                    Text(
                                        text = "Tippe hier oder drücke auf das Mikrofon, um deine Lebensgeschichte in Füllfeder-Tinte fließen zu lassen…",
                                        fontFamily = DancingScriptFontFamily,
                                        fontSize = 22.sp,
                                        lineHeight = 34.sp,
                                        color = if (isDark) Color(0x60EADBB6) else Color(0x603B2317)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    // Bottom Page Navigation Bar: "< Seite 1 von 5 >"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page button
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPreviousPage()
                            },
                            enabled = currentPage.pageNumber > 1,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("prev_page_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Vorherige Seite",
                                tint = if (currentPage.pageNumber > 1) {
                                    if (isDark) GoldAccentLight else LeatherBrown
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Page number in classical calligraphy
                        Text(
                            text = "— Seite ${currentPage.pageNumber} von $totalPages —",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = if (isDark) GoldAccentLight else LeatherDark
                        )

                        // Next Page button
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNextPage()
                            },
                            enabled = currentPage.pageNumber < totalPages,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("next_page_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Nächste Seite",
                                tint = if (currentPage.pageNumber < totalPages) {
                                    if (isDark) GoldAccentLight else LeatherBrown
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
