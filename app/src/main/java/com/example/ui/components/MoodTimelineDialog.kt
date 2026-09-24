package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DiaryEntity
import com.example.domain.MoodAnalysisResult
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.GreatVibesFontFamily
import com.example.ui.theme.InkChestnut
import com.example.ui.theme.InkSepiaDark
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.LeatherWarm
import com.example.ui.theme.ParchmentDark
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodTimelineDialog(
    pages: List<DiaryEntity>,
    currentPageId: Long,
    hasApiKey: Boolean,
    onSelectPage: (DiaryEntity) -> Unit,
    onReanalyzeCurrentPage: () -> Unit,
    onDismiss: () -> Unit
) {
    // Filter and sort pages by pageNumber / date
    val sortedPages = remember(pages) {
        if (pages.isEmpty()) {
            listOf(
                DiaryEntity(
                    id = 1,
                    pageNumber = 1,
                    chapterTitle = "Kapitel 1",
                    pageTitle = "Heutige Seite",
                    dateString = "Heute",
                    content = "",
                    mood = "Offen",
                    moodScore = 6.0f
                )
            )
        } else {
            pages.sortedBy { it.pageNumber }
        }
    }

    var selectedIndex by remember(sortedPages, currentPageId) {
        val idx = sortedPages.indexOfFirst { it.id == currentPageId }
        mutableIntStateOf(if (idx >= 0) idx else (sortedPages.size - 1).coerceAtLeast(0))
    }

    val selectedPage = sortedPages.getOrNull(selectedIndex) ?: sortedPages.first()

    // Aggregate statistics
    val averageScore = remember(sortedPages) {
        val valid = sortedPages.map { it.moodScore }
        if (valid.isNotEmpty()) valid.average().toFloat() else 6.0f
    }

    val moodCounts = remember(sortedPages) {
        sortedPages.groupingBy { it.mood.ifBlank { "Nachdenklich" } }.eachCount()
    }

    val dominantMood = remember(moodCounts) {
        moodCounts.maxByOrNull { it.value }?.key ?: "Nachdenklich"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(LeatherBrown, CircleShape)
                            .border(1.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = GoldAccentLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Seelen-Chronik",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = LeatherDark
                        )
                        Text(
                            text = "Emotionale Entwicklung über die Zeit",
                            fontFamily = CinzelFontFamily,
                            fontSize = 11.sp,
                            color = InkChestnut
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Schließen", tint = LeatherBrown)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Free Version / Gemini Indicator Banner
                Surface(
                    color = ParchmentMedium,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.8.dp, GoldAccentDark, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (hasApiKey) Icons.Default.AutoAwesome else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (hasApiKey) GoldAccentDark else Color(0xFF5E8B64),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasApiKey) "Gemini KI-Stimmungserkennung aktiv" else "Freie Version: Automatische Stimmungs-Erkennung",
                                fontFamily = CinzelFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LeatherDark
                            )
                        }

                        IconButton(
                            onClick = onReanalyzeCurrentPage,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Neu berechnen",
                                tint = LeatherBrown,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats overview cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Average score card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = ParchmentMedium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ParchmentDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "DURCHSCHNITT",
                                fontFamily = CinzelFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkChestnut
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.GERMAN, "%.1f / 10", averageScore),
                                fontFamily = CinzelFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LeatherDark
                            )
                            Text(
                                text = when {
                                    averageScore >= 7.5f -> "☀️ Strahlend & Erfüllt"
                                    averageScore >= 6.0f -> "🕊️ Friedvoll & Heiter"
                                    averageScore >= 4.5f -> "🕯️ Ausgeglichen"
                                    else -> "🌧️ Tiefgründig"
                                },
                                fontSize = 10.sp,
                                color = InkSepiaDark
                            )
                        }
                    }

                    // Dominant mood card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = ParchmentMedium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ParchmentDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "HAUPTGEFÜHL",
                                fontFamily = CinzelFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkChestnut
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${MoodAnalysisResult.getMoodEmoji(dominantMood)} $dominantMood",
                                fontFamily = CinzelFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(MoodAnalysisResult.getMoodColorHex(dominantMood))
                            )
                            Text(
                                text = "${moodCounts[dominantMood] ?: 1} Einträge",
                                fontSize = 10.sp,
                                color = InkSepiaDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chart Container Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verlaufskurve der Gefühle",
                        fontFamily = CinzelFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LeatherDark
                    )
                    Text(
                        text = "Tippe auf Punkte für Details",
                        fontSize = 10.sp,
                        color = InkChestnut
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Antique Mood Canvas Chart
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ParchmentLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .border(1.2.dp, GoldAccentDark, RoundedCornerShape(12.dp))
                        .testTag("mood_timeline_canvas")
                ) {
                    AntiqueMoodCanvas(
                        pages = sortedPages,
                        selectedIndex = selectedIndex,
                        onSelectPoint = { selectedIndex = it }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detail Card for Selected Entry
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ParchmentMedium,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccentLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Seite ${selectedPage.pageNumber} · ${selectedPage.chapterTitle}",
                                    fontFamily = CinzelFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkChestnut
                                )
                                Text(
                                    text = if (selectedPage.dateString.isNotBlank()) selectedPage.dateString else "Unbekanntes Datum",
                                    fontFamily = GreatVibesFontFamily,
                                    fontSize = 16.sp,
                                    color = GoldAccentDark
                                )
                            }

                            // Mood Pill Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(MoodAnalysisResult.getMoodColorHex(selectedPage.mood)).copy(alpha = 0.2f))
                                    .border(
                                        1.dp,
                                        Color(MoodAnalysisResult.getMoodColorHex(selectedPage.mood)),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = MoodAnalysisResult.getMoodEmoji(selectedPage.mood),
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${selectedPage.mood} (${String.format(Locale.GERMAN, "%.1f", selectedPage.moodScore)})",
                                        fontFamily = CinzelFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(MoodAnalysisResult.getMoodColorHex(selectedPage.mood))
                                    )
                                }
                            }
                        }

                        // Keywords tags
                        if (selectedPage.moodKeywords.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                selectedPage.moodKeywords.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .background(ParchmentLight, RoundedCornerShape(6.dp))
                                            .border(0.5.dp, ParchmentDark, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            fontSize = 9.sp,
                                            fontFamily = CinzelFontFamily,
                                            color = InkChestnut
                                        )
                                    }
                                }
                            }
                        }

                        // Content snippet
                        if (selectedPage.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "„${selectedPage.content.take(120).trim()}…“",
                                fontSize = 11.sp,
                                color = LeatherDark,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Jump to page button
                        Button(
                            onClick = {
                                onSelectPage(selectedPage)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LeatherBrown,
                                contentColor = GoldAccent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .testTag("jump_to_page_btn")
                        ) {
                            Text(
                                text = "Diese Seite aufschlagen",
                                fontFamily = CinzelFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mood breakdown distribution
                Text(
                    text = "Gefühls-Verteilung",
                    fontFamily = CinzelFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeatherDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                moodCounts.entries.sortedByDescending { it.value }.take(4).forEach { (mood, count) ->
                    val percentage = (count.toFloat() / sortedPages.size.coerceAtLeast(1))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${MoodAnalysisResult.getMoodEmoji(mood)} $mood",
                            fontSize = 10.sp,
                            fontFamily = CinzelFontFamily,
                            color = LeatherDark,
                            modifier = Modifier.width(110.dp)
                        )
                        LinearProgressIndicator(
                            progress = { percentage },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(MoodAnalysisResult.getMoodColorHex(mood)),
                            trackColor = ParchmentDark.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(percentage * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontFamily = CinzelFontFamily,
                            color = InkChestnut,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LeatherBrown,
                    contentColor = GoldAccent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Schließen", fontFamily = CinzelFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = ParchmentLight,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Beautiful Antique Canvas Chart drawing mood curves, levels, and golden node ink drops.
 */
@Composable
fun AntiqueMoodCanvas(
    pages: List<DiaryEntity>,
    selectedIndex: Int,
    onSelectPoint: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(pages) {
                detectTapGestures { offset ->
                    val width = size.width
                    val paddingX = 40f
                    val availableWidth = width - (paddingX * 2)

                    if (pages.size <= 1) {
                        onSelectPoint(0)
                        return@detectTapGestures
                    }

                    val stepX = availableWidth / (pages.size - 1)
                    val clickedIndex = ((offset.x - paddingX + (stepX / 2)) / stepX)
                        .toInt()
                        .coerceIn(0, pages.size - 1)
                    onSelectPoint(clickedIndex)
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingX = 45f
        val paddingTop = 25f
        val paddingBottom = 30f
        val availableHeight = height - paddingTop - paddingBottom
        val availableWidth = width - (paddingX * 2)

        // Draw horizontal antique guide lines
        // Levels: 8.5 (☀️), 6.5 (✨/🕊️), 4.5 (🕯️), 2.5 (🌧️)
        val levels = listOf(
            Triple(9.0f, "☀️", Color(0xFFD4AF37).copy(alpha = 0.35f)),
            Triple(6.5f, "🕊️", Color(0xFF6B8E70).copy(alpha = 0.35f)),
            Triple(4.5f, "🕯️", Color(0xFFB38F61).copy(alpha = 0.35f)),
            Triple(2.0f, "🌧️", Color(0xFF6C7A89).copy(alpha = 0.35f))
        )

        for ((level, _, color) in levels) {
            val normalized = (level - 1.0f) / 9.0f
            val y = height - paddingBottom - (normalized * availableHeight)
            drawLine(
                color = color,
                start = Offset(paddingX, y),
                end = Offset(width - paddingX, y),
                strokeWidth = 1f
            )
        }

        if (pages.isEmpty()) return@Canvas

        // Calculate points
        val points = mutableListOf<Offset>()
        if (pages.size == 1) {
            val score = pages[0].moodScore.coerceIn(1.0f, 10.0f)
            val normalized = (score - 1.0f) / 9.0f
            val y = height - paddingBottom - (normalized * availableHeight)
            val x = width / 2f
            points.add(Offset(x, y))
        } else {
            val stepX = availableWidth / (pages.size - 1)
            for (i in pages.indices) {
                val score = pages[i].moodScore.coerceIn(1.0f, 10.0f)
                val normalized = (score - 1.0f) / 9.0f
                val y = height - paddingBottom - (normalized * availableHeight)
                val x = paddingX + (i * stepX)
                points.add(Offset(x, y))
            }
        }

        // Draw smooth gradient curve
        if (points.size > 1) {
            val curvePath = Path()
            val fillPath = Path()

            curvePath.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height - paddingBottom)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                curvePath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
            }

            fillPath.lineTo(points.last().x, height - paddingBottom)
            fillPath.close()

            // Translucent warm gold fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFD4AF37).copy(alpha = 0.25f),
                        Color(0xFFE6D2A8).copy(alpha = 0.05f)
                    ),
                    startY = paddingTop,
                    endY = height - paddingBottom
                )
            )

            // Draw glowing antique line
            drawPath(
                path = curvePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF8C6239),
                        Color(0xFFD4AF37),
                        Color(0xFFC9A84C),
                        Color(0xFF8C6239)
                    )
                ),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }

        // Draw nodes for each page
        for (i in points.indices) {
            val pt = points[i]
            val isSelected = i == selectedIndex
            val mood = pages[i].mood
            val nodeColor = Color(MoodAnalysisResult.getMoodColorHex(mood))

            if (isSelected) {
                // Outer glowing halo
                drawCircle(
                    color = Color(0xFFD4AF37).copy(alpha = 0.35f),
                    radius = 14f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFFFAF4E8),
                    radius = 8f,
                    center = pt
                )
                drawCircle(
                    color = nodeColor,
                    radius = 6f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFF4A2C11),
                    radius = 8f,
                    center = pt,
                    style = Stroke(width = 2f)
                )
            } else {
                drawCircle(
                    color = Color(0xFFFAF4E8),
                    radius = 6f,
                    center = pt
                )
                drawCircle(
                    color = nodeColor,
                    radius = 4.5f,
                    center = pt
                )
                drawCircle(
                    color = Color(0xFF8C6239),
                    radius = 6f,
                    center = pt,
                    style = Stroke(width = 1.2f)
                )
            }
        }
    }
}
