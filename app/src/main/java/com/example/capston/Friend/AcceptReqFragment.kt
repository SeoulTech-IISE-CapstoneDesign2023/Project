package com.example.capston.Friend

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capston.R
import com.example.capston.User
import com.example.capston.databinding.FragmentAcceptReqBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AcceptReqFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AcceptReqFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentAcceptReqBinding
    var acceptReqList = mutableListOf<User>()
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
        // Inflate the layout for this fragment
        binding = FragmentAcceptReqBinding.inflate(inflater, container, false)

        val database = Firebase.database
        val auth = FirebaseAuth.getInstance()
        val userRef = database.getReference("user")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                this@AcceptReqFragment.acceptReqList = mutableListOf<User>()

                val reqSnapshot = snapshot.child(auth.uid.toString()).child("friend_info")
                    .child("accept_fr_req")
                for (userSnapshot in reqSnapshot.children) {

                    val uid = userSnapshot.key
                    val nickname = snapshot.child(uid.toString()).child("user_info")
                        .child("nickname").value.toString()

                    val userData = User(uid, nickname)
                    acceptReqList.add(userData)
                }
                if (acceptReqList.size == reqSnapshot.childrenCount.toInt()) {
                    updateRecyclerView(acceptReqList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("geon", "Failed to read friend data.", error.toException())
            }
        })


        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AcceptReqFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AcceptReqFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun updateRecyclerView(dataList: List<User>) {
        val adapter = AcceptReqAdapter(dataList as MutableList<User>)
        binding.acceptReqRecyclerView.adapter = adapter
        binding.acceptReqRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)

        adapter.notifyDataSetChanged()
    }
}