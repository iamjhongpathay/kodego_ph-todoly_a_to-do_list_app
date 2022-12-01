package com.kodego.app.todoly_to_dolist.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Task::class],
    version = 1
)

abstract class TodolyDatabase: RoomDatabase() {
    abstract fun getTasks():TaskDao

    companion object{
        @Volatile
        private var instance: TodolyDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance?: synchronized(LOCK){
            instance?: buildDatabase(context).also{
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            TodolyDatabase::class.java,
            "todoly")
            .build()
    }
}