package com.example.weathersimulator.ui.screens.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.R
import com.example.weathersimulator.data.repository.WeatherQuizQuestion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private val QuizBackground = Color(0xFF061625)
private val QuizBackgroundDeep = Color(0xFF02101C)
private val QuizSurface = Color(0xFF0A2437)
private val QuizSurfaceSoft = Color(0xFF10344D)
private val QuizBorder = Color(0xFF2D6D8F)
private val QuizCyan = Color(0xFF54D9FF)
private val QuizYellow = Color(0xFFFFCF54)
private val QuizGreen = Color(0xFF63E6A6)
private val QuizRed = Color(0xFFFF867A)
private val QuizMutedText = Color.White.copy(alpha = 0.72f)

@Composable
fun WeatherQuizScreen(
    onBack: () -> Unit,
    viewModel: WeatherQuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(containerColor = QuizBackground) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            QuizBackgroundDeep,
                            QuizBackground,
                            Color(0xFF0B3145)
                        )
                    )
                )
                .padding(padding)
        ) {
            QuizBackgroundPattern()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WeatherQuizHeader(
                    title = when (state.phase) {
                        WeatherQuizPhase.Setup -> "Meteo Quiz"
                        WeatherQuizPhase.Loading -> "Se incarca"
                        WeatherQuizPhase.Playing -> "Intrebarea ${state.currentQuestionIndex + 1}"
                        WeatherQuizPhase.Finished -> "Rezultat"
                    },
                    subtitle = "Teste meteo",
                    onBack = onBack
                )

                when (state.phase) {
                    WeatherQuizPhase.Setup -> WeatherQuizSetup(
                        state = state,
                        onQuestionCountSelected = viewModel::chooseQuestionCount,
                        onStartQuiz = { viewModel.startQuiz() }
                    )

                    WeatherQuizPhase.Loading -> WeatherQuizLoadingCard()

                    WeatherQuizPhase.Playing -> WeatherQuizPlaying(
                        state = state,
                        onAnswerSelected = viewModel::selectAnswer,
                        onNextQuestion = viewModel::goToNextQuestion,
                        onFinishQuiz = viewModel::finishQuiz
                    )

                    WeatherQuizPhase.Finished -> WeatherQuizResult(
                        state = state,
                        onRestart = { viewModel.startQuiz(state.requestedQuestionCount) },
                        onBackToSetup = viewModel::resetQuiz
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun WeatherQuizHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF102345).copy(alpha = 0.78f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Inapoi",
                tint = Color.White
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 27.sp,
                lineHeight = 30.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = QuizMutedText,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        QuizSectionLogoBadge()
    }
}

@Composable
private fun QuizSectionLogoBadge() {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        QuizCyan.copy(alpha = 0.30f),
                        Color(0xFF063E73).copy(alpha = 0.58f),
                        Color(0xFF081E3F)
                    )
                )
            )
            .border(BorderStroke(1.2.dp, QuizCyan.copy(alpha = 0.72f)), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(36.dp)) {
            val w = size.width
            val h = size.height
            val unit = size.minDimension

            drawCircle(
                color = QuizYellow,
                radius = unit * 0.20f,
                center = Offset(w * 0.40f, h * 0.34f)
            )
            drawCompactCloud(
                center = Offset(w * 0.46f, h * 0.56f),
                radius = unit * 0.11f,
                baseColor = Color(0xFFD8F5FF),
                shadowColor = Color(0xFF65BFEF)
            )
            drawLightning(
                center = Offset(w * 0.64f, h * 0.69f),
                size = unit * 0.18f
            )

            drawRoundRect(
                color = QuizCyan.copy(alpha = 0.88f),
                topLeft = Offset(w * 0.66f, h * 0.19f),
                size = Size(w * 0.14f, h * 0.06f),
                cornerRadius = CornerRadius(h * 0.05f, h * 0.05f)
            )
            drawRoundRect(
                color = QuizCyan.copy(alpha = 0.70f),
                topLeft = Offset(w * 0.66f, h * 0.30f),
                size = Size(w * 0.20f, h * 0.05f),
                cornerRadius = CornerRadius(h * 0.05f, h * 0.05f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.60f),
                radius = unit * 0.035f,
                center = Offset(w * 0.28f, h * 0.23f)
            )
        }
    }
}

@Composable
private fun WeatherQuizSetup(
    state: WeatherQuizUiState,
    onQuestionCountSelected: (Int) -> Unit,
    onStartQuiz: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        WeatherQuizHeroCard()

        QuizLengthSelectionCard(
            selectedCount = state.requestedQuestionCount,
            error = state.error,
            onQuestionCountSelected = onQuestionCountSelected,
            onStartQuiz = onStartQuiz
        )

        QuizReadyCard(selectedCount = state.requestedQuestionCount)
    }
}

@Composable
private fun WeatherQuizHeroCard() {
    Image(
        painter = painterResource(id = R.drawable.meteo_quiz_hero_card),
        contentDescription = "Meteo Quiz",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(792f / 570f)
            .clip(RoundedCornerShape(30.dp)),
        contentScale = ContentScale.FillBounds
    )
}

@Composable
private fun QuizTitleAccent() {
    Canvas(
        modifier = Modifier
            .padding(top = 3.dp, bottom = 6.dp)
            .size(width = 42.dp, height = 8.dp)
    ) {
        drawRoundRect(
            color = QuizCyan,
            topLeft = Offset(0f, size.height * 0.36f),
            size = Size(size.width * 0.66f, size.height * 0.24f),
            cornerRadius = CornerRadius(size.height, size.height)
        )
        drawCircle(
            color = QuizCyan,
            radius = size.height * 0.28f,
            center = Offset(size.width * 0.88f, size.height * 0.48f)
        )
    }
}

@Composable
private fun QuizHeroBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = QuizCyan.copy(alpha = 0.12f),
            radius = size.width * 0.44f,
            center = Offset(size.width * 0.88f, size.height * 0.46f)
        )
        drawCircle(
            color = Color(0xFF2D6DFF).copy(alpha = 0.10f),
            radius = size.width * 0.28f,
            center = Offset(size.width * 0.55f, size.height * 0.28f)
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.20f),
            radius = size.width * 0.36f,
            center = Offset(size.width * 0.18f, size.height * 1.03f)
        )

        repeat(4) { index ->
            val y = size.height * (0.10f + index * 0.18f)
            drawCircle(
                color = Color.White.copy(alpha = 0.035f),
                radius = size.width * (0.12f + index * 0.02f),
                center = Offset(size.width * (0.48f + index * 0.11f), y)
            )
        }
    }
}

@Composable
private fun QuizHeroWeatherIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val unit = min(w, h)
        val center = Offset(w * 0.56f, h * 0.48f)

        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    QuizCyan.copy(alpha = 0.24f),
                    Color(0xFF117BE8).copy(alpha = 0.13f),
                    Color.Transparent
                ),
                center = center,
                radius = unit * 0.70f
            ),
            radius = unit * 0.70f,
            center = center
        )
        drawCircle(
            color = QuizCyan.copy(alpha = 0.66f),
            radius = unit * 0.62f,
            center = center,
            style = Stroke(width = unit * 0.010f)
        )
        drawCircle(
            color = Color(0xFF5A9CFF).copy(alpha = 0.24f),
            radius = unit * 0.48f,
            center = center,
            style = Stroke(width = unit * 0.006f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.46f),
            radius = unit * 0.010f,
            center = Offset(w * 0.83f, h * 0.19f)
        )
        drawCircle(
            color = QuizCyan.copy(alpha = 0.72f),
            radius = unit * 0.012f,
            center = Offset(w * 0.21f, h * 0.71f)
        )

        drawSun(
            center = Offset(w * 0.43f, h * 0.25f),
            radius = unit * 0.135f
        )
        drawWeatherCloud(
            center = Offset(w * 0.34f, h * 0.42f),
            radius = unit * 0.087f,
            baseColor = Color(0xFFD8F3FF),
            shadowColor = Color(0xFF81BEF0)
        )
        drawWeatherCloud(
            center = Offset(w * 0.68f, h * 0.39f),
            radius = unit * 0.094f,
            baseColor = Color(0xFFEAF7FF),
            shadowColor = Color(0xFFB5D6FF)
        )
        drawWeatherCloud(
            center = Offset(w * 0.58f, h * 0.63f),
            radius = unit * 0.102f,
            baseColor = Color(0xFF7D90BD),
            shadowColor = Color(0xFF263353)
        )

        drawLightning(
            center = Offset(w * 0.55f, h * 0.78f),
            size = unit * 0.24f
        )

        drawRainDrop(Offset(w * 0.27f, h * 0.70f), unit * 0.040f)
        drawRainDrop(Offset(w * 0.78f, h * 0.57f), unit * 0.034f)
        drawRainDrop(Offset(w * 0.71f, h * 0.82f), unit * 0.038f)
        drawRainDrop(Offset(w * 0.89f, h * 0.74f), unit * 0.025f)

        drawWindLine(y = h * 0.74f, startX = w * 0.12f, endX = w * 0.39f)
        drawWindLine(y = h * 0.84f, startX = w * 0.18f, endX = w * 0.43f)

        drawSnowflake(center = Offset(w * 0.86f, h * 0.56f), radius = unit * 0.060f)
        drawSnowflake(center = Offset(w * 0.85f, h * 0.82f), radius = unit * 0.066f)
    }
}

@Composable
private fun QuizInfoChip(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF061B37).copy(alpha = 0.70f))
            .border(BorderStroke(1.dp, QuizBorder.copy(alpha = 0.86f)), RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
                .background(QuizCyan),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF06233A),
                modifier = Modifier.size(17.dp)
            )
        }
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun QuizLengthSelectionCard(
    selectedCount: Int,
    error: String?,
    onQuestionCountSelected: (Int) -> Unit,
    onStartQuiz: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = QuizSurface.copy(alpha = 0.94f)),
        border = BorderStroke(1.2.dp, QuizBorder.copy(alpha = 0.78f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(QuizCyan.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, QuizCyan.copy(alpha = 0.38f)), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    QuizClipboardIcon(
                        accent = QuizCyan,
                        selected = true,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column {
                    Text(
                        text = "Alege lungimea testului",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        lineHeight = 23.sp
                    )
                    QuizTitleAccent()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QuestionCountButton(
                    count = 5,
                    selected = selectedCount == 5,
                    label = "Rapid",
                    icon = Icons.Rounded.Bolt,
                    accent = QuizCyan,
                    onClick = { onQuestionCountSelected(5) },
                    modifier = Modifier.weight(1f)
                )
                QuestionCountButton(
                    count = 10,
                    selected = selectedCount == 10,
                    label = "Complet",
                    icon = Icons.Rounded.Star,
                    accent = Color(0xFF8A6CFF),
                    onClick = { onQuestionCountSelected(10) },
                    modifier = Modifier.weight(1f)
                )
            }

            if (error != null) {
                Text(
                    text = error,
                    color = QuizRed,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }

            QuizStartButton(
                onClick = onStartQuiz
            )
        }
    }
}

@Composable
private fun QuestionCountButton(
    count: Int,
    selected: Boolean,
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) QuizCyan else QuizBorder.copy(alpha = 0.52f)
    val labelColor = if (selected) Color.White else Color.White.copy(alpha = 0.76f)
    val tileBrush = if (selected) {
        Brush.linearGradient(
            listOf(
                Color(0xFF12CFFF).copy(alpha = 0.92f),
                Color(0xFF0B68C8).copy(alpha = 0.72f),
                Color(0xFF0A2347)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xFF0D2B51).copy(alpha = 0.72f),
                Color(0xFF081E3B).copy(alpha = 0.88f),
                Color(0xFF06172D)
            )
        )
    }

    Box(
        modifier = modifier
            .height(168.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(tileBrush)
            .border(BorderStroke(if (selected) 1.8.dp else 1.dp, borderColor), RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (selected) {
                drawCircle(
                    color = QuizCyan.copy(alpha = 0.20f),
                    radius = size.width * 0.78f,
                    center = Offset(size.width * 0.18f, size.height * 0.08f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.88f, size.height * 0.06f)
                )
            }
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(31.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF0B66A8),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = if (selected) 0.30f else 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                QuizClipboardIcon(
                    accent = if (selected) Color(0xFFB9F5FF) else accent,
                    selected = selected,
                    modifier = Modifier.size(35.dp)
                )
            }

            Spacer(Modifier.height(15.dp))

            Text(
                text = count.toString(),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                lineHeight = 36.sp
            )
            Text(
                text = "intrebari",
                color = labelColor,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(13.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0B3F73).copy(alpha = if (selected) 0.70f else 0.42f))
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun QuizClipboardIcon(
    accent: Color,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = Color.Black.copy(alpha = 0.18f),
            topLeft = Offset(w * 0.24f, h * 0.24f),
            size = Size(w * 0.58f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.11f, w * 0.11f)
        )
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(
                    accent.copy(alpha = if (selected) 0.92f else 0.74f),
                    Color(0xFF6AA7FF).copy(alpha = if (selected) 0.82f else 0.58f),
                    Color(0xFF274276).copy(alpha = 0.86f)
                )
            ),
            topLeft = Offset(w * 0.20f, h * 0.20f),
            size = Size(w * 0.58f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.11f, w * 0.11f)
        )
        drawRoundRect(
            color = Color.White.copy(alpha = if (selected) 0.30f else 0.16f),
            topLeft = Offset(w * 0.28f, h * 0.16f),
            size = Size(w * 0.42f, h * 0.18f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        drawCircle(
            color = accent.copy(alpha = 0.96f),
            radius = w * 0.07f,
            center = Offset(w * 0.49f, h * 0.18f)
        )

        repeat(3) { index ->
            val y = h * (0.43f + index * 0.14f)
            drawCircle(
                color = Color(0xFF09233E).copy(alpha = 0.70f),
                radius = w * 0.035f,
                center = Offset(w * 0.34f, y)
            )
            drawRoundRect(
                color = Color(0xFF09233E).copy(alpha = 0.58f),
                topLeft = Offset(w * 0.43f, y - h * 0.020f),
                size = Size(w * 0.20f, h * 0.040f),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
            )
        }

        drawCircle(
            color = Color.White.copy(alpha = 0.36f),
            radius = w * 0.06f,
            center = Offset(w * 0.34f, h * 0.30f)
        )
    }
}

@Composable
private fun QuizStartButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFE777),
                        Color(0xFFFFBE3D),
                        Color(0xFFFFA931)
                    )
                )
            )
            .border(BorderStroke(1.2.dp, Color(0xFFFFF094)), RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSparkle(Offset(size.width * 0.92f, size.height * 0.32f), size.minDimension * 0.18f)
            drawSparkle(Offset(size.width * 0.96f, size.height * 0.18f), size.minDimension * 0.08f)
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = size.width * 0.46f,
                center = Offset(size.width * 0.06f, size.height * 1.24f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14233C).copy(alpha = 0.90f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = QuizYellow,
                    modifier = Modifier.size(29.dp)
                )
            }

            Spacer(Modifier.size(16.dp))

            Text(
                text = "Start quiz",
                color = Color(0xFF08233A),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun QuizReadyCard(selectedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = QuizSurface.copy(alpha = 0.86f)),
        border = BorderStroke(1.dp, QuizBorder.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                QuizCyan.copy(alpha = 0.34f),
                                Color(0xFF0A3C74).copy(alpha = 0.56f),
                                Color(0xFF06172D)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.WbSunny,
                    contentDescription = null,
                    tint = QuizYellow,
                    modifier = Modifier.size(29.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.Cloud,
                    contentDescription = null,
                    tint = Color(0xFFB9EFFF),
                    modifier = Modifier
                        .padding(top = 22.dp)
                        .size(45.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Esti gata sa inveti?",
                    color = QuizCyan,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    lineHeight = 21.sp
                )
                Text(
                    text = "Testul tau de $selectedCount intrebari incepe acum!",
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }

            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = QuizCyan,
                modifier = Modifier.size(55.dp)
            )
        }
    }
}

private fun DrawScope.drawSun(
    center: Offset,
    radius: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                QuizYellow.copy(alpha = 0.42f),
                QuizYellow.copy(alpha = 0.10f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 2.55f
        ),
        radius = radius * 2.55f,
        center = center
    )

    repeat(14) { index ->
        val angle = (index / 14.0) * PI * 2.0
        val start = Offset(
            x = center.x + cos(angle).toFloat() * radius * 1.18f,
            y = center.y + sin(angle).toFloat() * radius * 1.18f
        )
        val end = Offset(
            x = center.x + cos(angle).toFloat() * radius * 1.72f,
            y = center.y + sin(angle).toFloat() * radius * 1.72f
        )
        drawLine(
            color = QuizYellow.copy(alpha = 0.86f),
            start = start,
            end = end,
            strokeWidth = radius * 0.17f
        )
    }

    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color(0xFFFFF4A6),
                QuizYellow,
                Color(0xFFFF9E2E),
                Color(0xFFFF7E1F)
            ),
            center = Offset(center.x - radius * 0.20f, center.y - radius * 0.24f),
            radius = radius
        ),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.36f),
        radius = radius * 0.24f,
        center = Offset(center.x - radius * 0.30f, center.y - radius * 0.34f)
    )
}

private fun DrawScope.drawWeatherCloud(
    center: Offset,
    radius: Float,
    baseColor: Color,
    shadowColor: Color
) {
    drawCircle(
        color = Color.Black.copy(alpha = 0.16f),
        radius = radius * 2.05f,
        center = Offset(center.x + radius * 0.15f, center.y + radius * 0.42f)
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(
                baseColor.copy(alpha = 0.96f),
                shadowColor.copy(alpha = 0.92f)
            )
        ),
        topLeft = Offset(center.x - radius * 1.74f, center.y - radius * 0.12f),
        size = Size(radius * 3.48f, radius * 1.04f),
        cornerRadius = CornerRadius(radius * 0.72f, radius * 0.72f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color.White.copy(alpha = 0.72f),
                baseColor,
                shadowColor
            ),
            center = Offset(center.x - radius * 1.14f, center.y - radius * 0.36f),
            radius = radius * 1.30f
        ),
        radius = radius * 0.95f,
        center = Offset(center.x - radius * 0.90f, center.y)
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color.White.copy(alpha = 0.82f),
                baseColor,
                shadowColor.copy(alpha = 0.90f)
            ),
            center = Offset(center.x - radius * 0.32f, center.y - radius * 0.84f),
            radius = radius * 1.42f
        ),
        radius = radius * 1.20f,
        center = Offset(center.x, center.y - radius * 0.38f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(
                Color.White.copy(alpha = 0.62f),
                baseColor.copy(alpha = 0.96f),
                shadowColor.copy(alpha = 0.86f)
            ),
            center = Offset(center.x + radius * 0.70f, center.y - radius * 0.36f),
            radius = radius * 1.14f
        ),
        radius = radius * 0.92f,
        center = Offset(center.x + radius * 1.00f, center.y + radius * 0.02f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.32f),
        radius = radius * 0.32f,
        center = Offset(center.x - radius * 0.18f, center.y - radius * 0.74f)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.10f),
        topLeft = Offset(center.x - radius * 1.32f, center.y + radius * 0.28f),
        size = Size(radius * 2.62f, radius * 0.20f),
        cornerRadius = CornerRadius(radius * 0.18f, radius * 0.18f)
    )
}

private fun DrawScope.drawCompactCloud(
    center: Offset,
    radius: Float,
    baseColor: Color,
    shadowColor: Color
) {
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(baseColor, shadowColor)
        ),
        topLeft = Offset(center.x - radius * 1.60f, center.y - radius * 0.04f),
        size = Size(radius * 3.20f, radius * 1.00f),
        cornerRadius = CornerRadius(radius * 0.62f, radius * 0.62f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.72f), baseColor, shadowColor),
            center = Offset(center.x - radius * 0.42f, center.y - radius * 0.52f),
            radius = radius * 1.28f
        ),
        radius = radius,
        center = Offset(center.x - radius * 0.58f, center.y)
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.84f), baseColor, shadowColor),
            center = Offset(center.x, center.y - radius * 0.74f),
            radius = radius * 1.35f
        ),
        radius = radius * 1.18f,
        center = Offset(center.x + radius * 0.25f, center.y - radius * 0.28f)
    )
}

private fun DrawScope.drawLightning(
    center: Offset,
    size: Float
) {
    val bolt = Path().apply {
        moveTo(center.x - size * 0.12f, center.y - size * 0.60f)
        lineTo(center.x + size * 0.42f, center.y - size * 0.60f)
        lineTo(center.x + size * 0.06f, center.y - size * 0.04f)
        lineTo(center.x + size * 0.44f, center.y - size * 0.04f)
        lineTo(center.x - size * 0.24f, center.y + size * 0.68f)
        lineTo(center.x - size * 0.04f, center.y + size * 0.14f)
        lineTo(center.x - size * 0.42f, center.y + size * 0.14f)
        close()
    }

    drawPath(
        path = bolt,
        brush = Brush.linearGradient(
            listOf(
                Color(0xFFFFF69B),
                QuizYellow,
                Color(0xFFFF8E24)
            )
        )
    )
}

private fun DrawScope.drawRainDrop(
    center: Offset,
    radius: Float
) {
    val drop = Path().apply {
        moveTo(center.x, center.y - radius * 1.35f)
        cubicTo(
            center.x - radius * 1.0f,
            center.y - radius * 0.34f,
            center.x - radius * 0.84f,
            center.y + radius * 0.84f,
            center.x,
            center.y + radius * 1.08f
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
                Color.White.copy(alpha = 0.86f),
                QuizCyan,
                Color(0xFF1476FF)
            ),
            center = Offset(center.x - radius * 0.24f, center.y - radius * 0.42f),
            radius = radius * 1.80f
        )
    )
}

private fun DrawScope.drawWindLine(
    y: Float,
    startX: Float,
    endX: Float
) {
    val path = Path().apply {
        moveTo(startX, y)
        cubicTo(
            startX + (endX - startX) * 0.26f,
            y - size.height * 0.05f,
            startX + (endX - startX) * 0.48f,
            y + size.height * 0.03f,
            endX,
            y
        )
    }
    drawPath(
        path = path,
        color = Color(0xFFB8EAFF).copy(alpha = 0.86f),
        style = Stroke(width = size.minDimension * 0.010f)
    )
}

private fun DrawScope.drawSnowflake(
    center: Offset,
    radius: Float
) {
    repeat(6) { index ->
        val angle = (index / 6.0) * PI * 2.0
        val end = Offset(
            center.x + cos(angle).toFloat() * radius,
            center.y + sin(angle).toFloat() * radius
        )
        drawLine(
            color = Color(0xFFBDEEFF).copy(alpha = 0.90f),
            start = center,
            end = end,
            strokeWidth = radius * 0.12f
        )
        drawCircle(
            color = Color(0xFFBDEEFF).copy(alpha = 0.88f),
            radius = radius * 0.10f,
            center = end
        )
    }
}

private fun DrawScope.drawSparkle(
    center: Offset,
    radius: Float
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        cubicTo(center.x + radius * 0.12f, center.y - radius * 0.22f, center.x + radius * 0.22f, center.y - radius * 0.12f, center.x + radius, center.y)
        cubicTo(center.x + radius * 0.22f, center.y + radius * 0.12f, center.x + radius * 0.12f, center.y + radius * 0.22f, center.x, center.y + radius)
        cubicTo(center.x - radius * 0.12f, center.y + radius * 0.22f, center.x - radius * 0.22f, center.y + radius * 0.12f, center.x - radius, center.y)
        cubicTo(center.x - radius * 0.22f, center.y - radius * 0.12f, center.x - radius * 0.12f, center.y - radius * 0.22f, center.x, center.y - radius)
        close()
    }
    drawPath(path, Color.White.copy(alpha = 0.86f))
}

@Composable
private fun WeatherQuizLoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = QuizSurface.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, QuizBorder.copy(alpha = 0.60f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = QuizCyan,
                trackColor = Color.White.copy(alpha = 0.14f)
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Aleg intrebari random din Firebase...",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
            Text(
                text = "Incerc sa evit intrebarile primite recent.",
                color = QuizMutedText,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun WeatherQuizPlaying(
    state: WeatherQuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onFinishQuiz: () -> Unit
) {
    val question = state.currentQuestion ?: return
    val progress = (state.currentQuestionIndex + 1) / max(1, state.questions.size).toFloat()
    val selectedIndex = state.selectedAnswerFor(question)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        WeatherQuizProgressCard(
            state = state,
            progress = progress
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = QuizSurface.copy(alpha = 0.96f)),
            border = BorderStroke(1.dp, QuizBorder.copy(alpha = 0.64f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuestionMetaRow(question = question)

                Text(
                    text = question.question,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 23.sp,
                    lineHeight = 29.sp
                )

                question.answers.forEachIndexed { index, answer ->
                    WeatherQuizAnswerOption(
                        index = index,
                        answer = answer,
                        selectedIndex = selectedIndex,
                        correctIndex = question.correctIndex,
                        onClick = { onAnswerSelected(index) }
                    )
                }

                Button(
                    onClick = {
                        if (state.isLastQuestion) {
                            onFinishQuiz()
                        } else {
                            onNextQuestion()
                        }
                    },
                    enabled = selectedIndex != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QuizYellow,
                        contentColor = Color(0xFF2A1C00),
                        disabledContainerColor = QuizSurfaceSoft,
                        disabledContentColor = QuizMutedText
                    )
                ) {
                    Text(
                        text = if (state.isLastQuestion) "Vezi scorul" else "Urmatoarea intrebare",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherQuizProgressCard(
    state: WeatherQuizUiState,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = QuizSurface.copy(alpha = 0.86f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progres",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${state.currentQuestionIndex + 1}/${state.questions.size}",
                    color = QuizCyan,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = QuizCyan,
                trackColor = Color.White.copy(alpha = 0.16f)
            )
        }
    }
}

@Composable
private fun QuestionMetaRow(question: WeatherQuizQuestion) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (question.category.isNotBlank()) {
            QuizTinyChip(
                text = question.category,
                accent = QuizCyan
            )
        }
        if (question.difficulty.isNotBlank()) {
            QuizTinyChip(
                text = question.difficulty,
                accent = QuizYellow
            )
        }
    }
}

@Composable
private fun QuizTinyChip(
    text: String,
    accent: Color
) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.14f))
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.34f)), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun WeatherQuizAnswerOption(
    index: Int,
    answer: String,
    selectedIndex: Int?,
    correctIndex: Int,
    onClick: () -> Unit
) {
    val isAnswered = selectedIndex != null
    val isSelected = selectedIndex == index
    val isCorrect = correctIndex == index
    val borderColor = when {
        isAnswered && isCorrect -> QuizGreen
        isAnswered && isSelected -> QuizRed
        isSelected -> QuizCyan
        else -> QuizBorder.copy(alpha = 0.58f)
    }
    val background = when {
        isAnswered && isCorrect -> QuizGreen.copy(alpha = 0.15f)
        isAnswered && isSelected -> QuizRed.copy(alpha = 0.14f)
        isSelected -> QuizCyan.copy(alpha = 0.13f)
        else -> Color.White.copy(alpha = 0.07f)
    }
    val label = when (index) {
        0 -> "A"
        1 -> "B"
        else -> "C"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(20.dp))
            .clickable(enabled = !isAnswered) { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(borderColor.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }

        Text(
            text = answer,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )

        if (isAnswered && (isCorrect || isSelected)) {
            Icon(
                imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (isCorrect) QuizGreen else QuizRed,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun WeatherQuizResult(
    state: WeatherQuizUiState,
    onRestart: () -> Unit,
    onBackToSetup: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = QuizSurface.copy(alpha = 0.96f)),
            border = BorderStroke(1.dp, QuizBorder.copy(alpha = 0.64f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(QuizYellow.copy(alpha = 0.16f))
                        .border(BorderStroke(1.dp, QuizYellow.copy(alpha = 0.44f)), RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = QuizYellow,
                        modifier = Modifier.size(47.dp)
                    )
                }

                Text(
                    text = "Scor: ${state.score}/${state.questions.size}",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp,
                    lineHeight = 34.sp
                )

                Text(
                    text = resultMessage(state.score, state.questions.size),
                    color = QuizMutedText,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackToSetup,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        border = BorderStroke(1.dp, QuizBorder.copy(alpha = 0.70f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Inapoi",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onRestart,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = QuizYellow,
                            contentColor = Color(0xFF2A1C00)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Replay,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "Din nou",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        WrongAnswerReview(state = state)
    }
}

@Composable
private fun WrongAnswerReview(state: WeatherQuizUiState) {
    val wrongAnswers = state.wrongAnswers

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = QuizSurface.copy(alpha = 0.90f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (wrongAnswers.isEmpty()) "Toate raspunsurile au fost corecte" else "Raspunsuri de revizuit",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )

            if (wrongAnswers.isEmpty()) {
                Text(
                    text = "Perfect. Ai parcurs testul fara greseli.",
                    color = QuizMutedText,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            } else {
                wrongAnswers.forEach { question ->
                    WrongAnswerItem(
                        question = question,
                        selectedIndex = state.selectedAnswerFor(question)
                    )
                }
            }
        }
    }
}

@Composable
private fun WrongAnswerItem(
    question: WeatherQuizQuestion,
    selectedIndex: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(BorderStroke(1.dp, QuizBorder.copy(alpha = 0.38f)), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = question.question,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 20.sp
        )
        Text(
            text = "Ai ales: ${selectedIndex?.let { question.answers.getOrNull(it) } ?: "--"}",
            color = QuizRed,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Text(
            text = "Corect: ${question.answers[question.correctIndex]}",
            color = QuizGreen,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Text(
            text = question.explanation,
            color = QuizMutedText,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun QuizBackgroundPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = QuizCyan.copy(alpha = 0.10f),
            radius = size.width * 0.54f,
            center = Offset(size.width * 1.02f, size.height * 0.08f)
        )
        drawCircle(
            color = QuizYellow.copy(alpha = 0.08f),
            radius = size.width * 0.42f,
            center = Offset(size.width * 0.02f, size.height * 0.84f)
        )
    }
}

private val Size.minDimension: Float
    get() = min(width, height)

private fun resultMessage(score: Int, total: Int): String {
    if (total <= 0) return "Test finalizat."
    val ratio = score / total.toFloat()

    return when {
        ratio >= 0.9f -> "Excelent. Ai prins foarte bine conceptele meteo."
        ratio >= 0.7f -> "Foarte bine. Mai sunt doar cateva detalii de fixat."
        ratio >= 0.5f -> "Bun inceput. Explicatiile de mai jos te ajuta sa inchizi golurile."
        else -> "Merita reluat testul. Fiecare raspuns gresit are o explicatie scurta."
    }
}
