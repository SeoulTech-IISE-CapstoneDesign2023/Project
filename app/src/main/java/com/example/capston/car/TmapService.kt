package com.example.capston.car

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface TmapService {
    @Headers(
        "accept: application/json",
        "content-Type: application/json",
        "appKey: IZF9Qw0f7k2uAhARttjL76jEdYrQefbt5iJPZMZ8"
    )
    @POST("tmap/routes/prediction")
    fun getCarRoute(
        @Query("version") version: Int = 1,
        @Query("resCoordType") resCoordType: String = "WGS84GEO",
        @Query("reqCoordType") reqCoordType: String = "WGS84GEO",
        @Query("sort") sort : String = "index",
        @Query("callback") callback : String = "function",
        @Query("totalValue") totalValue: Int = 2,
        @Body request: CarRouteRequest
    ): Call<Dto>
}

data class CarRouteRequest(
    val routesInfo: RoutesInfo
)

data class RoutesInfo(
    val departure: DepartureInfo,
    val destination: DestinationInfo,
    val predictionType: String,
    val predictionTime: String
)

data class DepartureInfo(
    val name: String,
    val lon: String,
    val lat: String
)

data class DestinationInfo(
    val name: String,
    val lon: String,
    val lat: String
)