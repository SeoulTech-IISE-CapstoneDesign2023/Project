package com.example.capston

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.capston.databinding.FragmentCalendarBinding
import com.example.capston.databinding.FragmentMainBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CalendarFragment : Fragment() {
    lateinit var binding: FragmentCalendarBinding

    // TODO: Rename and change types of parameters
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
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        //일정 생성 버튼
        binding.addtodoButton.setOnClickListener {
            val intent = Intent(requireActivity(), CreateActivity::class.java)
            requireActivity().startActivity(intent)
        }

        //today's list
        binding.calendarView.setOnDateChangeListener{ _, year, month, dayOfMonth ->
            val dateStr = "${year}/${month + 1}/$dayOfMonth"
            binding.txtTodaylist.text = dateStr+" List"
        }

        //일정 하나 상세 보기 -> 구현해야함

        return binding.root
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