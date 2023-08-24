package com.example.capston.alarm

data class AlarmItem(
    val userId: String? = null,
    val todo: String? = null,
    var notificationId: String? = null, //알람이 울리는 시간에서 앞에서 3자리를뺀 나머지
    var time: String? = null, //알람이 울리는 시간
    var timeFormat: String? = null, //알람이 울리는 시간을 0000년 00월 00일 00:00으로 나타낸것
    val startLat: Double? = null,
    val startLng: Double? = null,
    val arrivalLat: Double? = null,
    val arrivalLng: Double? = null,
    val isoDateTime: String? = null, //약속시간을 iso형식으로 나타낸것
    var timeTaken: String? = null, //약속장소까지 걸리는 시간 (초단위)
    var appointment_time: String? = null, //약속시간
    val trackingTime: String? = null, // 미리 준비하는 시간 분단위
    val type: String? = null, //이동수단
)
