package com.example.capston

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.databinding.ActivitySearchWebBinding
import com.example.capston.location.ApiClient
import com.example.capston.location.Dto
import com.example.capston.location.LocationAdapter
import com.example.capston.location.LocationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchWebActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchWebBinding
    private lateinit var locationAdapter: LocationAdapter
    private var searchKeyWord: String = ""
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)

        locationAdapter = LocationAdapter{
            val intent = Intent()
            intent.putExtra("data",it.name)
            intent.putExtra("x",it.frontLat)
            intent.putExtra("y",it.frontLon)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
        val linearLayoutManager = LinearLayoutManager(this@SearchWebActivity)

        binding.locationRecyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = locationAdapter
        }

        val runnable = Runnable {
            location(searchKeyWord)
        }

        binding.editTextView.addTextChangedListener {
            searchKeyWord = it.toString()
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 300)
        }

    }

    private fun location(searchKeyword:String) {
        val locationService = ApiClient.retrofit.create(LocationService::class.java)
        locationService.getLocation("1", searchKeyword, "WGS84GEO", "WGS84GEO",200)
            .enqueue(object : Callback<Dto> {
                override fun onResponse(call: Call<Dto>, response: Response<Dto>) {
                    val name = response.body()?.searchPoiInfo?.pois?.poi?.map { it.name }
                    val address = response.body()?.searchPoiInfo?.pois?.poi?.map { it.newAddressList.newAddress.map { it.fullAddressRoad } }
                    Log.e("result","$name $address")
                    //토탈 카운트를 사용
                    locationAdapter.submitList( response.body()?.searchPoiInfo?.pois?.poi)
                }

                override fun onFailure(call: Call<Dto>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }
}