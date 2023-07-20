package com.example.capston.car

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.databinding.ItemCarBinding
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CarAdapter : ListAdapter<Properties, CarAdapter.ViewHolder>(diffUtil) {
    val df = DecimalFormat("###,###")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

    inner class ViewHolder(private val viewBinding: ItemCarBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: Properties) {
            val departure = parseDateTime(item.departureTime)
            val arrival = parseDateTime(item.arrivalTime)
            val totalTime = formatTotalTime(item.totalTime)
            viewBinding.totalTimeTextView.text = totalTime
            viewBinding.totalFairTextView.text =
                if (item.totalFare > 0) df.format(item.totalFare) else "통행요금없음"
            viewBinding.taxiFareTextView.text = df.format(item.taxiFare) + "원"
            viewBinding.departureTimeTextView.text = departure
            viewBinding.arrivalTimeTextView.text = arrival
        }

    }

    private fun parseDateTime(dateTime: String): String {
        val zonedDateTime = ZonedDateTime.parse(dateTime, formatter)
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
        return zonedDateTime.format(DateTimeFormatter.ofPattern("MM월 dd일 HH시 mm분"))
    }

    private fun formatTotalTime(totalTime: Int): String {
        val hours = (totalTime / 60) / 60
        val minutes = (totalTime / 60) % 60
        return if (hours != 0) "약 $hours 시간 $minutes 분" else "약 $minutes 분"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCarBinding.inflate(
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
                return oldItem.departureTime == newItem.departureTime
            }

            override fun areContentsTheSame(oldItem: Properties, newItem: Properties): Boolean {
                return oldItem == newItem
            }

        }
    }


}