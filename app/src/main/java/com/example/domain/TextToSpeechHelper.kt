package com.example.domain

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class FemaleVoiceProfile(
    val id: String,
    val name: String,
    val description: String,
    val pitch: Float,
    val speechRate: Float,
    val locale: Locale,
    val systemVoiceName: String? = null
)

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _availableVoiceProfiles = MutableStateFlow<List<FemaleVoiceProfile>>(emptyList())
    val availableVoiceProfiles: StateFlow<List<FemaleVoiceProfile>> = _availableVoiceProfiles.asStateFlow()

    private val _selectedVoiceId = MutableStateFlow("clara")
    val selectedVoiceId: StateFlow<String> = _selectedVoiceId.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupVoiceProfiles()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e("TTSHelper", "TextToSpeech init failed with status: $status")
        }
    }

    private fun setupVoiceProfiles() {
        val germanLocale = Locale.GERMAN
        val swissLocale = Locale("de", "CH")

        val systemVoices = try {
            tts?.voices?.filter {
                it.locale.language == "de" && !it.isNetworkConnectionRequired
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList<Voice>()
        }

        val femaleSystemVoices = systemVoices.filter {
            it.name.contains("female", ignoreCase = true) ||
            it.name.contains("fem", ignoreCase = true) ||
            it.name.contains("f0", ignoreCase = true) ||
            it.name.contains("de-de-x-sfg", ignoreCase = true)
        }

        val baseProfiles = listOf(
            FemaleVoiceProfile(
                id = "clara",
                name = "Clara",
                description = "Warm, sanft & einfühlsam – Wie eine alte Vertraute",
                pitch = 1.0f,
                speechRate = 0.92f,
                locale = germanLocale,
                systemVoiceName = femaleSystemVoices.getOrNull(0)?.name
            ),
            FemaleVoiceProfile(
                id = "sophie",
                name = "Sophie",
                description = "Melodisch, heiter & lebendig – Voller Lebensfreude",
                pitch = 1.18f,
                speechRate = 0.98f,
                locale = germanLocale,
                systemVoiceName = femaleSystemVoices.getOrNull(1)?.name
            ),
            FemaleVoiceProfile(
                id = "eleonora",
                name = "Eleonora",
                description = "Klassisch, tief & literarisch – Bedachtsame Erzählung",
                pitch = 0.88f,
                speechRate = 0.85f,
                locale = germanLocale,
                systemVoiceName = femaleSystemVoices.getOrNull(2)?.name
            ),
            FemaleVoiceProfile(
                id = "elena",
                name = "Elena",
                description = "Klar, modern & natürlich – Angenehmer Lesefluss",
                pitch = 1.05f,
                speechRate = 1.02f,
                locale = germanLocale,
                systemVoiceName = femaleSystemVoices.getOrNull(3)?.name
            ),
            FemaleVoiceProfile(
                id = "heidi",
                name = "Heidi (CH)",
                description = "Sanftmut & Schweizer Klangfarbe – Heimelig und geborgen",
                pitch = 1.12f,
                speechRate = 0.92f,
                locale = swissLocale,
                systemVoiceName = systemVoices.firstOrNull { it.locale == swissLocale }?.name
            )
        )

        _availableVoiceProfiles.value = baseProfiles
    }

    fun selectVoice(profileId: String) {
        _selectedVoiceId.value = profileId
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized || tts == null) return

        val profile = _availableVoiceProfiles.value.firstOrNull { it.id == _selectedVoiceId.value }
            ?: _availableVoiceProfiles.value.firstOrNull()

        profile?.let {
            tts?.language = it.locale
            tts?.setPitch(it.pitch)
            tts?.setSpeechRate(it.speechRate)

            if (it.systemVoiceName != null) {
                try {
                    val matchingVoice = tts?.voices?.firstOrNull { v -> v.name == it.systemVoiceName }
                    if (matchingVoice != null) {
                        tts?.voice = matchingVoice
                    }
                } catch (e: Exception) {
                    Log.w("TTSHelper", "Could not apply system voice", e)
                }
            }
        }

        val params = Bundle()
        val utteranceId = "diary_tts_${System.currentTimeMillis()}"

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
