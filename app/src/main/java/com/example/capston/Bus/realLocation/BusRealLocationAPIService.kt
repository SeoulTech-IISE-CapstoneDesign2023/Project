package com.example.capston.Bus.realLocation

import com.google.android.gms.common.api.internal.ApiKey
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BusRealLocationAPIService {
    @GET("realtimeRoute")
    fun getRouteId(
        @Query("apiKey") apiKey: String,
        @Query("busID") busId: Int
    ): Call<BusRealTimeLocationDTO>
}