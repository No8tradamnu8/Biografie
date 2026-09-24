package com.example.ui.components

import android.app.TimePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reminder.ReminderSettings
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.InkChestnut
import com.example.ui.theme.InkSepiaDark
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.LeatherWarm
import com.example.ui.theme.ParchmentDark
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium
import java.util.Locale

@Composable
fun ReminderSettingsDialog(
    currentSettings: ReminderSettings,
    onSaveSettings: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onTestNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(currentSettings.isEnabled) }
    var selectedHour by remember { mutableIntStateOf(currentSettings.hour) }
    var selectedMinute by remember { mutableIntStateOf(currentSettings.minute) }

    val formattedTime = remember(selectedHour, selectedMinute) {
        String.format(Locale.GERMAN, "%02d:%02d Uhr", selectedHour, selectedMinute)
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
            },
            selectedHour,
            selectedMinute,
            true // 24-hour format
        )
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
                            .size(36.dp)
                            .background(LeatherBrown, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tägliche Erinnerung",
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
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Lass dich jeden Tag sanft daran erinnern, deine Gedanken und Erlebnisse im Tagebuch festzuhalten.",
                    fontSize = 13.sp,
                    color = LeatherDark,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Switch row: Erinnerung aktivieren
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ParchmentMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ParchmentDark, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Erinnerung aktivieren",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = LeatherDark
                            )
                            Text(
                                text = if (isEnabled) "Täglicher sanfter Hinweis aktiv" else "Erinnerungen deaktiviert",
                                fontSize = 11.sp,
                                color = InkChestnut
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldAccentLight,
                                checkedTrackColor = LeatherBrown,
                                uncheckedThumbColor = ParchmentDark,
                                uncheckedTrackColor = ParchmentLight
                            ),
                            modifier = Modifier.testTag("reminder_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time selection card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isEnabled) ParchmentLight else ParchmentLight.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.2.dp,
                            color = if (isEnabled) GoldAccentDark else ParchmentDark,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = isEnabled) {
                            timePickerDialog.updateTime(selectedHour, selectedMinute)
                            timePickerDialog.show()
                        }
                        .testTag("reminder_time_picker_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(LeatherWarm, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = GoldAccentLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Uhrzeit",
                                    fontFamily = CinzelFontFamily,
                                    fontSize = 11.sp,
                                    color = InkChestnut
                                )
                                Text(
                                    text = formattedTime,
                                    fontFamily = CinzelFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (isEnabled) LeatherDark else LeatherBrown.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Text(
                            text = "Ändern",
                            fontFamily = CinzelFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEnabled) GoldAccentDark else LeatherBrown.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Test notification button
                OutlinedButton(
                    onClick = onTestNotification,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_notification_btn"),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccentDark)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = LeatherBrown,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Probe-Benachrichtigung senden",
                        fontFamily = CinzelFontFamily,
                        fontSize = 11.sp,
                        color = LeatherBrown
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveSettings(isEnabled, selectedHour, selectedMinute)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LeatherBrown,
                    contentColor = GoldAccent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_reminder_btn")
            ) {
                Text(
                    text = "Speichern",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", fontFamily = CinzelFontFamily, color = LeatherBrown)
            }
        },
        containerColor = ParchmentLight,
        shape = RoundedCornerShape(16.dp)
    )
}
