package com.example.capston

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.Calendar.*
import com.example.capston.Create.CreateActivity
import com.example.capston.Friend.FriendListActivity
import com.example.capston.alarm.NotificationReceiver
import com.example.capston.databinding.FragmentCalendarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CalendarFragment : Fragment(), OnItemLongClickListener, OnItemShortClickListener {
    private lateinit var binding: FragmentCalendarBinding
    private var param1: String? = null
    private var param2: String? = null
    private var dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    lateinit var user: String
    val todoList = arrayListOf<Todo>()
    var todoKeys: ArrayList<String> = arrayListOf()   //메시지 키 목록
    val adapter = TodoListAdapter(todoList, this, this) // Firebase에서 일정 삭제

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
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        //calendar view custom
        binding.calendarView.addDecorators(
            SundayDecorator(),
            SaturdayDecorator(),
            MySelectorDecorator(requireActivity()),
            OneDayDecorator(),
            EventDecorator(
                Color.RED,
                Collections.singleton(CalendarDay.today())
            )
        )

        binding.friendButton.setOnClickListener {
            requireActivity().startActivity(Intent(context, FriendListActivity::class.java))
        }

        binding.settingButton.setOnClickListener {
            requireActivity().startActivity(Intent(context, SettingActivity::class.java))
        }

        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // todolist RecyclerView adapter
        binding.todoRecyclerView.adapter = adapter
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        //일정 생성 버튼
        binding.addtodoButton.setOnClickListener {
            saveDate()
        }
        // 오늘 날짜 받아오기
        val today = Calendar.getInstance()
        val todayYear = today[Calendar.YEAR]
        val todayMonth = today[Calendar.MONTH] + 1
        val todayDay = today[Calendar.DAY_OF_MONTH]
        val todayStr = String.format("%04d/%02d/%02d", todayYear, todayMonth, todayDay)

        // 시작 할 때 오늘 todolist 불러오기
        clickedDate(todayStr)

        //선택 날짜가 변경될 때 todolist 변경
        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            val year = date.year
            val month = date.month + 1
            val dayOfMonth = date.day
            dateStr = String.format("%04d/%02d/%02d", year, month, dayOfMonth)
            //오늘 날짜는 항상 Today's List
            if (dateStr == LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))) {
                binding.txtTodaylist.text = "Today's List"
            } else {
                binding.txtTodaylist.text = "$dateStr List"
            }
            //날짜에 따른 todolist 불러오기
            clickedDate(dateStr)
        }
        return binding.root
    }

    //캘린더에서 선택한 날짜 데이터 저장
    private fun saveDate() {
        val intent = Intent(requireContext(), CreateActivity::class.java)
        intent.putExtra("startDate", dateStr)
        startActivity(intent)
        Log.d("date", dateStr)
    }

    //일정 Database 읽기
    private fun clickedDate(date: String) {
        val clicked = splitDate(date)
        clickedYear = clicked[0].trim()
        clickedMonth = clicked[1].trim()
        clickedDay = clicked[2].trim()
        FirebaseDatabase.getInstance().getReference("calendar").child(user)
            .child(clickedYear + "년").child(clickedMonth + "월").child(clickedDay + "일")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("FirebaseData", "DataSnapshot: $dataSnapshot")
                    todoList.clear()
                    todoKeys.clear()
                    for (data in dataSnapshot.children) {
                        todoKeys.add(data.key!!)                    //키를 todoKeys 목록에 추가
                        todoList.add(data.getValue<Todo>()!!)
                    }
                    todoList.sortBy { it.st_time }
                    adapter.notifyDataSetChanged()          //화면 업데이트
                    Log.d("FirebaseData", "checkYear: $clickedYear")
                    Log.d("FirebaseData", "checkMonth: $clickedMonth")
                    Log.d("FirebaseData", "checkDay: $clickedDay")
                    Log.d("FirebaseData", "$todoKeys")
                }
            })
    }

    private fun splitDate(date: String): Array<String> {
        val splitText = date.split("/")
        val resultDate: Array<String> = Array(3) { "" }
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }

    override fun onShortClick(position: Int) {
        Log.d("TimetableFragment", "${todoList}")
        val todo = todoList[position] // 선택한 위치의 Todo객체를 가져옴
        val todoKey = todoKeys[position]
        //Fragment로 데이터 전송
        val bundle = Bundle()
        bundle.putParcelable("todo", todo)
        bundle.putString("todoKey", todoKey)
        //Activity로 데이터 전송
        val intent = Intent(requireContext(), CreateActivity::class.java)
        intent.putExtra("todo", todo)
        intent.putExtra("todoKey", todoKey)
        startActivity(intent)
    }

    override fun onLongClick(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("일정 삭제")
            .setMessage("일정을 삭제하시겠습니까?")
            .setPositiveButton("Yes",
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        deleteTodo(position)
                    }
                })
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteTodo(position: Int) {
        val todoKey = todoList[position].todoId
        todoList.removeAt(position) // todoList에서 삭제
        // 삭제할 일정 경로 참조
        val deleteReference = FirebaseDatabase.getInstance().getReference("calendar")
            .child(user)
            .child(clickedYear + "년")
            .child(clickedMonth + "월")
            .child(clickedDay + "일")
            .child(todoKey)
        // 일정 삭제
        deleteReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    data.ref.removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "일정이 삭제되었습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(requireContext(), "일정 삭제에 실패했습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }
            }
        })

    }

    private fun deleteAlarm(notificationId: Int, context: Context) {
        Firebase.database(Key.DB_URL).reference.child(Key.DB_ALARMS).child(user)
            .child(notificationId.toString()).removeValue()
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

    }

    companion object {
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