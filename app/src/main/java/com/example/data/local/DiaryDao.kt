package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_pages ORDER BY pageNumber ASC, id ASC")
    fun getAllPages(): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_pages WHERE id = :id LIMIT 1")
    fun getPageById(id: Long): Flow<DiaryEntity?>

    @Query("SELECT * FROM diary_pages WHERE content LIKE '%' || :query || '%' OR chapterTitle LIKE '%' || :query || '%' ORDER BY pageNumber ASC")
    fun searchPages(query: String): Flow<List<DiaryEntity>>

    @Query("SELECT COUNT(*) FROM diary_pages")
    suspend fun getPageCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPage(page: DiaryEntity): Long

    @Update
    suspend fun updatePage(page: DiaryEntity)

    @Delete
    suspend fun deletePage(page: DiaryEntity)
}
