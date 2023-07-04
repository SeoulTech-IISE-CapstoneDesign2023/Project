package com.example.capston

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.commit
import com.example.capston.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var backPressedCallback: OnBackPressedCallback

    lateinit var auth: FirebaseAuth
    lateinit var myUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 뒤로가기 콜백 초기화
        backPressedCallback = object : OnBackPressedCallback(true) {
            var waitTime = 0L
            override fun handleOnBackPressed() {
                // 로그인이 완료된 경우 메인화면의 뒤로가기 -> 앱 종료
                if (auth.currentUser != null && auth.currentUser?.isEmailVerified == true) {
                    if(System.currentTimeMillis() - waitTime >=1500 ) {
                        waitTime = System.currentTimeMillis()
                        Toast.makeText(baseContext,"뒤로가기 버튼을 한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show()
                    } else {
                        finishAffinity()
                    }
                }
            }
        }
        // 뒤로가기 콜백 활성화
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        //FirebaseAuth 객체의 인스턴스 가져오기
        auth = FirebaseAuth.getInstance()
        //익명으로 로그인
        //Anonymoulsy()

        val fManager = supportFragmentManager
        fManager.commit {
            add(binding.mainFragment.id,MainFragment())
        }
        binding.mainButton.setOnClickListener{
            val currentFragment = fManager.findFragmentById(binding.mainFragment.id)
            if(currentFragment !is MainFragment){
                fManager.commit {
                    replace(binding.mainFragment.id,MainFragment())
                }
            }
        }

        binding.calendarButton.setOnClickListener{
            val currentFragment = fManager.findFragmentById(binding.mainFragment.id)
            if(currentFragment !is CalendarFragment){
                fManager.commit {
                    replace(binding.mainFragment.id,CalendarFragment())
                }
            }
        }

        binding.timeTableButton.setOnClickListener{
            val currentFragment = fManager.findFragmentById(binding.mainFragment.id)
            if(currentFragment !is TimeTableFragment){
                fManager.commit {
                    replace(binding.mainFragment.id,TimeTableFragment())
                }
            }
        }
        setSupportActionBar(binding.toolbar2)

    }
    // Firebase Authentication 익명으로 로그인
    fun Anonymoulsy() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Login", "signInAnonymously:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Login", "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }
    private fun updateUI(user: FirebaseUser?) { //update ui code here
        if (user != null) {
            myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            Log.d("Login", myUid)
            FirebaseDatabase.getInstance().getReference("User").child("users")
                .child(myUid).setValue(User(myUid))
        }
    }
}