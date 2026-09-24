package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.local.DiaryEntity
import com.example.domain.MoodAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates beautifully formatted parchment-styled PDFs for diary entries.
 * Supports single entry export as well as complete book export with a vintage cover.
 */
object PdfExporter {

    // Standard A4 dimensions in PostScript points (72 DPI)
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    // Parchment palette
    private val COLOR_PARCHMENT_BG = Color.parseColor("#FAF4E8")
    private val COLOR_PARCHMENT_SHADOW = Color.parseColor("#F1E6D0")
    private val COLOR_GOLD_BORDER = Color.parseColor("#C29B38")
    private val COLOR_LEATHER_DARK = Color.parseColor("#3D2314")
    private val COLOR_LEATHER_WARM = Color.parseColor("#7A4926")
    private val COLOR_INK_SEPIA = Color.parseColor("#28160C")
    private val COLOR_INK_BLUEBLACK = Color.parseColor("#172439")
    private val COLOR_INK_CHESTNUT = Color.parseColor("#481F08")
    private val COLOR_GOLD_LIGHT = Color.parseColor("#DFBC69")
    private val COLOR_MUTED_GOLD = Color.parseColor("#9B783E")

    data class ExportResult(
        val file: File,
        val uri: Uri,
        val totalPages: Int
    )

    /**
     * Exports a single diary page to a parchment-style PDF.
     */
    suspend fun exportSinglePagePdf(
        context: Context,
        page: DiaryEntity,
        pageNumberDisplay: Int = page.pageNumber
    ): ExportResult = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        val cinzelTypeface = ResourcesCompat.getFont(context, R.font.cinzel) ?: Typeface.SERIF
        val scriptTypeface = ResourcesCompat.getFont(context, R.font.dancing_script) ?: Typeface.SERIF
        val vibesTypeface = ResourcesCompat.getFont(context, R.font.great_vibes) ?: Typeface.SERIF

        val cleanContent = page.content.ifBlank { "Diese Seite wurde im Tagebuch festgehalten." }

        // Measure and paginate text to handle long entries
        val contentPaints = createContentPaint(scriptTypeface, page.inkStyle)
        val textWidth = PAGE_WIDTH - 104 // 52 margin left & right
        val contentHeightAvailable = 600

        val pagesTextChunks = splitTextIntoPdfPages(cleanContent, contentPaints, textWidth, contentHeightAvailable)
        val totalSubPages = pagesTextChunks.size.coerceAtLeast(1)

        pagesTextChunks.forEachIndexed { subIndex, textChunk ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, subIndex + 1).create()
            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas = pdfPage.canvas

            // 1. Draw Antique Parchment Background & Vintage Frame
            drawParchmentBackground(canvas)

            // 2. Draw Book & Chapter Header
            drawPageHeader(
                canvas = canvas,
                chapterTitle = page.chapterTitle,
                dateString = page.dateString,
                mood = page.mood,
                moodScore = page.moodScore,
                cinzel = cinzelTypeface,
                vibes = vibesTypeface,
                isSubPage = subIndex > 0
            )

            // 3. Draw Body Text
            drawPageContent(
                canvas = canvas,
                text = textChunk,
                textPaint = contentPaints,
                textWidth = textWidth,
                topY = 145f
            )

            // 4. Draw Footer with leaf divider and page numbering
            val footerPageLabel = if (totalSubPages > 1) {
                "Blatt $pageNumberDisplay · Teil ${subIndex + 1}/$totalSubPages"
            } else {
                "Blatt $pageNumberDisplay"
            }
            drawPageFooter(canvas, footerPageLabel, cinzelTypeface)

            pdfDocument.finishPage(pdfPage)
        }

        // Save PDF to cache
        val pdfDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val safeDate = page.dateString.replace("[^a-zA-Z0-9]".toRegex(), "_").take(15)
        val filename = "Tagebuch_Blatt_${page.pageNumber}_${safeDate.ifBlank { "Eintrag" }}.pdf"
        val outputFile = File(pdfDir, filename)

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )

        ExportResult(outputFile, uri, totalSubPages)
    }

    /**
     * Exports all diary pages into a bound book PDF with an ornamental cover page.
     */
    suspend fun exportFullDiaryPdf(
        context: Context,
        pages: List<DiaryEntity>
    ): ExportResult = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        val cinzelTypeface = ResourcesCompat.getFont(context, R.font.cinzel) ?: Typeface.SERIF
        val scriptTypeface = ResourcesCompat.getFont(context, R.font.dancing_script) ?: Typeface.SERIF
        val vibesTypeface = ResourcesCompat.getFont(context, R.font.great_vibes) ?: Typeface.SERIF

        var pdfPageCounter = 1

        // --- 1. Ornamental Book Cover Page ---
        val coverInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageCounter++).create()
        val coverPage = pdfDocument.startPage(coverInfo)
        drawBookCover(
            canvas = coverPage.canvas,
            totalPages = pages.size,
            firstDate = pages.firstOrNull()?.dateString.orEmpty(),
            lastDate = pages.lastOrNull()?.dateString.orEmpty(),
            cinzel = cinzelTypeface,
            vibes = vibesTypeface
        )
        pdfDocument.finishPage(coverPage)

        // --- 2. Subsequent Diary Pages ---
        val textWidth = PAGE_WIDTH - 104
        val contentHeightAvailable = 600

        pages.forEachIndexed { pageIndex, entity ->
            val cleanContent = entity.content.ifBlank { "Leere Seite." }
            val contentPaints = createContentPaint(scriptTypeface, entity.inkStyle)
            val chunks = splitTextIntoPdfPages(cleanContent, contentPaints, textWidth, contentHeightAvailable)
            val subTotal = chunks.size.coerceAtLeast(1)

            chunks.forEachIndexed { subIndex, textChunk ->
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageCounter++).create()
                val currentPdfPage = pdfDocument.startPage(pageInfo)
                val canvas = currentPdfPage.canvas

                drawParchmentBackground(canvas)

                drawPageHeader(
                    canvas = canvas,
                    chapterTitle = entity.chapterTitle,
                    dateString = entity.dateString,
                    mood = entity.mood,
                    moodScore = entity.moodScore,
                    cinzel = cinzelTypeface,
                    vibes = vibesTypeface,
                    isSubPage = subIndex > 0
                )

                drawPageContent(
                    canvas = canvas,
                    text = textChunk,
                    textPaint = contentPaints,
                    textWidth = textWidth,
                    topY = 145f
                )

                val footerLabel = if (subTotal > 1) {
                    "Kapitel ${pageIndex + 1} (${subIndex + 1}/$subTotal)"
                } else {
                    "Kapitel ${pageIndex + 1}"
                }
                drawPageFooter(canvas, footerLabel, cinzelTypeface)

                pdfDocument.finishPage(currentPdfPage)
            }
        }

        // Save book PDF
        val pdfDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.GERMAN).format(Date())
        val outputFile = File(pdfDir, "Mein_Leben_Gesamtes_Tagebuch_$dateStamp.pdf")

        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )

        ExportResult(outputFile, uri, pdfPageCounter - 1)
    }

    /**
     * Creates an Intent to share or send the generated PDF.
     */
    fun createShareIntent(uri: Uri, title: String = "Mein Leben – Tagebuch (PDF)"): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Creates an Intent to view/open or print the generated PDF.
     */
    fun createViewIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    // --- Private Drawing Helpers ---

    private fun createContentPaint(typeface: Typeface, inkStyle: String): TextPaint {
        val color = when (inkStyle) {
            "BlueBlack" -> COLOR_INK_BLUEBLACK
            "Chestnut" -> COLOR_INK_CHESTNUT
            else -> COLOR_INK_SEPIA
        }
        return TextPaint().apply {
            isAntiAlias = true
            this.color = color
            textSize = 15.5f
            this.typeface = typeface
        }
    }

    private fun drawParchmentBackground(canvas: Canvas) {
        val bgPaint = Paint().apply {
            color = COLOR_PARCHMENT_BG
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Subtle vintage margin shadow
        val shadowPaint = Paint().apply {
            color = COLOR_PARCHMENT_SHADOW
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(8f, 8f, (PAGE_WIDTH - 8).toFloat(), (PAGE_HEIGHT - 8).toFloat(), shadowPaint)

        // Outer ornate frame in Gold
        val goldFramePaint = Paint().apply {
            color = COLOR_GOLD_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 1.8f
            isAntiAlias = true
        }
        canvas.drawRect(26f, 26f, (PAGE_WIDTH - 26).toFloat(), (PAGE_HEIGHT - 26).toFloat(), goldFramePaint)

        // Inner ornate frame in Warm Leather
        val innerFramePaint = Paint().apply {
            color = COLOR_LEATHER_WARM
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
            isAntiAlias = true
        }
        canvas.drawRect(31f, 31f, (PAGE_WIDTH - 31).toFloat(), (PAGE_HEIGHT - 31).toFloat(), innerFramePaint)

        // Corner Diamond Rosettes
        drawCornerFlourish(canvas, 28.5f, 28.5f)
        drawCornerFlourish(canvas, (PAGE_WIDTH - 28.5f), 28.5f)
        drawCornerFlourish(canvas, 28.5f, (PAGE_HEIGHT - 28.5f))
        drawCornerFlourish(canvas, (PAGE_WIDTH - 28.5f), (PAGE_HEIGHT - 28.5f))
    }

    private fun drawCornerFlourish(canvas: Canvas, cx: Float, cy: Float) {
        val paint = Paint().apply {
            color = COLOR_MUTED_GOLD
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val path = Path().apply {
            moveTo(cx, cy - 4.5f)
            lineTo(cx + 4.5f, cy)
            lineTo(cx, cy + 4.5f)
            lineTo(cx - 4.5f, cy)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawPageHeader(
        canvas: Canvas,
        chapterTitle: String,
        dateString: String,
        mood: String,
        moodScore: Float,
        cinzel: Typeface,
        vibes: Typeface,
        isSubPage: Boolean
    ) {
        val topTitlePaint = Paint().apply {
            color = COLOR_LEATHER_WARM
            typeface = cinzel
            textSize = 9.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("~ MEIN LEBEN · DAS TAGEBUCH ~", PAGE_WIDTH / 2f, 54f, topTitlePaint)

        // Chapter title
        val chapterPaint = Paint().apply {
            color = COLOR_LEATHER_DARK
            typeface = cinzel
            textSize = 15.5f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val titleText = if (isSubPage) "$chapterTitle (Fortsetzung)" else chapterTitle
        canvas.drawText(titleText, PAGE_WIDTH / 2f, 78f, chapterPaint)

        // Date on Left
        val datePaint = Paint().apply {
            color = COLOR_LEATHER_WARM
            typeface = vibes
            textSize = 15f
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        val displayDate = if (dateString.isNotBlank()) dateString else "Datum unbekannt"
        canvas.drawText(displayDate, 48f, 102f, datePaint)

        // Mood Stamp on Right
        val moodPaint = Paint().apply {
            color = COLOR_MUTED_GOLD
            typeface = cinzel
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val emoji = MoodAnalysisResult.getMoodEmoji(mood)
        val scoreText = if (moodScore > 0f) " (${String.format(Locale.GERMAN, "%.1f", moodScore)}/10)" else ""
        canvas.drawText("$emoji $mood$scoreText", (PAGE_WIDTH - 48).toFloat(), 102f, moodPaint)

        // Gold divider line with central diamond
        drawOrnamentalDivider(canvas, 115f)
    }

    private fun drawOrnamentalDivider(canvas: Canvas, y: Float) {
        val linePaint = Paint().apply {
            color = COLOR_GOLD_BORDER
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val midX = PAGE_WIDTH / 2f
        canvas.drawLine(48f, y, midX - 16f, y, linePaint)
        canvas.drawLine(midX + 16f, y, (PAGE_WIDTH - 48).toFloat(), y, linePaint)

        // Center diamond
        val diamondPaint = Paint().apply {
            color = COLOR_GOLD_LIGHT
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val path = Path().apply {
            moveTo(midX, y - 4f)
            lineTo(midX + 6f, y)
            lineTo(midX, y + 4f)
            lineTo(midX - 6f, y)
            close()
        }
        canvas.drawPath(path, diamondPaint)

        val borderDiamondPaint = Paint().apply {
            color = COLOR_GOLD_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        canvas.drawPath(path, borderDiamondPaint)
    }

    private fun drawPageContent(
        canvas: Canvas,
        text: String,
        textPaint: TextPaint,
        textWidth: Int,
        topY: Float
    ) {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4.5f, 1.25f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(52f, topY)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawPageFooter(canvas: Canvas, pageLabel: String, cinzel: Typeface) {
        val lineY = 780f
        val linePaint = Paint().apply {
            color = COLOR_GOLD_BORDER
            strokeWidth = 0.8f
            isAntiAlias = true
        }
        canvas.drawLine(80f, lineY, (PAGE_WIDTH - 80).toFloat(), lineY, linePaint)

        val pagePaint = Paint().apply {
            color = COLOR_LEATHER_WARM
            typeface = cinzel
            textSize = 9.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("~ $pageLabel ~", PAGE_WIDTH / 2f, 798f, pagePaint)

        val signPaint = Paint().apply {
            color = Color.parseColor("#A89582")
            typeface = cinzel
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Festgehalten im Pergament-Tagebuch", PAGE_WIDTH / 2f, 810f, signPaint)
    }

    private fun drawBookCover(
        canvas: Canvas,
        totalPages: Int,
        firstDate: String,
        lastDate: String,
        cinzel: Typeface,
        vibes: Typeface
    ) {
        // Deep antique parchment fill
        val coverBgPaint = Paint().apply {
            color = Color.parseColor("#F5ECD8")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), coverBgPaint)

        // Multiple ornate borders
        val outerBorder = Paint().apply {
            color = COLOR_LEATHER_DARK
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawRect(24f, 24f, (PAGE_WIDTH - 24).toFloat(), (PAGE_HEIGHT - 24).toFloat(), outerBorder)

        val innerGoldBorder = Paint().apply {
            color = COLOR_GOLD_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawRect(30f, 30f, (PAGE_WIDTH - 30).toFloat(), (PAGE_HEIGHT - 30).toFloat(), innerGoldBorder)

        val midX = PAGE_WIDTH / 2f

        // Top decorative flourish
        val subtitlePaint = Paint().apply {
            color = COLOR_LEATHER_WARM
            typeface = cinzel
            textSize = 12f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("CHRONIK DER ERINNERUNGEN", midX, 150f, subtitlePaint)

        // Big Majestic Title
        val titlePaint = Paint().apply {
            color = COLOR_LEATHER_DARK
            typeface = cinzel
            textSize = 34f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("MEIN LEBEN", midX, 195f, titlePaint)

        val subBookPaint = Paint().apply {
            color = COLOR_MUTED_GOLD
            typeface = cinzel
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("DAS PERSÖNLICHE TAGEBUCH", midX, 222f, subBookPaint)

        drawOrnamentalDivider(canvas, 245f)

        // Hand-drawn Seal in center
        val sealCy = 390f
        val sealPaint = Paint().apply {
            color = COLOR_GOLD_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawCircle(midX, sealCy, 60f, sealPaint)

        val innerSealPaint = Paint().apply {
            color = COLOR_LEATHER_WARM
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawCircle(midX, sealCy, 54f, innerSealPaint)

        val sealCorePaint = Paint().apply {
            color = Color.parseColor("#EAD6B8")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(midX, sealCy, 53f, sealCorePaint)

        val sealTextPaint = Paint().apply {
            color = COLOR_LEATHER_DARK
            typeface = cinzel
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("SIGILLUM", midX, sealCy - 6f, sealTextPaint)
        canvas.drawText("MEMORIAE", midX, sealCy + 10f, sealTextPaint)

        // Poetic Quote
        val quotePaint = Paint().apply {
            color = COLOR_LEATHER_DARK
            typeface = vibes
            textSize = 19f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("»Jeder Tag ist eine wertvolle Seite in dem Buch,", midX, 515f, quotePaint)
        canvas.drawText("das du mit deinen eigenen Erlebnissen schreibst.«", midX, 540f, quotePaint)

        drawOrnamentalDivider(canvas, 580f)

        // Metadata box
        val metaPaint = Paint().apply {
            color = COLOR_LEATHER_WARM
            typeface = cinzel
            textSize = 11f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Gesamtumfang: $totalPages Kapitel & Blätter", midX, 640f, metaPaint)

        if (firstDate.isNotBlank() || lastDate.isNotBlank()) {
            val range = if (firstDate == lastDate || lastDate.isBlank()) firstDate else "$firstDate — $lastDate"
            val rangePaint = Paint().apply {
                color = COLOR_MUTED_GOLD
                typeface = cinzel
                textSize = 10f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Zeitraum: $range", midX, 660f, rangePaint)
        }

        val footerExportDate = SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN).format(Date())
        val dateLabelPaint = Paint().apply {
            color = Color.parseColor("#8E755D")
            typeface = cinzel
            textSize = 9.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Gebunden als Pergament-Dokument am $footerExportDate", midX, 770f, dateLabelPaint)
    }

    /**
     * Splits long diary text across multiple PDF pages when it exceeds available page height.
     */
    private fun splitTextIntoPdfPages(
        text: String,
        textPaint: TextPaint,
        textWidth: Int,
        maxHeight: Int
    ): List<String> {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4.5f, 1.25f)
            .setIncludePad(false)
            .build()

        if (staticLayout.height <= maxHeight) {
            return listOf(text)
        }

        val pages = mutableListOf<String>()
        var startLine = 0
        val lineCount = staticLayout.lineCount

        while (startLine < lineCount) {
            val lineTopStart = staticLayout.getLineTop(startLine)
            var endLine = startLine

            while (endLine < lineCount && (staticLayout.getLineBottom(endLine) - lineTopStart) <= maxHeight) {
                endLine++
            }

            // Ensure at least one line is included per page to prevent infinite loops
            if (endLine == startLine) {
                endLine = startLine + 1
            }

            val startIndex = staticLayout.getLineStart(startLine)
            val endIndex = staticLayout.getLineEnd(endLine - 1)
            val chunk = text.substring(startIndex, endIndex).trimEnd()

            if (chunk.isNotBlank()) {
                pages.add(chunk)
            }

            startLine = endLine
        }

        return if (pages.isEmpty()) listOf(text) else pages
    }
}
