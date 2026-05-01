package com.example.weathersimulator.data.local.ai

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_conversations")
data class AiConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)