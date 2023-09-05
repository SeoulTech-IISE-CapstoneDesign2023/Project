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
import com.example.capston.Friend.FriendListActivity
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
    var todoKeys: java.util.ArrayList<String> = arrayListOf()
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

        binding.friendButton.setOnClickListener {
            requireActivity().startActivity(Intent(context, FriendListActivity::class.java))
        }

        binding.settingButton.setOnClickListener {
            requireActivity().startActivity(Intent(context, SettingActivity::class.java))
        }

        // 오늘 날짜, 년, 월 받아오기
        val today = Calendar.getInstance()
        val todayYear = String.format("%04d", today[Calendar.YEAR])
        val todayMonth = String.format("%02d", today[Calendar.MONTH]+1)
        Log.d("TimeTableFragment", "Today: $todayYear + $todayMonth")

        // 오늘 날짜 기준 해당 주차의 시작 날짜와 끝 날짜를 계산
        val startOfWeek = today.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)  // 월요일 시작
        val endOfWeek = today.clone() as Calendar
        endOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)    // 일요일 마지막
        endOfWeek.add(Calendar.DAY_OF_MONTH, 7)

        // 요일이 바뀔 것을 계산
        val startOfWeekYear = String.format("%04d", startOfWeek.get(Calendar.YEAR))
        val startOfWeekMonth = String.format("%02d", startOfWeek.get(Calendar.MONTH) + 1)
        val startOfWeekDay = String.format("%02d", startOfWeek.get(Calendar.DAY_OF_MONTH))

        val endOfWeekYear = String.format("%04d", endOfWeek.get(Calendar.YEAR))
        val endOfWeekMonth = String.format("%02d", endOfWeek.get(Calendar.MONTH) + 1)
        val endOfWeekDay = String.format("%02d", endOfWeek.get(Calendar.DAY_OF_MONTH))

        // 월이 바뀌지 않는 경우
        if (startOfWeekMonth == endOfWeekMonth) {
            FirebaseDatabase.getInstance().getReference("calendar")
                .child(user)
                .child("$startOfWeekYear" + "년")
                .child("$startOfWeekMonth" + "월")
                .orderByKey() // 날짜를 기준으로 정렬합니다.
                .startAt("$startOfWeekDay" + "일")
                .endAt("$endOfWeekDay" + "일")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {}
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // 데이터 처리
                        // 데이터가 존재하는 경우
                        for (data in dataSnapshot.children) {
                            for (childData in data.children) {
                                todoList.clear()
                                todoKeys.clear()
                                todoKeys.add(childData.key!!)
                                todoList.add(childData.getValue<Todo>()!!)
                                val todo = childData.getValue(Todo::class.java)
                                // 가져온 데이터를 활용하여 처리
                                if (todo != null) {
                                    val day = todo.st_date  // 일정이 있는 날짜
                                    val todoKey = childData.key
                                    val dayOfWeek = getDayOfWeek(day) // 일정 요일 구하기
                                    // 일정의 key값으로 구분해 같은 일정은 같은 색, 다른 일정은 다른 색
                                    val color = if (titleColorMap.containsKey(todoKey)) {
                                        titleColorMap[todoKey]!!
                                    } else {
                                        val randomColor = getRandomColor()
                                        titleColorMap[todoKey!!] = randomColor
                                        randomColor
                                    }
                                    // 요일과 시간에 맞는 Block 찾아 데이터 설정
                                    val timeRanges = 0..23 // 0부터 23까지의 범위
                                    for (hour in timeRanges) {
                                        val key = "${dayOfWeek}${hour.toString().padStart(2, '0')}00"
                                        val viewId = context.resources.getIdentifier(key, "id", context.packageName)
                                        val view = binding.root.findViewById<TextView>(viewId)
                                        if (view != null) {
                                            timeBlocks[key] = view
                                            // 일정이 있다면 배경색 : 랜덤 / 글씨색 : 하얀색
                                            val hasSchedule =
                                                checkScheduleForTime(todoList, dayOfWeek, hour)
                                            if (hasSchedule) {
                                                view.setBackgroundColor(color)
                                                view.text = todo.title
                                                view.setTextColor(Color.WHITE)

                                                // 일정을 클릭하면 해당 일정으로 이동
                                                view.setOnClickListener {
                                                    val intent =
                                                        Intent(context, CreateActivity::class.java)
                                                    intent.putExtra("todo", todo)
                                                    startActivity(intent)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Log.d("TimeTableFragment", "시간표 데이터가 없어요.")
                                }
                            }
                        }
                    }
                }
                )
        } else {
            // 월이 바뀌는 경우, 두 개의 데이터를 가져와서 합칩니다.
            val ref1 = FirebaseDatabase.getInstance().getReference("calendar")
                .child(user)
                .child("$startOfWeekYear" + "년")
                .child("$startOfWeekMonth" + "월")
                .orderByKey() // 날짜를 기준으로 정렬합니다.
                .startAt("$startOfWeekDay" + "일")
            val ref2 = FirebaseDatabase.getInstance().getReference("calendar")
                .child(user)
                .child("$endOfWeekYear" + "년")
                .child("$endOfWeekMonth" + "월")
                .orderByKey() // 날짜를 기준으로 정렬합니다.
                .endAt("$endOfWeekDay" + "일")

            // 두 개의 데이터를 가져와서 합칩니다.
            // 첫 번째 데이터 처리
            ref1.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (data in dataSnapshot.children) {
                        for (childData in data.children) {
                            todoKeys.add(childData.key!!)                    //키를 todoKeys 목록에 추가
                            todoList.add(childData.getValue<Todo>()!!)
                            val todo = childData.getValue(Todo::class.java)
                            if (todo != null) {
                                val day = todo.st_date  // 일정이 있는 날짜
                                val todoKey = childData.key
                                val dayOfWeek = getDayOfWeek(day) // 일정 요일 구하기
                                // 일정의 key값으로 구분해 같은 일정은 같은 색, 다른 일정은 다른 색
                                val color = if (titleColorMap.containsKey(todoKey)) {
                                    titleColorMap[todoKey]!!
                                } else {
                                    val randomColor = getRandomColor()
                                    titleColorMap[todoKey!!] = randomColor
                                    randomColor
                                }
                                // 요일과 시간에 맞는 Block 찾아 데이터 설정
                                val timeRanges = 0..23 // 0부터 23까지의 범위
                                for (hour in timeRanges) {
                                    val key = "${dayOfWeek}${hour.toString().padStart(2, '0')}00"
                                    val viewId = context.resources.getIdentifier(key, "id", context.packageName)
                                    val view = binding.root.findViewById<TextView>(viewId)
                                    if (view != null) {
                                        timeBlocks[key] = view
                                        // 일정이 있다면 배경색 : 랜덤 / 글씨색 : 하얀색
                                        val hasSchedule =
                                            checkScheduleForTime(todoList, dayOfWeek, hour)
                                        if (hasSchedule) {
                                            view.setBackgroundColor(color)
                                            view.text = todo.title
                                            view.setTextColor(Color.WHITE)

                                            // 일정을 클릭하면 해당 일정으로 이동
                                            view.setOnClickListener {
                                                val intent =
                                                    Intent(context, CreateActivity::class.java)
                                                intent.putExtra("todo", todo)
                                                startActivity(intent)
                                            }
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
            // 두 번째 데이터 처리
            ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (data in dataSnapshot.children) {
                        for (childData in data.children) {
                            todoKeys.add(childData.key!!)                    //키를 todoKeys 목록에 추가
                            todoList.add(childData.getValue<Todo>()!!)
                            val todo = childData.getValue(Todo::class.java)
                            if (todo != null) {
                                val day = todo.st_date  // 일정이 있는 날짜
                                val todoKey = childData.key
                                val dayOfWeek = getDayOfWeek(day) // 일정 요일 구하기
                                // 일정의 key값으로 구분해 같은 일정은 같은 색, 다른 일정은 다른 색
                                val color = if (titleColorMap.containsKey(todoKey)) {
                                    titleColorMap[todoKey]!!
                                } else {
                                    val randomColor = getRandomColor()
                                    titleColorMap[todoKey!!] = randomColor
                                    randomColor
                                }
                                // 요일과 시간에 맞는 Block 찾아 데이터 설정
                                val timeRanges = 0..23 // 0부터 23까지의 범위
                                for (hour in timeRanges) {
                                    val key = "${dayOfWeek}${hour.toString().padStart(2, '0')}00"
                                    val viewId = context.resources.getIdentifier(
                                        key,
                                        "id",
                                        context.packageName
                                    )
                                    val view = binding.root.findViewById<TextView>(viewId)
                                    if (view != null) {
                                        timeBlocks[key] = view
                                        // 일정이 있다면 배경색 : 랜덤 / 글씨색 : 하얀색
                                        val hasSchedule =
                                            checkScheduleForTime(todoList, dayOfWeek, hour)
                                        if (hasSchedule) {
                                            view.setBackgroundColor(color)
                                            view.text = todo.title
                                            view.setTextColor(Color.WHITE)

                                            // 일정을 클릭하면 해당 일정으로 이동
                                            view.setOnClickListener {
                                                val intent =
                                                    Intent(context, CreateActivity::class.java)
                                                intent.putExtra("todo", todo)
                                                startActivity(intent)
                                            }
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
        }

        return binding.root
    }

    // 날짜로 요일 구하는 함수
    private fun getDayOfWeek(date: String): String {
        if (date.isEmpty()) {
            return "" // 빈 문자열인 경우 빈 문자열 반환 또는 다른 기본 값을 반환하세요.
        }
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
        if (todoList != null) {
            for (todo in todoList) {
                val startTime = todo.st_time
                val endTime = todo.end_time

                // startTime 또는 endTime이 null인지 확인
                if (startTime == null || endTime == null) {
                    continue // startTime 또는 endTime이 null이면 다음 todo로 이동
                }

                val startHour = startTime.substring(3, 5).toInt()
                val endHour = endTime?.substring(3, 5)?.toInt()

                if (getDayOfWeek(todo.st_date) == day && hour in startHour..endHour!!) {
                    return true // 일정이 있는 경우 true 반환
                }
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

    override fun onDestroy() {
        super.onDestroy()
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