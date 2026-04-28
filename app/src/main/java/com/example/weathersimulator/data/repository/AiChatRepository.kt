package com.example.weathersimulator.data.repository

import com.example.weathersimulator.data.local.ai.AiMessageDao
import com.example.weathersimulator.data.local.ai.AiMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AiChatRepository @Inject constructor(
    private val dao: AiMessageDao
) {
    fun getMessages(): Flow<List<AiMessageEntity>> = dao.getAllMessages()

    suspend fun insertMessage(text: String, isUser: Boolean) {
        dao.insert(
            AiMessageEntity(
                text = text,
                isFromUser = isUser,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clear() = dao.clearAll()
}