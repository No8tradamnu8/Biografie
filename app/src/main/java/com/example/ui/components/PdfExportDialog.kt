package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DiaryEntity
import com.example.domain.MoodAnalysisResult
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.InkChestnut
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.LeatherWarm
import com.example.ui.theme.ParchmentDark
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium

enum class ExportScope {
    CURRENT_PAGE,
    ALL_PAGES,
    MARKDOWN_TEXT
}

enum class ExportAction {
    SHARE,
    VIEW_PRINT
}

@Composable
fun PdfExportDialog(
    currentPage: DiaryEntity,
    totalPagesCount: Int,
    isExporting: Boolean,
    exportStatusMessage: String,
    onExportPdf: (ExportScope, ExportAction) -> Unit,
    onExportMarkdown: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedScope by remember { mutableStateOf(ExportScope.CURRENT_PAGE) }

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
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
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pergament-Export",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = LeatherDark
                        )
                        Text(
                            text = "Als stilvolles Dokument verewigen",
                            fontSize = 11.sp,
                            color = InkChestnut
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !isExporting,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Schließen",
                        tint = LeatherBrown,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                if (isExporting) {
                    // Loading State during PDF creation
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = GoldAccentDark,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = exportStatusMessage.ifBlank { "Pergament-Layout wird gerendert…" },
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = LeatherDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kalligraphie, Zierrahmen & Tinte werden aufbereitet",
                            fontSize = 11.sp,
                            color = InkChestnut
                        )
                    }
                } else {
                    Text(
                        text = "Wähle das Format für dein Tagebuch:",
                        fontFamily = CinzelFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LeatherBrown
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 1: Current Page as PDF
                    ExportOptionCard(
                        title = "Aktuelles Blatt als PDF",
                        subtitle = "Seite ${currentPage.pageNumber}: »${currentPage.chapterTitle}« mit ${MoodAnalysisResult.getMoodEmoji(currentPage.mood)} ${currentPage.mood}-Stempel",
                        icon = Icons.Default.Description,
                        isSelected = selectedScope == ExportScope.CURRENT_PAGE,
                        onClick = { selectedScope = ExportScope.CURRENT_PAGE },
                        testTag = "export_option_current_page"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Full Diary as Bound Book PDF
                    ExportOptionCard(
                        title = "Gesamtes Tagebuch als PDF-Buch",
                        subtitle = "Alle $totalPagesCount Kapitel gebunden, inklusive kunstvollem Deckblatt & Siegel",
                        icon = Icons.Default.AutoStories,
                        isSelected = selectedScope == ExportScope.ALL_PAGES,
                        onClick = { selectedScope = ExportScope.ALL_PAGES },
                        testTag = "export_option_full_diary"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 3: Plain text / Markdown
                    ExportOptionCard(
                        title = "Als Text / Markdown teilen",
                        subtitle = "Reiner Text für Notiz-Apps, Zwischenablage oder einfaches Backup",
                        icon = Icons.Default.TextFields,
                        isSelected = selectedScope == ExportScope.MARKDOWN_TEXT,
                        onClick = { selectedScope = ExportScope.MARKDOWN_TEXT },
                        testTag = "export_option_markdown"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Parchment preview info box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GoldAccentLight, RoundedCornerShape(8.dp)),
                        color = ParchmentLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📜",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedScope == ExportScope.MARKDOWN_TEXT) {
                                    "Ermöglicht schnelles Kopieren und Sichern in allen Text-Editoren."
                                } else {
                                    "Das PDF wird mit antikem Pergament-Hintergrund, goldenem Zierrahmen und deiner gewählten Tinte (${currentPage.inkStyle}) formatiert."
                                },
                                fontSize = 10.5.sp,
                                color = LeatherDark,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isExporting) {
                if (selectedScope == ExportScope.MARKDOWN_TEXT) {
                    Button(
                        onClick = {
                            onDismiss()
                            onExportMarkdown()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LeatherBrown,
                            contentColor = GoldAccentLight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_share_markdown")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Als Text teilen",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Open / Print
                        OutlinedButton(
                            onClick = {
                                onExportPdf(selectedScope, ExportAction.VIEW_PRINT)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = LeatherDark
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(GoldAccentDark)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_open_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                tint = LeatherBrown,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Öffnen / Drucken",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        }

                        // Share
                        Button(
                            onClick = {
                                onExportPdf(selectedScope, ExportAction.SHARE)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LeatherBrown,
                                contentColor = GoldAccentLight
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_share_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PDF Teilen",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (!isExporting) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Abbrechen",
                        fontFamily = CinzelFontFamily,
                        color = InkChestnut,
                        fontSize = 11.sp
                    )
                }
            }
        },
        containerColor = ParchmentMedium,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 1.8.dp else 0.8.dp,
                color = if (isSelected) GoldAccent else ParchmentDark,
                shape = RoundedCornerShape(8.dp)
            )
            .testTag(testTag),
        color = if (isSelected) ParchmentLight else Color(0xFFF7EEDD).copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (isSelected) LeatherBrown else ParchmentDark,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) GoldAccentLight else LeatherBrown,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isSelected) LeatherDark else LeatherBrown
                )
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = InkChestnut,
                    lineHeight = 13.sp
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(GoldAccentDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = ParchmentLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
