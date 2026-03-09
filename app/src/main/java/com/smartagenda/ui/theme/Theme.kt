package com.smartagenda.ui.theme
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val Purple40  = Color(0xFF6650A4)
val Purple80  = Color(0xFFD0BCFF)

private val LightColors = lightColorScheme(primary = Purple40, background = Color(0xFFF2F2F7))
private val DarkColors  = darkColorScheme(primary = Purple80, background = Color(0xFF1A1A2E))

@Composable
fun SmartAgendaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
