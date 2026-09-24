package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DiaryEntity
import com.example.ui.components.AudioWaveformIndicator
import com.example.ui.components.MoodTimelineDialog
import com.example.ui.components.ParchmentPage
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.ReminderSettingsDialog
import com.example.ui.components.SuggestionCard
import com.example.ui.components.VintageBottomToolbar
import com.example.ui.components.VoiceSelectionDialog
import com.example.ui.theme.LeatherDark
import com.example.ui.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allPages by viewModel.allPages.collectAsStateWithLifecycle()
    val currentPageIndex by viewModel.currentPageIndex.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val currentText by viewModel.currentText.collectAsStateWithLifecycle()
    val activeSuggestions by viewModel.activeSuggestions.collectAsStateWithLifecycle()
    val isWritingAnimation by viewModel.isWritingAnimation.collectAsStateWithLifecycle()

    val pageToDisplay = currentPage ?: DiaryEntity(
        chapterTitle = "Kapitel 1: Meine Erinnerungen",
        pageTitle = "Erste Seite",
        content = currentText,
        pageNumber = 1
    )

    val isListening by viewModel.speechHelper.isListening.collectAsStateWithLifecycle()
    val soundLevel by viewModel.speechHelper.soundLevel.collectAsStateWithLifecycle()
    val partialSpeech by viewModel.speechHelper.partialResult.collectAsStateWithLifecycle()
    val languageTag by viewModel.speechHelper.currentLanguageTag.collectAsStateWithLifecycle()

    val isSpeaking by viewModel.ttsHelper.isSpeaking.collectAsStateWithLifecycle()
    val availableVoices by viewModel.ttsHelper.availableVoiceProfiles.collectAsStateWithLifecycle()
    val selectedVoiceId by viewModel.ttsHelper.selectedVoiceId.collectAsStateWithLifecycle()

    val showChapterDialog by viewModel.showChapterDialog.collectAsStateWithLifecycle()
    val showVoiceDialog by viewModel.showVoiceDialog.collectAsStateWithLifecycle()
    val showGeminiLiveDialog by viewModel.showGeminiLiveDialog.collectAsStateWithLifecycle()
    val showReminderDialog by viewModel.showReminderDialog.collectAsStateWithLifecycle()
    val showMoodDialog by viewModel.showMoodDialog.collectAsStateWithLifecycle()
    val showExportDialog by viewModel.showExportDialog.collectAsStateWithLifecycle()
    val showSummaryDialog by viewModel.showSummaryDialog.collectAsStateWithLifecycle()
    val summaryContent by viewModel.summaryContent.collectAsStateWithLifecycle()
    val isExportingPdf by viewModel.isExportingPdf.collectAsStateWithLifecycle()
    val exportStatusMessage by viewModel.exportStatusMessage.collectAsStateWithLifecycle()
    val pdfIntent by viewModel.pdfIntent.collectAsStateWithLifecycle()
    val reminderSettings by viewModel.reminderSettings.collectAsStateWithLifecycle()

    val liveChatMessages by viewModel.liveChatMessages.collectAsStateWithLifecycle()
    val isLoadingGeminiLive by viewModel.isLoadingGeminiLive.collectAsStateWithLifecycle()

    val exportMarkdown by viewModel.exportMarkdown.collectAsStateWithLifecycle()

    // Handle export share intent (Plain text / Markdown)
    LaunchedEffect(exportMarkdown) {
        exportMarkdown?.let { text ->
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_TITLE, "Mein Leben – Das Tagebuch (Export)")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Tagebuch teilen")
            context.startActivity(shareIntent)
            viewModel.clearExportMarkdown()
        }
    }

    // Handle PDF export intent (Share or View/Print)
    LaunchedEffect(pdfIntent) {
        pdfIntent?.let { intent ->
            try {
                if (intent.action == Intent.ACTION_SEND) {
                    val chooser = Intent.createChooser(intent, "Tagebuch als Pergament-PDF teilen")
                    context.startActivity(chooser)
                } else {
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                try {
                    val chooser = Intent.createChooser(intent, "Tagebuch-PDF öffnen")
                    context.startActivity(chooser)
                } catch (ex: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Keine passende App zum Öffnen von PDF-Dateien gefunden.")
                    }
                }
            }
            viewModel.clearPdfIntent()
        }
    }

    // Permission launcher for microphone
    val pendingMicAction = remember { androidx.compose.runtime.mutableStateOf<(() -> Unit)?>(null) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingMicAction.value?.invoke()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Mikrofon-Berechtigung ist für Spracheingabe erforderlich.")
            }
        }
        pendingMicAction.value = null
    }

    // Permission launcher for Android 13+ POST_NOTIFICATIONS
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                snackbarHostState.showSnackbar("Benachrichtigungen sind aktiv.")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Benachrichtigungen wurden deaktiviert. Du kannst sie in den Android-Einstellungen erlauben.")
            }
        }
    }

    // Request notification permission once if enabled and targeting Android 13+
    /*LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }*/

    fun executeWithMicPermission(action: () -> Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            action()
        } else {
            pendingMicAction.value = action
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = LeatherDark,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            VintageBottomToolbar(
                isListening = isListening,
                isSpeaking = isSpeaking,
                languageTag = languageTag,
                onToggleListen = {
                    executeWithMicPermission {
                        viewModel.toggleSpeechRecognition()
                    }
                },
                onOpenGeminiLive = {
                    executeWithMicPermission {
                        viewModel.openGeminiLive()
                    }
                },
                onToggleReadAloud = { viewModel.toggleReadAloud() },
                onOpenVoiceSettings = { viewModel.openVoiceDialog() },
                onToggleLanguage = { viewModel.toggleSpeechLanguage() },
                onSummarizeChapter = { viewModel.openSummaryDialog() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Parchment Page
            ParchmentPage(
                currentPage = pageToDisplay,
                totalPages = allPages.size.coerceAtLeast(1),
                content = currentText,
                onContentChange = { viewModel.onTextChanged(it) },
                onChapterClick = { viewModel.openChapterDialog() },
                onPreviousPage = { viewModel.previousPage() },
                onNextPage = { viewModel.nextPage() },
                onNewPage = { viewModel.addNewPage() },
                onDeletePage = { viewModel.deleteCurrentPage() },
                onExportMarkdown = { viewModel.openExportDialog() },
                onOpenReminder = {
                    // Check notification permission if on Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val perm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                        if (perm != PackageManager.PERMISSION_GRANTED) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    viewModel.openReminderDialog()
                },
                onOpenMoodTimeline = {
                    viewModel.openMoodDialog()
                },
                isWritingAnimation = isWritingAnimation
            )

            // Floating Speech Waveform Indicator (when dictating in diary)
            AnimatedVisibility(
                visible = isListening,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                AudioWaveformIndicator(
                    isListening = true,
                    soundLevel = soundLevel,
                    partialText = partialSpeech,
                    languageTag = languageTag
                )
            }

            // Always-Active AI Text Improvement Suggestions Floating Card
            activeSuggestions.firstOrNull()?.let { suggestion ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { -it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                ) {
                    SuggestionCard(
                        suggestion = suggestion,
                        onApply = { viewModel.applySuggestion(suggestion) },
                        onDismiss = { viewModel.dismissSuggestion(suggestion) }
                    )
                }
            }
        }
    }

    // --- Dialogs ---

    // 1. Gemini Live Biographer Mode
    if (showGeminiLiveDialog) {
        GeminiLiveDialog(
            chatMessages = liveChatMessages,
            isListening = isListening,
            isSpeaking = isSpeaking,
            isLoadingAi = isLoadingGeminiLive,
            soundLevel = soundLevel,
            partialSpeech = partialSpeech,
            languageTag = languageTag,
            onStartListening = {
                executeWithMicPermission {
                    viewModel.startLiveSpeech()
                }
            },
            onStopListening = { viewModel.stopLiveSpeech() },
            onSendManualText = { viewModel.handleLiveInput(it) },
            onApplyToDiary = { viewModel.applyLiveSuggestionToDiary(it) },
            onClose = { viewModel.closeGeminiLive() }
        )
    }

    // 2. Voice Selection Dialog (5 female voices)
    if (showVoiceDialog) {
        VoiceSelectionDialog(
            availableVoices = availableVoices,
            selectedVoiceId = selectedVoiceId,
            onSelectVoice = { viewModel.selectVoiceProfile(it) },
            onTestVoice = { viewModel.testVoiceProfile(it) },
            onDismiss = { viewModel.closeVoiceDialog() }
        )
    }

    // 3. Chapter List & Settings Dialog
    if (showChapterDialog) {
        val reminderTimeString = String.format(
            Locale.GERMAN,
            "%02d:%02d",
            reminderSettings.hour,
            reminderSettings.minute
        )
        ChapterListDialog(
            pages = allPages,
            currentPageId = currentPage?.id ?: 0L,
            onSelectPage = { viewModel.selectPage(it) },
            onAddChapter = { viewModel.addNewPage(it) },
            onSetInkStyle = { viewModel.setInkStyle(it) },
            currentInkStyle = currentPage?.inkStyle ?: "Sepia",
            hasApiKey = viewModel.hasValidGeminiKey(),
            onOpenReminderSettings = {
                viewModel.closeChapterDialog()
                viewModel.openReminderDialog()
            },
            onOpenMoodTimeline = {
                viewModel.closeChapterDialog()
                viewModel.openMoodDialog()
            },
            onOpenExportDialog = {
                viewModel.closeChapterDialog()
                viewModel.openExportDialog()
            },
            reminderTimeString = reminderTimeString,
            isReminderEnabled = reminderSettings.isEnabled,
            onDismiss = { viewModel.closeChapterDialog() }
        )
    }

    // 4. Daily Reminder Settings Dialog
    if (showReminderDialog) {
        ReminderSettingsDialog(
            currentSettings = reminderSettings,
            onSaveSettings = { enabled, hour, minute ->
                viewModel.saveReminderSettings(enabled, hour, minute)
                scope.launch {
                    if (enabled) {
                        val timeStr = String.format(Locale.GERMAN, "%02d:%02d", hour, minute)
                        snackbarHostState.showSnackbar("Tägliche Erinnerung für $timeStr Uhr eingerichtet.")
                    } else {
                        snackbarHostState.showSnackbar("Tägliche Erinnerung deaktiviert.")
                    }
                }
            },
            onTestNotification = {
                // Ensure notification permission is requested before firing test
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val perm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (perm != PackageManager.PERMISSION_GRANTED) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                viewModel.triggerTestNotification()
                scope.launch {
                    snackbarHostState.showSnackbar("Probe-Benachrichtigung gesendet!")
                }
            },
            onDismiss = { viewModel.closeReminderDialog() }
        )
    }

    // 5. Emotional Mood Timeline & Seelen-Chronik Dialog
    if (showMoodDialog) {
        MoodTimelineDialog(
            pages = allPages,
            currentPageId = currentPage?.id ?: 0L,
            hasApiKey = viewModel.hasValidGeminiKey(),
            onSelectPage = { viewModel.selectPage(it) },
            onReanalyzeCurrentPage = {
                viewModel.reanalyzeCurrentPage()
                scope.launch {
                    snackbarHostState.showSnackbar("Stimmung wird neu analysiert…")
                }
            },
            onDismiss = { viewModel.closeMoodDialog() }
        )
    }

    // 6. Pergament PDF & Text Export Dialog
    if (showExportDialog) {
        PdfExportDialog(
            currentPage = pageToDisplay,
            totalPagesCount = allPages.size.coerceAtLeast(1),
            isExporting = isExportingPdf,
            exportStatusMessage = exportStatusMessage,
            onExportPdf = { scope, action ->
                viewModel.exportPdf(scope, action)
            },
            onExportMarkdown = {
                viewModel.closeExportDialog()
                viewModel.exportToMarkdown()
            },
            onDismiss = { viewModel.closeExportDialog() }
        )
    }

    // 7. Chapter Summary Dialog
    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeSummaryDialog() },
            title = { Text(text = "Zusammenfassung der Lebensgeschichte") },
            text = { Text(text = summaryContent) },
            confirmButton = {
                TextButton(onClick = { viewModel.closeSummaryDialog() }) {
                    Text("Schließen")
                }
            }
        )
    }
}
