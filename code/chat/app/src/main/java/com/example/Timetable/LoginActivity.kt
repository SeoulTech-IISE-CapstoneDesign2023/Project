package com.example.Timetable

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.Timetable.databinding.ActivityLoginBinding
import java.time.format.SignStyle


class LoginActivity : AppCompatActivity() {
    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.SignInBtn.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        binding.SignUpBtn.setOnClickListener{
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.findPWBtn.setOnClickListener{
            val intent = Intent(this,FindPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}