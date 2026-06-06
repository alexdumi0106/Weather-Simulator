package com.example.weathersimulator.ui.screens.games.cloudcatcher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.R

@Composable
fun CloudCatcherScreen(
    onBack: () -> Unit,
    viewModel: CloudCatcherViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.phase == CloudCatcherPhase.Ready) {
        CloudCatcherIntroScreen(
            bestScore = state.bestScore,
            onBack = onBack,
            onStartClick = viewModel::startGame
        )
        return
    }

    LaunchedEffect(state.phase, state.roundId) {
        if (state.phase != CloudCatcherPhase.Running) return@LaunchedEffect

        var lastFrameNanos = 0L
        while (viewModel.state.value.phase == CloudCatcherPhase.Running) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = frameNanos
                    return@withFrameNanos
                }

                val deltaSeconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
                lastFrameNanos = frameNanos
                viewModel.step(deltaSeconds)
            }
        }
    }

    CloudCatcherPlayScreen(
        state = state,
        onPlaneYChange = viewModel::movePlane,
        onPauseClick = viewModel::pauseGame,
        onResumeClick = viewModel::resumeGame,
        onBack = onBack,
        onRestartClick = viewModel::startGame
    )
}

@Composable
private fun CloudCatcherIntroScreen(
    bestScore: Int,
    onBack: () -> Unit,
    onStartClick: () -> Unit
) {
    Scaffold(
        containerColor = CloudCatcherBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF020918),
                            Color(0xFF05182D),
                            Color(0xFF05233C)
                        )
                    )
                )
                .padding(padding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1C5BAA).copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.90f, size.height * 0.20f),
                        radius = size.width * 0.95f
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                CloudCatcherHeaderImage(onBack = onBack)

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    CloudCatcherIntroStats(bestScore = bestScore)

                    Spacer(Modifier.height(24.dp))

                    CloudCatcherIntroHero(onStartClick = onStartClick)

                    Spacer(Modifier.height(28.dp))

                    CloudCatcherIntroImage(
                        drawableRes = R.drawable.cloud_catcher_intro_how,
                        contentDescription = "Cum functioneaza Cloud Catcher",
                        aspectRatio = 542f / 218f
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CloudCatcherIntroStats(
    bestScore: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(542f / 97f)
            .clip(RoundedCornerShape(23.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF071B36),
                        Color(0xFF06152E),
                        Color(0xFF06162F)
                    )
                )
            )
            .border(BorderStroke(1.dp, Color(0xFF3981D8).copy(alpha = 0.78f)), RoundedCornerShape(23.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CloudCatcherIntroStatTile(
            icon = Icons.Rounded.Star,
            label = "Record",
            value = bestScore.scoreText(),
            accent = CloudCatcherYellow,
            modifier = Modifier.weight(1f)
        )
        CloudCatcherIntroStatTile(
            icon = Icons.Rounded.Favorite,
            label = "Vieti",
            value = StartingLives.toString(),
            accent = Color(0xFFFF6FA1),
            modifier = Modifier.weight(1f)
        )
        CloudCatcherIntroStatTile(
            icon = Icons.Rounded.Timer,
            label = "Timp",
            value = "60s",
            accent = Color(0xFF5DA7FF),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CloudCatcherIntroStatTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(17.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = 0.18f),
                        Color(0xFF0A2449).copy(alpha = 0.72f)
                    )
                )
            )
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(31.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.24f))
                .border(BorderStroke(1.dp, accent.copy(alpha = 0.55f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 9.sp,
                lineHeight = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                lineHeight = 17.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CloudCatcherHeaderImage(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(599f / 128f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.cloud_catcher_intro_header),
            contentDescription = "Cloud Catcher",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .clickable { onBack() }
        )
    }
}

@Composable
private fun CloudCatcherIntroHero(
    onStartClick: () -> Unit
) {
    Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1059f / 1485f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.cloud_catcher_intro_hero),
            contentDescription = "Cloud Catcher start",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        CloudCatcherStartHotspot(onStartClick = onStartClick)
    }
}

@Composable
private fun CloudCatcherStartHotspot(
    onStartClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.weight(0.859f))
        Row(modifier = Modifier.weight(0.102f)) {
            Spacer(modifier = Modifier.weight(0.073f))
            Box(
                modifier = Modifier
                    .weight(0.862f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(44.dp))
                    .clickable { onStartClick() }
            )
            Spacer(modifier = Modifier.weight(0.065f))
        }
        Spacer(modifier = Modifier.weight(0.039f))
    }
}

@Composable
private fun CloudCatcherIntroImage(
    drawableRes: Int,
    contentDescription: String,
    aspectRatio: Float
) {
    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio),
        contentScale = ContentScale.FillBounds
    )
}

@Composable
private fun CloudCatcherPlayScreen(
    state: CloudCatcherGameState,
    onPlaneYChange: (Float) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onBack: () -> Unit,
    onRestartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF041021))
    ) {
        Image(
            painter = painterResource(id = R.drawable.cloud_catcher_game_background),
            contentDescription = "Cloud Catcher gameplay",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        onPlaneYChange(change.position.y / size.height)
                    }
                }
        ) {
            drawCloudCatcherInteractiveLayer(
                planeY = state.planeY,
                objects = state.objects
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CloudCatcherGameHud(
                phase = state.phase,
                score = state.score,
                lives = state.lives,
                remainingMillis = state.remainingMillis,
                onPauseClick = onPauseClick,
                onExitClick = onBack
            )

            CloudCatcherProgressBar(
                progress = state.progress
            )
        }

        CloudCatcherControls(
            onUpClick = { onPlaneYChange(state.planeY - 0.08f) },
            onDownClick = { onPlaneYChange(state.planeY + 0.08f) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 230.dp)
        )

        CloudCatcherGameLegend(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 14.dp, end = 14.dp, bottom = 18.dp)
        )

        if (state.phase == CloudCatcherPhase.Finished) {
            CloudCatcherOverlay(
                phase = state.phase,
                score = state.score,
                lives = state.lives,
                onStartClick = onRestartClick,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (state.phase == CloudCatcherPhase.Paused) {
            CloudCatcherPauseDialog(
                onContinueClick = onResumeClick,
                onExitClick = onBack,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun CloudCatcherGameHud(
    phase: CloudCatcherPhase,
    score: Int,
    lives: Int,
    remainingMillis: Long,
    onPauseClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(
            onClick = {
                when (phase) {
                    CloudCatcherPhase.Running -> onPauseClick()
                    CloudCatcherPhase.Finished -> onExitClick()
                    else -> Unit
                }
            },
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF112A4C).copy(alpha = 0.70f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)), CircleShape)
        ) {
            if (phase == CloudCatcherPhase.Finished) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Iesi din joc",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(
                    text = "II",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(74.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF071D39).copy(alpha = 0.72f))
                .border(BorderStroke(1.dp, Color(0xFF6EA7FF).copy(alpha = 0.42f)), RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scor",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = score.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(Color.White.copy(alpha = 0.13f))
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vieti",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(StartingLives) { index ->
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = if (index < lives) CloudCatcherRed else Color.White.copy(alpha = 0.28f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .height(74.dp)
                .width(96.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF071D39).copy(alpha = 0.72f))
                .border(BorderStroke(1.dp, Color(0xFF6EA7FF).copy(alpha = 0.42f)), RoundedCornerShape(22.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(27.dp)
            )
            Column {
                Text(
                    text = "Timp",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = remainingMillis.secondsText(),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CloudCatcherProgressBar(
    progress: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF07182E).copy(alpha = 0.72f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF61A9FF),
                                CloudCatcherCyan,
                                Color(0xFF9C50FF)
                            )
                        )
                    )
            )
        }

        Icon(
            imageVector = Icons.Rounded.Flag,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CloudCatcherControls(
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onUpClick,
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFF082B46).copy(alpha = 0.78f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = "Sus",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
        IconButton(
            onClick = onDownClick,
            modifier = Modifier
                .size(54.dp)
                .background(Color(0xFF082B46).copy(alpha = 0.78f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Jos",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun CloudCatcherGameLegend(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF071D39).copy(alpha = 0.78f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text(
                text = "Fenomene in joc",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CloudCatcherLegendTile(
                    type = CloudCatcherObjectType.WhiteCloud,
                    title = "Nor alb",
                    value = "+10",
                    valueColor = CloudCatcherCyan,
                    modifier = Modifier.weight(1f)
                )
                CloudCatcherLegendTile(
                    type = CloudCatcherObjectType.RainDrop,
                    title = "Picatura",
                    value = "+5",
                    valueColor = CloudCatcherCyan,
                    modifier = Modifier.weight(1f)
                )
                CloudCatcherLegendTile(
                    type = CloudCatcherObjectType.Lightning,
                    title = "Fulger",
                    value = "-5",
                    valueColor = CloudCatcherRed,
                    modifier = Modifier.weight(1f)
                )
                CloudCatcherLegendTile(
                    type = CloudCatcherObjectType.StormCloud,
                    title = "Nor negru",
                    value = "-1 viata",
                    valueColor = CloudCatcherRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CloudCatcherLegendTile(
    type: CloudCatcherObjectType,
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF102A4E).copy(alpha = 0.78f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), RoundedCornerShape(16.dp))
            .padding(horizontal = 4.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Canvas(
                modifier = Modifier.size(52.dp)
            ) {
                val objectRadius = when (type) {
                    CloudCatcherObjectType.WhiteCloud -> 0.24f
                    CloudCatcherObjectType.RainDrop -> 0.24f
                    CloudCatcherObjectType.Lightning -> 0.25f
                    CloudCatcherObjectType.StormCloud -> 0.24f
                }
                drawCloudCatcherObject(
                    CloudCatcherObject(
                        id = 0L,
                        type = type,
                        x = 0.50f,
                        y = 0.48f,
                        speed = 0f,
                        radius = objectRadius
                    )
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CloudCatcherPauseDialog(
    onContinueClick: () -> Unit,
    onExitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dialogShape = RoundedCornerShape(30.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.34f))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(dialogShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF12314E).copy(alpha = 0.97f),
                            Color(0xFF071A2B).copy(alpha = 0.98f),
                            Color(0xFF03101D).copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    BorderStroke(1.dp, Color(0xFF75C8FF).copy(alpha = 0.34f)),
                    dialogShape
                )
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    CloudCatcherYellow.copy(alpha = 0.42f),
                                    Color(0xFF153B58).copy(alpha = 0.88f)
                                )
                            )
                        )
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "II",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                }

                Text(
                    text = "Joc in pauza",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Doresti sa inchei sesiunea de joc?",
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onExitClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "Iesi din joc",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onContinueClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CloudCatcherYellow,
                            contentColor = Color(0xFF111827)
                        )
                    ) {
                        Text(
                            text = "Continua",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudCatcherOverlay(
    phase: CloudCatcherPhase,
    score: Int,
    lives: Int,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lostLives = phase == CloudCatcherPhase.Finished && lives <= 0
    val overlayShape = RoundedCornerShape(30.dp)
    val iconTint = if (lostLives) CloudCatcherRed else CloudCatcherYellow

    Box(
        modifier = modifier
            .fillMaxWidth(0.88f)
            .clip(overlayShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF102B45).copy(alpha = 0.96f),
                        Color(0xFF071827).copy(alpha = 0.97f),
                        Color(0xFF03101D).copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                BorderStroke(1.dp, Color(0xFF75C8FF).copy(alpha = 0.34f)),
                overlayShape
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                iconTint.copy(alpha = 0.46f),
                                Color(0xFF103A57).copy(alpha = 0.82f)
                            )
                        )
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (lostLives) Icons.Rounded.Bolt else Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = "Runda incheiata",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF071D39).copy(alpha = 0.72f))
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Scor final",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    Text(
                        text = score.toString(),
                        color = CloudCatcherYellow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 38.sp,
                        lineHeight = 40.sp,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = if (lostLives) {
                    "Ai pierdut toate vietile. Evita norii de furtuna si fulgerele."
                } else {
                    "Timpul s-a incheiat. Ai colectat cat mai multe fenomene bune."
                },
                color = Color.White.copy(alpha = 0.76f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CloudCatcherYellow,
                    contentColor = Color(0xFF111827)
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = "Joaca din nou",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }
    }
}
