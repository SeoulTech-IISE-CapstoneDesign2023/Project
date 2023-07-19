package com.example.capston.subway

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SubwayTimeTableConnection {
    companion object{
        private const val BASE_URL = "https://api.odsay.com/v1/api/"
        private var INSTANCE: Retrofit? = null
        fun getInstance(): Retrofit {
            if (INSTANCE == null) {
                INSTANCE = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return INSTANCE!!
        }
    }
}