package com.example.capston

data class Todo(
    val title: String = "",         //일정 제목
    val st_date: String = "",       //일정 시작 날짜
    val st_time: String = "",       //일정 시작 시간
    val end_date: String ?= null,   //일정 종료 날짜
    val end_time: String ?= null,   //일정 종료 시간
    val place: String? = null,      //일정 장소
    val memo: String? = null,       //일정 관련 메모
)
