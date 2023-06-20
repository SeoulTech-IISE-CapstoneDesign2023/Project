package com.example.capston

import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.databinding.FragmentMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainFragment : Fragment(), OnItemLongClickListener {
    lateinit var binding: FragmentMainBinding
    private var param1: String? = null
    private var param2: String? = null
    lateinit var user: String
    val todoList = arrayListOf<Todo>()
    val adapter = TodoListAdapter(todoList,this)

    lateinit var yearToday: String
    lateinit var monthToday: String
    lateinit var dayToday: String

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
        binding = FragmentMainBinding.inflate(inflater, container, false)
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        //RecyclerView 설정
        binding.todoRecyclerView.adapter = adapter
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 오늘 날짜 가져오기
        var today = Calendar.getInstance()
        var todayYear = today[Calendar.YEAR]
        var todayMonth = today[Calendar.MONTH]
        var todayDay = today[Calendar.DAY_OF_MONTH]
        val todayStr = "${todayYear}/${todayMonth + 1}/$todayDay"

        // 오늘 todolist 불러오기
        todayDate(todayStr)

        //알림 배너 버튼 -> 구현해야함
//        binding.noticeButton.setOnClickListener{
//
//        }

        //일정 생성 버튼
        binding.addtodoButton.setOnClickListener {
            val intent = Intent(requireActivity(), CreateActivity::class.java)
            requireActivity().startActivity(intent)
        }

        //일정 하나 상세 보기 -> 구현해야함
        return binding.root
    }
    // today's todolist 불러오기
    private fun todayDate(date: String) {
        val todayInfo = splitDate(date)
        yearToday = todayInfo[0].trim()
        monthToday = todayInfo[1].trim()
        dayToday = todayInfo[2].trim()
        FirebaseDatabase.getInstance().getReference("calendar").child(user)
            .child("$yearToday"+"년").child("$monthToday"+"월").child("$dayToday"+"일")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    todoList.clear()
                    for (data in dataSnapshot.children) {
                        todoList.add(data.getValue<Todo>()!!)
                    }
                    adapter.notifyDataSetChanged()          //화면 업데이트
                }
            })
    }
    private fun splitDate(date: String): Array<String> {
        val splitText = date.split("/")
        val resultDate: Array<String> = Array(3){""}
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }
    override fun onLongClick(position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("일정 삭제")
        builder.setMessage("정말 삭제하시겠습니까?")
        builder.setNegativeButton("NO", null)
        builder.setPositiveButton("YES",
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    deleteTodo(position)
                }
            }
        )
        builder.show()
    }
    private fun deleteTodo(position: Int) {
        todoList.removeAt(position)
        FirebaseDatabase.getInstance().getReference("calendar")
            .child("$yearToday"+"년").child("$monthToday"+"월").child("$dayToday"+"일").removeValue()
        adapter.notifyDataSetChanged()
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}