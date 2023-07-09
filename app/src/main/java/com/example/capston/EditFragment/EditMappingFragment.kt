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
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.*
import com.example.capston.databinding.FragmentEditMappingBinding
import com.example.capston.retrofit.PublicTransitRoute
import com.example.capston.retrofit.SubPath
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EditMappingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEditMappingBinding
    private var minTotalTime: Int? = null
    private var info = mutableListOf<Info>()
    private val g by lazy { android.location.Geocoder(requireContext(), Locale.KOREAN) } //geocoder
    private var locationList = Array<Double>(4, { 0.0 })

    interface OnDataPassListener {//data를 전달하는 listener
    fun onStartPass(startPlace: String)
        fun onArrivePass(arrivePlace: String)
    }
    private lateinit var dataPassListener : EditTodoFragment.OnDataPassListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as EditTodoFragment.OnDataPassListener
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
    ): View? {
        binding = FragmentEditMappingBinding.inflate(inflater, container, false)
        //list에서 일정 하나 선택했을 때 내용 수정
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
        binding.searchlayer.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("startAddress", binding.startValueTextView.text.toString())
            intent.putExtra("arrivalAddress", binding.arrivalValueTextView.text.toString())
            startActivity(intent)
        }
        //recylcerView adapter

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
                        Toast.makeText(requireContext(), "주소를 입력해주세요", Toast.LENGTH_SHORT).show()
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
                                binding.totalTimeTextView.apply {
                                    setText("총 소요시간 : ${minTotalTime}분")
                                    isVisible = true
                                }
                                initRecyclerView()
                            }, 1000)
                        }
                    }
                }
            }

        }

        return binding.root
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
        val routeAdapter = RouteAdapter(info)
        Log.d("info", "$info")
        binding.routeSearchResultRecyclerView.apply {
            adapter = routeAdapter
            layoutManager = LinearLayoutManager(requireContext())
            val dividerItemDecoration =
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }
        Toast.makeText(requireContext(), "리사이클러뷰 생성", Toast.LENGTH_SHORT).show()
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
                //info데이터 초기화
                info = mutableListOf<Info>()
                minSubPathList.forEach {
                    info.add(trafficTypeCase(it))
                }
                Log.d("info", "$info")
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
                Toast.makeText(requireContext(), "길찾기 경로 로딩완료", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<PublicTransitRoute>, t: Throwable) {
                Toast.makeText(requireContext(), "길찾기 경로를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun trafficTypeCase(subPath: SubPath): Info {
        val trafficType = subPath.trafficType //1-지하철, 2-버스, 3-도보
        var startName: String? = null
        var endName: String? = null
        var sectionTime: Int? = null
        var lane: Int? = null
        var busno: String? = null
        when (trafficType) {
            //지하철일때
            1 -> {
                startName = subPath.startName
                endName = subPath.endName
                sectionTime = subPath.sectionTime
                lane = subPath.lane.map { it.subwayCode }.firstOrNull()
            }
            //버스일때
            2 -> {
                startName = subPath.startName
                endName = subPath.endName
                busno = subPath.lane.map { it.busNo }.firstOrNull()
                sectionTime = subPath.sectionTime
            }
            //도보일때
            3 -> {
                sectionTime = subPath.sectionTime
            }
        }
        return Info(trafficType, startName, endName, sectionTime, lane, busno)
    }

    data class Info(
        val trafficType: Int,
        var startName: String?,
        var endName: String?,
        val sectionTime: Int?,
        val lane: Int?,
        val busno: String?,
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