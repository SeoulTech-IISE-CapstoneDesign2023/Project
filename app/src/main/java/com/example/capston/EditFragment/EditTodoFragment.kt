package com.example.capston.EditFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.capston.Todo
import com.example.capston.databinding.FragmentEditTodoBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EditTodoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEditTodoBinding

    interface OnDataPassListener {//data를 전달하는 listener
        fun onDataPass(data:Int?)
        fun onMemoPass(memo: String)
        fun onPlacePass(place: String)
    }
    private lateinit var dataPassListener : OnDataPassListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as OnDataPassListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)//data를 전달
        binding.placeEditTextView.addTextChangedListener { text ->
            dataPassListener.onPlacePass(text.toString())
        }
        binding.textInputEditText.addTextChangedListener {text->
            dataPassListener.onMemoPass(text.toString())
        }
        binding.textInputEditText.addTextChangedListener {text->
            dataPassListener.onDataPass(text?.length)
        }
    }

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
        binding = FragmentEditTodoBinding.inflate(inflater, container, false)
        //list에서 일정 하나 선택했을 때 내용 수정
        val todo = requireActivity().intent.getParcelableExtra<Todo>("todo")
        if (todo != null) {
            // 기존의 Todo를 수정하는 경우, Todo객체를 사용하여 화면을 초기화
            Log.d("DataPass","place is :${todo.place}")
            binding.placeEditTextView.setText(todo.place)
            binding.textInputEditText.setText(todo.memo)
        } else {
            // 새로운 Todo를 생성하는 경우, 화면을 초기화
            binding.placeEditTextView.setText("")
            binding.textInputEditText.setText("")
        }
        binding.textInputEditText.addTextChangedListener {
            it?.let { text ->
                binding.textTextInputLayout.error = if (text.length > 100) {
                    "글자수를 초과하였습니다."
                } else null
            }
        }
        return binding.root
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditTodoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}