package com.example.weathersimulator.ui.screens.games

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersimulator.R

private val GamesBackground = Color(0xFF061625)
private val GamesBackgroundDeep = Color(0xFF03111F)
private val GamesCyan = Color(0xFF4ED7FF)
private val GamesMutedText = Color.White.copy(alpha = 0.70f)

@Composable
fun WeatherGamesScreen(
    onBack: () -> Unit,
    onCloudCatcherClick: () -> Unit,
    onWeatherMemoryClick: () -> Unit
) {
    Scaffold(
        containerColor = GamesBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GamesBackgroundDeep,
                            GamesBackground,
                            Color(0xFF0B3145)
                        )
                    )
                )
                .padding(padding)
        ) {
            GamesBackgroundPattern()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 17.dp)
            ) {
                WeatherGamesHeader(onBack = onBack)

                WeatherGamesReferenceImage(
                    drawableRes = R.drawable.weather_games_hero_card,
                    contentDescription = "Weather Games",
                    aspectRatio = 820f / 418f
                )

                AdventureTitle()

                WeatherGamesReferenceImage(
                    drawableRes = R.drawable.weather_games_cloud_catcher_card,
                    contentDescription = "Cloud Catcher",
                    aspectRatio = 820f / 380f,
                    onClick = onCloudCatcherClick
                )

                Spacer(Modifier.height(15.dp))

                WeatherMemoryHubCard(
                    onClick = onWeatherMemoryClick
                )

                Spacer(Modifier.height(15.dp))

                WeatherGamesReferenceImage(
                    drawableRes = R.drawable.weather_games_next_card,
                    contentDescription = "Urmatorul joc",
                    aspectRatio = 820f / 120f
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun GamesBackgroundPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF183E8E).copy(alpha = 0.28f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.95f, size.height * 0.04f),
                radius = size.width * 0.86f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF7A2CFF).copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.84f, size.height * 0.55f),
                radius = size.width * 0.86f
            )
        )
        drawCircle(
            color = GamesCyan.copy(alpha = 0.07f),
            radius = size.width * 0.44f,
            center = Offset(size.width * 0.02f, size.height * 0.30f)
        )
    }
}

@Composable
private fun WeatherGamesHeader(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(106.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(58.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Inapoi",
                tint = Color.White,
                modifier = Modifier.size(43.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jocuri meteo",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 30.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Mini-jocuri despre vreme",
                color = Color.White.copy(alpha = 0.66f),
                fontSize = 20.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun AdventureTitle() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Alege aventura",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            lineHeight = 28.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .fillMaxWidth(0.14f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GamesCyan)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GamesCyan)
            )
        }
        Text(
            text = "Testeaza-ti reflexele si memoria cu jocuri rapide inspirate de vreme.",
            color = GamesMutedText,
            fontSize = 14.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun WeatherGamesReferenceImage(
    @DrawableRes drawableRes: Int,
    contentDescription: String,
    aspectRatio: Float,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(27.dp)
    val baseModifier = Modifier
        .fillMaxWidth()
        .aspectRatio(aspectRatio)
        .clip(shape)

    Box(
        modifier = if (onClick != null) {
            baseModifier.clickable { onClick() }
        } else {
            baseModifier
        }
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
private fun WeatherMemoryHubCard(
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(27.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1744f / 902f)
            .clip(shape)
            .background(Color(0xFF071334))
    ) {
        Image(
            painter = painterResource(id = R.drawable.weather_memory_card),
            contentDescription = "Weather Memory",
            modifier = Modifier
                .fillMaxSize()
                .border(BorderStroke(1.dp, GamesCyan.copy(alpha = 0.76f)), shape),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onClick) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width * 0.179f
                        val centerY = size.height * 0.848f
                        val radius = size.height * 0.108f
                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY

                        if (dx * dx + dy * dy <= radius * radius) {
                            onClick()
                        }
                    }
                }
        )
    }
}
