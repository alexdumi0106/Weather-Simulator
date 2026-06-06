package com.example.weathersimulator.ui.screens.games.cloudcatcher

import androidx.compose.ui.geometry.Size
import kotlin.math.ceil
import kotlin.math.min
import kotlin.random.Random

internal fun CloudCatcherObject.collidesWithPlane(planeY: Float): Boolean {
    val dx = x - PlaneX
    val dy = y - planeY
    val hitRadius = radius + 0.070f
    return dx * dx + dy * dy <= hitRadius * hitRadius
}

internal fun createCloudCatcherObject(
    id: Long,
    random: Random,
    progress: Float
): CloudCatcherObject {
    val roll = random.nextFloat()
    val type = when {
        roll < 0.42f -> CloudCatcherObjectType.WhiteCloud
        roll < 0.70f -> CloudCatcherObjectType.RainDrop
        roll < 0.86f -> CloudCatcherObjectType.Lightning
        else -> CloudCatcherObjectType.StormCloud
    }

    val baseSpeed = when (type) {
        CloudCatcherObjectType.WhiteCloud -> 0.18f
        CloudCatcherObjectType.RainDrop -> 0.22f
        CloudCatcherObjectType.StormCloud -> 0.20f
        CloudCatcherObjectType.Lightning -> 0.26f
    }

    val radius = when (type) {
        CloudCatcherObjectType.WhiteCloud -> 0.074f
        CloudCatcherObjectType.RainDrop -> 0.050f
        CloudCatcherObjectType.StormCloud -> 0.082f
        CloudCatcherObjectType.Lightning -> 0.062f
    }

    return CloudCatcherObject(
        id = id,
        type = type,
        x = 1.12f,
        y = random.nextFloat().coerceIn(0f, 1f) * 0.46f + 0.18f,
        speed = baseSpeed + random.nextFloat() * 0.06f + progress * 0.08f,
        radius = radius
    )
}

internal fun initialCloudCatcherObjects(): List<CloudCatcherObject> {
    return listOf(
        CloudCatcherObject(
            id = 0L,
            type = CloudCatcherObjectType.WhiteCloud,
            x = 0.50f,
            y = 0.24f,
            speed = 0.18f,
            radius = 0.074f
        ),
        CloudCatcherObject(
            id = 1L,
            type = CloudCatcherObjectType.RainDrop,
            x = 0.72f,
            y = 0.34f,
            speed = 0.23f,
            radius = 0.050f
        ),
        CloudCatcherObject(
            id = 2L,
            type = CloudCatcherObjectType.WhiteCloud,
            x = 0.25f,
            y = 0.43f,
            speed = 0.18f,
            radius = 0.078f
        ),
        CloudCatcherObject(
            id = 3L,
            type = CloudCatcherObjectType.Lightning,
            x = 0.66f,
            y = 0.44f,
            speed = 0.26f,
            radius = 0.064f
        ),
        CloudCatcherObject(
            id = 4L,
            type = CloudCatcherObjectType.StormCloud,
            x = 0.86f,
            y = 0.43f,
            speed = 0.20f,
            radius = 0.084f
        )
    )
}

internal fun spawnInterval(remainingMillis: Long): Float {
    val progress = gameProgress(remainingMillis)
    return (0.92f - progress * 0.34f).coerceAtLeast(0.46f)
}

internal fun gameProgress(remainingMillis: Long): Float {
    return (1f - remainingMillis / TotalGameMillis.toFloat()).coerceIn(0f, 1f)
}

internal fun Long.secondsText(): String {
    return "${ceil(this / 1000.0).toInt().coerceAtLeast(0)}s"
}

internal fun Int.scoreText(): String {
    return toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

internal val Size.minDimension: Float
    get() = min(width, height)
