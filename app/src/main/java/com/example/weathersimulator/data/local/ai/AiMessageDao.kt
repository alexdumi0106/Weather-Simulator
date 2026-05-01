package com.example.weathersimulator.data.local.ai

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AiMessageEntity)

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<AiMessageEntity>>

    @Query("DELETE FROM ai_messages WHERE conversationId = :conversationId")
    suspend fun clearConversation(conversationId: Long)

    @Query("DELETE FROM ai_messages")
    suspend fun clearAll()
}