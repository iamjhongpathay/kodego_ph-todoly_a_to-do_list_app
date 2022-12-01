package com.kodego.app.todoly_to_dolist.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    var taskTitle: String,
    var description: String,
    var isTaskDone: Boolean = false
    ){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    }