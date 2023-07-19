package com.example.capston.Bus.realtime

data class Lane(
    val busCityCode: Int,
    val busCityName: String,
    val busDirectionName: String,
    val busDirectionStationID: Int,
    val busDirectionType: Int,
    val busEndPoint: String,
    val busFirstTime: String,
    val busID: Int,
    val busInterval: String,
    val busLastTime: String,
    val busLocalBlID: String,
    val busNo: String,
    val busStartPoint: String,
    val busStationIdx: Int,
    val type: Int
)