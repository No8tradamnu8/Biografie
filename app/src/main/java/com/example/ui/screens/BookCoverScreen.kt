package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.LeatherBrown
import com.example.ui.theme.LeatherDark
import com.example.ui.theme.LeatherWarm
import com.example.ui.theme.WaxRed
import com.example.ui.theme.WaxRedLight
import kotlinx.coroutines.delay

@Composable
fun BookCoverScreen(onUnlock: () -> Unit) {
    var sequence by remember { mutableStateOf("") }
    // Sequence: Triangle(2), Circle(1), X(3), Square(4) -> "2134"
    val correctSequence = "2134"
    var isUnlocking by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val hardwareFocusRequester = remember { FocusRequester() }
    val softKeyboardFocusRequester = remember { FocusRequester() }

    // Request keyboard focus immediately on launch
    LaunchedEffect(Unit) {
        hardwareFocusRequester.requestFocus()
    }

    fun handleDigitInput(digit: String) {
        if (sequence.length < 4 && !isUnlocking) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            sequence += digit
        }
    }

    // Sequence validation logic
    LaunchedEffect(sequence) {
        if (sequence.length == 4) {
            if (sequence == correctSequence) {
                isError = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isUnlocking = true
            } else {
                isError = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(600)
                sequence = ""
                isError = false
            }
        }
    }

    LaunchedEffect(isUnlocking) {
        if (isUnlocking) {
            delay(1500) // Duration of opening animation
            onUnlock()
        }
    }

    AnimatedContent(
        targetState = isUnlocking,
        transitionSpec = {
            if (targetState) {
                fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(1500))
            } else {
                fadeIn() togetherWith fadeOut()
            }
        },
        label = "book_transition"
    ) { unlocking ->
        if (!unlocking) {
            // Locked Cover State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFD2B48C))
                    .focusRequester(hardwareFocusRequester)
                    .focusable()
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            val digit = when (keyEvent.key) {
                                Key.One, Key.NumPad1 -> "1"
                                Key.Two, Key.NumPad2 -> "2"
                                Key.Three, Key.NumPad3 -> "3"
                                Key.Four, Key.NumPad4 -> "4"
                                Key.Five, Key.NumPad5 -> "5"
                                Key.Six, Key.NumPad6 -> "6"
                                Key.Seven, Key.NumPad7 -> "7"
                                Key.Eight, Key.NumPad8 -> "8"
                                Key.Nine, Key.NumPad9 -> "9"
                                Key.Zero, Key.NumPad0 -> "0"
                                Key.Backspace -> {
                                    if (sequence.isNotEmpty()) {
                                        sequence = sequence.dropLast(1)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    return@onKeyEvent true
                                }
                                else -> {
                                    val unicode = keyEvent.nativeKeyEvent.unicodeChar
                                    if (unicode in '0'.code..'9'.code) {
                                        unicode.toChar().toString()
                                    } else {
                                        null
                                    }
                                }
                            }
                            if (digit != null) {
                                handleDigitInput(digit)
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Book Cover Background Artwork
                Image(
                    painter = painterResource(id = R.drawable.book_cover),
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            hardwareFocusRequester.requestFocus()
                        },
                    contentScale = ContentScale.Crop
                )

                // Invisible Touch Layers directly over the silver 3D buttons
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    // Circle (O) -> 1 (Top Left)
                    InvisibleButton(DpOffset((-80).dp, (-80).dp)) {
                        hardwareFocusRequester.requestFocus()
                        handleDigitInput("1")
                    }
                    // Triangle (Δ) -> 2 (Top Right)
                    InvisibleButton(DpOffset(80.dp, (-80).dp)) {
                        hardwareFocusRequester.requestFocus()
                        handleDigitInput("2")
                    }
                    // X (X) -> 3 (Bottom Left)
                    InvisibleButton(DpOffset((-80).dp, 80.dp)) {
                        hardwareFocusRequester.requestFocus()
                        handleDigitInput("3")
                    }
                    // Square (□) -> 4 (Bottom Right)
                    InvisibleButton(DpOffset(80.dp, 80.dp)) {
                        hardwareFocusRequester.requestFocus()
                        handleDigitInput("4")
                    }
                }

                // Hidden text field for soft keyboard input
                BasicTextField(
                    value = sequence,
                    onValueChange = { newVal ->
                        val digits = newVal.filter { it.isDigit() }.take(4)
                        if (digits != sequence) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            sequence = digits
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .size(1.dp)
                        .alpha(0f)
                        .focusRequester(softKeyboardFocusRequester)
                )

                // Bottom Combination Status Bar & On-Screen Zahlentasten Toggle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp)
                ) {
                    // Lock Indicator & Zahlentasten Button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.62f))
                            .border(
                                1.2.dp,
                                if (isError) WaxRedLight else GoldAccentDark.copy(alpha = 0.8f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                showKeypad = !showKeypad
                                hardwareFocusRequester.requestFocus()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isUnlocking) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUnlocking) GoldAccentLight else if (isError) WaxRed else GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )

                        // 4 Combination Rivets / Dots
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(4) { index ->
                                val isFilled = index < sequence.length
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isError -> WaxRed
                                                isUnlocking -> GoldAccentLight
                                                isFilled -> GoldAccent
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            1.2.dp,
                                            if (isError) WaxRedLight else GoldAccentDark,
                                            CircleShape
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = null,
                            tint = GoldAccentLight,
                            modifier = Modifier.size(15.dp)
                        )

                        Text(
                            text = if (showKeypad) "Schließen" else "Zahlentasten (2134)",
                            fontSize = 11.sp,
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldAccentLight
                        )
                    }

                    // Expandable Vintage Number Keypad
                    AnimatedVisibility(
                        visible = showKeypad,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        VintageNumpad(
                            onDigitClick = { digit ->
                                handleDigitInput(digit)
                                hardwareFocusRequester.requestFocus()
                            },
                            onBackspaceClick = {
                                if (sequence.isNotEmpty()) {
                                    sequence = sequence.dropLast(1)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                hardwareFocusRequester.requestFocus()
                            },
                            onCloseClick = { showKeypad = false },
                            onOpenSoftKeyboard = {
                                softKeyboardFocusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        )
                    }
                }
            }
        } else {
            // "Opening" State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFD2B48C)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Das Buch öffnet sich...",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun VintageNumpad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onCloseClick: () -> Unit,
    onOpenSoftKeyboard: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = LeatherDark.copy(alpha = 0.94f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(GoldAccentLight, GoldAccentDark, LeatherWarm)
            ),
            width = 1.5.dp
        ),
        modifier = Modifier
            .padding(top = 10.dp)
            .shadow(16.dp, RoundedCornerShape(22.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header with title and soft-keyboard / close triggers
            Row(
                modifier = Modifier.width(220.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zahlenschloss",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GoldAccentLight
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSoftKeyboard,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "System-Tastatur",
                            tint = GoldAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✕",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clickable { onCloseClick() }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Row: 1, 2, 3, 4 (Highlighted combination keys)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("1", "2", "3", "4").forEach { digit ->
                    VintageKeypadButton(
                        text = digit,
                        isKeypadPrimary = true,
                        onClick = { onDigitClick(digit) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Row: 5, 6, 7, 8
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("5", "6", "7", "8").forEach { digit ->
                    VintageKeypadButton(
                        text = digit,
                        isKeypadPrimary = false,
                        onClick = { onDigitClick(digit) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Third Row: 9, 0, Backspace
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VintageKeypadButton(
                    text = "9",
                    isKeypadPrimary = false,
                    onClick = { onDigitClick("9") }
                )
                VintageKeypadButton(
                    text = "0",
                    isKeypadPrimary = false,
                    onClick = { onDigitClick("0") }
                )
                VintageKeypadIconButton(
                    icon = Icons.Default.Backspace,
                    onClick = onBackspaceClick
                )
            }
        }
    }
}

@Composable
fun VintageKeypadButton(
    text: String,
    isKeypadPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "keypad_btn_scale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(46.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = if (isKeypadPrimary) {
                        listOf(LeatherBrown, LeatherDark)
                    } else {
                        listOf(LeatherDark, Color(0xFF120905))
                    }
                )
            )
            .border(
                width = if (isKeypadPrimary) 1.5.dp else 1.dp,
                brush = Brush.verticalGradient(
                    if (isKeypadPrimary) {
                        listOf(GoldAccentLight, GoldAccentDark)
                    } else {
                        listOf(GoldAccentDark.copy(alpha = 0.7f), Color.Transparent)
                    }
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            fontFamily = CinzelFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = if (isKeypadPrimary) GoldAccentLight else GoldAccent.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun VintageKeypadIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "keypad_icon_scale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(46.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(LeatherDark)
            .border(1.dp, GoldAccentDark.copy(alpha = 0.5f), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Löschen",
            tint = GoldAccentLight,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun BoxScope.InvisibleButton(
    offset: DpOffset,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(offset.x, offset.y)
            .size(100.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No ripple
                onClick = onClick
            )
    )
}
