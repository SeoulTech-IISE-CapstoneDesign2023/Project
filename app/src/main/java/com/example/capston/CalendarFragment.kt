package com.example.capston

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capston.databinding.FragmentCalendarBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CalendarFragment : Fragment() {
    private lateinit var binding: FragmentCalendarBinding
    private var param1: String? = null
    private var param2: String? = null
    private var dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/ M/ dd"))


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
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        //일정 생성 버튼
        binding.addtodoButton.setOnClickListener {
            saveDate()
        }

        //today's list
        binding.calendarView.setOnDateChangeListener{ _, year, month, dayOfMonth ->
            dateStr = "${year}/ ${month + 1}/ $dayOfMonth"
            if (dateStr == LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/ M/ dd"))){
                binding.txtTodaylist.text = "Today's List"
            }else{
                binding.txtTodaylist.text = dateStr+" List"
            }
        }
        //일정 하나 상세 보기 -> 구현해야함
        return binding.root
    }

    //캘린더에서 선택한 날짜 데이터 저장
    private fun saveDate(){
        val intent = Intent(requireContext(), CreateActivity::class.java)
        intent.putExtra("startDate",dateStr)
        startActivity(intent)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}