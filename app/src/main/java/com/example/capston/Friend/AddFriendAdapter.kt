package com.example.capston.Friend

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.User
import com.example.capston.databinding.MyfriendsViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.coroutineContext

class AddFriendAdapter(val User: MutableList<User>) : RecyclerView.Adapter<AddFriendAdapter
.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val binding = MyfriendsViewBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return ViewHolder(context, binding)
    }

    override fun getItemCount(): Int {
        return Math.min(40, User.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = User.get(position)
        holder.bind(user)
    }

    class ViewHolder(val context: Context, val binding: MyfriendsViewBinding) : RecyclerView
    .ViewHolder(binding.root) {
        fun bind(user: User) {

            // friendView 레이아웃 조작
            binding.friendName.text = user.nickname
            binding.buttonFollow.visibility = View.VISIBLE
            binding.imageCal.visibility = View.GONE

            val database = Firebase.database
            val auth = FirebaseAuth.getInstance()
            val userRef =
                database.getReference("user").child(auth.uid.toString()).child("friend_info")

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.child("friends").children) {
                        val friend = userSnapshot.key.toString()

                        if (user.uid == friend) {
                            // 이미 친구인 경우 follow 버튼 비활성화
                            binding.buttonFollow.isEnabled = false
                            break
                        }
                    }

                    for (userSnapshot in snapshot.child("send_fr_req").children) {
                        val friend = userSnapshot.key.toString()

                        if (user.uid == friend) {
                            // 이미 친구인 경우 follow 버튼 비활성화
                            binding.buttonFollow.isEnabled = false
                            binding.buttonFollow.text = "신청중"
                            break
                        }
                    }

                    for (userSnapshot in snapshot.child("accept_fr_req").children) {
                        val friend = userSnapshot.key.toString()

                        if (user.uid == friend) {
                            // 이미 친구인 경우 follow 버튼 비활성화
                            binding.buttonFollow.isEnabled = false
                            binding.buttonFollow.text = "수락 대기중"
                            break
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            // 버튼 기능 부여 (데이터베이스 조작)
            binding.buttonFollow.setOnClickListener {
                val database = Firebase.database
                val auth = FirebaseAuth.getInstance()
                val userRef = database.getReference("user")

                userRef.child(auth.uid.toString())
                    .child("friend_info")
                    .child("send_fr_req")
                    .child(user.uid.toString())
                    .setValue(true)

                userRef.child(user.uid.toString())
                    .child("friend_info")
                    .child("accept_fr_req")
                    .child((auth.uid.toString()))
                    .setValue(true)

                Toast.makeText(context, "친구 요청을 보냈습니다!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}