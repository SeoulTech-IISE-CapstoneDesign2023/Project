package com.example.Timetable

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.Timetable.databinding.ActivityAddFriendsBinding

class addFriendsActivity : AppCompatActivity() {
    val binding by lazy { ActivityAddFriendsBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}