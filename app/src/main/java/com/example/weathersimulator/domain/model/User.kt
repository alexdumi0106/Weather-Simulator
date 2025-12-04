package com.example.weathersimulator.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val preferences: Map<String, Any> = emptyMap()
)
