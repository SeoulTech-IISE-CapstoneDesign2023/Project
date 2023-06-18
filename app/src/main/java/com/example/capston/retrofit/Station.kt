package com.example.capston.retrofit

data class Station(
    val arsID: String,
    val index: Int,
    val isNonStop: String,
    val localStationID: String,
    val stationCityCode: Int,
    val stationID: Int,
    val stationName: String,
    val stationProviderCode: Int,
    val x: String,
    val y: String
)