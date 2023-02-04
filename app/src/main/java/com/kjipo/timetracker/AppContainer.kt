package com.kjipo.timetracker

import android.content.Context
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.Project
import com.kjipo.timetracker.database.Task
import com.kjipo.timetracker.database.TimeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import java.time.Instant.now
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.launch


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