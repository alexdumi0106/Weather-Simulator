package com.example.weathersimulator.ui.weather

import com.example.weathersimulator.R

data class WeatherVisual(
    val iconRes: Int,
    val label: String
)

object WeatherIconRules {

    fun resolve(weatherCode: Int, isDay: Boolean, cloudCover: Int?): WeatherVisual {
        val cloud = normalizeCloudCover(cloudCover, weatherCode)
        val hasSomeClearSky = cloud <= 70

        // Storm detection codes (with priority over WMO standard codes)
        when (weatherCode) {
            998 -> {
                // Furtună puternică (score >= 11)
                return WeatherVisual(
                    iconRes = R.drawable.icon_weather_15,
                    label = "Furtună puternică"
                )
            }

            997 -> {
                // Furtună normală (score >= 8)
                return if (hasSomeClearSky) {
                    WeatherVisual(
                        iconRes = if (isDay) R.drawable.icon_weather_16 else R.drawable.icon_weather_42,
                        label = if (isDay) "Furtună cu soare" else "Furtună"
                    )
                } else {
                    WeatherVisual(
                        iconRes = R.drawable.icon_weather_15,
                        label = "Furtună"
                    )
                }
            }

            996 -> {
                // Averse / risc de furtună (score >= 6)
                return WeatherVisual(
                    iconRes = if (isDay) R.drawable.icon_weather_16 else R.drawable.icon_weather_42,
                    label = "Averse / risc de furtună"
                )
            }
        }

        // Snow + Rain combined (not a storm, but a precipitation mix)
        if (weatherCode == 999) {
            return WeatherVisual(
                iconRes = R.drawable.icon_weather_29,
                label = "Ninsoare și ploaie"
            )
        }

        when (weatherCode) {
            45, 48 -> {
                return WeatherVisual(
                    iconRes = R.drawable.icon_weather_11,
                    label = "Ceață"
                )
            }

            51, 53, 55, 56, 57, 80 -> {
                return if (isDay) {
                    if (hasSomeClearSky) {
                        WeatherVisual(
                            iconRes = R.drawable.icon_weather_14,
                            label = "Ploaie ușoară"
                        )
                    } else {
                        WeatherVisual(
                            iconRes = R.drawable.icon_weather_12,
                            label = "Ploaie ușoară"
                        )
                    }
                } else {
                    if (hasSomeClearSky) {
                        WeatherVisual(
                            iconRes = R.drawable.icon_weather_39,
                            label = "Ploaie ușoară"
                        )
                    } else {
                        WeatherVisual(
                            iconRes = R.drawable.icon_weather_12,
                            label = "Ploaie ușoară"
                        )
                    }
                }
            }

            61, 81 -> {
                return WeatherVisual(
                    iconRes = R.drawable.icon_weather_18,
                    label = "Ploaie"
                )
            }

            63, 65, 66, 67, 82 -> {
                return WeatherVisual(
                    iconRes = R.drawable.icon_weather_18,
                    label = "Ploaie intensa"
                )
            }

            71, 85 -> {
                return if (hasSomeClearSky) {
                    WeatherVisual(
                        iconRes = if (isDay) R.drawable.icon_weather_21 else R.drawable.icon_weather_43,
                        label = "Ninsoare usoara"
                    )
                } else {
                    WeatherVisual(
                        iconRes = R.drawable.icon_weather_19,
                        label = "Ninsoare usoara"
                    )
                }
            }

            73, 75, 77, 86 -> {
                return if (hasSomeClearSky) {
                    WeatherVisual(
                        iconRes = if (isDay) R.drawable.icon_weather_23 else R.drawable.icon_weather_44,
                        label = "Ninsoare intensa"
                    )
                } else {
                    WeatherVisual(
                        iconRes = R.drawable.icon_weather_22,
                        label = "Ninsoare intensa"
                    )
                }
            }
        }

        return resolveCloudBasedIcon(cloud, isDay)
    }

    private fun resolveCloudBasedIcon(cloudCover: Int, isDay: Boolean): WeatherVisual {
        return when (cloudCover) {
            in 0..10 -> WeatherVisual(
                iconRes = if (isDay) R.drawable.icon_weather_01 else R.drawable.icon_weather_33,
                label = if (isDay) "Însorit" else "Senin"
            )

            in 11..30 -> WeatherVisual(
                iconRes = if (isDay) R.drawable.icon_weather_02 else R.drawable.icon_weather_34,
                label = if (isDay) "Predominant însorit" else "Predominant senin"
            )

            in 31..50 -> WeatherVisual(
                iconRes = if (isDay) R.drawable.icon_weather_03 else R.drawable.icon_weather_35,
                label = if (isDay) "Parțial însorit" else "Parțial noros"
            )

            in 51..70 -> WeatherVisual(
                iconRes = if (isDay) R.drawable.icon_weather_04 else R.drawable.icon_weather_36,
                label = if (isDay) "Nori și soare" else "Parțial spre predominant noros"
            )

            in 71..90 -> WeatherVisual(
                iconRes = if (isDay) R.drawable.icon_weather_06 else R.drawable.icon_weather_38,
                label = "Predominant noros"
            )

            else -> WeatherVisual(
                iconRes = R.drawable.icon_weather_07,
                label = "Noros"
            )
        }
    }

    private fun normalizeCloudCover(cloudCover: Int?, weatherCode: Int): Int {
        if (cloudCover != null) return cloudCover.coerceIn(0, 100)

        return when (weatherCode) {
            0 -> 5
            1 -> 20
            2 -> 40
            3 -> 95
            else -> 100
        }
    }
}
