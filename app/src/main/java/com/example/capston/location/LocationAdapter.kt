package com.example.capston.location

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.databinding.ItemLocationBinding

class LocationAdapter(private val onClick: (Poi) -> Unit) :
    ListAdapter<Poi, LocationAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val viewBinding: ItemLocationBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: Poi) {
            viewBinding.nameTextView.text = item.name
            val rawFullAdressRoad =
                item.newAddressList.newAddress.map { it.fullAddressRoad }.toString()
            val fullAdressRoad = rawFullAdressRoad.replace("[", "").replace("]", "")
            viewBinding.fullAddressRoadTextView.text = fullAdressRoad
            viewBinding.root.setOnClickListener {
                val x = item.frontLat
                val y = item.frontLon
                Log.e("위경도", "$x $y")
                onClick(item)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLocationBinding.inflate(
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
        val diffUtil = object : DiffUtil.ItemCallback<Poi>() {
            override fun areItemsTheSame(oldItem: Poi, newItem: Poi): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Poi, newItem: Poi): Boolean {
                return oldItem == newItem
            }

        }
    }


}