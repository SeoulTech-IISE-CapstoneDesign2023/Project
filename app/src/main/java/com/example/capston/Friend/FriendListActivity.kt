package com.example.capston.Friend

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.R
import com.example.capston.User
import com.example.capston.databinding.ActivityFriendListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase

class FriendListActivity : AppCompatActivity(), OnItemLongClickListener {

    var myFriends = mutableListOf<User>()


    val binding by lazy { ActivityFriendListBinding.inflate(layoutInflater) }
    lateinit var auth: FirebaseAuth

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.friendListToolbar.inflateMenu(R.menu.friend_list_menus)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.goToAddFr -> {
                val intent = Intent(this@FriendListActivity, AddFriendActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.goToManageFr -> {
                val intent = Intent(this@FriendListActivity, ManageFriendActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.friendListToolbar)


        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser?.uid

        // 친구 데이터 경로 설정
        val usersRef = FirebaseDatabase.getInstance().getReference("user")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 친구들 저장할 리스트 생성
                this@FriendListActivity.myFriends = mutableListOf<User>()

                if (dataSnapshot.child(currentUser.toString())
                        .child("friend_info")
                        .child("accept_fr_req")
                        .childrenCount.toInt() != 0
                ) {
                    binding.friendReqChkTxt.visibility = View.VISIBLE
                } else binding.friendReqChkTxt.visibility = View.GONE

                val friendsSnapshot =
                    dataSnapshot.child(currentUser.toString())
                        .child("friend_info")
                        .child("friends")

                for (friendSnapshot in friendsSnapshot.children) {
                    val friendUid = friendSnapshot.key  // 유저의 친구들 식별자 가져오기

                    // 해당 친구의 정보를 가져오기
                    val friend = dataSnapshot.child(friendUid.toString())

                    val nickname = friend.child("user_info")
                        .child("nickname").value.toString()

                    val friendData = User(friendUid, nickname)
                    myFriends.add(friendData)

                    // 친구들 정보가 다 담아졌을 때 리사이클러 뷰 연결
                    if (myFriends.size == friendsSnapshot.childrenCount.toInt()) {
                        updateRecyclerView(myFriends)
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    // 뒤로가기 시 업데이트 되도록 설정
    override fun onResume() {
        super.onResume()

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser?.uid

        // 친구 데이터 경로 설정
        val usersRef = FirebaseDatabase.getInstance().getReference("user")

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 친구들 저장할 리스트 생성
                this@FriendListActivity.myFriends = mutableListOf<User>()

                if (dataSnapshot.child(currentUser.toString())
                        .child("friend_info")
                        .child("accept_fr_req")
                        .childrenCount.toInt() != 0
                ) {
                    binding.friendReqChkTxt.visibility = View.VISIBLE
                } else binding.friendReqChkTxt.visibility = View.GONE

                val friendsSnapshot =
                    dataSnapshot.child(currentUser.toString())
                        .child("friend_info")
                        .child("friends")

                for (friendSnapshot in friendsSnapshot.children) {
                    val friendUid = friendSnapshot.key  // 유저의 친구들 식별자 가져오기

                    // 해당 친구의 정보를 가져오기
                    val friend = dataSnapshot.child(friendUid.toString())

                    val nickname = friend.child("user_info")
                        .child("nickname").value.toString()

                    val friendData = User(friendUid, nickname)
                    myFriends.add(friendData)

                    // 친구들 정보가 다 담아졌을 때 리사이클러 뷰 연결
                    if (myFriends.size == friendsSnapshot.childrenCount.toInt()) {
                        updateRecyclerView(myFriends)
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onLongClick(position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("친구 삭제")
        builder.setMessage("정말 삭제하시겠습니까?")
        builder.setNegativeButton("NO", null)
        builder.setPositiveButton(
            "YES"
        ) { _, _ -> deleteFriend(position) }
        builder.show()
    }

    private fun deleteFriend(position: Int) {

        val value = myFriends[position].uid
        myFriends.removeAt(position) // todoList에서 삭제

        val currentUser = auth.currentUser?.uid

        val userRef = FirebaseDatabase.getInstance().getReference("user")

        userRef.child(currentUser.toString())
            .child("friend_info")
            .child("friends")
            .child(value.toString())
            .removeValue()

        userRef.child(value.toString())
            .child("friend_info")
            .child("friends")
            .child(currentUser.toString())
            .removeValue()

        updateRecyclerView(myFriends)
    }

    private fun updateRecyclerView(dataList: List<User>) {
        val friendAdapter = FriendListAdapter(
            this@FriendListActivity,
            myFriends, this@FriendListActivity
        )
        binding.recyclerviewFL.adapter = friendAdapter
        binding.recyclerviewFL.layoutManager = LinearLayoutManager(this@FriendListActivity)

        friendAdapter.notifyDataSetChanged()
    }
}