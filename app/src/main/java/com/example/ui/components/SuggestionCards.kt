package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.TextImprovement
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.DancingScriptFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.InkChestnut
import com.example.ui.theme.InkSepiaDark
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.ParchmentDark
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.WaxRed

@Composable
fun SuggestionCard(
    suggestion: TextImprovement,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = GoldAccent.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("suggestion_card_${suggestion.id}"),
        shape = RoundedCornerShape(14.dp),
        color = ParchmentLight.copy(alpha = 0.97f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Category Badge + AI feather icon + Dismiss button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(GoldAccent.copy(alpha = 0.2f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccentDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = suggestion.category,
                                fontFamily = CinzelFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LeatherBrown
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("dismiss_suggestion_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Verwerfen",
                        tint = InkChestnut.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Explanation / reasoning
            if (suggestion.reasoning.isNotBlank()) {
                Text(
                    text = suggestion.reasoning,
                    fontSize = 12.sp,
                    color = InkChestnut,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Before / After snippet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ParchmentDark.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vorschlag:",
                        fontFamily = CinzelFontFamily,
                        fontSize = 10.sp,
                        color = GoldAccentDark
                    )
                    Text(
                        text = "„${suggestion.suggestedText}“",
                        fontFamily = DancingScriptFontFamily,
                        fontSize = 19.sp,
                        color = InkSepiaDark,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Übernehmen & Verwerfen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkChestnut),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Verwerfen", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onApply,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = LeatherBrown,
                        contentColor = GoldAccent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("apply_suggestion_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Übernehmen",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
