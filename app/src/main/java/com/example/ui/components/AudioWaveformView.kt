package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DancingScriptFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkSepiaDark
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium
import kotlin.math.sin

@Composable
fun AudioWaveformIndicator(
    isListening: Boolean,
    soundLevel: Float,
    partialText: String,
    languageTag: String,
    modifier: Modifier = Modifier
) {
    if (!isListening) return

    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = ParchmentMedium.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = GoldAccent.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("audio_waveform_box")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(LeatherBrown, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mikrofon aktiv",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Die Füllfeder lauscht… (${if (languageTag == "de-CH") "Schweizerdeutsch" else "Hochdeutsch"})",
                    fontFamily = DancingScriptFontFamily,
                    fontSize = 20.sp,
                    color = InkSepiaDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Liquid ink waveform canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                val width = size.width
                val height = size.height
                val midY = height / 2f
                val barCount = 32
                val spacing = width / barCount

                for (i in 0 until barCount) {
                    val x = i * spacing + spacing / 2
                    val waveFactor = sin(phase + i * 0.35f)
                    val baseAmp = (soundLevel * 2.5f + 4f).coerceIn(4f, midY - 2f)
                    val amp = (baseAmp * (0.5f + 0.5f * waveFactor)).coerceAtLeast(3f)

                    drawLine(
                        color = if (i % 2 == 0) LeatherDark else GoldAccent,
                        start = Offset(x, midY - amp),
                        end = Offset(x, midY + amp),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                }
            }

            if (partialText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "„$partialText“",
                    fontFamily = DancingScriptFontFamily,
                    fontSize = 18.sp,
                    color = LeatherBrown,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
