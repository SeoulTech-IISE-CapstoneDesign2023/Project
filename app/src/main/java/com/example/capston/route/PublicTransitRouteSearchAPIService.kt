package com.example.capston.route

import com.example.capston.retrofit.PublicTransitRoute
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PublicTransitRouteSearchAPIService {
    @GET("searchPubTransPathT")
    fun getPublicTransitRoute(
        @Query("apiKey") apiKey: String,
        @Query("SX") startX: String,
        @Query("SY") startY: String,
        @Query("EX") endX: String,
        @Query("EY") endY: String,
        @Query("OPT") opt: Int = 0,
        @Query("SearchType") searchType: Int = 0,
        @Query("SearchPathType") searchPathType: Int = 0,
    ): Call<PublicTransitRoute>
}