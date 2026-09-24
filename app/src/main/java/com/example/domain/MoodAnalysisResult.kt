package com.example.domain

data class MoodAnalysisResult(
    val mood: String, // e.g. "Dankbar", "Heiter", "Friedvoll", "Nachdenklich", "Melancholisch"
    val score: Float, // 1.0 (melancholisch / schmerzlich) bis 10.0 (strahlend / glückselig)
    val emoji: String,
    val keywords: List<String>,
    val reflection: String
) {
    companion object {
        val DEFAULT = MoodAnalysisResult(
            mood = "Nachdenklich",
            score = 5.5f,
            emoji = "🕯️",
            keywords = listOf("Ruhe", "Gedanken"),
            reflection = "Ein Moment des Innehaltens und der Betrachtung."
        )

        fun getMoodColorHex(mood: String): Long {
            return when (mood.lowercase()) {
                "glücklich", "heiter", "strahlend", "begeistert" -> 0xFFD4AF37 // Gold
                "dankbar", "erfüllt", "gesegnet" -> 0xFFC9A84C // Warmes Antikgold
                "friedvoll", "gelassen", "ruhig", "entspannt" -> 0xFF6B8E70 // Salbeigrün
                "hoffnungsvoll", "zuversichtlich", "mutig" -> 0xFF88A858 // Frisches Moosgrün
                "nachdenklich", "philosophisch", "innewerdend" -> 0xFFB38F61 // Warmes Ocker
                "nostalgisch", "sehnsuchtsvoll", "wehmütig" -> 0xFFB07253 // Antikes Kupfer
                "melancholisch", "traurig", "schmerzlich" -> 0xFF6C7A89 // Schieferblau
                "besorgt", "aufgewühlt", "unruhig" -> 0xFFA0522D // Siena / Umbra
                "erschöpft", "müde" -> 0xFF7D7287 // Gedämpftes Lavendelgrau
                else -> 0xFF9E8772 // Pergament-Braun
            }
        }

        fun getMoodEmoji(mood: String): String {
            return when (mood.lowercase()) {
                "glücklich", "heiter", "strahlend" -> "☀️"
                "dankbar", "erfüllt", "gesegnet" -> "✨"
                "friedvoll", "gelassen", "ruhig" -> "🕊️"
                "hoffnungsvoll", "zuversichtlich" -> "🌱"
                "nachdenklich", "philosophisch" -> "🕯️"
                "nostalgisch", "sehnsuchtsvoll" -> "🍂"
                "melancholisch", "traurig" -> "🌧️"
                "besorgt", "aufgewühlt" -> "⚡"
                "erschöpft", "müde" -> "🌙"
                else -> "📖"
            }
        }
    }
}
