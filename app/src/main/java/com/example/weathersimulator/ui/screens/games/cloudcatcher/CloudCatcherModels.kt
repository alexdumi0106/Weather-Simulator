package com.example.weathersimulator.ui.screens.games.cloudcatcher

import androidx.compose.ui.graphics.Color

internal const val TotalGameMillis = 60_000L
internal const val StartingLives = 3
internal const val PlaneX = 0.57f
internal const val MinPlaneY = 0.26f
internal const val MaxPlaneY = 0.72f

internal val CloudCatcherBackground = Color(0xFF061625)
internal val CloudCatcherCyan = Color(0xFF4ED7FF)
internal val CloudCatcherYellow = Color(0xFFFFCF54)
internal val CloudCatcherRed = Color(0xFFFF867A)

enum class CloudCatcherPhase {
    Ready,
    Running,
    Paused,
    Finished
}

enum class CloudCatcherObjectType {
    WhiteCloud,
    RainDrop,
    StormCloud,
    Lightning
}

data class CloudCatcherObject(
    val id: Long,
    val type: CloudCatcherObjectType,
    val x: Float,
    val y: Float,
    val speed: Float,
    val radius: Float
)

data class CloudCatcherGameState(
    val roundId: Long = 0L,
    val phase: CloudCatcherPhase = CloudCatcherPhase.Ready,
    val bestScore: Int = 0,
    val score: Int = 0,
    val lives: Int = StartingLives,
    val remainingMillis: Long = TotalGameMillis,
    val planeY: Float = 0.56f,
    val objects: List<CloudCatcherObject> = emptyList(),
    val nextObjectId: Long = 0L
) {
    val progress: Float
        get() = (remainingMillis / TotalGameMillis.toFloat()).coerceIn(0f, 1f)
}
