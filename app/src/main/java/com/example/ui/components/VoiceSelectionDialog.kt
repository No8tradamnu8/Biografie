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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.FemaleVoiceProfile
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.DancingScriptFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.InkChestnut
import com.example.ui.theme.InkSepiaDark
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.ParchmentDark
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium

@Composable
fun VoiceSelectionDialog(
    availableVoices: List<FemaleVoiceProfile>,
    selectedVoiceId: String,
    onSelectVoice: (String) -> Unit,
    onTestVoice: (FemaleVoiceProfile) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(LeatherBrown, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Vorlesestimme wählen",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = LeatherDark
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Wähle deine bevorzugte Frauenstimme für die Vorlesung deines Tagebuchs:",
                    fontSize = 13.sp,
                    color = InkChestnut,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableVoices) { profile ->
                        val isSelected = profile.id == selectedVoiceId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectVoice(profile.id) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                    color = if (isSelected) GoldAccent else ParchmentDark,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) ParchmentMedium else ParchmentLight
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectVoice(profile.id) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = LeatherBrown,
                                        unselectedColor = GoldAccentDark
                                    )
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 6.dp)
                                ) {
                                    Text(
                                        text = profile.name,
                                        fontFamily = CinzelFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = LeatherDark
                                    )
                                    Text(
                                        text = profile.description,
                                        fontSize = 11.sp,
                                        color = InkChestnut,
                                        lineHeight = 14.sp
                                    )
                                }

                                IconButton(
                                    onClick = { onTestVoice(profile) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Stimme testen",
                                        tint = LeatherBrown,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
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
                Text("Fertig", fontFamily = CinzelFontFamily)
            }
        },
        containerColor = ParchmentLight,
        shape = RoundedCornerShape(16.dp)
    )
}
