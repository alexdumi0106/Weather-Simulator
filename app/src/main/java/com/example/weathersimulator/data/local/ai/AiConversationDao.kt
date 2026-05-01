package com.example.weathersimulator.data.local.ai

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiConversationDao {

    @Insert
    suspend fun insert(conversation: AiConversationEntity): Long

    @Query("SELECT * FROM ai_conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<AiConversationEntity>>

    @Query("UPDATE ai_conversations SET updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun updateConversationTime(conversationId: Long, updatedAt: Long)

    @Query("DELETE FROM ai_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("DELETE FROM ai_conversations")
    suspend fun clearAll()
}