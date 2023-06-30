package com.example.capston.Bus.realtime

data class Arrival1(
    val arrivalSec: Int,
    val busPlateNo: String,
    val busStatus: String,
    val endBusYn: String,
    val fulCarAt: String,
    val leftStation: Int,
    val lowBusYn: String,
    val waitStatus: String
)