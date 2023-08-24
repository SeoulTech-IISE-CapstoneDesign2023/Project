package com.example.capston.Create


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.capston.R
import com.example.capston.databinding.ActivitySearchBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
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
    private var isChanged = false
    private val startMarker = Marker()
    private val arrivalMarker = Marker()
    private var startLng = 0.0
    private var startLat = 0.0
    private var arrivalLng = 0.0
    private var arrivalLat = 0.0

    //장소검색 도로명 주소 받아오기
    private val getSearchResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val searchData = data.getStringExtra("data")
                    val lat = data.getStringExtra("x")
                    val lng = data.getStringExtra("y")
                    Log.e("위경도넘어온거", "$lat $lng")

                    if (isArrivalValueTextViewClicked) {
                        binding.arrivalValueTextView.text = searchData
                        updateArrivalMap(lat!!.toDouble(), lng!!.toDouble())
                        arrivalLat = lat.toDouble()
                        arrivalLng = lng.toDouble()
                        saveLocation()
                    } else {
                        binding.startValueTextView.text = searchData
                        updateStartMap(lat!!.toDouble(), lng!!.toDouble())
                        startLat = lat.toDouble()
                        startLng = lng.toDouble()
                        saveLocation()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createActionToolbar)

        val startAddress = intent.getStringExtra("startAddress")
        val arrivalAddress = intent.getStringExtra("arrivalAddress")

        loadLocation()
        binding.startValueTextView.apply {
            text = startAddress
            setOnClickListener {
                val intent = Intent(context, SearchWebActivity::class.java)
                isArrivalValueTextViewClicked = false
                getSearchResult.launch(intent)
                isChanged = false
            }
        }

        binding.arrivalValueTextView.apply {
            text = arrivalAddress
            setOnClickListener {
                val intent = Intent(context, SearchWebActivity::class.java)
                isArrivalValueTextViewClicked = true
                getSearchResult.launch(intent)
                isChanged = false
            }
        }
        binding.startTextView.setOnClickListener {
            updateStartMap(startLat, startLng)
        }

        binding.arrivalTextView.setOnClickListener {
            updateArrivalMap(arrivalLat, arrivalLng)
        }
        binding.navermap.onCreate(savedInstanceState)
        binding.navermap.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        binding.myLocationButton.setOnClickListener {
            checkPermission()
            isChanged = false
        }

        binding.changeAddressButton.setOnClickListener {
            changeAddress()
            val saveLat = startLat
            val saveLng = startLng
            startLat = arrivalLat
            startLng = arrivalLng
            arrivalLat = saveLat
            arrivalLng = saveLng
            updateStartMap(startLat, startLng)
            updateArrivalMap(arrivalLat, arrivalLng)
            isChanged = true
            saveLocation()
        }

    }

    override fun onStart() {
        super.onStart()
        binding.navermap.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.navermap.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.navermap.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.navermap.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.navermap.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.navermap.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.navermap.onLowMemory()
    }


    private fun updateStartMap(lat: Double, lng: Double) {
        Log.e("출발", "$lat $lng")
        geocoder(binding.startValueTextView.text.toString()) { _, _ ->
            if (lat != null && lng != null) {
                Handler(Looper.getMainLooper()).post {
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
                    cameraUpdate.animate(CameraAnimation.Fly, 1000)
                    naverMap.moveCamera(cameraUpdate)
                    naverMap.minZoom = 5.0
                    naverMap.maxZoom = 18.0
                    startMarker.apply {
                        position = LatLng(lat, lng)
                        icon = OverlayImage.fromResource(R.drawable.baseline_place_24)
                        width = 150
                        height = 150
                        captionText = "출발지"
                        captionTextSize = 20F
                        iconTintColor = Color.GREEN
                        map = naverMap
                    }
                }
            }
        }
    }

    private fun updateArrivalMap(lat: Double, lng: Double) {
        Log.e("도착", "$lat $lng")
        geocoder(binding.arrivalValueTextView.text.toString()) { _, _ ->
            if (lat != null && lng != null) {
                Handler(Looper.getMainLooper()).post {
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(lat, lng))
                    cameraUpdate.animate(CameraAnimation.Fly, 1000)
                    naverMap.moveCamera(cameraUpdate)
                    naverMap.minZoom = 5.0
                    naverMap.maxZoom = 18.0
                    arrivalMarker.apply {
                        position = LatLng(lat, lng)
                        icon = OverlayImage.fromResource(R.drawable.baseline_place_24)
                        width = 150
                        height = 150
                        captionText = "도착지"
                        captionTextSize = 20F
                        iconTintColor = Color.BLUE
                        map = naverMap
                    }

                }
            }
        }
    }

    private fun changeAddress() {
        val str = binding.startValueTextView.text.toString()
        binding.startValueTextView.text = binding.arrivalValueTextView.text.toString()
        binding.arrivalValueTextView.text = str
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

    private fun geocoder(address: String, callback: (lat: Double?, lng: Double?) -> Unit) {
        Thread {
            try {
                val adrresses = g.getFromLocationName(address, 1)
                if (adrresses!!.isNotEmpty()) {
                    val location = adrresses[0]
                    val lat = location.latitude
                    val lng = location.longitude
                    callback(lat, lng)
                } else {
                    callback(null, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(null, null)
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
                if (binding.arrivalValueTextView.text == "" || binding.startValueTextView.text == "") {
                    Toast.makeText(this, "주소를 입력해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    saveData()
                    val intent = Intent(this, CreateActivity::class.java)
                    intent.putExtra("fragmentToShow", "mappingFragment") // 원하는 프래그먼트 식별자 전달
                    startActivity(intent)
                    finish()
                }

                Log.d("okMenu", binding.startValueTextView.text.toString())

                true
            }

            R.id.cancelMenu -> {
                cancelMenuDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cancelMenuDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("주소를 정말 삭제하시겠습니까?")
            setNegativeButton("아니오", null)
            setPositiveButton("네") { _, _ ->
                deletData()
                initLocation()
                val intent = Intent(context, CreateActivity::class.java)
                intent.putExtra("fragmentToShow", "mappingFragment") // 원하는 프래그먼트 식별자 전달
                startActivity(intent)
                finish()
            }
        }.show()
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

        if (arrivalLat != 0.0) {
            updateArrivalMap(arrivalLat, arrivalLng)
            updateStartMap(startLat, startLng)
        } else {
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.5666102, 126.9783881))
                .animate(CameraAnimation.Easing)
            naverMap.moveCamera(cameraUpdate)
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
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    updateStartTextView()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //출발 도착 위경도 저장
    private fun saveLocation() {
        with(getSharedPreferences("location", Context.MODE_PRIVATE).edit()) {
            putString("startLat", startLat.toString())
            putString("startLng", startLng.toString())
            putString("arrivalLat", arrivalLat.toString())
            putString("arrivalLng", arrivalLng.toString())
            apply()
        }
    }

    //맵 저장취소버튼을 눌렀을때 데이터 초기화해주기
    private fun initLocation() {
        with(getSharedPreferences("location", Context.MODE_PRIVATE).edit()) {
            putString("startLat", "0.0")
            putString("startLng", "0.0")
            putString("arrivalLat", "0.0")
            putString("arrivalLng", "0.0")
            apply()
        }
    }

    private fun loadLocation() {
        with(getSharedPreferences("location", Context.MODE_PRIVATE)) {
            startLat = getString("startLat", "0.0")!!.toDouble()
            startLng = getString("startLng", "0.0")!!.toDouble()
            arrivalLat = getString("arrivalLat", "0.0")!!.toDouble()
            arrivalLng = getString("arrivalLng", "0.0")!!.toDouble()
        }
    }
}