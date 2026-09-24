package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val modelName = "gemini-3.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    private fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank() || key == "MY_GEMINI_API_KEY") "" else key
        } catch (e: Exception) {
            ""
        }
    }

    fun hasValidApiKey(): Boolean = getApiKey().isNotBlank()

    /**
     * Gemini Live Mode: Conversational biographer helper.
     * Supports Swiss German & High German inputs.
     * Returns a pair of: (conversationalReply, diarySnippetToAdopt)
     */
    suspend fun chatWithBiographer(
        conversationHistory: List<Pair<String, String>>, // user -> ai
        userInput: String,
        diaryContext: String
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.success(
                Pair(
                    "Ich habe deine Erzählung gehört: „$userInput“. Für lebendige Gemini KI-Antworten trage bitte deinen API-Schlüssel in AI Studio ein. Hier ist eine poetische Fassung für dein Tagebuch:",
                    "In jenem Augenblick spürte ich: $userInput – ein Moment, der sich für immer in mein Gedächtnis einprägen sollte."
                )
            )
        }

        val systemPrompt = """
            Du bist ein warmherziger, weiser und poetischer Biograf für das Tagebuch 'Mein Leben'.
            Der Erzähler teilt mit dir Erinnerungen, Gedanken und Geschichten (oft auf Schweizerdeutsch oder Hochdeutsch).
            
            Verstehe Schweizerdeutsch und Mundart perfekt.
            Antworte in elegantem, einfühlsamem Hochdeutsch mit natürlicher Wärme.
            Halte deine gesprochene Antwort prägnant (ca. 2-4 Sätze), stelle eine sanfte Frage oder bestärke die Emotion.
            
            WICHTIG: Erstelle am Schluss IMMER einen geschliffenen, wunderschönen Textabschnitt in der ersten Person ("Ich..."), 
            den der Nutzer direkt mit einem Klick in sein handschriftliches Tagebuch übernehmen kann.
            
            Formatiere deine Antwort EXAKT so:
            [GESPRÄCH]
            Hier deine gesprochene Antwort an den Nutzer...
            [TAGEBUCH_VORSCHLAG]
            Hier die poetisch geschliffene Tagebuch-Passage in der Ich-Form...
        """.trimIndent()

        val contentsJson = JSONArray()

        // Append historical turns
        for ((user, ai) in conversationHistory.takeLast(4)) {
            val userObj = JSONObject()
            userObj.put("role", "user")
            userObj.put("parts", JSONArray().put(JSONObject().put("text", user)))
            contentsJson.put(userObj)

            val modelObj = JSONObject()
            modelObj.put("role", "model")
            modelObj.put("parts", JSONArray().put(JSONObject().put("text", ai)))
            contentsJson.put(modelObj)
        }

        // Add current input with diary context
        val currentPrompt = if (diaryContext.isNotBlank()) {
            "Kontext aus bisherigem Tagebuch:\n\"$diaryContext\"\n\nMeine gesprochene Erzählung: \"$userInput\""
        } else {
            "Meine gesprochene Erzählung: \"$userInput\""
        }

        val currentUserObj = JSONObject()
        currentUserObj.put("role", "user")
        currentUserObj.put("parts", JSONArray().put(JSONObject().put("text", currentPrompt)))
        contentsJson.put(currentUserObj)

        val rootRequest = JSONObject().apply {
            put("contents", contentsJson)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 600)
            })
        }

        try {
            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(rootRequest.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiClient", "API error: ${response.code} - $bodyString")
                return@withContext Result.failure(Exception("Gemini Fehler (${response.code})"))
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Parse [GESPRÄCH] and [TAGEBUCH_VORSCHLAG]
            var talkPart = text
            var diaryPart = ""

            if (text.contains("[TAGEBUCH_VORSCHLAG]")) {
                val split = text.split("[TAGEBUCH_VORSCHLAG]")
                talkPart = split[0].replace("[GESPRÄCH]", "").trim()
                diaryPart = split.getOrNull(1)?.trim() ?: ""
            } else {
                diaryPart = text
            }

            Result.success(Pair(talkPart, diaryPart))
        } catch (e: Exception) {
            Log.e("GeminiClient", "Exception during Gemini call", e)
            Result.failure(e)
        }
    }

    /**
     * Always-Active AI Text Enhancement:
     * Analyzes writing for sentence structure, emotional richness, flow, and Swiss/German idioms.
     */
    suspend fun analyzeAndSuggestImprovements(text: String): Result<List<TextImprovement>> = withContext(Dispatchers.IO) {
        if (text.length < 15) {
            return@withContext Result.success(emptyList())
        }

        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            // Local fallback suggestion when offline or key not yet inserted
            val words = text.trim().split("\\s+".toRegex())
            if (words.size >= 4) {
                val sampleSnippet = words.take(6).joinToString(" ")
                return@withContext Result.success(
                    listOf(
                        TextImprovement(
                            category = "Poetischer",
                            originalText = sampleSnippet,
                            suggestedText = "$sampleSnippet, wie ein leiser Nachklang vergangener Tage",
                            reasoning = "Verleiht dem Tagebucheintrag eine noch sanftere, lyrische Note."
                        )
                    )
                )
            }
            return@withContext Result.success(emptyList())
        }

        val prompt = """
            Du bist ein meisterhafter Lektor für autobiografische Tagebücher.
            Analysiere den folgenden Tagebuchtext und mache 1 bis maximal 2 dezente, literarische Verbesserungsvorschläge.
            Achte auf: Satzbau, poetischen Ausdruck, emotionale Tiefe und fließende Übergänge (auch bei Schweizerdeutsch-Einflüssen).
            
            Text:
            "$text"
            
            Antworte im exakten JSON-Format als Array:
            [
              {
                "category": "Poetischer" | "Ausdruck" | "Flüssigkeit" | "Grammatik",
                "originalText": "Der genaue Textausschnitt aus dem Original",
                "suggestedText": "Der verbesserte Textausschnitt",
                "reasoning": "Kurze Erklärung auf Deutsch, warum diese Formulierung schöner klingt (1 Satz)."
              }
            ]
            Gib NUR das gültige JSON-Array ohne Markdown-Code-Blöcke zurück.
        """.trimIndent()

        val rootRequest = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            }))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 500)
            })
        }

        try {
            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(rootRequest.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini HTTP ${response.code}"))
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val rawText = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            val cleanedJson = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val list = mutableListOf<TextImprovement>()
            val array = JSONArray(cleanedJson)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                    TextImprovement(
                        category = item.optString("category", "Stil"),
                        originalText = item.optString("originalText"),
                        suggestedText = item.optString("suggestedText"),
                        reasoning = item.optString("reasoning")
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.w("GeminiClient", "Improvement parsing failed", e)
            Result.success(emptyList())
        }
    }

    /**
     * Automatic Mood & Emotional Recognition:
     * Uses Gemini 3.5 Flash when API key is present,
     * or seamlessly falls back to LocalMoodAnalyzer for free/offline mode.
     */
    suspend fun analyzeMood(text: String): Result<com.example.domain.MoodAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        // If no API key is set, immediately use intelligent local analyzer (zero key requirement)
        if (apiKey.isBlank()) {
            return@withContext Result.success(com.example.domain.LocalMoodAnalyzer.analyze(text))
        }

        if (text.trim().length < 10) {
            return@withContext Result.success(com.example.domain.LocalMoodAnalyzer.analyze(text))
        }

        val prompt = """
            Du bist ein feinfühliger psychologischer und literarischer Stimmungsanalytiker für das autobiografische Tagebuch 'Mein Leben'.
            Analysiere den emotionalen Kern und die Stimmung des folgenden Tagebucheintrags (auch bei schweizerdeutschem Wortschatz oder Mundart).
            
            Text:
            "$text"
            
            Bestimme:
            1. "mood": Das treffendste Hauptgefühl als einzelnes deutsches Wort (z. B. "Heiter", "Dankbar", "Friedvoll", "Zuversichtlich", "Nachdenklich", "Nostalgisch", "Melancholisch", "Besorgt", "Erschöpft").
            2. "score": Ein Wert von 1.0 (sehr betrübt/schmerzhaft) bis 10.0 (überwältigend glücklich/euphorisch). 5.0 bis 6.0 ist neutral/ausgeglichen/ruhig.
            3. "emoji": Ein passendes einzelnes Emoji (z. B. ☀️, ✨, 🕊️, 🌱, 🕯️, 🍂, 🌧️, ⚡, 🌙).
            4. "keywords": Eine Liste von 2 bis 4 emotionalen Schlüsselwörtern.
            5. "reflection": Ein einzelner, einfühlsamer poetischer Satz zur emotionalen Essenz dieses Eintrags (auf Deutsch).
            
            Antworte EXAKT im folgenden JSON-Format ohne Markdown-Codeblöcke:
            {
              "mood": "Dankbar",
              "score": 8.0,
              "emoji": "✨",
              "keywords": ["Familie", "Sonnenuntergang", "Zufriedenheit"],
              "reflection": "Ein wärmender Eintrag geprägt von tiefer innerer Dankbarkeit."
            }
        """.trimIndent()

        val rootRequest = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            }))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 300)
            })
        }

        try {
            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(rootRequest.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiClient", "Gemini mood call failed (${response.code}), using local analyzer")
                return@withContext Result.success(com.example.domain.LocalMoodAnalyzer.analyze(text))
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val rawText = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            val cleanedJson = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val obj = JSONObject(cleanedJson)
            val mood = obj.optString("mood", "Nachdenklich")
            val score = obj.optDouble("score", 6.0).toFloat().coerceIn(1.0f, 10.0f)
            val emoji = obj.optString("emoji", "🕯️")
            val keywordsArray = obj.optJSONArray("keywords")
            val keywords = mutableListOf<String>()
            if (keywordsArray != null) {
                for (i in 0 until keywordsArray.length()) {
                    keywords.add(keywordsArray.getString(i))
                }
            }
            val reflection = obj.optString("reflection", "")

            Result.success(
                com.example.domain.MoodAnalysisResult(
                    mood = mood,
                    score = score,
                    emoji = emoji,
                    keywords = keywords.ifEmpty { listOf("Gedanken", "Erinnerung") },
                    reflection = reflection.ifBlank { "Ein bedeutsamer Moment auf deiner Lebensreise." }
                )
            )
        } catch (e: Exception) {
            Log.w("GeminiClient", "Gemini mood call exception, falling back to local analyzer", e)
            Result.success(com.example.domain.LocalMoodAnalyzer.analyze(text))
        }
    }
}
