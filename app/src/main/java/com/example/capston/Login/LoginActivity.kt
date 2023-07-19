package com.example.capston.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import com.example.capston.MainActivity
import com.example.capston.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        performTaskWithLoading()

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

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)

                            val currentUser = auth.currentUser
                            Toast.makeText(baseContext, "지금 로그인된 유저 이메일 ${currentUser?.email}",
                                Toast.LENGTH_SHORT,).show()

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

    // Coroutine을 실행하는 함수
    fun performTaskWithLoading() {
        // 로딩 화면을 표시
        showLoading()

        // Coroutine
        coroutineScope.launch {
            // 비동기 작업을 수행
            val result = withContext(Dispatchers.Default) {
                performAsyncTask()
            }
            handleResult(result)
        }
    }

    // 비동기 작업을 수행하는 함수
    suspend fun performAsyncTask(): Int {
        // 비동기 작업을 수행하는 코드
        // Initialize Firebase Auth
        auth = Firebase.auth

        Log.d("geon_test_curUser","현재 사용자 로그인 여부 ${auth.currentUser?.email}")

        // 인증 상태 변화 이벤트 처리
        val user = auth.currentUser
        var count = 0
        if (user != null) {
            // 사용자가 로그인한 경우
            Log.d("geon_test","로그인 처리")

            val userData = FirebaseDatabase.getInstance().getReference("user")
            userData.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        if (snapshot.key == auth.currentUser?.uid) {
                            count = 1
                            break
                        } else continue
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // 에러 처리
                }
            })
        } else {
            // 사용자가 로그아웃한 경우 또는 인증 정보가 없는 경우
            Firebase.auth.signOut()
        }
        delay(1000) // 2초간 대기하는 비동기 작업을 가정
        return count
    }

    fun showLoading() {
        val loading = binding.imageLoadingView
        loading.visibility = View.VISIBLE
    }

    // 로딩 화면을 숨기는 함수
    fun hideLoading() {
        val loading = binding.imageLoadingView
        loading.visibility = View.GONE
    }

    // 결과를 처리하는 함수
    fun handleResult(result: Int) {
        if (result == 1) {
            val intent =
                Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        } else {
            Firebase.auth.signOut()
            hideLoading()
        }
    }

}