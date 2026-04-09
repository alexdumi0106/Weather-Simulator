package com.example.weathersimulator.data.local.weather

import android.content.Context

data class WeatherCsvDataset(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val utcOffsetSeconds: Int,
    val timezone: String,
    val timezoneAbbreviation: String,
    val rows: List<WeatherCsvRow>,
    val dailyRows: List<WeatherCsvDailyRow>
)

data class WeatherCsvRow(
    val time: String,
    val temperatureC: Double,
    val relativeHumidityPercent: Int,
    val surfacePressureHpa: Double,
    val cloudCoverPercent: Int,
    val windSpeed10mKmh: Double,
    val rainMm: Double,
    val snowfallCm: Double,
    val precipitationMm: Double,
    val windGusts10mKmh: Double,
    val weatherCode: Int
)

data class WeatherCsvDailyRow(
    val time: String,
    val temperatureMaxC: Double,
    val temperatureMinC: Double,
    val sunrise: String,
    val sunset: String,
    val snowfallCm: Double,
    val rainMm: Double,
    val cloudCoverMeanPercent: Int,
    val relativeHumidityMeanPercent: Int,
    val surfacePressureMeanHpa: Double,
    val windSpeed10mMeanKmh: Double
)

class WeatherCsvReader(
    private val context: Context,
    private val fileName: String = "open-meteo-45.80N21.18E96m.csv"
) {

    private val hourlyHeaderPrefix = "time,temperature_2m (°C)"
    private val dailyHeaderPrefix = "time,temperature_2m_max (°C)"

    fun read(): WeatherCsvDataset {
        context.assets.open(fileName).bufferedReader().use { reader ->
            var metadata: List<String>? = null
            var inHourlySection = false
            var inDailySection = false
            val dataRows = mutableListOf<WeatherCsvRow>()
            val dailyRows = mutableListOf<WeatherCsvDailyRow>()

            reader.forEachLine { rawLine ->
                val line = rawLine.trim()
                if (line.isEmpty()) {
                    return@forEachLine
                }

                if (metadata == null && line.count { it == ',' } >= 5 && !line.startsWith("latitude,")) {
                    val candidate = line.split(',')
                    if (candidate.size >= 6) {
                        metadata = candidate
                    }
                    return@forEachLine
                }

                if (line.startsWith(hourlyHeaderPrefix)) {
                    inHourlySection = true
                    inDailySection = false
                    return@forEachLine
                }

                if (line.startsWith(dailyHeaderPrefix)) {
                    inHourlySection = false
                    inDailySection = true
                    return@forEachLine
                }

                if (line.startsWith("time,")) {
                    return@forEachLine
                }

                if (inHourlySection) {
                    dataRows.add(parseRow(line))
                    return@forEachLine
                }

                if (inDailySection) {
                    dailyRows.add(parseDailyRow(line))
                }
            }

            val parsedMetadata = metadata
            require(parsedMetadata != null && parsedMetadata.size >= 6) {
                "CSV metadata row is malformed: $fileName"
            }

            require(dataRows.isNotEmpty()) {
                "CSV hourly section is missing or empty: $fileName"
            }

            return WeatherCsvDataset(
                latitude = parsedMetadata[0].toDouble(),
                longitude = parsedMetadata[1].toDouble(),
                elevation = parsedMetadata[2].toDouble(),
                utcOffsetSeconds = parsedMetadata[3].toInt(),
                timezone = parsedMetadata[4],
                timezoneAbbreviation = parsedMetadata[5],
                rows = dataRows,
                dailyRows = dailyRows
            )
        }
    }

    private fun parseRow(line: String): WeatherCsvRow {
        val columns = line.split(',')
        require(columns.size >= 11) {
            "CSV data row has too few columns: $line"
        }

        return WeatherCsvRow(
            time = columns[0],
            temperatureC = columns[1].toDouble(),
            relativeHumidityPercent = columns[2].toInt(),
            surfacePressureHpa = columns[3].toDouble(),
            cloudCoverPercent = columns[4].toInt(),
            windSpeed10mKmh = columns[5].toDouble(),
            rainMm = columns[6].toDouble(),
            snowfallCm = columns[7].toDouble(),
            precipitationMm = columns[8].toDouble(),
            windGusts10mKmh = columns[9].toDouble(),
            weatherCode = columns[10].toInt()
        )
    }

    private fun parseDailyRow(line: String): WeatherCsvDailyRow {
        val columns = line.split(',')
        require(columns.size >= 11) {
            "CSV daily data row has too few columns: $line"
        }

        return WeatherCsvDailyRow(
            time = columns[0],
            temperatureMaxC = columns[1].toDouble(),
            temperatureMinC = columns[2].toDouble(),
            sunrise = columns[3],
            sunset = columns[4],
            snowfallCm = columns[5].toDouble(),
            rainMm = columns[6].toDouble(),
            cloudCoverMeanPercent = columns[7].toInt(),
            relativeHumidityMeanPercent = columns[8].toInt(),
            surfacePressureMeanHpa = columns[9].toDouble(),
            windSpeed10mMeanKmh = columns[10].toDouble()
        )
    }
}