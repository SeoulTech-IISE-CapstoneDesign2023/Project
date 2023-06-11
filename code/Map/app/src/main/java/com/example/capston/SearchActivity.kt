package com.example.capston

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import com.example.capston.R
import com.example.capston.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchBinding
    private var isArrivalValueTextViewClicked: Boolean = false
    //장소검색 도로명 주소 받아오기
    private val getSearchResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val searchData = data.getStringExtra("data")
                if (isArrivalValueTextViewClicked) {
                    binding.arrivalValueTextView.text = searchData
                } else {
                    binding.startValueTextView.text = searchData
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)

        binding.startValueTextView.setOnClickListener{
            val intent = Intent(this,SearchWebActivity::class.java)
            isArrivalValueTextViewClicked = false
            getSearchResult.launch(intent)
        }
        binding.arrivalValueTextView.setOnClickListener {
            val intent = Intent(this,SearchWebActivity::class.java)
            isArrivalValueTextViewClicked = true
            getSearchResult.launch(intent)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.okMenu ->{
                val intent = Intent(this,CreateActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.createActionToolbar.inflateMenu(R.menu.create_menu)
        return true
    }
}