package com.kodego.app.todoly_to_dolist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kodego.app.todoly_to_dolist.databinding.ActivityEditTaskBinding
import com.kodego.app.todoly_to_dolist.db.TodolyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class EditTaskActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditTaskBinding
    lateinit var todolyDB: TodolyDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        todolyDB = TodolyDatabase.invoke(this)

        var id: Int = intent.getIntExtra("id", 0)
        var taskName: String? = intent.getStringExtra("task")
        var description: String? = intent.getStringExtra("description")
        binding.etEnterNewTaskTitle.setText(taskName)
        binding.etEnterNewDescription.setText(description)

        binding.btnUpdateTask.setOnClickListener(){
            try{
                if(binding.etEnterNewTaskTitle.text?.isEmpty() == true){
                    Toast.makeText(applicationContext, "Task field is required.", Toast.LENGTH_SHORT).show()

                }else{
                    var newTask: String = binding.etEnterNewTaskTitle.text.toString()
                    var newDescription: String = binding.etEnterNewDescription.text.toString()

                    GlobalScope.launch(Dispatchers.IO) {
                        todolyDB.getTasks().updateTask(id, newTask, newDescription)
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Successfully Updated!", Toast.LENGTH_LONG).show()
                        }
                    }
                    finish()
                }

            }catch (e: Exception){
                Toast.makeText(applicationContext, "$e", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnCancelEditTask.setOnClickListener(){
            showDiscardDialog()
        }
    }

    override fun onBackPressed() {
        showDiscardDialog()
    }

    private fun showDiscardDialog(){

        AlertDialog.Builder(this).setMessage("Discard changes?")
            .setPositiveButton("Discard"){ dialog, item2 ->
                finish()
            }
            .setNegativeButton("Cancel"){dialog, item ->
            }.show()
    }
}