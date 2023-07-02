package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.capston.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        Log.d("geon_test_curUser","${auth.currentUser}")

        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 임시 로그아웃
        // Firebase.auth.signOut()

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

                            val intent = Intent(this,MainActivity::class.java)
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
            val intent = Intent(this,SignUpActivity::class.java)
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
}