package com.example.capston.EditFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.Bus.realLocation.BusRealLocationAPIService
import com.example.capston.Bus.realLocation.BusRealLocationConnection
import com.example.capston.Bus.realLocation.BusRealTimeLocationDTO
import com.example.capston.Bus.realtime.BusRealTimeAPIService
import com.example.capston.Bus.realtime.BusRealTimeConnection
import com.example.capston.Bus.realtime.RealTimeArrivalBus
import com.example.capston.R
import com.example.capston.Create.SearchActivity
import com.example.capston.Create.UserInfo
import com.example.capston.Key
import com.example.capston.Key.Companion.ALARMTIME
import com.example.capston.Key.Companion.KEY_ALARMTIME
import com.example.capston.Key.Companion.PLUS_TIME
import com.example.capston.Key.Companion.TYPE
import com.example.capston.Todo
import com.example.capston.car.CarAdapter
import com.example.capston.car.CarRouteRequest
import com.example.capston.car.DepartureInfo
import com.example.capston.car.DestinationInfo
import com.example.capston.car.Dto
import com.example.capston.car.RoutesInfo
import com.example.capston.car.TmapService
import com.example.capston.databinding.FragmentEditMappingBinding
import com.example.capston.retrofit.PublicTransitRoute
import com.example.capston.retrofit.SubPath
import com.example.capston.route.PublicTransitRouteConnection
import com.example.capston.route.PublicTransitRouteSearchAPIService
import com.example.capston.route.RoteAdapter
import com.example.capston.subway.SubwayTimeTableConnection
import com.example.capston.subway.SubwayTimeTableService
import com.example.capston.walk.RouteData
import com.example.capston.walk.WalkAdapter
import com.example.capston.walk.WalkService
import com.example.retrofit_example.retrofit2.stationTimeTableDTO
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.CountDownLatch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EditMappingFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEditMappingBinding
    private var minTotalTime: Int? = null
    private var plusTime  = ""
    private var info = mutableListOf<Info>()
    private val g by lazy { android.location.Geocoder(requireContext(), Locale.KOREAN) } //geocoder
    private var locationList = Array(4) { 0.0 }
    private var latestTime: Int? = null
    private val handler = Handler()
    private var isFailed = 0
    private var startX = 0.0
    private var startY = 0.0
    private var endX = 0.0
    private var endY = 0.0
    private lateinit var walkAdapter: WalkAdapter
    private lateinit var carAdapter: CarAdapter
    private val carRetrofit = Retrofit.Builder()
        .baseUrl("https://apis.openapi.sk.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    var startDate = ""
    var startTime = ""
    private var startLng = 0.0
    private var startLat = 0.0
    private var arrivalLng = 0.0
    private var arrivalLat = 0.0
    var currentUserFcmToken: String = ""//fcmToken정보 fcm서비스로 보내는거임
    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    // data를 전달하는 listener
    interface OnDataPassListener {
        fun onStartPass(startPlace: String?)
        fun onArrivePass(arrivePlace: String?)
    }
    private lateinit var dataPassListener : OnDataPassListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as OnDataPassListener
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditMappingBinding.inflate(inflater, container, false)

        //fcmapiKey
        val fcmServerKey = getString(R.string.fcm_server_key)
        //fcmToken 가져오기
        val currentUserDB = Firebase.database.reference.child(Key.DB_USERS).child(currentUserId)
            .child(Key.DB_USER_INFO)
        currentUserDB.get().addOnSuccessListener {
            val currentUserItem = it.getValue(UserInfo::class.java)
            currentUserFcmToken = currentUserItem?.fcmToken ?: ""
        }

        //list에서 일정 하나 선택했을 때 내용 수정 !! 여기를 수정해야할듯
        val todo = requireActivity().intent.getParcelableExtra<Todo>("todo")
        if (todo != null) {
            // 기존의 Todo를 수정하는 경우, Todo객체를 사용하여 화면을 초기화
            binding.startValueTextView.text = todo.startPlace
            binding.arrivalValueTextView.text = todo.arrivePlace
        } else {
            // 새로운 Todo를 생성하는 경우, 화면을 초기화
            binding.startValueTextView.text = ""
            binding.arrivalValueTextView.text = ""
        }
        //spinner adapter
        binding.trackingTimeSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.trackingTimes,
            android.R.layout.simple_list_item_1
        )
        //아이템 선택되었을때 콜백 되도록 허락
        binding.trackingTimeSpinner.onItemSelectedListener = this
        binding.searchlayer.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("startAddress", binding.startValueTextView.text.toString())
            intent.putExtra("arrivalAddress", binding.arrivalValueTextView.text.toString())
            startActivity(intent)
            requireActivity().finish()
        }

        // searchActivity에서 출발도착장소 업데이트
        binding.startValueTextView.text = param1
        binding.arrivalValueTextView.text = param2

        //교통수단 선택 버튼
        var transportation = 0 //자동차 1 대중교통 2 도보 3
        binding.carWayButton.setOnClickListener {
            transportation = 1
            Toast.makeText(context, "이동수단 : 자동차", Toast.LENGTH_SHORT).show()
        }
        binding.publicTransportButton.setOnClickListener {
            transportation = 2
            Toast.makeText(context, "이동수단 : 대중교통", Toast.LENGTH_SHORT).show()
        }
        binding.walkingButton.setOnClickListener {
            transportation = 3
            Toast.makeText(context, "이동수단 : 도보", Toast.LENGTH_SHORT).show()
        }
        //장소검색 버튼
        binding.searchButton.setOnClickListener {
            val startAdress = binding.startValueTextView.text.toString()
            val arrivalAdress = binding.arrivalValueTextView.text.toString()
            getDate()
            Log.d("getDate를통해 얻은 startTime과 startDate", "$startTime $startDate")
            when (transportation) {
                0 -> {
                    Toast.makeText(context, "교통수단을 설정해주세요.", Toast.LENGTH_SHORT).show()
                }

                1 -> {
                    //자동차
                    val inputFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                    val outputFormat = DateTimeFormatter.ISO_DATE_TIME
                    val pattern = Regex("\\d{1,2}:(\\d{1,2})")
                    val matchResult = pattern.find(startTime)
                    startTime = matchResult?.value ?: ""
                    Log.d("자동차 길찾기를 위한 startTime변환", startTime)
                    val inputDateTime = "$startDate $startTime"
                    val dateTime = LocalDateTime.parse(inputDateTime, inputFormat)
                    val isoDateTime = dateTime.format(outputFormat)
                    Log.d("iso", isoDateTime)
                    if (isoDateTime == null) {
                        Toast.makeText(context, "날짜와 시간을 입력해주세요", Toast.LENGTH_SHORT).show()
                    } else {
                        geocoder(startAdress) { _, _ ->
                            if (startLat == 0.0 || startLng == 0.0) {
                                requireActivity().runOnUiThread {
                                    Toast.makeText(
                                        requireContext(),
                                        "주소를 입력해주세요",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            } else {
                                locationList[0] = startLng
                                locationList[1] = startLat
                                geocoder(arrivalAdress) { _, _ ->
                                    locationList[2] = arrivalLng
                                    locationList[3] = arrivalLat
                                    Log.d(
                                        "locationList",
                                        "${locationList[0]} ${locationList[1]} ${locationList[2]} ${locationList[3]}"
                                    )
                                    startX = locationList[0]
                                    startY = locationList[1]
                                    endX = locationList[2]
                                    endY = locationList[3]
                                    requireActivity().runOnUiThread {
                                        //body생성
                                        val body = CarRouteRequest(
                                            routesInfo = RoutesInfo(
                                                departure = DepartureInfo(
                                                    name = "출발지",
                                                    lon = startX.toString(),
                                                    lat = startY.toString()
                                                ),
                                                destination = DestinationInfo(
                                                    name = "도착지",
                                                    lon = endX.toString(),
                                                    lat = endY.toString()
                                                ),
                                                predictionType = "departure",
                                                predictionTime = "$isoDateTime+0900"
                                            )
                                        )
                                        carAdapter = CarAdapter()
                                        binding.routeSearchResultRecyclerView.apply {
                                            layoutManager = LinearLayoutManager(context)
                                            adapter = carAdapter
                                        }
                                        searchRoute(body)

                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // 대중교통
                    geocoder(startAdress) { _, _ ->
                        if (startLat == 0.0 || startLng == 0.0) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "주소를 입력해주세요", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            locationList[0] = startLng
                            locationList[1] = startLat
                            geocoder(arrivalAdress) { _, _ ->
                                locationList[2] = arrivalLng
                                locationList[3] = arrivalLat
                                Log.d(
                                    "locationList",
                                    "${locationList[0]} ${locationList[1]} ${locationList[2]} ${locationList[3]}"
                                )
                                startX = locationList[0]
                                startY = locationList[1]
                                endX = locationList[2]
                                endY = locationList[3]
                                requireActivity().runOnUiThread {
                                    getPublicTransitRouteSearchData(startX, startY, endX, endY)
                                    Log.d("실행", "실행1")
                                    handler.postDelayed({
                                        initRecyclerView()
                                        if (isFailed == 0) {
                                            binding.totalTimeTextView.apply {
                                                if (minTotalTime != null) {
                                                    text = "총 소요시간 : ${minTotalTime}분"
                                                    val hours = minTotalTime!! / 60
                                                    val minutes = minTotalTime!! % 60
                                                    val alarmTime = String.format("%02d:%02d", hours, minutes)
                                                    saveAlarmTime(alarmTime,"subway")
                                                    isVisible = true
                                                }
                                            }
                                        }

                                    }, 1500)
                                    Log.d("실행", "실행2")
                                }
                            }
                        }
                    }
                    Toast.makeText(context, "길찾기 경로 로딩 중", Toast.LENGTH_SHORT).show()
                    isFailed = 0
                }

                3 -> {
                    //도보
                    geocoder(startAdress) { _, _ ->
                        if (startLat == 0.0 || startLng == 0.0) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "주소를 입력해주세요",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        } else {
                            locationList[0] = startLng
                            locationList[1] = startLat
                            geocoder(arrivalAdress) { _, _ ->
                                locationList[2] = arrivalLng
                                locationList[3] = arrivalLat
                                Log.d(
                                    "locationList",
                                    "${locationList[0]} ${locationList[1]} ${locationList[2]} ${locationList[3]}"
                                )
                                startX = locationList[0]
                                startY = locationList[1]
                                endX = locationList[2]
                                endY = locationList[3]
                                requireActivity().runOnUiThread {
                                    //body생성
                                    val body = RouteData(
                                        startX = startX,
                                        startY = startY,
                                        endX = endX,
                                        endY = endY,
                                        startName = "%EC%B6%9C%EB%B0%9C%EC%A7%80",
                                        endName = "%EB%8F%84%EC%B0%A9%EC%A7%80",
                                        searchOption = 4
                                    )
                                    walkAdapter = WalkAdapter()
                                    binding.routeSearchResultRecyclerView.apply {
                                        layoutManager = LinearLayoutManager(context)
                                        adapter = walkAdapter
                                    }
                                    walkRoute(body)
                                }
                            }
                        }
                    }
                }
            }
        }
        return binding.root
    }

    //시간데이터얻어오기
    private fun getDate() {
        requireActivity().getSharedPreferences("date", Context.MODE_PRIVATE).apply {
            startDate = getString("startDate1", "").toString()
            startTime = getString("startTime1", "").toString()
        }

        requireActivity().getSharedPreferences("location", Context.MODE_PRIVATE).apply {
            startLat = getString("startLat", "0.0")!!.toDouble()
            startLng = getString("startLng", "0.0")!!.toDouble()
            arrivalLat = getString("arrivalLat", "0.0")!!.toDouble()
            arrivalLng = getString("arrivalLng", "0.0")!!.toDouble()
        }
    }

    //자동차 경로 함수
    private fun searchRoute(body: CarRouteRequest) {
        //바디를 생성할때 필요한것은 출발지와 도착지와 출발 시간을 알려준다
        Log.d("body", body.toString())
        val service = carRetrofit.create(TmapService::class.java)
        Toast.makeText(context, "실시간 경로를 불러오고있습니다.", Toast.LENGTH_SHORT).show()
        service.getCarRoute(request = body).enqueue(object : Callback<Dto> {
            override fun onResponse(call: Call<Dto>, response: Response<Dto>) {
                Log.e("MainActivity", "${response.body().toString()}")
                binding.totalTimeTextView.isVisible = false
                carAdapter.submitList(response.body()?.features?.map { it.properties })
                //알람 시간을 저장
                val rawAlarmTime =
                    response.body()?.features?.map { it.properties.departureTime }?.firstOrNull()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
                val zonedDateTime = ZonedDateTime.parse(rawAlarmTime, formatter)
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                val alarmTime =
                    zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
                Log.e("alarmTime", alarmTime)
                saveAlarmTime(alarmTime,"car")

            }

            override fun onFailure(call: Call<Dto>, t: Throwable) {
                Log.e("fail", "실패")
                t.printStackTrace()
            }
        })
    }

    //길찾기를 통한 알람시간을 저장 자동차는 yyyy/MM/dd HH:mm으로 저장 대중교통,걷기는 HH:mm으로 저장 이거를 CreatActivity에 보내서 받아야함
    private fun saveAlarmTime(time: String,type:String) {
        with(requireActivity().getSharedPreferences(KEY_ALARMTIME, Context.MODE_PRIVATE).edit()) {
            Log.e("saveAlarmTime", "alarmTime : $time type : $type")
            putString(ALARMTIME, time)
            putString(PLUS_TIME,plusTime)
            putString(TYPE,type)
            apply()
        }
    }

    //도보 이동 소요시간 함수
    private fun walkRoute(body: RouteData) {
        //바디를 생성할때 필요한것은 출발지와 도착지와 출발 시간을 알려준다
        Log.d("body", body.toString())
        val service = carRetrofit.create(WalkService::class.java)
        Toast.makeText(context, "실시간 경로를 불러오고있습니다.", Toast.LENGTH_SHORT).show()
        service.getWalkingTime(request = body)
            .enqueue(object : Callback<com.example.capston.walk.Dto> {
                override fun onResponse(
                    call: Call<com.example.capston.walk.Dto>,
                    response: Response<com.example.capston.walk.Dto>
                ) {
                    Log.e("MainActivity", "${response.body().toString()}")
                    val good = response.body()?.features?.filter { it.properties.index == 0 }
                    binding.totalTimeTextView.isVisible = false
                    walkAdapter.submitList(good?.map { it.properties })
                    val time = good?.map { it.properties.totalTime }?.firstOrNull() ?: 0
                    val hours = (time / 60) / 60
                    val minutes = (time / 60) % 60
                    val alarmTime = String.format("%02d:%02d", hours, minutes)
                    Log.e("alarmTime", alarmTime) //저장되는 형태는 HH:mm
                    saveAlarmTime(alarmTime,"walk")
                }

                override fun onFailure(call: Call<com.example.capston.walk.Dto>, t: Throwable) {
                    Log.e("fail", "실패")
                    t.printStackTrace()
                }
            })
    }

    //대중교통 시간표 불러오기 함수
    private fun getPublicTransportationData(
        stationId: String,
        wayCode: Int,
        plusTime: Int = 0,
        callback: (Int?) -> Unit
    ) {

        val retrofitAPI =
            SubwayTimeTableConnection.getInstance().create(SubwayTimeTableService::class.java)
        val call = retrofitAPI.getStationTimeTableData(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            stationId
        )
        call.enqueue(object : Callback<stationTimeTableDTO> {
            override fun onResponse(
                call: Call<stationTimeTableDTO>,
                response: Response<stationTimeTableDTO>
            ) {
                val waitingTimes: MutableList<String> = mutableListOf()
                val data = response.body()
                val currentTime = Calendar.getInstance()
                var hour = currentTime.get(Calendar.HOUR_OF_DAY)//핸드폰 현재 시
                var minute = currentTime.get(Calendar.MINUTE) + plusTime// 현드폰 현재 분 + 앞에있는 경로 시간

                if (minute >= 60) {
                    hour += 1
                    minute -= 60
                }
                when (wayCode) {
                    1 -> {
                        data?.result?.OrdList?.up?.time?.forEach { time ->
                            if (time.Idx == hour || (time.Idx) - 1 == hour) {
                                val timeTable = time.list // 해당 시간에 맞는 지하철 시간표
                                Log.d("timeTable", timeTable)
                                val regex = Regex("\\d+\\([^)]+\\)")
                                waitingTimes.addAll(
                                    regex.findAll(timeTable)
                                        .mapNotNull { matchResult ->
                                            val timeString = matchResult.value // ex) "04(동두천)"
                                            val time =
                                                timeString.substringBefore('(').toInt() // 분 부분 추출
                                            if (minute < time) {
                                                timeString.substring(0, 2)
                                            } else {
                                                null
                                            }
                                        }
                                )
                            }
                        }
                        if (waitingTimes.isNotEmpty()) {
                            latestTime = if ((waitingTimes[0].toInt() - minute) >= 0) {
                                waitingTimes[0].toInt() - minute
                            } else {
                                minute + 60 - waitingTimes[0].toInt()
                            }
                            callback(latestTime)
                        } else {
                            callback(null)
                        }
                        Log.d("latestTime", latestTime.toString())
                    }


                    2 -> {
                        data?.result?.OrdList?.down?.time?.forEach { time ->
                            if (time.Idx == hour || (time.Idx) - 1 == hour) {
                                val timeTable = time.list // 해당 시간에 맞는 지하철 시간표
                                Log.d("timeTable", timeTable)
                                val regex = Regex("\\d+\\([^)]+\\)")
                                waitingTimes.addAll(
                                    regex.findAll(timeTable)
                                        .mapNotNull { matchResult ->
                                            val timeString = matchResult.value // ex) "04(동두천)"
                                            val time =
                                                timeString.substringBefore('(').toInt() // 분 부분 추출
                                            if (minute < time) {
                                                timeString.substring(0, 2)
                                            } else {
                                                null
                                            }
                                        }
                                )
                            }
                        }
                        if (waitingTimes.isNotEmpty()) {
                            latestTime = if ((waitingTimes[0].toInt() - minute) >= 0) {
                                waitingTimes[0].toInt() - minute
                            } else {
                                minute + 60 - waitingTimes[0].toInt()
                            }
                            callback(latestTime)
                        } else {
                            callback(null)
                        }
                        Log.d("latestTime", latestTime.toString())
                    }
                }
            }

            override fun onFailure(call: Call<stationTimeTableDTO>, t: Throwable) {
                Toast.makeText(requireContext(), "업데이트 실패", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        })
    }


    //주소를 위경도로 변환
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

    private fun initRecyclerView() {
        Log.d("실행", "실행7")
        //todo 자동차는 생각을 해봐야함
        info.forEach {
            if (it.waitTime == null && (it.trafficType == 1 || it.trafficType == 2)) {
                isFailed++
            }
            Log.d("실행", "실행8")
        }

        if (isFailed == 0) {
            val routeAdapter = RoteAdapter(info)
            binding.routeSearchResultRecyclerView.apply {
                adapter = routeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                val dividerItemDecoration =
                    DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                addItemDecoration(dividerItemDecoration)
            }
            Log.d("실행", "실행9")
            Toast.makeText(requireContext(), "리사이클러뷰 생성", Toast.LENGTH_SHORT).show()
        } else {
            Thread {
                getPublicTransitRouteSearchData(startX, startY, endX, endY)
                Log.d("실행", "실행10")
                handler.postDelayed({
                    val routeAdapter = RoteAdapter(info)
                    requireActivity().runOnUiThread {
                        binding.routeSearchResultRecyclerView.apply {
                            adapter = routeAdapter
                            layoutManager = LinearLayoutManager(requireContext())
                            val dividerItemDecoration =
                                DividerItemDecoration(
                                    requireContext(),
                                    LinearLayoutManager.VERTICAL
                                )
                            addItemDecoration(dividerItemDecoration)
                        }

                        Toast.makeText(requireContext(), "리사이클러뷰 생성", Toast.LENGTH_SHORT).show()
                        binding.totalTimeTextView.apply {
                            if (minTotalTime != null) {
                                text = "총 소요시간 : ${minTotalTime}분"
                                isVisible = true
                                Log.e("mintotalTimeinrecyclerView", minTotalTime.toString())
                                val hours = minTotalTime!! / 60
                                val minutes = minTotalTime!! % 60
                                val alarmTime = String.format("%02d:%02d", hours, minutes)
                                saveAlarmTime(alarmTime,"subway")
                            }
                        }
                    }
                }, 1500)
            }.start()
        }
    }

    private fun getRealTimeArrivalBus(stationId: Int, routeId: Int, callback: (Int?) -> Unit) {
        val retrofitApi =
            BusRealTimeConnection.getInstance().create(BusRealTimeAPIService::class.java)
        val call = retrofitApi.getBusRealTime(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            stationId
        )

        call.enqueue(object : Callback<RealTimeArrivalBus> {
            override fun onResponse(
                call: Call<RealTimeArrivalBus>,
                response: Response<RealTimeArrivalBus>
            ) {
                val data = response.body()
                // 여기에서 실시간 데이터를 추출할때 필요한 요소는 stationID는 이미있어서 괜찮고 routeID를 얻어야함 그래서 추가로 routeID를 얻어오는 작업을 할 예정
                try {
                    val latestTime =
                        data?.result?.real?.filter { it.localRouteId.toInt() == routeId }
                            ?.map { it.arrival1.arrivalSec }?.firstOrNull() //초임
                    val latestTimeMinute = latestTime?.div(60)
                    Log.d("busLatestTime", latestTimeMinute.toString())

                    callback(latestTimeMinute)
                } catch (e: NullPointerException) {
                    callback(null)
                }


            }

            override fun onFailure(call: Call<RealTimeArrivalBus>, t: Throwable) {
                Toast.makeText(context, "버스실시간 도착정보를 불러오지 못하였습니다", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        })
    }

    private fun getRouteId(busId: Int, callback: (Int?) -> Unit) {
        val retrofitApi =
            BusRealLocationConnection.getInstance().create(BusRealLocationAPIService::class.java)
        val call = retrofitApi.getRouteId(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            busId
        )

        call.enqueue(object : Callback<BusRealTimeLocationDTO> {
            override fun onResponse(
                call: Call<BusRealTimeLocationDTO>,
                response: Response<BusRealTimeLocationDTO>
            ) {
                //여기서는 busId를 넣으면은 routeID를 가져오는 작업을 진행
                val data = response.body()
                val routeId =
                    data?.result?.real?.filter { it.busId == busId.toString() }?.map { it.routeId }
                        ?.firstOrNull()
                Log.d("routeId", routeId.toString())

                callback(routeId?.toInt())
            }

            override fun onFailure(call: Call<BusRealTimeLocationDTO>, t: Throwable) {
                Toast.makeText(context, "버스RouteId를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        })
    }

    private fun getPublicTransitRouteSearchData(
        startX: Double,
        startY: Double,
        endX: Double,
        endY: Double,
    ) {
        val retrofitApi = PublicTransitRouteConnection.getInstance()
            .create(PublicTransitRouteSearchAPIService::class.java)
        val call = retrofitApi.getPublicTransitRoute(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            startX.toString(),
            startY.toString(),
            endX.toString(),
            endY.toString()
        )

        call.enqueue(object : Callback<PublicTransitRoute> {
            override fun onResponse(
                call: Call<PublicTransitRoute>,
                response: Response<PublicTransitRoute>
            ) {
                val data = response.body()
                val pathType = data?.result?.path?.get(1) // 1: 지하철, 2: 버스 3:버스+지하철
                val minTimePath = data?.result?.path?.minByOrNull { it.info.totalTime }
                val nextMinTimePath =
                    data?.result?.path?.filter { it.info.totalTime != minTimePath?.info?.totalTime }
                        ?.minByOrNull { it.info.totalTime } // 최단시간의 경로중 대중교통이 없을 경우 그다음으로 빠른 경로를 추천
                Log.d("minTimePath", minTimePath?.subPath.toString())
                Log.d("nextMinTimePath", nextMinTimePath?.subPath.toString())
                Log.d("실행", "실행3")
                var minSubPathList = mutableListOf<SubPath>()
                //최단시간이 안되면 그다음 최단시간을 불러온다
                if (isFailed == 0) {
                    minTotalTime = minTimePath?.info?.totalTime
                    minTimePath?.subPath?.forEach { subPath ->
                        if (subPath.sectionTime == 0 && subPath.trafficType == 3) {

                        } else {
                            minSubPathList.add(subPath)
                        }

                    }
                } else {
                    minTotalTime = nextMinTimePath?.info?.totalTime
                    nextMinTimePath?.subPath?.forEach { subPath ->
                        if (subPath.sectionTime == 0 && subPath.trafficType == 3) {

                        } else {
                            minSubPathList.add(subPath)
                        }
                    }
                }

                //info데이터 초기화
                info = mutableListOf()
                //비동기적이여서 쓰레드를 새로만듬
                Thread {
                    // 순차적으로 하기위해서 countdonwlatch사용
                    Log.d("실행", "실행4")
                    val countDownLatch = CountDownLatch(minSubPathList.size)
                    for (item in minSubPathList) {
                        val innerCountDownLatch = CountDownLatch(1)
                        trafficTypeCase(item) { data ->
                            info.add(data)
                            innerCountDownLatch.countDown()
                        }
                        innerCountDownLatch.await()
                        countDownLatch.countDown()
                    }
                    countDownLatch.await()
                    Log.d("실행", "실행5")
                    Log.d("info2", info.toString())

                    //맨처음 도착지점 같은 경우 두번째 리스트에 있는 것으로 설정
                    for (i in 0 until info.size) {
                        if (i > 0 && i < info.size - 1 && info[i].endName == null) {
                            info[i].endName = info[i + 1].startName
                        }
                        if (i > 0 && info[i].startName == null) {
                            info[i].startName = info[i - 1].endName
                        }
                    }
                    try { // info를 불러올수없는경우 거리가 가까울경우
                        info[0].startName = "출발지"
                        info[info.size - 1].endName = "도착지"
                        info[0].endName = info[1].startName
                        info[info.size - 1].startName = info[info.size - 2].endName
                        // 변경된 대기시간을 가져와야함
                        if (info.isNotEmpty()) {
                            var plusTime = 0
                            val countDownLatch2 = CountDownLatch(info.size)
                            for (data in info) {
                                plusTime += data.sectionTime!!
                                if (data.trafficType == 1 && data.waitTime != null) {
                                    getPublicTransportationData(
                                        data.subwayCode!!,
                                        data.wayCode!!,
                                        plusTime
                                    ) { time ->
                                        data.waitTime = time
                                        //최단시간에 버스및 지하철대기시간 추가해주기
                                        minTotalTime = time?.let { minTotalTime?.plus(it) }

                                    }
                                }
                                countDownLatch2.countDown()
                            }
                            countDownLatch2.await()
                            //만약 info데이터안에 지하철이나 버스의 현재 운영중인 경우가 없을 경우 그 다음 경로를 추천
                            Log.d("infoaaa", "$info")
                            Log.d("실행", "실행6")
                        }
                    } catch (e: IndexOutOfBoundsException) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "거리가 너무 가깝습니다(700m이내)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.d("실행", "실행6")
                    }
                }.start()

                //todo pathListSize만큼 반복문을 돌면서 만약 대기시간이 null이 아닌값이 나온다면 그 경로를 알려주고 다 null이라면 이용가능한 대중교통이 없다고 알려주기
            }

            override fun onFailure(call: Call<PublicTransitRoute>, t: Throwable) {
                Toast.makeText(requireContext(), "길찾기 경로를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }

        })
    }


    private fun trafficTypeCase(subPath: SubPath, callback: (Info) -> Unit) {
        val trafficType = subPath.trafficType //1-지하철, 2-버스, 3-도보
        var startName: String? = null
        var endName: String? = null
        var sectionTime: Int?
        var lane: Int? = null
        var busno: String? = null
        var subwayCode: String? = null // 지하철 코드
        var wayCode: Int? = null // 1.상행 2. 하행
        var waitTime: Int? = null
        var busId: Int? = null
        when (trafficType) {
            //지하철일때
            1 -> {
                startName = subPath.startName
                endName = subPath.endName
                sectionTime = subPath.sectionTime
                lane = subPath.lane.map { it.subwayCode }.firstOrNull()
                subwayCode = subPath.startID.toString()
                wayCode = subPath.wayCode
                Log.d("subwaydata", "$subwayCode, $wayCode")
                getPublicTransportationData(subwayCode, wayCode) { time ->
                    waitTime = time
                    val info = Info(
                        trafficType,
                        startName,
                        endName,
                        sectionTime,
                        lane,
                        busno,
                        subwayCode,
                        wayCode,
                        waitTime,
                        busId
                    )
                    Log.d("subwayinfo", info.toString())
                    callback(info)
                    //지금 콜백이 실행이 안되고있다
                }

            }
            //버스일때
            2 -> {
                startName = subPath.startName
                endName = subPath.endName
                busno = subPath.lane.map { it.busNo }.firstOrNull()
                sectionTime = subPath.sectionTime
                subwayCode = subPath.startID.toString()
                wayCode = subPath.wayCode
                busId = subPath.lane.map { it.busID }.firstOrNull()
                Log.d("busid", busId.toString())
                Log.d("startId", subwayCode)
                // busID를 입력하게 되면 routeID를 얻게 된다.
                getRouteId(busId!!) { routeId ->
                    if (routeId != null) {
                        getRealTimeArrivalBus(
                            subwayCode.toInt(),
                            routeId
                        ) { time -> //얻은 루트아이디와 busID를 활용해서 하면된다
                            waitTime = time
                            val info = Info(
                                trafficType,
                                startName,
                                endName,
                                sectionTime,
                                lane,
                                busno,
                                subwayCode,
                                wayCode,
                                waitTime,
                                busId
                            )
                            callback(info)
                            Log.d("businfo", info.toString())
                        }
                    } else {
                        val info = Info(
                            trafficType,
                            startName,
                            endName,
                            sectionTime,
                            lane,
                            busno,
                            subwayCode,
                            wayCode,
                            waitTime,
                            busId
                        )
                        callback(info)
                        Log.d("businfo", info.toString())
                    }
                }
            }
            //도보일때
            3 -> {
                sectionTime = subPath.sectionTime
                val info = Info(
                    trafficType,
                    startName,
                    endName,
                    sectionTime,
                    lane,
                    busno,
                    subwayCode,
                    wayCode,
                    waitTime,
                    busId
                )
                callback(info)
                Log.d("walkinfo", info.toString())//그다음 이쪽이 2번째로 작동
            }
        }
    }

    data class Info(
        val trafficType: Int,
        var startName: String?,
        var endName: String?,
        val sectionTime: Int?,
        val lane: Int?,
        val busno: String?,
        val subwayCode: String?,
        val wayCode: Int?,
        var waitTime: Int?,
        var busId: Int?,
    )


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditMappingFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditMappingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    //spiner item 골랏을 때 콜백
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, p2: Int, p3: Long) {
        plusTime = parent?.getItemAtPosition(p2).toString().replace("분","")
        Log.e("plusTime","$plusTime")
        with(requireActivity().getSharedPreferences(KEY_ALARMTIME, Context.MODE_PRIVATE).edit()) {
            putString(PLUS_TIME,plusTime)
            apply()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        plusTime = ""
        Log.e("plusTime","$plusTime")
    }
}