package com.example.capston.Bus.realtime

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BusRealTimeConnection {
    companion object {
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