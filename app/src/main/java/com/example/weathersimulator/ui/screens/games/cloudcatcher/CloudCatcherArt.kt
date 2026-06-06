package com.example.weathersimulator.ui.screens.games.cloudcatcher

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

internal fun DrawScope.drawCloudCatcherInteractiveLayer(
    planeY: Float,
    objects: List<CloudCatcherObject>
) {
    objects.forEach { item ->
        drawCloudCatcherObject(item)
    }

    drawPlane(
        center = Offset(size.width * PlaneX, size.height * planeY),
        unit = min(size.width, size.height) * 0.060f
    )
}

internal fun DrawScope.drawCloudCatcherObject(item: CloudCatcherObject) {
    val center = Offset(size.width * item.x, size.height * item.y)
    val radius = size.minDimension * item.radius

    when (item.type) {
        CloudCatcherObjectType.WhiteCloud -> {
            drawCircle(
                color = Color.White.copy(alpha = 0.16f),
                radius = radius * 1.85f,
                center = center
            )
            drawCloud(
                center = center,
                radius = radius,
                baseColor = Color.White,
                shadowColor = Color(0xFFC7DDF2)
            )
        }

        CloudCatcherObjectType.RainDrop -> {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        CloudCatcherCyan.copy(alpha = 0.54f),
                        CloudCatcherCyan.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 2.05f
                ),
                radius = radius * 2.05f,
                center = center
            )
            drawRainDrop(
                center = center,
                radius = radius,
                color = CloudCatcherCyan
            )
        }

        CloudCatcherObjectType.StormCloud -> {
            drawCircle(
                color = Color.Black.copy(alpha = 0.22f),
                radius = radius * 1.90f,
                center = center + Offset(radius * 0.08f, radius * 0.20f)
            )
            drawCloud(
                center = center,
                radius = radius,
                baseColor = Color(0xFF5B6675),
                shadowColor = Color(0xFF171C26)
            )
            drawLightningBolt(
                center = center + Offset(radius * 0.18f, radius * 0.94f),
                size = radius * 1.05f,
                color = Color(0xFFFFD34A)
            )
        }

        CloudCatcherObjectType.Lightning -> {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        CloudCatcherYellow.copy(alpha = 0.58f),
                        CloudCatcherYellow.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 2.10f
                ),
                radius = radius * 2.10f,
                center = center
            )
            drawLightningBolt(
                center = center,
                size = radius * 1.82f,
                color = CloudCatcherYellow
            )
        }
    }
}

private fun DrawScope.drawPlane(
    center: Offset,
    unit: Float
) {
    val body = Path().apply {
        moveTo(center.x + unit * 2.0f, center.y)
        cubicTo(
            center.x + unit * 0.70f,
            center.y - unit * 0.70f,
            center.x - unit * 1.30f,
            center.y - unit * 0.50f,
            center.x - unit * 1.65f,
            center.y - unit * 0.08f
        )
        lineTo(center.x - unit * 1.65f, center.y + unit * 0.08f)
        cubicTo(
            center.x - unit * 1.30f,
            center.y + unit * 0.50f,
            center.x + unit * 0.70f,
            center.y + unit * 0.70f,
            center.x + unit * 2.0f,
            center.y
        )
        close()
    }
    drawPath(body, Color(0xFFFFF3D1))

    val wing = Path().apply {
        moveTo(center.x - unit * 0.15f, center.y)
        lineTo(center.x - unit * 0.90f, center.y + unit * 1.18f)
        lineTo(center.x + unit * 0.82f, center.y + unit * 0.38f)
        close()
    }
    drawPath(wing, CloudCatcherCyan)

    val topWing = Path().apply {
        moveTo(center.x - unit * 0.10f, center.y)
        lineTo(center.x - unit * 0.78f, center.y - unit * 0.96f)
        lineTo(center.x + unit * 0.72f, center.y - unit * 0.30f)
        close()
    }
    drawPath(topWing, Color(0xFF7BE6FF))

    val tail = Path().apply {
        moveTo(center.x - unit * 1.28f, center.y - unit * 0.26f)
        lineTo(center.x - unit * 1.78f, center.y - unit * 1.0f)
        lineTo(center.x - unit * 0.92f, center.y - unit * 0.38f)
        close()
    }
    drawPath(tail, CloudCatcherYellow)

    drawCircle(
        color = Color(0xFF173A5E),
        radius = unit * 0.22f,
        center = Offset(center.x + unit * 0.70f, center.y - unit * 0.06f)
    )
}

private fun DrawScope.drawCloud(
    center: Offset,
    radius: Float,
    baseColor: Color,
    shadowColor: Color
) {
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(center.x - radius * 1.52f, center.y - radius * 0.10f),
        size = Size(radius * 3.04f, radius * 0.98f),
        cornerRadius = CornerRadius(radius * 0.50f, radius * 0.50f)
    )
    drawCircle(
        color = baseColor.copy(alpha = 0.96f),
        radius = radius * 0.84f,
        center = Offset(center.x - radius * 0.74f, center.y)
    )
    drawCircle(
        color = baseColor,
        radius = radius,
        center = Offset(center.x, center.y - radius * 0.24f)
    )
    drawCircle(
        color = baseColor.copy(alpha = 0.94f),
        radius = radius * 0.72f,
        center = Offset(center.x + radius * 0.78f, center.y + radius * 0.04f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.22f),
        radius = radius * 0.38f,
        center = Offset(center.x - radius * 0.18f, center.y - radius * 0.58f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.10f),
        topLeft = Offset(center.x - radius * 1.30f, center.y + radius * 0.32f),
        size = Size(radius * 2.60f, radius * 0.42f),
        cornerRadius = CornerRadius(radius * 0.22f, radius * 0.22f)
    )
}

private fun DrawScope.drawRainDrop(
    center: Offset,
    radius: Float,
    color: Color
) {
    val drop = Path().apply {
        moveTo(center.x, center.y - radius * 1.35f)
        cubicTo(
            center.x - radius * 1.0f,
            center.y - radius * 0.34f,
            center.x - radius * 0.84f,
            center.y + radius * 0.84f,
            center.x,
            center.y + radius * 1.06f
        )
        cubicTo(
            center.x + radius * 0.84f,
            center.y + radius * 0.84f,
            center.x + radius * 1.0f,
            center.y - radius * 0.34f,
            center.x,
            center.y - radius * 1.35f
        )
        close()
    }
    drawPath(
        path = drop,
        brush = Brush.radialGradient(
            listOf(
                Color.White.copy(alpha = 0.88f),
                color,
                Color(0xFF1E88FF)
            ),
            center = Offset(center.x - radius * 0.20f, center.y - radius * 0.36f),
            radius = radius * 1.70f
        )
    )
    drawPath(
        path = drop,
        color = Color.White.copy(alpha = 0.42f),
        style = Stroke(width = radius * 0.11f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.70f),
        radius = radius * 0.18f,
        center = Offset(center.x - radius * 0.26f, center.y - radius * 0.28f)
    )
}

private fun DrawScope.drawLightningBolt(
    center: Offset,
    size: Float,
    color: Color
) {
    val bolt = Path().apply {
        moveTo(center.x - size * 0.12f, center.y - size * 0.64f)
        lineTo(center.x + size * 0.42f, center.y - size * 0.64f)
        lineTo(center.x + size * 0.08f, center.y - size * 0.06f)
        lineTo(center.x + size * 0.46f, center.y - size * 0.06f)
        lineTo(center.x - size * 0.22f, center.y + size * 0.72f)
        lineTo(center.x - size * 0.03f, center.y + size * 0.16f)
        lineTo(center.x - size * 0.42f, center.y + size * 0.16f)
        close()
    }
    drawPath(bolt, color)
}
