package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capston.Login.LoginActivity
import com.example.capston.databinding.ActivitySettingBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private val uid = Firebase.auth.currentUser?.uid ?: ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //로그아웃버튼
        setSignOutButton()
        //toolbar에 뒤로가기버튼 활성화
        setSupportActionBar(binding.toolbar2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        updateNickName()
    }

    private fun setSignOutButton() {
        binding.signOutButton.setOnClickListener {
            Firebase.auth.signOut()
            //로그아웃을하면은 로그인 액티비티로 이동함
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateNickName() {
        Firebase.database.reference.child(Key.DB_USERS).child(uid).child(Key.DB_USER_INFO).get()
            .addOnSuccessListener {
                val userInfo = it.getValue(User::class.java)
                userInfo?.let { userInfo ->
                    binding.nickNameTextView.text = userInfo.nickname
                }
            }
    }
}