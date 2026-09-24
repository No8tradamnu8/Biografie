package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DiaryEntity
import com.example.domain.MoodAnalysisResult
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.DancingScriptFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.InkBlueBlack
import com.example.ui.theme.InkChestnut
import com.example.ui.theme.InkSepiaDark
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.LeatherWarm
import com.example.ui.theme.ParchmentDark
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium

@Composable
fun ChapterListDialog(
    pages: List<DiaryEntity>,
    currentPageId: Long,
    onSelectPage: (DiaryEntity) -> Unit,
    onAddChapter: (String) -> Unit,
    onSetInkStyle: (String) -> Unit,
    currentInkStyle: String,
    hasApiKey: Boolean,
    onOpenReminderSettings: () -> Unit = {},
    onOpenMoodTimeline: () -> Unit = {},
    onOpenExportDialog: () -> Unit = {},
    reminderTimeString: String = "20:00",
    isReminderEnabled: Boolean = true,
    onDismiss: () -> Unit
) {
    var newChapterTitle by remember { mutableStateOf("") }
    var showApiKeyInfo by remember { mutableStateOf(false) }

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
                            .size(34.dp)
                            .background(LeatherBrown, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Inhaltsverzeichnis",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = LeatherDark
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Schließen", tint = LeatherBrown)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Ink Style Selector
                Text(
                    text = "Tintenfarbe der Füllfeder:",
                    fontFamily = CinzelFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeatherBrown
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InkColorOption("Sepia", InkSepiaDark, currentInkStyle == "Sepia") { onSetInkStyle("Sepia") }
                    InkColorOption("Blauschwarz", InkBlueBlack, currentInkStyle == "BlueBlack") { onSetInkStyle("BlueBlack") }
                    InkColorOption("Kastanie", InkChestnut, currentInkStyle == "Chestnut") { onSetInkStyle("Chestnut") }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Daily Reminder Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenReminderSettings() }
                        .border(1.dp, GoldAccentDark, RoundedCornerShape(8.dp)),
                    color = ParchmentMedium,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (isReminderEnabled) GoldAccentDark else InkChestnut,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isReminderEnabled) "Tägliche Erinnerung: $reminderTimeString Uhr" else "Tägliche Erinnerung (inaktiv)",
                                fontFamily = CinzelFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LeatherDark
                            )
                        }
                        Text(
                            text = "Ändern",
                            fontFamily = CinzelFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccentDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Seelen-Chronik / Mood Timeline Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onOpenMoodTimeline()
                        }
                        .border(1.dp, GoldAccentDark, RoundedCornerShape(8.dp)),
                    color = ParchmentMedium,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = GoldAccentDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Seelen-Chronik (Stimmungsverlauf)",
                                fontFamily = CinzelFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LeatherDark
                            )
                        }
                        Text(
                            text = "Öffnen",
                            fontFamily = CinzelFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccentDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // PDF Export Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            onOpenExportDialog()
                        }
                        .border(1.dp, GoldAccentDark, RoundedCornerShape(8.dp)),
                    color = ParchmentMedium,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = GoldAccentDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Als Pergament-PDF exportieren",
                                fontFamily = CinzelFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LeatherDark
                            )
                        }
                        Text(
                            text = "Export",
                            fontFamily = CinzelFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccentDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gemini API Key Status Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showApiKeyInfo = !showApiKeyInfo }
                        .border(1.dp, GoldAccentDark, RoundedCornerShape(8.dp)),
                    color = ParchmentMedium,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = if (hasApiKey) Color(0xFF2E7D32) else GoldAccentDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasApiKey) "Gemini KI verbunden ✓" else "Gemini API-Schlüssel konfigurieren ⓘ",
                            fontFamily = CinzelFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LeatherDark
                        )
                    }
                }

                if (showApiKeyInfo) {
                    Text(
                        text = "Für Gemini Live und automatische Lektorats-Vorschläge kannst du deinen Gemini API-Key im Secrets-Panel von Google AI Studio eintragen (Variable: GEMINI_API_KEY).",
                        fontSize = 11.sp,
                        color = InkChestnut,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of Chapters & Pages
                Text(
                    text = "Seiten & Abschnitte:",
                    fontFamily = CinzelFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeatherBrown
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(pages) { page ->
                        val isSelected = page.id == currentPageId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectPage(page)
                                    onDismiss()
                                }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) GoldAccent else ParchmentDark,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ParchmentMedium else ParchmentLight
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Seite ${page.pageNumber}: ${page.chapterTitle}",
                                        fontFamily = CinzelFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LeatherDark
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${MoodAnalysisResult.getMoodEmoji(page.mood)} ${page.mood}",
                                            fontFamily = CinzelFontFamily,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(MoodAnalysisResult.getMoodColorHex(page.mood))
                                        )
                                        if (page.dateString.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "· ${page.dateString}",
                                                fontSize = 10.sp,
                                                color = InkChestnut
                                            )
                                        }
                                    }
                                }
                                if (page.content.isNotBlank()) {
                                    Text(
                                        text = page.content.take(50) + "…",
                                        fontFamily = DancingScriptFontFamily,
                                        fontSize = 15.sp,
                                        color = InkSepiaDark,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add New Chapter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newChapterTitle,
                        onValueChange = { newChapterTitle = it },
                        placeholder = { Text("Neuer Kapiteltitel…", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = ParchmentDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_chapter_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (newChapterTitle.isNotBlank()) {
                                onAddChapter(newChapterTitle)
                                newChapterTitle = ""
                                onDismiss()
                            }
                        },
                        enabled = newChapterTitle.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LeatherBrown,
                            contentColor = GoldAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("create_chapter_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen", fontFamily = CinzelFontFamily, color = LeatherBrown)
            }
        },
        containerColor = ParchmentLight,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun InkColorOption(
    name: String,
    color: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GoldAccent else ParchmentDark,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = ParchmentLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontFamily = CinzelFontFamily,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = LeatherDark
            )
        }
    }
}
