package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.AudioWaveformIndicator
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.DancingScriptFontFamily
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
import com.example.ui.theme.WaxRed

data class LiveChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val diarySuggestion: String? = null
)

@Composable
fun GeminiLiveDialog(
    chatMessages: List<LiveChatMessage>,
    isListening: Boolean,
    isSpeaking: Boolean,
    isLoadingAi: Boolean,
    soundLevel: Float,
    partialSpeech: String,
    languageTag: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSendManualText: (String) -> Unit,
    onApplyToDiary: (String) -> Unit,
    onClose: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(listOf(GoldAccentLight, GoldAccentDark)),
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("gemini_live_dialog"),
            color = LeatherDark,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header: Old Brass Telephone style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(LeatherWarm, CircleShape)
                                .border(1.5.dp, GoldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = GoldAccentLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Gemini Live Gespräch",
                                fontFamily = CinzelFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = GoldAccentLight
                            )
                            Text(
                                text = "Dein einfühlsamer Lebens-Biograf",
                                fontFamily = DancingScriptFontFamily,
                                fontSize = 14.sp,
                                color = ParchmentDark
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .background(WaxRed, CircleShape)
                            .testTag("close_gemini_live_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Gespräch beenden",
                            tint = ParchmentLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Speech Waveform indicator inside live mode
                if (isListening) {
                    AudioWaveformIndicator(
                        isListening = true,
                        soundLevel = soundLevel,
                        partialText = partialSpeech,
                        languageTag = languageTag,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // AI Processing Indicator
                if (isLoadingAi) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GoldAccent,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Der Biograf denkt nach und formuliert…",
                            fontFamily = DancingScriptFontFamily,
                            fontSize = 17.sp,
                            color = GoldAccentLight
                        )
                    }
                }

                // Chat Messages History
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages) { message ->
                        if (message.isUser) {
                            // User Message Bubble
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(0.85f),
                                    shape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp),
                                    color = LeatherWarm,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccentDark)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Du erzählst:",
                                            fontFamily = CinzelFontFamily,
                                            fontSize = 10.sp,
                                            color = GoldAccentLight
                                        )
                                        Text(
                                            text = message.text,
                                            fontFamily = DancingScriptFontFamily,
                                            fontSize = 18.sp,
                                            color = ParchmentLight
                                        )
                                    }
                                }
                            }
                        } else {
                            // AI Biographer Bubble
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(0.92f),
                                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
                                    color = ParchmentMedium,
                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldAccent)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = LeatherBrown,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Biograf (Gemini):",
                                                fontFamily = CinzelFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LeatherDark
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = message.text,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            color = InkSepiaDark
                                        )

                                        // Highlighted diary suggestion box + "Übernehmen" button
                                        if (!message.diarySuggestion.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                color = ParchmentLight,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(0.8.dp, GoldAccent)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        text = "Tagebuch-Vorschlag:",
                                                        fontFamily = CinzelFontFamily,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldAccentDark
                                                    )
                                                    Text(
                                                        text = "„${message.diarySuggestion}“",
                                                        fontFamily = DancingScriptFontFamily,
                                                        fontSize = 18.sp,
                                                        color = InkSepiaDark,
                                                        lineHeight = 23.sp
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Button(
                                                        onClick = { onApplyToDiary(message.diarySuggestion) },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = LeatherBrown,
                                                            contentColor = GoldAccentLight
                                                        ),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier
                                                            .align(Alignment.End)
                                                            .testTag("apply_live_suggestion_btn")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Übernehmen",
                                                            fontFamily = CinzelFontFamily,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Controls: Microphone toggle + quick text input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big Speak Toggle Button
                    IconButton(
                        onClick = {
                            if (isListening) onStopListening() else onStartListening()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = if (isListening) WaxRed else LeatherWarm,
                                shape = CircleShape
                            )
                            .border(2.dp, GoldAccent, CircleShape)
                            .testTag("gemini_live_mic_btn")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stoppen" else "Sprechen",
                            tint = if (isListening) ParchmentLight else GoldAccentLight,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        placeholder = {
                            Text(
                                "Oder tippe eine Erinnerung…",
                                fontSize = 13.sp,
                                color = ParchmentDark.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ParchmentLight,
                            unfocusedTextColor = ParchmentLight,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = LeatherWarm,
                            cursorColor = GoldAccent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("live_chat_input")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (manualInput.isNotBlank()) {
                                onSendManualText(manualInput)
                                manualInput = ""
                            }
                        },
                        enabled = manualInput.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .background(if (manualInput.isNotBlank()) GoldAccent else LeatherWarm, CircleShape)
                            .testTag("live_chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Senden",
                            tint = LeatherDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
