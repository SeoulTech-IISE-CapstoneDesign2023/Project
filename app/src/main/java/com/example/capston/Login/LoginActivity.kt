package com.example.capston.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.capston.Key.Companion.DB_URL
import com.example.capston.Key.Companion.DB_USERS
import com.example.capston.Key.Companion.DB_USER_INFO
import com.example.capston.MainActivity
import com.example.capston.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        val user = auth.currentUser
        Log.d("geon_test_curUser","현재 사용자 로그인 여부 ${user?.email}")

        // 로딩화면 형성
        showLoading()

        // 인증 상태 변화 이벤트 처리
        var count = 0
        if (user != null) {
            // 사용자가 로그인한 경우
            Log.d("geon_test","유저 정보 확인 -> DB 확인 진행중")
            val userData = FirebaseDatabase.getInstance().getReference("user")
            userData.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        Log.d("geon","   ${snapshot.key}와 ${user.uid} 비교...")
                        if (snapshot.key == user.uid) {
                            count = 1
                            break
                        } else continue
                    }
                    Log.d("geon","반복 조회 결과 $count")
                    // 1이면 로그인 자동진행 아니면 로그인창 진입
                    if (count == 1) {
                        val intent =
                            Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Firebase.auth.signOut()
                        hideLoading()
                    }
                    Log.d("geon","유저상태 확인 과정 종료")
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                }
            })
        } else {
            // 사용자가 로그아웃한 경우 또는 인증 정보가 없는 경우
            Firebase.auth.signOut()
            Log.d("geon","로그아웃 됨 (이미 정보 없음)")
            hideLoading()
        }

        // 로그인 버튼 클릭 -> 로그인 과정 진행
        binding.SignInBtn.setOnClickListener{

            val email = binding.inputID.text.toString()
            val password = binding.inputPW.text.toString()

            if (email == "") {
                Toast.makeText(baseContext, "아이디를 입력해주세요", Toast
                    .LENGTH_SHORT,)
                    .show()
            } else if (password == "") {
                Toast.makeText(baseContext, "비밀번호를 입력해주세요", Toast
                    .LENGTH_SHORT,)
                    .show()
            } else {
                // 파이어베이스 로그인
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("geon_test_login", "signInWithEmail:success")

                            val currentUser = auth.currentUser
                            val userId = currentUser?.uid ?: ""
                            Toast.makeText(baseContext, "지금 로그인된 유저 이메일 ${currentUser?.email}",
                                Toast.LENGTH_SHORT,).show()
                            //로그인이 된후에 fcm토큰 생성 한후 realTimeDataBase에 업데이트
                            Firebase.messaging.token.addOnCompleteListener {
                                val token = it.result
                                val user = mutableMapOf<String,Any>()
                                user["userId"] = userId
                                user["fcmToken"] = token

                                Firebase.database(DB_URL).reference.child(DB_USERS).child(userId).child(DB_USER_INFO).updateChildren(user)

                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("geon_test_login", "signInWithEmail:failure")
                            Toast.makeText(
                                baseContext,
                                "아이디 비밀번호를 확인하세요",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }

        binding.SignUpBtn.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.findPWBtn.setOnClickListener{

            val emailAddress = binding.inputID.text.toString()

            if (emailAddress == "") {
                Toast.makeText(baseContext, "이메일을 입력해주세요", Toast.LENGTH_SHORT,).show()
            } else {
                Firebase.auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // 이메일 전송 성공
                            Toast.makeText(baseContext, "비밀번호 변경 이메일이 전송되었습니다", Toast
                                .LENGTH_SHORT,)
                                .show()
                            Log.d("geon_test", "Email sent.")
                        } else {
                            // 이메일 전송 실패
                            val exception = task.exception
                            Log.e("geon_test", "Email sending failed")
                            Toast.makeText(baseContext, "이메일을 다시 확인해주세요", Toast
                                .LENGTH_SHORT,)
                                .show()
                        }
                    }
            }
        }


    }
    fun showLoading() {
        Log.d("geon","showLoading()")
        val loading = binding.imageLoadingView
        loading.visibility = View.VISIBLE
        binding.SignInBtn.isVisible = false
        binding.logo.isVisible = false
        binding.inputID.isVisible = false
        binding.inputPW.isVisible = false
        binding.textView.isVisible = false
        binding.textView2.isVisible = false
        binding.SignUpBtn.isVisible = false
        binding.findPWBtn.isVisible = false
    }

    // 로딩 화면을 숨기는 함수
    fun hideLoading() {
        Log.d("geon","hideLoading()")
        val loading = binding.imageLoadingView
        loading.visibility = View.GONE
        binding.SignInBtn.isVisible = true
        binding.logo.isVisible = true
        binding.inputID.isVisible = true
        binding.inputPW.isVisible = true
        binding.textView.isVisible = true
        binding.textView2.isVisible = true
        binding.SignUpBtn.isVisible = true
        binding.findPWBtn.isVisible = true
    }
}