package com.smartagenda.data.model

import com.google.gson.annotations.SerializedName

data class TodayResponse(
    @SerializedName("date") val date: String = "",
    @SerializedName("day_name") val dayName: String = "",
    @SerializedName("events") val events: List<AgendaEvent> = emptyList(),
    @SerializedName("weather") val weather: WeatherData? = null,
    @SerializedName("uv_index") val uvIndex: UvData? = null,
    @SerializedName("holiday") val holiday: String? = null,
    @SerializedName("school_vacation") val schoolVacation: String? = null,
    @SerializedName("location") val location: String? = null
)

data class AgendaEvent(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("start_time") val startTime: String = "00:00",
    @SerializedName("description") val description: String = "",
    @SerializedName("type") val type: String = "general"
)

data class WeatherData(
    @SerializedName("temperature") val temperature: Double = 0.0,
    @SerializedName("temp_min") val tempMin: Double = 0.0,
    @SerializedName("temp_max") val tempMax: Double = 0.0,
    @SerializedName("description") val description: String = "",
    @SerializedName("icon") val icon: String = "",
    @SerializedName("humidity") val humidity: Int = 0,
    @SerializedName("wind_speed") val windSpeed: Double = 0.0
)

data class UvData(
    @SerializedName("value") val value: Double = 0.0,
    @SerializedName("value_max") val valueMax: Double = 0.0,
    @SerializedName("level") val level: String = "",
    @SerializedName("color") val color: String = "#289500",
    @SerializedName("protection") val protection: String = ""
)

data class LocationData(
    @SerializedName("name") val name: String = "",
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("forecast_days") val forecastDays: Int = 7
)

data class LocationResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("location") val location: LocationData? = null
)

data class ServerConfig(val serverUrl: String = "", val password: String = "")

// Coordonnées GPS du téléphone (null = non disponibles)
data class GpsLocation(val latitude: Double, val longitude: Double)
