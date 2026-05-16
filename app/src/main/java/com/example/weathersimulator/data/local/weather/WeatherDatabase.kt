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
import com.example.weathersimulator.data.local.city.FavoriteCityEntity
import com.example.weathersimulator.data.local.city.FavoriteCityDao

@Database(
    entities = [
        UserEntity::class,
        AiMessageEntity::class,
        AiConversationEntity::class,
        FavoriteCityEntity::class
    ],
    version = 5
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun aiMessageDao(): AiMessageDao
    abstract fun aiConversationDao(): AiConversationDao
    abstract fun userDao(): UserDao
    abstract fun favoriteCityDao(): FavoriteCityDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE ai_conversations ADD COLUMN userId TEXT NOT NULL DEFAULT 'guest'"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favorite_cities (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                displayName TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                country TEXT,
                timezone TEXT,
                isLastSelected INTEGER NOT NULL DEFAULT 0,
                savedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}