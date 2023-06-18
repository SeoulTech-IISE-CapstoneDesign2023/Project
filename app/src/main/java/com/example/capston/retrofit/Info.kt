package com.example.capston.retrofit

data class Info(
    val busStationCount: Int,
    val busTransitCount: Int,
    val checkIntervalTime: Int,
    val checkIntervalTimeOverYn: String,
    val firstStartStation: String,
    val lastEndStation: String,
    val mapObj: String,
    val payment: Int,
    val subwayStationCount: Int,
    val subwayTransitCount: Int,
    val totalDistance: Int,
    val totalStationCount: Int,
    val totalTime: Int,
    val totalWalk: Int,
    val totalWalkTime: Int,
    val trafficDistance: Int,
    val transitIntervalTime: Int
)