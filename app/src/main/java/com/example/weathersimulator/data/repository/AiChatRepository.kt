package com.example.weathersimulator.data.repository

import com.example.weathersimulator.data.local.ai.AiConversationDao
import com.example.weathersimulator.data.local.ai.AiConversationEntity
import com.example.weathersimulator.data.local.ai.AiMessageDao
import com.example.weathersimulator.data.local.ai.AiMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AiChatRepository @Inject constructor(
    private val messageDao: AiMessageDao,
    private val conversationDao: AiConversationDao
) {
    fun getConversations(userId: String): Flow<List<AiConversationEntity>> =
        conversationDao.getConversationsForUser(userId)

    fun getMessages(conversationId: Long): Flow<List<AiMessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun createConversation(title: String, userId: String): Long {
        val now = System.currentTimeMillis()
        return conversationDao.insert(
            AiConversationEntity(
                title = title,
                userId = userId,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun migrateGuestConversations(userId: String) {
        conversationDao.migrateGuestConversations(userId)
    }

    suspend fun insertMessage(
        conversationId: Long,
        text: String,
        isUser: Boolean
    ) {
        messageDao.insert(
            AiMessageEntity(
                conversationId = conversationId,
                text = text,
                isFromUser = isUser,
                timestamp = System.currentTimeMillis()
            )
        )

        conversationDao.updateConversationTime(
            conversationId = conversationId,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun clearConversation(conversationId: Long) {
        messageDao.clearConversation(conversationId)
    }

    suspend fun deleteConversation(conversationId: Long) {
        messageDao.clearConversation(conversationId)
        conversationDao.deleteConversation(conversationId)
    }
}