package com.kjipo.timetracker

import androidx.room.Transaction
import com.kjipo.timetracker.database.*
import java.time.Instant


interface TaskRepository {

    suspend fun getTaskWithTimeEntries(taskId: Long): TaskWithTimeEntries?
    suspend fun getTasks(): List<Task>

    suspend fun createTask(title: String = ""): Task

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    suspend fun getTasksWithTimeEntries(): List<TaskWithTimeEntries>

    suspend fun addTimeEntry(timeEntry: TimeEntry)

    suspend fun setStopForTimeEntry(timeEntryId: Long, stop: Instant)

    suspend fun getTimeEntry(timeEntryId: Long): TimeEntry?

    suspend fun updateTimeEntry(timeEntry: TimeEntry)
    suspend fun saveTask(taskId: Long, taskName: String)

    suspend fun getTags(): List<Tag>

    suspend fun getTag(id: Long): Tag?

    suspend fun insertTag(tag: Tag): Long
    suspend fun updateTag(tag: Tag)

    suspend fun deleteTag(tag: Tag)

    suspend fun addTag(taskId: Long, tagId: Long)
    suspend fun removeTag(taskId: Long, tagId: Long)

    suspend fun getTasksForTag(tagId: Long): List<TagWithTaskEntries>

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

    override suspend fun deleteTask(task: Task) {
        appDatabase.taskDao().deleteTask(task)
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

    override suspend fun getTags(): List<Tag> {
        return appDatabase.tagDao().getTags()
    }

    override suspend fun getTag(id: Long): Tag? {
        return appDatabase.tagDao().getTag(id)
    }

    override suspend fun insertTag(tag: Tag): Long {
        return appDatabase.tagDao().insertTag(tag)
    }

    override suspend fun updateTag(tag: Tag) {
        appDatabase.tagDao().updateTag(tag)
    }

    override suspend fun deleteTag(tag: Tag) {
        appDatabase.tagDao().deleteTag(tag)
    }

    override suspend fun addTag(taskId: Long, tagId: Long) {
        appDatabase.taskDao().insertTaskAndTagCrossRef(TagTasksCrossRef(tagId, taskId))
    }

    override suspend fun removeTag(taskId: Long, tagId: Long) {
        appDatabase.taskDao().removeTaskAndTagCrossRef(TagTasksCrossRef(tagId, taskId))
    }

    override suspend fun getTasksForTag(tagId: Long): List<TagWithTaskEntries> {
        return appDatabase.tagDao().getTasksForTag(tagId)
    }


}