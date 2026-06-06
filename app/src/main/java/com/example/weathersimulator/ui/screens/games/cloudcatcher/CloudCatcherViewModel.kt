package com.example.weathersimulator.ui.screens.games.cloudcatcher

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class CloudCatcherViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(
        CloudCatcherGameState(bestScore = prefs.getInt(BestScoreKey, 0))
    )
    val state: StateFlow<CloudCatcherGameState> = _state.asStateFlow()

    private val random = Random(System.currentTimeMillis())
    private var nextRoundId = 1L
    private var spawnAccumulator = 0f

    fun startGame() {
        spawnAccumulator = 0f
        val bestScore = _state.value.bestScore
        _state.value = CloudCatcherGameState(
            roundId = nextRoundId++,
            phase = CloudCatcherPhase.Running,
            bestScore = bestScore,
            score = 0,
            lives = StartingLives,
            remainingMillis = TotalGameMillis,
            planeY = 0.56f,
            objects = initialCloudCatcherObjects(),
            nextObjectId = 5L
        )
    }

    fun pauseGame() {
        _state.update { state ->
            if (state.phase == CloudCatcherPhase.Running) {
                state.copy(phase = CloudCatcherPhase.Paused)
            } else {
                state
            }
        }
    }

    fun resumeGame() {
        _state.update { state ->
            if (state.phase == CloudCatcherPhase.Paused) {
                state.copy(phase = CloudCatcherPhase.Running)
            } else {
                state
            }
        }
    }

    fun movePlane(planeY: Float) {
        _state.update { state ->
            state.copy(planeY = planeY.coerceIn(MinPlaneY, MaxPlaneY))
        }
    }

    fun step(deltaSeconds: Float) {
        val current = _state.value
        if (current.phase != CloudCatcherPhase.Running) return

        val safeDelta = deltaSeconds.coerceIn(0f, 0.05f)
        val remainingMillis = (current.remainingMillis - (safeDelta * 1000f).roundToLong())
            .coerceAtLeast(0L)
        var nextObjectId = current.nextObjectId
        var score = current.score
        var lives = current.lives
        var objects = current.objects

        spawnAccumulator += safeDelta
        val interval = spawnInterval(remainingMillis)
        if (spawnAccumulator >= interval) {
            spawnAccumulator = 0f
            val newObject = createCloudCatcherObject(
                id = nextObjectId,
                random = random,
                progress = gameProgress(remainingMillis)
            )
            nextObjectId += 1L
            objects = objects + newObject
        }

        objects = objects.mapNotNull { item ->
            val moved = item.copy(x = item.x - item.speed * safeDelta)
            val collided = moved.collidesWithPlane(current.planeY)

            when {
                collided -> {
                    when (moved.type) {
                        CloudCatcherObjectType.WhiteCloud -> score += 10
                        CloudCatcherObjectType.RainDrop -> score += 5
                        CloudCatcherObjectType.Lightning -> score = (score - 5).coerceAtLeast(0)
                        CloudCatcherObjectType.StormCloud -> lives = (lives - 1).coerceAtLeast(0)
                    }
                    null
                }

                moved.x < -0.16f -> null
                else -> moved
            }
        }

        val isFinished = remainingMillis <= 0L || lives <= 0
        val bestScore = if (isFinished && score > current.bestScore) {
            prefs.edit().putInt(BestScoreKey, score).apply()
            score
        } else {
            current.bestScore
        }

        _state.value = current.copy(
            phase = if (isFinished) {
                CloudCatcherPhase.Finished
            } else {
                CloudCatcherPhase.Running
            },
            bestScore = bestScore,
            score = score,
            lives = lives,
            remainingMillis = remainingMillis,
            objects = objects,
            nextObjectId = nextObjectId
        )
    }

    private companion object {
        const val PrefsName = "cloud_catcher_prefs"
        const val BestScoreKey = "best_score"
    }
}
