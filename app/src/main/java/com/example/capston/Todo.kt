package com.example.capston

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class Todo(
    val title: String = "",         //일정 제목
    val st_date: String = "",       //일정 시작 날짜
    val st_time: String = "",       //일정 시작 시간
    val end_date: String? = null,   //일정 종료 날짜
    val end_time: String? = null,   //일정 종료 시간
    val place: String? = null,      //일정 장소
    val memo: String? = null,       //일정 관련 메모
    val startPlace: String? = null,   //일정 출발지
    val arrivePlace: String? = null,   //일정 도착지
    val totalTime: String? = null,     //걸리는 총 시간
    var todoId: String = "",             // 일정 구별 아이디
    val trackTime: String? = null,   //알림 추적 시간
    val notificationId: String? = null, //알람 구별 아이디
    val startLat: Double? = null,
    val startLng: Double? = null,
    val arrivalLat: Double? = null,
    val arrivalLng: Double? = null,
    val usingAlarm: Boolean? = null,
) : Parcelable {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        startLat = parcel.readDouble(),
        startLng = parcel.readDouble(),
        arrivalLat = parcel.readDouble(),
        arrivalLng = parcel.readDouble(),
        usingAlarm = parcel.readBoolean()
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(st_date)
        parcel.writeString(st_time)
        parcel.writeString(end_date)
        parcel.writeString(end_time)
        parcel.writeString(place)
        parcel.writeString(memo)
        parcel.writeString(startPlace)
        parcel.writeString(arrivePlace)
        parcel.writeString(totalTime)
        parcel.writeString(todoId)
        parcel.writeString(trackTime)
        parcel.writeString(notificationId)
        startLat?.let { parcel.writeDouble(it) }
        startLng?.let { parcel.writeDouble(it) }
        arrivalLat?.let { parcel.writeDouble(it) }
        arrivalLng?.let { parcel.writeDouble(it) }
        usingAlarm?.let { parcel.writeBoolean(it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Todo> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Todo {
            return Todo(parcel)
        }

        override fun newArray(size: Int): Array<Todo?> {
            return arrayOfNulls(size)
        }
    }
}
