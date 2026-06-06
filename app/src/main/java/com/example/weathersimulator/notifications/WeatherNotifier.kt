package com.example.weathersimulator.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.weathersimulator.MainActivity
import com.example.weathersimulator.R
import com.example.weathersimulator.domain.weather.WeatherAlert

object WeatherNotifier {
    private const val CHANNEL_ID = "weather_alerts"
    private const val URGENT_CHANNEL_ID = "weather_urgent_alerts"
    private const val PREFS_NAME = "weather_notification_prefs"
    private const val LAST_SENT_PREFIX = "last_sent_"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel = NotificationChannel(
                CHANNEL_ID,
                "Weather messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Mesaje meteo utile bazate pe prognoza locala."
            }

            val urgentChannel = NotificationChannel(
                URGENT_CHANNEL_ID,
                "Urgent weather alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerte meteo importante pentru furtuna, polei sau vreme severa."
            }

            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(defaultChannel)
            nm.createNotificationChannel(urgentChannel)
        }
    }

    fun showWeatherMessages(
        context: Context,
        alerts: List<WeatherAlert>
    ) {
        alerts.forEach { alert ->
            show(context, alert)
        }
    }

    fun show(context: Context, alert: WeatherAlert) {
        if (!shouldShow(context, alert)) return

        val shown = show(
            context = context,
            title = alert.title,
            message = alert.message,
            notificationId = alert.notificationId(),
            urgent = alert.urgent
        )

        if (shown) {
            rememberSent(context, alert)
        }
    }

    fun show(context: Context, title: String, message: String) {
        show(
            context = context,
            title = title,
            message = message,
            notificationId = 1001,
            urgent = false
        )
    }

    private fun show(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        urgent: Boolean
    ): Boolean {
        ensureChannel(context)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            if (urgent) URGENT_CHANNEL_ID else CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (urgent) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
        return true
    }

    private fun shouldShow(
        context: Context,
        alert: WeatherAlert
    ): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSent = prefs.getLong(LAST_SENT_PREFIX + alert.id, 0L)
        return System.currentTimeMillis() - lastSent >= alert.cooldownMs
    }

    private fun rememberSent(
        context: Context,
        alert: WeatherAlert
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(LAST_SENT_PREFIX + alert.id, System.currentTimeMillis())
            .apply()
    }

    private fun WeatherAlert.notificationId(): Int {
        return 10_000 + ((id.hashCode() and Int.MAX_VALUE) % 20_000)
    }
}
