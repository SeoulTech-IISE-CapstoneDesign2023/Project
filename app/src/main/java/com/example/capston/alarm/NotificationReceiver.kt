package com.example.capston.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.capston.Key.Companion.DB_ALARMS
import com.example.capston.Key.Companion.DB_URL
import com.example.capston.Key.Companion.MESSAGE
import com.example.capston.Key.Companion.NOTIFICATION_ID
import com.example.capston.MainActivity
import com.example.capston.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        var mediaPlayer :MediaPlayer? = null
    }
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = intent.getIntExtra(NOTIFICATION_ID,0)
        val message = intent.getStringExtra(MESSAGE)

        //알람이 울리면은 파이어베이스에 있는 정보 삭제
        val userId = Firebase.auth.currentUser?.uid ?: ""
        Firebase.database(DB_URL).reference.child(DB_ALARMS).child(userId).child(notificationId.toString()).removeValue()
            .addOnSuccessListener {
                Log.e("success","userId : $userId notificationId : $notificationId")
            }
            .addOnFailureListener {
                Log.e("firebaseupdate","$it")
            }

        //채널 생성
        val name = "출발 알림"
        val descriptionText = "출발 알림입니다."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channelId = context.getString(R.string.default_notification_channel_id)
        val mChannel = NotificationChannel(channelId,name,importance)
        mChannel.description = descriptionText

        notificationManager.createNotificationChannel(mChannel)

        val notificationIntent = Intent(context,MainActivity::class.java)
        //이부분에 추가로 정보를 담을 수 있음
        //예를 들어 notificationIntent.putExtra("key","value") 이런식으로

        val pendingIntent =PendingIntent.getActivity(context,notificationId,notificationIntent,PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder =NotificationCompat.Builder(context,context.getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.baseline_access_alarm_24)
            .setContentTitle("MapMyDay")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //알림음 설정
        mediaPlayer =MediaPlayer.create(context,R.raw.alarm)
        mediaPlayer?.start()

        notificationManager.notify(notificationId,notificationBuilder.build())
    }
}