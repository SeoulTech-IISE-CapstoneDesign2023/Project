package com.example.capston.Friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.User
import com.example.capston.databinding.MyfriendsViewBinding

class FriendListAdapter(
    private val context: FriendListActivity,
    private val myFriend: MutableList<User>,
    private val itemLongClicklistener: OnItemLongClickListener
) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendListAdapter.ViewHolder {
        val binding =
            MyfriendsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, context)
    }

    override fun onBindViewHolder(
        holder: FriendListAdapter.ViewHolder, position: Int
    ) {

        val friend = myFriend.get(position)
        holder.bind(friend)

        holder.binding.root.setOnLongClickListener {
            itemLongClicklistener.onLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return myFriend.size
    }

    class ViewHolder(val binding: MyfriendsViewBinding, val context: FriendListActivity) :
        RecyclerView
        .ViewHolder(binding.root) {
        fun bind(friend: User) {
            binding.friendName.text = friend.nickname

        }
    }
}