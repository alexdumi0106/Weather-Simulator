package com.example.weathersimulator.data.local.ai

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSettingsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getServerUrl(): String {
        val savedUrl = prefs.getString(KEY_SERVER_URL, null)?.trim()

        val url = when {
            savedUrl.isNullOrBlank() -> DEFAULT_SERVER_URL
            savedUrl.contains("10.0.2.2") -> DEFAULT_SERVER_URL
            savedUrl.contains("127.0.0.1") -> DEFAULT_SERVER_URL
            savedUrl.contains("localhost") -> DEFAULT_SERVER_URL
            else -> savedUrl
        }

        return if (url.endsWith("/")) url else "$url/"
    }

    fun setServerUrl(value: String) {
        prefs.edit()
            .putString(KEY_SERVER_URL, value)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "ai_settings"
        private const val KEY_SERVER_URL = "server_url"
        private const val DEFAULT_SERVER_URL = "http://192.168.100.80:8000/"
    }
}