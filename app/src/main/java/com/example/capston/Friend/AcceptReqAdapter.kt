package com.example.capston.Friend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.User
import com.example.capston.databinding.MyfriendsViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AcceptReqAdapter(val User: MutableList<User>) : RecyclerView.Adapter<AcceptReqAdapter
.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MyfriendsViewBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return Math.min(40, User.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = User.get(position)
        holder.bind(user)
    }

    inner class ViewHolder(val binding: MyfriendsViewBinding) : RecyclerView
    .ViewHolder(binding.root) {

        fun bind(user: User) {
            // friendView 레이아웃 조작
            binding.friendName.text = user.nickname
            binding.buttonAccept.visibility = View.VISIBLE
            binding.buttonRefuse.visibility = View.VISIBLE
            binding.imageCal.visibility = View.GONE


            val database = Firebase.database
            val auth = FirebaseAuth.getInstance()
            val userRef = database.getReference("user")


            // 수락 버튼 기능 부여 (데이터베이스 조작과 어댑터 데이터 갱신)
            binding.buttonAccept.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = User[position]
                    // 데이터베이스 조작 코드
                    userRef.child(auth.uid.toString()).child("friend_info").child("friends")
                        .child(user.uid.toString()).setValue(true)
                    userRef.child(user.uid.toString()).child("friend_info").child("friends")
                        .child(auth.uid.toString()).setValue(true)
                    userRef.child(auth.uid.toString()).child("friend_info").child("accept_fr_req")
                        .child((user.uid.toString())).removeValue()
                    userRef.child(user.uid.toString()).child("friend_info").child("send_fr_req")
                        .child((auth.uid.toString())).removeValue()

                    // 어댑터의 데이터 갱신
                    removeItem(position)
                }
            }

            // 거절 버튼 기능 부여 (데이터베이스 조작과 어댑터 데이터 갱신)
            binding.buttonRefuse.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val user = User[position]

                    // 데이터베이스 조작 코드는 여기에 작성
                    userRef.child(auth.uid.toString()).child("friend_info").child("accept_fr_req")
                        .child((user.uid.toString())).removeValue()
                    userRef.child(user.uid.toString()).child("friend_info").child("send_fr_req")
                        .child((auth.uid.toString())).removeValue()

                    // 어댑터의 데이터 갱신
                    removeItem(position)
                }
            }


        }
    }

    fun removeItem(position: Int) {
        if (position in 0 until User.size) {
            User.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}