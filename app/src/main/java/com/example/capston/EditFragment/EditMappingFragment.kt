package com.example.capston.EditFragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.capston.SearchActivity
import com.example.capston.databinding.FragmentEditMappingBinding
import com.example.capston.retrofit.PublicTransitRoute
import com.example.capston.retrofit.SubPath
import com.example.capston.route.PublicTransitRoteConnection
import com.example.capston.route.PublicTransitRoteSearchAPIService
import com.example.capston.route.RoteAdapter
import com.example.capston.subway.SubwayTimeTableConnection
import com.example.capston.subway.SubwayTimeTableService
import com.example.retrofit_example.retrofit2.stationTimeTableDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.CountDownLatch


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditMappingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditMappingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEditMappingBinding
    private var minTotalTime: Int? = null
    private var routeCount: Int? = null
    private var info = mutableListOf<Info>()
    private val g by lazy { android.location.Geocoder(requireContext(), Locale.KOREAN) } //geocoder
    private var locationList = Array(4) { 0.0 }
    private var latestTime: Int? = null
    private var isLoad = true

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
        //spinner adapter
        binding.trackingTimeSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.trackingTimes,
            android.R.layout.simple_list_item_1
        )
        binding.searchlayer.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("startAddress", binding.startValueTextView.text.toString())
            intent.putExtra("arrivalAddress", binding.arrivalValueTextView.text.toString())
            startActivity(intent)
        }

        // searchActivity에서 출발도착장소 업데이트
        binding.startValueTextView.text = param1
        binding.arrivalValueTextView.text = param2
        //장소검색 버튼
        binding.searchButton.setOnClickListener {
            val startAdress = binding.startValueTextView.text.toString()
            val arrivalAdress = binding.arrivalValueTextView.text.toString()
            geocoder(startAdress) { lat, lng ->
                if (lat == null || lng == null) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "주소를 입력해주세요", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    locationList[0] = lng
                    locationList[1] = lat
                    geocoder(arrivalAdress) { lat, lng ->
                        locationList[2] = lng!!
                        locationList[3] = lat!!
                        val startX = locationList[0]
                        val startY = locationList[1]
                        val endX = locationList[2]
                        val endY = locationList[3]
                        requireActivity().runOnUiThread {
                            getPublicTransitRouteSearchData(startX, startY, endX, endY)
                            val handler = Handler()
                            handler.postDelayed({
                                initRecyclerView()
                                if (isLoad) {
                                    binding.totalTimeTextView.apply {
                                        if (minTotalTime == null) {
                                            text = "현재이용가능한 대중교통 없음"
                                        } else {
                                            text = "총 소요시간 : ${minTotalTime}분"
                                        }
                                        isVisible = true
                                    }
                                }
                            }, 1500)
                        }
                    }
                }
            }
        }

        return binding.root
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
                        }
                    }

                    2 -> {
                        data?.result?.OrdList?.down?.time?.forEach { time ->
                            if (time.Idx == hour) {
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
                        }
                    }

                    else -> {
                        callback(null)
                    }
                }

                Log.d("latestTime", latestTime.toString())

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
        val routeAdapter = RoteAdapter(info)
        Log.d("info", "$info")
        Log.d("info", "${info.size}")
        if (routeCount != info.size) errorCallback() //대중교통 길찾기 경로가 다를 경우 error처리 todo 자동차는 생각을 해봐야함
        Log.d("errorCallback", isLoad.toString())
        if (isLoad) {
            binding.routeSearchResultRecyclerView.apply {
                adapter = routeAdapter
                layoutManager = LinearLayoutManager(requireContext())
                val dividerItemDecoration =
                    DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
                addItemDecoration(dividerItemDecoration)
            }
            Toast.makeText(requireContext(), "리사이클러뷰 생성", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "현재이용가능한 대중교통은 없습니다.", Toast.LENGTH_SHORT).show()
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
                //todo 여기에서 실시간 데이터를 추출할때 필요한 요소는 stationID는 이미있어서 괜찮고 routeID를 얻어야함 그래서 추가로 routeID를 얻어오는 작업을 할 예정
                val latestTime = data?.result?.real?.filter { it.localRouteId.toInt() == routeId }
                    ?.map { it.arrival1.arrivalSec }?.firstOrNull() //초임
                val latestTimeMinute = latestTime?.div(60)
                Log.d("busLatestTime", latestTimeMinute.toString())
                callback(latestTimeMinute)

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
                //todo 여기서는 busId를 넣으면은 routeID를 가져오는 작업을 진행해야한다
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
        val retrofitApi = PublicTransitRoteConnection.getInstance()
            .create(PublicTransitRoteSearchAPIService::class.java)
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
                minTotalTime = minTimePath?.info?.totalTime
                val minTotalWalk = minTimePath?.info?.totalWalk
                val minSubPathList = mutableListOf<SubPath>()
                minTimePath?.subPath?.forEach { subPath ->
                    if (subPath.sectionTime != 0) minSubPathList.add(subPath)
                    if (subPath.trafficType == 1) {
                        subPath.endName += "역"
                        subPath.startName += "역"
                    }
                }
                routeCount = minSubPathList.size
                Log.d("minSubPathList", minSubPathList.size.toString())
                //info데이터 초기화
                info = mutableListOf()
                //비동기적이여서 쓰레드를 새로만듬
                Thread {
                    // 순차적으로 하기위해서 countdonwlatch사용
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

                    //맨처음 도착지점 같은 경우 두번째 리스트에 있는 것으로 설정
                    for (i in 0 until info.size) {
                        if (i > 0 && i < info.size - 1 && info[i].endName == null) {
                            info[i].endName = info[i + 1].startName
                        }
                        if (i > 0 && info[i].startName == null) {
                            info[i].startName = info[i - 1].endName
                        }
                    }
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

                                }
                                countDownLatch2.countDown()
                            }
                        }
                        countDownLatch2.await()
                    }


                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "길찾기 경로 로딩완료", Toast.LENGTH_SHORT).show()
                    }
                }.start()


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
                Log.d("subwayinfo1", info.toString())
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
                    callback(info)
                    Log.d("subwayinfo", info.toString())
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
                Log.d("businfo1", info.toString())
                if (busId != null) { // routeID를 가져와서 그 routeId와 busid하고 결합해서 실시간 도착정보를 받아온다
                    getRouteId(busId) { routeId ->
                        if (routeId != null) {
                            getRealTimeArrivalBus(subwayCode.toInt(), routeId) { time ->
                                if (time != null) {
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
                            }
                        }
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
                Log.d("walkinfo", info.toString())
            }
        }
    }

    private fun errorCallback() {
        isLoad = false
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
}