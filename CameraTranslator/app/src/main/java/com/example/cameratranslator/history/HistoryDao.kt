package com.example.cameratranslator.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TranslationHistory>>

    @Insert
    suspend fun insertHistory(history: TranslationHistory)

    @Query("DELETE FROM translation_history")
    suspend fun clearHistory()

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteHistory(id: Int)
}
