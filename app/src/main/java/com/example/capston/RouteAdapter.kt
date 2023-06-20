package com.example.capston

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.EditFragment.EditMappingFragment
import com.example.capston.databinding.FragmentEditMappingBinding
import com.example.capston.databinding.ItemRouteBinding

class RouteAdapter(
    private val list: MutableList<EditMappingFragment.Info>
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemRouteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RouteViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val info = list.get(position)
        holder.bind(info)
    }

    class RouteViewHolder(val binding: ItemRouteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(info: EditMappingFragment.Info) {
            binding.apply {
                when (info.trafficType) {
                    1 -> {
                        trafficTypeTextView.text = "지하철"
                        startAreaTextView.text = info.startName
                        arrivalAreaTextView.text = info.endName
                        sectionTimeTextView.text = info.sectionTime.toString() + "분"
                    }

                    2 -> {
                        trafficTypeTextView.text = "버스"
                        startAreaTextView.text = info.startName
                        arrivalAreaTextView.text = info.endName
                        sectionTimeTextView.text = info.sectionTime.toString() + "분"
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