package com.example.weathersimulator.ui.sensors.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepo: LocationRepository,
    private val geocodingRepo: GeocodingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LocationUiState())
    val state: StateFlow<LocationUiState> = _state
    private var timeoutJob: Job? = null


    fun setPermission(granted: Boolean) {
        _state.update { it.copy(hasPermission = granted, error = null) }
        if (!granted) stop()
    }

    fun start() {
        if (!_state.value.hasPermission) return

        // Reset UI înainte de a porni
        _state.update { it.copy(error = null) }

        // Timeout: dacă nu primim niciun update în 8 sec, afișăm eroare
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(8_000)
            // dacă încă nu avem coordonate, înseamnă că nu a venit niciun callback
            if (_state.value.lat == null || _state.value.lon == null) {
                _state.update {
                    it.copy(
                        error = "Nu pot determina locația. Activează GPS și încearcă din nou."
                    )
                }
            }
        }

        viewModelScope.launch {
            locationRepo.start { lat, lon, accuracy ->
                // am primit update -> anulăm timeout
                timeoutJob?.cancel()

                _state.update {
                    it.copy(
                        lat = lat,
                        lon = lon,
                        accuracyMeters = accuracy,
                        lastUpdatedEpochMs = System.currentTimeMillis(),
                        error = null
                    )
                }

                // geocoding pe IO
                viewModelScope.launch(Dispatchers.IO) {
                    val place = geocodingRepo.reverseGeocode(lat, lon)

                    _state.update {
                        it.copy(
                            placeName = place ?: "Locație detectată"
                        )
                    }
                }
            }
        }
    }

    fun stop() {
        timeoutJob?.cancel()
        locationRepo.stop()
    }

    override fun onCleared() {
        timeoutJob?.cancel()
        locationRepo.stop()
        super.onCleared()
    }
}