package com.kodego.app.todoly_to_dolist.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert
    fun addTask(task: Task)

    @Query("SELECT * FROM Task")
    fun getAllTask(): MutableList<Task>

    @Query("DELETE FROM Task WHERE id = :id")
    fun deleteTask(id: Int)

    @Query("UPDATE Task SET taskTitle = :taskTitle, description = :description WHERE id = :id")
    fun updateTask(id: Int, taskTitle: String, description: String)

    @Query("UPDATE Task SET isTaskDone = :isTaskDone WHERE id = :id")
    fun taskIsDone(id: Int, isTaskDone: Boolean)

    @Query("DELETE FROM Task")
    fun clearAllTask()
}