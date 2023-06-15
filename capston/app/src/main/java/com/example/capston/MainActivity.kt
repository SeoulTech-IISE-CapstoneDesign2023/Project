package com.example.capston

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
}