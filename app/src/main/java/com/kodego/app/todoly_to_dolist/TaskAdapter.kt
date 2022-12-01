package com.kodego.app.todoly_to_dolist

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kodego.app.todoly_to_dolist.databinding.RowTaskBinding
import com.kodego.app.todoly_to_dolist.db.Task

class TaskAdapter(var taskModel: MutableList<Task>): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    var onTaskClick: ((Task, Int) -> Unit) ? = null
    var onTaskDelete: ((Task, Int) -> Unit) ? = null
    var onTaskCheckBoxClicked: ((Task, Int) -> Unit) ? = null

    inner class TaskViewHolder(var binding: RowTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RowTaskBinding.inflate(layoutInflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.binding.apply {
            tvTaskTitle.text = taskModel[position].taskTitle
            tvDescription.text = taskModel[position].description
            if(taskModel[position].isTaskDone){
                cbTaskDone.isChecked = taskModel[position].isTaskDone
                cbTaskDone.isEnabled = false
                tvTaskTitle.setPaintFlags(tvTaskTitle.paintFlags or STRIKE_THRU_TEXT_FLAG)
                tvDescription.setPaintFlags(tvDescription.paintFlags or STRIKE_THRU_TEXT_FLAG)
                holder.itemView.isEnabled = false
            }


            btnDeleteIcon.setOnClickListener(){
                onTaskDelete?.invoke(taskModel[position], position)
            }
            cbTaskDone.setOnClickListener(){
                onTaskCheckBoxClicked?.invoke(taskModel[position], position)
            }

        }
        holder.itemView.setOnClickListener(){
            onTaskClick?.invoke(taskModel[position], position)
        }
    }

    override fun getItemCount(): Int {
        return taskModel.size
    }
}
