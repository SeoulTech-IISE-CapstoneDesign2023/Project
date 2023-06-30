package com.example.capston.Bus.realtime

data class Base(
    val arsID: String,
    val `do`: String,
    val dong: String,
    val gu: String,
    val lane: List<Lane>,
    val localStationID: String,
    val stationCityCode: String,
    val stationID: Int,
    val stationName: String,
    val x: Double,
    val y: Double
)