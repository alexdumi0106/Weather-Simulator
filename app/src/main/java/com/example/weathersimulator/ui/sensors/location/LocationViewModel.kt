package com.example.weathersimulator.ui.sensors.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepo: LocationRepository,
    private val geocodingRepo: GeocodingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LocationUiState())
    val state: StateFlow<LocationUiState> = _state

    fun setPermission(granted: Boolean) {
        _state.update { it.copy(hasPermission = granted, error = null) }
        if (!granted) stop()
    }

    fun start() {
        if (!_state.value.hasPermission) return

        viewModelScope.launch {
            locationRepo.start { lat, lon, accuracy ->
                _state.update {
                    it.copy(
                        lat = lat,
                        lon = lon,
                        accuracyMeters = accuracy,
                        lastUpdatedEpochMs = System.currentTimeMillis(),
                        error = null
                    )
                }
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val place = geocodingRepo.reverseGeocode(lat, lon)
                    if (place != null) {
                        _state.update { it.copy(placeName = place) }
                    }
                }
            }
        }
    }

    fun stop() = locationRepo.stop()

    override fun onCleared() {
        locationRepo.stop()
        super.onCleared()
    }
}