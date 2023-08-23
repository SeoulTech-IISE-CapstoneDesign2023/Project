package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.capston.Friend.FriendListActivity
import com.example.capston.Login.LoginActivity
import com.example.capston.alarm.NotificationReceiver
import com.example.capston.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var backPressedCallback: OnBackPressedCallback
    private val mainFragment = MainFragment()
    private val calendarFragment = CalendarFragment()
    private val timeTableFragment = TimeTableFragment()

    lateinit var auth: FirebaseAuth
    lateinit var myUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //메인으로 오면은 알림소리 꺼짐
        NotificationReceiver.mediaPlayer?.release()
        NotificationReceiver.mediaPlayer = null

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


        binding.friendButton.setOnClickListener{
            val intent = Intent(this, FriendListActivity::class.java)
            startActivity(intent)
        }

        //fragment 이동
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.main ->{
                    replaceFragment(mainFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.calendar ->{
                    replaceFragment(calendarFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.timeTable ->{
                    replaceFragment(timeTableFragment)
                    return@setOnItemSelectedListener true
                }
                else ->{
                    return@setOnItemSelectedListener false
                }
            }
        }
        //임시로 그냥 로그아웃버튼 만듬
        binding.signOutButton.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
        replaceFragment(mainFragment)
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

    private fun replaceFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.mainFragment,fragment)
                commit()
            }
    }
}