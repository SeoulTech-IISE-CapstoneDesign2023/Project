package com.example.capston.Friend

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.R
import com.example.capston.User
import com.example.capston.databinding.ActivityAddFriendBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AddFriendActivity : AppCompatActivity() {

    val binding by lazy { ActivityAddFriendBinding.inflate(layoutInflater)}

    var originalList= mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val database = Firebase.database
        val auth = FirebaseAuth.getInstance()

        val userRef = database.getReference("user")

        val searchView = binding.searchView

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 전체 유저 데이터 담아오기
                originalList.clear()
                for (userSnapshot in snapshot.children) {

                    val uid = userSnapshot.key
                    val nickname = userSnapshot.child("user_info").child("nickname").value.toString()

                    if (uid == auth.uid) {
                        continue
                    }
                    // userData 객체를 생성하여 정보를 저장
                    val userData = User(uid,nickname)
                    originalList.add(userData)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("geon","Failed to read friend data.", error.toException())
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                // 검색 버튼을 눌렀을 때 호출됩니다.
                performSearch(query)

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty() ) {

                    // 검색창 빈칸될 경우 처리 (x) 버튼 구현?
                    Log.d("geon","Empty Query")
                    var emptyList= mutableListOf<User>()
                    updateRecyclerView(emptyList)
                }
                return true
            }
        })


    }
    private fun updateRecyclerView(dataList: List<User>) {

        // 어댑터에 새로운 데이터 설정하여 업데이트
        val searchAdapter = AddFriendAdapter(dataList as MutableList<User>)
        binding.searchRcView.adapter = searchAdapter
        binding.searchRcView.layoutManager = LinearLayoutManager(binding.root.context)

        searchAdapter.notifyDataSetChanged()
    }

    private fun performSearch(query: String) {
        // 검색어를 사용
        val filteredList = originalList.filter { item ->
            item.nickname!!.contains(query, ignoreCase = true) // 대소문자를 구분하지 않고 검색어를 포함하는지 확인합니다.
        }

        updateRecyclerView(filteredList)
    }
}