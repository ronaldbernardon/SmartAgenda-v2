package com.smartagenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartagenda.data.AppConfig
import com.smartagenda.data.model.*
import com.smartagenda.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(uiState: UiState, onRefresh: () -> Unit, onSettingsClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📅", fontSize = 16.sp)
                        Text("SmartAgenda", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4A2890),
                    titleContentColor = Color.White
                ),
                actions = {
                    uiState.lastUpdated?.let {
                        Text(it, color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp,
                            modifier = Modifier.padding(end = 16.dp))
                    }
                }
            )
        },
        containerColor = Color(0xFFF0EEF8)
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                uiState.isLoading && uiState.todayData == null -> LoadingView()
                uiState.error != null && uiState.todayData == null ->
                    ErrorView(uiState.error, onRefresh)
                uiState.todayData != null -> {
                    val serverLocation = uiState.location?.name
                        ?.takeIf { it.isNotBlank() }
                        ?: uiState.todayData.location?.takeIf { it.isNotBlank() }
                        ?: ""
                    RefreshableContent(
                        data         = uiState.todayData,
                        locationName = serverLocation,
                        usingGps     = uiState.usingGps,
                        isRefreshing = uiState.isLoading,
                        onRefresh    = onRefresh
                    )
                }
                else -> LoadingView()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshableContent(
    data: TodayResponse,
    locationName: String,
    usingGps: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val pullState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DateCard(data, locationName, usingGps)

            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UvCard(data.uvIndex, Modifier.weight(1f).fillMaxHeight())
                WeatherCard(data.weather, Modifier.weight(1f).fillMaxHeight())
            }

            EventsCard(data.events)

            Text("v${AppConfig.VERSION}", color = Color(0xFFBBBBBB), fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = Color(0xFF7B4FBE), strokeWidth = 3.dp)
            Text("Connexion…", color = Color(0xFF7B4FBE), fontSize = 14.sp)
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFFFE0E0)),
                contentAlignment = Alignment.Center) { Text("⚠️", fontSize = 30.sp) }
            Text("Connexion impossible", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(message, textAlign = TextAlign.Center, color = Color(0xFF666666),
                fontSize = 13.sp, lineHeight = 20.sp)
            Button(onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B4FBE)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)) {
                Text("Réessayer", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ═══ CARTE DATE ═══════════════════════════════
@Composable
fun DateCard(data: TodayResponse, locationName: String, usingGps: Boolean = false) {
    val months = listOf("","janvier","février","mars","avril","mai","juin",
        "juillet","août","septembre","octobre","novembre","décembre")
    val parts     = data.date.split("-")
    val day       = parts.getOrNull(2)?.toIntOrNull() ?: 0
    val month     = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val year      = parts.getOrNull(0) ?: ""
    val dayName   = data.dayName.replaceFirstChar { it.uppercase() }
    val monthName = if (month in 1..12) months[month] else ""
    val hasHoliday  = !data.holiday.isNullOrEmpty()
    val hasVacation = !data.schoolVacation.isNullOrEmpty()

    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF6A35C2), Color(0xFF4A7FD4))))) {

        Box(Modifier.matchParentSize()) {
            Box(Modifier.size(110.dp).offset(x = (-28).dp, y = (-28).dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.07f)))
            Box(Modifier.size(70.dp).align(Alignment.TopEnd).offset(x = 18.dp, y = (-18).dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.05f)))
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(dayName, color = Color.White.copy(alpha = 0.80f),
                    fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("$day", color = Color.White,
                    fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(monthName, color = Color.White,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(year, color = Color.White.copy(alpha = 0.70f), fontSize = 13.sp)
            }

            // Ville + indicateur GPS
            if (locationName.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // 📍 fixe si config serveur, 🛰️ si position GPS du téléphone
                    Text(if (usingGps) "🛰️" else "📍", fontSize = 12.sp)
                    Text(locationName, color = Color.White.copy(alpha = 0.88f),
                        fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    if (usingGps) {
                        Text("· GPS", color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp)
                    }
                }
            }

            if (hasHoliday || hasVacation) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)) {
                    Text(if (hasHoliday) "🎉" else "🎒", fontSize = 14.sp)
                    Text(
                        if (hasHoliday) data.holiday!! else data.schoolVacation!!,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ═══ CARTE UV ════════════════════════════════
// Couleurs officielles OMS par niveau d'indice UV
fun uvColors(value: Double): Pair<Color, Color> {
    // Retourne Pair(couleurFond, couleurTexte)
    return when {
        value < 3  -> Pair(Color(0xFF289500), Color.White)   // Vert  — Faible
        value < 6  -> Pair(Color(0xFFF7E400), Color(0xFF3A3000)) // Jaune — Modéré (texte foncé)
        value < 8  -> Pair(Color(0xFFF85900), Color.White)   // Orange — Élevé
        value < 11 -> Pair(Color(0xFFD8001D), Color.White)   // Rouge  — Très élevé
        else       -> Pair(Color(0xFF6B49C8), Color.White)   // Violet — Extrême
    }
}

@Composable
fun UvCard(uv: UvData?, modifier: Modifier = Modifier) {
    val uvValue = uv?.value ?: 0.0
    val (bgColor, textColor) = uvColors(uvValue)
    val bgColorLight = bgColor.copy(alpha = 0.75f)

    Box(modifier.clip(RoundedCornerShape(18.dp))
        .background(Brush.linearGradient(listOf(bgColor, bgColorLight)))
        .padding(horizontal = 14.dp, vertical = 10.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("☀️ Indice UV", color = textColor.copy(alpha = 0.85f), fontSize = 12.sp,
                fontWeight = FontWeight.Bold)
            if (uv == null) {
                Text("—", color = textColor, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Text("Indisponible", color = textColor.copy(alpha = 0.75f), fontSize = 11.sp)
            } else {
                Text(uv.value.toString().let {
                    if (it.endsWith(".0")) it.dropLast(2) else it.replace(".", ",") },
                    color = textColor, fontSize = 34.sp,
                    fontWeight = FontWeight.Black, lineHeight = 38.sp)
                Text(uv.level, color = textColor, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold)
                // Max journalier en petit — sur une ligne pour ne pas agrandir la carte
                if (uv.valueMax > 0.0) {
                    val maxStr = uv.valueMax.toString().let {
                        if (it.endsWith(".0")) it.dropLast(2) else it.replace(".", ",") }
                    Text("max $maxStr", color = textColor.copy(alpha = 0.70f),
                        fontSize = 10.sp, lineHeight = 12.sp)
                }
                if (uv.protection.isNotEmpty())
                    Text(uv.protection, color = textColor.copy(alpha = 0.85f), fontSize = 10.sp,
                        textAlign = TextAlign.Center, lineHeight = 13.sp)
            }
        }
    }
}

// ═══ CARTE MÉTÉO ═════════════════════════════
@Composable
fun WeatherCard(weather: WeatherData?, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(18.dp))
        .background(Brush.linearGradient(listOf(Color(0xFFBBDEFB), Color(0xFF90CAF9))))
        .padding(horizontal = 14.dp, vertical = 10.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("🌤 Météo", color = Color(0xFF0D47A1), fontSize = 12.sp,
                fontWeight = FontWeight.Bold)
            if (weather == null) {
                Text("—", color = Color(0xFF0D47A1), fontSize = 32.sp, fontWeight = FontWeight.Black)
                Text("Indisponible", color = Color(0xFF1565C0), fontSize = 11.sp)
            } else {
                Text(weather.icon, fontSize = 22.sp, lineHeight = 26.sp)
                Text("${weather.temperature.toInt()}°", color = Color(0xFF0D47A1),
                    fontSize = 30.sp, fontWeight = FontWeight.Black, lineHeight = 34.sp)
                Text("${weather.tempMin.toInt()}° – ${weather.tempMax.toInt()}°",
                    color = Color(0xFF1565C0), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(weather.description, color = Color(0xFF1565C0), fontSize = 10.sp,
                    textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("💧${weather.humidity}%", color = Color(0xFF1565C0), fontSize = 10.sp)
                    Text("💨${weather.windSpeed.toInt()}km/h", color = Color(0xFF1565C0), fontSize = 10.sp)
                }
            }
        }
    }
}

// ═══ CARTE ÉVÉNEMENTS ════════════════════════
@Composable
fun EventsCard(events: List<AgendaEvent>) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
        .background(Color.White).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
            Text("Agenda du jour", color = Color(0xFF1A1A2E), fontSize = 15.sp,
                fontWeight = FontWeight.Bold)
            if (events.isNotEmpty())
                Box(Modifier.clip(CircleShape).background(Color(0xFF7B4FBE))
                    .padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("${events.size}", color = Color.White, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold)
                }
        }
        if (events.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF5F0FF)).padding(12.dp)) {
                Text("✅", fontSize = 18.sp)
                Text("Aucun événement aujourd'hui", color = Color(0xFF7B4FBE),
                    fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            events.forEachIndexed { index, event ->
                EventRow(event)
                if (index < events.size - 1)
                    Divider(Modifier.padding(start = 44.dp),
                        color = Color(0xFFF0F0F0), thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun EventRow(event: AgendaEvent) {
    val dotColor = when (event.type) {
        "travail"  -> Color(0xFF2196F3)
        "santé"    -> Color(0xFFE53935)
        "loisirs"  -> Color(0xFF43A047)
        "réunion"  -> Color(0xFF9C27B0)
        else       -> Color(0xFF7B4FBE)
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(34.dp)) {
            Text(event.startTime, color = Color(0xFF999999), fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(2.dp))
            Box(Modifier.size(7.dp).clip(CircleShape).background(dotColor))
        }
        Text(event.title, color = Color(0xFF1A1A2E), fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}
