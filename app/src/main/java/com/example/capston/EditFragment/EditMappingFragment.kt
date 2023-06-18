package com.example.capston.EditFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
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
        binding.searchButton.setOnClickListener{
            // 출발 주소와 도착 주소의 위경도를 가져오기
            // 이때 길찾기 경로가 대중교통일시 지하철 + 버스로 가져왔음
            // 그 주소를 가지고 길찾기 경로 파악
            val startX = 127.077472
            val startY = 37.631728
            val endX = 127.034667
            val endY = 37.507705
            val resutlTextView = binding.roadSearchValueTextView
            getPublicTransitRouteSearchData(startX,startY,endX, endY,resutlTextView)
        }

        return binding.root
    }

    private fun getPublicTransitRouteSearchData(
        startX: Double,
        startY: Double,
        endX: Double,
        endY: Double,
        resultTextView: TextView
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
                val minSubPath = minTimePath?.subPath
                // 해야할것은 1-지하철, 2-버스, 3-도보 이게 traffic Type임 그래서 이거에 해당되게 경로를 가져와야함
                // subpath에 있는 것들을 리스트로 가져와서 각 traffic type에 맞게 case를 부여하면 됨
                // 지하철일 경우 startName을 통해 출발역을 가져오고 lane에 있는 subwaycode로 몇호선인지 받아오고 endName을 통해 도착역을 가져옴 sectiontime으로 소요시간 가져옴
                // 여기서 추가로 생각해야할것은 환승인 경우도 고려해야함 우선은 환승이 없을 때로 해보고 나중에 추가할예정
                // 버스일 경우 sectionTime으로 소요시간 가져오고 busno로 버스 번호를 가져옴 startName과 endName으로 출발 도착 정류장을 가져옴
                // 도보일 경우 제일 간단함 sectinoTime으로 시간만 가져오면 됨
                // 여기서 추가로 뭔가 가능성있는 것을 찾아냄 html로 이런 경로에 대한 길찾기를 보여줄 수 있음
                // 그래서 webView를 사용해서 경로를 지도에다가 대강 보여줄수있을 거 같음
                resultTextView.text = minSubPath.toString()
                Log.d("data", minSubPath.toString())
                Toast.makeText(requireContext(), "길찾기 경로 로딩완료", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<PublicTransitRoute>, t: Throwable) {
                Toast.makeText(requireContext(), "길찾기 경로를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }

        })
    }


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