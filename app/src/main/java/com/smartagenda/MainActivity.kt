package com.smartagenda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartagenda.data.PreferencesManager
import com.smartagenda.data.model.ServerConfig
import com.smartagenda.ui.MainViewModel
import com.smartagenda.ui.screens.HomeScreen
import com.smartagenda.ui.screens.SettingsScreen
import com.smartagenda.ui.theme.SmartAgendaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    @Inject lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SmartAgendaTheme {
                SmartAgendaApp(viewModel, preferencesManager)
            }
        }
    }
}

@Composable
fun SmartAgendaApp(viewModel: MainViewModel, preferencesManager: PreferencesManager) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // null = lecture en cours, true = montrer settings, false = montrer home
    var showSettings by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val config = preferencesManager.serverConfigFlow.first()
        // V8 : l'URL est hardcodée, on vérifie uniquement le mot de passe
        showSettings = config.password.isEmpty()
    }

    when (showSettings) {
        null -> {
            // Fond violet le temps de lire la DataStore
            Box(Modifier.fillMaxSize().background(Color(0xFF5C35A0)))
        }
        true -> {
            val config by preferencesManager.serverConfigFlow.collectAsStateWithLifecycle(
                initialValue = ServerConfig()
            )
            SettingsScreen(
                currentUrl = config.serverUrl,
                currentPassword = config.password,
                onSave = { url, pwd ->
                    viewModel.saveConfig(url, pwd)
                    showSettings = false
                },
                onBack = { /* pas de retour si pas encore configuré */ }
            )
        }
        false -> {
            HomeScreen(
                uiState = uiState,
                onRefresh = { viewModel.refresh() },
                onSettingsClick = {}
            )
        }
    }
}
