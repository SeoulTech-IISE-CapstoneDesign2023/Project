package com.example.capston

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.capston.Create.CreateActivity
import com.example.capston.databinding.FragmentTimeTableBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TimeTableFragment : Fragment() {
    private lateinit var binding: FragmentTimeTableBinding
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var context: Context
    lateinit var user: String
    val todoList = arrayListOf<Todo>()
    var todoKeys: java.util.ArrayList<String> = arrayListOf()   //할일 키 목록
    val timeBlocks = HashMap<String, View>()
    val titleColorMap = HashMap<String, Int>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeTableBinding.inflate(inflater, container, false)
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // 오늘 날짜, 년, 월 받아오기
        var today = Calendar.getInstance()
        var todayYear = today[Calendar.YEAR]
        var todayMonth = today[Calendar.MONTH]+1

        // 오늘 날짜 기준 해당 주차의 시작 날짜와 끝 날짜를 계산
        val startOfWeek = today.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)  // 월요일 시작
        val endOfWeek = today.clone() as Calendar
        endOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)    // 일요일 마지막
        endOfWeek.add(Calendar.DAY_OF_MONTH, 7)

        val startOfWeekDay = startOfWeek.get(Calendar.DAY_OF_MONTH)
        val endOfWeekDay = endOfWeek.get(Calendar.DAY_OF_MONTH)

        // Firebase 데이터 가져오기
        FirebaseDatabase.getInstance().getReference("calendar")
            .child(user)
            .child("$todayYear"+"년")
            .child("$todayMonth"+"월")
            .orderByKey() // 날짜를 기준으로 정렬합니다.
            .startAt(startOfWeekDay.toString()+"일")
            .endAt(endOfWeekDay.toString()+"일")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("TimeTableFragment", "DataSnapshot: $dataSnapshot")
                    // 데이터가 존재하는 경우
                    for (data in dataSnapshot.children) {
                        Log.d("TimeTableFragment", "${data}")
                        for (childData in data.children) {
                            Log.i("TimeTableFragment", "${childData}")
                            todoKeys.add(childData.key!!)                    //키를 todoKeys 목록에 추가
                            todoList.add(childData.getValue<Todo>()!!)
                            val todo = childData.getValue(Todo::class.java)
                            // 가져온 데이터를 활용하여 처리
                            if (todo != null) {
                                val day = todo.st_date  // 일정이 있는 날짜
                                val title = todo.title  // 일정의 제목
                                Log.d("TimeTableFragment", "Todo: $todo")
                                val dayOfWeek = getDayOfWeek(day) // 일정 요일 구하기
                                // 요일과 시간에 맞는 Block 찾아 데이터 설정
                                val timeRanges = 0..23 // 0부터 23까지의 범위
                                for (hour in timeRanges) {
                                    val key = "${dayOfWeek}${hour.toString().padStart(2, '0')}00"
                                    val viewId = context.resources.getIdentifier(key, "id", context.packageName)
                                    val view = binding.root.findViewById<TextView>(viewId)
                                    timeBlocks[key] = view
                                    // 일정의 title로 구분해 같은 일정은 같은 색, 다른 일정은 다른 색
                                    val color = if (titleColorMap.containsKey(title)) {
                                        titleColorMap[title]!!
                                    } else {
                                        val randomColor = getRandomColor()
                                        titleColorMap[title] = randomColor
                                        randomColor
                                    }
                                    // 일정이 있다면 배경색 : 랜덤 / 글씨색 : 하얀색
                                    val hasSchedule = checkScheduleForTime(todoList, dayOfWeek, hour)
                                    if (hasSchedule) {
                                        view.setBackgroundColor(color)
                                        view.text = todo.title
                                        view.setTextColor(Color.WHITE)

                                        // 일정을 클릭하면 해당 일정으로 이동
                                        view.setOnClickListener {
                                            val intent = Intent(context, CreateActivity::class.java)
                                            intent.putExtra("todo", todo)
                                            startActivity(intent)
                                        }
                                    }
                                }
                            } else {
                                Log.d("TimeTableFragment", "시간표 데이터가 없어요.")
                            }
                        }
                    }
                }
            })
        return binding.root
    }

    // 날짜로 요일 구하는 함수
    private fun getDayOfWeek(date: String): String {
        val splitText = date.split("/")
        val year = splitText[0].toInt()
        val month = splitText[1].toInt()-1
        val day = splitText[2].toInt()

        val cal: Calendar = Calendar.getInstance()
        cal.set(year, month, day)
        val nWeek: Int = cal.get(Calendar.DAY_OF_WEEK)

        return when (nWeek) {
            Calendar.SUNDAY -> "sunday"
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            else -> ""
        }
    }
    // 받아온 데이터 todo의 존재여부 확인 함수
    private fun checkScheduleForTime(todoList: List<Todo>, day: String, hour: Int): Boolean {
        for (todo in todoList) {
            val startTime = todo.st_time
            val endTime = todo.end_time
            val startHour = startTime.substring(3, 5).toInt()
            val endHour = endTime?.substring(3, 5)?.toInt()

            if (getDayOfWeek(todo.st_date) == day && hour in startHour..endHour!!) {
                return true // 일정이 있는 경우 true 반환
            }
        }
        return false // 일정이 없는 경우 false 반환
    }
    // 색 랜덤 함수
    private fun getRandomColor(): Int {
        val random = Random()
        val r = random.nextInt(256)
        val g = random.nextInt(256)
        val b = random.nextInt(256)
        return Color.rgb(r, g, b)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TimeTableFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}