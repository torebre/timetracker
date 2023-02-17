package com.kjipo.timetracker

import android.content.Context
import androidx.room.Room
import com.kjipo.timetracker.database.AppDatabase


interface AppContainer {
    val taskRepository: TaskRepository
}


class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-database")
            // TODO Only here while developing
            .fallbackToDestructiveMigration()
            .build()
    }


    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(appDatabase)
    }



}