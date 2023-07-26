package com.example.capston.Create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.commit
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.EditFragment.EditTodoFragment
import com.example.capston.MainActivity
import com.example.capston.R
import com.example.capston.Todo
import com.example.capston.databinding.ActivityCreateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateActivity : AppCompatActivity(),
    EditTodoFragment.OnDataPassListener,
    EditMappingFragment.OnDataPassListener {
    private lateinit var binding: ActivityCreateBinding
    var todoKeys: java.util.ArrayList<String> = arrayListOf()   //일정 키 목록
    lateinit var user: String
    private var startAddress = ""
    private var arrivalAddress = ""
    private var dateString = ""
    private var timeString = ""
    private var startTime = ""
    private var arrivalTime = ""
    private var editTextLength = 0
    private var editTextPlace = ""
    private var editTextMemo = ""
    private var editStartPlace = ""
    private var editArrivePlace = ""
    private var isEditMode = false  // 추가: 편집 모드 여부를 나타내는 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        //list에서 일정 하나 선택했을 때 내용 수정
        val todo = intent.getParcelableExtra<Todo>("todo")
        val startDate = intent.getStringExtra("startDate") // "yyyy/M/d" 형식으로 받음
        if (todo != null) {
            // 기존의 Todo를 수정하는 경우, Todo객체를 사용하여 화면을 초기화
            Log.d("DataPass", "time is :${todo.st_time}")
            Log.d("DataPass", "create place is :${todo.place}")
            initializeEditMode(todo)
        } else {
            // 새로운 Todo를 생성하는 경우, 화면을 초기화
            initializeCreateMode(startDate!!)
        }
        getData()
        val fManager = supportFragmentManager
        val mappingFragment = EditMappingFragment.newInstance(startAddress, arrivalAddress)
        fManager.commit {
            add(binding.frameLayout.id, EditTodoFragment())
        }
        val fragmentToShow = intent.getStringExtra("fragmentToShow")
        if (fragmentToShow == "mappingFragment") {
            fManager.commit {
                replace(binding.frameLayout.id, mappingFragment) // 프래그먼트가 표시될 레이아웃 ID
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
        }
        //mapping fragment
        binding.goMappingButton.setOnClickListener {
            val currentFragment = fManager.findFragmentById(binding.frameLayout.id)
            if (currentFragment !is EditMappingFragment) {
                fManager.commit {
                    replace(binding.frameLayout.id, mappingFragment)
                }
            }
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
        binding.startDateValueTextView.addTextChangedListener {
            saveDateData()
        }

        binding.startTimeValueTextView.addTextChangedListener {
            saveDateData()
        }
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
        // 화면을 초기화하는 작업 수행
        binding.editTodoText.setText("")
        binding.startDateValueTextView.text = startDate
        binding.startTimeValueTextView.text = "오전 00:00"
        binding.arriveDateValueTextView.text = "0000/00/00"
        binding.arriveTimeValueTextView.text = "오전 00:00"
        Log.i("date","${startDate}")
    }

    private fun convertToNumericInt(inputString: String): Long {
        // 정규식으로 "/", ":", 공백을 제거하고 숫자만 남김
        val numericString = inputString.replace("[/:\\s]".toRegex(), "")

        return numericString.toLong()
    }

    //일정생성할때 날짜체크
    private fun checkDate(): Boolean {
        val changeStartTime = binding.startTimeValueTextView.text.toString().replace("오후", "").replace("오전", "")

        val changeArrivalTime =  binding.arriveTimeValueTextView.text.toString().replace("오후", "").replace("오전", "")

        val startTimeText =
            binding.startDateValueTextView.text.toString() + changeStartTime
        val arrivalTimeText =
            binding.arriveDateValueTextView.text.toString() + changeArrivalTime

        val startTimeToLong = convertToNumericInt(startTimeText)
        val arrivalTimeToLong = convertToNumericInt(arrivalTimeText)
        Log.e("날짜 확인", startTimeToLong.toString())
        Log.e("날짜 확인", arrivalTimeToLong.toString())
        return startTimeToLong < arrivalTimeToLong
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val todoKey = intent.getStringExtra("todoKey")
        //만약 오전이면 없애고 오후면은 12를 더해줘야함

        return when (item.itemId) {
            R.id.okMenu -> {
                if (!checkDate()) {
                    Toast.makeText(this, "시작시간은 도착시간보다 늦을 수 없습니다", Toast.LENGTH_SHORT).show()
                }else{
                    if (editTextLength > 100) {
                        Toast.makeText(this, "메모 글자수가 100을 넘었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        if (isEditMode) {
                            // 기존의 Todo를 수정하는 경우
                            updateTodo(todoKey!!)
                        } else {
                            // 새로운 Todo를 생성하는 경우
                            createTodo()
                        }
                        finish()
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

    private fun createTodo() {
        val title = binding.editTodoText.text.toString()  //제목 입력창에 작성한 내용 문자열로 받아 title 변수에 저장
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
        val todo =
            Todo(title, st_date, st_time, end_date, end_time, place, memo, startPlace, arrivePlace)
        val TodoRef = FirebaseDatabase.getInstance().getReference("calendar")
            .child(user)
            .child("$clickedYear" + "년")
            .child("$clickedMonth" + "월")
            .child("$clickedDay" + "일")
            .push()
        todoKeys.add(TodoRef.key!!)
        TodoRef.setValue(todo).addOnSuccessListener {
            Toast.makeText(applicationContext, "일정 생성 완료", Toast.LENGTH_SHORT).show()
            Log.i("FirebaseData", "${todoKeys}")
            Log.i("FirebaseData", "데이터 전송에 성공하였습니다.")
            Log.i("FirebaseData", "title:${todo.title}, time:${todo.st_time}")
        }.addOnCanceledListener {
            Log.i("FirebaseData", "데이터 전송에 실패하였습니다")
        }
    }

    private fun updateTodo(todoKey: String) {
        val todoKey = intent.getStringExtra("todoKey")

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

        val todoReference = FirebaseDatabase.getInstance().getReference("calendar")
            .child(user)
            .child("$clickedYear" + "년")
            .child("$clickedMonth" + "월")
            .child("$clickedDay" + "일")

        if (todoKey != null) {
            todoReference
                .child(todoKey)
                .setValue(todoUpdates)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "일정 수정 완료", Toast.LENGTH_SHORT).show()
                    Log.i("FirebaseData", "데이터 업데이트에 성공하였습니다.")
                    Log.i("FirebaseData", "title:${title}, time:${st_time}")
                    Log.d("TodoKeys", todoKey) // 또는 Toast 메시지를 사용하거나 디버거를 이용하여 확인할 수 있습니다.
                }
                .addOnCanceledListener {
                    Log.i("FirebaseData", "데이터 업데이트에 실패하였습니다.")
                }
        }

    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("일정저장을 취소하시겠습니까?")
            setPositiveButton("네") { dialog, id ->
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
            val formatHour = String.format("%02d",hourOfDay)
            val formatMinute = String.format("%02d",minute)
            timeString = "오전 ${formatHour}:${formatMinute}"
            val isAfternoon = hourOfDay >= 12
            val timeString = "${if (isAfternoon) "오후" else "오전"} ${formatHour}:${formatMinute}"
            if (separator == 0) {
                binding.startTimeValueTextView.text = timeString
                startTime = "$dateString ${formatHour}:${formatMinute}"
                Log.e("날짜확인", startTime)
            } else {
                binding.arriveTimeValueTextView.text = timeString
                arrivalTime = "$dateString ${formatHour}:${formatMinute}"
            }
            // 출발시간이 도착시간보다 빨리 못하게 나중에 버튼누르면 안되게 해야함
            val dateTimeFormat = SimpleDateFormat("yyyy/M/d HH:mm", Locale.KOREA)
            try {
                val startDate = dateTimeFormat.parse(startTime)
                val arrivalDate = dateTimeFormat.parse(arrivalTime)
                Log.d("time", "$startDate $arrivalDate")
                if (startDate != null) {
                    if (startDate >= arrivalDate) {
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
            dateString = String.format("%04d/%d/%d", year, month + 1, dayOfMonth) // yyyy/M/d 형식으로 변경
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
    private fun saveDateData() {
        with(getSharedPreferences("date", Context.MODE_PRIVATE).edit()) {
            putString("startDate1", binding.startDateValueTextView.text.toString())
            putString("startTime1", binding.startTimeValueTextView.text.toString())
            putString("arrivalDate1", binding.arriveDateValueTextView.text.toString())
            putString("arrivalTime1", binding.arriveTimeValueTextView.text.toString())
            apply()
        }
    }

    private fun splitDate(date: String): Array<String> {
        val splitText = date.split("/")
        val resultDate: Array<String> = Array(3) { "" }
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.createActionToolbar.inflateMenu(R.menu.create_menu)
        return true
    }

    //EditTodoFragment에서 메모장 텍스트 길이 받아오기
    override fun onDataPass(data: Int?) {
        Log.d("DataPass", "memo's lenght is :$data")
        editTextLength = data!!
    }

    //EditTodoFragment에서 메모장 텍스트 받아오기
    override fun onMemoPass(memo: String) {
        Log.d("DataPass", "memo is :$memo")
        editTextMemo = memo
    }

    //EditTodoFragment에서 장소 텍스트 받아오기
    override fun onPlacePass(place: String) {
        Log.d("DataPass", "place is :$place")
        editTextPlace = place
    }

    //EditMappingFragment에서 출발지 텍스트 받아오기
    override fun onStartPass(startPlace: String) {
        Log.d("DataPass", "startPlace is :$startPlace")
        editStartPlace = startPlace

    }

    //EditMappingFragment에서 도착지 텍스트 받아오기
    override fun onArrivePass(arrivePlace: String) {
        Log.d("DataPass", "arrivePlace is :$arrivePlace")
        editArrivePlace = arrivePlace
    }

}