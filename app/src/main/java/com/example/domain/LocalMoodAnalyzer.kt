package com.example.domain

import java.util.Locale

object LocalMoodAnalyzer {

    // High German & Swiss German keyword groups
    private val joyKeywords = listOf(
        "freude", "glücklich", "glück", "lachen", "schön", "wunderbar", "herrlich", "toll",
        "begeistert", "stolz", "sonne", "sommer", "strahlen", "fest", "lustig", "feiern",
        "froh", "jauchzen", "genuss", "geniessen", "genossen", "super", "herzlich", "verliebt",
        "liebe", "lächeln", "lächle", "lächlä", "schöns", "gmüetlich", "mega", "juhui",
        "zfriede", "liebi", "gold", "blumen", "erfolg", "jubel", "freu", "strahlt"
    )

    private val gratitudeKeywords = listOf(
        "danke", "dankbar", "dankbarkeit", "geschenk", "schätzen", "segen", "wertvoll",
        "glückspilz", "gnade", "hilfe", "beistand", "geborgen", "unterstützung", "treue",
        "merci", "vielmal", "dankä", "gschänk", "schätze", "dankbarheit", "reich beschenkt"
    )

    private val peaceKeywords = listOf(
        "ruhig", "ruhe", "stille", "frieden", "friedlich", "gelassen", "entspannt",
        "harmonie", "ausruhen", "sanft", "ausgeglichen", "balance", "kaffee", "tee",
        "spaziergang", "natur", "wald", "see", "berge", "atem", "atmen", "fride",
        "ruehig", "pause", "gmietlich", "stilli", "loslassen", "einklang"
    )

    private val hopeKeywords = listOf(
        "hoffnung", "zuversicht", "morgen", "zukunft", "neuanfang", "mut", "kraft",
        "glaube", "wachsen", "ziel", "traum", "pläne", "schaffen", "vorfreude",
        "vorwärts", "chraft", "hoffnig", "vertroue", "blick nach vorn", "neuer schritt"
    )

    private val reflectiveKeywords = listOf(
        "nachdenken", "gedanken", "erinnerung", "vergangenheit", "sinn", "fragen",
        "tagebuch", "schreiben", "überlegen", "zeit", "lebenszeit", "warum",
        "verstehen", "erinnern", "dänke", "überlege", "erinnere", "gspür", "bhalte",
        "bedeutung", "spuren", "augenblick", "leben", "innerlich"
    )

    private val nostalgiaKeywords = listOf(
        "nostalgie", "sehnsucht", "früher", "damals", "kindheit", "vergangen",
        "alte zeiten", "jugend", "heimweh", "vermissen", "sehne", "einst",
        "gedenken", "erinnerig", "jahrzehnte", "damals als"
    )

    private val melancholyKeywords = listOf(
        "traurig", "tränen", "weinen", "trauer", "schmerz", "einsam", "schwer",
        "verlust", "fehlen", "dunkel", "grau", "trurig", "weine", "weh",
        "wehtun", "kummer", "kummervoll", "einsamkeit", "leer", "leere",
        "abschied", "vermiss", "schad", "leid", "härzschmerz", "herzschmerz"
    )

    private val anxietyKeywords = listOf(
        "angst", "sorge", "sorgen", "stress", "hektik", "wut", "ärger",
        "nervös", "ungewiss", "panik", "überfordert", "krise", "sturm",
        "streit", "konflikt", "problem", "sorg", "sträss", "ärgere", "druck"
    )

    private val fatigueKeywords = listOf(
        "müde", "erschöpft", "kraftlos", "fertig", "kaputt", "schlaf",
        "ausgelaugt", "müed", "schlapp", "schwerfällig", "schlaflos"
    )

    fun analyze(text: String): MoodAnalysisResult {
        val trimmed = text.trim()
        if (trimmed.length < 10) {
            return MoodAnalysisResult(
                mood = "Offen",
                score = 5.5f,
                emoji = "📖",
                keywords = listOf("Erwartung", "Neubeginn"),
                reflection = "Ein neues Pergamentblatt wartet auf deine Geschichten und Gedanken."
            )
        }

        val lower = trimmed.lowercase(Locale.GERMAN)
        val words = lower.split(Regex("[\\s.,;:!?\"'()\\[\\]\\-]+")).filter { it.isNotBlank() }

        var joyScore = 0f
        var gratitudeScore = 0f
        var peaceScore = 0f
        var hopeScore = 0f
        var reflectiveScore = 0f
        var nostalgiaScore = 0f
        var melancholyScore = 0f
        var anxietyScore = 0f
        var fatigueScore = 0f

        val detectedKeywords = mutableListOf<String>()

        fun checkMatch(word: String, list: List<String>): Boolean {
            return list.any { word.contains(it) || it.contains(word) }
        }

        for (word in words) {
            if (checkMatch(word, joyKeywords)) {
                joyScore += 2.2f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Freude")) detectedKeywords.add("Freude")
            }
            if (checkMatch(word, gratitudeKeywords)) {
                gratitudeScore += 2.5f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Dankbarkeit")) detectedKeywords.add("Dankbarkeit")
            }
            if (checkMatch(word, peaceKeywords)) {
                peaceScore += 1.9f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Ruhe")) detectedKeywords.add("Ruhe")
            }
            if (checkMatch(word, hopeKeywords)) {
                hopeScore += 1.8f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Zuversicht")) detectedKeywords.add("Zuversicht")
            }
            if (checkMatch(word, reflectiveKeywords)) {
                reflectiveScore += 1.4f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Besinnung")) detectedKeywords.add("Besinnung")
            }
            if (checkMatch(word, nostalgiaKeywords)) {
                nostalgiaScore += 1.7f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Nostalgie")) detectedKeywords.add("Nostalgie")
            }
            if (checkMatch(word, melancholyKeywords)) {
                melancholyScore += 2.4f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Wehmut")) detectedKeywords.add("Wehmut")
            }
            if (checkMatch(word, anxietyKeywords)) {
                anxietyScore += 2.2f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Unruhe")) detectedKeywords.add("Unruhe")
            }
            if (checkMatch(word, fatigueKeywords)) {
                fatigueScore += 1.7f
                if (detectedKeywords.size < 4 && !detectedKeywords.contains("Erschöpfung")) detectedKeywords.add("Erschöpfung")
            }
        }

        // Determine dominant mood
        val scores = listOf(
            Triple("Heiter", joyScore, "☀️"),
            Triple("Dankbar", gratitudeScore, "✨"),
            Triple("Friedvoll", peaceScore, "🕊️"),
            Triple("Zuversichtlich", hopeScore, "🌱"),
            Triple("Nostalgisch", nostalgiaScore, "🍂"),
            Triple("Nachdenklich", reflectiveScore + 0.8f, "🕯️"), // Slight baseline for diary writing
            Triple("Melancholisch", melancholyScore, "🌧️"),
            Triple("Besorgt", anxietyScore, "⚡"),
            Triple("Erschöpft", fatigueScore, "🌙")
        )

        val dominant = scores.maxByOrNull { it.second } ?: Triple("Nachdenklich", 1.0f, "🕯️")

        // Calculate continuous valence score (1.0 to 10.0)
        val positiveSum = joyScore + gratitudeScore + peaceScore + hopeScore
        val negativeSum = melancholyScore + anxietyScore + fatigueScore

        val baseScore = when (dominant.first) {
            "Heiter" -> 8.8f
            "Dankbar" -> 8.2f
            "Friedvoll" -> 7.4f
            "Zuversichtlich" -> 7.2f
            "Nachdenklich" -> 5.5f
            "Nostalgisch" -> 4.8f
            "Erschöpft" -> 3.2f
            "Melancholisch" -> 2.6f
            "Besorgt" -> 2.4f
            else -> 5.5f
        }

        val adjustedScore = (baseScore + (positiveSum * 0.15f) - (negativeSum * 0.18f)).coerceIn(1.0f, 9.8f)
        val finalScore = (Math.round(adjustedScore * 10f) / 10f)

        if (detectedKeywords.isEmpty()) {
            detectedKeywords.add("Erinnerungen")
            detectedKeywords.add("Gedanken")
        }

        val reflection = when (dominant.first) {
            "Heiter" -> "Ein Tag voller Leichtigkeit, sonniger Momente und warmer Freude."
            "Dankbar" -> "Getragen von tiefer Dankbarkeit für das, was das Leben heute geschenkt hat."
            "Friedvoll" -> "Ein stiller, harmonischer Augenblick der inneren Einkehr und Ruhe."
            "Zuversichtlich" -> "Mit wachem Blick und Mut nach vorn gerichtet – getragen von Hoffnung."
            "Nostalgisch" -> "Ein zarter Hauch vergangener Tage und kostbarer Erinnerungen."
            "Melancholisch" -> "Ein berührend tiefgründiger Eintrag, der auch den leisen Schmerz achtet."
            "Besorgt" -> "Ein bewegter Tag – das Pergament fängt die aufgewühlten Gedanken auf."
            "Erschöpft" -> "Nach vielen Schritten Zeit, die Last abzulegen und zur Ruhe zu kommen."
            else -> "Ein kostbarer Gedankensplitter auf deiner Lebensreise niedergeschrieben."
        }

        return MoodAnalysisResult(
            mood = dominant.first,
            score = finalScore,
            emoji = dominant.third,
            keywords = detectedKeywords,
            reflection = reflection
        )
    }
}
