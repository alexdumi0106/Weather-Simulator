package com.example.weathersimulator.sensors.pressure

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PressureSensorDataSource(
    context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    fun isAvailable(): Boolean = pressureSensor != null

    fun pressureHpaFlow(samplingPeriodUs: Int = SensorManager.SENSOR_DELAY_NORMAL): Flow<Float> = callbackFlow {
        val sensor = pressureSensor
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val hPa = event.values.firstOrNull() ?: return
                trySend(hPa).isSuccess
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, sensor, samplingPeriodUs)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
