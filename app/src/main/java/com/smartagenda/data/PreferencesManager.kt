package com.smartagenda.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.smartagenda.data.model.ServerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smartagenda_prefs")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val KEY_PASSWORD = stringPreferencesKey("password")
    }

    // URL toujours depuis AppConfig — jamais depuis les prefs
    val serverConfigFlow: Flow<ServerConfig> = context.dataStore.data.map { prefs ->
        ServerConfig(
            serverUrl = AppConfig.SERVER_URL,
            password  = prefs[KEY_PASSWORD] ?: ""
        )
    }

    suspend fun savePassword(password: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PASSWORD] = password
        }
    }

    suspend fun saveServerConfig(url: String, password: String) {
        // url ignoré — on garde AppConfig.SERVER_URL
        context.dataStore.edit { prefs ->
            prefs[KEY_PASSWORD] = password
        }
    }

    suspend fun clear() { context.dataStore.edit { it.clear() } }
}
