package com.kjipo.timetracker

import androidx.room.Transaction
import com.kjipo.timetracker.database.AppDatabase
import com.kjipo.timetracker.database.Task
import com.kjipo.timetracker.database.TaskWithTimeEntries
import com.kjipo.timetracker.database.TimeEntry
import java.time.Instant


interface TaskRepository {

    suspend fun getTaskWithTimeEntries(taskId: Long): TaskWithTimeEntries?
    suspend fun getTasks(): List<Task>

    suspend fun createTask(title: String = ""): Task

    suspend fun updateTask(task: Task)
    suspend fun getTasksWithTimeEntries(): List<TaskWithTimeEntries>

    suspend fun addTimeEntry(timeEntry: TimeEntry)

    suspend fun setStopForTimeEntry(timeEntryId: Long, stop: Instant)

    suspend fun getTimeEntry(timeEntryId: Long): TimeEntry?

    suspend fun updateTimeEntry(timeEntry: TimeEntry)
    suspend fun saveTask(taskId: Long, taskName: String)

}


class TaskRepositoryImpl(private val appDatabase: AppDatabase) : TaskRepository {

    override suspend fun getTaskWithTimeEntries(taskId: Long): TaskWithTimeEntries? {
        return appDatabase.taskDao().getTaskWithTimeEntries(taskId)
    }

    override suspend fun getTasks(): List<Task> {
        return appDatabase.taskDao().getTasks()
    }

    override suspend fun createTask(title: String): Task {
        val newTask = Task(0, title)
        appDatabase.taskDao().insertTask(newTask).also { newTask.taskId = it }

        return newTask
    }

    override suspend fun updateTask(task: Task) {
        appDatabase.taskDao().updateTask(task)
    }

    override suspend fun getTasksWithTimeEntries(): List<TaskWithTimeEntries> {
        return appDatabase.taskDao().getTasksWithTimeEntries()
    }

    override suspend fun addTimeEntry(timeEntry: TimeEntry) {
        appDatabase.timeEntryDao().insertTimeEntry(timeEntry)
    }

    override suspend fun setStopForTimeEntry(timeEntryId: Long, stop: Instant) {
        appDatabase.timeEntryDao().getTimeEntry(timeEntryId)?.let { timeEntry ->
            appDatabase.timeEntryDao().updateTimeEntry(timeEntry.copy(stop = stop))
        }
    }

    override suspend fun getTimeEntry(timeEntryId: Long): TimeEntry? {
        return appDatabase.timeEntryDao().getTimeEntry(timeEntryId)
    }

    override suspend fun updateTimeEntry(timeEntry: TimeEntry) {
        appDatabase.timeEntryDao().updateTimeEntry(timeEntry)
    }

    @Transaction
    override suspend fun saveTask(taskId: Long, taskName: String) {
        appDatabase.taskDao().getTask(taskId)?.let { task ->
            appDatabase.taskDao().updateTask(task.copy(title = taskName))
        }
    }


}