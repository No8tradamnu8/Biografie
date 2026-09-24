package com.example.data.repository

import com.example.data.local.DiaryDao
import com.example.data.local.DiaryEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryRepository(private val dao: DiaryDao) {
    val allPages: Flow<List<DiaryEntity>> = dao.getAllPages()

    fun getPageById(id: Long): Flow<DiaryEntity?> = dao.getPageById(id)

    fun searchPages(query: String): Flow<List<DiaryEntity>> = dao.searchPages(query)

    suspend fun savePage(page: DiaryEntity): Long {
        return if (page.id == 0L) {
            dao.insertPage(page.copy(updatedTimestamp = System.currentTimeMillis()))
        } else {
            dao.updatePage(page.copy(updatedTimestamp = System.currentTimeMillis()))
            page.id
        }
    }

    suspend fun deletePage(page: DiaryEntity) {
        dao.deletePage(page)
    }

    suspend fun createNewPage(chapterTitle: String = "Neues Kapitel"): Long {
        val totalCount = dao.getPageCount()
        val today = SimpleDateFormat("d. MMMM yyyy", Locale.GERMAN).format(Date())
        val newPage = DiaryEntity(
            chapterTitle = chapterTitle,
            pageTitle = "Neue Seite",
            dateString = today,
            content = "",
            pageNumber = totalCount + 1,
            mood = "Offen",
            moodScore = 6.0f,
            moodKeywords = "Neubeginn",
            inkStyle = "Sepia"
        )
        return dao.insertPage(newPage)
    }

    fun exportToMarkdown(pages: List<DiaryEntity>): String {
        val sb = StringBuilder()
        sb.append("# Mein Leben – Das Tagebuch\n\n")
        sb.append("Exportiert am: ").append(SimpleDateFormat("d. MMMM yyyy, HH:mm", Locale.GERMAN).format(Date())).append("\n\n")
        for (page in pages) {
            sb.append("## ").append(page.chapterTitle).append("\n")
            sb.append("### ").append(page.pageTitle).append(" (").append(page.dateString).append(")\n")
            sb.append("*Stimmung: ").append(page.mood).append(" (Score: ").append(page.moodScore).append("/10)*\n")
            if (page.moodKeywords.isNotBlank()) {
                sb.append("*Gefühlswelt: ").append(page.moodKeywords).append("*\n")
            }
            sb.append("\n").append(page.content).append("\n\n---\n\n")
        }
        return sb.toString()
    }
}
