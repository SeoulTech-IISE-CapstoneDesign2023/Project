package com.example.capston.walk

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.databinding.ItemWalkBinding
import java.text.DecimalFormat

class WalkAdapter :
    androidx.recyclerview.widget.ListAdapter<Properties, WalkAdapter.ViewHolder>(diffUtil) {
    val df = DecimalFormat("###,###")
    inner class ViewHolder(private val viewBinding: ItemWalkBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item:Properties) {
            val totalTime = formatTotalTime(item.totalTime)
            viewBinding.totalTimeTextView.text = totalTime
            viewBinding.totalDistanceTextView.text = df.format(item.totalDistance) + "m"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWalkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Properties>() {
            override fun areItemsTheSame(oldItem: Properties, newItem: Properties): Boolean {
                return oldItem.index == newItem.index
            }

            override fun areContentsTheSame(oldItem: Properties, newItem: Properties): Boolean {
                return oldItem == newItem
            }

        }
    }

    private fun formatTotalTime(totalTime: Int): String {
        val hours = (totalTime / 60) / 60
        val minutes = (totalTime / 60) % 60
        return if (hours != 0) "약 $hours 시간 $minutes 분" else "약 $minutes 분"
    }


}