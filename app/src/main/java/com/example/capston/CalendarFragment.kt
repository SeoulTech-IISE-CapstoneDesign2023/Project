package com.example.capston

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.databinding.FragmentCalendarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CalendarFragment : Fragment(), OnItemLongClickListener {
    private lateinit var binding: FragmentCalendarBinding
    private var param1: String? = null
    private var param2: String? = null
    private var dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/ M/ dd"))
    lateinit var user: String
    val todoList = arrayListOf<Todo>()
    val adapter = TodoListAdapter(todoList,this) // Firebase에서 일정 삭제

    // 선택한 날짜
    lateinit var clickedYear: String
    lateinit var clickedMonth: String
    lateinit var clickedDay: String

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
        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // todolist RecyclerView adapter
        binding.todoRecyclerView.adapter = adapter
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        //일정 생성 버튼
        binding.addtodoButton.setOnClickListener {
            saveDate()
        }
        // 오늘 날짜 받아오기
        val today = android.icu.util.Calendar.getInstance()
        val todayYear = today[android.icu.util.Calendar.YEAR]
        val todayMonth = today[android.icu.util.Calendar.MONTH]
        val todayDay = today[android.icu.util.Calendar.DAY_OF_MONTH]
        val todayStr = "${todayYear}/${todayMonth + 1}/$todayDay"

        // 시작 할 때 오늘 todolist 불러오기
        clickedDate(todayStr)

        //선택 날짜가 변경될 때 todolist 변경
        binding.calendarView.setOnDateChangeListener{ _, year, month, dayOfMonth ->
            dateStr = "${year}/ ${month + 1}/ $dayOfMonth"
            //오늘 날짜는 항상 Today's List
            if (dateStr == LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/ M/ dd"))){
                binding.txtTodaylist.text = "Today's List"
            }else{
                binding.txtTodaylist.text = dateStr+" List"
            }
            //날짜에 따른 todolist 불러오기
            clickedDate(dateStr)
        }
        return binding.root
    }
    //캘린더에서 선택한 날짜 데이터 저장
    private fun saveDate(){
        val intent = Intent(requireContext(), CreateActivity::class.java)
        intent.putExtra("startDate",dateStr)
        startActivity(intent)
    }
    //일정 Database 읽기
    private fun clickedDate(date: String) {
        val clicked = splitDate(date)
        clickedYear = clicked[0].trim()
        clickedMonth = clicked[1].trim()
        clickedDay = clicked[2].trim()
        FirebaseDatabase.getInstance().getReference("calendar").child(user)
            .child("$clickedYear"+"년").child("$clickedMonth"+"월").child("$clickedDay"+"일")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("FirebaseData", "DataSnapshot: $dataSnapshot")
                    todoList.clear()
                    for (data in dataSnapshot.children) {
                        todoList.add(data.getValue<Todo>()!!)
                    }
                    adapter.notifyDataSetChanged()          //화면 업데이트
                    Log.d("FirebaseData", "checkYear: ${clickedYear}")
                    Log.d("FirebaseData", "checkMonth: ${clickedMonth}")
                    Log.d("FirebaseData", "checkDay: ${clickedDay}")
                    Log.d("FirebaseData", "Data loaded: ${todoList.size} items")
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
        builder.setPositiveButton("YES",
            object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    deleteTodo(position)
                }
            })
        builder.setNegativeButton("NO", null)
        builder.show()
    }
    private fun deleteTodo(position: Int) {
        FirebaseDatabase.getInstance().getReference("calendar")
            .child("$clickedYear"+"년").child("$clickedMonth"+"월").child("$clickedDay"+"일").removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 성공적으로 삭제되었을 때만 로컬 리스트에서 삭제
                    todoList.removeAt(position)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "일정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
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