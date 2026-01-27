package com.example.weathersimulator.sensors.pressure

import kotlin.math.abs

class PressureAnalyzer(
    private val smoothWindow: Int = 10,            // ultimele 10 citiri (ex 10-20 sec)
    private val trendWindowMs: Long = 15 * 60_000L  // 15 min (poți face 30/60)
) {
    private val smoothBuffer = ArrayDeque<Float>()
    private val samples = ArrayDeque<Pair<Long, Float>>() // (timestampMs, smoothedHpa)

    fun addSample(rawHpa: Float, nowMs: Long): Result {
        // 1) smoothing
        smoothBuffer.addLast(rawHpa)
        if (smoothBuffer.size > smoothWindow) smoothBuffer.removeFirst()
        val smoothed = smoothBuffer.average().toFloat()

        // 2) păstrăm doar fereastra de trend
        samples.addLast(nowMs to smoothed)
        val cutoff = nowMs - trendWindowMs
        while (samples.isNotEmpty() && samples.first().first < cutoff) {
            samples.removeFirst()
        }

        val baseline = samples.firstOrNull()?.second
        val oldest = samples.firstOrNull()
        val newest = samples.lastOrNull()

        val trendHpaPerHour = if (oldest != null && newest != null && newest.first > oldest.first) {
            val dp = newest.second - oldest.second
            val dtHours = (newest.first - oldest.first) / 3_600_000f
            dp / dtHours
        } else null

        val label = classify(trendHpaPerHour)

        return Result(smoothed, baseline, trendHpaPerHour, label)
    }

    private fun classify(trendHpaPerHour: Float?): PressureTrend {
        if (trendHpaPerHour == null) return PressureTrend.UNKNOWN

        // praguri simple (poți ajusta)
        return when {
            trendHpaPerHour <= -3.0f -> PressureTrend.RAPID_FALL
            trendHpaPerHour < -1.0f  -> PressureTrend.FALLING
            abs(trendHpaPerHour) <= 0.5f -> PressureTrend.STABLE
            trendHpaPerHour >= 1.0f  -> PressureTrend.RISING
            else -> PressureTrend.STABLE
        }
    }

    data class Result(
        val pressureHpa: Float,
        val baselineHpa: Float?,
        val trendHpaPerHour: Float?,
        val trendLabel: PressureTrend
    )
}

private fun ArrayDeque<Float>.average(): Double {
    if (isEmpty()) return 0.0
    var sum = 0.0
    for (v in this) sum += v
    return sum / size
}
