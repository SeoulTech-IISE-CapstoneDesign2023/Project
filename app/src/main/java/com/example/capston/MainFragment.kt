package com.example.capston

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.Calendar.OnItemLongClickListener
import com.example.capston.Calendar.OnItemShortClickListener
import com.example.capston.Calendar.TodoListAdapter
import com.example.capston.Create.CreateActivity
import com.example.capston.Key.Companion.DB_ALARMS
import com.example.capston.Key.Companion.DB_URL
import com.example.capston.alarm.AlarmItem
import com.example.capston.alarm.NotificationReceiver
import com.example.capston.databinding.FragmentMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainFragment : Fragment(), OnItemLongClickListener, OnItemShortClickListener {
    lateinit var binding: FragmentMainBinding
    private var param1: String? = null
    private var param2: String? = null
    lateinit var user: String
    private var todayStr: String = ""
    val todoList = arrayListOf<Todo>()
    var todoKeys: java.util.ArrayList<String> = arrayListOf()   //메시지 키 목록
    val adapter = TodoListAdapter(todoList, this,this)
    lateinit var yearToday: String
    lateinit var monthToday: String
    lateinit var dayToday: String
    private lateinit var alarmAdapter: AlarmAdapter
    private val list = mutableListOf<AlarmItem>()

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
        binding = FragmentMainBinding.inflate(inflater, container, false)
        user = Firebase.auth.currentUser?.uid ?: ""

        //RecyclerView 설정
        binding.todoRecyclerView.adapter = adapter
        binding.todoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        //bottomSheet 알람
        alarmAdapter = AlarmAdapter{
            //데이터 삭제
            alertDialog(it)
            //todo todo도 삭제해야함
        }
        binding.bottomSheetLayout.alarmListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = alarmAdapter
        }

        // 오늘 날짜 가져오기
        var today = Calendar.getInstance()
        var todayYear = today[Calendar.YEAR]
        var todayMonth = today[Calendar.MONTH]
        var todayDay = today[Calendar.DAY_OF_MONTH]
//        todayStr = "${todayYear}/${todayMonth + 1}/$todayDay"
        todayStr = String.format("%04d/%02d/%02d",todayYear,todayMonth+1,todayDay)
        Log.e("todayStr",todayStr)


        // 오늘 todolist 불러오기
        todayDate(todayStr)

        //일정 생성 버튼 플러스 버튼 누르면 오늘 날짜 createActivity에 값 추가 및 이동
        binding.addtodoButton.setOnClickListener {
            val intent = Intent(requireActivity(), CreateActivity::class.java)
            intent.putExtra("startDate", todayStr)
            requireActivity().startActivity(intent)
        }

        updateBottomSheetRecyclerView()
        binding.bottomSheetLayout.noItemTextView.isVisible = alarmAdapter.itemCount == 0
        return binding.root
    }

    private fun alertDialog(alarmItem: AlarmItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("알림을 취소하시겠습니까?")
            .setPositiveButton("네"){_,_ ->
                val notificationId = alarmItem.notificationId?.toInt() ?: 0
                Firebase.database(DB_URL).reference.child(DB_ALARMS).child(user).child(notificationId.toString()).removeValue()
                deleteAlarm(notificationId,requireContext())
            }
            .setNegativeButton("아니오"){dialogInterface,_ ->
                dialogInterface.cancel()
            }
            .show()

    }

    private fun deleteAlarm(notificationId: Int, context: Context) {
        val notificationIntent = Intent(context,NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

    }

    //bottomSheet recyclerView 업데이트
    private fun updateBottomSheetRecyclerView(){
        val currentUserAlarmDB = Firebase.database.reference.child(DB_ALARMS).child(user)
        currentUserAlarmDB.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.value as Map<*, *>
                Log.e("onChildAdded",data.toString())
                val notificationId = data["notificationId"] as String?
                val alarmItem = AlarmItem(
                    todo = data["todo"] as String?,
                    notificationId = notificationId,
                    time = data["time"] as String?,
                    userId = data["userId"] as String?,
                    timeFormat = data["timeFormat"] as String?,
                )
                Log.e("onChildAdded","$alarmItem")

                if(notificationId != null && !listContatinNotificationId(notificationId)){
                    list.add(alarmItem)
                    list.sortBy { alarmItem ->
                        alarmItem.notificationId
                    }
                }
                alarmAdapter.submitList(list)
                //데이터가 없을 경우 아이템이 없다고 보여줌
                binding.bottomSheetLayout.noItemTextView.isVisible = alarmAdapter.itemCount == 0

                Log.e("MainFragment","$list")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.value as Map<*, *>
                Log.e("onChildChanged",data.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val data = snapshot.value as Map<*, *>
                Log.e("onChildRemoved",data.toString())
                val notificationId = data["notificationId"] as String?
                val alarmItem = AlarmItem(
                    todo = data["todo"] as String?,
                    notificationId = notificationId,
                    time = data["time"] as String?,
                    userId = data["userId"] as String?,
                    timeFormat = data["timeFormat"] as String?,
                )
                list.remove(alarmItem)
                alarmAdapter.notifyDataSetChanged()
                //데이터가 없을 경우 아이템이 없다고 보여줌
                binding.bottomSheetLayout.noItemTextView.isVisible = alarmAdapter.itemCount == 0
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.value as Map<*, *>
                Log.e("onChildMoved",data.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("onCancelled",error.message)
            }

        })
    }

    private fun listContatinNotificationId(notificationId :String) : Boolean {
        return list.any{alarmItem ->
            alarmItem.notificationId == notificationId
        }
    }

    // today's todolist 불러오기
    private fun todayDate(date: String) {
        val todayInfo = splitDate(date)
        yearToday = todayInfo[0].trim()
        monthToday = todayInfo[1].trim()
        dayToday = todayInfo[2].trim()
        FirebaseDatabase.getInstance().getReference("calendar").child(user)
            .child("$yearToday"+"년").child("$monthToday"+"월").child("$dayToday"+"일")
            .orderByChild("st_time")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    todoList.clear()
                    for (data in dataSnapshot.children) {
                        todoList.add(data.getValue<Todo>()!!)
                    }
                    todoList.sortBy{ it.st_time }
                    adapter.notifyDataSetChanged()          //화면 업데이트
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
        val todo = todoList[position] // 선택한 위치의 todo객체를 가져옴
        val todoKey = todoKeys[position]
        //Fragment로 데이터 전송
        val bundle = Bundle()
        bundle.putParcelable("todo", todo)
        bundle.putString("todoKey", todoKey)
        //Activity로 데이터 전송
        val intent = Intent(requireContext(), CreateActivity::class.java)
        intent.putExtra("todo", todo)
        intent.putExtra("todoKey",todoKey)
        startActivity(intent)
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
        val value = todoList[position].title
        todoList.removeAt(position) // todoList에서 삭제
        val databaseReference = FirebaseDatabase.getInstance().getReference("calendar").child(user)
            .child("$yearToday" + "년").child("$monthToday" + "월").child("$dayToday" + "일")
        val query = databaseReference.orderByChild("title").equalTo(value)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    data.ref.removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "일정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        })
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