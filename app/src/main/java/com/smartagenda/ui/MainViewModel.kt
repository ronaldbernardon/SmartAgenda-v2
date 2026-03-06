package com.smartagenda.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartagenda.data.PreferencesManager
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
import javax.inject.Inject

data class UiState(
    val isLoading: Boolean = false,
    val isConfigured: Boolean = false,
    val todayData: TodayResponse? = null,
    val location: LocationData? = null,
    val error: String? = null,
    val lastUpdated: String? = null
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

            when (val r = repository.fetchToday()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        todayData = r.data,
                        isConfigured = true,
                        lastUpdated = java.text.SimpleDateFormat("HH:mm",
                            java.util.Locale.FRANCE).format(java.util.Date())
                    )
                    // Programmer alarmes pour aujourd'hui
                    EventNotificationReceiver.scheduleAll(context, r.data.events)
                    // Programmer job minuit pour demain
                    MidnightSchedulerWorker.schedule(context)
                }
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = r.message)
                else -> {}
            }

            when (val r = repository.fetchLocation()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(location = r.data)
                else -> {}
            }
        }
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
