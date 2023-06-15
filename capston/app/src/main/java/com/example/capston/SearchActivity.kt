package com.example.capston


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.databinding.ActivitySearchBinding
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Locale


class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivitySearchBinding
    private var isArrivalValueTextViewClicked: Boolean = false
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private var myLocationlat = 0.0
    private var myLocationlng = 0.0
    private val g by lazy { android.location.Geocoder(this, Locale.KOREAN) } //geocoder
    private var address: List<Address>? = null

    //장소검색 도로명 주소 받아오기
    private val getSearchResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        binding.startValueTextView.setOnClickListener {
            val intent = Intent(this, SearchWebActivity::class.java)
            isArrivalValueTextViewClicked = false
            getSearchResult.launch(intent)
        }
        binding.arrivalValueTextView.setOnClickListener {
            val intent = Intent(this, SearchWebActivity::class.java)
            isArrivalValueTextViewClicked = true
            getSearchResult.launch(intent)
        }
        binding.navermap.getMapAsync(this)

        binding.myLocationButton.setOnClickListener {
            if (myLocationlat == 0.0) {
                Toast.makeText(this, "지도의 자기위치를 업데이트해주세요", Toast.LENGTH_SHORT).show()
            } else {
                getMyLocation(myLocationlat, myLocationlng) { result ->
                    val address = result.replace("대한민국", "")
                    runOnUiThread {
                        binding.startValueTextView.text = address
                    }
                }
            }

        }


    }

    //내 위경도를 주소로 변환
    private fun getMyLocation(lat: Double, lng: Double, callback: (result: String) -> Unit) {
        Thread {
            var result = ""
            try {
                address = g.getFromLocation(lat, lng, 10)
                if (address!!.isNotEmpty()) {
                    address?.get(0)?.let {
                        Log.d("address", it.getAddressLine(0))
                        result = it.getAddressLine(0)
                    }
                }
                callback(result)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun saveData() {
        with(getSharedPreferences("addressInformation", Context.MODE_PRIVATE).edit()) {
            putString("startAddress", binding.startValueTextView.text.toString())
            putString("arrivalAddress", binding.arrivalValueTextView.text.toString())
            apply()
        }
        Toast.makeText(this,"데이터가 저장됨",Toast.LENGTH_SHORT).show()
    }
    private fun deletData(){
        with(getSharedPreferences("addressInformation", Context.MODE_PRIVATE).edit()) {
            putString("startAddress", "")
            putString("arrivalAddress", "")
            apply()
        }
        Toast.makeText(this,"데이터가 삭제됨",Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.okMenu -> {
                saveData()
                val intent = Intent(this, CreateActivity::class.java)
                intent.putExtra("fragmentToShow", "mappingFragment") // 원하는 프래그먼트 식별자 전달
                startActivity(intent)
                finish()
                true
            }
            R.id.cancelMenu ->{
                deletData()
                val intent = Intent(this, CreateActivity::class.java)
                intent.putExtra("fragmentToShow", "mappingFragment") // 원하는 프래그먼트 식별자 전달
                startActivity(intent)
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.createActionToolbar.inflateMenu(R.menu.create_menu)
        return true
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onMapReady(naverMap: NaverMap) {
        naverMap.mapType = NaverMap.MapType.Basic //네이버 지도 유형
        naverMap.uiSettings.isLocationButtonEnabled = true //현재위치 버튼
        naverMap.locationSource = locationSource
        //위치변화할때 내 위경도 데이터 업데이트
        naverMap.addOnLocationChangeListener { location ->
            myLocationlat = location.latitude
            myLocationlng = location.longitude
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) { //권한이 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}