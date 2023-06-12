package com.example.capston

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.commit
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.EditFragment.EditTodoFragment
import com.example.capston.databinding.ActivityCreateBinding

class CreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)
        val fManager = supportFragmentManager
        fManager.commit {
            add(binding.frameLayout.id, EditTodoFragment())
        }

        binding.goTodoButton.setOnClickListener{
            val currentFragment = fManager.findFragmentById(binding.frameLayout.id)
            if(currentFragment !is EditTodoFragment){
                fManager.commit {
                    replace(binding.frameLayout.id,EditTodoFragment())
                }
            }
        }

        binding.goMappingButton.setOnClickListener{
            val currentFragment = fManager.findFragmentById(binding.frameLayout.id)
            if (currentFragment !is EditMappingFragment){
                fManager.commit {
                    replace(binding.frameLayout.id,EditMappingFragment())
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.createActionToolbar.inflateMenu(R.menu.create_menu)
        return true
    }
}