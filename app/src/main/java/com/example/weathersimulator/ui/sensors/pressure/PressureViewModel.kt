package com.example.weathersimulator.sensors.pressure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PressureViewModel @Inject constructor(
    private val dataSource: PressureSensorDataSource
) : ViewModel() {

    private val analyzer = PressureAnalyzer()

    private val _uiState = MutableStateFlow(
        PressureUiState(isAvailable = dataSource.isAvailable())
    )
    val uiState: StateFlow<PressureUiState> = _uiState

    fun start() {
        if (!dataSource.isAvailable()) {
            _uiState.value = PressureUiState(isAvailable = false)
            return
        }

        viewModelScope.launch {
            dataSource.pressureHpaFlow()
                .onEach { hPa ->
                    val now = System.currentTimeMillis()
                    val res = analyzer.addSample(hPa, now)

                    _uiState.value = _uiState.value.copy(
                        pressureHpa = res.pressureHpa,
                        baselineHpa = res.baselineHpa,
                        trendHpaPerHour = res.trendHpaPerHour,
                        trendLabel = res.trendLabel
                    )
                }
                .catch {
                    _uiState.value = _uiState.value.copy(isAvailable = false)
                }
                .collect { }
        }
    }
}
