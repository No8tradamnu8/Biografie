package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.WaxRed

@Composable
fun VintageBottomToolbar(
    isListening: Boolean,
    isSpeaking: Boolean,
    languageTag: String,
    onToggleListen: () -> Unit,
    onOpenGeminiLive: () -> Unit,
    onToggleReadAloud: () -> Unit,
    onOpenVoiceSettings: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSummarizeChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Vintage leather bar container with brass / gold borders
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(GoldAccentLight, GoldAccentDark)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
        color = LeatherDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Upper row: Language switch + Audio reading status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language toggle badge: de-CH vs de-DE
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LeatherWarm)
                        .border(0.8.dp, GoldAccentDark, RoundedCornerShape(12.dp))
                        .clickable { onToggleLanguage() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("lang_toggle_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (languageTag == "de-CH") "🇨🇭 Schwiizertütsch" else "🇩🇪 Hochdeutsch",
                            fontFamily = CinzelFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccentLight
                        )
                    }
                }

                // Voice selection shortcut
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenVoiceSettings() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("voice_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Stimme wählen",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Stimme",
                        fontFamily = CinzelFontFamily,
                        fontSize = 11.sp,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Vorlesen Button (Antique Speaker)
                VintageActionButton(
                    label = if (isSpeaking) "Halt" else "Vorlesen",
                    icon = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    isActive = isSpeaking,
                    activeColor = WaxRed,
                    onClick = onToggleReadAloud,
                    testTag = "read_aloud_btn"
                )

                // 2. Microphone Button (Speech-to-Text - Large center centerpiece)
                VintageMicrophoneButton(
                    isListening = isListening,
                    onClick = onToggleListen
                )

                // 3. Zusammenfassung Button
                VintageActionButton(
                    label = "Zusammenf.",
                    icon = Icons.Default.Book,
                    isActive = false,
                    activeColor = GoldAccent,
                    onClick = onSummarizeChapter,
                    testTag = "chapter_summary_btn"
                )

                // 4. Telefon-Button (Gemini Live Modus - Vintage telephone handset)
                VintageActionButton(
                    label = "Gemini Live",
                    icon = Icons.Default.Call,
                    isActive = false,
                    activeColor = GoldAccent,
                    onClick = onOpenGeminiLive,
                    testTag = "gemini_live_btn"
                )
            }
        }
    }
}

/**
 * Large, ornate vintage microphone button with glowing embossed brass ring.
 */
@Composable
fun VintageMicrophoneButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(if (isListening) pulseScale else 1f)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isListening) {
                            listOf(WaxRed, LeatherDark)
                        } else {
                            listOf(LeatherWarm, LeatherDark)
                        }
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(GoldAccentLight, GoldAccentDark, GoldAccentLight)
                    ),
                    shape = CircleShape
                )
                .clickable(onClick = onClick)
                .testTag("microphone_main_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "Aufnahme stoppen" else "Diktieren starten",
                tint = if (isListening) ParchmentLight else GoldAccentLight,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isListening) "Zuhören…" else "Diktieren",
            fontFamily = CinzelFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isListening) GoldAccentLight else GoldAccent
        )
    }
}

/**
 * Vintage secondary action button (Telephone / Speaker)
 */
@Composable
fun VintageActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isActive) {
                            listOf(activeColor, LeatherDark)
                        } else {
                            listOf(LeatherWarm, LeatherDark)
                        }
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 1.5.dp,
                    color = GoldAccentDark,
                    shape = CircleShape
                )
                .clickable(onClick = onClick)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) ParchmentLight else GoldAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = CinzelFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) GoldAccentLight else ParchmentDark
        )
    }
}
