package com.example.capston.Friend

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capston.R
import com.example.capston.databinding.ActivityManageFriendBinding
import com.google.android.material.tabs.TabLayoutMediator

class ManageFriendActivity : AppCompatActivity() {

    val binding by lazy { ActivityManageFriendBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 프레그먼트 페이지 설정
        val acceptReqFragment = AcceptReqFragment()
        val sendReqFragment = SendReqFragment()

        val myFrags = listOf(
            acceptReqFragment, sendReqFragment
        )

        // ViewPager 설정 (연결시키기)
        val fragAdapter = FriendFragAdapter(this)
        fragAdapter.fragList = myFrags

        binding.friendManageVP.adapter = fragAdapter

        // 탭 레이아웃 설정
        val tabs = listOf("받은 요청", "보낸 요청")
        TabLayoutMediator(binding.tabLayout, binding.friendManageVP) { tab, position ->
            tab.text = tabs.get(position)
        }.attach()
    }
}