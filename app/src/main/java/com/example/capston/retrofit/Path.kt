package com.example.capston.retrofit

data class Path(
    val info: Info,
    val pathType: Int,
    val subPath: List<SubPath>
)