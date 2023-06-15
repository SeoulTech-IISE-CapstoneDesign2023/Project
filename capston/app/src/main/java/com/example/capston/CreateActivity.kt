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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding
    private var startAddress = ""
    private var arrivalAddress = ""
    private var dateString = ""
    private var timeString = ""
    private var startTime = ""
    private var arrivalTime = ""
//    val intent = Intent(this, CreateActivity::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)
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

        binding.goTodoButton.setOnClickListener {
            val currentFragment = fManager.findFragmentById(binding.frameLayout.id)
            if (currentFragment !is EditTodoFragment) {
                fManager.commit {
                    replace(binding.frameLayout.id, EditTodoFragment())
                }
            }
        }

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.okMenu -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.cancelMenu ->{
                showAlertDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
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

    private fun setDate(separator: Int) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            dateString = "${year}/ ${month + 1} / ${dayOfMonth}"
            if (separator == 0) {
                binding.startDateValueTextView.text = "$dateString $timeString"
                startTime = "$dateString $timeString"
            } else {
                binding.arriveDateValueTextView.text = "$dateString $timeString"
                arrivalTime = "$dateString $timeString"
            }
        }
        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            timeString = "오전 ${hourOfDay}:${minute}"
            if (hourOfDay > 12) {
                timeString = "오후 ${hourOfDay - 12}:${minute}"
            } else if (hourOfDay == 12) {
                timeString = "오후 ${hourOfDay}:${minute}"
            }
            if (separator == 0) {
                binding.startDateValueTextView.text = "$dateString $timeString"
                startTime = "$dateString ${hourOfDay}:${minute}"
            } else {
                binding.arriveDateValueTextView.text = "$dateString $timeString"
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
                        binding.arriveDateValueTextView.text = "0000/00/00 오전 00:00"
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.createActionToolbar.inflateMenu(R.menu.create_menu)
        return true
    }
}