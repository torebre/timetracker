package com.kjipo.timetracker

import android.content.Context
import androidx.room.Room
import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.MIGRATION_7_8
import com.kjipo.timetracker.database.TaskRepository
import com.kjipo.timetracker.database.TaskRepositoryImpl


interface AppContainer {
    val appDatabase: AppDatabase

    val taskRepository: TaskRepository
}


class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    override val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-database")
//            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION_7_8)
            .build()
    }


    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(appDatabase)
    }


}