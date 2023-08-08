package com.example.capston.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.capston.Create.CreateActivity
import com.example.capston.Key.Companion.DATETIME
import com.example.capston.Key.Companion.DB_ALARMS
import com.example.capston.Key.Companion.DB_URL
import com.example.capston.Key.Companion.KEY_DATETIME
import com.example.capston.Key.Companion.MESSAGE
import com.example.capston.Key.Companion.NOTIFICATION_ID
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Calendar

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var notificationId = 0
    private var dateTime = ""
    private lateinit var body: String
    private val calendar = Calendar.getInstance()


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        body = message.notification?.body ?: "" //보낸 메세지
        //todo 시간을 가져와야함
        dateTime = if(getSharedPreferences(KEY_DATETIME,Context.MODE_PRIVATE).contains(DATETIME)){
            getSharedPreferences(KEY_DATETIME,Context.MODE_PRIVATE).getString(DATETIME,"")
        } else {
            Toast.makeText(applicationContext,"dateTime을 불러오지 못하였습니다.",Toast.LENGTH_SHORT).show()
            return
        }.toString()

        if (dateTime != "" && body != ""){
            checkAlarmTime(body)
        }else {
            Log.e("fcmService","데이터를 불러오지 못함 dateTime : $dateTime body : $body")
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("service",token)

    }

    private fun checkAlarmTime(message:String) {
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
            Log.e("fcmService","현재시간보다 늦게 알람 설정은 안됨")
            return
        }

        notificationId = dateTime.substring(3).toInt()
        scheduleNotification(calendar, this, notificationId, body)
    }

    //스케쥴링을 위한 함수 시간을 설정하기위한 함수
    private fun scheduleNotification(
        calendar: Calendar,
        context: Context,
        notificationId: Int,
        message: String,
    ) {
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

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}