package com.example.capston.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.capston.Bus.realLocation.BusRealLocationAPIService
import com.example.capston.Bus.realLocation.BusRealLocationConnection
import com.example.capston.Bus.realLocation.BusRealTimeLocationDTO
import com.example.capston.Bus.realtime.BusRealTimeAPIService
import com.example.capston.Bus.realtime.BusRealTimeConnection
import com.example.capston.Bus.realtime.RealTimeArrivalBus
import com.example.capston.Create.UserInfo
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.Key
import com.example.capston.Key.Companion.DB_ALARMS
import com.example.capston.Key.Companion.DB_USERS
import com.example.capston.Key.Companion.DB_USER_INFO
import com.example.capston.Key.Companion.KEY_UPDATE_ROUTE
import com.example.capston.Key.Companion.MESSAGE
import com.example.capston.Key.Companion.NOTIFICATION_ID
import com.example.capston.Key.Companion.UPDATE_NOTIFICATION_ID
import com.example.capston.Key.Companion.UPDATE_ROUTE
import com.example.capston.R
import com.example.capston.User
import com.example.capston.car.CarRouteRequest
import com.example.capston.car.DepartureInfo
import com.example.capston.car.DestinationInfo
import com.example.capston.car.Dto
import com.example.capston.car.RoutesInfo
import com.example.capston.car.TmapService
import com.example.capston.retrofit.PublicTransitRoute
import com.example.capston.retrofit.SubPath
import com.example.capston.route.PublicTransitRouteConnection
import com.example.capston.route.PublicTransitRouteSearchAPIService
import com.example.capston.subway.SubwayTimeTableConnection
import com.example.capston.subway.SubwayTimeTableService
import com.example.retrofit_example.retrofit2.stationTimeTableDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.concurrent.CountDownLatch

class UpdateRouteService : Service() {
    private val uid = Firebase.auth.currentUser?.uid ?: ""
    private var notificationId = 0
    private var startX: Double? = 0.0
    private var startY: Double? = 0.0
    private var endX: Double? = 0.0
    private var endY: Double? = 0.0
    private var isoDateTime: String? = ""
    private var timeTaken: String? = ""
    private var newNotificationId: String = ""
    private val carRetrofit = Retrofit.Builder()
        .baseUrl("https://apis.openapi.sk.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //대중교통에 필요한 변수
    private var minTotalTime: Int? = null
    private var info = mutableListOf<EditMappingFragment.Info>()
    private var latestTime: Int? = null
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //시작되면 retrofit을 통해 시간 정보를 확인얻어와보자 -> 자동차는 성공적으로 함 이제 대중교통만 받으면 됨 해야할것 type을 받아와서 해준다
        //알람 정보 획득
        notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        Firebase.database.reference.child(DB_ALARMS).child(uid).child(notificationId.toString())
            .get()
            .addOnSuccessListener {
                val alarmData = it.getValue(AlarmItem::class.java)
                Log.e("service", alarmData.toString())
                initVariable(alarmData)
                if ((alarmData?.type ?: "") == "car") {
                    searchCarRoute()
                } else if ((alarmData?.type ?: "") == "subway") {
                    searchSubwayRoute()
                } else {
                    Log.e("UpdateRouteService", "이동수단이 없음")
                }
            }
        stopSelf()

        return START_NOT_STICKY //서비스를 종료시키면 서비스를 재생성하지 않음
    }

    private fun initVariable(alarmData: AlarmItem?) {
        startX = alarmData?.startLng ?: 0.0
        startY = alarmData?.startLat ?: 0.0
        endX = alarmData?.arrivalLng ?: 0.0
        endY = alarmData?.arrivalLat ?: 0.0
        isoDateTime = alarmData?.isoDateTime ?: ""
        timeTaken = alarmData?.timeTaken ?: ""
    }

    private fun searchCarRoute() {
        //body생성
        val body = createCarBody()
        Log.d("body", body.toString())
        val service = carRetrofit.create(TmapService::class.java)
        service.getCarRoute(request = body).enqueue(object : Callback<Dto> {
            @RequiresApi(Build.VERSION_CODES.S)
            @SuppressLint("ScheduleExactAlarm")
            override fun onResponse(call: Call<Dto>, response: Response<Dto>) {
                Log.e("service", response.body().toString())
                //업데이트된 시간
                val newTimeTaken =
                    response.body()?.features?.map { it.properties.totalTime }.toString()
                        .replace("[", "").replace("]", "").toInt()
                //업데이트된 알람 시간
                val newAlarmTime = newAlarmTime(response) ?: ""
                Log.e("alarmTime", newAlarmTime)
                //newAlarmTime에서 추가로 trackingTime만큼 빼줘야함 데이터불러오는 곳에서 빼줌
                var oldAlarmItem: AlarmItem
                val userAlarmDB = Firebase.database.reference.child(DB_ALARMS).child(uid)
                    .child(notificationId.toString())
                userAlarmDB.get().addOnSuccessListener {
                    oldAlarmItem = it.getValue(AlarmItem::class.java)!!
                    Log.e("routeService", oldAlarmItem.toString())
                    //trackingTime을 빼주는 작업
                    val updateTime = updateTrackingTime(newAlarmTime, oldAlarmItem)
                    Log.e("routeService", "updateTime : $updateTime")
                    updateNewAlarmItem(oldAlarmItem, newTimeTaken, updateTime)
                    //시간이 동일할경우 알람을 재생성하지 않음
                    if (notificationId.toString() == oldAlarmItem.notificationId) {
                        Log.e("updateRouteService", "시간변화없음")
                        return@addOnSuccessListener
                    }
                    //새롭게얻은 정보를 firebase에서 다시 업데이트
                    userAlarmDB.removeValue() //데이터를 지움
                    updateAlarmItem(oldAlarmItem)
                    //알람을 지움
                    deleteAlarm(notificationId, applicationContext)
                    //알람을 삭제한 후 새로운 알람을 생성
                    Firebase.database.reference.child(DB_USERS).child(DB_USER_INFO).get()
                        .addOnSuccessListener { userInfo ->
                            val user = userInfo.getValue(User::class.java)
                            val body = "${user?.nickname ?: ""}님 이제 ${oldAlarmItem.todo}할 시간이에요~!"
                            // 알람을 여기서 보내줌
                            createNewAlarm(newAlarmTime, body)
                        }
                }
            }

            override fun onFailure(call: Call<Dto>, t: Throwable) {
                Log.e("fail", "실패")
                t.printStackTrace()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createNewAlarm(newAlarmTime: String, body: String) {
        //newAlarmTime이 새로운 알람시간임
        val year = newAlarmTime.substring(0, 4).toInt()
        val month = newAlarmTime.substring(4, 6).toInt()
        val day = newAlarmTime.substring(6, 8).toInt()
        val hour = newAlarmTime.substring(8, 10).toInt()
        val minute = newAlarmTime.substring(10, 12).toInt()
        Log.e("updateRouteService", "$year $month $day $hour $minute")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        val notificationIntent = Intent(this, NotificationReceiver::class.java)
        notificationIntent.putExtra(NOTIFICATION_ID, newAlarmTime.substring(3).toInt())
        Log.d("updateRouteService", newAlarmTime.substring(3))
        notificationIntent.putExtra(MESSAGE, body)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            newAlarmTime.substring(3).toInt(),
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        //알람을 주기위한 것
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun searchSubwayRoute() {
        Log.e("updateRouteService", "mintotalTime : $minTotalTime")
        getPublicTransitRouteSearchData(startX!!, startY!!, endX!!, endY!!)
        Handler().postDelayed({
            if (minTotalTime != null) {
                val hours = minTotalTime!! / 60
                val minutes = minTotalTime!! % 60
                val alarmTime =
                    String.format("%02d%02d", hours, minutes)
                Log.e("updateRouteService", "mintotalTime : $minTotalTime")
                Log.e("updateRouteService", "alarmTime : $alarmTime")
                Firebase.database.reference.child(DB_ALARMS).child(uid)
                    .child(notificationId.toString()).get()
                    .addOnSuccessListener { dataSnapshot ->
                        val oldAlarmItem = dataSnapshot.getValue(AlarmItem::class.java)
                        val newAlarmTime =
                            newAlarmTimeForSubway(oldAlarmItem, alarmTime) //time으로 들어가야함
                        //시간이 동일할경우 알람을 재생성하지 않음
                        if (notificationId.toString() == newAlarmTime.substring(3)) {
                            Log.e("updateRouteService", "시간변화없음")
                            return@addOnSuccessListener
                        }
                        //기존에 있던 데이터를 삭제
                        Firebase.database.reference.child(DB_ALARMS).child(uid)
                            .child(notificationId.toString()).removeValue()
                        //새로업데이트된 알람 정보를 업데이트하고 firebase에 업로드
                        updateAlarmDataForSubway(oldAlarmItem, newAlarmTime)
                        //알람을 지움
                        deleteAlarm(notificationId, applicationContext)
                        //알람을 지운후 새로운 알람 생성
                        Firebase.database.reference.child(DB_USERS).child(DB_USER_INFO).get()
                            .addOnSuccessListener { userInfo ->
                                val user = userInfo.getValue(User::class.java)
                                val body =
                                    "${user?.nickname ?: ""}님 이제 ${oldAlarmItem?.todo}할 시간이에요~!"
                                createNewAlarm(newAlarmTime, body)
                            }
                    }
            }
        }, 1500)
    }

    private fun createCarBody() = CarRouteRequest(
        routesInfo = RoutesInfo(
            departure = DepartureInfo(
                name = "출발지",
                lon = startX.toString(),
                lat = startY.toString()
            ),
            destination = DestinationInfo(
                name = "도착지",
                lon = endX.toString(),
                lat = endY.toString()
            ),
            predictionType = "departure",
            predictionTime = "$isoDateTime+0900"
        )
    )

    private fun newAlarmTime(response: Response<Dto>): String? {
        val rawAlarmTime =
            response.body()?.features?.map { it.properties.departureTime }?.firstOrNull()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        val zonedDateTime = ZonedDateTime.parse(rawAlarmTime, formatter)
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
    }

    private fun updateTrackingTime(newAlarmTime: String, oldAlarmItem: AlarmItem): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
        val timeForamt = SimpleDateFormat("mm")
        val date: Date = dateFormat.parse(newAlarmTime)
        calendar.time = date
        val addTime = timeForamt.parse(oldAlarmItem.trackingTime)
        calendar.set(
            Calendar.MINUTE,
            calendar.get(Calendar.MINUTE) - addTime.minutes
        )
        val modifiedDate = calendar.time
        return dateFormat.format(modifiedDate)
    }

    private fun updateNewAlarmItem(
        oldAlarmItem: AlarmItem,
        newTimeTaken: Int,
        updateTime: String
    ) {
        oldAlarmItem.timeTaken = newTimeTaken.toString()
        oldAlarmItem.time = updateTime
        oldAlarmItem.notificationId = updateTime.substring(3)
        oldAlarmItem.timeFormat = "${updateTime.substring(0, 4)}년 ${updateTime.substring(4, 6)}월 ${
            updateTime.substring(
                6,
                8
            )
        }일 ${updateTime.substring(8, 10)}:${updateTime.substring(10, 12)}"
        newNotificationId = updateTime.substring(3)
        Log.e("routeService", "새로운 정보 업데이트 : $oldAlarmItem")
    }

    private fun updateAlarmItem(oldAlarmItem: AlarmItem) {
        val alarmItem = mutableMapOf<String, Any>()
        alarmItem["todo"] = oldAlarmItem.todo.toString()
        alarmItem["time"] = oldAlarmItem.time.toString()
        alarmItem["userId"] = oldAlarmItem.userId.toString()
        alarmItem["startLat"] = oldAlarmItem.startLat ?: 0.0
        alarmItem["timeFormat"] = oldAlarmItem.timeFormat.toString()
        alarmItem["startLng"] = oldAlarmItem.startLng ?: 0.0
        alarmItem["arrivalLat"] = oldAlarmItem.arrivalLat ?: 0.0
        alarmItem["appointment_time"] = oldAlarmItem.appointment_time.toString()
        alarmItem["arrivalLng"] = oldAlarmItem.arrivalLng ?: 0.0
        alarmItem["isoDateTime"] = oldAlarmItem.isoDateTime.toString()
        alarmItem["notificationId"] = oldAlarmItem.notificationId.toString()
        alarmItem["timeTaken"] = oldAlarmItem.timeTaken.toString()
        alarmItem["trackingTime"] = oldAlarmItem.trackingTime.toString()
        Firebase.database.reference.child(DB_ALARMS).child(uid)
            .child(oldAlarmItem.notificationId.toString()).updateChildren(alarmItem)
    }

    private fun deleteAlarm(notificationId: Int, context: Context) {
        Firebase.database(Key.DB_URL).reference.child(DB_ALARMS).child(uid)
            .child(notificationId.toString()).removeValue()
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun getPublicTransitRouteSearchData(
        startX: Double,
        startY: Double,
        endX: Double,
        endY: Double,
    ) {
        val retrofitApi = PublicTransitRouteConnection.getInstance()
            .create(PublicTransitRouteSearchAPIService::class.java)
        val call = retrofitApi.getPublicTransitRoute(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            startX.toString(),
            startY.toString(),
            endX.toString(),
            endY.toString()
        )

        call.enqueue(object : Callback<PublicTransitRoute> {
            override fun onResponse(
                call: Call<PublicTransitRoute>,
                response: Response<PublicTransitRoute>
            ) {
                val data = response.body()
                val pathType = data?.result?.path?.get(1) // 1: 지하철, 2: 버스 3:버스+지하철
                val minTimePath = data?.result?.path?.minByOrNull { it.info.totalTime }
                Log.d("UpdateRouteService", minTimePath?.subPath.toString())
                var minSubPathList = mutableListOf<SubPath>()
                //최단시간이 안되면 그다음 최단시간을 불러온다
                minTotalTime = minTimePath?.info?.totalTime
                minTimePath?.subPath?.forEach { subPath ->
                    if (subPath.sectionTime == 0 && subPath.trafficType == 3) {

                    } else {
                        minSubPathList.add(subPath)
                    }
                }
                //info데이터 초기화
                info = mutableListOf()
                //비동기적이여서 쓰레드를 새로만듬
                Thread {
                    // 순차적으로 하기위해서 countdonwlatch사용
                    val countDownLatch = CountDownLatch(minSubPathList.size)
                    for (item in minSubPathList) {
                        val innerCountDownLatch = CountDownLatch(1)
                        trafficTypeCase(item) { data ->
                            info.add(data)
                            innerCountDownLatch.countDown()
                        }
                        innerCountDownLatch.await()
                        countDownLatch.countDown()
                    }
                    countDownLatch.await()
                    Log.d("UpdateRouteService", info.toString())
                    //맨처음 도착지점 같은 경우 두번째 리스트에 있는 것으로 설정
                    for (i in 0 until info.size) {
                        if (i > 0 && i < info.size - 1 && info[i].endName == null) {
                            info[i].endName = info[i + 1].startName
                        }
                        if (i > 0 && info[i].startName == null) {
                            info[i].startName = info[i - 1].endName
                        }
                    }
                    try { // info를 불러올수없는경우 거리가 가까울경우
                        info[0].startName = "출발지"
                        info[info.size - 1].endName = "도착지"
                        info[0].endName = info[1].startName
                        info[info.size - 1].startName = info[info.size - 2].endName
                        // 변경된 대기시간을 가져와야함
                        if (info.isNotEmpty()) {
                            var plusTime = 0
                            val countDownLatch2 = CountDownLatch(info.size)
                            for (data in info) {
                                plusTime += data.sectionTime!!
                                if (data.trafficType == 1 && data.waitTime != null) {
                                    getPublicTransportationData(
                                        data.subwayCode!!,
                                        data.wayCode!!,
                                        plusTime
                                    ) { time ->
                                        data.waitTime = time
                                        //최단시간에 버스및 지하철대기시간 추가해주기
                                        minTotalTime = time?.let { minTotalTime?.plus(it) }
                                    }
                                }
                                countDownLatch2.countDown()
                            }
                            countDownLatch2.await()
                            //만약 info데이터안에 지하철이나 버스의 현재 운영중인 경우가 없을 경우 그 다음 경로를 추천
                            Log.d("updateRouteService", "$info")
                        }
                    } catch (e: IndexOutOfBoundsException) {
                        Log.e("updateRouteService", "거리가 너무 가깝습니다(700m이내)")
                    }
                }.start()

            }

            override fun onFailure(call: Call<PublicTransitRoute>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun trafficTypeCase(subPath: SubPath, callback: (EditMappingFragment.Info) -> Unit) {
        val trafficType = subPath.trafficType //1-지하철, 2-버스, 3-도보
        var startName: String? = null
        var endName: String? = null
        var sectionTime: Int?
        var lane: Int? = null
        var busno: String? = null
        var subwayCode: String? = null // 지하철 코드
        var wayCode: Int? = null // 1.상행 2. 하행
        var waitTime: Int? = null
        var busId: Int? = null
        when (trafficType) {
            //지하철일때
            1 -> {
                startName = subPath.startName
                endName = subPath.endName
                sectionTime = subPath.sectionTime
                lane = subPath.lane.map { it.subwayCode }.firstOrNull()
                subwayCode = subPath.startID.toString()
                wayCode = subPath.wayCode
                Log.d("updateRouteService", "$subwayCode, $wayCode")
                getPublicTransportationData(subwayCode, wayCode) { time ->
                    waitTime = time
                    val info = EditMappingFragment.Info(
                        trafficType,
                        startName,
                        endName,
                        sectionTime,
                        lane,
                        busno,
                        subwayCode,
                        wayCode,
                        waitTime,
                        busId
                    )
                    Log.d("updateRouteService", info.toString())
                    callback(info)
                    //지금 콜백이 실행이 안되고있다
                }
            }
            //버스일때
            2 -> {
                startName = subPath.startName
                endName = subPath.endName
                busno = subPath.lane.map { it.busNo }.firstOrNull()
                sectionTime = subPath.sectionTime
                subwayCode = subPath.startID.toString()
                wayCode = subPath.wayCode
                busId = subPath.lane.map { it.busID }.firstOrNull()
                Log.d("updateRouteService", busId.toString())
                Log.d("updateRouteService", subwayCode)
                // busID를 입력하게 되면 routeID를 얻게 된다.
                getRouteId(busId!!) { routeId ->
                    if (routeId != null) {
                        getRealTimeArrivalBus(
                            subwayCode.toInt(),
                            routeId
                        ) { time -> //얻은 루트아이디와 busID를 활용해서 하면된다
                            waitTime = time
                            val info = EditMappingFragment.Info(
                                trafficType,
                                startName,
                                endName,
                                sectionTime,
                                lane,
                                busno,
                                subwayCode,
                                wayCode,
                                waitTime,
                                busId
                            )
                            callback(info)
                            Log.d("updateRouteService", info.toString())
                        }
                    } else {
                        val info = EditMappingFragment.Info(
                            trafficType,
                            startName,
                            endName,
                            sectionTime,
                            lane,
                            busno,
                            subwayCode,
                            wayCode,
                            waitTime,
                            busId
                        )
                        callback(info)
                        Log.d("updateRouteService", info.toString())
                    }
                }
            }
            //도보일때
            3 -> {
                sectionTime = subPath.sectionTime
                val info = EditMappingFragment.Info(
                    trafficType,
                    startName,
                    endName,
                    sectionTime,
                    lane,
                    busno,
                    subwayCode,
                    wayCode,
                    waitTime,
                    busId
                )
                callback(info)
                Log.d("updateRouteService", info.toString())//그다음 이쪽이 2번째로 작동
            }
        }
    }

    private fun getPublicTransportationData(
        stationId: String,
        wayCode: Int,
        plusTime: Int = 0,
        callback: (Int?) -> Unit
    ) {
        val retrofitAPI =
            SubwayTimeTableConnection.getInstance().create(SubwayTimeTableService::class.java)
        val call = retrofitAPI.getStationTimeTableData(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            stationId
        )
        call.enqueue(object : Callback<stationTimeTableDTO> {
            override fun onResponse(
                call: Call<stationTimeTableDTO>,
                response: Response<stationTimeTableDTO>
            ) {
                val waitingTimes: MutableList<String> = mutableListOf()
                val data = response.body()
                val currentTime = Calendar.getInstance()
                var hour = currentTime.get(Calendar.HOUR_OF_DAY)//핸드폰 현재 시
                var minute = currentTime.get(Calendar.MINUTE) + plusTime// 현드폰 현재 분 + 앞에있는 경로 시간
                if (minute >= 60) {
                    hour += 1
                    minute -= 60
                }
                when (wayCode) {
                    1 -> {
                        data?.result?.OrdList?.up?.time?.forEach { time ->
                            if (time.Idx == hour || (time.Idx) - 1 == hour) {
                                val timeTable = time.list // 해당 시간에 맞는 지하철 시간표
                                Log.d("updateRouteService", timeTable)
                                val regex = Regex("\\d+\\([^)]+\\)")
                                waitingTimes.addAll(
                                    regex.findAll(timeTable)
                                        .mapNotNull { matchResult ->
                                            val timeString = matchResult.value // ex) "04(동두천)"
                                            val time =
                                                timeString.substringBefore('(').toInt() // 분 부분 추출
                                            if (minute < time) {
                                                timeString.substring(0, 2)
                                            } else {
                                                null
                                            }
                                        }
                                )
                            }
                        }
                        if (waitingTimes.isNotEmpty()) {
                            latestTime = if ((waitingTimes[0].toInt() - minute) >= 0) {
                                waitingTimes[0].toInt() - minute
                            } else {
                                minute + 60 - waitingTimes[0].toInt()
                            }
                            callback(latestTime)
                        } else {
                            callback(null)
                        }
                        Log.d("updateRouteService", latestTime.toString())
                    }

                    2 -> {
                        data?.result?.OrdList?.down?.time?.forEach { time ->
                            if (time.Idx == hour || (time.Idx) - 1 == hour) {
                                val timeTable = time.list // 해당 시간에 맞는 지하철 시간표
                                Log.d("updateRouteService", timeTable)
                                val regex = Regex("\\d+\\([^)]+\\)")
                                waitingTimes.addAll(
                                    regex.findAll(timeTable)
                                        .mapNotNull { matchResult ->
                                            val timeString = matchResult.value // ex) "04(동두천)"
                                            val time =
                                                timeString.substringBefore('(').toInt() // 분 부분 추출
                                            if (minute < time) {
                                                timeString.substring(0, 2)
                                            } else {
                                                null
                                            }
                                        }
                                )
                            }
                        }
                        if (waitingTimes.isNotEmpty()) {
                            latestTime = if ((waitingTimes[0].toInt() - minute) >= 0) {
                                waitingTimes[0].toInt() - minute
                            } else {
                                minute + 60 - waitingTimes[0].toInt()
                            }
                            callback(latestTime)
                        } else {
                            callback(null)
                        }
                        Log.d("updateRouteService", latestTime.toString())
                    }
                }
            }

            override fun onFailure(call: Call<stationTimeTableDTO>, t: Throwable) {
                t.printStackTrace()
                callback(null)
            }
        })
    }

    private fun getRouteId(busId: Int, callback: (Int?) -> Unit) {
        val retrofitApi =
            BusRealLocationConnection.getInstance().create(BusRealLocationAPIService::class.java)
        val call = retrofitApi.getRouteId(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            busId
        )
        call.enqueue(object : Callback<BusRealTimeLocationDTO> {
            override fun onResponse(
                call: Call<BusRealTimeLocationDTO>,
                response: Response<BusRealTimeLocationDTO>
            ) {
                //여기서는 busId를 넣으면은 routeID를 가져오는 작업을 진행
                val data = response.body()
                val routeId =
                    data?.result?.real?.filter { it.busId == busId.toString() }?.map { it.routeId }
                        ?.firstOrNull()
                Log.d("routeId", routeId.toString())

                callback(routeId?.toInt())
            }

            override fun onFailure(call: Call<BusRealTimeLocationDTO>, t: Throwable) {
                t.printStackTrace()
                callback(null)
            }
        })
    }

    private fun getRealTimeArrivalBus(stationId: Int, routeId: Int, callback: (Int?) -> Unit) {
        val retrofitApi =
            BusRealTimeConnection.getInstance().create(BusRealTimeAPIService::class.java)
        val call = retrofitApi.getBusRealTime(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            stationId
        )
        call.enqueue(object : Callback<RealTimeArrivalBus> {
            override fun onResponse(
                call: Call<RealTimeArrivalBus>,
                response: Response<RealTimeArrivalBus>
            ) {
                val data = response.body()
                // 여기에서 실시간 데이터를 추출할때 필요한 요소는 stationID는 이미있어서 괜찮고 routeID를 얻어야함 그래서 추가로 routeID를 얻어오는 작업을 할 예정
                try {
                    val latestTime =
                        data?.result?.real?.filter { it.localRouteId.toInt() == routeId }
                            ?.map { it.arrival1.arrivalSec }?.firstOrNull() //초임
                    val latestTimeMinute = latestTime?.div(60)
                    Log.d("busLatestTime", latestTimeMinute.toString())

                    callback(latestTimeMinute)
                } catch (e: NullPointerException) {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<RealTimeArrivalBus>, t: Throwable) {
                t.printStackTrace()
                callback(null)
            }
        })
    }

    private fun newAlarmTimeForSubway(
        oldAlarmItem: AlarmItem?,
        alarmTime: String
    ): String {
        val appointmentTime = oldAlarmItem?.appointment_time ?: ""
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
        val timeFormat = SimpleDateFormat("mm")
        val takenTimeFormat = SimpleDateFormat("HHmm")
        val newTakenTime = takenTimeFormat.parse(alarmTime)
        val date: Date = dateFormat.parse(appointmentTime)
        calendar.time = date
        val addTime = timeFormat.parse(oldAlarmItem?.trackingTime ?: "")
        calendar.set(
            Calendar.MINUTE,
            calendar.get(Calendar.MINUTE) - addTime.minutes - newTakenTime.minutes
        )
        //몇시간이 걸릴수도있으니
        calendar.set(
            Calendar.HOUR,
            calendar.get(Calendar.HOUR) - newTakenTime.hours
        )
        val modifiedDate = calendar.time
        return dateFormat.format(modifiedDate)
    }

    private fun updateAlarmDataForSubway(
        oldAlarmItem: AlarmItem?,
        newAlarmTime: String
    ) {
        val alarmItem = mutableMapOf<String, Any>()
        oldAlarmItem?.let {
            alarmItem["todo"] = oldAlarmItem.todo.toString()
            alarmItem["time"] = newAlarmTime
            alarmItem["userId"] = oldAlarmItem.userId.toString()
            alarmItem["startLat"] = oldAlarmItem.startLat ?: 0.0
            alarmItem["timeFormat"] = "${newAlarmTime.substring(0, 4)}년 ${
                newAlarmTime.substring(
                    4,
                    6
                )
            }월 ${newAlarmTime.substring(6, 8)}일 ${
                newAlarmTime.substring(
                    8,
                    10
                )
            }:${newAlarmTime.substring(10, 12)}"
            alarmItem["startLng"] = oldAlarmItem.startLng ?: 0.0
            alarmItem["arrivalLat"] = oldAlarmItem.arrivalLat ?: 0.0
            alarmItem["appointment_time"] = oldAlarmItem.appointment_time.toString()
            alarmItem["arrivalLng"] = oldAlarmItem.arrivalLng ?: 0.0
            alarmItem["isoDateTime"] = oldAlarmItem.isoDateTime.toString()
            alarmItem["notificationId"] = newAlarmTime.substring(3)
            alarmItem["timeTaken"] = oldAlarmItem.timeTaken.toString()
            alarmItem["trackingTime"] = oldAlarmItem.trackingTime.toString()
            alarmItem["type"] = oldAlarmItem.type.toString()
            Firebase.database.reference.child(DB_ALARMS).child(uid)
                .child(newAlarmTime.substring(3)).updateChildren(alarmItem)
            newNotificationId = newAlarmTime.substring(3)
        }
    }
}