package com.example.capston.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.databinding.ItemRouteBinding

class RoteAdapter(
    private val list: MutableList<EditMappingFragment.Info>
) : RecyclerView.Adapter<RoteAdapter.RouteViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RouteViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val info = list[position]
        holder.bind(info)
    }

    class RouteViewHolder(val binding: ItemRouteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(info: EditMappingFragment.Info) {
            val laneText = when (info.lane) {
                in 1..9 -> info.lane.toString()
                101 -> "공항철도"
                102 -> "자기부상철도"
                104 -> "경의중앙선"
                107 -> "에버라인"
                108 -> "경춘선"
                109 -> "신분당선"
                110 -> "의정부경전철"
                112 -> "경강선"
                113 -> "우이신설선"
                114 -> "서해선"
                115 -> "김포골드라인"
                116 -> "수인분당선"
                117 -> "신림선"
                21 -> "인천 1호선"
                22 -> "인천 2호선"
                else -> null
            }
            binding.apply {
                when (info.trafficType) {
                    1 -> {
                        trafficTypeTextView.text = "지하철"
                        startAreaTextView.text = info.startName
                        arrivalAreaTextView.text = info.endName
                        sectionTimeTextView.text = info.sectionTime.toString() + "분"
                        //지하철 호선 추가
                        laneText?.let {
                            detailTypeTextView.text = if (info.lane in 1..9) {
                                "${it}호선"
                            } else {
                                it
                            }
                        }

                        detailTypeTextView.isVisible = true
                        waitingTimeTextView.isVisible = true
                        if (info.waitTime != null) {
                            waitingTimeTextView.text = "대기시간 ${info.waitTime}분"
                        } else if (info.waitTime == 0) {
                            waitingTimeTextView.text = "곧도착"
                        } else {
                            waitingTimeTextView.text = "운행정보 없음"
                        }

                    }

                    2 -> {
                        trafficTypeTextView.text = "버스"
                        startAreaTextView.text = info.startName
                        arrivalAreaTextView.text = info.endName
                        sectionTimeTextView.text = info.sectionTime.toString() + "분"
                        detailTypeTextView.text = info.busno
                        detailTypeTextView.isVisible = true
                        waitingTimeTextView.isVisible = true
                        if (info.waitTime == 0) {
                            waitingTimeTextView.text = "곧도착"
                        } else if (info.waitTime != null) {
                            waitingTimeTextView.text = "대기시간 ${info.waitTime}분"
                        } else {
                            waitingTimeTextView.text = "운행정보 없음"
                        }
                    }

                    3 -> {
                        if (info.startName == info.endName) trafficTypeTextView.text =
                            "환승" else trafficTypeTextView.text = "도보"
                        sectionTimeTextView.text = info.sectionTime.toString() + "분"
                        startAreaTextView.text = info.startName
                        arrivalAreaTextView.text = info.endName
                    }
                }
            }
        }

    }
}