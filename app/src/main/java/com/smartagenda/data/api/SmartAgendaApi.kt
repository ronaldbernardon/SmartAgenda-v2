package com.smartagenda.data.api
import com.smartagenda.data.model.LocationResponse
import com.smartagenda.data.model.TodayResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface SmartAgendaApi {
    @GET("api/today")
    suspend fun getToday(@Header("X-Password") password: String): Response<TodayResponse>
    @GET("api/location")
    suspend fun getLocation(): Response<LocationResponse>
}
