package com.example.capston.Bus.realtime

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BusRealTimeAPIService {
    @GET("realtimeStation")
    fun getBusRealTime(
        @Query("apiKey") apikey: String,
        @Query("stationID") stationId: Int,
    ): Call<RealTimeArrivalBus>
}