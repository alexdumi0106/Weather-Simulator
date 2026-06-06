package com.example.weathersimulator.ui.screens.skyanalyzer

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import kotlin.math.abs
import kotlin.math.roundToInt

object SkyAnalyzer {
    fun analyze(bitmap: Bitmap): SkyAnalysisResult {
        val targetWidth = 96
        val targetHeight = ((bitmap.height / bitmap.width.toFloat()) * targetWidth)
            .roundToInt()
            .coerceIn(64, 144)
        val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

        var total = 0
        var sky = 0
        var brightCloud = 0
        var mediumCloud = 0
        var darkCloudCandidate = 0
        var warmLight = 0
        var fogLike = 0
        var brightnessSum = 0f
        var saturationSum = 0f
        val brightnessValues = ArrayList<Float>(targetWidth * targetHeight)
        val hsv = FloatArray(3)

        val analysisHeight = (scaled.height * 0.82f).roundToInt().coerceAtLeast(1)

        for (y in 0 until analysisHeight) {
            for (x in 0 until scaled.width) {
                val color = scaled.getPixel(x, y)
                AndroidColor.colorToHSV(color, hsv)

                val hue = hsv[0]
                val saturation = hsv[1]
                val brightness = hsv[2]
                val red = AndroidColor.red(color)
                val green = AndroidColor.green(color)
                val blue = AndroidColor.blue(color)
                val channelSpread = (maxOf(red, green, blue) - minOf(red, green, blue)) / 255f
                val isVegetation = hue in 72f..170f && saturation > 0.18f && green >= red
                val isBuiltSurface = hue in 18f..68f &&
                    saturation in 0.10f..0.55f &&
                    brightness in 0.25f..0.88f &&
                    red >= blue
                val isNeutral = saturation < 0.30f && channelSpread < 0.24f
                val isCoolGray = hue in 185f..265f &&
                    saturation in 0.08f..0.52f &&
                    brightness in 0.18f..0.72f
                val isAtmosphericRegion = y < scaled.height * 0.78f

                val isBlueSky = isAtmosphericRegion &&
                    hue in 190f..245f &&
                    saturation > 0.18f &&
                    brightness > 0.46f &&
                    channelSpread > 0.16f &&
                    blue > red * 1.12f
                val isBrightCloud = isAtmosphericRegion &&
                    isNeutral &&
                    brightness > 0.66f &&
                    !isVegetation &&
                    !isBuiltSurface
                val isMediumCloud = isAtmosphericRegion &&
                    !isBlueSky &&
                    !isVegetation &&
                    !isBuiltSurface &&
                    (isNeutral || isCoolGray) &&
                    brightness in 0.40f..0.78f
                val isDarkCloud = isAtmosphericRegion &&
                    !isBlueSky &&
                    (isNeutral || isCoolGray || channelSpread < 0.32f) &&
                    brightness in 0.18f..0.58f &&
                    blue >= red * 0.72f &&
                    !isVegetation &&
                    !isBuiltSurface
                val isWarm = hue in 12f..55f &&
                    saturation > 0.26f &&
                    brightness > 0.40f &&
                    y < scaled.height * 0.58f
                val isFogLike = isNeutral && brightness in 0.48f..0.84f

                if (isBlueSky) sky++
                if (isBrightCloud) brightCloud++
                if (isDarkCloud) darkCloudCandidate++
                if (isMediumCloud && !isDarkCloud) mediumCloud++
                if (isWarm) warmLight++
                if (isFogLike) fogLike++

                brightnessSum += brightness
                saturationSum += saturation
                brightnessValues += brightness
                total++
            }
        }

        val averageBrightness = brightnessSum / total
        val averageSaturation = saturationSum / total
        val contrast = brightnessValues
            .map { abs(it - averageBrightness) }
            .average()
            .toFloat()
            .coerceIn(0f, 1f)

        val skyRatio = sky / total.toFloat()
        val brightCloudRatio = brightCloud / total.toFloat()
        val mediumCloudRatio = mediumCloud / total.toFloat()
        val rawDarkCloudRatio = darkCloudCandidate / total.toFloat()
        val cloudEvidenceRatio = brightCloudRatio + mediumCloudRatio + rawDarkCloudRatio
        val darkCloudRatio = if (cloudEvidenceRatio < 0.10f && skyRatio < 0.08f) {
            rawDarkCloudRatio * 0.45f
        } else {
            rawDarkCloudRatio
        }
        val cloudRatio = (brightCloudRatio + mediumCloudRatio + darkCloudRatio).coerceIn(0f, 1f)

        val metrics = SkyMetrics(
            skyRatio = skyRatio,
            cloudRatio = cloudRatio,
            darkCloudRatio = darkCloudRatio,
            warmLightRatio = warmLight / total.toFloat(),
            averageBrightness = averageBrightness,
            averageSaturation = averageSaturation,
            contrast = contrast
        )

        val lowLightRainBoost = if (metrics.cloudRatio > 0.24f) {
            (0.64f - metrics.averageBrightness).coerceAtLeast(0f) * 52f
        } else {
            0f
        }
        val textureRainBoost = if (metrics.cloudRatio > 0.22f) metrics.contrast * 26f else 0f
        val rainProbability = probability(
            metrics.cloudRatio * 38f +
                metrics.darkCloudRatio * 118f +
                lowLightRainBoost +
                textureRainBoost
        ).let { if (metrics.cloudRatio < 0.14f) it.coerceAtMost(18) else it }

        val textureStormBoost = if (metrics.darkCloudRatio > 0.08f) metrics.contrast * 58f else 0f
        val lowCeilingStormBoost = if (metrics.darkCloudRatio > 0.16f && metrics.averageBrightness < 0.58f) {
            14f
        } else {
            0f
        }
        val stormProbability = probability(
            metrics.darkCloudRatio * 136f +
                textureStormBoost +
                metrics.cloudRatio * 16f +
                lowCeilingStormBoost +
                if (rainProbability > 65) 14f else 0f
        ).let { if (metrics.darkCloudRatio < 0.08f) it.coerceAtMost(22) else it }

        val sunsetScore = score(
            metrics.warmLightRatio * 150f +
                metrics.contrast * 55f +
                (1f - metrics.cloudRatio).coerceAtLeast(0f) * 18f
        )
        val sunriseScore = score(
            metrics.warmLightRatio * 126f +
                (0.76f - metrics.averageBrightness).coerceAtLeast(0f) * 30f +
                metrics.skyRatio * 20f
        )
        val stormScore = score(stormProbability * 0.78f + metrics.darkCloudRatio * 32f)
        val dramaticCloudsScoreRaw = score(
            metrics.cloudRatio * 70f +
                metrics.darkCloudRatio * 62f +
                if (metrics.cloudRatio > 0.18f) metrics.contrast * 64f else 0f
        )
        val dramaticCloudsScore = if (metrics.cloudRatio < 0.18f) {
            dramaticCloudsScoreRaw.coerceAtMost(12)
        } else {
            dramaticCloudsScoreRaw
        }
        val fogRatio = fogLike / total.toFloat()
        val fogScore = if (fogRatio > 0.42f && metrics.contrast < 0.14f) {
            score(
                fogRatio * 88f +
                    (0.18f - metrics.contrast).coerceAtLeast(0f) * 100f
            )
        } else {
            score(fogRatio * 18f).coerceAtMost(14)
        }

        val categoryScores = listOf(
            "Apus" to sunsetScore,
            "Rasarit" to sunriseScore,
            "Furtuni" to stormScore,
            "Nori dramatici" to dramaticCloudsScore,
            "Ceata" to fogScore
        )
        val best = categoryScores.maxBy { it.second }

        return SkyAnalysisResult(
            cloudType = resolveCloudType(metrics),
            rainProbability = rainProbability,
            stormProbability = stormProbability,
            photographyScore = best.second,
            bestMoment = best.first,
            sunsetScore = sunsetScore,
            sunriseScore = sunriseScore,
            stormScore = stormScore,
            dramaticCloudsScore = dramaticCloudsScore,
            fogScore = fogScore,
            shortAdvice = buildAdvice(best.first, rainProbability, stormProbability, metrics),
            metrics = metrics
        )
    }

    private fun resolveCloudType(metrics: SkyMetrics): String {
        return when {
            metrics.cloudRatio < 0.10f && metrics.skyRatio > 0.14f -> "Cer senin sau aproape senin"
            metrics.cloudRatio < 0.14f -> "Cer predominant senin"
            metrics.darkCloudRatio > 0.18f && metrics.cloudRatio > 0.38f && metrics.contrast >= 0.12f -> "Cumulonimbus / nori de furtuna"
            metrics.darkCloudRatio > 0.14f && metrics.cloudRatio > 0.46f -> "Nimbostratus / strat gros de ploaie"
            metrics.cloudRatio > 0.62f && metrics.contrast < 0.16f -> "Altostratus / cer acoperit uniform"
            metrics.cloudRatio > 0.30f && metrics.contrast >= 0.16f -> "Cumulus / nori pufosi cu relief"
            metrics.skyRatio > 0.42f && metrics.cloudRatio in 0.10f..0.35f -> "Cirrus / nori subtiri"
            else -> "Nori mixti"
        }
    }

    private fun buildAdvice(
        bestMoment: String,
        rainProbability: Int,
        stormProbability: Int,
        metrics: SkyMetrics
    ): String {
        return when {
            stormProbability >= 60 -> "Cadru dramatic, cu nori amenintatori; evita zonele expuse si fotografiaza dintr-un loc sigur."
            rainProbability >= 55 -> "Cerul pare incarcat; protejeaza telefonul si cauta reflexii daca incepe ploaia."
            bestMoment == "Apus" -> "Cauta directia luminii calde si include o silueta pentru profunzime."
            bestMoment == "Ceata" -> "Foloseste subiecte apropiate si contraste simple; atmosfera este punctul forte."
            bestMoment == "Nori dramatici" -> "Subexpune usor cerul ca sa pastrezi textura norilor."
            metrics.skyRatio > 0.45f -> "Cer curat bun pentru compozitii minimaliste sau timelapse."
            else -> "Cauta un prim-plan clar ca fotografia sa aiba ancora vizuala."
        }
    }

    private fun probability(value: Float): Int = value.roundToInt().coerceIn(0, 100)

    private fun score(value: Float): Int = value.roundToInt().coerceIn(0, 100)
}
