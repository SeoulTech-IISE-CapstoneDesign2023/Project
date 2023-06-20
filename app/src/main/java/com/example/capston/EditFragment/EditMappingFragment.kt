package com.example.capston.EditFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.capston.PublicTransitRouteConnection
import com.example.capston.PublicTransitRouteSearchAPIService
import com.example.capston.R
import com.example.capston.SearchActivity
import com.example.capston.databinding.FragmentEditMappingBinding
import com.example.capston.retrofit.PublicTransitRoute
import com.example.capston.retrofit.SubPath
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private var info = mutableListOf<Info>()

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
        //spinner adapter
        binding.trackingTimeSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.trackingTimes,
            android.R.layout.simple_list_item_1
        )
        binding.searchlayer.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
        // searchActivity에서 출발도착장소 업데이트
        binding.startValueTextView.text = param1
        binding.arrivalValueTextView.text = param2
        //장소검색 버튼
        binding.searchButton.setOnClickListener {
            // 출발 주소와 도착 주소의 위경도를 가져오기
            // 이때 길찾기 경로가 대중교통일시 지하철 + 버스로 가져왔음
            // 그 주소를 가지고 길찾기 경로 파악
            val startX = 127.077472
            val startY = 37.631728
            val endX = 127.034667
            val endY = 37.507705
            getPublicTransitRouteSearchData(startX, startY, endX, endY)
        }

        return binding.root
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
                val minTotalTime = minTimePath?.info?.totalTime
                val minTotalWalk = minTimePath?.info?.totalWalk
                val minSubPathList = mutableListOf<SubPath>()
                minTimePath?.subPath?.forEach { subPath ->
                    minSubPathList.add(subPath)
                }
                val info1 = trafficTypeCase(minSubPathList[0])
                minSubPathList.forEach {
                    info.add(trafficTypeCase(it))
                }
                //지금 각 케이스에 대해서 정보를 가져오는 것은 성공 그러면 이제 ui를 업데이트해야함
                //recyclerView를 활용
                Log.d("data", info1.toString())
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
        val startName: String?,
        val endName: String?,
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