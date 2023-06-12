package com.example.Timetable

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.Timetable.databinding.ActivityFriendListBinding

class FriendListActivity : AppCompatActivity() {

    val binding by lazy { ActivityFriendListBinding.inflate(layoutInflater) }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        binding.friendListToolbar.inflateMenu(R.menu.friend_list_menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addFriends -> {
                val intent = Intent(this@FriendListActivity, addFriendsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.friendListToolbar)
    }
}