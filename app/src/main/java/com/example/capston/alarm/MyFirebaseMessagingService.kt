package com.example.capston.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.capston.Key.Companion.DATETIME
import com.example.capston.Key.Companion.DB_ALARMS
import com.example.capston.Key.Companion.KEY_ALARMTIME
import com.example.capston.Key.Companion.KEY_DATETIME
import com.example.capston.Key.Companion.KEY_UPDATE_ROUTE
import com.example.capston.Key.Companion.MESSAGE
import com.example.capston.Key.Companion.NOTIFICATION_ID
import com.example.capston.Key.Companion.TYPE
import com.example.capston.Key.Companion.UPDATE_NOTIFICATION_ID
import com.example.capston.Key.Companion.UPDATE_ROUTE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Calendar

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var notificationId = 0
    private var dateTime = ""
    private lateinit var body: String
    private val calendar = Calendar.getInstance()
    private var type = ""

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        body = message.notification?.body ?: "" //보낸 메세지
        Log.e("fcm", body)
        //처음 알람을 생성할때
        with(getSharedPreferences(KEY_ALARMTIME, Context.MODE_PRIVATE)) {
            type = getString(TYPE, "").toString()
        }
        if (type == "") {
            //어떤 종류의 이동수단인지 받아오지 못함
            Log.e("onMessageReceived", "이동수단을 불러오지 못하였습니다.")
            return
        }
        //이 밑에는 무조건 type이 존재함
        dateTime =
            if (getSharedPreferences(KEY_DATETIME, Context.MODE_PRIVATE).contains(DATETIME)) {
                getSharedPreferences(KEY_DATETIME, Context.MODE_PRIVATE).getString(DATETIME, "")
            } else {
                Log.e("onMessageReceived", "dateTime을 불러오지 못하였습니다.")
                return
            }.toString()
        if (dateTime != "" && body != "") {
            checkAlarmTime()
        } else {
            Log.e("fcmService", "데이터를 불러오지 못함 dateTime : $dateTime body : $body")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("onNewToken", token)

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAlarmTime() {
        val year = dateTime.substring(0, 4).toInt()
        val month = dateTime.substring(4, 6).toInt()
        val day = dateTime.substring(6, 8).toInt()
        val hour = dateTime.substring(8, 10).toInt()
        val minute = dateTime.substring(10, 12).toInt()
        Log.e("scheduleNotification", "$year $month $day $hour $minute")

        //알람시간을 위한 calendar 객체 생성

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        //현재 시간과 알람 시간을 비교해서 알람 시간이 과거면은 알람 설정
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Log.e("fcmService", "현재시간보다 늦게 알람 설정은 안됨")
            return
        }

        notificationId = dateTime.substring(3).toInt()
        Log.e("fcm", "notificationId $notificationId")
        scheduleNotification(calendar, this, notificationId, body)
    }

    //스케쥴링을 위한 함수 시간을 설정하기위한 함수
    @RequiresApi(Build.VERSION_CODES.S)
    private fun scheduleNotification(
        calendar: Calendar,
        context: Context,
        notificationId: Int,
        message: String,
    ) {
        val alarmTime = calendar.timeInMillis
        Log.e("fcm", "${calendar.time}")
        calendar.add(Calendar.HOUR, -1) //1시간을 뺌
        Log.e("fcm", "${calendar.time}")
        val minusOneHour = calendar.timeInMillis
        //이동수단이 걷기가 아닐경우만 service를 통한 알람 업데이트 + 서비스를 불러오기로한 1시간전 시간이 현재시간보다 빠르게되면 그냥 지금 것을 사용
        if (minusOneHour >= System.currentTimeMillis()) {
            if (type == "car" || type == "subway") {
                val serviceIntent = Intent(context, UpdateRouteService::class.java)
                serviceIntent.putExtra(NOTIFICATION_ID, notificationId)
                val servicePendingIntent = PendingIntent.getService(
                    context,
                    notificationId,
                    serviceIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                val serviceAlarmManger =
                    context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                serviceAlarmManger.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    minusOneHour,
                    servicePendingIntent
                )
            }
        }

        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        notificationIntent.putExtra(NOTIFICATION_ID, notificationId)
        notificationIntent.putExtra(MESSAGE, message)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //알람을 주기위한 것
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
    }
}