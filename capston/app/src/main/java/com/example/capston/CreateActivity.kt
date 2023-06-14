package com.example.capston

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.commit
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.EditFragment.EditTodoFragment
import com.example.capston.databinding.ActivityCreateBinding

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding
    private var startAddress = ""
    private var arrivalAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)
        getData()
        val fManager = supportFragmentManager
        val mappingFragment = EditMappingFragment.newInstance(startAddress, arrivalAddress)
        fManager.commit {
            add(binding.frameLayout.id, EditTodoFragment())
        }
        val fragmentToShow = intent.getStringExtra("fragmentToShow")
        if (fragmentToShow == "mappingFragment") {
            fManager.commit {
                replace(binding.frameLayout.id, mappingFragment) // 프래그먼트가 표시될 레이아웃 ID
            }
        }

        binding.goTodoButton.setOnClickListener {
            val currentFragment = fManager.findFragmentById(binding.frameLayout.id)
            if (currentFragment !is EditTodoFragment) {
                fManager.commit {
                    replace(binding.frameLayout.id, EditTodoFragment())
                }
            }
        }

        binding.goMappingButton.setOnClickListener {
            val currentFragment = fManager.findFragmentById(binding.frameLayout.id)
            if (currentFragment !is EditMappingFragment) {
                fManager.commit {
                    replace(binding.frameLayout.id, mappingFragment)
                }
            }
        }
    }

    private fun getData() {
        with(getSharedPreferences("addressInformation", Context.MODE_PRIVATE)) {
            startAddress = getString("startAddress", "").toString()
            arrivalAddress = getString("arrivalAddress", "").toString()
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.createActionToolbar.inflateMenu(R.menu.create_menu)
        return true
    }
}