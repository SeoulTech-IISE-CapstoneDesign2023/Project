package com.example.capston

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
import androidx.fragment.app.commit
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.EditFragment.EditTodoFragment
import com.example.capston.databinding.ActivityCreateBinding
import com.example.capston.databinding.FragmentEditTodoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateActivity : AppCompatActivity(),EditTodoFragment.OnDataPassListener {
    private lateinit var binding: ActivityCreateBinding
    lateinit var user: String
    private var startAddress = ""
    private var arrivalAddress = ""
    private var dateString = ""
    private var timeString = ""
    private var startTime = ""
    private var arrivalTime = ""
    private var editTextLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

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
        //CalendarFragment에서 날짜 데이터 받아서 약속 시간 날짜만 가져오기
        binding.startDateValueTextView.text = intent.getStringExtra("startDate") ?: "0000/00/00"



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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.okMenu -> {
                if (editTextLength > 100){
                    Toast.makeText(this,"메모 글자수가 100을 넘었습니다.",Toast.LENGTH_SHORT).show()
                }else{
                    putTodo()
                    finish()
                }
                true
            }
            R.id.cancelMenu ->{
                showAlertDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun putTodo() {
        val title = binding.editTodoText.text.toString()  //제목 입력창에 작성한 내용 문자열로 받아 title 변수에 저장
        val st_date = binding.startDateValueTextView.text.toString()
        val st_time = binding.startTimeValueTextView.text.toString()
        val end_date = binding.arriveDateValueTextView.text.toString()
        val end_time = binding.arriveTimeValueTextView.text.toString()
        val check = splitDate(st_date)
        val clickedYear = check[0].trim()
        val clickedMonth = check[1].trim()
        val clickedDay = check[2].trim()
//        val place = todoFragmentBinding.place.getText().toString()
//        val memo = todoFragmentBinding.memo.getText().toString()
        val todo = Todo(title, st_date, st_time, end_date, end_time, null, null)   //todo data class에 필요한 내용 넣고 todo 변수에 저장
        FirebaseDatabase.getInstance().getReference("calendar").child(user)
            .child("$clickedYear"+"년").child("$clickedMonth"+"월").child("$clickedDay"+"일")
            .push().setValue(todo).addOnSuccessListener {
                Toast.makeText(applicationContext,"일정 생성 완료",Toast.LENGTH_SHORT).show();
                Log.i("FirebaseData", "데이터 전송에 성공하였습니다.")
                Log.i("FirebaseData", "title:${todo.title}, time:${todo.st_time}")
            }.addOnCanceledListener {
                Log.i("FirebaseData", "데이터 전송에 실패하였습니다")
            }
    }
    private fun showAlertDialog(){
        AlertDialog.Builder(this).apply {
            setMessage("일정저장을 취소하시겠습니까?")
            setPositiveButton("네"){dialog,id->
                val intent = Intent(this@CreateActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            setNegativeButton("아니오",null)
        }.show()
    }

    private fun setTime(separator:Int){
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            timeString = "오전 ${hourOfDay}:${minute}"
            if (hourOfDay > 12) {
                timeString = "오후 ${hourOfDay - 12}:${minute}"
            } else if (hourOfDay == 12) {
                timeString = "오후 ${hourOfDay}:${minute}"
            }
            if (separator == 0) {
                binding.startTimeValueTextView.text = "$timeString"
                startTime = "$dateString ${hourOfDay}:${minute}"
            } else {
                binding.arriveTimeValueTextView.text = "$timeString"
                arrivalTime = "$dateString ${hourOfDay}:${minute}"
            }
            // 출발시간이 도착시간보다 빨리 못하게 나중에 버튼누르면 안되게 해야함
            val dateTimeFormat = SimpleDateFormat("yyyy/ M / dd HH:mm", Locale.KOREA)
            try{
                val startDate = dateTimeFormat.parse(startTime)
                val arrivalDate = dateTimeFormat.parse(arrivalTime)
                Log.d("time","$startDate $arrivalDate")
                if (startDate != null) {
                    if (startDate >= arrivalDate){
                        Toast.makeText(this,"시작시간은 도착시간보다 늦을 수 없습니다",Toast.LENGTH_SHORT).show()
                        binding.arriveDateValueTextView.text = "0000/00/00"
                        binding.arriveTimeValueTextView.text = "오전 00:00"
                        arrivalTime = ""
                    }
                }
            }catch (e: ParseException){
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
            dateString = "${year}/ ${month + 1} / ${dayOfMonth}"
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
    private fun splitDate(date: String): Array<String> {
        val splitText = date.split("/")
        val resultDate: Array<String> = Array(3){""}
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.createActionToolbar.inflateMenu(R.menu.create_menu)
        return true
    }

    //editTodoFragment에서 메모장 텍스트 길이 받아오기
    override fun onDataPass(data: Int?) {
        Log.d("DataPass","$data")
        editTextLength = data!!
    }
}