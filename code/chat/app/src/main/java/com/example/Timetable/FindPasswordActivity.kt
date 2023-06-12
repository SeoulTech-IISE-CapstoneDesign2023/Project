package com.example.Timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.Timetable.databinding.ActivityFindPasswordBinding

class FindPasswordActivity : AppCompatActivity() {
    val binding by lazy { ActivityFindPasswordBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}