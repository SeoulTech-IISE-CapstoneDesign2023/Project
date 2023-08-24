package com.example.capston.EditFragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEditTodoBinding
    private lateinit var previousPlace: String
    private lateinit var changedPlace: String
    private lateinit var previousMemo: String
    private lateinit var changedMemo: String

    // data를 전달하는 listener
    interface OnDataPassListener {
        //data를 전달하는 listener
        fun onDataPass(data: Int?)
        fun onMemoPass(memo: String?)
        fun onPlacePass(place: String?)
    }

    private lateinit var dataPassListener: OnDataPassListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPassListener = context as OnDataPassListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)//data를 전달
        val todo = requireActivity().intent.getParcelableExtra<Todo>("todo")
        // 기존 일정의 장소와 메모
        if (todo != null) {
            previousPlace = todo?.place.toString()
            previousMemo = todo?.memo.toString()
            // 장소와 메모가 변경 없이 그대로 일 경우 기존 데이터 유지
            dataPassListener.onPlacePass(previousPlace)
            dataPassListener.onMemoPass(previousMemo)
            Log.i("DataPass", "이전 장소 안 바뀜: $previousPlace")
            Log.i("DataPass", "이전 메모 안 바뀜: $previousMemo")
        }
        previousPlace = null.toString()
        previousMemo = null.toString()
        // 장소 변경을 시도할 경우
        binding.placeEditTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                changedPlace = s.toString()
                if (changedPlace != previousPlace) {
                    // 텍스트가 변경되었을 때
                    dataPassListener.onPlacePass(changedPlace)
                    Log.d("DataPass", "장소가 변경되었습니다: $previousPlace -> $changedPlace")
                }
                if (changedPlace.isEmpty()) {
                    // 텍스트가 입력창에 아무 것도 입력되지 않았을 때
                    Log.i("DataPass", "장소에 입력된 내용이 없습니다")
                }
            }
        })

        // 메모 변경을 시도할 경우
        binding.memoEditTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                changedMemo = s.toString()
                if (changedMemo != previousMemo) {
                    // 텍스트가 변경되었을 때
                    dataPassListener.onMemoPass(changedMemo)
                    Log.d("DataPass", "메모가 변경되었습니다: $previousMemo -> $changedMemo")
                }
                if (changedMemo.isEmpty()) {
                    // 텍스트가 입력창에 아무 것도 입력되지 않았을 때
                    Log.i("DataPass", "메모에 입력된 내용이 없습니다")
                }
            }
        })
        // 메모장 텍스트 길이 받기
        binding.memoEditTextView.addTextChangedListener { text ->
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
            Log.d("DataPass", "place is :${todo.place}")
            binding.placeEditTextView.setText(todo.place)
            binding.memoEditTextView.setText(todo.memo)
        } else {
            // 새로운 Todo를 생성하는 경우, 화면을 초기화
            binding.placeEditTextView.setText("")
            binding.memoEditTextView.setText("")
        }
        // 메모장 글자수가 100자가 넘어가면 error 표시
        binding.memoEditTextView.addTextChangedListener {
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