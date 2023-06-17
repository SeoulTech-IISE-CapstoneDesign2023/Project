package com.example.capston


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        binding.myLocationButton.setOnClickListener {
            checkPermission()
        }


    }

    private fun updateStartTextView() {
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
        Toast.makeText(this, "데이터가 저장됨", Toast.LENGTH_SHORT).show()
    }

    private fun deletData() {
        with(getSharedPreferences("addressInformation", Context.MODE_PRIVATE).edit()) {
            putString("startAddress", "")
            putString("arrivalAddress", "")
            apply()
        }
        Toast.makeText(this, "데이터가 삭제됨", Toast.LENGTH_SHORT).show()
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

            R.id.cancelMenu -> {
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
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.mapType = NaverMap.MapType.Basic //네이버 지도 유형
        naverMap.uiSettings.isLocationButtonEnabled = true //현재위치 버튼
        //위치변화할때 내 위경도 데이터 업데이트
        naverMap.addOnLocationChangeListener { location ->
            myLocationlat = location.latitude
            myLocationlng = location.longitude
        }
    }

    private fun checkPermission() {
        when {//permission이 되었을 때
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                updateStartTextView()
            }
            // permission을 거부가 되었을 때 다이얼로그로 한번더 확인
            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionDialog()
            }
            else -> {
                requestLocationTrack()
            }
        }

    }

    //위치권한 확인 다이얼로그
    private fun showPermissionDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("위치 정보를 가져오기 위해서는 위치추적 권한이 필요합니다")
            setNegativeButton("취소", null)
            setPositiveButton("동의") { _, _ ->
                requestLocationTrack()
            }
        }.show()
    }

    //위치권한 요청
    private fun requestLocationTrack() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE ->{
                if(grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED){
                    updateStartTextView()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}