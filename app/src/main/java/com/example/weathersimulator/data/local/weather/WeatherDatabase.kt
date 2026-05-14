package com.example.weathersimulator.data.local.weather

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weathersimulator.data.local.user.UserDao
import com.example.weathersimulator.data.local.user.UserEntity
import com.example.weathersimulator.data.local.ai.AiMessageEntity
import com.example.weathersimulator.data.local.ai.AiMessageDao
import com.example.weathersimulator.data.local.ai.AiConversationDao
import com.example.weathersimulator.data.local.ai.AiConversationEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        AiMessageEntity::class,
        AiConversationEntity::class
    ],
    version = 4
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun aiMessageDao(): AiMessageDao
    abstract fun aiConversationDao(): AiConversationDao
    abstract fun userDao(): UserDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE ai_conversations ADD COLUMN userId TEXT NOT NULL DEFAULT 'guest'"
        )
    }
}