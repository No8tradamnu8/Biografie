package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfExportService(private val context: Context) {

    fun exportToPdf(title: String, content: String): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Parchment Background
        val paintBg = Paint()
        paintBg.color = Color.parseColor("#D2B48C")
        canvas.drawRect(0f, 0f, 595f, 842f, paintBg)

        // Text Painting
        val paintText = Paint()
        paintText.color = Color.BLACK
        paintText.textSize = 20f

        // Draw Title
        paintText.isFakeBoldText = true
        canvas.drawText(title, 50f, 100f, paintText)

        // Draw Content
        paintText.isFakeBoldText = false
        paintText.textSize = 16f
        
        val lines = content.split("\n")
        var yPosition = 150f
        for (line in lines) {
            canvas.drawText(line, 50f, yPosition, paintText)
            yPosition += 25f
        }

        document.finishPage(page)

        val file = File(context.cacheDir, "diary_export.pdf")
        try {
            document.writeTo(FileOutputStream(file))
            document.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Teile Tagebuch als PDF"))
    }
}
