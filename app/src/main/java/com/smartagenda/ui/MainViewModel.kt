package com.smartagenda.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.smartagenda.data.PreferencesManager
import com.smartagenda.data.model.GpsLocation
import com.smartagenda.data.model.LocationData
import com.smartagenda.data.model.TodayResponse
import com.smartagenda.repository.Result
import com.smartagenda.repository.SmartAgendaRepository
import com.smartagenda.worker.EventNotificationReceiver
import com.smartagenda.worker.MidnightSchedulerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume

data class UiState(
    val isLoading: Boolean = false,
    val isConfigured: Boolean = false,
    val todayData: TodayResponse? = null,
    val location: LocationData? = null,
    val error: String? = null,
    val lastUpdated: String? = null,
    val usingGps: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SmartAgendaRepository,
    private val preferencesManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { checkConfiguration() }

    private fun checkConfiguration() {
        viewModelScope.launch {
            val configured = repository.isConfigured()
            _uiState.value = _uiState.value.copy(isConfigured = configured)
            if (configured) refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val gps = getGpsLocation()

            when (val r = repository.fetchToday(gps)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        todayData    = r.data,
                        isConfigured = true,
                        usingGps     = gps != null,
                        lastUpdated  = java.text.SimpleDateFormat(
                            "HH:mm", java.util.Locale.FRANCE
                        ).format(java.util.Date())
                    )
                    EventNotificationReceiver.scheduleAll(context, r.data.events)
                    MidnightSchedulerWorker.schedule(context)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = r.message
                )
                else -> {}
            }

            when (val r = repository.fetchLocation()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(location = r.data)
                else -> {}
            }
        }
    }

    /**
     * Stratégie V10.1 :
     * 1. lastLocation (instantané) si < 5 min → utilisé directement
     * 2. Sinon getCurrentLocation (timeout 8s)
     * 3. Sinon lastLocation même vieille → plutôt que rien
     */
    private suspend fun getGpsLocation(): GpsLocation? {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        // Étape 1 : lastLocation (immédiat, fonctionne GPS froid)
        val lastKnown = getLastKnownLocation(fusedClient)
        if (lastKnown != null) {
            val ageMs = System.currentTimeMillis() - lastKnown.time
            if (ageMs < 5 * 60 * 1000) {
                // Position récente (< 5 min) → on l'utilise directement
                return GpsLocation(lastKnown.latitude, lastKnown.longitude)
            }
        }

        // Étape 2 : position fraîche si lastLocation absente ou trop vieille
        val fresh = withTimeoutOrNull(8_000L) {
            suspendCancellableCoroutine { cont ->
                val cts = CancellationTokenSource()
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token
                ).addOnSuccessListener { location ->
                    cont.resume(location)
                }.addOnFailureListener {
                    cont.resume(null)
                }
                cont.invokeOnCancellation { cts.cancel() }
            }
        }

        if (fresh != null) return GpsLocation(fresh.latitude, fresh.longitude)

        // Étape 3 : lastLocation même vieille plutôt que rien
        return lastKnown?.let { GpsLocation(it.latitude, it.longitude) }
    }

    private suspend fun getLastKnownLocation(
        fusedClient: com.google.android.gms.location.FusedLocationProviderClient
    ): Location? = suspendCancellableCoroutine { cont ->
        fusedClient.lastLocation
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resume(null) }
    }

    fun saveConfig(url: String, password: String) {
        viewModelScope.launch {
            preferencesManager.saveServerConfig(url, password)
            _uiState.value = _uiState.value.copy(isConfigured = true)
            refresh()
        }
    }

    val serverConfigFlow get() = preferencesManager.serverConfigFlow
}
