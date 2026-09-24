package com.example.domain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognitionHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialResult = MutableStateFlow("")
    val partialResult: StateFlow<String> = _partialResult.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private val _currentLanguageTag = MutableStateFlow("de-DE")
    val currentLanguageTag: StateFlow<String> = _currentLanguageTag.asStateFlow()

    fun setLanguage(languageTag: String) {
        _currentLanguageTag.value = languageTag
    }

    fun startListening(
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Spracherkennung ist auf diesem Gerät leider nicht verfügbar.")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _partialResult.value = ""
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {
                    _soundLevel.value = (rmsdB + 2f).coerceIn(0f, 10f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Keine Sprache erkannt"
                        SpeechRecognizer.ERROR_NETWORK -> "Netzwerkfehler"
                        SpeechRecognizer.ERROR_AUDIO -> "Audio-Aufnahmefehler"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon-Berechtigung fehlt"
                        else -> "Erkennungsfehler ($error)"
                    }
                    Log.w("SpeechHelper", "SpeechRecognizer error: $errorMsg ($error)")
                    onError(errorMsg)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val bestMatch = matches?.firstOrNull() ?: ""
                    if (bestMatch.isNotBlank()) {
                        onFinalResult(bestMatch)
                    }
                    _partialResult.value = ""
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    _partialResult.value = text
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, _currentLanguageTag.value)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, _currentLanguageTag.value)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e("SpeechHelper", "Error starting speech recognition", e)
            _isListening.value = false
            onError("Fehler beim Starten des Mikrofons: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w("SpeechHelper", "Error stopping speech recognizer", e)
        } finally {
            speechRecognizer = null
            _isListening.value = false
            _soundLevel.value = 0f
        }
    }
}
