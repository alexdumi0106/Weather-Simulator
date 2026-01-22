package com.example.weathersimulator.ui.sensors.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

class AudioController(private val context: Context) {

    private var thunderPlayer: MediaPlayer? = null
    private var windPlayer: MediaPlayer? = null
    private var rainPlayer: MediaPlayer? = null


    fun playThunder(resId: Int) {
        releaseThunder()

        val mp = MediaPlayer()
        thunderPlayer = mp

        try {
            val afd = context.resources.openRawResourceFd(resId)
                ?: run {
                    Log.e("AudioController", "openRawResourceFd null for resId=$resId")
                    releaseThunder()
                    return
                }

            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )

            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mp.setOnPreparedListener {
                Log.d("AudioController", "Thunder prepared -> start()")
                it.setVolume(1f, 1f)
                it.start()
            }

            mp.setOnErrorListener { _, what, extra ->
                Log.e("AudioController", "Thunder error what=$what extra=$extra")
                releaseThunder()
                true
            }

            mp.prepareAsync()

        } catch (e: Exception) {
            Log.e("AudioController", "Thunder exception: ${e.message}", e)
            releaseThunder()
        }
    }

    fun startRainLoop(resId: Int, volume: Float = 0.6f) {
        if (rainPlayer?.isPlaying == true) {
            setRainVolume(volume)
            return
        }

        releaseRain()

        val mp = MediaPlayer()
        rainPlayer = mp

        try {
            val afd = context.resources.openRawResourceFd(resId)
                ?: run {
                    Log.e("AudioController", "openRawResourceFd null for rain resId=$resId")
                    releaseRain()
                    return
                }

            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )

            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mp.isLooping = true

            mp.setOnPreparedListener {
                Log.d("AudioController", "Rain prepared -> start()")
                val v = volume.coerceIn(0f, 1f)
                it.setVolume(v, v)
                it.start()
            }

            mp.setOnErrorListener { _, what, extra ->
                Log.e("AudioController", "Rain error what=$what extra=$extra")
                releaseRain()
                true
            }

            mp.prepareAsync()

        } catch (e: Exception) {
            Log.e("AudioController", "Rain exception: ${e.message}", e)
            releaseRain()
        }
    }

    fun startWindLoop(resId: Int, volume: Float = 1f) {
        if (windPlayer?.isPlaying == true) {
            setWindVolume(volume)
            return
        }

        releaseWind()

        val mp = MediaPlayer()
        windPlayer = mp

        try {
            val afd = context.resources.openRawResourceFd(resId)
            if (afd == null) {
                Log.e("AudioController", "openRawResourceFd returned null for wind resId=$resId")
                releaseWind()
                return
            }

            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )

            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            mp.isLooping = true

            mp.setOnPreparedListener {
                Log.d("AudioController", "Wind prepared -> start()")
                val v = volume.coerceIn(0f, 1f)
                it.setVolume(1f, 1f)
                it.start()
            }

            mp.setOnErrorListener { _, what, extra ->
                Log.e("AudioController", "Wind error what=$what extra=$extra")
                releaseWind()
                true
            }

            Log.d("AudioController", "Wind prepareAsync called")
            mp.prepareAsync()

        } catch (e: Exception) {
            Log.e("AudioController", "Wind exception: ${e.message}", e)
            releaseWind()
        }
    }

    fun stopWind() {
        releaseWind()
    }

    fun setWindVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        windPlayer?.setVolume(v, v)
    }

    fun stopRain() {
        releaseRain()
    }

    fun setRainVolume(volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        rainPlayer?.setVolume(v, v)
    }

    fun releaseAll() {
        releaseThunder()
        releaseWind()
        releaseRain()
    }

    private fun releaseThunder() {
        thunderPlayer?.release()
        thunderPlayer = null
    }

    private fun releaseWind() {
        windPlayer?.release()
        windPlayer = null
    }

    private fun releaseRain() {
        rainPlayer?.release()
        rainPlayer = null
    }

}
