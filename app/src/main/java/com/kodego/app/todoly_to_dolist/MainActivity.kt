package com.kodego.app.todoly_to_dolist

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.kodego.app.todoly_to_dolist.databinding.ActivityMainBinding
import com.kodego.app.todoly_to_dolist.databinding.DialogAboutBinding
import com.kodego.app.todoly_to_dolist.databinding.DialogAddTaskBinding
import com.kodego.app.todoly_to_dolist.db.Task
import com.kodego.app.todoly_to_dolist.db.TodolyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var todolyDB: TodolyDatabase
    lateinit var taskAdapter: TaskAdapter
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var toolbar: MaterialToolbar
    lateinit var textView: TextView
    lateinit var font: Typeface

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Date
        var calendar: Calendar = Calendar.getInstance()
        var simpleDateFormat = SimpleDateFormat("EEEE, MMMM dd")
        var currentDate = simpleDateFormat.format(calendar.time)
        binding.tvDate.text = currentDate

        //initialized
        todolyDB = TodolyDatabase.invoke(this)

        viewTask()
        showAddTaskDialog()

        //setting the font of appbar title
        toolbar = binding.topAppBar
        textView = toolbar.getChildAt(0) as TextView
        font = ResourcesCompat.getFont(applicationContext, R.font.pacifico_regular)!!
        textView.typeface = font

        //appbar
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.about -> {
                    //show dialog_about_app
                    val dialog = Dialog(this)
                    val binding: DialogAboutBinding = DialogAboutBinding.inflate(layoutInflater)
                    dialog.setContentView(binding.root)
                    dialog.show()

                    //copy developers email to clipboard
                    binding.btnCopyEmail.setOnClickListener(){
                        var clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        var clip = ClipData.newPlainText("email", binding.txtDeveloperEmail.text)
                        clipboard.setPrimaryClip(clip)

                        Toast.makeText(this,"'${binding.txtDeveloperEmail.text}' \ncopied!", Toast.LENGTH_SHORT).show()
                    }

                    binding.btnAboutDismiss.setOnClickListener(){
                        dialog.dismiss()
                    }
                    true
                }
                else -> false
            }
        }

        binding.btnClearTask.setOnClickListener {
            showClearAllTaskDialog()
        }
        //swipe down to refresh recyclerview
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false

            viewTask()
            taskAdapter.notifyDataSetChanged()
        }
    }

    private fun save(task: Task){
        GlobalScope.launch(Dispatchers.IO) {
            todolyDB.getTasks().addTask(task)
            viewTask()
        }
    }

    private  fun delete(task: Task){
        GlobalScope.launch(Dispatchers.IO) {
            todolyDB.getTasks().deleteTask(task.id)
            viewTask()
        }
    }

    private fun viewTask(){
        lateinit var task: MutableList<Task>
        GlobalScope.launch(Dispatchers.IO) {
            task = todolyDB.getTasks().getAllTask()

            withContext(Dispatchers.Main){
                taskAdapter = TaskAdapter(task)
                binding.recyclerViewTasks.adapter = taskAdapter
                binding.recyclerViewTasks.layoutManager = LinearLayoutManager(applicationContext)

                showDeleteDialog()

                taskAdapter.onTaskClick={ item: Task, position: Int ->
                    val intent = Intent(applicationContext, EditTaskActivity::class.java)
                    intent.putExtra("id", item.id)
                    intent.putExtra("task", item.taskTitle)
                    intent.putExtra("description", item.description)
                    startActivity(intent)
                }

                if (task.isNotEmpty()) {
                    binding.swipeRefreshLayout.visibility = View.VISIBLE
                    binding.btnClearTask.visibility = View.VISIBLE
                    binding.txtTaskTitle.text = "All Tasks"
                    binding.txtNoTaskDesc.visibility = View.GONE

                } else {
                    binding.swipeRefreshLayout.visibility = View.GONE
                    binding.btnClearTask.visibility = View.GONE
                    binding.txtTaskTitle.text = "No Task"
                    binding.txtNoTaskDesc.visibility = View.VISIBLE
                }

                taskAdapter.onTaskCheckBoxClicked={ item: Task, position: Int ->
                    taskIsDone(item.id)
                }
            }
        }
    }

    private fun showAddTaskDialog(){
        binding.fabAddTask.setOnClickListener {
            val dialog = Dialog(this)
            val binding: DialogAddTaskBinding = DialogAddTaskBinding.inflate(layoutInflater)
            dialog.setContentView(binding.root)
            dialog.show()

            binding.btnAddTask.setOnClickListener {
                try{
                    if(binding.etEnterTaskTitle.text?.isEmpty() == true){
                        Toast.makeText(applicationContext, "Task field is required.", Toast.LENGTH_SHORT).show()

                    }else{
                        var taskName: String = binding.etEnterTaskTitle.text.toString()
                        var description: String = binding.etEnterDescription.text.toString()
                        var isTaskDone: Boolean = false
                        val task = Task(taskName, description, isTaskDone)
                        save(task)

                        Toast.makeText(applicationContext, "Successfully Saved!", Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    }
                }catch (e: Exception){
                    Toast.makeText(applicationContext, "$e", Toast.LENGTH_LONG).show()
                }

            }
            binding.btnCancelAddTask.setOnClickListener(){
                dialog.dismiss()
            }
        }
    }

    private fun showDeleteDialog(){
        taskAdapter.onTaskDelete = { item: Task, position: Int ->
            AlertDialog.Builder(this).setMessage("Do you want to delete this task? \n'${item.taskTitle}'")
                .setPositiveButton("Delete"){ dialog, item2 ->
                    try {
                        delete(item)
                        taskAdapter.taskModel.removeAt(position)
                        taskAdapter.notifyDataSetChanged()
                        Toast.makeText(applicationContext, "Successful Deleted!", Toast.LENGTH_SHORT).show()
                    }catch (e: Exception){
                        Toast.makeText(applicationContext, "$e", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancel"){dialog, item ->
                }.show()
        }
    }

    private fun showClearAllTaskDialog(){
        AlertDialog.Builder(this).setMessage("Do you want to clear all tasks?")
            .setPositiveButton("Yes"){ dialog, item2 ->
                try {
                    GlobalScope.launch(Dispatchers.IO) {
                        todolyDB.getTasks().clearAllTask()
                        viewTask()
                    }
                    taskAdapter.taskModel.clear()
                    taskAdapter.notifyDataSetChanged()
                    Toast.makeText(applicationContext, "All task cleared!", Toast.LENGTH_SHORT).show()
                }catch (e: Exception){
                    Toast.makeText(applicationContext, "$e", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel"){dialog, item ->
            }.show()
    }

    private fun taskIsDone(id: Int){
        var isTaskDone: Boolean = true
       GlobalScope.launch(Dispatchers.IO) {
           todolyDB.getTasks().taskIsDone(id, isTaskDone)
           viewTask()
       }
    }
}