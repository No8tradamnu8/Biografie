package com.example

import android.content.Intent
import android.net.Uri
import com.example.domain.MoodAnalysisResult
import com.example.util.PdfExporter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun moodAnalysis_fallbackEmoji_isReturnedCorrectly() {
    val emojiHappy = MoodAnalysisResult.getMoodEmoji("Glücklich")
    assertEquals("☀️", emojiHappy)

    val emojiGrateful = MoodAnalysisResult.getMoodEmoji("Dankbar")
    assertEquals("✨", emojiGrateful)

    val emojiMelancholic = MoodAnalysisResult.getMoodEmoji("Melancholisch")
    assertEquals("🌧️", emojiMelancholic)
  }

  @Test
  fun pdfExporter_intents_areConfiguredProperly() {
    val mockUri = Uri.parse("content://com.example.fileprovider/pdfs/test.pdf")
    
    val shareIntent = PdfExporter.createShareIntent(mockUri, "Mein Leben – PDF")
    assertEquals(Intent.ACTION_SEND, shareIntent.action)
    assertEquals("application/pdf", shareIntent.type)
    assertTrue(shareIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)

    val viewIntent = PdfExporter.createViewIntent(mockUri)
    assertEquals(Intent.ACTION_VIEW, viewIntent.action)
    assertEquals("application/pdf", viewIntent.type)
    assertTrue(viewIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
  }
}

