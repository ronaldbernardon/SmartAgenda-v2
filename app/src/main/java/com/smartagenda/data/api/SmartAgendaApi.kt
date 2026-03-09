package com.smartagenda.data.api

import com.smartagenda.data.model.LocationResponse
import com.smartagenda.data.model.TodayResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SmartAgendaApi {
    @GET("api/today")
    suspend fun getToday(
        @Header("X-Password") password: String,
        @Query("lat") latitude: Double? = null,
        @Query("lon") longitude: Double? = null
    ): Response<TodayResponse>

    @GET("api/location")
    suspend fun getLocation(): Response<LocationResponse>
}
