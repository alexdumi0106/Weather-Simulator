package com.example.weathersimulator.data.remote.ai

data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)
