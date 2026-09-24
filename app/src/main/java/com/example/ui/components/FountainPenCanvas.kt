package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RuledLineSepia
import com.example.ui.theme.RuledMarginRed

/**
 * Draws realistic aged parchment paper effects:
 * - Subtle ruled lines that align with handwriting line heights
 * - Left margin rule in faint vintage red ink
 * - Soft dark vignette shadow at the outer edges
 * - Book spine shadow on the left binding
 */
@Composable
fun ParchmentBackgroundLines(
    modifier: Modifier = Modifier,
    lineSpacingDp: Float = 36f,
    topMarginDp: Float = 140f,
    leftMarginDp: Float = 48f,
    isDarkMode: Boolean = false
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val density = this.density

        val lineSpacing = lineSpacingDp * density
        val topMargin = topMarginDp * density
        val leftMargin = leftMarginDp * density

        // Draw Left Book Spine Shadow (3D depth of bound diary)
        val spineGradient = Brush.horizontalGradient(
            colors = listOf(
                Color(0x38150C07),
                Color(0x18150C07),
                Color.Transparent
            ),
            startX = 0f,
            endX = 32f * density
        )
        drawRect(brush = spineGradient, size = size.copy(width = 32f * density))

        // Draw outer page vignette shadow
        val outerVignette = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x12150C07),
                Color(0x28150C07)
            ),
            startX = width - 24f * density,
            endX = width
        )
        drawRect(
            brush = outerVignette,
            topLeft = Offset(width - 24f * density, 0f),
            size = size.copy(width = 24f * density)
        )

        // Draw faint vintage red margin line on the left
        val marginColor = if (isDarkMode) Color(0x30B54B4B) else RuledMarginRed
        drawLine(
            color = marginColor,
            start = Offset(leftMargin, topMargin - 20f),
            end = Offset(leftMargin, height - 20f),
            strokeWidth = 1.5f * density
        )

        // Draw ruled lines across the page
        val ruleColor = if (isDarkMode) Color(0x228C6848) else RuledLineSepia
        var currentY = topMargin

        while (currentY < height - 30f * density) {
            drawLine(
                color = ruleColor,
                start = Offset(leftMargin - 10f * density, currentY),
                end = Offset(width - 16f * density, currentY),
                strokeWidth = 1f * density
            )
            currentY += lineSpacing
        }
    }
}

/**
 * Animated Ink Flow Effect:
 * Simulates a shimmering gold/sepia fountain pen nib writing flow.
 */
@Composable
fun FountainPenInkGlow(
    modifier: Modifier = Modifier,
    isWriting: Boolean = false
) {
    if (!isWriting) return

    val infiniteTransition = rememberInfiniteTransition(label = "inkFlow")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val xPos = width * shimmerOffset
        val path = Path().apply {
            moveTo(xPos - 40f, height - 10f)
            quadraticTo(xPos, height - 30f, xPos + 40f, height - 10f)
        }

        drawPath(
            path = path,
            color = GoldAccent.copy(alpha = 0.35f),
            style = Stroke(width = 4f)
        )
    }
}
