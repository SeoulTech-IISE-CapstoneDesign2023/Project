package com.example.capston.walk

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface WalkService {
    @Headers(
        "accept: application/json",
        "content-Type: application/json",
        "appKey: IZF9Qw0f7k2uAhARttjL76jEdYrQefbt5iJPZMZ8"
    )
    @POST("tmap/routes/pedestrian")
    fun getWalkingTime(
        @Query("version") version: Int = 1,
        @Body request: RouteData
    ): Call<Dto>
}

data class RouteData(
    val startX: Double,
    val startY: Double,
    val endX: Double,
    val endY: Double,
    val startName: String,
    val endName: String,
    val searchOption: Int,
)