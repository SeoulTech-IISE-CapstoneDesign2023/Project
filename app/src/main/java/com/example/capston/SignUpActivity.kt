package com.example.capston

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.capston.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    val binding by lazy { ActivitySignUpBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        val checkPW = binding.checkPW
        val authUserBtn = binding.authUserBtn
        val authEmailBtn = binding.authEmailBtn
        val createBtn = binding.createUserBtn
        val authCheckBtn = binding.authCheckBtn

        val editEmail = binding.editEmail
        val editPW = binding.editPW
        val editCheckPW = binding.editCheckPW

        val authCheckUser = binding.authCheckText1
        val authCheckEmail = binding.authCheckText2


        var checkStatusPW = 0

        // 이메일 형식에 대한 유효성 체크 -> 버튼 활성화 방식
        editEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                authUserBtn.isEnabled = false
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때마다 호출됩니다.
                val inputText = s.toString()
                // 버튼 활성화/비활성화 조건을 확인합니다.
                val isButtonEnabled = isValidEmail(inputText)
                authUserBtn.isEnabled = isButtonEnabled
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        // 비밀번호 유효성 체크
        editPW.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkStatusPW = 0
                editCheckPW.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
                editCheckPW.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.black))
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        // 비밀번호 유효성 체크
        editCheckPW.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkStatusPW = 0
                editCheckPW.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
                editCheckPW.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.black))

            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        // 비밀번호 확인에 대한 체크 과정
        checkPW.setOnClickListener {
            // 비밀번호 입력 체크 버튼 (일치 여부확인)
            if (editPW.text.length < 6) {
                Toast.makeText(baseContext, "비밀번호는 6자 이상이어야 합니다. 다시 입력해주세요", Toast.LENGTH_SHORT,)
                    .show()
            } else {
                if (editPW.text.toString() == editCheckPW.text.toString()) {
                    checkStatusPW = 2
                    editCheckPW.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.green
                        )
                    )
                    editCheckPW.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.green
                        )
                    )
                    Toast.makeText(baseContext, "비밀번호가 일치합니다", Toast.LENGTH_SHORT,)
                        .show()
                } else {
                    checkStatusPW = 1
                    editCheckPW.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.red
                        )
                    )
                    editCheckPW.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.red
                        )
                    )
                    Toast.makeText(baseContext, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT,)
                        .show()
                }
            }
            Log.d("geon_test_2323", "현재 이용자? ${auth.currentUser}")
        }

        authUserBtn.setOnClickListener {
            // 사용자 생성 버튼 클릭시
            // 비밀번호 확인절차 체크
            // 계정 생성 함수 (파이어베이스에)
            if (checkStatusPW == 2) {

                val email = editEmail.text.toString()
                val password = editPW.text.toString()

                // 파이어베이스에 계정 생성
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            authCheckUser.visibility = View.VISIBLE
                            authUserBtn.visibility = View.GONE
                            authEmailBtn.visibility = View.VISIBLE
                            authCheckBtn.visibility = View.VISIBLE


                            editEmail.isEnabled = false
                            editPW.isEnabled = false
                            editCheckPW.isEnabled = false
                            checkPW.isEnabled = false

                            // Sign in success
                            val user = auth.currentUser
                            Toast.makeText(baseContext, "사용자가 생성되었습니다.", Toast.LENGTH_SHORT,)
                                .show()
                            Log.d("geon_test_user","user create $email & $user")
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(baseContext, "이미 가입된 사용자입니다.", Toast.LENGTH_SHORT,)
                                .show()
                        }
                    }

            } else if (checkStatusPW == 1) {
                Toast.makeText(baseContext, "비밀번호 일치 여부를 확인해주세요", Toast.LENGTH_SHORT,)
                    .show()
            } else {
                Toast.makeText(baseContext, "비밀번호 일치 확인을 진행해주세요", Toast.LENGTH_SHORT,)
                    .show()
            }
            Log.d("geon_test_email", "현재 이용자? ${auth.currentUser?.email}")
        }

        val user = auth.currentUser

        authEmailBtn.setOnClickListener {
            sendEmailVerification()
        }



        val MAX_RETRY_COUNT = 5 // 최대 반복 횟수

        val handler = Handler(Looper.getMainLooper())
        var retryCount = 0 // 반복 횟수를 저장할 변수

        val runnable = object : Runnable {
            override fun run() {
                // 일정 작업 수행
                // 사용자의 이메일 확인 작업 감지
                auth.addAuthStateListener { firebaseAuth ->
                    val user = firebaseAuth.currentUser

                    onEmailVerificationCompleted()

                    // 원하는 결과가 나왔을 때 종료 및 알림 전송
                    if (user != null && user.isEmailVerified) {
                        // 성공 알림 전송
                        Log.d("geon_test_email", "이메일 확인")
                        // 사용자가 로그인 상태이고 이메일이 확인되었습니다.
                        // 추가 작업 수행
                        authCheckUser.visibility = View.GONE
                        authCheckEmail.visibility = View.VISIBLE
                        authEmailBtn.visibility = View.GONE
                        authCheckBtn.visibility = View.GONE

                        createBtn.isEnabled = true
                    } else {
                        if (retryCount < MAX_RETRY_COUNT) {
                            // 일정 시간 후에 작업을 반복하기 위해 핸들러에 다시 post
                            handler.postDelayed(this, 500)
                            retryCount++
                            Log.d("geon_test_123", "사용자 이메일 인증 -> ${user?.isEmailVerified}")
                        } else {
                            // 실패 알림 전송
                            Log.d("geon_test_email", "이메일 인증 실패")
                        }
                    }
                }
            }
        }

        // 버튼 클릭 이벤트에서 작업 시작
        authCheckBtn.setOnClickListener {
            retryCount = 0
            // 쓰레드 시작
            Thread(runnable).start()

            Log.d("geon_test_email", "현재 이용자? ${auth.currentUser}")
        }

        createBtn.setOnClickListener {
            val user = Firebase.auth.currentUser

            val myUid = user?.uid

            FirebaseDatabase.getInstance().getReference("User").child("users")
                .child(myUid.toString()).child("uid").setValue(myUid)

            Firebase.auth.signOut()

            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // 현재 사용자가 존재하는 경우
        if (currentUser != null && currentUser?.isEmailVerified == false) {
            // 사용자를 삭제합니다.
            currentUser.delete()
                .addOnSuccessListener {
                    // 사용자 삭제 성공
                    // 추가적인 작업
                }
                .addOnFailureListener { exception ->
                    // 사용자 삭제 실패
                    // 실패 처리를 수행 오류 메시지를 표시
                }
        }
    }

    // 이메일 유효성 체크 함수
    fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    fun sendEmailVerification() {
        // [START send_email_verification]
        val user = Firebase.auth.currentUser

        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "이메일 인증을 진행해주세요", Toast.LENGTH_SHORT,)
                        .show()
                }
            }
        // [END send_email_verification]
    }

    // 사용자의 이메일 확인 상태를 업데이트하는 메서드
    fun updateEmailVerificationStatus() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updatedUser = auth.currentUser
                val isEmailVerified = updatedUser?.isEmailVerified

                if (isEmailVerified == true) {
                    // 이메일이 확인되었습니다. 추가 작업 수행
                } else {
                    // 이메일이 아직 확인되지 않았습니다.
                }
            } else {
                // 업데이트 실패
                val exception = task.exception
                // 실패 처리 로직 구현
            }
        }
    }

    // 이메일 확인 작업이 완료된 경우 호출되는 메서드
    fun onEmailVerificationCompleted() {
        // 사용자의 이메일 확인 상태를 업데이트
        updateEmailVerificationStatus()
    }

}