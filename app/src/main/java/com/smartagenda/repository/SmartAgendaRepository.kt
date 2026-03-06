package com.smartagenda.repository

import com.smartagenda.data.AppConfig
import com.smartagenda.data.PreferencesManager
import com.smartagenda.data.api.ApiClient
import com.smartagenda.data.model.LocationData
import com.smartagenda.data.model.TodayResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class SmartAgendaRepository @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    private fun api() = ApiClient.create(AppConfig.SERVER_URL)

    suspend fun fetchToday(): Result<TodayResponse> {
        return try {
            val password = preferencesManager.serverConfigFlow.first().password
            val response = api().getToday(password)
            if (response.isSuccessful) {
                response.body()?.let { Result.Success(it) } ?: Result.Error("Réponse vide")
            } else {
                when (response.code()) {
                    401  -> Result.Error("Mot de passe incorrect")
                    500  -> Result.Error("Erreur serveur (500)")
                    else -> Result.Error("Erreur HTTP ${response.code()}")
                }
            }
        } catch (e: java.net.ConnectException) {
            Result.Error("Impossible de joindre le serveur\nVérifiez votre VPN WireGuard")
        } catch (e: java.net.SocketTimeoutException) {
            Result.Error("Délai dépassé — VPN actif ?")
        } catch (e: Exception) {
            Result.Error("Erreur : ${e.localizedMessage}")
        }
    }

    // Ville depuis /api/location — source de vérité
    suspend fun fetchLocation(): Result<LocationData> {
        return try {
            val response = api().getLocation()
            if (response.isSuccessful) {
                val loc = response.body()?.location
                if (loc != null && loc.name.isNotEmpty())
                    Result.Success(loc)
                else
                    Result.Error("Lieu absent")
            } else Result.Error("HTTP ${response.code()}")
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Erreur")
        }
    }

    suspend fun isConfigured(): Boolean {
        val config = preferencesManager.serverConfigFlow.first()
        return config.password.isNotEmpty()
    }
}
