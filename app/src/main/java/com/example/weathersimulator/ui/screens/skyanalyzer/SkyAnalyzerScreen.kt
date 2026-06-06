package com.example.weathersimulator.ui.screens.skyanalyzer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weathersimulator.R
import kotlin.math.roundToInt

private val ScreenBackground = Color(0xFF061829)
private val CardBackground = Color(0xFF071E35)
private val CardBackgroundSoft = Color(0xFF092742)
private val CardBorder = Color(0xFF1D5D8C)
private val CyanAccent = Color(0xFF37D6F5)
private val BlueAccent = Color(0xFF55A8FF)
private val PurpleAccent = Color(0xFFA85CFF)
private val GreenAccent = Color(0xFF32D889)
private val YellowAccent = Color(0xFFFFC43D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyAnalyzerScreen(
    onBack: () -> Unit,
    vm: SkyAnalyzerViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            vm.analyze(bitmap, SkyPhotoSource.Camera)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                vm.analyze(bitmap, SkyPhotoSource.Gallery)
            } else {
                vm.showImageLoadError()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            vm.showPermissionMessage()
        }
    }

    fun openCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            cameraLauncher.launch(null)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.sky_analyzer_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF020916).copy(alpha = 0.32f),
                            Color(0xFF031B34).copy(alpha = 0.08f),
                            Color(0xFF021221).copy(alpha = 0.46f)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Camera Sky Analyzer",
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            Text(
                                text = "AI pentru cer și fotografie",
                                color = Color.White.copy(alpha = 0.74f),
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(start = 14.dp)
                                .size(56.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF142846).copy(alpha = 0.82f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Inapoi",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 14.dp)
                                .size(56.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF142846).copy(alpha = 0.82f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = YellowAccent,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = YellowAccent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 30.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                SkyPhotoHero(state = state)

                SkyAnalyzerActionTiles(
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onCameraClick = ::openCamera
                )

                Box(modifier = Modifier.padding(top = 120.dp)) {
                    SkyPrivacyCard()
                }

                state.permissionMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFFFD5D5),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                state.result?.let { result ->
                    PhotographyScoreCard(result = result)
                    SkyDetailsCard(result = result)
                    CloudStructureCard(result = result)
                    PhenomenaCard(result = result)
                    EvolutionCard(
                        result = result,
                        photoSource = state.photoSource ?: SkyPhotoSource.Camera
                    )
                    AiObservationCard(
                        result = result,
                        isLoading = state.isGeneratingAiObservation,
                        error = state.aiObservationError
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SkyPhotoHero(
    state: SkyAnalyzerUiState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A315C).copy(alpha = 0.86f),
                        Color(0xFF061D36).copy(alpha = 0.96f),
                        Color(0xFF020B17).copy(alpha = 0.99f)
                    )
                )
            )
            .border(
                BorderStroke(1.2.dp, Color(0xFF6A8FD0).copy(alpha = 0.42f)),
                RoundedCornerShape(28.dp)
            )
    ) {
        if (state.photo != null) {
            Image(
                bitmap = state.photo.asImageBitmap(),
                contentDescription = "Fotografie cer",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            EmptyPhotoPlaceholder()
        }

        if (state.photo != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(132.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.74f)
                            )
                        )
                    )
            )
            Text(
                text = "Fotografie selectată",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }

        if (state.isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.18f)
                )
            }
        }
    }
}

@Composable
private fun EmptyPhotoPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF7DB5FF).copy(alpha = 0.34f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Cloud,
                contentDescription = null,
                tint = Color(0xFFD8E7FF),
                modifier = Modifier.size(68.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Analizează cerul",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Încarcă o fotografie pentru a obține\ninformații meteo precise",
            color = Color.White.copy(alpha = 0.73f),
            fontSize = 17.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        PhoneCameraMockup()
    }
}

@Composable
private fun PhoneCameraMockup() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .height(68.dp)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 0.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF07111F).copy(alpha = 0.88f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = YellowAccent,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Sfat: fotografiază cerul într-un loc\ndeschis pentru rezultate mai bune",
                    color = Color.White.copy(alpha = 0.84f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun SkyAnalyzerActionTiles(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkyAnalyzerActionTile(
            title = "Galerie",
            subtitle = "Alege din galerie",
            icon = Icons.Rounded.PhotoLibrary,
            onClick = onGalleryClick,
            modifier = Modifier.weight(1f),
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF082044).copy(alpha = 0.96f),
                    Color(0xFF020B17).copy(alpha = 0.96f)
                )
            )
        )

        SkyAnalyzerActionTile(
            title = "Cameră",
            subtitle = "Fă o fotografie",
            icon = Icons.Rounded.PhotoCamera,
            onClick = onCameraClick,
            modifier = Modifier.weight(1f),
            brush = Brush.linearGradient(
                listOf(
                    Color(0xFF42B8FF),
                    Color(0xFF135BFF),
                    Color(0xFF052B9B)
                )
            )
        )
    }
}

@Composable
private fun SkyAnalyzerActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush
) {
    Box(
        modifier = modifier
            .height(136.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(brush)
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                RoundedCornerShape(22.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 15.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SkyPrivacyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF243A5A).copy(alpha = 0.78f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF2F7B69).copy(alpha = 0.82f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = Color(0xFF2AFF9A),
                    modifier = Modifier.size(30.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Confidențialitate garantată",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = "Fotografiile tale rămân pe dispozitivul tău\nși nu sunt stocate sau distribuite.",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun HeroActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PhotographyScoreCard(result: SkyAnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF3A3D92).copy(alpha = 0.62f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF3F202E),
                            Color(0xFF1A1554),
                            Color(0xFF101A49)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreRing(score = result.photographyScore)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Photography Score",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = scoreLabel(result.photographyScore),
                        color = scoreColor(result.photographyScore),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = result.shortAdvice,
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                GradientSparkline(
                    modifier = Modifier
                        .width(92.dp)
                        .height(52.dp),
                    result = result
                )
            }
        }
    }
}

@Composable
private fun ScoreRing(score: Int) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.size(72.dp),
            color = PurpleAccent,
            trackColor = Color.White.copy(alpha = 0.12f),
            strokeWidth = 5.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "/100",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun GradientSparkline(
    result: SkyAnalysisResult,
    modifier: Modifier = Modifier
) {
    val values = listOf(
        result.sunriseScore,
        result.sunsetScore,
        result.dramaticCloudsScore,
        result.stormScore,
        result.photographyScore
    )

    Canvas(modifier = modifier) {
        val stepX = size.width / (values.size - 1).coerceAtLeast(1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - (value / 100f) * size.height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = PurpleAccent,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - (value / 100f) * size.height
            drawCircle(
                color = CyanAccent,
                radius = 2.5.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun SkyDetailsCard(result: SkyAnalysisResult) {
    val details = listOf(
        DetailMetric("Culori", colorScore(result), Icons.Rounded.Palette, YellowAccent),
        DetailMetric("Contrast", contrastScore(result), Icons.Rounded.Visibility, PurpleAccent),
        DetailMetric("Nori", cloudScore(result), Icons.Rounded.Cloud, BlueAccent),
        DetailMetric("Compozitie", compositionScore(result), Icons.Rounded.GridView, GreenAccent)
    )

    AnalysisCard(
        title = "Detalii analiza cer",
        icon = Icons.Rounded.Info
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            details.forEach { detail ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = detail.label,
                        color = Color.White.copy(alpha = 0.74f),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = detail.icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.82f),
                        modifier = Modifier.size(23.dp)
                    )
                    Text(
                        text = "${detail.value}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    LinearProgressIndicator(
                        progress = { detail.value / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = detail.color,
                        trackColor = Color.White.copy(alpha = 0.10f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CloudStructureCard(result: SkyAnalysisResult) {
    val layers = cloudLayers(result)

    AnalysisCard(
        title = "Structura nori detectata",
        icon = Icons.Rounded.Cloud
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                layers.forEach { layer ->
                    CloudLayerRow(layer)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0E2E4D),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                modifier = Modifier.size(width = 96.dp, height = 104.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CloudIllustration(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        result = result
                    )
                    Text(
                        text = primaryCloudName(result.cloudType),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = cloudSubtitle(result.cloudType),
                        color = Color.White.copy(alpha = 0.64f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun CloudLayerRow(layer: CloudLayer) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = layer.label,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(88.dp),
            maxLines = 1
        )
        LinearProgressIndicator(
            progress = { layer.value / 100f },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
            color = layer.color,
            trackColor = Color.White.copy(alpha = 0.10f)
        )
        Text(
            text = "${layer.value}%",
            color = Color.White.copy(alpha = 0.80f),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CloudIllustration(
    result: SkyAnalysisResult,
    modifier: Modifier = Modifier
) {
    val dark = result.stormProbability > 45 || result.metrics.darkCloudRatio > 0.16f
    val baseColor = if (dark) Color(0xFF7D8794) else Color.White
    val shadowColor = if (dark) Color(0xFF404B59) else Color(0xFFD8E9F8)

    Canvas(modifier = modifier) {
        val h = size.height
        val w = size.width
        drawCircle(baseColor.copy(alpha = 0.92f), radius = h * 0.26f, center = Offset(w * 0.36f, h * 0.54f))
        drawCircle(baseColor, radius = h * 0.34f, center = Offset(w * 0.52f, h * 0.44f))
        drawCircle(baseColor.copy(alpha = 0.96f), radius = h * 0.24f, center = Offset(w * 0.68f, h * 0.58f))
        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(w * 0.22f, h * 0.52f),
            size = androidx.compose.ui.geometry.Size(w * 0.58f, h * 0.26f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.13f, h * 0.13f)
        )
    }
}

@Composable
private fun PhenomenaCard(result: SkyAnalysisResult) {
    val phenomena = detectedPhenomena(result)

    AnalysisCard(
        title = "Fenomene detectate",
        icon = Icons.Rounded.Visibility
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                phenomena.take(2).forEach { item ->
                    PhenomenonChip(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                phenomena.drop(2).forEach { item ->
                    SmallPhenomenonChip(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhenomenonChip(
    item: PhenomenonUi,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0E2E4D),
        border = BorderStroke(
            1.dp,
            if (item.active) item.color.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = item.label,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            if (item.active) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun SmallPhenomenonChip(
    item: PhenomenonUi,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0B243D),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (item.active) 0.20f else 0.08f))
    ) {
        Text(
            text = item.label,
            color = Color.White.copy(alpha = if (item.active) 0.90f else 0.58f),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EvolutionCard(
    result: SkyAnalysisResult,
    photoSource: SkyPhotoSource
) {
    val isGalleryPhoto = photoSource == SkyPhotoSource.Gallery
    val items = if (isGalleryPhoto) {
        visualCueItems(result)
    } else {
        evolutionItems(result)
    }

    AnalysisCard(
        title = if (isGalleryPhoto) "Indicii vizuale" else "Evolutie vizuala estimata",
        icon = Icons.Rounded.Bolt,
        trailing = null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.time,
                        color = Color.White.copy(alpha = 0.74f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(25.dp)
                    )
                    Text(
                        text = "${item.probability}%",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        if (!isGalleryPhoto) {
            Text(
                text = "Estimare orientativa pe baza cerului fotografiat acum. Nu este avertizare meteo oficiala.",
                color = Color.White.copy(alpha = 0.58f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
        }
        LinearProgressIndicator(
            progress = { result.rainProbability / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = YellowAccent,
            trackColor = Color.White.copy(alpha = 0.10f)
        )
    }
}

@Composable
private fun AiObservationCard(
    result: SkyAnalysisResult,
    isLoading: Boolean,
    error: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground.copy(alpha = 0.98f)
        ),
        border = BorderStroke(1.dp, CardBorder.copy(alpha = 0.62f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .border(1.dp, PurpleAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = PurpleAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "AI Observation",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            if (isLoading) {
                Text(
                    text = "AI-ul formuleaza descrierea norilor...",
                    color = Color.White.copy(alpha = 0.76f),
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = PurpleAccent,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
            } else {
                Text(
                    text = result.aiObservation.ifBlank { result.shortAdvice },
                    color = Color.White.copy(alpha = 0.84f),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 25.sp
                )
                error?.let {
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.48f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisCard(
    title: String,
    icon: ImageVector,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, CardBorder.copy(alpha = 0.48f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                trailing?.let {
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.58f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            content()
        }
    }
}

private data class DetailMetric(
    val label: String,
    val value: Int,
    val icon: ImageVector,
    val color: Color
)

private data class CloudLayer(
    val label: String,
    val value: Int,
    val color: Color
)

private data class PhenomenonUi(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val active: Boolean
)

private data class EvolutionUi(
    val time: String,
    val probability: Int,
    val icon: ImageVector,
    val color: Color
)

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (_: Exception) {
        null
    }
}

private fun colorScore(result: SkyAnalysisResult): Int {
    return score(
        result.metrics.warmLightRatio * 155f +
            result.metrics.averageSaturation * 54f +
            result.sunsetScore * 0.32f +
            result.sunriseScore * 0.24f
    )
}

private fun contrastScore(result: SkyAnalysisResult): Int {
    return score(result.metrics.contrast * 235f + result.metrics.darkCloudRatio * 36f)
}

private fun cloudScore(result: SkyAnalysisResult): Int {
    return score(result.metrics.cloudRatio * 92f + result.metrics.darkCloudRatio * 45f + result.dramaticCloudsScore * 0.34f)
}

private fun compositionScore(result: SkyAnalysisResult): Int {
    val balance = 1f - kotlin.math.abs(result.metrics.skyRatio - result.metrics.cloudRatio).coerceAtMost(1f)
    return score(balance * 48f + result.photographyScore * 0.42f + result.metrics.contrast * 28f)
}

private fun cloudLayers(result: SkyAnalysisResult): List<CloudLayer> {
    val primary = primaryCloudName(result.cloudType)
    val cloudBase = (result.metrics.cloudRatio * 100f).roundToInt().coerceIn(0, 100)
    val darkBase = (result.metrics.darkCloudRatio * 100f).roundToInt().coerceIn(0, 100)

    return when {
        primary == "Cumulonimbus" -> listOf(
            CloudLayer("Cumulonimbus", (cloudBase + darkBase * 2).coerceIn(45, 96), BlueAccent),
            CloudLayer("Nimbostratus", (darkBase * 2 + result.rainProbability / 3).coerceIn(18, 88), PurpleAccent),
            CloudLayer("Cirrus", (result.metrics.skyRatio * 22f).roundToInt().coerceIn(4, 24), CyanAccent)
        )

        primary == "Cumulus" -> listOf(
            CloudLayer("Cumulus", (cloudBase * 1.25f).roundToInt().coerceIn(35, 88), BlueAccent),
            CloudLayer("Altocumulus", (cloudBase * 0.42f + result.metrics.contrast * 30f).roundToInt().coerceIn(8, 58), PurpleAccent),
            CloudLayer("Cirrus", (result.metrics.skyRatio * 24f).roundToInt().coerceIn(5, 35), CyanAccent)
        )

        primary == "Cirrus" -> listOf(
            CloudLayer("Cirrus", (result.metrics.skyRatio * 72f).roundToInt().coerceIn(42, 92), CyanAccent),
            CloudLayer("Cumulus", (cloudBase * 0.44f).roundToInt().coerceIn(4, 34), BlueAccent),
            CloudLayer("Altostratus", (cloudBase * 0.32f).roundToInt().coerceIn(3, 28), PurpleAccent)
        )

        else -> listOf(
            CloudLayer(primary, cloudBase.coerceIn(12, 82), BlueAccent),
            CloudLayer("Altocumulus", (cloudBase * 0.52f).roundToInt().coerceIn(6, 58), PurpleAccent),
            CloudLayer("Cirrus", (result.metrics.skyRatio * 18f).roundToInt().coerceIn(3, 28), CyanAccent)
        )
    }
}

private fun detectedPhenomena(result: SkyAnalysisResult): List<PhenomenonUi> {
    val sunset = result.sunsetScore >= 44 || result.bestMoment == "Apus"
    val rays = result.metrics.warmLightRatio > 0.04f && result.metrics.contrast > 0.12f
    val mammatus = result.stormScore > 72 && result.metrics.darkCloudRatio > 0.18f
    val shelf = result.stormProbability > 62
    val arcus = result.stormProbability > 72 && result.metrics.contrast > 0.20f
    val virga = result.rainProbability > 42 && result.metrics.darkCloudRatio > 0.12f

    return listOf(
        PhenomenonUi("Apus", Icons.Rounded.WbSunny, YellowAccent, sunset),
        PhenomenonUi("Raze crepusculare", Icons.Rounded.AutoAwesome, GreenAccent, rays),
        PhenomenonUi("Mammatus", Icons.Rounded.Cloud, Color.White, mammatus),
        PhenomenonUi("Shelf cloud", Icons.Rounded.Thunderstorm, Color.White, shelf),
        PhenomenonUi("Arcus", Icons.Rounded.Cloud, Color.White, arcus),
        PhenomenonUi("Virga", Icons.Rounded.Visibility, Color.White, virga)
    )
}

private fun evolutionItems(result: SkyAnalysisResult): List<EvolutionUi> {
    val now = (result.rainProbability * 0.18f).roundToInt().coerceIn(0, 100)
    val oneHour = (result.rainProbability * 0.48f + result.stormProbability * 0.10f).roundToInt().coerceIn(0, 100)
    val threeHours = (result.rainProbability * 0.68f + result.stormProbability * 0.22f).roundToInt().coerceIn(0, 100)
    val sixHours = (result.rainProbability * 0.52f + result.stormProbability * 0.34f).roundToInt().coerceIn(0, 100)
    val storm = (result.stormProbability * 0.72f).roundToInt().coerceIn(0, 100)

    return listOf(
        EvolutionUi("Acum", now, Icons.Rounded.WbSunny, YellowAccent),
        EvolutionUi("+1h", oneHour, Icons.Rounded.Cloud, Color.White.copy(alpha = 0.88f)),
        EvolutionUi("+3h", threeHours, Icons.Rounded.Cloud, Color.White.copy(alpha = 0.82f)),
        EvolutionUi("+6h", sixHours, Icons.Rounded.Thunderstorm, BlueAccent),
        EvolutionUi("+6h+", storm, Icons.Rounded.Bolt, YellowAccent)
    )
}

private fun visualCueItems(result: SkyAnalysisResult): List<EvolutionUi> {
    return listOf(
        EvolutionUi(
            "Cer",
            (result.metrics.skyRatio * 100f).roundToInt().coerceIn(0, 100),
            Icons.Rounded.WbSunny,
            YellowAccent
        ),
        EvolutionUi(
            "Nori",
            (result.metrics.cloudRatio * 100f).roundToInt().coerceIn(0, 100),
            Icons.Rounded.Cloud,
            Color.White.copy(alpha = 0.88f)
        ),
        EvolutionUi(
            "Intun.",
            (result.metrics.darkCloudRatio * 100f).roundToInt().coerceIn(0, 100),
            Icons.Rounded.Cloud,
            Color.White.copy(alpha = 0.70f)
        ),
        EvolutionUi(
            "Ploaie",
            result.rainProbability,
            Icons.Rounded.Thunderstorm,
            BlueAccent
        ),
        EvolutionUi(
            "Furt.",
            result.stormProbability,
            Icons.Rounded.Bolt,
            YellowAccent
        )
    )
}

private fun primaryCloudName(cloudType: String): String {
    return when {
        cloudType.contains("Cumulonimbus", ignoreCase = true) -> "Cumulonimbus"
        cloudType.contains("Nimbostratus", ignoreCase = true) -> "Nimbostratus"
        cloudType.contains("Altostratus", ignoreCase = true) -> "Altostratus"
        cloudType.contains("Cumulus", ignoreCase = true) -> "Cumulus"
        cloudType.contains("Cirrus", ignoreCase = true) -> "Cirrus"
        cloudType.contains("senin", ignoreCase = true) -> "Cer senin"
        else -> "Nori mixti"
    }
}

private fun cloudSubtitle(cloudType: String): String {
    return when {
        cloudType.contains("furtuna", ignoreCase = true) -> "Dezvoltare verticala"
        cloudType.contains("senin", ignoreCase = true) -> "Putini nori"
        cloudType.contains("pufosi", ignoreCase = true) -> "Nori pufosi, de vreme buna"
        cloudType.contains("subtiri", ignoreCase = true) -> "Nori inalti si fini"
        cloudType.contains("ploaie", ignoreCase = true) -> "Strat gros de ploaie"
        else -> "Structura mixta"
    }
}

private fun scoreLabel(score: Int): String {
    return when {
        score >= 84 -> "Excellent"
        score >= 68 -> "Very good"
        score >= 50 -> "Good"
        score >= 34 -> "Fair"
        else -> "Low"
    }
}

private fun scoreColor(score: Int): Color {
    return when {
        score >= 72 -> GreenAccent
        score >= 50 -> YellowAccent
        else -> Color(0xFFFF8F70)
    }
}

private fun score(value: Float): Int = value.roundToInt().coerceIn(0, 100)
