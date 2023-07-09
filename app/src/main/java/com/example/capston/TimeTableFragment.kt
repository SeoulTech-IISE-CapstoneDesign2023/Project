package com.example.capston

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capston.databinding.FragmentTimeTableBinding
import com.prolificinteractive.materialcalendarview.CalendarUtils.getDayOfWeek
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TimeTableFragment : Fragment() {
    private lateinit var binding: FragmentTimeTableBinding
    private var param1: String? = null
    private var param2: String? = null

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
        val bundle = arguments
        val todoList: ArrayList<Todo>? = bundle?.getParcelableArrayList<Todo>("todoList")
        if (arguments != null) {
            // todoList 데이터를 기반으로 시간표를 설정
            if (todoList != null) {
                for (todo in todoList) {
                    val day = todo.st_date // 일정이 있는 날짜
                    val dayOfWeek = getDayOfWeek(day) // 일정 요일 구하기
                    Log.d("TimeTableFragment", "Todo: $todo")
                    val startTime = todo.st_time // 일정 시작 시간
                    val endTime = todo.end_time // 일정 종료 시간

                    // 요일과 시간에 맞는 TextView를 찾아 데이터 설정
                    when (dayOfWeek) {
                        "월" -> {
                            val monday0800Block = binding.monday0800
                            val hasSchedule = checkScheduleForTime(todoList, "월")
                            Log.d("TimeTableFragment","요일: ${dayOfWeek}")
                            if (hasSchedule) {
                                monday0800Block.setBackgroundColor(Color.RED)
                            } else {
                                monday0800Block.setBackgroundColor(Color.TRANSPARENT)
                            }
                        }
                    }
                }
            } else {
                // 데이터가 없을 때 처리 로직
                Log.d("TimeTableFragment", "시간표 데이터가 없어요.")
            }
        } else {
            // 데이터가 없을 때 처리 로직
            Log.d("TimeTableFragment", "시간표 데이터가 없습니다.")
        }
        return binding.root
    }
    private fun getDayOfWeek(date: String): String {
        val cal: Calendar = Calendar.getInstance()
        val year = date.substring(0, 4).toInt()
        val month = date.substring(5, 7).toInt() - 1 // Calendar.MONTH는 0부터 시작하므로 1을 빼줌
        val day = date.substring(8).toInt()

        cal.set(year, month, day)
        val nWeek: Int = cal.get(Calendar.DAY_OF_WEEK)

        return when (nWeek) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }
    }
    private fun checkScheduleForTime(todoList: List<Todo>, day: String): Boolean {
        for (todo in todoList) {
            if (todo.st_date == day) {
                return true // 일정이 있는 경우 true 반환
            }
        }
        return false // 일정이 없는 경우 false 반환
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
    //    companion object {
//        private const val ARG_TODO_LIST = "todoList"
//
//        @JvmStatic
//        fun newInstance(todoList: ArrayList<Todo>): TimeTableFragment {
//            val fragment = TimeTableFragment()
//            val args = Bundle().apply {
//                putParcelableArrayList(ARG_TODO_LIST, todoList)
//            }
//            fragment.arguments = args
//            return fragment
//        }
//    }
}