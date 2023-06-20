package com.example.capston

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.capston.databinding.ItemTodoBinding

class TodoListAdapter(private val todoList: ArrayList<Todo>, private val listener : OnItemLongClickListener) :
    RecyclerView.Adapter<TodoListAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListAdapter.ViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoListAdapter.ViewHolder, position: Int) {
        val todoEntity = todoList[position]
        holder.setTodoListUI(todoEntity,position)

        // 일정이 길게 클릭되었을 때 리스너 함수 실행
        holder.root.setOnLongClickListener {
            listener.onLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    inner class ViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root){
        val root = binding.root
        fun setTodoListUI(todo: Todo, position: Int){
            binding.todotitle.text = todo.title
            binding.todoTime.text = todo.st_time
        }
    }
}