package com.example.capston.subway

import com.example.retrofit_example.retrofit2.stationTimeTableDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SubwayTimeTableService {
    @GET("subwayTimeTable")
    fun getStationTimeTableData(
        @Query("apiKey") apikey: String,
        @Query("stationID") stationID: String
    ): Call<stationTimeTableDTO>
}