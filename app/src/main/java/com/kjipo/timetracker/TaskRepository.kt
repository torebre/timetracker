package com.kjipo.timetracker

import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.Task


interface TaskRepository {

    suspend fun getTasks(): List<Task>

    suspend fun updateTask(task: Task)

}


class TaskRepositoryImpl(private val appDatabase: AppDatabase): TaskRepository {

    override suspend fun getTasks(): List<Task> {
        return appDatabase.taskDao().getTasks()
    }

    override suspend fun updateTask(task: Task) {
        appDatabase.taskDao().updateTask(task)
    }

}