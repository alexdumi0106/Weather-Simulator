package com.example.weathersimulator.ui.screens.simulator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.weathersimulator.sensors.pressure.PressureTrend
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun WeatherScene(
    cloudCoverage: Float,
    isStormy: Boolean,
    pressureTrend: PressureTrend,
    windSpeed: Float,
    humidity: Float,
    temperature: Float,
    weatherDescription: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedSky(
            cloudCoverage = cloudCoverage,
            isStormy = isStormy,
            pressureTrend = pressureTrend,
            windSpeed = windSpeed,
            humidity = humidity,
            weatherDescription = weatherDescription
        )

        RainLayer(
            isStormy = isStormy,
            humidity = humidity,
            windSpeed = windSpeed,
            temperature = temperature
        )

        SnowLayer(
            temperature = temperature,
            humidity = humidity,
            windSpeed = windSpeed
        )

        LightningLayer(
            isStormy = isStormy
        )
    }
}

@Composable
fun LightningLayer(
    isStormy: Boolean
) {
    if (!isStormy) return

    var flashVisible by remember { mutableStateOf(false) }
    var boltSeed by remember { mutableStateOf(0) }

    LaunchedEffect(isStormy) {
        while (isStormy) {
            delay((1500L..4500L).random())

            boltSeed = (0..100000).random()
            flashVisible = true
            delay(140)
            flashVisible = false

            if ((0..100).random() < 35) {
                delay(90)
                boltSeed = (0..100000).random()
                flashVisible = true
                delay(90)
                flashVisible = false
            }
        }
    }

    if (flashVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.18f))
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val startX = width * (0.25f + (boltSeed % 40) / 100f)
            val startY = 0f

            val path = Path().apply {
                moveTo(startX, startY)

                var currentX = startX
                var currentY = startY

                repeat(7) { index ->
                    val xShift = when (index % 3) {
                        0 -> -35f
                        1 -> 20f
                        else -> -15f
                    } + (boltSeed % 17)

                    val yStep = height * 0.08f

                    currentX += xShift
                    currentY += yStep

                    lineTo(currentX, currentY)
                }
            }

            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = 6f)
            )

            drawPath(
                path = path,
                color = Color(0xFFBBDEFB).copy(alpha = 0.65f),
                style = Stroke(width = 12f)
            )
        }
    }
}

@Composable
fun RainLayer(
    isStormy: Boolean,
    humidity: Float,
    windSpeed: Float,
    temperature: Float
) {
    val shouldRain = (isStormy || humidity >= 85f) && temperature > 0f
    if (!shouldRain) return

    val infinite = rememberInfiniteTransition(label = "rain")

    val rainProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isStormy) 700 else 950,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val density = if (isStormy) 180 else 115
        val slant = (windSpeed / 120f) * 22f

        repeat(density) { i ->
            val xBase = (i * 37f) % width
            val yBase = (i * 83f) % height

            val layerFactor = when (i % 3) {
                0 -> 0.85f
                1 -> 1.0f
                else -> 1.2f
            }

            val dropLength = when (i % 4) {
                0 -> 16f
                1 -> 22f
                2 -> 28f
                else -> 34f
            }

            val alpha = when (i % 4) {
                0 -> 0.18f
                1 -> 0.24f
                2 -> 0.32f
                else -> if (isStormy) 0.48f else 0.36f
            }

            val y = (yBase + rainProgress * height * 1.3f * layerFactor) % height
            val x = (xBase + rainProgress * slant * 40f * layerFactor) % width

            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(x, y),
                end = Offset(
                    x - slant * layerFactor,
                    y + dropLength + (windSpeed / 12f)
                ),
                strokeWidth = if (i % 3 == 0) 1.4f else 2f
            )
        }
    }
}

@Composable
fun SnowLayer(
    temperature: Float,
    humidity: Float,
    windSpeed: Float
) {
    val shouldSnow = temperature <= 0f && humidity >= 70f
    if (!shouldSnow) return

    val infinite = rememberInfiniteTransition(label = "snow")

    val snowProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4600,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val drift = (windSpeed / 120f) * 55f

        val flakes = 95

        repeat(flakes) { i ->
            val xBase = (i * 53f) % width
            val yBase = (i * 97f) % height

            val depthFactor = when (i % 3) {
                0 -> 0.75f
                1 -> 1.0f
                else -> 1.2f
            }

            val localOffset = ((i % 5) - 2) * 6f
            val wave = kotlin.math.sin((snowProgress * 2f * Math.PI + i).toFloat()) * (6f + i % 4)

            val y = (yBase + snowProgress * height * 1.05f * depthFactor) % height
            val x = (xBase + drift * snowProgress * depthFactor + wave + localOffset) % width

            val radius = when (i % 4) {
                0 -> 2.2f
                1 -> 3.2f
                2 -> 4.2f
                else -> 5f
            }

            val alpha = when (i % 4) {
                0 -> 0.45f
                1 -> 0.62f
                2 -> 0.78f
                else -> 0.92f
            }

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}