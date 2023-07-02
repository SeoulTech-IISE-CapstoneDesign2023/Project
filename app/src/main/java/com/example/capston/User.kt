package com.example.capston

import android.provider.ContactsContract.CommonDataKinds.Nickname

// 로그인 한 사용자 정보 저장하는 객체
data class User(
    val uid:String?="",  //사용자 uid
    val nickname:String?= "" //사용자 nickname
)
