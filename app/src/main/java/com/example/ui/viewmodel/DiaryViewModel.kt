package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.DiaryEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.TextImprovement
import com.example.data.repository.DiaryRepository
import com.example.domain.FemaleVoiceProfile
import com.example.domain.SpeechRecognitionHelper
import com.example.domain.TextToSpeechHelper
import com.example.reminder.DiaryReminderScheduler
import com.example.reminder.ReminderPreferences
import com.example.reminder.ReminderReceiver
import com.example.reminder.ReminderSettings
import com.example.ui.components.ExportAction
import com.example.ui.components.ExportScope
import com.example.ui.screens.LiveChatMessage
import com.example.util.PdfExporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DiaryRepository
    private val geminiClient = GeminiClient()
    val speechHelper = SpeechRecognitionHelper(application)
    val ttsHelper = TextToSpeechHelper(application)
    private val reminderPreferences = ReminderPreferences(application)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DiaryRepository(db.diaryDao())
    }

    val allPages: StateFlow<List<DiaryEntity>> = repository.allPages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminderSettings: StateFlow<ReminderSettings> = reminderPreferences.settings

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    private val _currentText = MutableStateFlow("")
    val currentText: StateFlow<String> = _currentText.asStateFlow()

    private val _activeSuggestions = MutableStateFlow<List<TextImprovement>>(emptyList())
    val activeSuggestions: StateFlow<List<TextImprovement>> = _activeSuggestions.asStateFlow()

    private val _isAnalyzingText = MutableStateFlow(false)
    val isAnalyzingText: StateFlow<Boolean> = _isAnalyzingText.asStateFlow()

    private val _isWritingAnimation = MutableStateFlow(false)
    val isWritingAnimation: StateFlow<Boolean> = _isWritingAnimation.asStateFlow()

    private val _showChapterDialog = MutableStateFlow(false)
    val showChapterDialog: StateFlow<Boolean> = _showChapterDialog.asStateFlow()

    private val _showVoiceDialog = MutableStateFlow(false)
    val showVoiceDialog: StateFlow<Boolean> = _showVoiceDialog.asStateFlow()

    private val _showGeminiLiveDialog = MutableStateFlow(false)
    val showGeminiLiveDialog: StateFlow<Boolean> = _showGeminiLiveDialog.asStateFlow()

    private val _showReminderDialog = MutableStateFlow(false)
    val showReminderDialog: StateFlow<Boolean> = _showReminderDialog.asStateFlow()

    private val _showMoodDialog = MutableStateFlow(false)
    val showMoodDialog: StateFlow<Boolean> = _showMoodDialog.asStateFlow()

    private val _liveChatMessages = MutableStateFlow<List<LiveChatMessage>>(emptyList())
    val liveChatMessages: StateFlow<List<LiveChatMessage>> = _liveChatMessages.asStateFlow()

    private val _isLoadingGeminiLive = MutableStateFlow(false)
    val isLoadingGeminiLive: StateFlow<Boolean> = _isLoadingGeminiLive.asStateFlow()

    private val _exportMarkdown = MutableStateFlow<String?>(null)
    val exportMarkdown: StateFlow<String?> = _exportMarkdown.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showSummaryDialog = MutableStateFlow(false)
    val showSummaryDialog: StateFlow<Boolean> = _showSummaryDialog.asStateFlow()

    private val _summaryContent = MutableStateFlow("")
    val summaryContent: StateFlow<String> = _summaryContent.asStateFlow()

    fun openSummaryDialog() {
        _showSummaryDialog.value = true
        summarizeChapters()
    }

    fun closeSummaryDialog() {
        _showSummaryDialog.value = false
        _summaryContent.value = ""
    }

    private fun summarizeChapters() {
        _summaryContent.value = "Biografie wird zusammengefasst..."
        viewModelScope.launch {
            val fullContent = allPages.value.joinToString("\n\n") { "Kapitel: ${it.chapterTitle}\n${it.content}" }
            
            val result = geminiClient.chatWithBiographer(
                conversationHistory = emptyList(),
                userInput = "Fasse die gesamte Biografie zusammen und formuliere sie als eine zusammenhängende Lebensgeschichte.",
                diaryContext = fullContent.take(10000) // Gemini limit safety
            )

            result.onSuccess { (summary, _) ->
                _summaryContent.value = summary
            }.onFailure {
                _summaryContent.value = "Zusammenfassung konnte nicht erstellt werden."
            }
        }
    }

    private val _isExportingPdf = MutableStateFlow(false)
    val isExportingPdf: StateFlow<Boolean> = _isExportingPdf.asStateFlow()

    private val _exportStatusMessage = MutableStateFlow("")
    val exportStatusMessage: StateFlow<String> = _exportStatusMessage.asStateFlow()

    private val _pdfIntent = MutableStateFlow<Intent?>(null)
    val pdfIntent: StateFlow<Intent?> = _pdfIntent.asStateFlow()

    private var autoSaveJob: Job? = null
    private var aiAnalysisJob: Job? = null
    private var lastAnalyzedText = ""
    private var moodAnalysisJob: Job? = null
    private var lastMoodAnalyzedText = ""

    init {
        // Observe pages and set initial page
        viewModelScope.launch {
            allPages.collect { pages ->
                if (pages.isNotEmpty()) {
                    val idx = _currentPageIndex.value.coerceIn(0, pages.size - 1)
                    val page = pages[idx]
                    if (_currentText.value.isEmpty() && page.content.isNotEmpty()) {
                        _currentText.value = page.content
                    }
                }
            }
        }

        // Schedule reminder alarm on startup according to current preferences
        val currentReminder = reminderPreferences.getSettings()
        if (currentReminder.isEnabled) {
            DiaryReminderScheduler.scheduleReminder(
                application,
                currentReminder.hour,
                currentReminder.minute
            )
        }

        // Welcome message for Gemini Live
        _liveChatMessages.value = listOf(
            LiveChatMessage(
                isUser = false,
                text = "Grüezi und Willkommen. Ich begleite dich beim Festhalten deiner Lebensgeschichte. Erzähle mir einfach frei auf Schweizerdeutsch oder Hochdeutsch – ich helfe dir, deine Erinnerungen in berührende Worte zu fassen.",
                diarySuggestion = null
            )
        )
    }

    val currentPage: StateFlow<DiaryEntity?> = combine(allPages, currentPageIndex) { pages, index ->
        pages.getOrNull(index) ?: pages.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onTextChanged(newText: String) {
        _currentText.value = newText
        triggerWritingAnimation()
        scheduleAutoSave(newText)
        scheduleAiAnalysis(newText)
        scheduleMoodAnalysis(newText)
    }

    private fun triggerWritingAnimation() {
        _isWritingAnimation.value = true
        viewModelScope.launch {
            delay(1500)
            _isWritingAnimation.value = false
        }
    }

    private fun scheduleAutoSave(text: String) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1000)
            val current = currentPage.value
            if (current != null) {
                repository.savePage(current.copy(content = text))
            } else if (text.isNotBlank()) {
                // If database had no pages yet, create first real page with current content
                val newId = repository.createNewPage("Kapitel 1: Meine Erinnerungen")
                val updated = repository.getPageById(newId)
            }
        }
    }

    private fun scheduleAiAnalysis(text: String) {
        if (text == lastAnalyzedText || text.length < 20) return
        aiAnalysisJob?.cancel()
        aiAnalysisJob = viewModelScope.launch {
            delay(3500) // Debounce so user can finish sentence
            _isAnalyzingText.value = true
            lastAnalyzedText = text
            val result = geminiClient.analyzeAndSuggestImprovements(text)
            result.onSuccess { suggestions ->
                _activeSuggestions.value = suggestions
            }
            _isAnalyzingText.value = false
        }
    }

    private fun scheduleMoodAnalysis(text: String) {
        if (text == lastMoodAnalyzedText || text.trim().length < 8) return
        moodAnalysisJob?.cancel()
        moodAnalysisJob = viewModelScope.launch {
            delay(2200) // Debounce for natural typing flow
            lastMoodAnalyzedText = text
            val result = geminiClient.analyzeMood(text)
            result.onSuccess { moodResult ->
                val current = currentPage.value
                if (current != null) {
                    val updated = current.copy(
                        mood = moodResult.mood,
                        moodScore = moodResult.score,
                        moodKeywords = moodResult.keywords.joinToString(", ")
                    )
                    repository.savePage(updated)
                }
            }
        }
    }

    fun reanalyzeCurrentPage() {
        val current = currentPage.value ?: return
        val text = _currentText.value.ifBlank { current.content }
        viewModelScope.launch {
            val result = geminiClient.analyzeMood(text)
            result.onSuccess { moodResult ->
                val updated = current.copy(
                    content = text,
                    mood = moodResult.mood,
                    moodScore = moodResult.score,
                    moodKeywords = moodResult.keywords.joinToString(", ")
                )
                repository.savePage(updated)
            }
        }
    }

    fun applySuggestion(suggestion: TextImprovement) {
        val current = _currentText.value
        val updated = if (current.contains(suggestion.originalText)) {
            current.replaceFirst(suggestion.originalText, suggestion.suggestedText)
        } else {
            current + "\n" + suggestion.suggestedText
        }
        _currentText.value = updated
        _activeSuggestions.value = _activeSuggestions.value.filter { it.id != suggestion.id }
        triggerWritingAnimation()
        scheduleAutoSave(updated)
        scheduleMoodAnalysis(updated)
    }

    fun dismissSuggestion(suggestion: TextImprovement) {
        _activeSuggestions.value = _activeSuggestions.value.filter { it.id != suggestion.id }
    }

    // --- Speech Recognition ---
    fun toggleSpeechRecognition() {
        if (speechHelper.isListening.value) {
            speechHelper.stopListening()
        } else {
            speechHelper.startListening(
                onFinalResult = { text ->
                    val updated = if (_currentText.value.isBlank()) {
                        text
                    } else {
                        "${_currentText.value.trimEnd()} $text"
                    }
                    _currentText.value = updated
                    triggerWritingAnimation()
                    scheduleAutoSave(updated)
                    scheduleAiAnalysis(updated)
                    scheduleMoodAnalysis(updated)
                },
                onError = { /* Logged in helper */ }
            )
        }
    }

    fun toggleSpeechLanguage() {
        val current = speechHelper.currentLanguageTag.value
        val next = if (current == "de-DE") "de-CH" else "de-DE"
        speechHelper.setLanguage(next)
    }

    // --- Text to Speech (Vorlesen) ---
    fun toggleReadAloud() {
        if (ttsHelper.isSpeaking.value) {
            ttsHelper.stop()
        } else {
            val textToRead = _currentText.value.ifBlank {
                currentPage.value?.content ?: "Dieses Tagebuchblatt ist noch leer."
            }
            ttsHelper.speak(textToRead)
        }
    }

    fun selectVoiceProfile(voiceId: String) {
        ttsHelper.selectVoice(voiceId)
    }

    fun testVoiceProfile(profile: FemaleVoiceProfile) {
        ttsHelper.selectVoice(profile.id)
        ttsHelper.speak("Guten Tag. Dies ist eine Kostprobe meiner Vorlesestimme für dein Tagebuch.")
    }

    // --- Gemini Live Dialog ---
    fun openGeminiLive() {
        _showGeminiLiveDialog.value = true
    }

    fun closeGeminiLive() {
        speechHelper.stopListening()
        ttsHelper.stop()
        _showGeminiLiveDialog.value = false
    }

    fun startLiveSpeech() {
        speechHelper.startListening(
            onFinalResult = { userSpokenText ->
                handleLiveInput(userSpokenText)
            }
        )
    }

    fun stopLiveSpeech() {
        speechHelper.stopListening()
    }

    fun handleLiveInput(userInput: String) {
        if (userInput.isBlank()) return

        val currentChat = _liveChatMessages.value.toMutableList()
        currentChat.add(LiveChatMessage(isUser = true, text = userInput))
        _liveChatMessages.value = currentChat

        _isLoadingGeminiLive.value = true

        val history = currentChat.dropLast(1).chunked(2).mapNotNull {
            if (it.size == 2) Pair(it[0].text, it[1].text) else null
        }

        viewModelScope.launch {
            val result = geminiClient.chatWithBiographer(
                conversationHistory = history,
                userInput = userInput,
                diaryContext = _currentText.value.takeLast(600)
            )

            _isLoadingGeminiLive.value = false

            result.onSuccess { (speechReply, diarySuggestion) ->
                val updatedChat = _liveChatMessages.value.toMutableList()
                updatedChat.add(
                    LiveChatMessage(
                        isUser = false,
                        text = speechReply,
                        diarySuggestion = diarySuggestion
                    )
                )
                _liveChatMessages.value = updatedChat

                ttsHelper.speak(speechReply)
            }.onFailure { error ->
                val updatedChat = _liveChatMessages.value.toMutableList()
                updatedChat.add(
                    LiveChatMessage(
                        isUser = false,
                        text = "Entschuldige, ich konnte deine Worte gerade nicht verarbeiten: ${error.localizedMessage ?: "Verbindung prüfen"}. Bitte überprüfe ggf. deinen GEMINI_API_KEY.",
                        diarySuggestion = null
                    )
                )
                _liveChatMessages.value = updatedChat
            }
        }
    }

    fun applyLiveSuggestionToDiary(textToApply: String) {
        val updated = if (_currentText.value.isBlank()) {
            textToApply
        } else {
            "${_currentText.value.trimEnd()}\n\n$textToApply"
        }
        _currentText.value = updated
        triggerWritingAnimation()
        scheduleAutoSave(updated)
        scheduleMoodAnalysis(updated)
        closeGeminiLive()
    }

    // --- Page & Chapter Management ---
    fun selectPage(page: DiaryEntity) {
        val idx = allPages.value.indexOfFirst { it.id == page.id }
        if (idx >= 0) {
            _currentPageIndex.value = idx
            _currentText.value = page.content
            _activeSuggestions.value = emptyList()
        }
    }

    fun previousPage() {
        if (_currentPageIndex.value > 0) {
            _currentPageIndex.value -= 1
            allPages.value.getOrNull(_currentPageIndex.value)?.let {
                _currentText.value = it.content
                _activeSuggestions.value = emptyList()
            }
        }
    }

    fun nextPage() {
        if (_currentPageIndex.value < allPages.value.size - 1) {
            _currentPageIndex.value += 1
            allPages.value.getOrNull(_currentPageIndex.value)?.let {
                _currentText.value = it.content
                _activeSuggestions.value = emptyList()
            }
        }
    }

    fun addNewPage(chapterTitle: String = "Neues Kapitel") {
        viewModelScope.launch {
            val newId = repository.createNewPage(chapterTitle)
            delay(200)
            val updatedPages = allPages.value
            val idx = updatedPages.indexOfFirst { it.id == newId }
            if (idx >= 0) {
                _currentPageIndex.value = idx
                _currentText.value = ""
                _activeSuggestions.value = emptyList()
            }
        }
    }

    fun deleteCurrentPage() {
        val page = currentPage.value ?: return
        if (allPages.value.size <= 1) return
        viewModelScope.launch {
            repository.deletePage(page)
            if (_currentPageIndex.value > 0) {
                _currentPageIndex.value -= 1
            }
            allPages.value.getOrNull(_currentPageIndex.value)?.let {
                _currentText.value = it.content
            }
        }
    }

    fun setInkStyle(style: String) {
        currentPage.value?.let { page ->
            viewModelScope.launch {
                repository.savePage(page.copy(inkStyle = style))
            }
        }
    }

    // --- Daily Reminder Settings ---
    fun openReminderDialog() {
        _showReminderDialog.value = true
    }

    fun closeReminderDialog() {
        _showReminderDialog.value = false
    }

    // --- Mood & Emotional Timeline Dialog ---
    fun openMoodDialog() {
        _showMoodDialog.value = true
    }

    fun closeMoodDialog() {
        _showMoodDialog.value = false
    }

    fun saveReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        val app = getApplication<Application>()
        reminderPreferences.saveSettings(enabled, hour, minute)
        if (enabled) {
            DiaryReminderScheduler.scheduleReminder(app, hour, minute)
        } else {
            DiaryReminderScheduler.cancelReminder(app)
        }
    }

    fun triggerTestNotification() {
        val app = getApplication<Application>()
        val intent = Intent(app, ReminderReceiver::class.java).apply {
            action = "com.example.reminder.TRIGGER_TEST"
        }
        app.sendBroadcast(intent)
    }

    fun openChapterDialog() {
        _showChapterDialog.value = true
    }

    fun closeChapterDialog() {
        _showChapterDialog.value = false
    }

    fun openVoiceDialog() {
        _showVoiceDialog.value = true
    }

    fun closeVoiceDialog() {
        _showVoiceDialog.value = false
    }

    fun exportToMarkdown(): String {
        val markdown = repository.exportToMarkdown(allPages.value)
        _exportMarkdown.value = markdown
        return markdown
    }

    fun clearExportMarkdown() {
        _exportMarkdown.value = null
    }

    fun openExportDialog() {
        _showExportDialog.value = true
    }

    fun closeExportDialog() {
        if (!_isExportingPdf.value) {
            _showExportDialog.value = false
        }
    }

    fun clearPdfIntent() {
        _pdfIntent.value = null
    }

    fun exportPdf(scope: ExportScope, action: ExportAction) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            _isExportingPdf.value = true
            _exportStatusMessage.value = when (scope) {
                ExportScope.CURRENT_PAGE -> "Aktuelles Blatt wird auf Pergament gerendert…"
                ExportScope.ALL_PAGES -> "Tagebuch-Buch mit Titelblatt wird gebunden…"
                ExportScope.MARKDOWN_TEXT -> "Text wird formatiert…"
            }

            try {
                // Ensure current edited text is saved before exporting
                val current = currentPage.value
                if (current != null && current.content != _currentText.value) {
                    val updatedPage = current.copy(content = _currentText.value)
                    repository.savePage(updatedPage)
                }

                val exportResult = when (scope) {
                    ExportScope.CURRENT_PAGE -> {
                        val pageToExport = currentPage.value?.copy(content = _currentText.value)
                            ?: DiaryEntity(
                                chapterTitle = "Kapitel 1: Meine Erinnerungen",
                                pageTitle = "Erste Seite",
                                content = _currentText.value,
                                pageNumber = 1
                            )
                        PdfExporter.exportSinglePagePdf(app, pageToExport)
                    }
                    ExportScope.ALL_PAGES -> {
                        val pages = allPages.value.map { p ->
                            if (p.id == currentPage.value?.id) p.copy(content = _currentText.value) else p
                        }
                        PdfExporter.exportFullDiaryPdf(app, pages)
                    }
                    ExportScope.MARKDOWN_TEXT -> null
                }

                if (exportResult != null) {
                    val title = if (scope == ExportScope.ALL_PAGES) {
                        "Mein Leben – Gesamtes Tagebuch (PDF)"
                    } else {
                        "Mein Leben – Tagebucheintrag (PDF)"
                    }
                    val intent = when (action) {
                        ExportAction.SHARE -> PdfExporter.createShareIntent(exportResult.uri, title)
                        ExportAction.VIEW_PRINT -> PdfExporter.createViewIntent(exportResult.uri)
                    }
                    _pdfIntent.value = intent
                    _showExportDialog.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isExportingPdf.value = false
                _exportStatusMessage.value = ""
            }
        }
    }

    fun hasValidGeminiKey(): Boolean = geminiClient.hasValidApiKey()

    override fun onCleared() {
        super.onCleared()
        speechHelper.stopListening()
        ttsHelper.shutdown()
    }
}
