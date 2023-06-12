package com.example.capston

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import com.example.capston.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val fManager = supportFragmentManager
        fManager.commit {
            add(binding.mainFragment.id,MainFragment())
        }
        // fragment main
        binding.mainButton.setOnClickListener{
            val currentFragment = fManager.findFragmentById(binding.mainFragment.id)
            if(currentFragment !is MainFragment){
                fManager.commit {
                    replace(binding.mainFragment.id,MainFragment())
                }
            }
        }

        // fragment calendar
        binding.calendarButton.setOnClickListener{
            val currentFragment = fManager.findFragmentById(binding.mainFragment.id)
            if(currentFragment !is CalendarFragment){
                fManager.commit {
                    replace(binding.mainFragment.id,CalendarFragment())
                }
            }
        }

        // fragment timetable
        binding.timeTableButton.setOnClickListener{
            val currentFragment = fManager.findFragmentById(binding.mainFragment.id)
            if(currentFragment !is TimeTableFragment){
                fManager.commit {
                    replace(binding.mainFragment.id,TimeTableFragment())
                }
            }
        }
        setSupportActionBar(binding.toolbar2)

        //친구 버튼
//        binding.friendButton.setOnClickListener {
//            val intent = Intent(this, FriendListActivity::class.java)
//            startActivity(intent)
//        }

        //환경설정 버튼
//        binding.settingButton.setOnClickListener {
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
//        }

    }
}