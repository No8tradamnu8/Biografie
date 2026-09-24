package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_pages")
data class DiaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterTitle: String = "Kapitel 1: Die frühen Jahre",
    val pageTitle: String = "Ein neuer Anfang",
    val dateString: String = "",
    val content: String = "",
    val pageNumber: Int = 1,
    val mood: String = "Nachdenklich",
    val moodScore: Float = 6.0f, // 1.0f (tiefgründig / melancholisch) bis 10.0f (strahlend heiter)
    val moodKeywords: String = "", // z. B. "Freude, Ruhe, Dankbarkeit"
    val inkStyle: String = "Sepia", // Sepia, BlueBlack, Chestnut
    val updatedTimestamp: Long = System.currentTimeMillis()
)
