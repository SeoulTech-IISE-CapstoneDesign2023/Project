package com.example.capston.Create

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.commit
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.EditFragment.EditTodoFragment
import com.example.capston.Key
import com.example.capston.Key.Companion.ALARMTIME
import com.example.capston.Key.Companion.ARRIVAL_DATE_FOR_SEARCHACTIVITY
import com.example.capston.Key.Companion.ARRIVAL_TIME_FOR_SEARCHACTIVITY
import com.example.capston.Key.Companion.DATETIME
import com.example.capston.Key.Companion.DB_ALARMS
import com.example.capston.Key.Companion.DB_CALENDAR
import com.example.capston.Key.Companion.DB_URL
import com.example.capston.Key.Companion.DB_USERS
import com.example.capston.Key.Companion.DB_USER_INFO
import com.example.capston.Key.Companion.KEY_ALARMTIME
import com.example.capston.Key.Companion.KEY_DATETIME
import com.example.capston.Key.Companion.KEY_USING_ALARM
import com.example.capston.Key.Companion.PLUS_TIME
import com.example.capston.Key.Companion.START_DATE_FOR_SEARCHACTIVITY
import com.example.capston.Key.Companion.START_TIME_FOR_SEARCHACTIVITY
import com.example.capston.Key.Companion.TIME_TAKEN
import com.example.capston.Key.Companion.TYPE
import com.example.capston.Key.Companion.USING_ALARM
import com.example.capston.MainActivity
import com.example.capston.R
import com.example.capston.Todo
import com.example.capston.alarm.NotificationReceiver
import com.example.capston.databinding.ActivityCreateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateActivity : AppCompatActivity(),
    EditTodoFragment.OnDataPassListener,
    EditMappingFragment.OnDataPassListener {
    private lateinit var binding: ActivityCreateBinding
    private var todoKeys: java.util.ArrayList<String> = arrayListOf()   //일정 키 목록
    private lateinit var user: String
    private var startAddress = ""
    private var arrivalAddress = ""
    private var dateString = ""
    private var timeString = ""
    private var title = ""
    private var startTime = ""
    private var arrivalDate = ""
    private var arrivalTime = ""
    private var editTextLength = 0
    private var editTextPlace = ""
    private var editTextMemo = ""
    private var editStartPlace = ""
    private var editArrivePlace = ""
    private var isEditMode = false  // 편집 모드 여부를 나타내는 변수
    private var currentUserFcmToken = ""
    private var nickname = ""
    private var notificationId = ""
    private var plusTime = ""
    private val calendarForAlarm = Calendar.getInstance()
    private var startLat = 0.0
    private var startLng = 0.0
    private var arrivalLat = 0.0
    private var arrivalLng = 0.0
    private var isoDateTime = ""
    private var timeTaken = ""
    private var startTimeToLong: Long = 0 //약속시간을 저장하는 변수
    private var type = "" //이동수단을 저장하는 변수
    private var alarmCheck = true
    private var touchedTodoButton = true

    //권한요청 할 때 필요함
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            //알림권한 없음 -> 설정창으로 한번더 보내서 알림 권한 하라고 요청
            Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)
        user = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        //알림을 설정할건지 permission창
        askNotificationPermission()

        //fcmToken정보 받기
        val userDB = Firebase.database.reference.child(DB_USERS).child(user).child(DB_USER_INFO)
        userDB.get().addOnSuccessListener {
            val userInfo = it.getValue(UserInfo::class.java)
            currentUserFcmToken = userInfo?.fcmToken ?: ""
            nickname = userInfo?.nickname ?: ""
        }
        //list에서 일정 하나 선택했을 때 내용 수정
        val todo = intent.getParcelableExtra<Todo>("todo")
        var startDate = intent.getStringExtra("startDate") // "yyyy/MM/dd" 형식으로 받음
        Log.e("createActivity", startDate.toString())
        if (todo != null) {
            // 기존의 Todo를 수정하는 경우, Todo객체를 사용하여 화면을 초기화
            Log.d("DataPass", "time is :${todo.st_time}")
            initializeEditMode(todo)
        } else {
            //searActivity에서 다시 돌아올때 화면이 깨지는 에러 방지용
            if (startDate == null) {
                with(getSharedPreferences("date", Context.MODE_PRIVATE)) {
                    startDate = getString(START_DATE_FOR_SEARCHACTIVITY, "").toString()
                    startTime = getString(START_TIME_FOR_SEARCHACTIVITY, "").toString()
                    arrivalTime = getString(ARRIVAL_TIME_FOR_SEARCHACTIVITY, "").toString()
                    arrivalDate = getString(ARRIVAL_DATE_FOR_SEARCHACTIVITY, "").toString()
                    title = getString("title1", "").toString()
                }
            }
            // 새로운 Todo를 생성하는 경우, 화면을 초기화
            initializeCreateMode(startDate!!)
        }
        getData() //todo 임시로 저장해놓으거임 firebase로 넣어줘야함
        val fManager = supportFragmentManager
        val mappingFragment = EditMappingFragment.newInstance(startAddress, arrivalAddress)
        fManager.commit {
            add(binding.frameLayout.id, EditTodoFragment())
        }
        val fragmentToShow = intent.getStringExtra("fragmentToShow")
        if (fragmentToShow == "mappingFragment") {
            fManager.commit {
                replace(binding.frameLayout.id, mappingFragment)
            }
        }

        //일정 fragment
        binding.goTodoButton.setOnClickListener {
            val currentFragment = fManager.findFragmentById(binding.frameLayout.id)
            if (currentFragment !is EditTodoFragment) {
                fManager.commit {
                    replace(binding.frameLayout.id, EditTodoFragment())
                }
            }
            touchedTodoButton = true
        }
        //mapping fragment
        binding.goMappingButton.setOnClickListener {
            val currentFragment = fManager.findFragmentById(binding.frameLayout.id)
            if (currentFragment !is EditMappingFragment) {
                fManager.commit {
                    replace(binding.frameLayout.id, mappingFragment)
                }
            }
            touchedTodoButton = false
        }
        binding.startDateValueTextView.setOnClickListener {
            setDate(0)
        }
        binding.arriveDateValueTextView.setOnClickListener {
            setDate(1)
        }
        binding.startTimeValueTextView.setOnClickListener {
            setTime(0)
        }
        binding.arriveTimeValueTextView.setOnClickListener {
            setTime(1)
        }

        whenDateTimeValueChangedSaveDateData()

        saveNotificationIdWhenEditModeForEditAlarm()
        saveDateData()
    }

    private fun whenDateTimeValueChangedSaveDateData() {
        binding.startDateValueTextView.addTextChangedListener {
            saveDateData()
        }

        binding.startTimeValueTextView.addTextChangedListener {
            saveDateData()
        }
        binding.arriveDateValueTextView.addTextChangedListener {
            saveDateData()
        }

        binding.arriveTimeValueTextView.addTextChangedListener {
            saveDateData()
        }
    }

    private fun saveNotificationIdWhenEditModeForEditAlarm() {
        //notifiactionId 저장 -> editMode일때 알람을 수정하기 위해서
        val changeStartTime =
            binding.startTimeValueTextView.text.toString().replace("오후", "").replace("오전", "")
                .replace(":", "").replace(" ", "")
        val split = splitDate(binding.startDateValueTextView.text.toString())
        val year = split[0].trim().toInt()
        val month = split[1].trim().toInt()
        val day = split[2].trim().toInt()
        val date = String.format("%04d%02d%02d", year, month, day)
        notificationId = "$date$changeStartTime".substring(3)
    }

    private fun initializeEditMode(todo: Todo) {
        // 기존의 Todo를 수정하는 경우, 해당 Todo의 정보를 사용하여 화면을 초기화
        isEditMode = true  // 기존 Todo를 수정하는 편집 모드임을 나타냄
        // Todo의 제목
        binding.editTodoText.setText(todo.title)
        // 시작 날짜 및 시간
        binding.startDateValueTextView.text = todo.st_date
        binding.startTimeValueTextView.text = todo.st_time
        // 도착 날짜 및 시간
        binding.arriveDateValueTextView.text = todo.end_date
        binding.arriveTimeValueTextView.text = todo.end_time
    }

    private fun initializeCreateMode(startDate: String?) {
        // 새로운 일정을 생성하는 경우, 화면을 초기화하는 작업 수행
        binding.editTodoText.setText(if (title != "") title else "")
        binding.startDateValueTextView.text = startDate
        binding.startTimeValueTextView.text = if (startTime != "") startTime else "오전 00:00"
        binding.arriveDateValueTextView.text = if (arrivalDate != "") arrivalDate else "0000/00/00"
        binding.arriveTimeValueTextView.text = if (arrivalTime != "") arrivalTime else "오전 00:00"
    }

    private fun convertToNumericInt(inputString: String): Long {
        // 정규식으로 "/", ":", 공백을 제거하고 숫자만 남김
        val numericString = inputString.replace("[/:\\s]".toRegex(), "")

        return numericString.toLong()
    }

    // 일정 생성 할 때 날짜 체크
    private fun checkDate(): Boolean {
        val changeStartTime =
            binding.startTimeValueTextView.text.toString().replace("오후", "").replace("오전", "")

        val changeArrivalTime =
            binding.arriveTimeValueTextView.text.toString().replace("오후", "").replace("오전", "")

        val startTimeText =
            binding.startDateValueTextView.text.toString() + changeStartTime
        val arrivalTimeText =
            binding.arriveDateValueTextView.text.toString() + changeArrivalTime

        startTimeToLong = convertToNumericInt(startTimeText)
        val arrivalTimeToLong = convertToNumericInt(arrivalTimeText)
        Log.e("날짜 확인", startTimeToLong.toString())
        Log.e("날짜 확인", arrivalTimeToLong.toString())
        return startTimeToLong < arrivalTimeToLong
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val todoKey = intent.getStringExtra("todoKey")
        val todo = intent.getParcelableExtra<Todo>("todo")
        var usingAlarm: Boolean
        with(getSharedPreferences(KEY_USING_ALARM, Context.MODE_PRIVATE)) {
            usingAlarm = getBoolean(USING_ALARM, false)
        }
        Log.e("usingAlarm", "$usingAlarm")
        getLocationData()

        return when (item.itemId) {
            R.id.okMenu -> {
                // 일정 시작시간이 종료시간보다 늦을 경우 일정 생성 불가 토스트
                if (!checkDate()) {
                    Toast.makeText(this, "시작시간은 종료시간보다 늦을 수 없습니다", Toast.LENGTH_SHORT).show()
                } else {
                    // 메모장 텍스트 100자 넘어가면 일정 생성 불가 토스트
                    if (editTextLength > 100) {
                        Toast.makeText(this, "메모 글자수가 100을 넘었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        if (isEditMode) {
                            //기존의 todo를 수정하고 알림은 사용하지 않는 경우
                            if (!usingAlarm) {
                                updateTodo(todoKey!!, todo!!)
                                finish()
                            } else {
                                //기존의 todo를 수정하면서 알람도 사용하는 경우
                                createAlarm()
                                if (alarmCheck) {
                                    updateTodo(todoKey!!, todo!!)
                                    deleteAlarmWhenEdit()
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "지금 출발하기에는 이미 늦었습니다. 일정을 다시 생성해주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            // 새로운 Todo를 생성하는 경우
                            //새로운 todo를 생성하면서 알림은 없는 경우
                            if (!usingAlarm) {
                                createTodo()
                                finish()
                            } else {
                                //새로운 todo를 생성하면서 알림도 있는 경우
                                createAlarm()
                                if (alarmCheck) {
                                    createTodo()
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "지금 출발하기에는 이미 늦었습니다. 일정을 다시 생성해주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
                true
            }

            R.id.cancelMenu -> {
                showAlertDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getLocationData() {
        //위경도와 iso시간 가져옴
        getSharedPreferences("location", MODE_PRIVATE).apply {
            startLat = getString("startLat", "0.0")!!.toDouble()
            startLng = getString("startLng", "0.0")!!.toDouble()
            arrivalLat = getString("arrivalLat", "0.0")!!.toDouble()
            arrivalLng = getString("arrivalLng", "0.0")!!.toDouble()
            isoDateTime = getString("isoDateTime", "").toString()
        }
    }

    private fun deleteAlarmWhenEdit() {
        Log.e("deleteAlarmWhenEdit", "uid : $user notificationId : $notificationId")
        Firebase.database.reference.child(DB_ALARMS).child(user).child(notificationId).removeValue()
        val notificationIntent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId.toInt(),
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun createAlarm() {
        //mapping에서 시간가져오기
        var alarmTime = ""

        with(getSharedPreferences(KEY_ALARMTIME, Context.MODE_PRIVATE)) {
            alarmTime = getString(ALARMTIME, "").toString()
            type = getString(TYPE, "").toString()
            plusTime = getString(PLUS_TIME, "").toString()
            timeTaken = getString(TIME_TAKEN, "").toString()
        }
        Log.e("alarmTimeinCreateAlarm", "$alarmTime $type $plusTime $timeTaken")
        //잘 가져온것을 파악
        val todo = binding.editTodoText.text.toString()
        val alarm = mutableMapOf<String, Any>()
        //1번 자동차일 경우 받아온 시간을 그대로 변경해주면 됨 2,3번일 경우 받아온 시간만큼 빼주면 됨 우선은 자동차같은 경우는 2023/08/23 16:35 이런식으로 되어있고 나머지는 12:45이런식으로 되어있음
        when (type) {
            "car" -> {
                makeCarAlarm(alarmTime, alarm, todo)
            }

            "walk" -> {
                makeWalkAlarm(alarmTime, alarm, todo)
            }

            "subway" -> {
                makeWalkAlarm(alarmTime, alarm, todo)
            }
        }
    }

    //도보 알람만들기
    private fun makeWalkAlarm(
        alarmTime: String,
        alarm: MutableMap<String, Any>,
        todo: String
    ) {
        //받은 시간은 00:00 이런 형태임 그러면은 시작시간에서 그만큼 빼주면 됨
        val changeStartTime =
            binding.startTimeValueTextView.text.toString().replace("오후", "").replace("오전", "")
                .replace(":", "").replace(" ", "")
        val split = splitDate(binding.startDateValueTextView.text.toString())
        val year = split[0].trim().toInt()
        val month = split[1].trim().toInt()
        val day = split[2].trim().toInt()
        val date = String.format("%04d%02d%02d", year, month, day)
        val time = "$date$changeStartTime"
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
        try {
            val targetTime: Date = dateFormat.parse(time)
            calendarForAlarm.time = targetTime
            Log.e("aa", "${calendarForAlarm.time}")
            val timeFormat = SimpleDateFormat("HH:mm")
            val minusTime = timeFormat.parse(alarmTime)
            val minuteFormat = SimpleDateFormat("mm")
            val addTime = minuteFormat.parse(plusTime)
            calendarForAlarm.set(
                Calendar.HOUR_OF_DAY,
                calendarForAlarm.get(Calendar.HOUR_OF_DAY) - minusTime.hours
            )
            Log.e("aa", "${calendarForAlarm.time}")
            calendarForAlarm.set(
                Calendar.MINUTE,
                calendarForAlarm.get(Calendar.MINUTE) - minusTime.minutes - addTime.minutes
            )
            Log.e("aa", "${calendarForAlarm.time}")

            val modifiedDate = calendarForAlarm.time
            Log.e("modifiedDate", "$modifiedDate")
            val outputDate = dateFormat.format(modifiedDate)
            Log.e("modifiedDate", "$outputDate")
            //현재 시간과 알람 시간을 비교해서 알람 시간이 과거면은 알람 설정
            Log.e(
                "시간 체크",
                "알람시간 : ${calendarForAlarm.timeInMillis} 현재시간 : ${System.currentTimeMillis()}"
            )
            if (calendarForAlarm.timeInMillis <= System.currentTimeMillis()) {
                alarmCheck = false
                Log.e("createActivity에서 알람 체크 확인", "현재시간보다 늦게 알람 설정은 안됨")
                return
            }
            alarmCheck = true
            alarm["todo"] = todo
            alarm["time"] = outputDate //알람이 울리는 시간
            alarm["timeFormat"] = "${outputDate.substring(0, 4)}년 ${
                outputDate.substring(
                    4,
                    6
                )
            }월 ${outputDate.substring(6, 8)}일 ${outputDate.substring(8, 10)}:${
                outputDate.substring(
                    10,
                    12
                )
            }"//2023년 08월 15일 15:49
            alarm["userId"] = user
            alarm["startLat"] = startLat
            alarm["startLng"] = startLng
            alarm["arrivalLat"] = arrivalLat
            alarm["arrivalLng"] = arrivalLng
            alarm["type"] = type
            alarm["appointment_time"] = startTimeToLong.toString() //startTime을 가져오면됨
            notificationId = outputDate.substring(3)
            alarm["notificationId"] = notificationId
            alarm["timeTaken"] = alarmTime
            alarm["trackingTime"] = plusTime
            //알람 데이터 업데이트 기기에 저장을 하고 서비스에서 받음
            with(getSharedPreferences(KEY_DATETIME, MODE_PRIVATE).edit()) {
                putString(DATETIME, outputDate)
                apply()
            }
            Firebase.database(DB_URL).reference.child(DB_ALARMS).child(user).child(notificationId)
                .updateChildren(alarm)
            //알람 생성
            val body = "${nickname}님 이제 ${todo}할 시간이에요~!"
            Toast.makeText(this, "알람을 생성하였습니다", Toast.LENGTH_SHORT).show()
            sendFcm(body)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("걷기시간 변형", "시간변형에 실패")
        }
    }

    //자동차 알람만들기
    private fun makeCarAlarm(
        alarmTime: String,
        alarm: MutableMap<String, Any>,
        todo: String
    ) {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
        val timeForamt = SimpleDateFormat("mm")
        val time =
            alarmTime.replace("/", "").replace(" ", "").replace(":", "") //202308231635 이런식으로 바꿔줌
        val date: Date = dateFormat.parse(time)
        calendarForAlarm.time = date
        val addTime = timeForamt.parse(plusTime)
        calendarForAlarm.set(
            Calendar.MINUTE,
            calendarForAlarm.get(Calendar.MINUTE) - addTime.minutes
        )
        val modifiedDate = calendarForAlarm.time
        val resultTime = dateFormat.format(modifiedDate)

        //현재 시간과 알람 시간을 비교해서 알람 시간이 과거면은 알람 설정
        Log.e(
            "시간 체크",
            "알람시간 : ${calendarForAlarm.timeInMillis} 현재시간 : ${System.currentTimeMillis()}"
        )
        if (calendarForAlarm.timeInMillis <= System.currentTimeMillis()) {
            alarmCheck = false
            Log.e("createActivity에서 알람 체크 확인", "현재시간보다 늦게 알람 설정은 안됨")
            return
        }
        alarmCheck = true
        alarm["todo"] = todo
        alarm["time"] = resultTime
        alarm["userId"] = user
        alarm["startLat"] = startLat
        alarm["type"] = type
        alarm["timeFormat"] = "${resultTime.substring(0, 4)}년 ${resultTime.substring(4, 6)}월 ${
            resultTime.substring(
                6,
                8
            )
        }일 ${resultTime.substring(8, 10)}:${resultTime.substring(10, 12)}"//2023년 08월 15일 15:49
        alarm["startLng"] = startLng
        alarm["arrivalLat"] = arrivalLat
        alarm["appointment_time"] = startTimeToLong.toString()//약속시간
        alarm["arrivalLng"] = arrivalLng
        alarm["isoDateTime"] = isoDateTime
        notificationId = resultTime.substring(3)
        alarm["notificationId"] = notificationId
        alarm["timeTaken"] = timeTaken
        alarm["trackingTime"] = plusTime
        //알람 데이터 업데이트 기기에 저장을 하고 서비스에서 받음
        with(getSharedPreferences(KEY_DATETIME, MODE_PRIVATE).edit()) {
            putString(DATETIME, resultTime)
            apply()
        }
        Firebase.database(DB_URL).reference.child(DB_ALARMS).child(user).child(notificationId)
            .updateChildren(alarm)
        //알람 생성
        val body = "${nickname}님 이제 ${todo}할 시간이에요~!"
        Toast.makeText(this, "알람을 생성하였습니다", Toast.LENGTH_SHORT).show()
        sendFcm(body)
    }

    //fcm을 보내는 기능
    private fun sendFcm(body: String) {
        if (currentUserFcmToken.isNotEmpty()) {
            val client = OkHttpClient()
            val root = JSONObject()
            val notification = JSONObject()
            notification.put("title", "MapMyDay")
            notification.put("body", body)
            root.put("to", currentUserFcmToken)
            root.put("priority", "high")
            root.put("notification", notification)

            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val key = getString(R.string.fcm_server_key)
            val request =
                Request.Builder().post(requestBody).url("https://fcm.googleapis.com/fcm/send")
                    .header("Authorization", "key=${key}").build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e("CreateActivityFcm", response.toString())

                }
            })
        }
    }

    private fun createTodo() {
        val title = binding.editTodoText.text.toString()
        val st_date = binding.startDateValueTextView.text.toString()
        val st_time = binding.startTimeValueTextView.text.toString()
        val end_date = binding.arriveDateValueTextView.text.toString()
        val end_time = binding.arriveTimeValueTextView.text.toString()
        val place = editTextPlace
        val memo = editTextMemo
        val startPlace = editStartPlace
        val arrivePlace = editArrivePlace

        val check = splitDate(st_date)
        val clickedYear = check[0].trim()
        val clickedMonth = check[1].trim()
        val clickedDay = check[2].trim()

        val TodoRef = FirebaseDatabase.getInstance().getReference("calendar")
            .child(user)
            .child(clickedYear + "년")
            .child(clickedMonth + "월")
            .child(clickedDay + "일")
            .push()

        val todo =
            Todo(
                title = title,
                st_date = st_date,
                st_time = st_time,
                end_date = end_date,
                end_time = end_time,
                place = place,
                memo = memo,
                startPlace = startPlace,
                arrivePlace = arrivePlace,
                notificationId = notificationId,
            ) // notificationId를 통해서 알람정보 획득

        todoKeys.add(TodoRef.key!!)

        TodoRef.setValue(todo).addOnSuccessListener {
            Toast.makeText(applicationContext, "일정 생성 완료", Toast.LENGTH_SHORT).show()
            Log.i("FirebaseData", "데이터 전송에 성공하였습니다.")
        }.addOnCanceledListener {
            Log.i("FirebaseData", "데이터 전송에 실패하였습니다")
        }
    }

    private fun updateTodo(todoKey: String, todo: Todo) {
        val todoKey = intent.getStringExtra("todoKey")
        val todo = intent.getParcelableExtra<Todo>("todo")

        // 기존 일정 시작 날짜와 일정 제목
        val oldStartDate = todo?.st_date.toString()
        val oldTitle = todo?.title.toString()

        val title = binding.editTodoText.text.toString()
        val st_date = binding.startDateValueTextView.text.toString()
        val st_time = binding.startTimeValueTextView.text.toString()
        val end_date = binding.arriveDateValueTextView.text.toString()
        val end_time = binding.arriveTimeValueTextView.text.toString()
        val place = editTextPlace
        val memo = editTextMemo
        val startPlace = editStartPlace
        val arrivePlace = editArrivePlace

        val todoUpdates: MutableMap<String, Any> = HashMap()
        todoUpdates["title"] = title
        todoUpdates["st_date"] = st_date
        todoUpdates["st_time"] = st_time
        todoUpdates["end_date"] = end_date
        todoUpdates["end_time"] = end_time
        todoUpdates["place"] = place
        todoUpdates["memo"] = memo
        todoUpdates["startPlace"] = startPlace
        todoUpdates["arrivePlace"] = arrivePlace

        // 변경된 일정 시작 날짜
        val newStartDate = todoUpdates["st_date"].toString()

        // 시작 날짜가 변경될 경우
        if (oldStartDate != newStartDate) {

            val deleteFirebase = splitDate(oldStartDate)
            val oldYear = deleteFirebase[0].trim()
            val oldMonth = deleteFirebase[1].trim()
            val oldDay = deleteFirebase[2].trim()
            // 이전 시작 날짜의 경로 참조
            val oldTodoReference = FirebaseDatabase.getInstance().getReference("calendar")
                .child(user)
                .child(oldYear + "년")
                .child(oldMonth + "월")
                .child(oldDay + "일")
            // 이전 날짜의 일정 삭제
            val query = oldTodoReference.orderByChild("title").equalTo(oldTitle)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (data in dataSnapshot.children) {
                        data.ref.removeValue()
                            .addOnSuccessListener {
                                Log.i("FirebaseData", "기존 데이터 삭제")
                            }
                            .addOnCanceledListener {
                                Log.i("FirebaseData", "기존 데이터 삭제 실패.")
                            }
                    }
                }
            })
            // 변경한 날짜의 일정 생성
            val createFirebase = splitDate(newStartDate)
            val newYear = createFirebase[0].trim()
            val newMonth = createFirebase[1].trim()
            val newDay = createFirebase[2].trim()
            // 변경한 시작 날짜의 경로 참조
            val newTodoReference = FirebaseDatabase.getInstance().getReference("calendar")
                .child(user)
                .child(newYear + "년")
                .child(newMonth + "월")
                .child(newDay + "일")
            // 변경한 날짜에 일정 업데이트
            if (todoKey != null) {
                newTodoReference
                    .child(todoKey)
                    .setValue(todoUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "일정 수정 완료", Toast.LENGTH_SHORT).show()
                        Log.i("FirebaseData", "데이터 업데이트에 성공하였습니다.")
                    }
                    .addOnCanceledListener {
                        Log.i("FirebaseData", "데이터 업데이트에 실패하였습니다.")
                    }
            }
        }
        // 시작 날짜 변경 없고 다른 내용 수정인 경우
        else {
            val updateFirebase = splitDate(oldStartDate)
            val originalYear = updateFirebase[0].trim()
            val originalMonth = updateFirebase[1].trim()
            val originalDay = updateFirebase[2].trim()

            // 기존 날짜 경로 참조
            val todoReference = FirebaseDatabase.getInstance().getReference("calendar")
                .child(user)
                .child(originalYear + "년")
                .child(originalMonth + "월")
                .child(originalDay + "일")
            // 기존 날짜에 일정 업데이트
            if (todoKey != null) {
                todoReference
                    .child(todoKey)
                    .setValue(todoUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "일정 수정 완료", Toast.LENGTH_SHORT).show()
                        Log.i("FirebaseData", "데이터 업데이트에 성공하였습니다.")
                    }
                    .addOnCanceledListener {
                        Log.i("FirebaseData", "데이터 업데이트에 실패하였습니다.")
                    }
            }
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("일정 생성을 취소하시겠습니까?")
            setPositiveButton("네") { dialog, id ->
                //알람이 존재한다면 알람을 삭제해야함
                Log.e("isEditMode", isEditMode.toString())
                if (isEditMode) {
                    //편집 모드
                    deleteAlarmWhenEdit()
                }
                val intent = Intent(this@CreateActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            setNegativeButton("아니오", null)
        }.show()
    }

    private fun setTime(separator: Int) {
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val formatHour = String.format("%02d", hourOfDay)
            val formatMinute = String.format("%02d", minute)
            timeString = "오전 ${formatHour}:${formatMinute}"
            val isAfternoon = hourOfDay >= 12
            val timeString = "${if (isAfternoon) "오후" else "오전"} ${formatHour}:${formatMinute}"
            if (separator == 0) {
                binding.startTimeValueTextView.text = timeString
                startTime = "$dateString ${formatHour}:${formatMinute}"
            } else {
                binding.arriveTimeValueTextView.text = timeString
                arrivalTime = "$dateString ${formatHour}:${formatMinute}"
            }
            // 출발시간이 도착시간보다 빨리 못하게 나중에 버튼누르면 안되게 해야함
            val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.KOREA)
            try {
                val startDate = dateTimeFormat.parse(startTime.replace("오전", "").replace("오후", ""))
                val arrivalDate =
                    dateTimeFormat.parse(arrivalTime.replace("오전", "").replace("오후", ""))
                Log.d("time", "$startDate $arrivalDate")
                if (startDate != null) {
                    if (startDate >= arrivalDate) {
                        Log.e("setTime", "$startDate $arrivalDate")
                        Toast.makeText(this, "시작시간은 도착시간보다 늦을 수 없습니다", Toast.LENGTH_SHORT).show()
                        binding.arriveDateValueTextView.text = "0000/00/00"
                        binding.arriveTimeValueTextView.text = "오전 00:00"
                        arrivalTime = ""
                    }
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        TimePickerDialog(
            this,
            timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setDate(separator: Int) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            dateString =
                String.format("%04d/%02d/%02d", year, month + 1, dayOfMonth) // yyyy/MM/dd 형식으로 변경
            if (separator == 0) {
                binding.startDateValueTextView.text = "$dateString"
                startTime = "$dateString $timeString"
            } else {
                binding.arriveDateValueTextView.text = "$dateString"
                arrivalTime = "$dateString $timeString"
            }
        }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun getData() {
        with(getSharedPreferences("addressInformation", Context.MODE_PRIVATE)) {
            startAddress = getString("startAddress", "").toString()
            arrivalAddress = getString("arrivalAddress", "").toString()
        }
    }

    // searchActivity에서 돌아올때 화면 깨짐 방지로 데이터 저장
    private fun saveDateData() {
        with(getSharedPreferences("date", Context.MODE_PRIVATE).edit()) {
            putString(START_DATE_FOR_SEARCHACTIVITY, binding.startDateValueTextView.text.toString())
            putString(START_TIME_FOR_SEARCHACTIVITY, binding.startTimeValueTextView.text.toString())
            putString(
                ARRIVAL_DATE_FOR_SEARCHACTIVITY,
                binding.arriveDateValueTextView.text.toString()
            )
            putString(
                ARRIVAL_TIME_FOR_SEARCHACTIVITY,
                binding.arriveTimeValueTextView.text.toString()
            )
            putString("title1", binding.editTodoText.text.toString())
            putString("startDate", binding.startDateValueTextView.text.toString())
            apply()
        }
    }

    private fun splitDate(date: String): Array<String> {
        Log.e("splitDate", date)
        val splitText = date.split("/")
        val resultDate: Array<String> = Array(3) { "" }
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //허용된거임 그러면 그냥 ㅇㅋ
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                //여기는 거절하고 한번더 알려주는거
                showPermissionRationalDialog()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("알림 권한이 없으면 알림을 받을 수 없습니다.")
            .setPositiveButton("권한 허용하기") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("취소") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.createActionToolbar.inflateMenu(R.menu.create_menu)
        return true
    }

    //EditTodoFragment 메모장 텍스트 길이 받기
    override fun onDataPass(data: Int?) {
        Log.d("DataPass", "memo's lenght is :$data")
        editTextLength = data!!
    }

    //EditTodoFragment 메모장 텍스트 받기
    override fun onMemoPass(memo: String?) {
        if (memo != null) {
            editTextMemo = memo
        }
    }

    //EditTodoFragment 장소 텍스트 받기
    override fun onPlacePass(place: String?) {
        if (place != null) {
            editTextPlace = place
        } else editTextPlace = ""
    }

    //EditMappingFragment 출발지 텍스트 받아오기
    override fun onStartPass(startPlace: String?) {
        Log.d("DataPass", "startPlace is :$startPlace")
        if (startPlace != null) {
            editStartPlace = startPlace
        } else editStartPlace = ""
    }

    //EditMappingFragment에서 도착지 텍스트 받아오기
    override fun onArrivePass(arrivePlace: String?) {
        Log.d("DataPass", "arrivePlace is :$arrivePlace")
        if (arrivePlace != null) {
            editStartPlace = arrivePlace
        } else editStartPlace = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        saveDateData()
    }

}